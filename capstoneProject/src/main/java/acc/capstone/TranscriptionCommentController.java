package acc.capstone;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

	@GetMapping("/transcriptionComment/{transcriptionId}")
	public String transcriptionComment(@PathVariable int transcriptionId, Model model, RedirectAttributes redirect) {
		if (isLoggedIn()) {
			redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in user?");
			return "redirect:/app/login";
		}
		Optional<Transcription> optionalTranscription = transcriptionRepository.findById(transcriptionId);
		if (optionalTranscription.isPresent()) {
			if (commentOnOwnTranscription(optionalTranscription)) {
				redirect.addFlashAttribute("failure",
						"You can't at this time make a comment on your own transcription");
				return "redirect:/app/timeline";
			}
			// model.addAttribute("loggedInUser", sessionManager.getLoggedInUser());
			model.addAttribute("transcription", optionalTranscription.get());
			model.addAttribute("post", optionalTranscription.get().getPost());
			model.addAttribute("transcriptionComment", new TranscriptionComment());
			// model.addAttribute("transcriptionProfile",
			// optionalTranscription.get().getProfile());
			// model.addAttribute("postProfile",
			// optionalTranscription.get().getPost().getProfile());
			return "transcriptionComment";
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	@PostMapping("/transcriptionComment/{transcriptionId}")
	public String transcriptionComment(@PathVariable int transcriptionId,
			@Valid TranscriptionComment transcriptionComment, Errors errors, Model model, RedirectAttributes redirect) {
		if (isLoggedIn()) {
			redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in user?");
			return "redirect:/app/login";
		}
		Optional<Transcription> optionalTranscription = transcriptionRepository.findById(transcriptionId);
		if (optionalTranscription.isPresent()) {
			if (commentOnOwnTranscription(optionalTranscription)) {
				redirect.addFlashAttribute("failure",
						"You can't at this time make a comment on your own transcription");
				return "redirect:/app/timeline";
			}
			if (transcriptionComment.getContent().length() < 4) {
				errors.rejectValue("content", "bad value",
						"Please increase the comment's length in order to satisfy our requirement");
			}
			if (transcriptionComment.getContent().length() > 60) {
				errors.rejectValue("content", "bad value",
						"Please shorten the comment's length in order to satisfy our requirement");
			}
			if (errors.hasErrors()) {
				// model.addAttribute("loggedInUser", sessionManager.getLoggedInUser());
				model.addAttribute("post", optionalTranscription.get().getPost());
				model.addAttribute("transcription", optionalTranscription.get());
				// model.addAttribute("transcriptionComment", transcriptionComment);
				return "transcriptionComment";
			} else {
				Transcription transcription = optionalTranscription.get();

				if (transcription.isEditableByAuthor() == true)
					transcription.setEditableByAuthor(false);
				if (transcription.isDeletableByAuthor() == true)
					transcription.setDeletableByAuthor(false);
				if (transcription.isCommentableByAuthor() == false)
					transcription.setCommentableByAuthor(true);
				if (transcription.isHasComments() == false)
					transcription.setHasComments(true);
				if (transcription.isHasOneComment() == false && transcription.getTranscriptionComments().size() == 0)
					transcription.setHasOneComment(true);

				this.transcriptionRepository.save(transcription);

				transcriptionComment.setTranscription(transcription);
				transcriptionComment.setAuthor(sessionManager.getLoggedInUser());
				transcriptionComment.setCommentDate(LocalDate.now());
				transcriptionComment.setCommentTime(LocalTime.now());
				transcriptionComment.setProfile(sessionManager.getLoggedInUser().getProfile());
				transcriptionComment.setDeletableByAuthor(true);
				transcriptionComment.setTranscriptionCommentLanguage(transcription.getTranscriptionLanguage());

				for (TranscriptionComment tc : this.transcriptionCommentRepository
						.findAllByTranscriptionId(optionalTranscription.get().getId())) {
					if (tc.getCommentDate().isBefore(transcriptionComment.getCommentDate())) {
						tc.setDeletableByAuthor(false);
						this.transcriptionCommentRepository.save(tc);
					}
					if (tc.getCommentDate().equals(transcriptionComment.getCommentDate())) {
						if (tc.getCommentTime().isBefore(transcriptionComment.getCommentTime())) {
							tc.setDeletableByAuthor(false);
							this.transcriptionCommentRepository.save(tc);
						}
					}
				}

				this.transcriptionCommentRepository.save(transcriptionComment);

				// profile of the author of the transcription comment
				Profile authorProfile = transcriptionComment.getAuthor().getProfile();
				if (authorProfile.isHasMadeTranscriptionComments() == false) {
					authorProfile.setHasMadeTranscriptionComments(true);
					authorProfile.setHasMadeOneTranscriptionComment(true);
				}
				if (authorProfile.isHasMadeOneTranscriptionComment() == true)
					authorProfile.setHasMadeTranscriptionComments(false);
				if (authorProfile.isHasMadeOneTranscriptionComment() == false
						&& authorProfile.getTranscriptionComments().size() == 0)
					authorProfile.setHasMadeOneTranscriptionComment(true);

				// authorProfile.getTranscriptionComments().add(transcriptionComment);
				this.profileRepository.save(authorProfile);

				redirect.addFlashAttribute("success",
						"Scroll accordingly to see your comment on the transcription called " + transcription.getName()
								+ ", " + sessionManager.getLoggedInUser().getUsername());
				return "redirect:/app/timeline";
			}
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	@PostMapping("/deleteTranscriptionComment/{transcriptionCommentId}")
	public String deleteTranscriptionComment(@PathVariable int transcriptionCommentId, RedirectAttributes redirect) {
		if (isLoggedIn()) {
			redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in user?");
			return "redirect:/app/login";
		}
		Optional<TranscriptionComment> optionalTranscriptionComment = transcriptionCommentRepository
				.findById(transcriptionCommentId);
		if (optionalTranscriptionComment.isPresent()) {
			if (isTranscriptionCommentDeletable(optionalTranscriptionComment)) {
				redirect.addFlashAttribute("failure",
						"You can't at this time delete a transcription comment from the post in question");
				return "redirect:/app/timeline";
			}
			if (whoseTranscriptionComment(optionalTranscriptionComment)) {
				redirect.addFlashAttribute("failure", "This is not your transcription comment to tinker with");
				return "redirect:/app/timeline";
			}
			Transcription transcription = optionalTranscriptionComment.get().getTranscription();
			if (transcription.isHasOneComment() == true) {
				transcription.setHasOneComment(false);
				transcription.setHasComments(false);
			}

			if (transcription.isHasComments() == true && transcription.getTranscriptionComments().size() == 2)
				transcription.setHasOneComment(true);

			if (transcription.isDeletableByAuthor() == false && transcription.getTranscriptionComments().size() == 1)
				transcription.setDeletableByAuthor(true);

			if (transcription.isEditableByAuthor() == false && transcription.getTranscriptionComments().size() == 1)
				transcription.setEditableByAuthor(true);

			if (transcription.isCommentableByAuthor() == true && transcription.getTranscriptionComments().size() == 1)
				transcription.setCommentableByAuthor(false);

			this.transcriptionRepository.save(transcription);

			Profile authorProfile = optionalTranscriptionComment.get().getProfile();
			if (authorProfile.isHasMadeOneTranscriptionComment() == true) {
				authorProfile.setHasMadeOneTranscriptionComment(false);
				authorProfile.setHasMadeTranscriptionComments(false);
			}

			if (authorProfile.isHasMadeTranscriptionComments() == true
					&& authorProfile.getTranscriptionComments().size() == 2) {
				authorProfile.setHasMadeOneTranscriptionComment(true);
			}

			this.profileRepository.save(authorProfile);
			this.transcriptionCommentRepository.delete(optionalTranscriptionComment.get());

			redirect.addFlashAttribute("success",
					"You've successfully deleted your comment from the transcription that is called "
							+ transcription.getName() + ", " + sessionManager.getLoggedInUser().getUsername()
							+ ". So don't scroll for it.");
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
}
