package acc.capstone;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
public class Post {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private LocalDate postDate;
	private LocalTime postTime;
	@JsonBackReference
	@ManyToOne
	private User author;
	@JsonBackReference
	@ManyToOne
	private Profile profile;
	@NotEmpty(message="we do not want any empty posts")
	@Size(min=10, max=120, message="between 10 and 120 characters, please")
	private String content;
	@NotEmpty(message="a post needs a name, yeah?")
	@Size(min=4, max=25, message="between 4 and 25 characters, please")
	private String name;
	@OneToOne(mappedBy="post")
	private Transcription transcription;
	@OneToMany(mappedBy="post")
	private List<PostComment> postComments;
	private boolean hasTranscription;
	private boolean hasComments;
	private boolean hasOneComment;
	private boolean editableByAuthor;
	private boolean deletableByAuthor;
	private boolean commentableByAuthor;
	@Enumerated(EnumType.STRING)
	private Language postLanguage; 
	/*
	 * @OneToMany(mappedBy="post") private List<Comment> postComments;
	 */

	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	public LocalDate getPostDate() {
		return postDate;
	}

	public void setPostDate(LocalDate postDate) {
		this.postDate = postDate;
	}
	
	public LocalTime getPostTime() {
		return postTime;
	}

	public void setPostTime(LocalTime postTime) {
		this.postTime = postTime;
	}

	public User getAuthor() {
		return author;
	}

	public void setAuthor(User author) {
		this.author = author;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Transcription getTranscription() {
		return transcription;
	}

	public void setTranscription(Transcription transcription) {
		this.transcription = transcription;
	}
	
	public List<PostComment> getPostComments() {
		return postComments;
	}

	public void setPostComments(List<PostComment> postComments) {
		this.postComments = postComments;
	}
	
	public boolean isHasTranscription() {
		return hasTranscription;
	}

	public void setHasTranscription(boolean hasTranscription) {
		this.hasTranscription = hasTranscription;
	}
	
	public boolean isHasComments() {
		return hasComments;
	}

	public void setHasComments(boolean hasComments) {
		this.hasComments = hasComments;
	}

	public Profile getProfile() {
		return profile;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	public boolean isEditableByAuthor() {
		return editableByAuthor;
	}

	public void setEditableByAuthor(boolean editableByAuthor) {
		this.editableByAuthor = editableByAuthor;
	}
	
	public boolean isHasOneComment() {
		return hasOneComment;
	}

	public void setHasOneComment(boolean hasOneComment) {
		this.hasOneComment = hasOneComment;
	}

	public boolean isDeletableByAuthor() {
		return deletableByAuthor;
	}

	public void setDeletableByAuthor(boolean deletableByAuthor) {
		this.deletableByAuthor = deletableByAuthor;
	}
	
	public boolean isCommentableByAuthor() {
		return commentableByAuthor;
	}

	public void setCommentableByAuthor(boolean commentableByAuthor) {
		this.commentableByAuthor = commentableByAuthor;
	}
	
	public Language getPostLanguage() {
		return postLanguage;
	}

	public void setPostLanguage(Language postLanguage) {
		this.postLanguage = postLanguage;
	}

	@Override
	public String toString() {
		return "Post ID is " + id;
	}
	
}
