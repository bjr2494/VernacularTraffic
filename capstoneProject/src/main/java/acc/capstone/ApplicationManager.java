package acc.capstone;

import java.util.List;

public class ApplicationManager {

	private List<Profile> profiles;
	private int numProfiles;
	private int numPosts;
	private String plural;
	private String verb;

	public List<Profile> getProfiles() {
		return profiles;
	}

	public void setProfiles(List<Profile> profiles) {
		this.profiles = profiles;
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
}
