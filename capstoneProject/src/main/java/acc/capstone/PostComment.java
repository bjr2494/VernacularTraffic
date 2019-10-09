package acc.capstone;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
public class PostComment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@ManyToOne
	@JsonBackReference
	private Post post;
	private LocalDate commentDate;
	private LocalTime commentTime;
	@ManyToOne
	private User author;
	@ManyToOne
	@JsonBackReference
	private Profile profile;
	@NotEmpty(message = "we disallow empty comments")
	@Size(min = 4, max = 60, message = "between 4 and 60 characters, please")
	private String content;
	private boolean deletableByAuthor;
	@Enumerated(EnumType.STRING)
	private Language postCommentLanguage;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Post getPost() {
		return post;
	}

	public void setPost(Post post) {
		this.post = post;
	}

	public LocalDate getCommentDate() {
		return commentDate;
	}

	public void setCommentDate(LocalDate commentDate) {
		this.commentDate = commentDate;
	}

	public LocalTime getCommentTime() {
		return commentTime;
	}

	public void setCommentTime(LocalTime commentTime) {
		this.commentTime = commentTime;
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

	public Profile getProfile() {
		return profile;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}
	
	public boolean isDeletableByAuthor() {
		return deletableByAuthor;
	}

	public void setDeletableByAuthor(boolean deletableByAuthor) {
		this.deletableByAuthor = deletableByAuthor;
	}

	public Language getPostCommentLanguage() {
		return postCommentLanguage;
	}

	public void setPostCommentLanguage(Language postCommentLanguage) {
		this.postCommentLanguage = postCommentLanguage;
	}

	@Override
	public String toString() {
		return content;
	}

}
