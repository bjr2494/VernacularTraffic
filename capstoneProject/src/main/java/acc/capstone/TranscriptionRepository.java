package acc.capstone;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface TranscriptionRepository extends CrudRepository<Transcription, Long> {
	public List<Transcription> findAllByOrderByPostId();

	public List<Transcription> findAllByAuthorId(int id);
	
	public Optional<Transcription> findByName(String transcriptionName);

	public Optional<Transcription> findById(int transcriptionId);

	public List<Transcription> findAllByProfileId(int profileId);
}
