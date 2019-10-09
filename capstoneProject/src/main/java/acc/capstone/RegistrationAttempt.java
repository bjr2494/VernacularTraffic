package acc.capstone;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

public class RegistrationAttempt {
	
	@NotEmpty
	@Size(min=5, max=15, message="between five and fifteen characters for the username, please")
	private String username;
	@NotEmpty
	@Size(min=5, max=15, message="between five and fifteen characters, please")
	private String passwordOne;
	@NotEmpty
	@Size(min=5, max=15, message="between five and fifteen characters, please")
	private String passwordTwo;
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPasswordOne() {
		return passwordOne;
	}

	public void setPasswordOne(String passwordOne) {
		this.passwordOne = passwordOne;
	}

	public String getPasswordTwo() {
		return passwordTwo;
	}

	public void setPasswordTwo(String passwordTwo) {
		this.passwordTwo = passwordTwo;
	}

	@Override
	public String toString() {
		return username;
	}

}
