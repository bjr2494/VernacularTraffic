package acc.capstone;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface ProfileRepository extends CrudRepository<Profile, Long>{
	public Optional<Profile> findById(int id);
	public List<Profile> findAllByOrderById();


	
}
