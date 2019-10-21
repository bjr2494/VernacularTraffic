package acc.capstone;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import javax.persistence.Column;
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
public class Transcription {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String name;
	private LocalDate transcriptionDate;
	private LocalTime transcriptionTime;
	@JsonBackReference
	@OneToOne
	private Post post;
	@ManyToOne
	private User author;
	@ManyToOne
	@JsonBackReference
	private Profile profile;
	//@NotEmpty(message="no empty transcriptions")
	//@Size(min=4, max=120, message="at the very least, no fewer than four characters, but please")
	private String content;
	@OneToMany(mappedBy="transcription") 
	private List<TranscriptionComment> transcriptionComments;
	private boolean hasComments;
	private boolean hasOneComment;
	private int numTranscriptionComments;
	private boolean editableByAuthor;
	private boolean deletableByAuthor;
	private boolean commentableByAuthor;
	@Enumerated(EnumType.STRING)
	private Language transcriptionLanguage;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public LocalDate getTranscriptionDate() {
		return transcriptionDate;
	}

	public void setTranscriptionDate(LocalDate transcriptionDate) {
		this.transcriptionDate = transcriptionDate;
	}
	
	public LocalTime getTranscriptionTime() {
		return transcriptionTime;
	}

	public void setTranscriptionTime(LocalTime transcriptionTime) {
		this.transcriptionTime = transcriptionTime;
	}

	public Post getPost() { 
		return post; 
	}
	  
	public void setPost(Post post) {
		this.post = post; 
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
		
	public List<TranscriptionComment> getTranscriptionComments() {
		return transcriptionComments;
	}

	public void setTranscriptionComments(List<TranscriptionComment> transcriptionComments) {
		this.transcriptionComments = transcriptionComments;
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
	
	public Language getTranscriptionLanguage() {
		return transcriptionLanguage;
	}

	public void setTranscriptionLanguage(Language transcriptionLanguage) {
		this.transcriptionLanguage = transcriptionLanguage;
	}
	
	public int getNumTranscriptionComments() {
		return numTranscriptionComments;
	}

	public void setNumTranscriptionComments(int numTranscriptionComments) {
		this.numTranscriptionComments = numTranscriptionComments;
	}

	@Override
	public String toString() {
		return "content = " + content;
	}
}
