# DEVELOPMENT PLAN – Voice-to-Trace Assistant (MVP v2.0)

## 1. Overview

The **Voice-to-Trace Assistant** enables agricultural field workers to log harvest and traceability events using voice. The system captures spoken input, converts it to text, interprets the text using NLP to extract relevant field data, generates structured JSON trace records, and transmits them to the Trace API.

This updated development plan aligns engineering implementation directly with the refined PRD (v2.0, Oct 2025).

---

## 2. Architecture & Stack

### **Tech Stack**
- **Frontend:** Angular 18, TypeScript, TailwindCSS, ShadCN-ui  
- **Backend:** Spring Boot (Java 11), MongoDB, Google Speech-to-Text API  
- **APIs:** REST (`/api/v1/...`)  
- **Deployment:**  
  - Backend → Render  
  - Frontend → Vercel  
- **Authentication:** JWT (Spring Security)  

### **Architecture Pattern**
- **Modular Monolith**
  - `/voice-module` = voice capture, upload, transcription endpoints  
  - `/nlp-module` = NLP + JSON formatting  
  - `/integration-module` = Trace API sync + retry logic  

---

## 3. Module Structure

| Module | Key Classes | Description |
|--------|--------------|-------------|
| **Audio Module** | `VoiceController`, `AudioRecord`, `SpeechToTextService` | Manages voice recording, uploading, and transcription |
| **NLP Module** | `TextToJsonService`, `ParsedEvent` model | Parses transcript into domain entities & JSON |
| **Trace Integration Module** | `TraceRecordService`, `TraceSyncController` | Pushes structured JSON to Trace system using secure API |
| **Security Module** | `JwtAuthFilter`, `AuthController` | Handles user authentication |
| **Storage Module** | `Mongo Repositories` | Persistence for Audio, Transcript, ParsedEvent, and TraceRecord |

---

## 4. Development Phases & Sprints

### **Sprint 0 – Environment Setup**
| Task | Description | Notes |
|------|--------------|-------|
| 1 | Repository sync with GitHub | Configure project structure |
| 2 | MongoDB Atlas connection | Confirm DB URL in `.env` |
| 3 | Google API credential setup | `GOOGLE_APPLICATION_CREDENTIALS` |
| 4 | Angular + Spring Boot scaffolding | Validate both servers run |
| 5 | Health Check endpoint | `GET /api/v1/health → {status:"ok"}` |
| 6 | Documentation setup | Root `README.md` with setup notes |

---

### **Sprint 1 – Voice Capture & Transcription**
| Task | Description | Acceptance Criteria |
|------|--------------|----------------------|
| 1 | Implement audio upload endpoint `/api/v1/voice/record` | Saves file and creates `AudioRecord` |
| 2 | Integrate Google Speech-to-Text API | Converts stored audio to text |
| 3 | Create Transcript entity + repository | Stores text + confidence |
| 4 | Expose `/api/v1/voice/transcribe?recordId` | Returns transcript JSON |
| 5 | UI buttons for Record, Stop, Upload | Angular app records & submits |
| 6 | User testing | Verify transcription latency <3s |

---

### **Sprint 2 – NLP & Structured JSON**
| Task | Description | Acceptance Criteria |
|------|--------------|----------------------|
| 1 | Implement `TextToJsonService` | Extracts intent, lot, field, actions |
| 2 | Add `/api/v1/voice/analyze/{transcriptId}` endpoint | Returns structured JSON |
| 3 | Frontend “View JSON” preview screen | Show generated trace record |
| 4 | Add NLP accuracy logs | Track entity extraction success |
| 5 | Validation workflow | Manual correction on frontend |
| 6 | Unit testing: NLP edge cases | Achieve ≥90% extraction accuracy |

---

### **Sprint 3 – Trace API Integration**
| Task | Description | Acceptance Criteria |
|------|--------------|----------------------|
| 1 | Develop TraceRecord entity | Stores API payload + status |
| 2 | Implement sync service `TraceRecordService` | Pushes to external API |
| 3 | Handle OAuth/Key-based auth | Via `.env` variable |
| 4 | Retry failed sends | Exponential backoff + max 3 retries |
| 5 | UI “Sync Status” page | Shows pending/completed events |
| 6 | Verify successful ingestion | 100% valid records posted |

---

### **Sprint 4 – Authentication & Audit Logging**
| Task | Description | Acceptance Criteria |
|------|--------------|----------------------|
| 1 | JWT Auth for all endpoints | Token in request header |
| 2 | Role-based login (worker) | Secure `/voice/*` APIs |
| 3 | Add audit log entries | Audio→Transcript→JSON chain log |
| 4 | Display user activity logs | Admin panel |
| 5 | Test with multiple users | Isolated session logs |

---

### **Sprint 5 – Deployment & QA**
| Task | Description | Acceptance Criteria |
|------|--------------|----------------------|
| 1 | Configure Render backend deployment | Auto deploy from Git branch |
| 2 | Configure Vercel frontend preview | Each branch = unique preview URL |
| 3 | Full regression testing | Validate workflow end-to-end |
| 4 | Field trial feedback integration | MVP refinement checkpoints |
| 5 | README updates + API docs | Include endpoint index |

---

## 5. Feature Traceability to PRD Requirements

| PRD Feature | Matching Sprint | Implementation Summary |
|--------------|------------------|--------------------------|
| FR-001 – Speech-to-Text | Sprint 1 | `/record` endpoint + API integration |
| FR-002 – NLP Parsing | Sprint 2 | `TextToJsonService` created |
| FR-003 – JSON Generation | Sprint 2 | Auto-created JSON schema exporter |
| FR-004 – Trace API Sync | Sprint 3 | `TraceRecordService` integration |
| FR-101 – Authentication | Sprint 4 | JWT Security |
| FR-102 – Audit Logging | Sprint 4 | `AuditEvent` Document + Mongo store |

---

## 6. Deliverables by End of MVP

✅ Full working chain:  
**Voice → Text → Parsed Entities → JSON → Trace API Sync**

✅ Documented APIs:  
| Endpoint | Description |
|-----------|--------------|
| `POST /api/v1/voice/record` | Upload audio |
| `POST /api/v1/voice/transcribe` | Convert audio to text |
| `POST /api/v1/voice/analyze/{id}` | Convert text → JSON |
| `POST /api/v1/trace/sync` | Push JSON to Trace system |

✅ CI/CD automation  
✅ Deployments (Vercel + Render)  
✅ User access with JWT  
✅ Mongo data persistence  

---

## 7. Validation & Testing

| Phase | Test Type | Tool / Method |
|--------|-------------|----------------|
| Unit Testing | Service-level | JUnit 5 |
| Integration | REST endpoints | Postman |
| UI Testing | Manual browser steps | Angular Dev Server |
| Regression | CI build hooks | GitHub Actions |
| Load | 50 concurrent API calls | JMeter |

Success metric:  
**End-to-end request (record → trace)** executes under 7 seconds | Accuracy ≥ 90%.

---

## 8. Next Steps Beyond MVP
- DF-001: Multilingual NLP pipeline (Phase 2+ enhancement)  
- DF-002: Batch sync with offline caching  
- DF-003: Extended reporting dashboard for compliance  

---

**Last Updated:** October 2025  
**Aligned To:** Refined PRD 2.0  
**Prepared By:** Harvey (Development Strategist)  
