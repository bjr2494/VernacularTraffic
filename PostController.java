package acc.capstone;

import java.time.LocalDate;
import java.time.LocalTime;
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
public class PostController {

	@Autowired
	SessionManager sessionManager;

	@Autowired
	PostRepository postRepository;

	@Autowired
	ProfileRepository profileRepository;

	@Autowired
	UserRepository userRepository;

	private static final String LOCALE_ATTR = "language";

	@GetMapping("/post/{userId}")
	public String post(@PathVariable int userId, Model model, RedirectAttributes redirect, HttpServletRequest request) {

		Locale whichLocale = localeResolverforPosts().resolveLocale(request);
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
		
		Optional<User> optionalUser = userRepository.findById(userId);
		if (optionalUser.isPresent()) {
			if (isPostPossible(optionalUser)) {
				if (whichLocale.equals(Locale.ENGLISH)) {
					redirect.addFlashAttribute("failure", "You cannot make a post for another user");
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					redirect.addFlashAttribute("failure", "Vous ne pouvez pas faire du post pour un autre utilisateur");
				}
				return "redirect:/app/timeline";
			}

			if (optionalUser.get().getProfile().getLanguages().size() > 1) {
				model.addAttribute("postLanguageChoice", "Choose your post language");
			}

			if (whichLocale.equals(Locale.FRENCH))
				model.addAttribute("extraLetters", "");
			// model.addAttribute("loggedInUser", sessionManager.getLoggedInUser());
			model.addAttribute("post", new Post());

			return "post";
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	@Transactional
	@PostMapping("/post/{userId}")
	public String post(@PathVariable int userId, @Valid Post post, Errors errors, Model model,
			RedirectAttributes redirect, HttpServletRequest request) {

		Optional<User> optionalUser = this.userRepository.findById(userId);
		if (optionalUser.isPresent()) {

			Locale whichLocale = localeResolverforPosts().resolveLocale(request);
			if (post.getName().length() < 5) {
				if (whichLocale.equals(Locale.ENGLISH)) {
					errors.rejectValue("name", "bad value", "not decent enough");
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					errors.rejectValue("name", "mauvaise valeur", "pas assez décent");
				}
			}

			if (post.getName().length() > 25) {
				if (whichLocale.equals(Locale.ENGLISH)) {
					errors.rejectValue("name", "bad value", "overly decent");
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					errors.rejectValue("name", "mauvaise valeur", "trop décent");
				}
			}

			if (post.getContent().length() < 10) {
				if (whichLocale.equals(Locale.ENGLISH)) {
					errors.rejectValue("content", "bad value", "obviously too short");
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					errors.rejectValue("content", "mauvaise valeur", "évidemment trop court");
				}
			}

			if (post.getContent().length() > 120) {
				if (whichLocale.equals(Locale.ENGLISH)) {
					errors.rejectValue("content", "bad value", "much too long");
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					errors.rejectValue("content", "mauvaise valeur", "beaucoup trop long");
				}
			}

			if (errors.hasErrors()) {
				if (whichLocale.equals(Locale.FRENCH)) {
					model.addAttribute("extraLetters", "");
				}
				if (optionalUser.get().getProfile().getLanguages().size() > 1) {
					model.addAttribute("postLanguageChoice", "Choose your post language");
				}
				model.addAttribute("post", post);
				return "post";
			} else {
				if (optionalUser.get().getProfile().getLanguages().size() == 1) {
					post.setPostLanguage(optionalUser.get().getProfile().getLanguages().get(0));
				}

				if (optionalUser.get().getProfile().getLanguages().size() == 2) {
					if (whichLocale.equals(Locale.ENGLISH)) {
						post.setPostLanguage(optionalUser.get().getProfile().getLanguages().get(0));
					}

					if (whichLocale.equals(Locale.FRENCH)) {
						post.setPostLanguage(optionalUser.get().getProfile().getLanguages().get(1));
					}
				}

				post.setAuthor(sessionManager.getLoggedInUser());
				post.setPostDate(LocalDate.now());
				post.setPostTime(LocalTime.now());
				post.setProfile(optionalUser.get().getProfile());
				post.setEditableByAuthor(true);
				post.setDeletableByAuthor(true);
				post.setCommentableByAuthor(false);
				post.setHasComments(false);
				post.setHasTranscription(false);
				post.setNumPostComments(0);
				post.setHasOneComment(false);

				this.postRepository.save(post);

				Profile authorProfile = optionalUser.get().getProfile();
				int authorProfilePosts = authorProfile.getNumPosts();
				authorProfilePosts++;
				authorProfile.setNumPosts(authorProfilePosts);

				if (authorProfilePosts == 1) {
					authorProfile.setHasMadePosts(true);
					authorProfile.setHasMadeOnePost(true);
				}

				if (authorProfilePosts > 1) {
					authorProfile.setHasMadeOnePost(false);
				}

				this.profileRepository.save(authorProfile);

				if (whichLocale.equals(Locale.ENGLISH)) {
					redirect.addFlashAttribute("success", "Here is your new post that is called " + post.getName()
							+ ", whose language is English, " + this.sessionManager.getLoggedInUser().getUsername());
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					redirect.addFlashAttribute("success", "Voici votre nouveau post qui s'appelle " + post.getName()
							+ ", dont la langue est français, " + this.sessionManager.getLoggedInUser().getUsername());
				}

				return "redirect:/app/timeline";
			}
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	@GetMapping("/editPost/{postId}")
	public String editPost(@PathVariable int postId, Model model, RedirectAttributes redirect,
			HttpServletRequest request) {

		Locale whichLocale = localeResolverforPosts().resolveLocale(request);
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
			if (whosePost(optionalPost)) {
				if (whichLocale.equals(Locale.ENGLISH)) {
					redirect.addFlashAttribute("failure", "That's not your post to tinker with");
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					redirect.addFlashAttribute("failure", "Vous ne pouvez pas interférer avec ce post");
				}
				return "redirect:/app/timeline";
			}
			if (isPostEditable(optionalPost)) {
				if (whichLocale.equals(Locale.ENGLISH)) {
					redirect.addFlashAttribute("failure", "You can't at this time edit the post in question");
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					redirect.addFlashAttribute("failure",
							"Vous ne pouvez pas pour l'instant éditer le post en question");
				}
				return "redirect:/app/timeline";
			}

			if (whichLocale.equals(Locale.ENGLISH) && optionalPost.get().getPostLanguage().equals(Language.FRENCH)) {
				redirect.addFlashAttribute("languageIssue",
						"Your working language conflicts with the language of the post that you want to edit; either"
								+ " change your mind or change your working language");
				return "redirect:/app/timeline";
			}

			if (whichLocale.equals(Locale.FRENCH) && optionalPost.get().getPostLanguage().equals(Language.ENGLISH)) {
				redirect.addFlashAttribute("languageIssue",
						"Votre langue actuelle se bat contre la langue du post que vous voulez à éditer; changez"
								+ " votre idée ou votre langue actuelle");
				return "redirect:/app/timeline";
			}

			if (whichLocale.equals(Locale.FRENCH))
				model.addAttribute("extraLetters", "");

			model.addAttribute("post", optionalPost.get());
			return "editPost";
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	@PostMapping("/editPost/{postId}")
	public String editPost(@PathVariable int postId, @Valid Post post, Model model, Errors errors,
			RedirectAttributes redirect, HttpServletRequest request) {
		Optional<Post> optionalPost = postRepository.findById(postId);
		if (optionalPost.isPresent()) {

			Locale whichLocale = localeResolverforPosts().resolveLocale(request);
			if (post.getName().length() < 5) {
				if (whichLocale.equals(Locale.ENGLISH)) {
					errors.rejectValue("name", "bad value", "not decent enough");
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					errors.rejectValue("name", "mauvaise valeur", "pas assez décent");
				}
			}

			if (post.getName().length() > 25) {
				if (whichLocale.equals(Locale.ENGLISH)) {
					errors.rejectValue("name", "bad value", "overly decent");
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					errors.rejectValue("name", "mauvaise valeur", "trop décent");
				}
			}

			if (post.getContent().length() < 10) {
				if (whichLocale.equals(Locale.ENGLISH)) {
					errors.rejectValue("content", "bad value", "obviously too short");
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					errors.rejectValue("content", "mauvaise valeur", "évidemment trop court");
				}
			}

			if (post.getContent().length() > 120) {
				if (whichLocale.equals(Locale.ENGLISH)) {
					errors.rejectValue("content", "bad value", "much too long");
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					errors.rejectValue("content", "mauvaise valeur", "beaucoup trop long");
				}
			}

			if (errors.hasErrors()) {
				if (whichLocale.equals(Locale.FRENCH))
					model.addAttribute("extraLetters", "");
				model.addAttribute("post", post);
				return "editPost";
			} else {

				if (optionalPost.get().getAuthor().getProfile().getLanguages().size() == 2) {
					// if language is English, or the first of the list
					if (optionalPost.get().getPostLanguage()
							.equals(optionalPost.get().getProfile().getLanguages().get(0))) {
						post.setPostLanguage(optionalPost.get().getProfile().getLanguages().get(0));
					}
					// if language is French, or the second of the list
					if (optionalPost.get().getPostLanguage()
							.equals(optionalPost.get().getProfile().getLanguages().get(1))) {
						post.setPostLanguage(optionalPost.get().getProfile().getLanguages().get(1));
					}
				}

				if (optionalPost.get().getAuthor().getProfile().getLanguages().size() == 1) {
					post.setPostLanguage(optionalPost.get().getProfile().getLanguages().get(0));
				}

				post.setId(optionalPost.get().getId());
				post.setAuthor(this.sessionManager.getLoggedInUser());
				post.setPostDate(LocalDate.now());
				post.setPostTime(LocalTime.now());
				post.setEditableByAuthor(true);
				post.setDeletableByAuthor(true);
				post.setCommentableByAuthor(false);
				post.setHasComments(false);
				post.setHasOneComment(false);
				post.setHasTranscription(false);
				post.setNumPostComments(optionalPost.get().getNumPostComments());

				Profile authorProfile = optionalPost.get().getAuthor().getProfile();
				post.setProfile(authorProfile);

				this.postRepository.delete(optionalPost.get());
				this.postRepository.save(post);

				if (whichLocale.equals(Locale.ENGLISH)) {
					redirect.addFlashAttribute("success", "Here is your freshly edited post called " + post.getName()
							+ ", " + sessionManager.getLoggedInUser().getUsername());
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					redirect.addFlashAttribute("success", "Voici votre post fraîchement édité appelé" + post.getName()
							+ ", " + sessionManager.getLoggedInUser().getUsername());
				}

				return "redirect:/app/timeline";
			}
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	@GetMapping("/deletePost/{postId}")
	public String deletePost(@PathVariable int postId, RedirectAttributes redirect, HttpServletRequest request) {
		Locale whichLocale = localeResolverforPosts().resolveLocale(request);
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
			if (whosePost(optionalPost)) {
				if (whichLocale.equals(Locale.ENGLISH)) {
					redirect.addFlashAttribute("failure", "That's not yours to tinker with");
				}
				
				if (whichLocale.equals(Locale.FRENCH)) {
					redirect.addFlashAttribute("failure", "Vous ne pouvez pas interagir avec ce post");
				}
				return "redirect:/app/timeline";
			}
			if (isPostDeletable(optionalPost)) {
				if (whichLocale.equals(Locale.ENGLISH)) {
					redirect.addFlashAttribute("failure", "You can't at this time delete the post in question");
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					redirect.addFlashAttribute("failure", "Vous ne pouvez pas pour l'instant supprimer le post en question");
				}
				return "redirect:/app/timeline";
			}
			
			if (whichLocale.equals(Locale.ENGLISH)) {
				redirect.addFlashAttribute("failure", "Please do not try to delete a post in this way");
			}

			if (whichLocale.equals(Locale.FRENCH)) {
				redirect.addFlashAttribute("failure",
						"N'essayez pas de supprimer un post de cette façon");
			}
			return "redirect:/app/timeline";
		}
		else throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	@Transactional
	@PostMapping("/deletePost/{postId}")
	public String deletePost1(@PathVariable int postId, RedirectAttributes redirect, HttpServletRequest request) {

		Locale whichLocale = localeResolverforPosts().resolveLocale(request);
		Optional<Post> optionalPost = postRepository.findById(postId);
		if (optionalPost.isPresent()) {

			this.postRepository.delete(optionalPost.get());

			Profile authorProfile = optionalPost.get().getAuthor().getProfile();
			int authorProfilePosts = authorProfile.getNumPosts();
			authorProfilePosts--;
			authorProfile.setNumPosts(authorProfilePosts);

			if (authorProfilePosts == 1) {
				authorProfile.setHasMadeOnePost(true);
			}

			if (authorProfilePosts == 0) {
				authorProfile.setHasMadeOnePost(false);
				authorProfile.setHasMadePosts(false);
			}

			if (whichLocale.equals(Locale.ENGLISH)) {
				redirect.addFlashAttribute("success",
						"You've successfully deleted the post that was called " + optionalPost.get().getName() + ", "
								+ this.sessionManager.getLoggedInUser().getUsername() + ". So don't scroll for it.");
			}
			
			if (whichLocale.equals(Locale.FRENCH)) {
				redirect.addFlashAttribute("success", "Vous avez supprimé avec succès votre post qui s'appelait " 
						+ optionalPost.get().getName() + ", " + this.sessionManager.getLoggedInUser().getUsername() 
						+ ". Alors ne faites pas défiler pour cela.");
			}

			return "redirect:/app/timeline";
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	private boolean isLoggedIn() {
		return !sessionManager.isLoggedIn();
	}

	private boolean isPostPossible(Optional<User> optionalUser) {
		return optionalUser.get().getId() != this.sessionManager.getLoggedInUser().getId();
	}

	private boolean whosePost(Optional<Post> optionalPost) {
		return optionalPost.get().getAuthor().getId() != sessionManager.getLoggedInUser().getId();
	}

	private boolean isPostEditable(Optional<Post> optionalPost) {
		return optionalPost.get().isEditableByAuthor() == false
				&& optionalPost.get().getAuthor().getId() == sessionManager.getLoggedInUser().getId();
	}

	private boolean isPostDeletable(Optional<Post> optionalPost) {
		return optionalPost.get().isDeletableByAuthor() == false
				&& optionalPost.get().getAuthor().getId() == sessionManager.getLoggedInUser().getId();
	}

	@Bean
	public LocaleResolver localeResolverforPosts() {
		SessionLocaleResolver r = new SessionLocaleResolver();
		r.setLocaleAttributeName(LOCALE_ATTR);
		return r;
	}
}
