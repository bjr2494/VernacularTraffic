package acc.capstone;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
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

	@GetMapping("/post/{profileId}")
	public String post(@PathVariable int profileId, Model model, RedirectAttributes redirect) {
		if (isLoggedIn()) {
			redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in user?");
			return "redirect:/app/login";
		}
		Optional<Profile> optionalProfile = profileRepository.findById(profileId);
		if (optionalProfile.isPresent()) {
			if (isPostPossible(optionalProfile)) {
				redirect.addFlashAttribute("failure", "You cannot make a post for another user");
				return "redirect:/app/timeline";
			}

			if (optionalProfile.get().getLanguages().size() > 1) {
				model.addAttribute("postLanguageChoice", "Choose your post language");
			}

			// model.addAttribute("loggedInUser", sessionManager.getLoggedInUser());
			model.addAttribute("post", new Post());

			return "post";
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	@PostMapping("/post/{profileId}")
	public String post(@PathVariable int profileId, @Valid Post post, Errors errors, Model model,
			RedirectAttributes redirect) {
		System.out.println(errors);
		if (isLoggedIn()) {
			redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in user?");
			return "redirect:/app/login";
		}

		Optional<Profile> optionalProfile = profileRepository.findById(profileId);
		if (optionalProfile.isPresent()) {
			if (isPostPossible(optionalProfile)) {
				redirect.addFlashAttribute("failure", "You cannot make a post for another user");
				return "redirect:/app/timeline";
			}

			if (errors.hasErrors()) {
				// model.addAttribute("loggedInUser", sessionManager.getLoggedInUser());
				model.addAttribute("post", post);
				return "post";
			} else {
				if (optionalProfile.get().getLanguages().size() == 1) {
					post.setPostLanguage(optionalProfile.get().getLanguages().get(0));
				}
				post.setAuthor(sessionManager.getLoggedInUser());
				post.setPostDate(LocalDate.now());
				post.setPostTime(LocalTime.now());
				post.setProfile(optionalProfile.get());
				post.setEditableByAuthor(true);
				post.setDeletableByAuthor(true);
				post.setCommentableByAuthor(false);
				post.setHasComments(false);
				post.setHasTranscription(false);

				Profile authorProfile = optionalProfile.get();
				if (authorProfile.isHasMadePosts() == false) {
					authorProfile.setHasMadePosts(true);
					authorProfile.setHasMadeOnePost(true);
				}

				if (authorProfile.isHasMadeOnePost() == true)
					authorProfile.setHasMadeOnePost(false);

				if (authorProfile.isHasMadeOnePost() == false && authorProfile.isHasMadePosts() == false) {
					authorProfile.setHasMadeOnePost(true);
					authorProfile.setHasMadePosts(true);
				}

				// authorProfile.getPosts().add(post);
				this.profileRepository.save(authorProfile);

				if (optionalProfile.get().getLanguages().size() == 1) {
					// fetching the only language that there is in this case
					post.setPostLanguage(optionalProfile.get().getLanguages().get(0));
					this.postRepository.save(post);

					redirect.addFlashAttribute("success",
							"Here is your new post that is called " + post.getName() + ", whose language is "
									+ post.getPostLanguage().toString() + ", "
									+ sessionManager.getLoggedInUser().getUsername());
					return "redirect:/app/timeline";
				}

				// otherwise, if there is a second language
				else {
					this.postRepository.save(post);
					return "redirect:/app/postLanguageChoice/" + post.getId();
				}
			}
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	@GetMapping("/editPost/{postId}")
	public String editPost(@PathVariable int postId, Model model, RedirectAttributes redirect) {
		if (isLoggedIn()) {
			redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in user?");
			return "redirect:/app/login";
		}
		Optional<Post> optionalPost = postRepository.findById(postId);
		if (optionalPost.isPresent()) {
			if (whosePost(optionalPost)) {
				redirect.addFlashAttribute("failure", "That's not yours to tinker with");
				return "redirect:/app/timeline";
			}
			if (isPostEditable(optionalPost)) {
				redirect.addFlashAttribute("failure", "You can't at this time edit the post in question");
				return "redirect:/app/timeline";
			}
			model.addAttribute("post", optionalPost.get());
			return "editPost";
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	@PostMapping("/editPost/{postId}")
	public String editPost(@PathVariable int postId, @Valid Post post, Model model, Errors errors,
			RedirectAttributes redirect) {
		if (isLoggedIn()) {
			redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in user?");
			return "redirect:/app/login";
		}
		Optional<Post> optionalPost = postRepository.findById(postId);
		if (optionalPost.isPresent()) {
			if (whosePost(optionalPost)) {
				redirect.addFlashAttribute("failure", "That's not yours to tinker with");
				return "redirect:/app/timeline";
			}
			if (isPostEditable(optionalPost)) {
				redirect.addFlashAttribute("failure", "You can't at this time edit the post in question");
				return "redirect:/app/timeline";
			}
			if (errors.hasErrors()) {
				return "editPost";
			} else {

				if (optionalPost.get().getProfile().getLanguages().size() == 2) {
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

				if (optionalPost.get().getProfile().getLanguages().size() == 1) {
					post.setPostLanguage(optionalPost.get().getProfile().getLanguages().get(0));
				}

				post.setId(optionalPost.get().getId());
				post.setAuthor(sessionManager.getLoggedInUser());
				post.setPostDate(LocalDate.now());
				post.setPostTime(LocalTime.now());
				post.setEditableByAuthor(true);
				post.setDeletableByAuthor(true);
				post.setCommentableByAuthor(false);
				post.setHasComments(false);
				post.setHasOneComment(false);
				post.setHasTranscription(false);

				Profile authorProfile = optionalPost.get().getProfile();
				post.setProfile(authorProfile);

				this.postRepository.delete(optionalPost.get());
				this.postRepository.save(post);

				if (authorProfile.isHasMadePosts() == false) {
					authorProfile.setHasMadePosts(true);
					authorProfile.setHasMadeOnePost(true);
				}

				if (authorProfile.isHasMadeOnePost() == true)
					authorProfile.setHasMadeOnePost(false);

				if (authorProfile.isHasMadeOnePost() == false && authorProfile.isHasMadePosts() == false) {
					authorProfile.setHasMadeOnePost(true);
					authorProfile.setHasMadePosts(true);
				}

				// authorProfile.getPosts().add(newPost);
				this.profileRepository.save(authorProfile);
				redirect.addFlashAttribute("success", "Here is your freshly edited post called " + post.getName() + ", "
						+ sessionManager.getLoggedInUser().getUsername());
				return "redirect:/app/timeline";
			}
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	@PostMapping("/deletePost/{postId}")
	public String deletePost(@PathVariable int postId, RedirectAttributes redirect) {
		if (isLoggedIn()) {
			redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in user?");
			return "redirect:/app/login";
		}
		Optional<Post> optionalPost = postRepository.findById(postId);
		if (optionalPost.isPresent()) {
			if (whosePost(optionalPost)) {
				redirect.addFlashAttribute("failure", "That's not yours to tinker with");
				return "redirect:/app/timeline";
			}
			if (isPostDeletable(optionalPost)) {
				redirect.addFlashAttribute("failure", "You can't at this time delete the post in question");
				return "redirect:/app/timeline";
			}
			Profile authorProfile = optionalPost.get().getProfile();
			if (authorProfile.isHasMadeOnePost() == true) {
				authorProfile.setHasMadeOnePost(false);
				authorProfile.setHasMadePosts(false);
			}
			if (authorProfile.isHasMadePosts() == true && authorProfile.getPosts().size() == 2)
				authorProfile.setHasMadeOnePost(true);

			this.profileRepository.save(authorProfile);
			this.postRepository.delete(optionalPost.get());
			redirect.addFlashAttribute("success",
					"You've successfully deleted the post that was called " + optionalPost.get().getName() + ", "
							+ sessionManager.getLoggedInUser().getUsername() + ". So don't scroll for it.");
			return "redirect:/app/timeline";
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	private boolean isLoggedIn() {
		return !sessionManager.isLoggedIn();
	}

	private boolean isPostPossible(Optional<Profile> optionalProfile) {
		return optionalProfile.get().getId() != sessionManager.getLoggedInUser().getId();
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
}
