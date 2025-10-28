package com.voicetotrace.repository;

import com.voicetotrace.model.SpeechTranscript;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SpeechTranscriptRepository extends MongoRepository<SpeechTranscript, String> {
    List<SpeechTranscript> findByAudioRecordId(String audioRecordId);
}