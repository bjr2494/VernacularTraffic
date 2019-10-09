package acc.capstone;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonBackReference;

/*
(1, 'boudica', 'queen', '', '2018-04-19', '', '[English, Spanish]'),
(2, 'hasdrubal', 'fair', '', '2018-05-22', '', 'English, French'),
(3, 'dámaso', 'domar', '', '2018-06-18', '', 'Spanish'), 
(4, 'chantal', 'chanteuse', '2018-07-22', '', 'French'),
(5, 'agnes', 'polyglot', '2018-08-15', '', 'English, French, Spanish'),
(6, 'adassa', 'reggaeton', '2018-09-04', '', 'Spanish, English'),
(7, 'vincent', 'callebaut', '2018-10-17', '', 'French, Spanish'),
(8, 'alfred', 'great', '2018-11-25', '', 'English');*/

@Entity
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(unique=true)
	@NotEmpty
	private String username;
	@NotEmpty
	private String password;
	@NotEmpty
	private String salt;
	@NotNull
	private LocalDate joinDate;
	@OneToOne
	@JsonBackReference
	private Profile profile;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public LocalDate getJoinDate() {
		return joinDate;
	}

	public void setJoinDate(LocalDate joinDate) {
		this.joinDate = joinDate;
	}

	public Profile getProfile() {
		return profile;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", username=" + username + ", password=" + password + ", salt=" + salt + ", joinDate="
				+ joinDate + ", profile=" + profile + "]";
	}

	
	
}
