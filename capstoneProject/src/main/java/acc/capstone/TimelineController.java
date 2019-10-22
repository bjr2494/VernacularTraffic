package acc.capstone;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
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
	private static final String LOCALE_ATTR = "language";

	@GetMapping("/timeline")
	public String timeline(Model model, RedirectAttributes redirect, HttpServletRequest request) {
		// System.out.println(this.sessionManager.getLoggedInUser());

		Locale whichLocale = localeResolverforTimeline().resolveLocale(request);
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

		Profile profile = profileRepository.findByUser(this.sessionManager.getLoggedInUser());

		if (profile == null) {
			if (whichLocale.equals(Locale.ENGLISH)) {
				redirect.addFlashAttribute("error", "You need to first finish creating your profile");
			}

			if (whichLocale.equals(Locale.FRENCH)) {
				redirect.addFlashAttribute("error", "Il faut que vous finissiez d'abord créer votre profil");
			}
			return "redirect:/app/createProfile/" + this.sessionManager.getLoggedInUser().getId();
		}

		PageRequest page = PageRequest.of(0, PAGE_SIZE, Sort.by(Order.desc("id")));

		if (profile.getLanguages().size() == 1) {

			Page<Post> postPage = postRepository.findAllByPostLanguage(
					this.sessionManager.getLoggedInUser().getProfile().getLanguages().get(0), page);
			model.addAttribute("posts", postPage);

			if (whichLocale.equals(Locale.ENGLISH)) {
				if (profile.getLanguages().get(0) != Language.ENGLISH) {
					redirect.addFlashAttribute("warning", "Votre langue choisie ne figure pas sur votre profil. "
							+ "Veuillez changer votre langue ou ajouter la langue actuelle à votre profil.");
					return "redirect:/app/profile/" + profile.getId();
				}
				model.addAttribute("languageMessage", "Your language is English");
			}

			if (whichLocale.equals(Locale.FRENCH)) {
				if (profile.getLanguages().get(0) != Language.FRENCH) {
					redirect.addFlashAttribute("warning", "Your chosen language is not listed on your profile. "
							+ "Please change your language or add the current one to your profile's langauges");
					return "redirect:/app/profile/" + profile.getId();
				}
				model.addAttribute("languageMessage", "Votre Langue Est Français");
			}
		}

		if (profile.getLanguages().size() == 2) {

			if (whichLocale.equals(Locale.ENGLISH)) {
				if (profile.getPreferredLanguage().equals(Language.ENGLISH))
					model.addAttribute("languageMessage", "Your languages are English and French");
				if (profile.getPreferredLanguage().equals(Language.FRENCH))
					model.addAttribute("languageMessage", "Your languages are French and English");
			}

			if (whichLocale.equals(Locale.FRENCH)) {
				if (profile.getPreferredLanguage().equals(Language.FRENCH))
					model.addAttribute("languageMessage", "Vos Langues Sont Français et Anglais");
				if (profile.getPreferredLanguage().equals(Language.ENGLISH))
					model.addAttribute("languageMessage", "Vos Langues Sont Anglais et Français");
			}

			model.addAttribute("languageChange", "change your language");
			Page<Post> postPage = postRepository.findAll(page);
			model.addAttribute("posts", postPage);
		}

		return "timeline";
	}

	@GetMapping("/timeline/{pageNumber}")
	public String timeline(Model model, @PathVariable int pageNumber, RedirectAttributes redirect,
			HttpServletRequest request) {
		Locale whichLocale = localeResolverforTimeline().resolveLocale(request);
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

		Profile profile = profileRepository.findByUser(this.sessionManager.getLoggedInUser());

		if (profile.getLanguages().size() == 1) {
			Page<Post> postPage = postRepository.findAllByPostLanguage(profile.getLanguages().get(0),
					PageRequest.of(pageNumber - 1, PAGE_SIZE, Sort.by(Order.desc("id"))));
			model.addAttribute("posts", postPage);

			if (whichLocale.equals(Locale.ENGLISH)) {
				if (profile.getLanguages().get(0) != Language.ENGLISH) {
					redirect.addFlashAttribute("warning", "Votre langue choisie ne figure pas sur votre profil. "
							+ "Veuillez changer votre langue ou ajouter la langue actuelle à votre profil.");
					return "redirect:/app/profile/" + profile.getId();
				}
				model.addAttribute("languageMessage", "Your language is English");
			}

			if (whichLocale.equals(Locale.FRENCH)) {
				if (profile.getLanguages().get(0) != Language.FRENCH) {
					redirect.addFlashAttribute("warning", "Your chosen language is not listed on your profile. "
							+ "Please change your language or add the current one to your profile's langauges");
					return "redirect:/app/profile/" + profile.getId();
				}
				model.addAttribute("languageMessage", "Votre Langue Est Français");
			}
		}

		if (profile.getLanguages().size() == 2) {

			Page<Post> postPage = postRepository
					.findAll(PageRequest.of(pageNumber - 1, PAGE_SIZE, Sort.by(Order.desc("id"))));
			model.addAttribute("posts", postPage);

			// model.addAttribute("multipleLanguageMessage", "");
			model.addAttribute("languageChange", "change your language");

			if (whichLocale.equals(Locale.ENGLISH)) {
				if (profile.getPreferredLanguage().equals(Language.ENGLISH))
					model.addAttribute("languageMessage", "Your languages are English and French");
				if (profile.getPreferredLanguage().equals(Language.FRENCH))
					model.addAttribute("languageMessage", "Your languages are French and English");
			}

			if (whichLocale.equals(Locale.FRENCH)) {
				if (profile.getPreferredLanguage().equals(Language.FRENCH))
					model.addAttribute("languageMessage", "Vos Langues Sont Français et Anglais");
				if (profile.getPreferredLanguage().equals(Language.ENGLISH))
					model.addAttribute("languageMessage", "Vos Langues Sont Anglais et Français");
			}
		}

		return "timeline";
	}

	private boolean isLoggedIn() {
		return !sessionManager.isLoggedIn();
	}

	@Bean
	public LocaleResolver localeResolverforTimeline() {
		SessionLocaleResolver r = new SessionLocaleResolver();
		r.setLocaleAttributeName(LOCALE_ATTR);
		return r;
	}
}