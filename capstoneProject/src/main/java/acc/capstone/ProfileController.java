package acc.capstone;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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
import org.springframework.web.util.WebUtils;

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

	private static final String LOCALE_ATTR = "language";

	@GetMapping("/profiles")
	public String profiles(Model model, RedirectAttributes redirect, HttpServletRequest request) {

		Locale whichLocale = localeResolverforProfileController().resolveLocale(request);
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
		List<User> usersForProfilesPage = new ArrayList<>();

		for (User user : this.userRepository.findAll()) {
			if (user.getId() != this.sessionManager.getLoggedInUser().getId()) {
				usersForProfilesPage.add(user);
			}
		}

		this.sessionManager.setUsers(usersForProfilesPage);
		return "profiles";
	}

	@GetMapping("/profile/{userId}")
	public String profile(@PathVariable int userId, Model model, RedirectAttributes redirect,
			HttpServletRequest request) {

		Locale whichLocale = localeResolverforProfileController().resolveLocale(request);
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

			Profile profile = optionalUser.get().getProfile();
			model.addAttribute("profile", profile);

			List<Post> profilePosts = fetchPostsForGivenProfile(profile);
			model.addAttribute("profilePosts", profilePosts);

			List<PostComment> profilePostComments = fetchPostCommentsForGivenProfile(profile);
			model.addAttribute("profilePostComments", profilePostComments);

			List<Transcription> profileTranscriptions = fetchTranscriptionsForGivenProfile(profile);
			model.addAttribute("profileTranscriptions", profileTranscriptions);

			List<TranscriptionComment> profileTranscriptionComments = fetchTranscriptionCommentsForGivenProfile(
					profile);
			model.addAttribute("profileTranscriptionComments", profileTranscriptionComments);

			if (profile.getId() == this.sessionManager.getLoggedInUser().getId()) {

				for (Post p : profilePosts) {
					if (p.isDeletableByAuthor())
						model.addAttribute("deletePost", "delete post");

					if (p.isEditableByAuthor())
						model.addAttribute("editPost", "edit post");
				}

				for (PostComment pc : profilePostComments) {
					if (pc.isDeletableByAuthor())
						model.addAttribute("deletePostComment", "delete post comment");
				}

				for (Transcription t : profileTranscriptions) {
					if (t.isDeletableByAuthor())
						model.addAttribute("deleteTranscription", "delete Transcription");

					if (t.isEditableByAuthor())
						model.addAttribute("editTranscription", "edit transcription");
				}

				for (TranscriptionComment tc : profileTranscriptionComments) {
					if (tc.isDeletableByAuthor())
						model.addAttribute("deleteTranscriptionComment", "delete transcription comment");
				}

				model.addAttribute("logOut", "log out now");
				model.addAttribute("editProfile", "edit profile");
				model.addAttribute("makePost", "make post");

				if (profile.getLanguages().size() > 1) {
					model.addAttribute("changeLanguage", "");
				}
			}
			return "profile";
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	@GetMapping("/editProfile/{profileId}")
	public String editProfile(@PathVariable int profileId, Model model, RedirectAttributes redirect,
			HttpServletRequest request) {

		Locale whichLocale = localeResolverforProfileController().resolveLocale(request);
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

		Optional<Profile> optionalProfile = profileRepository.findById(profileId);
		if (optionalProfile.isPresent()) {
			if (whoseProfile(optionalProfile)) {
				if (whichLocale.equals(Locale.ENGLISH)) {
					redirect.addFlashAttribute("failure", "You cannot edit another user's profile");
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					redirect.addFlashAttribute("failure",
							"Vous ne pouvez pas modifier le profil d'un autre utilisateur");
				}
				return "redirect:app/timeline";
			}

			model.addAttribute("profile", optionalProfile.get());
			return "editProfile";
		} else
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

	@Transactional
	@PostMapping("/editProfile/{profileId}")
	public String editProfile(@PathVariable int profileId, @Valid Profile profile, Model model, Errors errors, 
			RedirectAttributes redirect, HttpServletRequest request) {

		Locale whichLocale = localeResolverforProfileController().resolveLocale(request);
		Optional<Profile> optionalProfile = profileRepository.findById(profileId);
		if (optionalProfile.isPresent()) {

			profile.setId(profileId);

			if (firstNameTooShort(profile)) {

				if (whichLocale.equals(Locale.ENGLISH)) {
					errors.rejectValue("firstName", "bad value",
							"Even if your first name is 0 or 1 characters, please give us something longer");
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					errors.rejectValue("firstName", "mauvaise valeur",
							"Même si votre prénom est 0 ou 1 caractères, donnez-nous quelque chose de plus");
				}
			}

			this.cruxOfProfile(profile, errors, whichLocale);

			if (errors.hasErrors()) {
				// profile.setUser(this.sessionManager.getLoggedInUser());
				model.addAttribute("profile", profile);
				return "editProfile";
			}

			else {
				profile.setUser(this.sessionManager.getLoggedInUser());
				profile.setId(optionalProfile.get().getId());

				if (profile.getLanguages().size() == 1) {

					if (twoProfileLangsToOneAndLanguageCheck(optionalProfile)) {
						List<Post> profilePosts = this.fetchPostsForGivenProfile(optionalProfile.get());

						int postCounter = 0;
						for (Post post : profilePosts) {
							if (!post.getPostLanguage().equals(profile.getLanguages().get(0)))
								postCounter++;
						}

						if (postCounter == 1) {
							if (whichLocale.equals(Locale.ENGLISH)) {
								redirect.addFlashAttribute("failure",
										"This edit will exclude a post that you've already made. "
										+ "Please change your mind or delete the post or posts whose language "
										+ "you no longer care for");
							}
							
							if (whichLocale.equals(Locale.FRENCH)) {
								redirect.addFlashAttribute("failure", 
										"Cette modification exclut un post que vous avez déjà publié. "
										+ "Veuillez changer d’avis ou supprimer le post dont vous ne vous souciez"
										+ " plus la langue.");
							}
							return "redirect:/app/editProfile/" + profile.getId();
						}
						
						if (postCounter > 1) {
							if (whichLocale.equals(Locale.ENGLISH)) {
								redirect.addFlashAttribute("failure",
										"This edit will exclude some posts that you've already made. "
										+ "Please change your mind or delete the posts whose language "
										+ "you no longer care for");
							}
							
							if (whichLocale.equals(Locale.FRENCH)) {
								redirect.addFlashAttribute("failure", 
										"Cette modification exclut des posts que vous avez déjà publié. "
										+ "Veuillez changer d’avis ou supprimer les posts dont vous ne vous souciez"
										+ " plus la langue.");
							}
							return "redirect:/app/editProfile/" + profile.getId();
						}
					}
					
					if (twoProfileLangsToOneAndLanguageCheck(optionalProfile)) {
						
						List<Transcription> profileTranscriptions = 
								this.fetchTranscriptionsForGivenProfile(optionalProfile.get());
						int transcriptionCounter = 0;
						for (Transcription transcription : profileTranscriptions) {
							if (!transcription.getTranscriptionLanguage().equals(profile.getLanguages().get(0)))
								transcriptionCounter++;
						}
						
						if (transcriptionCounter == 1) {
							if (whichLocale.equals(Locale.ENGLISH)) {
								redirect.addFlashAttribute("failure",
										"This edit will exclude a transcription that you've already made. "
										+ "Please change your mind or delete the transcription or transcriptions whose language"
										+ " you no longer care for");
							}
							
							if (whichLocale.equals(Locale.FRENCH)) {
								redirect.addFlashAttribute("failure", 
										"Cette modification exclut une transcription que vous avez déjà publié. "
										+ "Veuillez changer d’avis ou supprimer la transcription"
										+ "dont vous ne vous souciez plus la langue.");
							}
							
							return "redirect:/app/editProfile/" + profile.getId();
						}
						
						if (transcriptionCounter > 1) {
							if (whichLocale.equals(Locale.ENGLISH)) {
								redirect.addFlashAttribute("failure",
										"This edit will exclude some transcriptions that you've already made. "
										+ "Please change your mind or delete the transcriptions whose language"
										+ " you no longer care for");
							}
							
							if (whichLocale.equals(Locale.FRENCH)) {
								redirect.addFlashAttribute("failure", 
										"Cette modification exclut des transcriptions que vous avez déjà publié. "
										+ "Veuillez changer d’avis ou supprimer les transcriptions dont vous ne vous"
										+ " souciez plus la langue.");
							}
							
							return "redirect:/app/editProfile/" + profile.getId();
						}
					}
					
					if (twoProfileLangsToOneAndLanguageCheck(optionalProfile)) {
						
						List<PostComment> profilePostComments = this.fetchPostCommentsForGivenProfile(optionalProfile.get());
						int postCommentCounter = 0;
						for (PostComment postComment : profilePostComments) {
							if (!postComment.getPostCommentLanguage().equals(profile.getLanguages().get(0))) 
								postCommentCounter++;
						}
						
						if (postCommentCounter == 1) {
							if (whichLocale.equals(Locale.ENGLISH)) {
								redirect.addFlashAttribute("failure",
										"This edit will exclude a post comment that you've already made. "
										+ "Please change your mind or delete the post comment whose language"
										+ " you no longer care for");
							}
							
							if (whichLocale.equals(Locale.FRENCH)) {
								redirect.addFlashAttribute("failure", 
										"Cette modification exclut un commentaire de post que vous avez déjà publié. "
										+ "Veuillez changer d’avis ou supprimer le commentaire"
										+ "dont vous ne vous souciez plus la langue.");
							}
							
							return "redirect:/app/editProfile/" + profile.getId();
						}
						
						if (postCommentCounter > 1) {
							if (whichLocale.equals(Locale.ENGLISH)) {
								redirect.addFlashAttribute("failure",
										"This edit will exclude some post comments that you've already made. "
										+ "Please change your mind or delete the post comments whose language"
										+ " you no longer care for");
							}
							
							if (whichLocale.equals(Locale.FRENCH)) {
								redirect.addFlashAttribute("failure", 
										"Cette modification exclut des commentaire de post que vous avez déjà publié. "
										+ "Veuillez changer d’avis ou supprimer les commentaires dont vous ne vous"
										+ " souciez plus la langue.");
							}
							
							return "redirect:/app/editProfile/" + profile.getId();
						}
						
					}
						
					if (twoProfileLangsToOneAndLanguageCheck(optionalProfile)) {
						
						List<TranscriptionComment> profileTranscriptionComments = 
								this.fetchTranscriptionCommentsForGivenProfile(optionalProfile.get());
						int transcriptionCommentCounter = 0;
						
						for (TranscriptionComment transcriptionComment : profileTranscriptionComments) {
							if (!transcriptionComment.getTranscriptionCommentLanguage().equals(profile.getLanguages().get(0))) 
								transcriptionCommentCounter++;
						}
						
						if (transcriptionCommentCounter == 1) {
							if (whichLocale.equals(Locale.ENGLISH)) {
								redirect.addFlashAttribute("failure",
										"This edit will exclude a transcription comment that you've already made. "
										+ "Please change your mind or delete the transcription comment whose language"
										+ " you no longer care for");
							}
							
							if (whichLocale.equals(Locale.FRENCH)) {
								redirect.addFlashAttribute("failure", 
										"Cette modification exclut un commentaire de transcription que vous avez déjà publié. "
										+ "Veuillez changer d’avis ou supprimer le commentaire"
										+ "dont vous ne vous souciez plus la langue.");
							}
							
							return "redirect:/app/editProfile/" + profile.getId();
						}
						
						if (transcriptionCommentCounter > 1) {
							if (whichLocale.equals(Locale.ENGLISH)) {
								redirect.addFlashAttribute("failure",
										"This edit will exclude some transcription comments that you've already made. "
										+ "Please change your mind or delete the post comments whose language"
										+ " you no longer care for");
							}
							
							if (whichLocale.equals(Locale.FRENCH)) {
								redirect.addFlashAttribute("failure", 
										"Cette modification exclut des commentaire de transcription que vous avez déjà publié. "
										+ "Veuillez changer d’avis ou supprimer les commentaires dont vous ne vous"
										+ " souciez plus la langue.");
							}
							
							return "redirect:/app/editProfile/" + profile.getId();
						}
					}
						
					this.extraProfileInfo(profile, optionalProfile);
					profile.setOneLanguage(true);
					profile.setPreferredLanguage(profile.getLanguages().get(0));
					if (profile.getLanguages().get(0).equals(Language.ENGLISH))
						WebUtils.setSessionAttribute(request, LOCALE_ATTR, Locale.ENGLISH);
					if (profile.getLanguages().get(0).equals(Language.FRENCH))
						WebUtils.setSessionAttribute(request, LOCALE_ATTR, Locale.FRENCH);
					this.profileRepository.save(profile);

					if (whichLocale.equals(Locale.ENGLISH)) {
						redirect.addFlashAttribute("success", "You've successfully edited your profile, "
								+ this.sessionManager.getLoggedInUser().getUsername());
					}

					if (whichLocale.equals(Locale.FRENCH)) {
						redirect.addFlashAttribute("success", "Vous avez édité avec succès votre profil, "
								+ this.sessionManager.getLoggedInUser().getUsername());
					}

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
	public String createProfile(@PathVariable int userId, Model model, RedirectAttributes redirect,
			HttpServletRequest request) {

		Locale whichLocale = localeResolverforProfileController().resolveLocale(request);
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

		Optional<User> optionalUser = this.userRepository.findById(userId);
		if (optionalUser.isPresent()) {
			if (whichUser(optionalUser)) {
				if (whichLocale.equals(Locale.ENGLISH)) {
					redirect.addAttribute("failure", "You cannot create a profile for another user");
				}

				if (whichLocale.equals(Locale.FRENCH)) {
					redirect.addAttribute("failure", "Vous ne pouvez pas créer de profil pour un autre utilisateur");
				}

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
			RedirectAttributes redirect, HttpServletRequest request) {

		Locale whichLocale = localeResolverforProfileController().resolveLocale(request);
		Optional<User> optionalUser = this.userRepository.findById(userId);
		if (optionalUser.isPresent()) {

			this.cruxOfProfile(profile, errors, whichLocale);

			if (errors.hasErrors()) {
				model.addAttribute("profile", profile);
				return "createProfile";
			} else {

				optionalUser.get().setProfile(profile);
				// this.sessionManager.login(optionalUser.get());
				// this.userRepository.persist(optionalUser.get());

				// profile.setUser(optionalUser.get());
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

					if (whichLocale.equals(Locale.ENGLISH)) {
						redirect.addFlashAttribute("success", "You've successfully made your profile, "
								+ this.sessionManager.getLoggedInUser().getUsername());
					}

					if (whichLocale.equals(Locale.FRENCH)) {
						redirect.addFlashAttribute("success", "Vous avez créé votre profil avec succès, "
								+ this.sessionManager.getLoggedInUser().getUsername());
					}

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

	private List<Post> fetchPostsForGivenProfile(Profile profile) {
		List<Post> profilePosts = new ArrayList<>();
		for (Post post : this.postRepository.findAll()) {
			if (post.getAuthor().getProfile().getId() == profile.getId())
				profilePosts.add(post);
		}
		return profilePosts;
	}

	private List<PostComment> fetchPostCommentsForGivenProfile(Profile profile) {
		List<PostComment> profilePostComments = new ArrayList<>();
		for (PostComment postComment : this.postCommentRepository.findAll()) {
			if (postComment.getAuthor().getProfile().getId() == profile.getId())
				profilePostComments.add(postComment);
		}
		return profilePostComments;
	}

	private List<Transcription> fetchTranscriptionsForGivenProfile(Profile profile) {
		List<Transcription> profileTranscriptions = new ArrayList<>();
		for (Transcription transcription : this.transcriptionRepository.findAll()) {
			if (transcription.getAuthor().getProfile().getId() == profile.getId()) {
				profileTranscriptions.add(transcription);
			}
		}
		return profileTranscriptions;
	}

	private List<TranscriptionComment> fetchTranscriptionCommentsForGivenProfile(Profile profile) {
		List<TranscriptionComment> profileTranscriptionComments = new ArrayList<>();
		for (TranscriptionComment transcriptionComment : this.transcriptionCommentRepository.findAll()) {
			if (transcriptionComment.getAuthor().getProfile().getId() == profile.getId()) {
				profileTranscriptionComments.add(transcriptionComment);
			}
		}
		return profileTranscriptionComments;
	}

	private boolean whichUser(Optional<User> optionalUser) {
		return optionalUser.get().getId() != this.sessionManager.getLoggedInUser().getId();
	}

	private void cruxOfProfile(Profile profile, Errors errors, Locale whichLocale) {
		if (firstNameTooLong(profile)) {

			if (whichLocale.equals(Locale.ENGLISH)) {
				errors.rejectValue("firstName", "bad value", "Consider a legal name change to a shorter one");
			}

			if (whichLocale.equals(Locale.FRENCH)) {
				errors.rejectValue("firstName", "mauvaise valeur",
						"Envisager un changement de nom légal en un nom plus court");
			}
		}

		if (lastNameTooShort(profile)) {

			if (whichLocale.equals(Locale.ENGLISH)) {
				errors.rejectValue("lastName", "bad value",
						"Even if your last name is 0 or 1 characters, please give us something longer");
			}

			if (whichLocale.equals(Locale.FRENCH)) {
				errors.rejectValue("firstName", "mauvaise valeur",
						"Même si votre nom de famille est 0 ou 1 caractères, donnez-nous quelque chose de plus");
			}

		}

		if (lastNameTooLong(profile)) {

			if (whichLocale.equals(Locale.ENGLISH)) {
				errors.rejectValue("lastName", "bad value",
						"Nothing longer than the white-space-inclusive last name of the governor of Buenos Aires"
								+ " in Candide");
			}

			if (whichLocale.equals(Locale.FRENCH)) {
				errors.rejectValue("lastName", "mauvaise valeur",
						"Rien de plus long que le nom de famille du gouverneur de Buenos Aires,"
								+ " inclus dans l'espace blanc, à Candide");
			}
			;
		}

		if (isAgeNull(profile)) {
			profile.setAge(0);
			if (whichLocale.equals(Locale.ENGLISH)) {
				errors.rejectValue("age", "bad value", "Perhaps you're a newborn, but we need a higher number here");
			}

			if (whichLocale.equals(Locale.FRENCH)) {
				errors.rejectValue("age", "mauvaise valeur",
						"Peut-être que vous êtes un nouveau-né, mais nous avons besoin d'un nombre plus élevé ici");
			}
		}

		if (isDecrepit(profile)) {

			if (whichLocale.equals(Locale.ENGLISH)) {
				errors.rejectValue("age", "bad value", "Perhaps thou are too old for this app");
			}

			if (whichLocale.equals(Locale.FRENCH)) {
				errors.rejectValue("age", "mauvaise valeur",
						"Peut-être que vous êtes trop vieux pour cette application");
			}

		}

		if (isLanguagesNull(profile)) {

			if (whichLocale.equals(Locale.ENGLISH)) {
				errors.rejectValue("languages", "bad value", "You cannot use this app without any language");
			}

			if (whichLocale.equals(Locale.FRENCH)) {
				errors.rejectValue("languages", "mauvaise valeur",
						"Vous ne pouvez pas utiliser cette application" + "sans langue");
			}

		}

		Pattern potentialEmailAddress = Pattern.compile("^[A-Z0-9]+@[A-Z]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
		Matcher emailAddressMatcher = potentialEmailAddress.matcher(profile.getEmailAddress());
		boolean potentialOrNot = emailAddressMatcher.matches();

		if (isEmailInvalid(potentialOrNot)) {

			if (whichLocale.equals(Locale.ENGLISH)) {
				errors.rejectValue("emailAddress", "bad value",
						"the input for the email address must at least glancingly resemble an email address");
			}

			if (whichLocale.equals(Locale.FRENCH)) {
				errors.rejectValue("emailAddress", "mauvaise valeur",
						"l'entrée pour l'adresse e-mail doit ressembler au moins à une adresse e-mail");
			}
		}
	}

	private void extraProfileInfo(Profile profile, Optional<Profile> optionalProfile) {
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

		List<Post> profilePosts = fetchPostsForGivenProfile(optionalProfile.get());
		for (Post p : profilePosts) {
			p.setProfile(profile);
			p.getProfile().setId(optionalProfile.get().getId());
		}

		List<PostComment> profilePostComments = fetchPostCommentsForGivenProfile(optionalProfile.get());
		for (PostComment pc : profilePostComments) {
			pc.setProfile(profile);
			pc.getProfile().setId(optionalProfile.get().getId());
		}

		List<Transcription> profileTranscriptions = fetchTranscriptionsForGivenProfile(optionalProfile.get());
		for (Transcription t : profileTranscriptions) {
			t.setProfile(profile);
			t.getProfile().setId(optionalProfile.get().getId());
		}

		List<TranscriptionComment> profileTranscriptionComments = fetchTranscriptionCommentsForGivenProfile(
				optionalProfile.get());
		for (TranscriptionComment tc : profileTranscriptionComments) {
			tc.setProfile(profile);
			tc.getProfile().setId(optionalProfile.get().getId());
		}
	}

	private boolean twoProfileLangsToOneAndLanguageCheck(Optional<Profile> optionalProfile) {
		return optionalProfile.get().getLanguages().size() == 2;
	}

	@Bean
	public LocaleResolver localeResolverforProfileController() {
		SessionLocaleResolver r = new SessionLocaleResolver();
		r.setLocaleAttributeName(LOCALE_ATTR);
		return r;
	}
}
