package acc.capstone;

import java.util.List;

import javax.persistence.ElementCollection;

//import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonBackReference;

//import javax.persistence.ManyToMany;

@Entity
public class Profile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;	
	@NotEmpty(message="this mustn't be empty")
	private String firstName;
	@NotEmpty(message="this mustn't be empty")
	private String lastName;
	//@NotNull(message="even if you're a newborn, we need something here")
	private Integer age;
	@NotNull(message="tell me how you expect to work this application with no language")
	@ElementCollection
	@Enumerated(EnumType.STRING)
	@Size(min=1, max=2, message="either one language or both")
	private List<Language> languages;
	//@NotEmpty(message="email address required")
	//@Email(message="your input must at least resemble an email address")
	private String emailAddress;
	@OneToOne(mappedBy="profile")
	private User user;
	@OneToMany(mappedBy="profile")
	private List<Post> posts;
	private boolean hasMadePosts;
	@OneToMany(fetch=FetchType.EAGER, mappedBy="profile")
	private List<PostComment> postComments;
	private boolean hasMadePostComments;
	@OneToMany(mappedBy="profile")
	private List<Transcription> transcriptions;
	private boolean hasMadeTranscriptions;
	@OneToMany(mappedBy="profile")
	private List<TranscriptionComment> transcriptionComments;
	private boolean hasMadeTranscriptionComments;
	private boolean hasMadeOnePost;
	private boolean hasMadeOnePostComment;
	private boolean hasMadeOneTranscription;
	private boolean hasMadeOneTranscriptionComment;
	private boolean oneLanguage;
	@Enumerated(EnumType.STRING)
	private Language preferredLanguage;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public List<Language> getLanguages() {
		  return languages; }
	  
	 public void setLanguages(List<Language> languages) { 
		 this.languages = languages;
	 }
	 
	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}
	
	public List<Post> getPosts() {
		return posts;
	}

	public void setPosts(List<Post> posts) {
		this.posts = posts;
	}
	
	public boolean isHasMadeOnePostComment() {
		return hasMadeOnePostComment;
	}

	public void setHasMadeOnePostComment(boolean hasMadeOnePostComment) {
		this.hasMadeOnePostComment = hasMadeOnePostComment;
	}

	public List<TranscriptionComment> getTranscriptionComments() {
		return transcriptionComments;
	}

	public void setTranscriptionComments(List<TranscriptionComment> transcriptionComments) {
		this.transcriptionComments = transcriptionComments;
	}

	public boolean isHasMadeTranscriptionComments() {
		return hasMadeTranscriptionComments;
	}

	public void setHasMadeTranscriptionComments(boolean hasMadeTranscriptionComments) {
		this.hasMadeTranscriptionComments = hasMadeTranscriptionComments;
	}

	public boolean isHasMadePosts() {
		return hasMadePosts;
	}

	public void setHasMadePosts(boolean hasMadePosts) {
		this.hasMadePosts = hasMadePosts;
	}

	public List<PostComment> getPostComments() {
		return postComments;
	}
	
	public boolean isHasMadePostComments() {
		return hasMadePostComments;
	}
	
	public void setHasMadePostComments(boolean hasMadePostComments) {
		this.hasMadePostComments = hasMadePostComments;
	}

	public List<Transcription> getTranscriptions() {
		return transcriptions;
	}

	public void setTranscriptions(List<Transcription> transcriptions) {
		this.transcriptions = transcriptions;
	}

	public boolean isHasMadeTranscriptions() {
		return hasMadeTranscriptions;
	}

	public void setHasMadeTranscriptions(boolean hasMadeTranscriptions) {
		this.hasMadeTranscriptions = hasMadeTranscriptions;
	}

	public void setPostComments(List<PostComment> postComments) {
		this.postComments = postComments;
	}
	
	public boolean isHasMadeOnePost() {
		return hasMadeOnePost;
	}

	public void setHasMadeOnePost(boolean hasMadeOnePost) {
		this.hasMadeOnePost = hasMadeOnePost;
	}

	public boolean isHasMadeOneTranscription() {
		return hasMadeOneTranscription;
	}

	public void setHasMadeOneTranscription(boolean hasMadeOneTranscription) {
		this.hasMadeOneTranscription = hasMadeOneTranscription;
	}

	public boolean isHasMadeOneTranscriptionComment() {
		return hasMadeOneTranscriptionComment;
	}

	public void setHasMadeOneTranscriptionComment(boolean hasMadeOneTranscriptionComment) {
		this.hasMadeOneTranscriptionComment = hasMadeOneTranscriptionComment;
	}
	
	public boolean isOneLanguage() {
		return oneLanguage;
	}

	public void setOneLanguage(boolean oneLanguage) {
		this.oneLanguage = oneLanguage;
	}

	public Language getPreferredLanguage() {
		return preferredLanguage;
	}

	public void setPreferredLanguage(Language preferredLanguage) {
		this.preferredLanguage = preferredLanguage;
	}

	@Override
	public String toString() {
		return "Profile [languages=" + languages + "]";
	}
	
}
