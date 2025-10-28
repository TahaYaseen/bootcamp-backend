package com.voicetotrace.repository;

import com.voicetotrace.model.AudioRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AudioRecordRepository extends MongoRepository<AudioRecord, String> {
    List<AudioRecord> findByUserId(Long userId);
}