package acc.capstone;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

public class SessionManager {
	
	@Autowired
	UserRepository userRepository;
	
	private User loggedInUser = null;
	
	public boolean isLoggedIn() {
		return loggedInUser != null;
	}
	
	public User getLoggedInUser() {
		//System.out.println(this.loggedInUser);  
		Optional<User> optionalUser = this.userRepository.findById(this.loggedInUser.getId());
		System.out.println(optionalUser.get().getProfile());
		System.out.println(optionalUser.get());
		return optionalUser.get();
	}
	
	public void setLoggedInUser(User loggedInUser) {
		this.loggedInUser = loggedInUser;
	}
    
    public void login(User user) {
    	this.loggedInUser = user;
    }
    
    public void logout() {
    	this.loggedInUser = null;
    }

    @Override
    public String toString() {
    	if (loggedInUser!=null) {
    		return "sessionManager[" + loggedInUser.getId() + ":" + loggedInUser.getUsername() + "]";
    	} else {
    		return "sessionmanager[loggedout]";
    	}
    }

}
