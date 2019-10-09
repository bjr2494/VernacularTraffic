package acc.capstone;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import javax.transaction.Transactional;
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
public class TranscriptionController {

	@Autowired
	PostRepository postRepository;

	@Autowired
	SessionManager sessionManager;

	@Autowired
	TranscriptionRepository transcriptionRepository;

	@Autowired
	ProfileRepository profileRepository;

	@GetMapping("/transcription/{postId}")
	public String transcription(@PathVariable int postId, Model model, RedirectAttributes redirect) {
		if (isLoggedIn()) {
			redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in user?");
			return "redirect:/app/login";
		}
		Optional<Post> optionalPost = postRepository.findById(postId);
		if (optionalPost.isPresent()) {
			if (whosePotentialTranscription(optionalPost)) {
				redirect.addFlashAttribute("failure", "You are not allowed to transcribe your own post");
				return "redirect:/app/timeline";
			}
			if (isPostTranscribed(optionalPost)) {
				redirect.addFlashAttribute("failure", "The post in question has already been transcribed");
				return "redirect:/app/timeline";
			}
			model.addAttribute("post", optionalPost.get());
			Transcription transcription = new Transcription();
			model.addAttribute("transcription", transcription);
			return "transcription";
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	@PostMapping("/transcription/{postId}")
	public String transcription(@PathVariable int postId, @Valid Transcription transcription, Errors errors,
			Model model, RedirectAttributes redirect) {
		if (isLoggedIn()) {
			redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in user?");
			return "redirect:/app/login";
		}
		Optional<Post> optionalPost = postRepository.findById(postId);
		if (optionalPost.isPresent()) {
			if (whosePotentialTranscription(optionalPost)) {
				redirect.addFlashAttribute("failure", "You are not allowed to transcribe your own post");
				return "redirect:/app/timeline";
			}
			if (isPostTranscribed(optionalPost)) {
				redirect.addFlashAttribute("failure", "The post in question has already been transcribed");
				return "redirect:/app/timeline";
			}
			int transcriptionLength = transcription.getContent().length();
			int postLength = optionalPost.get().getContent().length();
			if (transcriptionLength < (postLength -= (postLength / 4))) {
				errors.rejectValue("content", "bad value",
						"please increase the transcription's length so that it is closer to that of the post");
			}
			if (transcriptionLength > (postLength += (postLength / 4))) {
				errors.rejectValue("content", "bad value",
						"please shorten the transcription's length so that it is closer to that of the post");
			}

			if (errors.hasErrors()) {
				model.addAttribute("post", optionalPost.get());
				model.addAttribute("transcription", transcription);
				return "transcription";
			}

			else {

				Post post = optionalPost.get();
				transcription.setName("Transcription of " + post.getName());

				transcription.setAuthor(sessionManager.getLoggedInUser());
				transcription.setTranscriptionDate(LocalDate.now());
				transcription.setTranscriptionTime(LocalTime.now());

				// Optional<Profile> optionalProfile =
				// profileRepository.findById(sessionManager.getLoggedInUser().getId());
				transcription.setProfile(sessionManager.getLoggedInUser().getProfile());
				transcription.setName("Transcription of " + post.getName());
				transcription.setEditableByAuthor(true);
				transcription.setDeletableByAuthor(true);
				transcription.setCommentableByAuthor(false);
				transcription.setHasComments(false);
				transcription.setHasOneComment(false);
				transcription.setPost(post);
				transcription.setTranscriptionLanguage(post.getPostLanguage());

				this.transcriptionRepository.save(transcription);

				post.setEditableByAuthor(false);
				post.setDeletableByAuthor(false);
				post.setHasTranscription(true);
				post.setTranscription(transcription);
				this.postRepository.save(post);

				// profile of author of transcription
				Profile authorProfile = transcription.getProfile();

				if (authorProfile.isHasMadeTranscriptions() == false)
					authorProfile.setHasMadeTranscriptions(true);
				if (authorProfile.isHasMadeOneTranscription() == true)
					authorProfile.setHasMadeOneTranscription(false);
				if (authorProfile.isHasMadeOneTranscription() == false
						&& authorProfile.isHasMadeTranscriptions() == false) {
					authorProfile.setHasMadeOneTranscription(true);
					authorProfile.setHasMadeTranscriptions(true);
				}

				// authorProfile.getTranscriptions().add(transcription);
				this.profileRepository.save(authorProfile);

				redirect.addFlashAttribute("success", "Scroll accordingly to see your new transcription that is titled "
						+ transcription.getName() + ", " + sessionManager.getLoggedInUser().getUsername());
				return "redirect:/app/timeline";
			}
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	@GetMapping("/editTranscription/{transcriptionId}")
	public String editTranscription(@PathVariable int transcriptionId, Model model, RedirectAttributes redirect) {
		if (isLoggedIn()) {
			redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in user?");
			return "redirect:/app/login";
		}
		Optional<Transcription> optionalTranscription = transcriptionRepository.findById(transcriptionId);
		if (optionalTranscription.isPresent()) {

			if (whoseTranscription(optionalTranscription)) {
				redirect.addFlashAttribute("failure", "That is not your transcription to tinker with");
				return "redirect:/app/timeline";
			}
			if (isTranscriptionEditable(optionalTranscription)) {
				redirect.addFlashAttribute("failure", "You can't at this time edit the transcription in question");
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
			Errors errors, RedirectAttributes redirect) {
		if (isLoggedIn()) {
			redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in user?");
			return "redirect:/app/login";
		}
		Optional<Transcription> optionalTranscription = transcriptionRepository.findById(transcriptionId);
		if (optionalTranscription.isPresent()) {
			if (whoseTranscription(optionalTranscription)) {
				redirect.addFlashAttribute("failure", "That is not your transcription to tinker with");
				return "redirect:/app/timeline";
			}
			if (isTranscriptionEditable(optionalTranscription)) {
				redirect.addFlashAttribute("failure", "You can't at this time edit the transcription in question");
				return "redirect:/app/timeline";
			}
			int transcriptionLength = transcription.getContent().length();
			int postLength = optionalTranscription.get().getPost().getContent().length();
			if (transcriptionLength < (postLength -= (postLength / 4))) {
				errors.rejectValue("content", "bad value",
						"please increase the transcription's length so that it is closer to that of the post");
			}
			if (transcriptionLength > (postLength += (postLength / 4))) {
				errors.rejectValue("content", "bad value",
						"please shorten the transcription's length so that it is closer to that of the post");
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
				
				

				Profile authorProfile = optionalTranscription.get().getProfile();
				transcription.setProfile(authorProfile);

				Post post = optionalTranscription.get().getPost();
				post.setEditableByAuthor(false);
				post.setHasTranscription(true);
				post.setTranscription(transcription);
				
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
				
				this.postRepository.save(post);
				this.transcriptionRepository.delete(optionalTranscription.get());
				this.transcriptionRepository.save(transcription);

				if (authorProfile.isHasMadeTranscriptions() == false)
					authorProfile.setHasMadeTranscriptions(true);
				if (authorProfile.isHasMadeOneTranscription() == true)
					authorProfile.setHasMadeOneTranscription(false);
				if (authorProfile.isHasMadeOneTranscription() == false
						&& authorProfile.isHasMadeTranscriptions() == false) {
					authorProfile.setHasMadeOneTranscription(true);
					authorProfile.setHasMadeTranscriptions(true);
				}

				this.profileRepository.save(authorProfile);
				redirect.addFlashAttribute("success",
						"Scroll accordingly to see the edited version of your transcription" + " that is titled "
								+ transcription.getName() + ", " + sessionManager.getLoggedInUser().getUsername());
				return "redirect:/app/timeline";
			}
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	@PostMapping("/deleteTranscription/{transcriptionId}")
	public String deleteTranscription(@PathVariable int transcriptionId, RedirectAttributes redirect) {
		if (isLoggedIn()) {
			redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in user?");
			return "redirect:/app/login";
		}
		Optional<Transcription> optionalTranscription = transcriptionRepository.findById(transcriptionId);
		if (optionalTranscription.isPresent()) {
			if (whoseTranscription(optionalTranscription)) {
				redirect.addFlashAttribute("failure", "This is not your transcription to tinker with");
				return "redirect:/app/timeline";
			}
			if (isTranscriptionDeletable(optionalTranscription)) {
				redirect.addFlashAttribute("failure", "You can't at this time delete the transcription in question");
				return "redirect:/app/timeline";
			}
			Post post = optionalTranscription.get().getPost();
			post.setHasTranscription(false);
			if (post.isHasComments() == false && post.isHasOneComment() == false) {
				post.setEditableByAuthor(true);
				post.setDeletableByAuthor(true);
			}

			this.postRepository.save(post);

			Profile authorProfile = optionalTranscription.get().getProfile();
			if (authorProfile.isHasMadeOneTranscription() == true) {
				authorProfile.setHasMadeOneTranscription(false);
				authorProfile.setHasMadeTranscriptions(false);
			}

			if (authorProfile.isHasMadeTranscriptions() == true && authorProfile.getTranscriptions().size() == 2)
				authorProfile.setHasMadeOneTranscription(true);

			this.profileRepository.save(authorProfile);
			this.transcriptionRepository.delete(optionalTranscription.get());
			redirect.addFlashAttribute("success",
					"You've successfully deleted the transcription that was called"
							+ optionalTranscription.get().getName() + ", "
							+ sessionManager.getLoggedInUser().getUsername() + ". So don't scroll for it.");
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
		return optionalTranscription.get().isEditableByAuthor() == false && 
				optionalTranscription.get().getAuthor().getId() == sessionManager.getLoggedInUser().getId();
	}
	
	private boolean isTranscriptionDeletable(Optional<Transcription> optionalTranscription) {
		return optionalTranscription.get().isDeletableByAuthor() == false && 
					optionalTranscription.get().getAuthor().getId() == sessionManager.getLoggedInUser().getId();
	}
}
