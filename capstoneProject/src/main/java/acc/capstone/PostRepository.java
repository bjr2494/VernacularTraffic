package acc.capstone;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface PostRepository extends CrudRepository<Post, Long> {
	public List<Post> findAllByOrderByPostDateDesc();

	public List<Post> findAllByAuthorId(int id);
	
	public Optional<Post> findByName(String postName);

	public Optional<Post> findById(int postId);
	
	public List<Post> findAllByProfileId(int profileId);

	public List<Post> findAllByOrderByIdDesc();

	public Page<Post> findAll(Pageable pageable);

}
