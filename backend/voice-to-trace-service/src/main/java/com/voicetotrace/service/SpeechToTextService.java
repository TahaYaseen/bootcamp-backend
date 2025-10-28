package com.voicetotrace.service;

import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;

@Service
public class SpeechToTextService {

    public static class TranscriptionResult {
        public final String transcript;
        public final float confidence;
        public TranscriptionResult(String transcript, float confidence) {
            this.transcript = transcript;
            this.confidence = confidence;
        }
    }

    public TranscriptionResult transcribeAudio(String filePath) throws IOException, InterruptedException {
        // Automatically convert WebM/Opus audio recorded from Chrome into WAV/PCM for reliable transcription
        String processedFilePath = filePath;

        if (filePath.toLowerCase().endsWith(".webm") ||
            filePath.toLowerCase().endsWith(".opus") ||
            filePath.toLowerCase().endsWith(".ogg")) {

            String wavPath = filePath.substring(0, filePath.lastIndexOf('.')) + "_converted.wav";

            try {
                ProcessBuilder pb = new ProcessBuilder(
                        "ffmpeg", "-y",
                        "-i", filePath,
                        "-ac", "1",           // mono
                        "-ar", "16000",       // 16 kHz
                        wavPath
                );
                pb.redirectErrorStream(true);
                Process process = pb.start();
                process.waitFor();
                processedFilePath = wavPath;
                System.out.println("[FFmpeg] Converted WebM/Opus to WAV: " + processedFilePath);
            } catch (Exception e) {
                System.err.println("[FFmpeg ERROR] Conversion failed: " + e.getMessage());
            }
        }

        try (SpeechClient speechClient = SpeechClient.create()) {
            byte[] data = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(processedFilePath));
            ByteString audioBytes = ByteString.copyFrom(data);

            // Dynamically detect audio encoding based on file extension
            String lowerName = filePath.toLowerCase();
            RecognitionConfig.AudioEncoding encoding = RecognitionConfig.AudioEncoding.ENCODING_UNSPECIFIED;
            int sampleRate = 48000; // Chrome recordings default to 48kHz

            if (lowerName.endsWith(".wav") || lowerName.endsWith(".pcm")) {
                encoding = RecognitionConfig.AudioEncoding.LINEAR16;
                sampleRate = 16000;
            } else if (lowerName.endsWith(".flac")) {
                encoding = RecognitionConfig.AudioEncoding.FLAC;
            } else if (lowerName.endsWith(".ogg") || lowerName.endsWith(".opus")) {
                encoding = RecognitionConfig.AudioEncoding.OGG_OPUS;
            } else if (lowerName.endsWith(".webm")) {
                encoding = RecognitionConfig.AudioEncoding.WEBM_OPUS;
            } else if (lowerName.endsWith(".mp3")) {
                // MP3 not officially supported by some Speech API versions; fallback to unspecified
                encoding = RecognitionConfig.AudioEncoding.ENCODING_UNSPECIFIED;
            }

            // Determine encoding: for WEBM/OPUS audio, omit sample rate entirely to let Google auto-detect 48 kHz
            RecognitionConfig.Builder configBuilder = RecognitionConfig.newBuilder()
                    .setEncoding(encoding)
                    .setLanguageCode("en-US");

            String lowerProcessed = processedFilePath.toLowerCase();
            if (lowerProcessed.endsWith(".wav")) {
                configBuilder.setSampleRateHertz(16000);
            } else if (lowerProcessed.endsWith(".webm") || lowerProcessed.endsWith(".opus")) {
                // Force OPUS/WEBM to use correct encoding and NO sample rate at all
                encoding = RecognitionConfig.AudioEncoding.WEBM_OPUS;
                System.out.println("Detected OPUS/WebM audio – omitting sampleRate for 48kHz auto-detection");
            } else {
                // Default for anything else (no fixed rate)
                System.out.println("Unknown format – skipping sampleRate");
            }

            // Final config → DO NOT explicitly include any sample rate; let API detect it for all formats
            // Reuse existing builder to configure dynamically without redeclaration or conflicts
            configBuilder.clearSampleRateHertz();

            if (lowerProcessed.endsWith(".wav")) {
                // Standard 16kHz PCM audio
                configBuilder.setEncoding(RecognitionConfig.AudioEncoding.LINEAR16);
                configBuilder.setSampleRateHertz(16000);
            } else if (lowerProcessed.endsWith(".webm") || lowerProcessed.endsWith(".opus")) {
                // OPUS / WEBM encoded input; omit sample rate completely
                System.out.println("Detected OPUS/WebM input – omitting sampleRate entirely for 48kHz auto-detection");
                configBuilder.setEncoding(RecognitionConfig.AudioEncoding.ENCODING_UNSPECIFIED);
            } else {
                configBuilder.setEncoding(RecognitionConfig.AudioEncoding.ENCODING_UNSPECIFIED);
            }

            // Build final recognition configuration only once
            RecognitionConfig config = configBuilder.build();

            System.out.println("Detected file format: " + encoding + " | sampleRate=" + sampleRate);

            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setContent(audioBytes)
                    .build();

            RecognizeResponse response = speechClient.recognize(config, audio);
            StringBuilder transcriptBuilder = new StringBuilder();
            float confidence = 0.0f;

            for (SpeechRecognitionResult result : response.getResultsList()) {
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                transcriptBuilder.append(alternative.getTranscript());
                confidence = alternative.getConfidence();
            }

            return new TranscriptionResult(transcriptBuilder.toString(), confidence);
        }
    }
}