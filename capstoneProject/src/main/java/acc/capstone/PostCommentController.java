package acc.capstone;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
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
public class PostCommentController {

	@Autowired
	PostRepository postRepository;

	@Autowired
	SessionManager sessionManager;

	@Autowired
	PostCommentRepository postCommentRepository;

	@Autowired
	ProfileRepository profileRepository;

	@Autowired
	TranscriptionRepository transcriptionRepository;

	@Autowired
	TranscriptionCommentRepository transcriptionCommentRepository;

	@Autowired
	UserRepository userRepository;

	private static final String LOCALE_ATTR = "language";

	@GetMapping("/postComment/{postId}")
	public String postComment(@PathVariable int postId, Model model, RedirectAttributes redirect,
			HttpServletRequest request) {
		Locale whichLocale = localeResolverforPostCommentController().resolveLocale(request);

		if (isLoggedIn()) {
			if (whichLocale.equals(Locale.ENGLISH)) {
				redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in user?");
			}

			if (whichLocale.equals(Locale.FRENCH)) {
				redirect.addFlashAttribute("message",
						"Pourquoi devriez-vous être autorisé à faire comme utilisateur non connecté?");
			}
		}

		Optional<Post> optionalPost = postRepository.findById(postId);
		if (optionalPost.isPresent()) {
			if (commentOnOwnPost(optionalPost)) {
				if (whichLocale.equals(Locale.ENGLISH)) {
					redirect.addFlashAttribute("failure", "You can't at this time make a comment on your own post");
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					redirect.addFlashAttribute("failure",
							"Vous ne pouvez pas pour le moment faire de commentaire sur votre propre post");
				}
				return "redirect:/app/timeline";
			}

			model.addAttribute("post", optionalPost.get());

			if (whichLocale.equals(Locale.ENGLISH) && optionalPost.get().getPostLanguage().equals(Language.FRENCH)) {
				redirect.addFlashAttribute("languageIssue",
						"Your working language conflicts with the language of the post that you want to comment on; either"
								+ " change your mind or change your working language");
				return "redirect:/app/timeline";
			}

			if (whichLocale.equals(Locale.FRENCH) && optionalPost.get().getPostLanguage().equals(Language.ENGLISH)) {
				redirect.addFlashAttribute("languageIssue",
						"Votre langue actuelle se bat contre la langue du post sur lequel vous voulez faire un commentaire;"
								+ " changez votre idée ou votre langue actuelle");
				return "redirect:/app/timeline";
			}

			if (optionalPost.get().getPostLanguage().equals(Language.FRENCH))
				model.addAttribute("extraLetters", "");
			model.addAttribute("postComment", new PostComment());
			return "postComment";
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	@Transactional
	@PostMapping("/postComment/{postId}")
	public String postComment(@PathVariable int postId, @Valid PostComment postComment, Errors errors, Model model,
			RedirectAttributes redirect, HttpServletRequest request) {

		Locale whichLocale = localeResolverforPostCommentController().resolveLocale(request);
		Optional<Post> optionalPost = postRepository.findById(postId);
		if (optionalPost.isPresent()) {
			if (postComment.getContent().length() < 4) {
				if (whichLocale.equals(Locale.ENGLISH)) {
					errors.rejectValue("content", "bad value",
							"Please increase the comment's length in order to satisfy our requirement");
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					errors.rejectValue("content", "mauvaise valeur",
							"Veuillez augmenter la longueur du commentaire afin de satisfaire notre exigence");
				}
			}
			if (postComment.getContent().length() > 60) {
				if (whichLocale.equals(Locale.ENGLISH)) {
					errors.rejectValue("content", "bad value",
							"Please shorten the comment's length in order to satisfy our requirement");
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					errors.rejectValue("content", "mauvaise valeur",
							"Veuillez raccourcir la longueur du commentaire afin de satisfaire notre exigence");
				}
			}
			if (errors.hasErrors()) {
				model.addAttribute("post", optionalPost.get());
				if (optionalPost.get().getPostLanguage().equals(Language.FRENCH))
					model.addAttribute("extraLetters", "");
				return "postComment";
			} else {

				Post post = optionalPost.get();

				postComment.setPost(post);
				postComment.setCommentDate(LocalDate.now());
				postComment.setCommentTime(LocalTime.now());
				postComment.setAuthor(sessionManager.getLoggedInUser());
				Profile profile = this.profileRepository.findByUser(this.sessionManager.getLoggedInUser());
				postComment.setProfile(profile);

				int numPostComments = post.getNumPostComments();
				numPostComments++;
				post.setNumPostComments(numPostComments);
				postComment.setPostCommentLanguage(post.getPostLanguage());

				if (numPostComments == 1) {
					post.setHasOneComment(true);
					post.setHasComments(true);
					post.setEditableByAuthor(false);
					post.setDeletableByAuthor(false);
					post.setCommentableByAuthor(true);

				}

				if (numPostComments > 1) {
					post.setHasOneComment(false);
				}

				for (PostComment pc : this.postCommentRepository.findAll()) {
					if (pc.getPost().getId() == postComment.getPost().getId()) {
						pc.setDeletableByAuthor(false);
					}
				}

				postComment.setDeletableByAuthor(true);
				this.postCommentRepository.save(postComment);

				// profile for the author of the post comment
				Profile authorProfile = postComment.getAuthor().getProfile();
				int authorProfilePostComments = authorProfile.getNumPostComments();
				authorProfilePostComments++;
				authorProfile.setNumPostComments(authorProfilePostComments);
				if (authorProfilePostComments == 1) {
					authorProfile.setHasMadePostComments(true);
					authorProfile.setHasMadeOnePostComment(true);
				}

				if (authorProfilePostComments > 1) {
					authorProfile.setHasMadeOnePostComment(false);
				}

				this.profileRepository.save(authorProfile);

				if (whichLocale.equals(Locale.ENGLISH)) {
					redirect.addFlashAttribute("success", "Scroll accordingly to see your comment on the post called "
							+ post.getName() + ", " + sessionManager.getLoggedInUser().getUsername());
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					redirect.addFlashAttribute("success",
							"Faites défiler en conséquence pour voir votre commentaire sur le message appelé "
									+ post.getName() + ", " + sessionManager.getLoggedInUser().getUsername());
				}
				return "redirect:/app/timeline";
			}
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	@GetMapping("/deletePostComment/{postCommentId}")
	public String deletePostComment(@PathVariable int postCommentId, RedirectAttributes redirect,
			HttpServletRequest request) {

		Locale whichLocale = localeResolverforPostCommentController().resolveLocale(request);
		if (isLoggedIn()) {
			if (whichLocale.equals(Locale.ENGLISH)) {
				redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in user?");
			}

			if (whichLocale.equals(Locale.FRENCH)) {
				redirect.addFlashAttribute("message",
						"Pourquoi devriez-vous être autorisé à faire comme utilisateur non connecté?");
			}
		}

		Optional<PostComment> optionalPostComment = this.postCommentRepository.findById(postCommentId);
		if (optionalPostComment.isPresent()) {

			if (isPostCommentDeletable(optionalPostComment)) {
				if (whichLocale.equals(Locale.ENGLISH)) {
					redirect.addFlashAttribute("failure",
							"You can't at this time delete a comment from the post in question");
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					redirect.addFlashAttribute("failure",
							"Vous ne pouvez pas pour l'instant supprimer un commentaire du post en question");
				}
				return "redirect:/app/timeline";
			}
			if (whosePostComment(optionalPostComment)) {
				if (whichLocale.equals(Locale.ENGLISH)) {
					redirect.addFlashAttribute("failure", "This is not your post comment to tinker with");
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					redirect.addFlashAttribute("failure", "vous ne pouvez pas interférer avec ce commentaire de post");
				}
				return "redirect:/app/timeline";
			}

			if (whichLocale.equals(Locale.ENGLISH)) {
				redirect.addFlashAttribute("failure", "Please do not try to delete a post comment in this way");
			}

			if (whichLocale.equals(Locale.FRENCH)) {
				redirect.addFlashAttribute("failure",
						"N'essayez pas de supprimer un commentaire de post de cette façon");
			}

			return "redirect:/app/timeline";
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	@Transactional
	@PostMapping("/deletePostComment/{postCommentId}")
	public String deletePostComment1(@PathVariable int postCommentId, RedirectAttributes redirect,
			HttpServletRequest request) {

		Locale whichLocale = localeResolverforPostCommentController().resolveLocale(request);

		Optional<PostComment> optionalPostComment = postCommentRepository.findById(postCommentId);
		if (optionalPostComment.isPresent()) {
			this.postCommentRepository.delete(optionalPostComment.get());
			Post post = optionalPostComment.get().getPost();

			int numPostComments = post.getNumPostComments();
			numPostComments--;
			post.setNumPostComments(numPostComments);
			if (numPostComments == 1) {
				post.setHasOneComment(true);

				for (PostComment pc : this.postCommentRepository.findAll()) {
					System.out.println(pc.getPost());

					boolean deletable = pc.getPost().getId() == optionalPostComment.get().getPost().getId();
					System.out.println(deletable);
					if (deletable) {
						pc.setDeletableByAuthor(true);
					}
				}
			}

			if (numPostComments == 0) {
				post.setHasComments(false);
				post.setHasOneComment(false);
				post.setEditableByAuthor(true);
				post.setDeletableByAuthor(true);
				post.setCommentableByAuthor(false);
			}

			if (numPostComments > 1) {

				post.setHasOneComment(false);

				List<PostComment> postComments = new ArrayList<>();
				for (PostComment pc : this.postCommentRepository.findAll()) {
					if (pc.getPost().getId() == optionalPostComment.get().getPost().getId()) {
						postComments.add(pc);
					}
				}

				PostComment nextDeletableComment = postComments.get(postComments.size() - 1);
				int ndcId = nextDeletableComment.getId();

				for (PostComment pc : postComments) {
					if (pc.getId() != ndcId) {
						pc.setDeletableByAuthor(false);
					} else
						pc.setDeletableByAuthor(true);
				}
			}
			// this.postRepository.save(post);

			Profile authorProfile = optionalPostComment.get().getAuthor().getProfile();

			int authorProfilePostComments = authorProfile.getNumPostComments();
			authorProfilePostComments--;
			authorProfile.setNumPostComments(authorProfilePostComments);

			if (authorProfilePostComments == 1) {
				authorProfile.setHasMadeOnePostComment(true);
			}

			if (authorProfilePostComments == 0) {
				authorProfile.setHasMadeOnePostComment(false);
				authorProfile.setHasMadePostComments(false);
			}

			// this.profileRepository.save(authorProfile);

			if (whichLocale.equals(Locale.ENGLISH)) {
				redirect.addFlashAttribute("success",
						"You've successfully deleted your comment from the post called " + post.getName() + ", "
								+ this.sessionManager.getLoggedInUser().getUsername() + ". So don't scroll for it.");
			}

			if (whichLocale.equals(Locale.FRENCH)) {
				redirect.addFlashAttribute("success",
						"Vous avez supprimé avec succès votre commentaire du post appelé " + post.getName() + ", "
								+ this.sessionManager.getLoggedInUser().getUsername()
								+ ". Alors ne faites pas défiler pour cela.");
			}
			return "redirect:/app/timeline";
		} else {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
	}

	private boolean isLoggedIn() {
		return !sessionManager.isLoggedIn();
	}

	private boolean commentOnOwnPost(Optional<Post> optionalPost) {
		return optionalPost.get().isCommentableByAuthor() == false
				&& optionalPost.get().getAuthor().getId() == sessionManager.getLoggedInUser().getId();
	}

	private boolean isPostCommentDeletable(Optional<PostComment> optionalPostComment) {
		return optionalPostComment.get().isDeletableByAuthor() == false
				&& optionalPostComment.get().getAuthor().getId() == sessionManager.getLoggedInUser().getId();
	}

	private boolean whosePostComment(Optional<PostComment> optionalPostComment) {
		return optionalPostComment.get().getAuthor().getId() != sessionManager.getLoggedInUser().getId();
	}

	@Bean
	public LocaleResolver localeResolverforPostCommentController() {
		SessionLocaleResolver r = new SessionLocaleResolver();
		r.setLocaleAttributeName(LOCALE_ATTR);
		return r;
	}
}
