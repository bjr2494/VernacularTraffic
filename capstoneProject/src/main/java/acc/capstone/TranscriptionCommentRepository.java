package acc.capstone;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface TranscriptionCommentRepository extends CrudRepository<TranscriptionComment, Long> {

	public List<TranscriptionComment> findAllByAuthorId(int id);

	public List<TranscriptionComment> findAllByOrderByTranscriptionId();

	public List<TranscriptionComment> findAllByProfileId(int profileId);
	
	public Optional<TranscriptionComment> findById(int transcriptionCommentId);
	
	public List<TranscriptionComment> findAllByTranscriptionId(int transcriptionId);
}
