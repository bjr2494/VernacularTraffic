package acc.capstone;

import java.util.Locale;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.WebUtils;

@Controller
@Configuration
@RequestMapping("/app")
public class LanguageController {

	@Autowired
	ProfileRepository profileRepository;

	@Autowired
	SessionManager sessionManager;

	@Autowired
	UserRepository userRepository;

	@Autowired
	PostRepository postRepository;

	private static final String LOCALE_ATTR = "language";

	@GetMapping("/registrationLanguagePreference/{language}")
	public String registrationLanguagePreference(@PathVariable String language, HttpServletRequest request,
			RedirectAttributes redirect) {
		if (language.equals("en")) {
			WebUtils.setSessionAttribute(request, LOCALE_ATTR, Locale.ENGLISH);
			return "redirect:/app/register";
		}

		if (language.equals("fr")) {
			WebUtils.setSessionAttribute(request, LOCALE_ATTR, Locale.FRENCH);
			return "redirect:/app/register";
		} else
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
	}

	@GetMapping("/loginLanguagePreference/{language}")
	public String loginLanguagePreference(@PathVariable String language, HttpServletRequest request,
			RedirectAttributes redirect) {
		if (language.equals("en")) {
			WebUtils.setSessionAttribute(request, LOCALE_ATTR, Locale.ENGLISH);
			return "redirect:/app/login";
		}
		if (language.equals("fr")) {
			WebUtils.setSessionAttribute(request, LOCALE_ATTR, Locale.FRENCH);
			return "redirect:/app/login";
		} else
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
	}
	
	/*
	 * @GetMapping("/postLanguagePreference/{language}") public String
	 * postLanguagePreference(@PathVariable String language, HttpServletRequest
	 * request, RedirectAttributes redirect) { if (isLoggedIn()) {
	 * redirect.addFlashAttribute("message",
	 * "Why should you be allowed to do such as a non-logged-in user?"); return
	 * "redirect:/app/login"; }
	 * 
	 * if (language.equals("en")) { WebUtils.setSessionAttribute(request,
	 * LOCALE_ATTR, Locale.ENGLISH); return "redirect:/app/post/" +
	 * this.sessionManager.getLoggedInUser().getProfile().getId(); }
	 * 
	 * if (language.equals("fr")) { WebUtils.setSessionAttribute(request,
	 * LOCALE_ATTR, Locale.FRENCH); return "redirect:/app/post/" +
	 * this.sessionManager.getLoggedInUser().getProfile().getId(); } else throw new
	 * ResponseStatusException(HttpStatus.UNAUTHORIZED); }
	 */

	@GetMapping("/preferredLanguageProfileCreation/{profileId}")
	public String preferredLanguageProfileCreation(@PathVariable int profileId, RedirectAttributes redirect,
			Model model) {
		if (isLoggedIn()) {
			redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in user?");
			return "redirect:/app/login";
		}

		Optional<Profile> optionalProfile = profileRepository.findById(profileId);
		if (optionalProfile.isPresent()) {
			if (whoseProfile(optionalProfile)) {
				redirect.addFlashAttribute("failure", "You cannot make profile changes for another user");
				return "redirect:/app/timeline";
			}

			if (this.sessionManager.getLoggedInUser().getProfile().getLanguages().size() == 1) {
				redirect.addFlashAttribute("failure", "Your profile has only one language");
				return "redirect:/app/timeline";
			}

			model.addAttribute("preferredLanguage", new PreferredLanguage());
			model.addAttribute("profile", optionalProfile.get());
			return "preferredLanguage";
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	@PostMapping("/preferredLanguageProfileCreation/{profileId}")
	public String preferredLanguageProfileCreation1(@PathVariable int profileId, RedirectAttributes redirect,
			Model model, HttpServletRequest request, @Valid PreferredLanguage preferredLanguage, Errors errors) {
		System.out.println(errors);
		if (isLoggedIn()) {
			redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in user?");
			return "redirect:/app/login";
		}

		Optional<Profile> optionalProfile = profileRepository.findById(profileId);
		if (optionalProfile.isPresent()) {
			if (whoseProfile(optionalProfile)) {
				redirect.addFlashAttribute("failure", "You cannot make profile changes for another user");
				return "redirect:/app/timeline";
			}

			if (this.sessionManager.getLoggedInUser().getProfile().getLanguages().size() == 1) {
				redirect.addFlashAttribute("failure", "Your profile has only one language");
				return "redirect:/app/timeline";
			}

			if (!optionalProfile.get().getLanguages().contains(preferredLanguage.getName())) {
				errors.rejectValue("name", "bad value", "the chosen value is unsupported");
			}

			if (errors.hasErrors()) {
				model.addAttribute("preferredLanguage", preferredLanguage);
				model.addAttribute("profile", optionalProfile.get());
				return "preferredLanguage";
			}

			optionalProfile.get().setPreferredLanguage(preferredLanguage.getName());
			this.profileRepository.save(optionalProfile.get());

			redirect.addFlashAttribute("success",
					"Hooray, " + sessionManager.getLoggedInUser().getUsername() + "! Your preferred language is "
							+ preferredLanguage.getName().toString() + ", or the one that you're reading now!");
			return "redirect:/app/timeline";
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	/*
	 * @GetMapping("/postLanguageChoice/{profileId}") public String
	 * postLanguageChoice(@PathVariable int profileId, RedirectAttributes redirect,
	 * Model model) { if (isLoggedIn()) { redirect.addFlashAttribute("message",
	 * "Why should you be allowed to do such as a non-logged-in user?"); return
	 * "redirect:/app/login"; }
	 * 
	 * Optional<Profile> optionalProfile = profileRepository.findById(profileId); if
	 * (optionalProfile.isPresent()) { if (whoseProfile(optionalProfile)) {
	 * redirect.addFlashAttribute("failure",
	 * "You cannot make profile changes for another user"); return
	 * "redirect:/app/timeline"; }
	 * 
	 * if (this.sessionManager.getLoggedInUser().getProfile().getLanguages().size()
	 * == 1) { redirect.addFlashAttribute("failure",
	 * "Your profile has only one language"); return "redirect:/app/timeline"; }
	 * 
	 * model.addAttribute("postLanguageChoice", new PostLanguage());
	 * model.addAttribute("profile", optionalProfile.get()); return "postLanguage";
	 * } else throw new ResponseStatusException(HttpStatus.NOT_FOUND); }
	 */

	@GetMapping("/postLanguageChoice/{postId}")
	public String postLanguageChoice(@PathVariable int postId, RedirectAttributes redirect, Model model, 
			HttpServletRequest request) {
		if (isLoggedIn()) {
			redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in user?");
			return "redirect:/app/login";
		}
		Optional<Post> optionalPost = this.postRepository.findById(postId);
		if (optionalPost.isPresent()) {
			if (isLanguageEnglish(request)) {
				model.addAttribute("currentLanguageAlmostPost", "English");
			}

			if (isLanguageFrench(request)) {
				model.addAttribute("currentLanguageAlmostPost", "French");
			}
			
			model.addAttribute("post", optionalPost.get());
			return "postLanguage";
		}
		else throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}
	
	@PostMapping("/postLanguageChoice/{postId}")
	public String postLanguageChoice1(@PathVariable int postId,
			RedirectAttributes redirect, Model model, HttpServletRequest request) {
		if (isLoggedIn()) {
			redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in user?");
			return "redirect:/app/login";
		}
		Optional<Post> optionalPost = this.postRepository.findById(postId);
		if (optionalPost.isPresent()) {
			PostLanguage postLanguage = new PostLanguage();
			if (isLanguageEnglish(request)) {
				postLanguage.setName(optionalPost.get().getProfile().getLanguages().get(0));
			}

			if (isLanguageFrench(request)) {
				postLanguage.setName(optionalPost.get().getProfile().getLanguages().get(1));
			}
			
			optionalPost.get().setPostLanguage(postLanguage.getName());
			this.postRepository.save(optionalPost.get());
			
			redirect.addFlashAttribute("success",
					"You've successfully made a post called " + optionalPost.get().getName() + ", whose language is "
							+ optionalPost.get().getPostLanguage() + ", "
							+ this.sessionManager.getLoggedInUser().getUsername());
			return "redirect:/app/timeline";
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	private boolean isLanguageFrench(HttpServletRequest request) {
		return WebUtils.getSessionAttribute(request, LOCALE_ATTR) == Locale.FRENCH;
	}

	private boolean isLanguageEnglish(HttpServletRequest request) {
		return WebUtils.getSessionAttribute(request, LOCALE_ATTR) == Locale.ENGLISH;
	}

	private boolean whoseProfile(Optional<Profile> optionalProfile) {
		return optionalProfile.get().getId() != this.sessionManager.getLoggedInUser().getId();
	}

	@Bean
	public LocaleResolver localeResolver() {
		SessionLocaleResolver r = new SessionLocaleResolver();
		r.setLocaleAttributeName(LOCALE_ATTR);
		return r;
	}

	private boolean isLoggedIn() {
		return !sessionManager.isLoggedIn();
	}

}
