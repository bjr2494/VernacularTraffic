package acc.capstone;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotEmpty;

@Entity
public class PreferredLanguage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	private Language name;

	public Language getName() {
		return name;
	}

	public void setName(Language name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "PreferredLanguage [name=" + name + "]";
	}
}
