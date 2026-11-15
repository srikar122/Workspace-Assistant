# Executive AI Workspace — Agentic Meeting & Executive Assistant

An enterprise full-stack **Agentic Meeting & Executive Assistant** built with **Spring Boot 3**, **LangChain4j**, **Google Gemini**, **PostgreSQL**, and **React**. The application enables users to schedule or reschedule meetings, detect calendar conflicts, manage action-item tasks, and dispatch attendee email notifications through a single natural language prompt.

---

## Architecture & End-to-End Request Flow

```text
[React Frontend :5173]  (TanStack Query v5 + Axios Interceptor + Tailwind CSS)
         │
         │  POST /api/v1/workspace/chat  { userId, message }
         ▼
┌────────────────────────────────────────────────────────────────────────────┐
│ Spring Boot Backend :8080                                                  │
│                                                                            │
│  1. SecurityConfig       ──► CORS validation & Google OAuth2 token capture │
│  2. WorkspaceController  ──► @Valid DTO binding & ApiResponse<T> envelope  │
│  3. OrchestratorService  ──► Manages session lifecycle & DB persistence    │
│  4. ExecutiveAgent       ──► LangChain4j @AiService + Google Gemini LLM    │
│  5. Autonomous @Tools    ──► CalendarTools | TaskTools | EmailTools        │
│  6. Business Services    ──► Dual-mode Google API + PostgreSQL execution   │
└────────────────────────────────────────────────────────────────────────────┘
         │                                       │
         ▼                                       ▼
[PostgreSQL :5432]                      [Google Cloud APIs]
 • tasks                                 • Google Calendar API (v3)
 • calendar_events                       • Gmail API (v1)
 • chat_messages (@MemoryId history)
```

---

## Key Features

### Backend & AI Orchestration
* **Multi-Tool AI Orchestration:** Powered by LangChain4j (`@AiService` + `@Tool`) and Google Gemini (`gemini-3.5-flash-lite`), the agent autonomously chains multiple actions from one prompt (e.g., *inspecting calendar conflicts $\rightarrow$ rescheduling a meeting $\rightarrow$ emailing attendees $\rightarrow$ creating a follow-up task*).
* **Persistent Per-User Session Memory:** Custom `ChatMemoryStore` backed by PostgreSQL (`chat_messages`) isolates conversation history per `userId` (`@MemoryId`) while sanitizing turn sequences across requests.
* **Dual-Mode Google OAuth2 & Local Execution:**
  * **Connected Mode:** Authenticates via Spring Security OAuth2 (`/oauth2/authorization/google`) to read/write live Google Calendar events and send real emails via Gmail.
  * **Local Workspace Mode:** Allows instant sign-in with any Workspace ID/Email, gracefully falling back to PostgreSQL persistence and simulated email dispatch without requiring external OAuth setup.
* **Proactive Calendar Webhook Listener:** Exposes `POST /api/v1/webhooks/calendar` to handle Google Calendar push notifications asynchronously (`CompletableFuture`) and generate live conflict alerts.
* **Enterprise Backend Standards:** Implements the Interface–`ServiceImpl` pattern, standardized `ApiResponse<T>` records, Jakarta Bean Validation (`@Valid`), centralized `@RestControllerAdvice` exception handling, and JUnit 5 / Mockito / MockMvc test suites.

### Frontend Engineering & UI/UX
* **Reactive Server-State & Cache Synchronization (TanStack Query v5):** Custom query and mutation hooks (`useWorkspaceQueries.js`) manage caching for events, tasks, and per-user chat history. When `useSendPromptMutation` resolves an AI response, it automatically invalidates `['workspace', 'history', userId]`, `['workspace', 'events']`, and `['workspace', 'tasks']`—updating all three workspace panels simultaneously without manual state-sync boilerplate.
* **Decoupled Network Layer & Response Unwrapping:** A centralized `axiosClient` instance handles base URL configuration (`VITE_API_BASE_URL`), request timeouts (45s for multi-tool LLM execution), and response interception—automatically unwrapping the backend's `ApiResponse<T>` envelope (`response.data.data`) and normalizing backend validation/business errors into clean UI alerts.
* **Seamless Dual-Auth Session Management (`AuthContext`):** Supports both Google OAuth2 callback parameter hydration (`?connected=true&userId=...&name=...`), live backend session verification (`GET /auth/status`), and persistent `localStorage` Workspace ID sessions with clean URL history replacement (`window.history.replaceState`).
* **Multi-Stage AI Tool-Orchestration Loader & Skeleton Shimmers:**
  * Displays an animated, multi-step progress card inside `ChatContainer` that cycles through real-time execution stages (*Inspecting live calendar for conflicts* $\rightarrow$ *Executing autonomous tool chain* $\rightarrow$ *Dispatching emails & updating tasks*) while Gemini chains tools.
  * Renders custom CSS shimmer skeleton cards in `CalendarSidebar` during initial data fetches for smooth perceived performance.
* **Enterprise Light-Theme Design & Accessibility:** Built with Tailwind CSS v4 (`@tailwindcss/postcss`), custom keyframe animations (`fade-in-up`, `shimmer`), one-click starter prompt cards, auto-scrolling chat viewports, ARIA-labeled controls, and automated component tests using **Vitest** and **React Testing Library**.

---

## Frontend Architecture & State Flow

```text
[LoginPage / OAuth2 Redirect]
       │  Stores { userId, name, googleConnected } in AuthContext + localStorage
       ▼
[WorkspaceDashboard]
 ├── Navbar             ──► Displays active user badge, Google OAuth status, manual Sync & Sign Out
 ├── ChatContainer      ──► Submits prompt via useSendPromptMutation(userId)
 │                          └── Shows 3-stage animated tool-orchestration loader while pending
 └── Right Aside Panel
      ├── CalendarSidebar ──► Subscribes to useEventsQuery() (shimmer skeletons + attendee chips)
      └── TaskSidebar     ──► Subscribes to useTasksQuery() + one-click useCompleteTaskMutation()
```

---

## Tech Stack

| Layer | Technologies |
| :--- | :--- |
| **Backend** | Java 21, Spring Boot 3.3.4, Spring Web, Spring Data JPA, Hibernate, Spring Security OAuth2 Client, Jakarta Validation, Lombok |
| **AI & Integrations** | LangChain4j (`langchain4j-google-ai-gemini`, `langchain4j-spring-boot-starter`), Google Gemini API, Google Calendar API, Gmail API |
| **Database** | PostgreSQL 15+ (HikariCP connection pooling) |
| **Frontend** | React 18, Vite, TanStack Query v5, Axios, Tailwind CSS v4 (`@tailwindcss/postcss`), Lucide Icons |
| **Testing** | JUnit 5, Mockito, Spring MockMvc, Vitest, React Testing Library, JSDOM |

---

## Project Structure

```text
Workspace-Assistant/
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/org/workspace/
│       │   │   ├── WorkspaceApplication.java
│       │   │   ├── agent/          # ExecutiveAgent, CalendarTools, TaskTools, EmailTools
│       │   │   ├── common/         # ApiResponse<T>, BusinessException, GlobalExceptionHandler
│       │   │   ├── config/         # AgentConfig, SecurityConfig, GoogleTokenHolder
│       │   │   ├── controller/     # WorkspaceController, WebhookController
│       │   │   ├── domain/         # JPA Entities & Enums (TaskStatus, MessageType)
│       │   │   ├── dto/            # Immutable Java Request & Response Records
│       │   │   ├── repository/     # Spring Data JPA Repositories
│       │   │   └── service/        # Service Interfaces & Implementations
│       │   └── resources/
│       │       └── application.yml
│       └── test/                   # Unit & WebMvc Slice Tests
└── frontend/
    ├── package.json
    ├── vite.config.js
    ├── postcss.config.js
    ├── tailwind.config.js
    └── src/
        ├── api/                    # axiosClient.js (interceptor) & workspaceApi.js
        ├── components/             # Navbar, ChatContainer, CalendarSidebar, TaskSidebar
        ├── context/                # AuthContext.jsx (OAuth2 & Local session state)
        ├── hooks/                  # useWorkspaceQueries.js (TanStack Query hooks)
        ├── pages/                  # LoginPage.jsx, WorkspaceDashboard.jsx
        ├── providers/              # QueryProvider.jsx
        └── tests/                  # Vitest & React Testing Library component specs
