package acc.capstone;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
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

	@GetMapping("/postComment/{postId}")
	public String postComment(@PathVariable int postId, Model model, RedirectAttributes redirect) {
		if (isLoggedIn()) {
			redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in user?");
			return "redirect:/app/login";
		}
		Optional<Post> optionalPost = postRepository.findById(postId);
		if (optionalPost.isPresent()) {
			if (commentOnOwnPost(optionalPost)) {
				redirect.addFlashAttribute("failure", "You can't at this time make a comment on your own post");
				return "redirect:/app/timeline";
			}
			model.addAttribute("post", optionalPost.get());
			// model.addAttribute("profile", optionalPost.get().getProfile());
			// model.addAttribute("loggedInUser", sessionManager.getLoggedInUser());
			model.addAttribute("postComment", new PostComment());
			return "postComment";
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	@PostMapping("/postComment/{postId}")
	public String postComment(@PathVariable int postId, @Valid PostComment postComment, Errors errors, Model model,
			RedirectAttributes redirect) {
		if (isLoggedIn()) {
			redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in user?");
			return "redirect:/app/login";
		}
		Optional<Post> optionalPost = postRepository.findById(postId);
		if (optionalPost.isPresent()) {
			if (commentOnOwnPost(optionalPost)) {
				redirect.addFlashAttribute("failure", "You can't at this time make a comment on your own post");
				return "redirect:/app/timeline";
			}
			if (postComment.getContent().length() < 4) {
				errors.rejectValue("content", "bad value",
						"Please increase the comment's length in order to satisfy our requirement");
			}
			if (postComment.getContent().length() > 60) {
				errors.rejectValue("content", "bad value",
						"Please shorten the comment's length in order to satisfy our requirement");
			}
			if (errors.hasErrors()) {
				// model.addAttribute("loggedInUser", sessionManager.getLoggedInUser());
				model.addAttribute("post", optionalPost.get());
				// model.addAttribute("postComment", postComment);
				// model.addAttribute("contentError", errors.getFieldError("content"));
				return "postComment";
			} else {

				Post post = optionalPost.get();

				if (post.isEditableByAuthor() == true)
					post.setEditableByAuthor(false);
				if (post.isDeletableByAuthor() == true)
					post.setDeletableByAuthor(false);
				if (post.isCommentableByAuthor() == false)
					post.setCommentableByAuthor(true);
				if (post.isHasComments() == false)
					post.setHasComments(true);
				if (post.isHasOneComment() == false && post.getPostComments().size() == 0) {
					post.setHasOneComment(true);
					post.setHasComments(true);
				}

				this.postRepository.save(post);

				postComment.setPost(post);
				postComment.setCommentDate(LocalDate.now());
				postComment.setCommentTime(LocalTime.now());
				postComment.setAuthor(sessionManager.getLoggedInUser());
				postComment.setProfile(sessionManager.getLoggedInUser().getProfile());
				postComment.setDeletableByAuthor(true);
				postComment.setPostCommentLanguage(post.getPostLanguage());

				for (PostComment pc : this.postCommentRepository.findAllByPostId(optionalPost.get().getId())) {
					if (pc.getCommentDate().isBefore(postComment.getCommentDate())) {
						pc.setDeletableByAuthor(false);
						this.postCommentRepository.save(pc);
					}
					if (pc.getCommentDate().isEqual(postComment.getCommentDate())) {
						if (pc.getCommentTime().isBefore(postComment.getCommentTime())) {
							pc.setDeletableByAuthor(false);
							this.postCommentRepository.save(pc);
						}
					}
				}

				this.postCommentRepository.save(postComment);
				// profile for the author of the post comment
				Profile authorProfile = postComment.getAuthor().getProfile();

				if (authorProfile.isHasMadePostComments() == false) {
					authorProfile.setHasMadePostComments(true);
					authorProfile.setHasMadeOnePostComment(true);
				}
				if (authorProfile.isHasMadeOnePostComment() == true) {
					authorProfile.setHasMadeOnePostComment(false);
				}
				if (authorProfile.isHasMadeOnePostComment() == false && authorProfile.getPostComments().size() == 0) {
					authorProfile.setHasMadeOnePostComment(true);
					authorProfile.setHasMadePostComments(true);
				}

				// authorProfile.getPostComments().add(postComment);
				this.profileRepository.save(authorProfile);

				redirect.addFlashAttribute("success", "Scroll accordingly to see your comment on the post called "
						+ post.getName() + ", " + sessionManager.getLoggedInUser().getUsername());
				return "redirect:/app/timeline";
			}
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	@PostMapping("/deletePostComment/{postCommentId}")
	public String deletePostComment(@PathVariable int postCommentId, RedirectAttributes redirect) {
		if (isLoggedIn()) {
			redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in user!");
			return "login";
		}
		Optional<PostComment> optionalPostComment = postCommentRepository.findById(postCommentId);
		if (optionalPostComment.isPresent()) {
			if (isPostCommentDeletable(optionalPostComment)) {
				redirect.addFlashAttribute("failure",
						"You can't at this time delete a comment from the post in question");
				return "redirect:/app/timeline";
			}
			if (whosePostComment(optionalPostComment)) {
				redirect.addFlashAttribute("failure", "This is not your post comment to tinker with");
				return "redirect:/app/timeline";
			}
			Post post = optionalPostComment.get().getPost();
			if (post.isHasOneComment() == true) {
				post.setHasOneComment(false);
				post.setHasComments(false);
			}

			if (post.isHasComments() == true && post.getPostComments().size() == 2) {
				post.setHasOneComment(true);
			}

			if (post.isDeletableByAuthor() == false && post.getPostComments().size() == 1)
				post.setDeletableByAuthor(true);

			if (post.isEditableByAuthor() == false && post.getPostComments().size() == 1)
				post.setEditableByAuthor(true);

			if (post.isCommentableByAuthor() == true && post.getPostComments().size() == 1)
				post.setCommentableByAuthor(false);

			this.postRepository.save(post);

			Profile authorProfile = optionalPostComment.get().getProfile();
			if (authorProfile.isHasMadeOnePostComment() == true) {
				authorProfile.setHasMadeOnePostComment(false);
				authorProfile.setHasMadePostComments(false);
			}

			if (authorProfile.isHasMadePostComments() == true && authorProfile.getPostComments().size() == 2) {
				authorProfile.setHasMadeOnePostComment(true);
			}

			this.profileRepository.save(authorProfile);
			this.postCommentRepository.delete(optionalPostComment.get());

			redirect.addFlashAttribute("success",
					"You've successfully deleted your comment from the post called " + post.getName() + ", "
							+ sessionManager.getLoggedInUser().getUsername() + ". So don't scroll for it.");
			return "redirect:/app/timeline";
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
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
}
