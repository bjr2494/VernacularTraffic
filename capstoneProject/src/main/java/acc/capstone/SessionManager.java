package acc.capstone;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

public class SessionManager {
	
	@Autowired
	UserRepository userRepository;
	
	private User loggedInUser = null;
	

    private List<User> users;
	private int numProfiles;
	private int numPosts;
	private Page<Post> posts;
	private String plural;
	private String verb;
	
	public Page<Post> getPosts() {
		return posts;
	}

	public void setPosts(Page<Post> posts) {
		this.posts = posts;
	}

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
    
	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public int getNumPosts() {
		return numPosts;
	}

	public void setNumPosts(int numPosts) {
		this.numPosts = numPosts;
	}

	public int getNumProfiles() {
		return numProfiles;
	}

	public void setNumProfiles(int numProfiles) {
		this.numProfiles = numProfiles;
	}

	public String getPlural() {
		return plural;
	}

	public void setPlural(String plural) {
		this.plural = plural;
	}

	public String getVerb() {
		return verb;
	}

	public void setVerb(String verb) {
		this.verb = verb;
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
