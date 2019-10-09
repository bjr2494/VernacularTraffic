package acc.capstone;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface PostCommentRepository extends CrudRepository<PostComment, Long> {

	public List<PostComment> findAllByOrderByPostId();

	public List<PostComment> findAllByAuthorId(int id);

	public List<PostComment> findAllByProfileId(int profileId);
	
	public List<PostComment> findAllByPostId(int postId);

	public Optional<PostComment> findById(int postCommentId);
	
}
