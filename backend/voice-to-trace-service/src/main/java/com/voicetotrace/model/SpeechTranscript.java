package com.voicetotrace.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "speech_transcripts")
public class SpeechTranscript {

    @Id
    private String id;

    private String audioRecordId;
    private String text;
    private float confidence;
    private LocalDateTime createdAt;

    public SpeechTranscript() {
        this.createdAt = LocalDateTime.now();
    }

    public SpeechTranscript(String audioRecordId, String text, float confidence) {
        this.audioRecordId = audioRecordId;
        this.text = text;
        this.confidence = confidence;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getAudioRecordId() {
        return audioRecordId;
    }

    public void setAudioRecordId(String audioRecordId) {
        this.audioRecordId = audioRecordId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}