package acc.capstone;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Locale;
import java.util.Optional;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/app")
public class TranscriptionController {

	@Autowired
	PostRepository postRepository;

	@Autowired
	SessionManager sessionManager;

	@Autowired
	TranscriptionRepository transcriptionRepository;

	@Autowired
	ProfileRepository profileRepository;

	@Autowired
	UserRepository userRepository;

	private static final String LOCALE_ATTR = "language";

	@GetMapping("/transcription/{postId}")
	public String transcription(@PathVariable int postId, Model model, RedirectAttributes redirect,
			HttpServletRequest request) {

		Locale whichLocale = localeResolverforTranscriptionController().resolveLocale(request);
		if (isLoggedIn()) {
			if (whichLocale.equals(Locale.ENGLISH)) {
				redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in user?");
			}

			if (whichLocale.equals(Locale.FRENCH)) {
				redirect.addFlashAttribute("message",
						"Pourquoi devriez-vous être autorisé à faire comme utilisateur non connecté?");
			}
			return "redirect:/app/login";
		}
		Optional<Post> optionalPost = postRepository.findById(postId);
		if (optionalPost.isPresent()) {
			if (whosePotentialTranscription(optionalPost)) {
				if (whichLocale.equals(Locale.ENGLISH)) {
					redirect.addFlashAttribute("failure", "You are not allowed to transcribe your own post");
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					redirect.addFlashAttribute("failure", "Vous n'êtes pas autorisé à transcrire votre propre message");
				}
				return "redirect:/app/timeline";
			}

			if (isPostTranscribed(optionalPost)) {
				if (whichLocale.equals(Locale.ENGLISH)) {
					redirect.addFlashAttribute("failure", "The post in question has already been transcribed");
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					redirect.addFlashAttribute("failure", "Le post en question a déjà été transcrit");
				}

				return "redirect:/app/timeline";
			}

			if (whichLocale.equals(Locale.ENGLISH) && optionalPost.get().getPostLanguage().equals(Language.FRENCH)) {
				redirect.addFlashAttribute("languageIssue",
						"Your working language conflicts with the language of the post that you want to transcribe; either"
								+ " change your mind or change your working language");
				return "redirect:/app/timeline";
			}

			if (whichLocale.equals(Locale.FRENCH) && optionalPost.get().getPostLanguage().equals(Language.ENGLISH)) {
				redirect.addFlashAttribute("languageIssue",
						"Votre langue actuelle se bat contre la langue du post que vous voulez à transcrire; changez"
								+ " votre idée ou votre langue actuelle");
				return "redirect:/app/timeline";
			}

			model.addAttribute("post", optionalPost.get());
			Transcription transcription = new Transcription();
			model.addAttribute("transcription", transcription);
			return "transcription";
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	@Transactional
	@PostMapping("/transcription/{postId}")
	public String transcription(@PathVariable int postId, @Valid Transcription transcription, Errors errors,
			Model model, RedirectAttributes redirect, HttpServletRequest request) {

		Locale whichLocale = localeResolverforTranscriptionController().resolveLocale(request);
		Optional<Post> optionalPost = postRepository.findById(postId);
		if (optionalPost.isPresent()) {

			int transcriptionLength = transcription.getContent().length();
			int postLength = optionalPost.get().getContent().length();
			if (transcriptionLength < (postLength -= (postLength / 4))) {
				if (whichLocale.equals(Locale.ENGLISH)) {
					errors.rejectValue("content", "bad value",
							"please increase the transcription's length so that it is closer to that of the post");
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					errors.rejectValue("content", "bad value",
							"veuillez augmenter la longueur de la transcription pour qu'elle soit plus "
									+ "proche de celle du post");
				}
			}

			if (transcriptionLength > (postLength += (postLength / 4))) {

				if (whichLocale.equals(Locale.ENGLISH)) {
					errors.rejectValue("content", "bad value",
							"please shorten the transcription's length so that it is closer to that of the post");
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					errors.rejectValue("content", "bad value",
							"veuillez raccourcir la longueur de la transcription pour qu'elle soit plus proche "
									+ "de celle du post");
				}
			}

			if (errors.hasErrors()) {
				model.addAttribute("post", optionalPost.get());
				model.addAttribute("transcription", transcription);
				return "transcription";
			}

			else {

				Post post = optionalPost.get();
				transcription.setName("Transcription of " + post.getName());

				// Optional<Profile> optionalProfile =
				// profileRepository.findById(sessionManager.getLoggedInUser().getId());
				Profile profile = this.profileRepository.findByUser(this.sessionManager.getLoggedInUser());
				transcription.setProfile(profile);
				transcription.setAuthor(sessionManager.getLoggedInUser());
				transcription.setTranscriptionDate(LocalDate.now());
				transcription.setTranscriptionTime(LocalTime.now());

				if (whichLocale.equals(Locale.ENGLISH))
					transcription.setName("Transcription of " + post.getName());
				if (whichLocale.equals(Locale.FRENCH))
					transcription.setName("Transcription de " + post.getName());
				transcription.setEditableByAuthor(true);
				transcription.setDeletableByAuthor(true);
				transcription.setCommentableByAuthor(false);
				transcription.setHasComments(false);
				transcription.setHasOneComment(false);
				transcription.setPost(post);
				transcription.setTranscriptionLanguage(post.getPostLanguage());
				transcription.setNumTranscriptionComments(0);

				post.setEditableByAuthor(false);
				post.setDeletableByAuthor(false);
				post.setHasTranscription(true);
				// post.setTranscription(transcription);
				// this.postRepository.save(post);

				// profile of author of transcription
				Profile authorProfile = transcription.getAuthor().getProfile();
				int authorProfileTranscriptions = authorProfile.getNumTranscriptions();
				authorProfileTranscriptions++;
				authorProfile.setNumTranscriptions(authorProfileTranscriptions);

				if (authorProfileTranscriptions == 1) {
					authorProfile.setHasMadeOneTranscription(true);
					authorProfile.setHasMadeTranscriptions(true);
				}

				if (authorProfileTranscriptions > 1) {
					authorProfile.setHasMadeOneTranscription(false);
				}
				// authorProfile.getTranscriptions().add(transcription);
				this.profileRepository.save(authorProfile);

				if (whichLocale.equals(Locale.ENGLISH)) {

				}

				if (whichLocale.equals(Locale.FRENCH)) {

				}
				this.transcriptionRepository.save(transcription);

				if (whichLocale.equals(Locale.ENGLISH)) {
					redirect.addFlashAttribute("success",
							"Scroll accordingly to see your new transcription that is titled " + transcription.getName()
									+ ", " + sessionManager.getLoggedInUser().getUsername());
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					redirect.addFlashAttribute("success",
							"Faites défiler en conséquence pour voir votre nouvelle transcription intitulée"
									+ transcription.getName() + ", " + sessionManager.getLoggedInUser().getUsername());
				}

				return "redirect:/app/timeline";
			}
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	@GetMapping("/editTranscription/{transcriptionId}")
	public String editTranscription(@PathVariable int transcriptionId, Model model, RedirectAttributes redirect,
			HttpServletRequest request) {

		Locale whichLocale = localeResolverforTranscriptionController().resolveLocale(request);
		if (isLoggedIn()) {
			if (whichLocale.equals(Locale.ENGLISH)) {
				redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in user?");
			}

			if (whichLocale.equals(Locale.FRENCH)) {
				redirect.addFlashAttribute("message",
						"Pourquoi devriez-vous être autorisé à faire comme utilisateur non connecté?");
			}
			return "redirect:/app/login";
		}

		Optional<Transcription> optionalTranscription = transcriptionRepository.findById(transcriptionId);
		if (optionalTranscription.isPresent()) {

			if (whoseTranscription(optionalTranscription)) {
				if (whichLocale.equals(Locale.ENGLISH)) {
					redirect.addFlashAttribute("failure", "That is not your transcription to tinker with");
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					redirect.addFlashAttribute("failure", "Vous ne pouvez pas interagir avec cette transcription");
				}

				return "redirect:/app/timeline";
			}
			if (isTranscriptionEditable(optionalTranscription)) {
				if (whichLocale.equals(Locale.ENGLISH)) {
					redirect.addFlashAttribute("failure", "You can't at this time edit the transcription in question");
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					redirect.addFlashAttribute("failure",
							"Vous ne pouvez pas pour l'instant éditer la transcription en question");
				}

				return "redirect:/app/timeline";
			}

			if (whichLocale.equals(Locale.ENGLISH)
					&& optionalTranscription.get().getTranscriptionLanguage().equals(Language.FRENCH)) {
				redirect.addFlashAttribute("languageIssue",
						"Your working language conflicts with the language of the transcription "
								+ "that you want to edit; either change your mind or change your working language");
				return "redirect:/app/timeline";
			}

			if (whichLocale.equals(Locale.FRENCH)
					&& optionalTranscription.get().getTranscriptionLanguage().equals(Language.ENGLISH)) {
				redirect.addFlashAttribute("languageIssue",
						"Votre langue actuelle se bat contre la langue de la transcription que vous voulez à éditer;"
								+ " changez votre idée ou votre langue actuelle");
				return "redirect:/app/timeline";
			}

			model.addAttribute("transcription", optionalTranscription.get());
			model.addAttribute("post", optionalTranscription.get().getPost());
			return "editTranscription";
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	@PostMapping("/editTranscription/{transcriptionId}")
	public String editTranscription(@PathVariable int transcriptionId, @Valid Transcription transcription, Model model,
			Errors errors, RedirectAttributes redirect, HttpServletRequest request) {

		Locale whichLocale = localeResolverforTranscriptionController().resolveLocale(request);
		Optional<Transcription> optionalTranscription = transcriptionRepository.findById(transcriptionId);
		if (optionalTranscription.isPresent()) {

			transcription.setId(transcriptionId);

			int transcriptionLength = transcription.getContent().length();
			int postLength = optionalTranscription.get().getPost().getContent().length();
			if (transcriptionLength < (postLength -= (postLength / 4))) {
				if (whichLocale.equals(Locale.ENGLISH)) {
					errors.rejectValue("content", "bad value",
							"please increase the transcription's length so that it is closer to that of the post");
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					errors.rejectValue("content", "bad value",
							"veuillez augmenter la longueur de la transcription pour qu'elle soit plus proche "
									+ "de celle du post");
				}

			}
			if (transcriptionLength > (postLength += (postLength / 4))) {
				if (whichLocale.equals(Locale.ENGLISH)) {
					errors.rejectValue("content", "bad value",
							"please shorten the transcription's length so that it is closer to that of the post");
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					errors.rejectValue("content", "bad value",
							"veuillez raccourcir la longueur de la transcription pour qu'elle soit plus proche"
									+ " de celle du post");
				}

			}

			if (errors.hasErrors()) {
				model.addAttribute("post", optionalTranscription.get().getPost());
				return "editTranscription";
			} else {

				transcription.setId(optionalTranscription.get().getId());
				transcription.setAuthor(sessionManager.getLoggedInUser());
				transcription.setTranscriptionDate(LocalDate.now());
				transcription.setTranscriptionTime(LocalTime.now());
				transcription.setName("Transcription of " + optionalTranscription.get().getPost().getName());
				transcription.setEditableByAuthor(true);
				transcription.setDeletableByAuthor(true);
				transcription.setCommentableByAuthor(false);
				transcription.setHasComments(false);
				transcription.setHasOneComment(false);
				transcription.setPost(optionalTranscription.get().getPost());
				transcription.setNumTranscriptionComments(optionalTranscription.get().getNumTranscriptionComments());

				Profile authorProfile = optionalTranscription.get().getProfile();
				transcription.setProfile(authorProfile);

				Post post = optionalTranscription.get().getPost();
				post.setEditableByAuthor(false);
				post.setHasTranscription(true);
				post.setTranscription(transcription);

				this.transcriptionRepository.delete(optionalTranscription.get());
				this.transcriptionRepository.save(transcription);

				if (post.getProfile().getLanguages().size() == 2) {
					if (post.getPostLanguage().equals(post.getProfile().getLanguages().get(0))) {
						transcription.setTranscriptionLanguage(post.getProfile().getLanguages().get(0));
					}

					if (post.getPostLanguage().equals(post.getProfile().getLanguages().get(1))) {
						transcription.setTranscriptionLanguage(post.getProfile().getLanguages().get(1));
					}
				}

				if (post.getProfile().getLanguages().size() == 1) {
					transcription.setTranscriptionLanguage(post.getPostLanguage());
				}

				if (whichLocale.equals(Locale.ENGLISH)) {
					redirect.addFlashAttribute("success",
							"Scroll accordingly to see the edited version of your transcription that is titled "
									+ transcription.getName() + ", " + sessionManager.getLoggedInUser().getUsername());
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					redirect.addFlashAttribute("success",
							"Faites défiler en conséquence pour voir la version modifiée de votre transcription qui est"
									+ " intitulée " + transcription.getName() + ", "
									+ sessionManager.getLoggedInUser().getUsername());
				}

				return "redirect:/app/timeline";
			}
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	@GetMapping("/deleteTranscription/{transcriptionId}")
	public String deleteTranscription(@PathVariable int transcriptionId, RedirectAttributes redirect,
			HttpServletRequest request) {

		Locale whichLocale = localeResolverforTranscriptionController().resolveLocale(request);

		if (isLoggedIn()) {
			if (whichLocale.equals(Locale.ENGLISH)) {
				redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in user?");
			}

			if (whichLocale.equals(Locale.FRENCH)) {
				redirect.addFlashAttribute("message",
						"Pourquoi devriez-vous être autorisé à faire comme utilisateur non connecté?");
			}
			return "redirect:/app/login";
		}

		Optional<Transcription> optionalTranscription = this.transcriptionRepository.findById(transcriptionId);
		if (optionalTranscription.isPresent()) {
			if (whoseTranscription(optionalTranscription)) {
				if (whichLocale.equals(Locale.ENGLISH)) {
					redirect.addFlashAttribute("failure", "This is not your transcription to tinker with");
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					redirect.addFlashAttribute("failure", "Vous ne pouvez pas interférer avec cette transcription");
				}

				return "redirect:/app/timeline";
			}
		}

		if (isTranscriptionDeletable(optionalTranscription)) {
			if (whichLocale.equals(Locale.ENGLISH)) {
				redirect.addFlashAttribute("failure", "You can't at this time delete the transcription in question");
			}

			if (whichLocale.equals(Locale.FRENCH)) {
				redirect.addFlashAttribute("failure",
						"Vous ne pouvez pas pour l'instant supprimer la transcription en question");
			}

			return "redirect:/app/timeline";
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	@Transactional
	@PostMapping("/deleteTranscription/{transcriptionId}")
	public String deleteTranscription1(@PathVariable int transcriptionId, RedirectAttributes redirect,
				HttpServletRequest request) {

		Locale whichLocale = localeResolverforTranscriptionController().resolveLocale(request);
		Optional<Transcription> optionalTranscription = transcriptionRepository.findById(transcriptionId);
		if (optionalTranscription.isPresent()) {

			this.transcriptionRepository.delete(optionalTranscription.get());

			Post post = optionalTranscription.get().getPost();
			post.setHasTranscription(false);
			if (post.isHasComments() == false && post.isHasOneComment() == false) {
				post.setEditableByAuthor(true);
				post.setDeletableByAuthor(true);
			}

			// this.postRepository.save(post);

			Profile authorProfile = optionalTranscription.get().getAuthor().getProfile();
			int authorProfileTranscriptions = authorProfile.getNumTranscriptions();
			authorProfileTranscriptions--;
			authorProfile.setNumTranscriptions(authorProfileTranscriptions);

			if (authorProfileTranscriptions == 1) {
				authorProfile.setHasMadeOneTranscription(true);
			}

			if (authorProfileTranscriptions == 0) {
				authorProfile.setHasMadeOneTranscription(false);
				authorProfile.setHasMadeTranscriptions(false);
			}

			// this.profileRepository.save(authorProfile);

			if (whichLocale.equals(Locale.ENGLISH)) {
				redirect.addFlashAttribute("success",
						"You've successfully deleted the transcription that was called "
								+ optionalTranscription.get().getName() + ", "
								+ sessionManager.getLoggedInUser().getUsername() + ". So don't scroll for it.");
			}
			
			if (whichLocale.equals(Locale.FRENCH)) {
				redirect.addFlashAttribute("success",
						"Vous avez supprimé avec succès la transcription qui s'appelait "
								+ optionalTranscription.get().getName() + ", "
								+ sessionManager.getLoggedInUser().getUsername() + ". Alors ne faites pas défiler pour cela.");
			}

			return "redirect:/app/timeline";
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	private boolean isLoggedIn() {
		return !sessionManager.isLoggedIn();
	}

	private boolean whosePotentialTranscription(Optional<Post> optionalPost) {
		return optionalPost.get().getAuthor().getId() == sessionManager.getLoggedInUser().getId();
	}

	private boolean isPostTranscribed(Optional<Post> optionalPost) {
		return optionalPost.get().isHasTranscription() == true;
	}

	private boolean whoseTranscription(Optional<Transcription> optionalTranscription) {
		return optionalTranscription.get().getAuthor().getId() != sessionManager.getLoggedInUser().getId();
	}

	private boolean isTranscriptionEditable(Optional<Transcription> optionalTranscription) {
		return optionalTranscription.get().isEditableByAuthor() == false
				&& optionalTranscription.get().getAuthor().getId() == sessionManager.getLoggedInUser().getId();
	}

	private boolean isTranscriptionDeletable(Optional<Transcription> optionalTranscription) {
		return optionalTranscription.get().isDeletableByAuthor() == false
				&& optionalTranscription.get().getAuthor().getId() == sessionManager.getLoggedInUser().getId();
	}

	@Bean
	public LocaleResolver localeResolverforTranscriptionController() {
		SessionLocaleResolver r = new SessionLocaleResolver();
		r.setLocaleAttributeName(LOCALE_ATTR);
		return r;
	}
}
