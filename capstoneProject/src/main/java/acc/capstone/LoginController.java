package acc.capstone;

import java.util.Locale;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
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
	
	@GetMapping("/login")
	public String login(Model model, RedirectAttributes redirect, @RequestParam(required = false) String x, 
					SessionLocaleResolver session) {
		//session.setDefaultLocale(new Locale("fr"));
		//throw new ResponseStatusException(HttpStatus.BAD_GATEWAY);
		
		 if (alreadyLoggedIn()) {
		  	  redirect.addFlashAttribute("failure", "But you've logged in already!");
			  return "redirect:/app/timeline";
		  }
		  else {
			//System.out.println(x);
			model.addAttribute("loginAttempt", new LoginAttempt());
			return "login";
		}
		
	}
	
	@PostMapping("/login")
	public String login(@Valid LoginAttempt loginAttempt, Errors errors, Model model, RedirectAttributes redirect) {
		if (alreadyLoggedIn()) {
			redirect.addFlashAttribute("failure", "But you've logged in already!");
			return "redirect:/app/timeline";
		}
		if (!errors.hasErrors()) {
			Optional<User> optionalUser = userRepository.findByUsername(loginAttempt.getUsername());
			if (optionalUser.isPresent()) {
				String hashIncoming = Hash.hash(loginAttempt.getPassword(), optionalUser.get().getSalt());
				if (hashIncoming.equals(optionalUser.get().getPassword())) {
					this.sessionManager.login(optionalUser.get());
					  return "redirect:/app/timeline";
				}
				else {
					errors.rejectValue("password", "bad value", "the given password is incorrect; try again if you wish");
				}
			}
			else {
				errors.rejectValue("username", "bad value", "the given username is unfound; try again if you wish");
			}
			return "login";
		}
		else return "login";
	}
	
	@PostMapping("/logout")
	public String logout(RedirectAttributes redirect) {
		String username = sessionManager.getLoggedInUser().getUsername();
		redirect.addFlashAttribute("message", "Until next time," +
				username +
				", which can be right now if you want to log-in again immediately" );
		this.sessionManager.logout();
		//return "redirect:/app/login?x=" + username;
		return "redirect:/app/login";
	}
	
	private boolean alreadyLoggedIn() {
		return sessionManager.isLoggedIn();
	}
}
	
