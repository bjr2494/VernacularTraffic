package acc.capstone;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
public class UserController {

	@Autowired
	SessionManager sessionManager;

	@Autowired
	UserRepository userRepository;

	@GetMapping("/editUsername/{userId}")
	public String editUsername(@PathVariable int userId, Model model, RedirectAttributes redirect) {
		if (!alreadyLoggedIn()) {
			redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in user?");
			return "redirect:/app/login";
		}
		Optional<User> optionalUser = userRepository.findById(userId);
		if (optionalUser.isPresent()) {
			if (whichUser(optionalUser)) {
				redirect.addFlashAttribute("failure", "You cannot edit the username of another user");
				return "redirect:/app/timeline";
			}
			// model.addAttribute("user", optionalUser.get());
			model.addAttribute("loggedInUser", sessionManager.getLoggedInUser());
			return "editUsername";
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);

	}

	@PostMapping("/editUsername/{userId}")
	public String editUsername(@PathVariable int userId, @Valid User user, Errors errors, RedirectAttributes redirect) {
		if (!alreadyLoggedIn()) {
			redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in user?");
			return "redirect:/app/login";
		}
		Optional<User> optionalUser = this.userRepository.findById(userId);
		if (optionalUser.isPresent()) {
			if (whichUser(optionalUser)) {
				redirect.addFlashAttribute("failure", "You cannot edit the username of another user");
				return "redirect:/app/timeline";
			}
			if (errors.hasErrors())
				return "editProfile";
			else {
				user.setProfile(this.sessionManager.getLoggedInUser().getProfile());
				user.setId(this.sessionManager.getLoggedInUser().getId());
				user.setPassword(this.sessionManager.getLoggedInUser().getPassword());
				user.setJoinDate(this.sessionManager.getLoggedInUser().getJoinDate());
				user.setSalt(this.sessionManager.getLoggedInUser().getSalt());

				// this.userRepository.delete(optionalUser.get());
				this.userRepository.save(user);
				this.sessionManager.login(user);

				redirect.addFlashAttribute("success", "You've successfully changed your username to "
						+ sessionManager.getLoggedInUser().getUsername() + ". Enjoy thy new identity.");
				return "redirect:/app/timeline";
			}
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	@GetMapping("/register")
	public String userRegister(Model model, RedirectAttributes redirect) {
		if (alreadyLoggedIn()) {
			redirect.addFlashAttribute("failure", "But of course you've registered!");
			return "redirect:/app/timeline";
		}
		model.addAttribute("registrationAttempt", new RegistrationAttempt());
		return "registration";
	}

	@PostMapping("/register")
	public String userRegister(@Valid RegistrationAttempt registrationAttempt, Errors errors, Model model,
			RedirectAttributes redirect) {
		if (alreadyLoggedIn()) {
			redirect.addFlashAttribute("failure", "But of course you've registered!");
			return "redirect:/app/timeline";
		}
		
		for (User u : userRepository.findAll()) {
			if (registrationAttempt.getUsername().equals(u.getUsername())) {
				errors.rejectValue("username", "bad value", "The attempted username is already taken");
			}
		}
		
		if (registrationAttempt.getUsername().length() < 5)
			errors.rejectValue("username", "bad value", "Such is too short for a username here; retry");
		if (registrationAttempt.getUsername().length() > 15)
			errors.rejectValue("username", "bad value", "Such is too long for a username here; retry");
		if (registrationAttempt.getPasswordOne().length() < 5) {
			errors.rejectValue("passwordOne", "bad value", "Such is not secure enough for a password here; retry");
			if (registrationAttempt.getPasswordTwo().length() < 5)
				errors.rejectValue("passwordTwo", "bad value", "Such is not secure enough for a password here; retry");
		}
		if (registrationAttempt.getPasswordOne().length() > 15) {
			errors.rejectValue("passwordOne", "bad value", "Such is too secure for a password here; retry");
			if (registrationAttempt.getPasswordTwo().length() > 15)
				errors.rejectValue("passwordTwo", "bad value", "Such is too secure for a password here; retry");
		}
		
		if (!registrationAttempt.getPasswordOne().equals(registrationAttempt.getPasswordTwo())) {
			errors.rejectValue("passwordOne", "bad value", "The passwords do not match");
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

	private boolean whichUser(Optional<User> optionalUser) {
		return optionalUser.get().getId() != sessionManager.getLoggedInUser().getId();
	}

}
