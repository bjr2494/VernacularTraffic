package acc.capstone;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class ProfileController {

	@Autowired
	ProfileRepository profileRepository;

	@Autowired
	PostRepository postRepository;

	@Autowired
	TranscriptionRepository transcriptionRepository;

	@Autowired
	PostCommentRepository postCommentRepository;

	@Autowired
	TranscriptionCommentRepository transcriptionCommentRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	SessionManager sessionManager;

	@Autowired
	ApplicationManager applicationManager;

	@GetMapping("/profiles")
	public String profiles(Model model, RedirectAttributes redirect) {
		if (isLoggedIn()) {
			redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in-user?");
			return "redirect:/app/login";
		}

		List<Profile> profilesForProfilePage = new ArrayList<>();

		for (Profile profile : this.profileRepository.findAll()) {
			if (profile.getId() != this.sessionManager.getLoggedInUser().getProfile().getId()) {
				profilesForProfilePage.add(profile);
			}
		}

		this.applicationManager.setProfiles(profilesForProfilePage);
		return "profiles";
	}

	@GetMapping("/profile/{profileId}")
	public String profile(@PathVariable int profileId, Model model, RedirectAttributes redirect) {
		if (isLoggedIn()) {
			redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in user?");
			return "redirect:/app/login";
		}
		Optional<Profile> optionalProfile = profileRepository.findById(profileId);
		if (optionalProfile.isPresent()) {

			Profile profile = optionalProfile.get();

			if (optionalProfile.get().getId() == this.sessionManager.getLoggedInUser().getProfile().getId()) {
				profile = this.sessionManager.getLoggedInUser().getProfile();
				model.addAttribute("profile", profile);
				return "profile";
			} else {
				model.addAttribute("profile", profile);
				return "profile";
			}
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	@GetMapping("/editProfile/{profileId}")
	public String editProfile(@PathVariable int profileId, Model model, RedirectAttributes redirect) {
		if (isLoggedIn()) {
			redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in user?");
			return "redirect:/app/login";
		}
		Optional<Profile> optionalProfile = profileRepository.findById(profileId);
		if (optionalProfile.isPresent()) {
			if (whoseProfile(optionalProfile)) {
				redirect.addFlashAttribute("failure", "You cannot edit another user's profile");
				return "redirect:app/timeline";
			}
			model.addAttribute("profile", optionalProfile.get());
			// model.addAttribute("loggedInUser", sessionManager.getLoggedInUser());
			// model.addAttribute("user", optionalProfile.get().getUser());
			return "editProfile";
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	@PostMapping("/editProfile/{profileId}")
	public String editProfile(@PathVariable int profileId, @Valid Profile profile, Model model, Errors errors,
			RedirectAttributes redirect) {
		if (isLoggedIn()) {
			redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in user?");
			return "redirect:/app/login";
		}
		Optional<Profile> optionalProfile = profileRepository.findById(profileId);
		if (optionalProfile.isPresent()) {
			if (whoseProfile(optionalProfile)) {
				redirect.addFlashAttribute("failure", "You cannot edit another user's profile");
				return "redirect:app/timeline";
			}

			if (firstNameTooShort(profile)) {
				errors.rejectValue("firstName", "bad value",
						"Even if your first name is 0 or 1 characters, please give us something longer");
			}

			if (firstNameTooLong(profile)) {
				errors.rejectValue("firstName", "bad value", "Consider a legal name change to a shorter one");
			}

			if (lastNameTooShort(profile)) {
				errors.rejectValue("lastName", "bad value",
						"Even if your last name is 0 or 1 characters, please give us something longer");
			}

			if (lastNameTooLong(profile)) {
				errors.rejectValue("lastName", "bad value",
						"Nothing longer than the white-space-inclusive last name of the governor of Buenos Aires in Candide,");
			}

			if (isAgeNull(profile)) {
				profile.setAge(0);
				errors.rejectValue("age", "bad value", "Perhaps you're a newborn, but we need a higher number here");
			}

			if (isDecrepit(profile)) {
				errors.rejectValue("age", "bad value", "Perhaps thou are too old for this app");
			}

			if (isLanguagesNull(profile)) {
				errors.rejectValue("language", "bad value", "You cannot use this app without any language");
			}

			Pattern potentialEmailAddress = Pattern.compile("^[A-Z0-9]+@[A-Z]+\\.[A-Z]{2,6}$",
					Pattern.CASE_INSENSITIVE);
			Matcher emailAddressMatcher = potentialEmailAddress.matcher(profile.getEmailAddress());
			boolean potentialOrNot = emailAddressMatcher.matches();

			if (isEmailInvalid(potentialOrNot)) {
				errors.rejectValue("emailAddress", "bad value",
						"the input for the email address must at least glancingly resemble an email address");
			}

			if (errors.hasErrors()) {
				return "profile";
			}

			else {
				profile.setUser(optionalProfile.get().getUser());
				profile.setId(optionalProfile.get().getId());

				if (optionalProfile.get().isHasMadeOnePost() == true)
					profile.setHasMadeOnePost(true);
				else {
					profile.setHasMadeOnePost(false);
				}

				if (optionalProfile.get().isHasMadePosts() == true)
					profile.setHasMadePosts(true);
				else {
					profile.setHasMadePosts(false);
				}

				if (optionalProfile.get().isHasMadeOnePostComment() == true)
					profile.setHasMadeOnePostComment(true);
				else {
					profile.setHasMadeOnePostComment(false);
				}

				if (optionalProfile.get().isHasMadePostComments() == true)
					profile.setHasMadePostComments(true);
				else {
					profile.setHasMadePostComments(false);
				}

				if (optionalProfile.get().isHasMadeOneTranscription() == true)
					profile.setHasMadeOneTranscription(true);
				else {
					profile.setHasMadeOneTranscription(false);
				}

				if (optionalProfile.get().isHasMadeTranscriptions() == true)
					profile.setHasMadeTranscriptions(true);
				else {
					profile.setHasMadeTranscriptions(false);
				}

				if (optionalProfile.get().isHasMadeOneTranscriptionComment() == true)
					profile.setHasMadeOneTranscriptionComment(true);
				else {
					profile.setHasMadeOneTranscriptionComment(false);
				}

				if (optionalProfile.get().isHasMadeTranscriptionComments() == true)
					profile.setHasMadeTranscriptionComments(true);
				else {
					profile.setHasMadeTranscriptionComments(false);
				}

				for (Post p : optionalProfile.get().getPosts()) {
					p.setProfile(profile);
					p.getProfile().setId(optionalProfile.get().getId());
				}

				for (PostComment pc : optionalProfile.get().getPostComments()) {
					pc.setProfile(profile);
					pc.getProfile().setId(optionalProfile.get().getId());
				}

				for (Transcription t : optionalProfile.get().getTranscriptions()) {
					t.setProfile(profile);
					t.getProfile().setId(optionalProfile.get().getId());
				}

				for (TranscriptionComment tc : optionalProfile.get().getTranscriptionComments()) {
					tc.setProfile(profile);
					tc.getProfile().setId(optionalProfile.get().getId());
				}

				if (profile.getLanguages().size() == 1) {
					profile.setOneLanguage(true);
					profile.setPreferredLanguage(profile.getLanguages().get(0));
					this.profileRepository.save(profile);
					redirect.addFlashAttribute("success", "You've successfully made your profile, "
							+ this.sessionManager.getLoggedInUser().getUsername());
					return "redirect:/app/timeline";
				}
				// if profile's language count is greater than one
				else {
					profile.setOneLanguage(false);
					// arbitrarily and temporarily setting the profile's preferred language to the
					// first choice
					this.profileRepository.save(profile);
					return "redirect:/app/preferredLanguageProfileCreation/" + profile.getId();
				}
			}
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	@GetMapping("/createProfile/{userId}")
	public String createProfile(@PathVariable int userId, Model model, RedirectAttributes redirect) {
		if (isLoggedIn()) {
			redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in user?");
			return "redirect:/app/login";
		}
		Optional<User> optionalUser = this.userRepository.findById(userId);
		if (optionalUser.isPresent()) {
			if (optionalUser.get().getId() != this.sessionManager.getLoggedInUser().getId()) {
				redirect.addAttribute("failure", "You cannot create a profile for another user");
				return "redirect:/app/timeline";
			}

			Profile profile = new Profile();
			model.addAttribute("profile", profile);
			return "createProfile";
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	@PostMapping("/createProfile/{userId}")
	public String createProfile(@PathVariable int userId, @Valid Profile profile, Errors errors, Model model,
			RedirectAttributes redirect) {
		if (isLoggedIn()) {
			redirect.addFlashAttribute("message", "Why should you be allowed to do such as a non-logged-in user?");
			return "redirect:/app/login";
		}
		Optional<User> optionalUser = this.userRepository.findById(userId);
		if (optionalUser.isPresent()) {
			if (optionalUser.get().getId() != this.sessionManager.getLoggedInUser().getId()) {
				redirect.addAttribute("failure", "You cannot create a profile for another user");
				return "redirect:/app/timeline";
			}

			if (firstNameTooShort(profile)) {
				errors.rejectValue("firstName", "bad value",
						"Even if your first name is 0 or 1 character, please give us something longer");
			}

			if (firstNameTooLong(profile)) {
				errors.rejectValue("firstName", "bad value", "Consider a legal name change to a shorter one");
			}

			if (lastNameTooShort(profile)) {
				errors.rejectValue("lastName", "bad value",
						"Even if your last name is 0 or 1 character, please give us something longer");
			}

			if (lastNameTooLong(profile)) {
				errors.rejectValue("lastName", "bad value",
						"Nothing longer than the white-space-inclusive last name of the governor of Buenos Aires in Candide,");
			}

			if (isAgeNull(profile)) {
				profile.setAge(0);
				errors.rejectValue("age", "bad value", "Perhaps you're a newborn, but we need a higher number here");
			}

			if (isDecrepit(profile)) {
				errors.rejectValue("age", "bad value", "Perhaps thou are too old for this app");
			}

			if (isLanguagesNull(profile)) {
				errors.rejectValue("language", "bad value", "You cannot use this app without any language");
			}

			Pattern potentialEmailAddress = Pattern.compile("^[A-Z0-9]+@[A-Z]+\\.[A-Z]{2,6}$",
					Pattern.CASE_INSENSITIVE);
			Matcher emailAddressMatcher = potentialEmailAddress.matcher(profile.getEmailAddress());
			boolean potentialOrNot = emailAddressMatcher.matches();

			if (isEmailInvalid(potentialOrNot)) {
				errors.rejectValue("emailAddress", "bad value",
						"the input for the email address must at least glancingly resemble an email address");
			}

			if (errors.hasErrors()) {
				model.addAttribute("profile", profile);
				return "createProfile";
			} else {

				optionalUser.get().setProfile(profile);
				// this.sessionManager.login(optionalUser.get());
				// this.userRepository.persist(optionalUser.get());

				profile.setUser(optionalUser.get());
				profile.setHasMadePosts(false);
				profile.setHasMadeOnePost(false);
				profile.setHasMadeTranscriptions(false);
				profile.setHasMadeOneTranscription(false);
				profile.setHasMadePostComments(false);
				profile.setHasMadeOnePostComment(false);
				profile.setHasMadeTranscriptionComments(false);
				profile.setHasMadeOneTranscriptionComment(false);
				if (profile.getLanguages().size() == 1) {
					profile.setOneLanguage(true);
					profile.setPreferredLanguage(profile.getLanguages().get(0));
					this.profileRepository.save(profile);
					System.out.println(profile);
					redirect.addFlashAttribute("success", "You've successfully made your profile, "
							+ this.sessionManager.getLoggedInUser().getUsername());
					return "redirect:/app/timeline";

				}
				// if profile's language count is greater than one, which here means two
				else {
					profile.setOneLanguage(false);
					// arbitrarily and temporarily setting the profile's preferred language to the
					// first choice, which is English
					this.profileRepository.save(profile);
					return "redirect:/app/preferredLanguageProfileCreation/" + profile.getId();
				}

			}
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	private boolean isEmailInvalid(boolean potentialOrNot) {
		return potentialOrNot == false;
	}

	private boolean isLanguagesNull(Profile profile) {
		return profile.getLanguages() == null;
	}

	private boolean isDecrepit(Profile profile) {
		return profile.getAge() > 100;
	}

	private boolean isAgeNull(Profile profile) {
		return profile.getAge() == null;
	}

	private boolean lastNameTooLong(Profile profile) {
		return profile.getLastName().length() > 66;
	}

	private boolean lastNameTooShort(Profile profile) {
		return profile.getLastName().length() < 2;
	}

	private boolean firstNameTooLong(Profile profile) {
		return profile.getFirstName().length() > 35;
	}

	private boolean firstNameTooShort(Profile profile) {
		return profile.getFirstName().length() < 2;
	}

	private boolean isLoggedIn() {
		return !sessionManager.isLoggedIn();
	}

	private boolean whoseProfile(Optional<Profile> optionalProfile) {
		return optionalProfile.get().getId() != sessionManager.getLoggedInUser().getId();
	}
}
