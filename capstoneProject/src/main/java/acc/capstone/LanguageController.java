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

	@GetMapping("/timelineLanguagePreference/{language}")
	public String timelineLanguagePreference(@PathVariable String language, HttpServletRequest request,
			RedirectAttributes redirect) {
		
		this.isLoggedIn();
		
		if (language.equals("en")) {
			WebUtils.setSessionAttribute(request, LOCALE_ATTR, Locale.ENGLISH);
			return "redirect:/app/timeline";
		}
		if (language.equals("fr")) {
			WebUtils.setSessionAttribute(request, LOCALE_ATTR, Locale.FRENCH);
			return "redirect:/app/timeline";
		} else
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
	}
	
	@GetMapping("/profileLanguagePreference/{language}")
	public String profileLanguagePreference(@PathVariable String language, HttpServletRequest request,
			RedirectAttributes redirect) {

		Locale whichLocale = localeResolver().resolveLocale(request);
		
		if (isLoggedIn()) {
			if (whichLocale.equals(Locale.ENGLISH)) {
				redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in user?");
			}
			
			if (whichLocale.equals(Locale.FRENCH)) {
				redirect.addFlashAttribute("message", 
						"Pourquoi devriez-vous être autorisé à faire comme utilisateur non connecté?");
			}
		}
		
		Profile profile = this.profileRepository.findByUser(this.sessionManager.getLoggedInUser());
		if (profile.getLanguages().size() == 1) {
			if (profile.getPreferredLanguage().equals(Language.ENGLISH)) {
				redirect.addFlashAttribute("failure", "Your profile has only one language");
			}
			if (profile.getPreferredLanguage().equals(Language.FRENCH)) {
				redirect.addFlashAttribute("failure", "Votre profil n'a qu'une langue");
			}
			return "redirect:/app/timeline";
		}
		
		if (language.equals("en")) {
			WebUtils.setSessionAttribute(request, LOCALE_ATTR, Locale.ENGLISH);
			if (profile.getLanguages().size() == 1)
				redirect.addFlashAttribute("message", "You've changed your language to English, which is your only language");
			if (profile.getLanguages().size() > 1)
				redirect.addFlashAttribute("message", "You've changed your language to English");
			return "redirect:/app/profile/" + profile.getId();
		}
		if (language.equals("fr")) {
			WebUtils.setSessionAttribute(request, LOCALE_ATTR, Locale.FRENCH);
			if (profile.getLanguages().size() == 1)
				redirect.addFlashAttribute("message", "Vous avez changé votre langue à français, lequel est votre seule langue");
			if (profile.getLanguages().size() > 1)
				redirect.addFlashAttribute("message", "Vous avez changé votre langue à français");
			return "redirect:/app/profile/" + profile.getId();
		} else
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
	}

	@GetMapping("/postLanguagePreference/{language}")
	public String postLanguagePreference(@PathVariable String language, HttpServletRequest request, 
				RedirectAttributes redirect) {

		Locale whichLocale = localeResolver().resolveLocale(request);
		
		if (isLoggedIn()) {
			if (whichLocale.equals(Locale.ENGLISH)) {
				redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in user?");
			}
			
			if (whichLocale.equals(Locale.FRENCH)) {
				redirect.addFlashAttribute("message", 
						"Pourquoi devriez-vous être autorisé à faire comme utilisateur non connecté?");
			}
		}
		
		Profile profile = this.profileRepository.findByUser(this.sessionManager.getLoggedInUser());
		if (profile.getLanguages().size() == 1) {
			if (profile.getPreferredLanguage().equals(Language.ENGLISH)) {
				redirect.addFlashAttribute("failure", "Your profile has only one language");
			}
			if (profile.getPreferredLanguage().equals(Language.FRENCH)) {
				redirect.addFlashAttribute("failure", "Votre profil n'a qu'une langue");
			}
			return "redirect:/app/timeline";
		}
		
		if (language.equals("en")) {
			WebUtils.setSessionAttribute(request, LOCALE_ATTR, Locale.ENGLISH);
			redirect.addFlashAttribute("message",
					"Your working language (and soon-to-be post language, unless you change it again, is English");
			return "redirect:/app/post/" + profile.getId();
		}
		
		if (language.equals("fr")) {
			WebUtils.setSessionAttribute(request, LOCALE_ATTR, Locale.FRENCH);
			redirect.addFlashAttribute("message", "Le français est votre langue actuelle"
					+ " (et votre prochaine langue de publication, à moins que vous ne la changiez à nouveau)");
			return "redirect:/app/post/" + profile.getId();
		}
		else throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
	}
	
	@GetMapping("/preferredLanguageProfileCreation/{profileId}")
	public String preferredLanguageProfileCreation(@PathVariable int profileId, RedirectAttributes redirect,
			Model model, HttpServletRequest request) {
		
		Locale whichLocale = makeLocaleAndIsLoggedIn(redirect, request);

		Optional<Profile> optionalProfile = profileRepository.findById(profileId);
		if (optionalProfile.isPresent()) {
			if (whoseProfile(optionalProfile)) {
				if (localeIsEnglish(whichLocale)) {
					redirect.addFlashAttribute("failure", "You cannot make profile changes for another user");
				}
				
				if (localeIsFrench(whichLocale)) {
					redirect.addFlashAttribute("failure", "Vous ne pouvez pas modifier le profil d'un autre utilisateur");
				}
				return "redirect:/app/timeline";
			}

			if (this.sessionManager.getLoggedInUser().getProfile().getLanguages().size() == 1) {
				if (localeIsEnglish(whichLocale)) {
					redirect.addFlashAttribute("failure", "Your profile has only one language");
				}
				
				if (localeIsFrench(whichLocale)) {
					redirect.addFlashAttribute("failure", "Votre profil n'a qu'une langue");
				}
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
		
		Locale whichLocale = makeLocaleAndIsLoggedIn(redirect, request);

		Optional<Profile> optionalProfile = profileRepository.findById(profileId);
		if (optionalProfile.isPresent()) {
			if (!optionalProfile.get().getLanguages().contains(preferredLanguage.getName())) {
				if (localeIsEnglish(whichLocale)) {
					errors.rejectValue("name", "bad value", "You must make a choice");
				}
				
				if (localeIsFrench(whichLocale)) {
					errors.rejectValue("name", "mauvaise valeur", "Vous devez choisir une option");
				}
			}

			if (errors.hasErrors()) {
				model.addAttribute("preferredLanguage", preferredLanguage);
				model.addAttribute("profile", optionalProfile.get());
				return "preferredLanguage";
			}

			optionalProfile.get().setPreferredLanguage(preferredLanguage.getName());
			this.profileRepository.save(optionalProfile.get());
			
			if (preferredLanguage.getName().equals(Language.ENGLISH)) {
				WebUtils.setSessionAttribute(request, LOCALE_ATTR, Locale.ENGLISH);
			}
			
			if (preferredLanguage.getName().equals(Language.FRENCH)) {
				WebUtils.setSessionAttribute(request, LOCALE_ATTR, Locale.FRENCH);
			}

			if (localeIsEnglish(whichLocale)) {
				redirect.addFlashAttribute("success",
						"Hooray, " + sessionManager.getLoggedInUser().getUsername() + "! Your preferred language is English");
			}

			if (localeIsFrench(whichLocale)) {
				redirect.addFlashAttribute("success",
						"Bravo, " + sessionManager.getLoggedInUser().getUsername() + "! Votre langue préférée est le français");
			}
			return "redirect:/app/timeline";
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	private Locale makeLocaleAndIsLoggedIn(RedirectAttributes redirect, HttpServletRequest request) {
		Locale whichLocale = localeResolver().resolveLocale(request);
		if (isLoggedIn()) {
			if (localeIsEnglish(whichLocale)) {
				redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in user?");
			}
			
			if (localeIsFrench(whichLocale)) {
				redirect.addFlashAttribute("message", 
						"Pourquoi devriez-vous être autorisé à faire comme utilisateur non connecté?");
			}
		}
		return whichLocale;
	}

	private boolean localeIsFrench(Locale whichLocale) {
		return whichLocale.equals(Locale.FRENCH);
	}

	private boolean localeIsEnglish(Locale whichLocale) {
		return whichLocale.equals(Locale.ENGLISH);
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
