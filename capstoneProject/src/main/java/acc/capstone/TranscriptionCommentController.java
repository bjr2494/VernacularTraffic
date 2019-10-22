package acc.capstone;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

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
public class TranscriptionCommentController {

	@Autowired
	TranscriptionRepository transcriptionRepository;

	@Autowired
	SessionManager sessionManager;

	@Autowired
	TranscriptionCommentRepository transcriptionCommentRepository;

	@Autowired
	PostRepository postRepository;

	@Autowired
	ProfileRepository profileRepository;

	private static final String LOCALE_ATTR = "language";

	@GetMapping("/transcriptionComment/{transcriptionId}")
	public String transcriptionComment(@PathVariable int transcriptionId, Model model, RedirectAttributes redirect,
			HttpServletRequest request) {

		Locale whichLocale = localeResolverforTranscriptionCommentController().resolveLocale(request);
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

			if (commentOnOwnTranscription(optionalTranscription)) {

				if (whichLocale.equals(Locale.ENGLISH)) {
					redirect.addFlashAttribute("failure",
							"You can't at this time make a comment on your own transcription");
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					redirect.addFlashAttribute("failure",
							"Vous ne pouvez pas pour le moment faire de commentaire sur votre propre transcription");
				}
				return "redirect:/app/timeline";
			}

			if (whichLocale.equals(Locale.ENGLISH)
					&& optionalTranscription.get().getTranscriptionLanguage().equals(Language.FRENCH)) {
				redirect.addFlashAttribute("languageIssue",
						"Your working language conflicts with the language of the transcription that you want to comment on;"
								+ " either change your mind or change your working language");
				return "redirect:/app/timeline";
			}

			if (whichLocale.equals(Locale.FRENCH)
					&& optionalTranscription.get().getTranscriptionLanguage().equals(Language.ENGLISH)) {
				redirect.addFlashAttribute("languageIssue",
						"Votre langue actuelle se bat contre la langue de la transcription sur laquelle vous voulez faire"
								+ " un commentaire; changez votre idée ou votre langue actuelle");
				return "redirect:/app/timeline";

			}

			if (optionalTranscription.get().getTranscriptionLanguage().equals(Language.FRENCH))
				model.addAttribute("extraLetters", "");

			// model.addAttribute("loggedInUser", sessionManager.getLoggedInUser());
			model.addAttribute("transcription", optionalTranscription.get());
			model.addAttribute("post", optionalTranscription.get().getPost());
			model.addAttribute("transcriptionComment", new TranscriptionComment());

			return "transcriptionComment";
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	@Transactional
	@PostMapping("/transcriptionComment/{transcriptionId}")
	public String transcriptionComment(@PathVariable int transcriptionId,
			@Valid TranscriptionComment transcriptionComment, Errors errors, Model model, RedirectAttributes redirect,
			HttpServletRequest request) {

		Locale whichLocale = localeResolverforTranscriptionCommentController().resolveLocale(request);
		Optional<Transcription> optionalTranscription = transcriptionRepository.findById(transcriptionId);
		if (optionalTranscription.isPresent()) {

			if (transcriptionComment.getContent().length() < 4) {
				if (whichLocale.equals(Locale.ENGLISH)) {
					errors.rejectValue("content", "bad value",
							"Please increase the comment's length in order to satisfy our requirement");
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					errors.rejectValue("content", "bad value",
							"Veuillez augmenter la longueur du commentaire afin de satisfaire notre exigence");
				}

			}
			if (transcriptionComment.getContent().length() > 60) {
				if (whichLocale.equals(Locale.ENGLISH)) {
					errors.rejectValue("content", "bad value",
							"Please shorten the comment's length in order to satisfy our requirement");
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					errors.rejectValue("content", "bad value",
							"Veuillez raccourcir la longueur du commentaire afin de satisfaire notre exigence");
				}

			}
			if (errors.hasErrors()) {
				// model.addAttribute("loggedInUser", sessionManager.getLoggedInUser());
				model.addAttribute("post", optionalTranscription.get().getPost());
				model.addAttribute("transcription", optionalTranscription.get());
				if (optionalTranscription.get().getTranscriptionLanguage().equals(Language.FRENCH))
					model.addAttribute("extraLetters", "");
				// model.addAttribute("transcriptionComment", transcriptionComment);
				return "transcriptionComment";
			} else {
				Transcription transcription = optionalTranscription.get();

				int numTranscriptionComments = transcription.getNumTranscriptionComments();
				numTranscriptionComments++;
				transcription.setNumTranscriptionComments(numTranscriptionComments);

				if (numTranscriptionComments == 1) {
					transcription.setDeletableByAuthor(false);
					transcription.setEditableByAuthor(false);
					transcription.setCommentableByAuthor(true);
					transcription.setHasComments(true);
					transcription.setHasOneComment(true);
				}

				if (numTranscriptionComments > 1) {
					transcription.setHasOneComment(false);
				}

				// this.transcriptionRepository.save(transcription);

				transcriptionComment.setTranscription(transcription);
				transcriptionComment.setAuthor(sessionManager.getLoggedInUser());
				transcriptionComment.setCommentDate(LocalDate.now());
				transcriptionComment.setCommentTime(LocalTime.now());
				Profile profile = this.profileRepository.findByUser(this.sessionManager.getLoggedInUser());
				transcriptionComment.setProfile(profile);
				transcriptionComment.setDeletableByAuthor(true);
				transcriptionComment.setTranscriptionCommentLanguage(transcription.getTranscriptionLanguage());

				for (TranscriptionComment tc : this.transcriptionCommentRepository
						.findAllByTranscriptionId(optionalTranscription.get().getId())) {
					if (tc.getCommentDate().isBefore(transcriptionComment.getCommentDate())) {
						tc.setDeletableByAuthor(false);
						// this.transcriptionCommentRepository.save(tc);
					}
					if (tc.getCommentDate().equals(transcriptionComment.getCommentDate())) {
						if (tc.getCommentTime().isBefore(transcriptionComment.getCommentTime())) {
							tc.setDeletableByAuthor(false);
							// this.transcriptionCommentRepository.save(tc);
						}
					}
				}

				// profile of the author of the transcription comment
				Profile authorProfile = transcriptionComment.getAuthor().getProfile();
				int authorProfileTranscriptionComments = authorProfile.getNumTranscriptionComments();
				authorProfileTranscriptionComments++;
				authorProfile.setNumTranscriptionComments(authorProfileTranscriptionComments);

				if (authorProfileTranscriptionComments == 1) {
					authorProfile.setHasMadeTranscriptionComments(true);
					authorProfile.setHasMadeOneTranscriptionComment(true);
				}

				if (authorProfileTranscriptionComments > 1) {
					authorProfile.setHasMadeOneTranscriptionComment(false);
				}

				this.transcriptionCommentRepository.save(transcriptionComment);
				// authorProfile.getTranscriptionComments().add(transcriptionComment);
				this.profileRepository.save(authorProfile);

				if (whichLocale.equals(Locale.ENGLISH)) {
					redirect.addFlashAttribute("success",
							"Scroll accordingly to see your comment on the transcription called "
									+ transcription.getName() + ", " + sessionManager.getLoggedInUser().getUsername());
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					redirect.addFlashAttribute("success",
							"Faites défiler en conséquence pour voir votre commentaire sur la transcription appelée "
									+ transcription.getName() + ", " + sessionManager.getLoggedInUser().getUsername());
				}

				return "redirect:/app/timeline";
			}
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	@GetMapping("/deleteTranscriptionComment/{transcriptionCommentId}")
	public String deleteTranscriptionComment(@PathVariable int transcriptionCommentId, RedirectAttributes redirect,
			HttpServletRequest request) {
		Locale whichLocale = localeResolverforTranscriptionCommentController().resolveLocale(request);
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
		
		Optional<TranscriptionComment> optionalTranscriptionComment = this.transcriptionCommentRepository
				.findById(transcriptionCommentId);
		if (optionalTranscriptionComment.isPresent()) {
			if (isTranscriptionCommentDeletable(optionalTranscriptionComment)) {
				if (whichLocale.equals(Locale.ENGLISH)) {
					redirect.addFlashAttribute("failure",
							"You can't at this time delete a transcription comment from the post in question");
				}
				
				if (whichLocale.equals(Locale.FRENCH)) {
					redirect.addFlashAttribute("failure",
							"Vous ne pouvez pas actuellement supprimer un commentaire de transcription du post en question.");
				}

				return "redirect:/app/timeline";
			}
			
			if (whoseTranscriptionComment(optionalTranscriptionComment)) {
				
				if (whichLocale.equals(Locale.ENGLISH)) {
					redirect.addFlashAttribute("failure", "This is not your transcription comment to tinker with");
				}
				
				if (whichLocale.equals(Locale.FRENCH)) {
					redirect.addFlashAttribute("failure", 
							"Vous ne pouvez pas interférer avec ce commentaire de transcription");
				}

				return "redirect:/app/timeline";
			}
			
			if (whichLocale.equals(Locale.ENGLISH)) {
				redirect.addFlashAttribute("failure", "Please do not try to delete a transcription comment in this way");
			}

			if (whichLocale.equals(Locale.FRENCH)) {
				redirect.addFlashAttribute("failure",
						"N'essayez pas de supprimer un commentaire de transcription de cette façon");
			}
			return "redirect:/app/timeline";
		}
		else throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		
	
	}

	@Transactional
	@PostMapping("/deleteTranscriptionComment/{transcriptionCommentId}")
	public String deleteTranscriptionComment1(@PathVariable int transcriptionCommentId, RedirectAttributes redirect, 
			HttpServletRequest request) {

		Locale whichLocale = localeResolverforTranscriptionCommentController().resolveLocale(request);
		Optional<TranscriptionComment> optionalTranscriptionComment = this.transcriptionCommentRepository
				.findById(transcriptionCommentId);
		if (optionalTranscriptionComment.isPresent()) {

			this.transcriptionCommentRepository.delete(optionalTranscriptionComment.get());

			Transcription transcription = optionalTranscriptionComment.get().getTranscription();
			if (transcription.isHasOneComment() == true) {
				transcription.setHasOneComment(false);
				transcription.setHasComments(false);
			}

			int numTranscriptionComments = transcription.getNumTranscriptionComments();
			numTranscriptionComments--;
			transcription.setNumTranscriptionComments(numTranscriptionComments);
			if (numTranscriptionComments == 1) {
				transcription.setHasOneComment(true);

				for (TranscriptionComment tc : this.transcriptionCommentRepository.findAll()) {
					boolean deletable = tc.getTranscription().getId() == optionalTranscriptionComment.get()
							.getTranscription().getId();
					if (deletable) {
						tc.setDeletableByAuthor(true);
					}
				}
			}

			if (numTranscriptionComments == 0) {
				transcription.setHasComments(false);
				transcription.setHasOneComment(false);
				transcription.setEditableByAuthor(true);
				transcription.setDeletableByAuthor(true);
				transcription.setCommentableByAuthor(false);
			}

			if (numTranscriptionComments > 1) {
				transcription.setHasOneComment(false);

				List<TranscriptionComment> transcriptionComments = new ArrayList<>();
				for (TranscriptionComment tc : this.transcriptionCommentRepository.findAll()) {
					if (tc.getTranscription().getId() == optionalTranscriptionComment.get().getTranscription()
							.getId()) {
						transcriptionComments.add(tc);
					}
				}

				TranscriptionComment nextDeletableComment = transcriptionComments.get(transcriptionComments.size() - 1);
				int ndcId = nextDeletableComment.getId();

				for (TranscriptionComment tc : transcriptionComments) {
					if (tc.getId() != ndcId) {
						tc.setDeletableByAuthor(false);
					} else
						tc.setDeletableByAuthor(true);
				}
			}

			// this.transcriptionRepository.save(transcription);

			Profile authorProfile = optionalTranscriptionComment.get().getAuthor().getProfile();
			int authorProfileTranscriptionComments = authorProfile.getNumTranscriptionComments();
			authorProfileTranscriptionComments--;
			authorProfile.setNumTranscriptionComments(authorProfileTranscriptionComments);

			if (authorProfileTranscriptionComments == 1) {
				authorProfile.setHasMadeOneTranscriptionComment(true);
			}

			if (authorProfileTranscriptionComments == 0) {
				authorProfile.setHasMadeOneTranscriptionComment(false);
				authorProfile.setHasMadeTranscriptionComments(false);
			}
			// this.profileRepository.save(authorProfile);

			
			if (whichLocale.equals(Locale.ENGLISH)) {
				redirect.addFlashAttribute("success",
						"You've successfully deleted your comment from the transcription that is called "
								+ transcription.getName() + ", " + sessionManager.getLoggedInUser().getUsername()
								+ ". So don't scroll for it.");
			}

			if (whichLocale.equals(Locale.FRENCH)) {
				redirect.addFlashAttribute("success",
						"Vous avez supprimé avec succès votre commentaire de la transcription appelée "
								+ transcription.getName() + ", " + sessionManager.getLoggedInUser().getUsername()
								+ ". Alors ne faites pas défiler pour cela.");
			}

			return "redirect:/app/timeline";
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	private boolean isLoggedIn() {
		return !sessionManager.isLoggedIn();
	}

	private boolean commentOnOwnTranscription(Optional<Transcription> optionalTranscription) {
		return optionalTranscription.get().isCommentableByAuthor() == false
				&& optionalTranscription.get().getAuthor().getId() == sessionManager.getLoggedInUser().getId();
	}

	private boolean isTranscriptionCommentDeletable(Optional<TranscriptionComment> optionalTranscriptionComment) {
		return optionalTranscriptionComment.get().isDeletableByAuthor() == false
				&& optionalTranscriptionComment.get().getAuthor().getId() == sessionManager.getLoggedInUser().getId();
	}

	private boolean whoseTranscriptionComment(Optional<TranscriptionComment> optionalTranscriptionComment) {
		return optionalTranscriptionComment.get().getAuthor().getId() != sessionManager.getLoggedInUser().getId();
	}

	@Bean
	public LocaleResolver localeResolverforTranscriptionCommentController() {
		SessionLocaleResolver r = new SessionLocaleResolver();
		r.setLocaleAttributeName(LOCALE_ATTR);
		return r;
	}
}
