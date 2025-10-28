package com.voicetotrace.controller;

import com.voicetotrace.model.AudioRecord;
import com.voicetotrace.repository.AudioRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/voice")
public class VoiceController {

    @Autowired
    private AudioRecordRepository audioRecordRepository;

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/backend/voice-to-trace-service/uploads";

    @PostMapping("/record")
    public ResponseEntity<Object> uploadAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId) {

        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No file uploaded");
        }

        try {
            // ensure uploads directory exists
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // save file to local folder
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            file.transferTo(filePath.toFile());

            // persist record
            AudioRecord record = new AudioRecord(userId, filePath.toString());
            audioRecordRepository.save(record);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Audio uploaded successfully");
            response.put("filePath", record.getFilePath());
            response.put("recordId", record.getId());
            response.put("createdAt", record.getCreatedAt());

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving audio file: " + e.getMessage());
        }
    }

    @Autowired
    private com.voicetotrace.service.TextToJsonService textToJsonService;

    @PostMapping("/analyze/{transcriptId}")
    public ResponseEntity<Object> analyzeTranscript(@PathVariable String transcriptId) {
        try {
            var optionalTranscript = speechTranscriptRepository.findById(transcriptId);
            if (optionalTranscript.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Transcript not found");
            }
            var transcript = optionalTranscript.get();
            var jsonResult = textToJsonService.convertToJson(transcript.getText());
            return ResponseEntity.ok(jsonResult);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error analyzing transcript: " + e.getMessage());
        }
    }

    @Autowired
    private com.voicetotrace.service.SpeechToTextService speechToTextService;

    @Autowired
    private com.voicetotrace.repository.SpeechTranscriptRepository speechTranscriptRepository;

    @PostMapping("/transcribe")
    public ResponseEntity<Object> transcribeAudio(@RequestParam("recordId") String recordId) {
        try {
            java.util.Optional<com.voicetotrace.model.AudioRecord> optional = audioRecordRepository.findById(recordId);
            if (optional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("AudioRecord not found");
            }

            String filePath = optional.get().getFilePath();
            com.voicetotrace.service.SpeechToTextService.TranscriptionResult result =
                    speechToTextService.transcribeAudio(filePath);

            com.voicetotrace.model.SpeechTranscript transcript =
                    new com.voicetotrace.model.SpeechTranscript(recordId, result.transcript, result.confidence);
            speechTranscriptRepository.save(transcript);

            Map<String, Object> response = new HashMap<>();
            response.put("recordId", recordId);
            response.put("transcriptId", transcript.getId());
            response.put("text", transcript.getText());
            response.put("confidence", transcript.getConfidence());
            response.put("createdAt", transcript.getCreatedAt());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during transcription: " + e.getMessage());
        }
    }

    @GetMapping("/transcripts")
    public ResponseEntity<Object> getAllTranscripts() {
        try {
            return ResponseEntity.ok(speechTranscriptRepository.findAll());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching transcripts: " + e.getMessage());
        }
    }

    @PutMapping("/transcripts/{id}")
    public ResponseEntity<Object> updateTranscript(
            @PathVariable String id,
            @RequestBody Map<String, String> requestBody) {
        try {
            var optionalTranscript = speechTranscriptRepository.findById(id);
            if (optionalTranscript.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Transcript not found");
            }

            var transcript = optionalTranscript.get();
            transcript.setText(requestBody.get("text"));
            speechTranscriptRepository.save(transcript);

            return ResponseEntity.ok(transcript);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating transcript: " + e.getMessage());
        }
    }
}