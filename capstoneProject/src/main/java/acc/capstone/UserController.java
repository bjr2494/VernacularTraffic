package acc.capstone;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
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
public class UserController {

	@Autowired
	SessionManager sessionManager;

	@Autowired
	UserRepository userRepository;

	@Autowired
	ProfileRepository profileRepository;

	private static final String LOCALE_ATTR = "language";

	@GetMapping("/editUsername/{userId}")
	public String editUsername(@PathVariable int userId, Model model, RedirectAttributes redirect,
			HttpServletRequest request) {
		Locale whichLocale = localeResolverforUserController().resolveLocale(request);
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
			if (whichUser(optionalUser)) {

				if (whichLocale.equals(Locale.ENGLISH)) {
					redirect.addFlashAttribute("failure", "You cannot edit the username of another user");
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					redirect.addFlashAttribute("failure",
							"Vous ne pouvez pas éditer le nom d'utilisateur d'un autre utilisateur");
				}

				return "redirect:/app/timeline";
			}

			model.addAttribute("username", new Username());
			return "editUsername";
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	@PostMapping("/editUsername/{userId}")
	public String editUsername(@PathVariable int userId, @Valid Username username, Errors errors,
			RedirectAttributes redirect, Model model, HttpServletRequest request) {

		Locale whichLocale = localeResolverforUserController().resolveLocale(request);
		Optional<User> optionalUser = this.userRepository.findById(userId);
		if (optionalUser.isPresent()) {

			for (User u : this.userRepository.findAll()) {
				if (username.getUsername().equals(u.getUsername())) {

				}
				if (whichLocale.equals(Locale.ENGLISH)) {
					errors.rejectValue("username", "bad value", "this username is taken already");
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					errors.rejectValue("username", "mauvaise valeur", "ce nom d'utilisateur est déjà pris");
				}

			}

			if (username.getUsername().length() < 5) {
				if (whichLocale.equals(Locale.ENGLISH)) {
					errors.rejectValue("username", "bad value", "something longer");
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					errors.rejectValue("username", "mauvaise valeur", "quelque chose de plus");
				}

			}

			if (errors.hasErrors()) {
				model.addAttribute("newUsername", username);
				return "editUsername";
			}

			else {

				User user = optionalUser.get();
				user.setUsername(username.getUsername());

				// this.userRepository.save(user);
				this.sessionManager.login(user);

				if (whichLocale.equals(Locale.ENGLISH)) {
					redirect.addFlashAttribute("success", "You've successfully changed your username to "
							+ sessionManager.getLoggedInUser().getUsername() + ". Enjoy thy new identity.");
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					redirect.addFlashAttribute("success", "Vous avez changé votre nom d'utilisateur avec succès en "
							+ sessionManager.getLoggedInUser().getUsername() + "Profitez de votre nouvelle identité.");
				}

				return "redirect:/app/timeline";
			}
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	@GetMapping("/register")
	public String userRegister(Model model, RedirectAttributes redirect, HttpServletRequest request) {

		Locale whichLocale = localeResolverforUserController().resolveLocale(request);

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
		
		Profile profile = this.profileRepository.findByUser(this.sessionManager.getLoggedInUser());
		if (alreadyLoggedIn() && profile != null) {
			if (whichLocale.equals(Locale.ENGLISH)) {
				redirect.addFlashAttribute("failure", "But of course you've registered!");
			}

			if (whichLocale.equals(Locale.FRENCH)) {
				redirect.addFlashAttribute("failure", "Mais bien sûr, vous êtes inscrit!");
			}

			return "redirect:/app/timeline";
		}

		if (alreadyLoggedIn() && profile == null) {
			if (whichLocale.equals(Locale.ENGLISH)) {
				redirect.addFlashAttribute("error",
						"But you've already made your username and password. Please continue");
			}

			if (whichLocale.equals(Locale.FRENCH)) {
				redirect.addFlashAttribute("error",
						"Mais vous avez déjà fait votre nom d'utilisateur et mot de passe. Veuillez continuer");
			}

			return "redirect:/app/createProfile/" + this.sessionManager.getLoggedInUser().getId();
		}
		model.addAttribute("registrationAttempt", new RegistrationAttempt());
		return "registration";
	}

	@PostMapping("/register")
	public String userRegister(@Valid RegistrationAttempt registrationAttempt, Errors errors, Model model,
			RedirectAttributes redirect, HttpServletRequest request) {

		Locale whichLocale = localeResolverforUserController().resolveLocale(request);
		for (User u : userRepository.findAll()) {
			if (registrationAttempt.getUsername().equals(u.getUsername())) {
				if (whichLocale.equals(Locale.ENGLISH))
					errors.rejectValue("username", "bad value", "The attempted username is already taken");
				if (whichLocale.equals(Locale.FRENCH))
					errors.rejectValue("username", "mauvaise valeur", "le nom d'utilisateur tenté est déjà pris");
			}
		}

		if (registrationAttempt.getUsername().length() < 5) {
			if (whichLocale.equals(Locale.ENGLISH))
				errors.rejectValue("username", "bad value", "Such is too short for a username here; retry");
			if (whichLocale.equals(Locale.FRENCH))
				errors.rejectValue("username", "mauvaise valeur", "Trop court pour un nom d'utilisateur; réssayez");

		}

		if (registrationAttempt.getUsername().length() > 15) {
			if (whichLocale.equals(Locale.ENGLISH))
				errors.rejectValue("username", "bad value", "Such is too long for a username here; retry");
			if (whichLocale.equals(Locale.FRENCH))
				errors.rejectValue("username", "mauvaise valeur", "Trop long pour un nom d'utilisateur; réssayez");
		}

		if (registrationAttempt.getPasswordOne().length() < 5) {
			if (whichLocale.equals(Locale.ENGLISH)) {
				errors.rejectValue("passwordOne", "bad value", "Such is not secure enough for a password here; retry");
				errors.rejectValue("passwordTwo", "bad value", "Perhaps this is fine but the first is invalid");
				if (registrationAttempt.getPasswordTwo().length() < 5)
					errors.rejectValue("passwordTwo", "bad value", "Such is likewise not secure enough");
			}
			if (whichLocale.equals(Locale.FRENCH)) {
				errors.rejectValue("passwordOne", "mauvaise valeur", "Pas assez sécurisé pour un mot de passe");
				errors.rejectValue("passwordTwo", "mauvaise valeur",
						"Peut-être que celui-ci est bien mais le premier n'est pas valide");
				if (registrationAttempt.getPasswordTwo().length() < 5)
					errors.rejectValue("passwordTwo", "mauvaise valeur", "Aussi pas assez sécurisé");
			}
		}

		if (registrationAttempt.getPasswordOne().length() > 15) {
			if (whichLocale.equals(Locale.ENGLISH)) {
				errors.rejectValue("passwordOne", "bad value", "Such is too secure for a password here; retry");
				errors.rejectValue("passwordTwo", "bad value", "Perhaps this is fine but the first is invalid");
				if (registrationAttempt.getPasswordTwo().length() > 15)
					errors.rejectValue("passwordTwo", "bad value", "Such is likewise too secure");
			}

			if (whichLocale.equals(Locale.FRENCH)) {
				errors.rejectValue("passwordOne", "mauvaise valeur",
						"Trop sécurisé pour un mot de passe; soyez plus simple");
				errors.rejectValue("passwordTwo", "mauvaise valeur",
						"Peut-être que celui-ci est bien mais le premier n'est pas valide");
				if (registrationAttempt.getPasswordOne().length() > 15)
					errors.rejectValue("passwordTwo", "bad value", "Aussi trop sécurisé");
			}
		}

		if (!registrationAttempt.getPasswordOne().equals(registrationAttempt.getPasswordTwo())) {
			if (whichLocale.equals(Locale.ENGLISH))
				errors.rejectValue("passwordOne", "bad value", "The passwords do not match");
			if (whichLocale.equals(Locale.FRENCH))
				errors.rejectValue("passwordOne", "bad value", "Les mots de passe ne sont pas égaux");
		}
		if (!errors.hasErrors()) {
			String salt = Hash.generateSalt();
			String hashOne = Hash.hash(registrationAttempt.getPasswordOne(), salt);
			String hashTwo = Hash.hash(registrationAttempt.getPasswordTwo(), salt);
			if (hashOne.equals(hashTwo)) {
				User user = new User();
				user.setUsername(registrationAttempt.getUsername());
				user.setPassword(hashOne);
				user.setSalt(salt);
				user.setJoinDate(LocalDate.now());

				this.userRepository.save(user);
				this.sessionManager.login(user);

				return "redirect:/app/createProfile/" + this.sessionManager.getLoggedInUser().getId();
			}
		} else
			return "registration";
		return "registration";
	}

	private boolean alreadyLoggedIn() {
		return sessionManager.isLoggedIn();
	}

	private boolean isLoggedIn() {
		return !this.sessionManager.isLoggedIn();
	}

	private boolean whichUser(Optional<User> optionalUser) {
		return optionalUser.get().getId() != sessionManager.getLoggedInUser().getId();
	}

	@Bean
	public LocaleResolver localeResolverforUserController() {
		SessionLocaleResolver r = new SessionLocaleResolver();
		r.setLocaleAttributeName(LOCALE_ATTR);
		return r;
	}
}
