package acc.capstone;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/app") // to be changed to name of app once such is decided
public class TimelineController {

	@Autowired
	PostRepository postRepository;

	@Autowired
	PostCommentRepository postCommentRepository;

	@Autowired
	TranscriptionCommentRepository transcriptionCommentRepository;

	@Autowired
	TranscriptionRepository transcriptionRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	ProfileRepository profileRepository;

	@Autowired
	SessionManager sessionManager;

	@Autowired
	ApplicationManager applicationManager;

	private final static int PAGE_SIZE = 5;

	@GetMapping("/timeline")
	public String timeline(Model model, RedirectAttributes redirect) {
		//System.out.println(this.sessionManager.getLoggedInUser());
		if (isLoggedIn()) {
			redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in user?");
			return "redirect:/app/login";
		}

		/*
		 * if (numberOfLanguages()) { model.addAttribute("timelineLanguageChange",
		 * "Click here to change your language"); }
		 */

		PageRequest page = PageRequest.of(0, PAGE_SIZE, Sort.by(Order.desc("id")));
		Page<Post> postPage = postRepository.findAll(page);
		model.addAttribute("posts", postPage);

		for (Post p : postRepository.findAll()) {
			if (this.sessionManager.getLoggedInUser().getId() == p.getAuthor().getId()) {
				if (p.isHasComments() == false && p.isHasTranscription() == false) {
					p.setCommentableByAuthor(false);
					p.setEditableByAuthor(true);
					p.setDeletableByAuthor(true);
				}

				if (p.isHasComments() == false && p.isHasTranscription() == true) {
					p.setCommentableByAuthor(false);
					p.setEditableByAuthor(false);
					p.setDeletableByAuthor(false);
				}

				if (p.isHasComments() == true) {
					p.setCommentableByAuthor(true);
					p.setEditableByAuthor(false);
					p.setDeletableByAuthor(false);

					PostComment max = p.getPostComments().get(0);

					for (PostComment pc : p.getPostComments()) {
						pc.setDeletableByAuthor(false);
						if (pc.getCommentDate().isAfter(max.getCommentDate())) {
							max = pc;
							max.setDeletableByAuthor(true);
						}
						if (pc.getCommentDate().isEqual(max.getCommentDate())) {
							if (pc.getCommentTime().isAfter(max.getCommentTime())) {
								max = pc;
								max.setDeletableByAuthor(true);
							}
						}
					}

				}
			}

			if (this.sessionManager.getLoggedInUser().getId() != p.getAuthor().getId()) {
				if (p.isHasTranscription() == true) {
					if (p.getTranscription().getAuthor().getId() == this.sessionManager.getLoggedInUser().getId()) {
						if (p.getTranscription().isHasComments() == true) {
							p.getTranscription().setCommentableByAuthor(true);
							p.getTranscription().setEditableByAuthor(false);
							p.getTranscription().setDeletableByAuthor(false);

							TranscriptionComment max = p.getTranscription().getTranscriptionComments().get(0);

							for (TranscriptionComment tc : p.getTranscription().getTranscriptionComments()) {
								tc.setDeletableByAuthor(false);
								if (tc.getCommentDate().isAfter(max.getCommentDate())) {
									max = tc;
									max.setDeletableByAuthor(true);
								}
								if (tc.getCommentDate().isEqual(max.getCommentDate())) {
									if (tc.getCommentTime().isAfter(max.getCommentTime())) {
										max = tc;
										max.setDeletableByAuthor(true);
									}
								}
							}
						}
						if (p.getTranscription().isHasComments() == false) {
							p.getTranscription().setCommentableByAuthor(false);
							p.getTranscription().setEditableByAuthor(true);
							p.getTranscription().setDeletableByAuthor(true);
						}
					}
				}
			}
		}	
		
		
			List<Post> posts = postRepository.findAllByOrderByIdDesc();
			this.applicationManager.setNumPosts(posts.size());

			String plural = posts.size() == 1 ? "Post" : "Posts";
			this.applicationManager.setPlural(plural);

			String verb = posts.size() == 1 ? "Is" : "Are";
			this.applicationManager.setVerb(verb);

		return "timeline";
	}

	@GetMapping("/timeline/{pageNumber}")
	public String timeline(Model model, @PathVariable int pageNumber, RedirectAttributes redirect) {
		if (isLoggedIn()) {
			redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in user?");
			return "redirect:/app/login";
		}

		/*
		 * if (numberOfLanguages()) { model.addAttribute("timelineLanguageChange",
		 * "Click here to change your language"); }
		 */

		Page<Post> page = postRepository.findAll(PageRequest.of(pageNumber - 1, PAGE_SIZE, Sort.by(Order.desc("id"))));
		model.addAttribute("posts", page);
		return "timeline";
	}

	/*
	 * private boolean numberOfLanguages() { return
	 * this.sessionManager.getLoggedInUser().getProfile().getLanguages().size() ==
	 * 2; }
	 */

	private boolean isLoggedIn() {
		return !sessionManager.isLoggedIn();
	}
}