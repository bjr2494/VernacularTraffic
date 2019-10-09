package acc.capstone;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
	public User findLoggedInUserByUsername(String username);
	
	public Optional<User> findByUsername(String username);
	
	public Optional<User> findById(int userId);
	
	public List<User> findAllByOrderByJoinDateDesc();
}
