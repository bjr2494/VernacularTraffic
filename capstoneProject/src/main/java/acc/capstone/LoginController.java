package acc.capstone;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/app") // this will change to name of application once such is decided
public class LoginController {
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired 
	PostRepository postRepository;
	
	@Autowired
	TranscriptionRepository transcriptionRepository;
	
	@Autowired
	SessionManager sessionManager;
	
	@Autowired
	ProfileRepository profileRepository;
	
	private static final String LOCALE_ATTR = "language";
	
	
	@GetMapping("/login")
	public String login(Model model, RedirectAttributes redirect, @RequestParam(required = false) String x, 
					SessionLocaleResolver session, HttpServletRequest request) {

		Locale whichLocale = localeResolverforLoginController().resolveLocale(request);
		
		 if (alreadyLoggedIn()) {
			 if (whichLocale.equals(Locale.ENGLISH)) {
				  redirect.addFlashAttribute("failure", "But you've logged in already!");
			 }
			 
			 if (whichLocale.equals(Locale.FRENCH)) {
				 redirect.addFlashAttribute("failure", "Mais vous êtes déjà connecté!");
			 }
		  	
			  return "redirect:/app/timeline";
		  }
		  else {
			//System.out.println(x);
			model.addAttribute("loginAttempt", new LoginAttempt());
			return "login";
		}
		
	}
	
	@PostMapping("/login")
	public String login(@Valid LoginAttempt loginAttempt, Errors errors, Model model, 
			RedirectAttributes redirect, HttpServletRequest request) {
		
		Locale whichLocale = localeResolverforLoginController().resolveLocale(request);
		if (alreadyLoggedIn()) {
			 if (whichLocale.equals(Locale.ENGLISH)) {
				  redirect.addFlashAttribute("failure", "But you've logged in already!");
			 }
			 
			 if (whichLocale.equals(Locale.FRENCH)) {
				 redirect.addFlashAttribute("failure", "Mais vous êtes déjà connecté!");
			 }
		  	
			  return "redirect:/app/timeline";
		}
		if (!errors.hasErrors()) {
			Optional<User> optionalUser = this.userRepository.findByUsername(loginAttempt.getUsername());
			
			if (optionalUser.isPresent()) {
				//Profile profile = this.profileRepository.findByUser(optionalUser.get());
				String hashIncoming = Hash.hash(loginAttempt.getPassword(), optionalUser.get().getSalt());
				if (hashIncoming.equals(optionalUser.get().getPassword())) {
					this.sessionManager.login(optionalUser.get());
					  return "redirect:/app/timeline";
				}
				else {
					if (whichLocale.equals(Locale.ENGLISH))
						errors.rejectValue("password", "bad value", "the given password is incorrect; try again if you wish");
					if (whichLocale.equals(Locale.FRENCH))
						errors.rejectValue("password", "mauvaise valeur", 
								"le mot de passe donné est incorrect; réessayez si vous le souhaitez");
				}
			}
			else {
				if (whichLocale.equals(Locale.ENGLISH))
					errors.rejectValue("username", "bad value", "the given username is unfound; try again if you wish");
				if (whichLocale.equals(Locale.FRENCH))
					errors.rejectValue("username", "mauvaise valeur", 
							"le nom d'utilisateur donné est introuvable; réessayez si vous le souhaitez");
			}
			return "login";
		}
		else return "login";
	}
	
	@PostMapping("/logout")
	public String logout(RedirectAttributes redirect, HttpServletRequest request) {
		Locale whichLocale = localeResolverforLoginController().resolveLocale(request);
		String username = this.sessionManager.getLoggedInUser().getUsername();
		if (whichLocale.equals(Locale.ENGLISH)) {
			redirect.addFlashAttribute("message", "Until next time, " + username +
					", which can be right now if you want to log-in again immediately" );
		}
		
		if (whichLocale.equals(Locale.FRENCH)) {
			redirect.addFlashAttribute("message", 
					"jusqu'à la prochaine fois, " + username + 
					", ce qui peut être maintenant si vous souhaitez vous reconnecter immédiatement");
		}
		
		this.sessionManager.logout();

		return "redirect:/app/login";
	}
	
	private boolean alreadyLoggedIn() {
		return sessionManager.isLoggedIn();
	}
	
	@Bean
	public LocaleResolver localeResolverforLoginController() {
		SessionLocaleResolver r = new SessionLocaleResolver();
		r.setLocaleAttributeName(LOCALE_ATTR);
		return r;
	}
}
	
