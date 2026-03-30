# MIA (Memo Intelligence Assistant) — AI 채팅 기능 기획 & 플래닝

> 작성일: 2026-03-30
> 목적: Memozy 앱에 Claude 기반 AI 어시스턴트 "MIA" 채팅 기능을 설계하기 위한 선행 조사 + 전체 구현 플랜

---

## 0. 핵심 질문 답변

### Q. REST API로 Claude 호출하면 되는가?
**Yes.** Claude는 `POST https://api.anthropic.com/v1/messages` 단일 엔드포인트로 모든 것을 처리한다.
OkHttp/Retrofit 없이 **Ktor + Ktorfit** 조합으로 완전히 대체 가능하다.

### Q. AI가 앱의 메모를 관리할 수 있는가?
**Yes, Tool Use를 통해 가능하다.**
MIA가 메모를 CRUD하려면 우리가 도구(Tool)를 정의하고, Claude가 도구 호출을 요청하면 앱이 실제로 Repository를 통해 실행 후 결과를 돌려주는 방식이다.
→ Claude가 직접 DB를 건드리는 게 아니라, **앱이 중간 실행자** 역할을 한다.

---

## 1. 기술 스택 결정

| 역할 | 선택 | 이유 |
|------|------|------|
| HTTP 클라이언트 | **Ktor Client** | OkHttp/Retrofit 금지, Kotlin-first, Coroutine 네이티브 |
| REST 인터페이스 | **Ktorfit** | Retrofit 스타일 인터페이스 + Ktor 백엔드 |
| 스트리밍 | Ktor SSE (Server-Sent Events) | Claude streaming 지원 |
| API 키 저장 | **EncryptedSharedPreferences** | Android KeyStore 기반 AES-256 암호화 |
| 설정 저장 | **DataStore (Preferences)** | 비암호화 설정값 (모델 선택 등) |
| 채팅 DB | **Room** | 기존 MemoDatabase 패턴 그대로 확장 |
| DI | Hilt | 기존 패턴 유지 |
| AI 모델 | `claude-haiku-4-5` (기본) / `claude-sonnet-4-6` (고급) | 비용 vs 성능 |

### 왜 Ktor + Ktorfit인가?
```
OkHttp + Retrofit → Kotlin 비친화적, Java 기반 레거시
Ktor Client       → 순수 Kotlin, Coroutine Flow, Multiplatform 지원
Ktorfit           → @GET/@POST 어노테이션 방식 유지 + Ktor 백엔드
```

---

## 2. Claude REST API 구조 이해

### 기본 요청 형식
```
POST https://api.anthropic.com/v1/messages
Headers:
  x-api-key: {ANTHROPIC_API_KEY}
  anthropic-version: 2023-06-01
  Content-Type: application/json

Body:
{
  "model": "claude-haiku-4-5",
  "max_tokens": 4096,
  "system": "You are MIA, a memo management assistant...",
  "messages": [
    {"role": "user", "content": "오늘 일정 메모 만들어줘"},
    {"role": "assistant", "content": "..."},   ← 이전 대화 히스토리 전체 포함
    {"role": "user", "content": "방금 만든 거 삭제해줘"}
  ],
  "tools": [ ... ]   ← Tool Use 정의 (3절 참고)
}
```

### 응답 형식
```json
{
  "id": "msg_xxx",
  "type": "message",
  "role": "assistant",
  "content": [
    {"type": "text", "text": "메모를 만들어드릴게요!"},
    {"type": "tool_use", "id": "toolu_xxx", "name": "create_memo", "input": {...}}
  ],
  "stop_reason": "tool_use"   ← 또는 "end_turn"
}
```

**Claude API는 Stateless다.**
매 요청마다 전체 대화 히스토리를 포함해서 보내야 한다.
→ 로컬 DB에 대화 내역을 저장하고 매 요청 시 불러와서 포함시켜야 한다.

### 스트리밍 (Streaming)
긴 응답을 실시간으로 표시하려면 `stream: true` + SSE(Server-Sent Events) 사용.
Ktor의 `HttpStatement.execute { response -> ... }` + `ServerSentEventSession`으로 처리 가능.
→ 채팅 UI에서 타이핑 효과처럼 보임.

---

## 3. Tool Use 완전 이해

### 3.1 개념: AI가 어떻게 앱을 조종하는가?

```
사용자: "내 메모 목록 보여줘"

1. 앱 → Claude API 요청 (tools 포함)
2. Claude → stop_reason: "tool_use" 응답
           content: [{type: "tool_use", name: "get_memos", input: {}}]
3. 앱 → input을 보고 MemoRepository.getMemos() 실행
4. 앱 → 실행 결과를 tool_result로 Claude에게 재전송
5. Claude → stop_reason: "end_turn" 응답
           content: [{type: "text", text: "메모가 5개 있어요: ..."}]
6. 앱 → 텍스트를 채팅 UI에 표시
```

**핵심 원리:** Claude는 "이 도구를 써야겠다"고 판단만 하고, 실제 실행은 앱이 한다.
Claude가 직접 DB를 건드리거나 코드를 실행하는 게 아님.

### 3.2 작동 흐름 (루프 구조)

```
┌─────────────────────────────────────────────────────────┐
│                    Tool Use Loop                        │
│                                                         │
│  messages = [... 대화 히스토리 ...]                      │
│                                                         │
│  while true:                                            │
│    response = claude.sendMessage(messages, tools)       │
│                                                         │
│    if response.stopReason == "end_turn":                │
│      → UI에 텍스트 출력, 루프 종료                       │
│                                                         │
│    if response.stopReason == "tool_use":                │
│      → messages에 assistant 응답 추가                   │
│      → 각 tool_use 블록마다:                            │
│          result = executeLocalTool(name, input)         │
│          toolResults.add(tool_result)                   │
│      → messages에 tool_results 추가                     │
│      → 루프 재시작 (Claude에게 결과 전달)                │
└─────────────────────────────────────────────────────────┘
```

### 3.3 MIA가 가질 Tool 목록

| Tool 이름 | 설명 | 실행 주체 |
|-----------|------|-----------|
| `get_all_memos` | 전체 메모 목록 조회 | MemoRepository.getMemos() |
| `search_memos` | 키워드로 메모 검색 | MemoRepository에서 필터링 |
| `get_memo_by_id` | 특정 메모 내용 조회 | MemoRepository.getMemoById() |
| `create_memo` | 새 메모 생성 | MemoRepository.addMemo() |
| `update_memo` | 메모 수정 | MemoRepository.updateMemo() |
| `delete_memo` | 메모 삭제 | MemoRepository.deleteMemo() |
| `get_categories` | 카테고리 목록 | MemoRepository.getCategories() |

### 3.4 Tool 정의 JSON 예시

```json
{
  "name": "create_memo",
  "description": "새로운 메모를 생성합니다. 사용자가 메모 작성을 요청할 때 사용하세요.",
  "input_schema": {
    "type": "object",
    "properties": {
      "name": {
        "type": "string",
        "description": "메모 제목"
      },
      "content": {
        "type": "string",
        "description": "메모 내용"
      },
      "categoryId": {
        "type": "integer",
        "description": "카테고리 ID (1=일반, 2=업무, 3=아이디어, 4=할일, 5=공부, 6=일정, 7=가계부, 8=운동, 9=건강, 10=여행, 11=쇼핑, 12=미분류)"
      }
    },
    "required": ["name", "content", "categoryId"]
  }
}
```

### 3.5 기존 앱과의 연결 포인트

MIA는 이미 존재하는 `MemoRepository`를 그대로 사용한다.
```
MIA ViewModel → Tool 실행 요청
              → MemoRepository (기존) 호출
              → DB 변경 → HomeScreen이 자동으로 반영 (Flow 구독 중)
```
별도 로직 없이 기존 Repository를 DI로 주입받아 재활용.

---

## 4. API 키 보안 저장 설계

### 4.1 위협 모델

| 위협 | 대응 |
|------|------|
| 코드에 하드코딩 | 절대 금지, 런타임에 사용자 입력 |
| 일반 SharedPreferences (평문) | EncryptedSharedPreferences 사용 |
| 루팅된 기기에서 파일 접근 | Android KeyStore → AES-256-GCM 암호화 |
| 메모리 덤프 | 사용 후 즉시 참조 해제 권장 |

### 4.2 저장 구조

```
EncryptedSharedPreferences (KeyStore 기반)
└── key: "anthropic_api_key"  → value: "sk-ant-xxx" (암호화 저장)

DataStore<Preferences> (일반)
└── key: "selected_model"     → value: "claude-haiku-4-5"
└── key: "mia_enabled"        → value: true
└── key: "stream_mode"        → value: true
```

### 4.3 구현 방식

```kotlin
// ApiKeyStorage.kt (interface)
interface ApiKeyStorage {
    fun saveApiKey(key: String)
    fun getApiKey(): String?
    fun clearApiKey()
    fun hasApiKey(): Boolean
}

// ApiKeyStorageImpl.kt
// EncryptedSharedPreferences.create() → MasterKey (KeyStore AES-256-GCM)
// → getEncryptedSharedPreferences().edit().putString("api_key", key)
```

### 4.4 UI 플로우

```
설정 화면 → "API 키 입력" 버튼
         → TextField (입력 시 마스킹, visualTransformation = PasswordVisualTransformation)
         → "저장" 클릭 → ApiKeyStorage.saveApiKey()
         → 저장 완료 → MIA 탭 활성화
```

---

## 5. 채팅 로컬 DB 설계

### 5.1 테이블 구조

#### ChatSession (대화 세션)
```sql
CREATE TABLE chat_session (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    title       TEXT NOT NULL,           -- 첫 메시지 기반 자동 생성
    createdAt   INTEGER NOT NULL,
    updatedAt   INTEGER NOT NULL,
    messageCount INTEGER NOT NULL DEFAULT 0
)
```

#### ChatMessage (메시지)
```sql
CREATE TABLE chat_message (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    sessionId   INTEGER NOT NULL,        -- FK → chat_session.id
    role        TEXT NOT NULL,           -- "user" | "assistant" | "tool_result"
    content     TEXT NOT NULL,           -- 텍스트 내용 (JSON 직렬화)
    toolName    TEXT,                    -- tool_use일 때 도구명 (nullable)
    toolCallId  TEXT,                    -- tool_use_id (nullable)
    isError     INTEGER NOT NULL DEFAULT 0,
    createdAt   INTEGER NOT NULL,
    FOREIGN KEY (sessionId) REFERENCES chat_session(id) ON DELETE CASCADE
)
```

### 5.2 Entity 설계

```kotlin
// ChatSession.kt
@Entity(tableName = "chat_session")
data class ChatSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val messageCount: Int = 0
)

// ChatMessage.kt
@Entity(
    tableName = "chat_message",
    foreignKeys = [ForeignKey(
        entity = ChatSession::class,
        parentColumns = ["id"],
        childColumns = ["sessionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("sessionId")]
)
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: Int,
    val role: String,          // "user" | "assistant"
    val content: String,       // 텍스트
    val toolName: String? = null,
    val toolCallId: String? = null,
    val isError: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
```

### 5.3 DAO

```kotlin
@Dao
interface ChatSessionDao {
    @Query("SELECT * FROM chat_session ORDER BY updatedAt DESC")
    fun getAllSessions(): Flow<List<ChatSession>>

    @Insert
    suspend fun insertSession(session: ChatSession): Long

    @Update
    suspend fun updateSession(session: ChatSession)

    @Query("DELETE FROM chat_session WHERE id = :id")
    suspend fun deleteSession(id: Int)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_message WHERE sessionId = :sessionId ORDER BY createdAt ASC")
    fun getMessagesBySession(sessionId: Int): Flow<List<ChatMessage>>

    @Insert
    suspend fun insertMessage(message: ChatMessage): Long

    @Query("DELETE FROM chat_message WHERE sessionId = :sessionId")
    suspend fun clearSession(sessionId: Int)
}
```

### 5.4 Claude API용 메시지 변환

DB에는 user/assistant만 저장. API 요청 시 tool 관련 메시지는 메모리에서 관리.
```
DB 저장 (영구):  user, assistant (텍스트)
메모리 관리 (임시): tool_use, tool_result 블록 (루프 내 임시)
```
→ Claude에게 보내는 히스토리는 DB + 현재 루프 임시 메시지를 합산.

---

## 6. 모듈 구조 설계

### 6.1 신규 모듈 목록

기존 패턴(`datasource:local → data:repository → feature`) 그대로 따름.

```
settings.gradle.kts에 추가:
├── datasource:local:chat:api       ← ChatSession, ChatMessage Entity + DAO interface
├── datasource:local:chat:impl      ← Room DB 확장 (또는 MemoDatabase에 통합)
├── datasource:remote:claude:api    ← Claude API DTO, ClaudeApiService interface (Ktorfit)
├── datasource:remote:claude:impl   ← Ktor Client 설정, ClaudeApiServiceImpl
├── data:repository:chat:api        ← ChatRepository interface
├── data:repository:chat:impl       ← ChatRepositoryImpl
├── feature:mia:api                 ← MiaRoute (Navigation 상수)
└── feature:mia:impl                ← MiaViewModel, MiaScreen, Tool 실행기
```

### 6.2 의존성 방향

```
feature:mia:impl
    ↓ depends on
data:repository:chat:api + data:repository:memo:api   ← 기존 MemoRepository 재활용
    ↓ depends on
datasource:local:chat:api + datasource:remote:claude:api
    ↓ implements
datasource:local:chat:impl + datasource:remote:claude:impl
```

---

## 7. 레이어별 구현 플랜

### Layer 1 — Remote (Claude API)

#### 파일 구조
```
datasource:remote:claude:api
└── ClaudeApiService.kt      ← Ktorfit interface
└── dto/
    ├── MessageRequest.kt    ← API 요청 DTO
    ├── MessageResponse.kt   ← API 응답 DTO
    ├── ContentBlock.kt      ← text / tool_use / tool_result 블록
    └── ToolDefinition.kt    ← 도구 스키마 정의

datasource:remote:claude:impl
└── KtorClientProvider.kt   ← Ktor HttpClient 설정
└── ClaudeApiServiceImpl.kt ← Ktorfit 구현체
└── di/RemoteModule.kt      ← Hilt 바인딩
```

#### Ktorfit 인터페이스 설계
```kotlin
// ClaudeApiService.kt
interface ClaudeApiService {
    @Headers(
        "anthropic-version: 2023-06-01",
        "content-type: application/json"
    )
    @POST("v1/messages")
    suspend fun sendMessage(
        @Header("x-api-key") apiKey: String,
        @Body request: MessageRequest
    ): MessageResponse
}
```

#### Ktor Client 설정 포인트
```kotlin
// KtorClientProvider.kt
HttpClient(OkHttp) {  // 또는 CIO 엔진
    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    install(HttpTimeout) {
        requestTimeoutMillis = 60_000
        connectTimeoutMillis = 10_000
    }
    install(Logging) { level = LogLevel.BODY }   // Debug만
}
```

#### MessageRequest DTO
```kotlin
data class MessageRequest(
    val model: String,           // "claude-haiku-4-5"
    val max_tokens: Int,         // 4096
    val system: String,          // MIA 시스템 프롬프트
    val messages: List<ClaudeMessage>,
    val tools: List<ToolDefinition>? = null
)

data class ClaudeMessage(
    val role: String,            // "user" | "assistant"
    val content: Any             // String 또는 List<ContentBlock> (JSON 다형성)
)
```

---

### Layer 2 — Local (채팅 DB)

#### DB 버전 전략
**기존 MemoDatabase에 통합** (권장) → version 4 → 5로 올리기

```kotlin
@Database(
    entities = [Memo::class, Category::class, ChatSession::class, ChatMessage::class],
    version = 5,
    exportSchema = true
)
abstract class MemoDatabase : RoomDatabase() {
    abstract fun memoDao(): MemoDao
    abstract fun categoryDao(): CategoryDao
    abstract fun chatSessionDao(): ChatSessionDao   // 추가
    abstract fun chatMessageDao(): ChatMessageDao   // 추가
}

// MIGRATION_4_5
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS chat_session ...")
        db.execSQL("CREATE TABLE IF NOT EXISTS chat_message ...")
    }
}
```

---

### Layer 3 — Repository

#### ChatRepository interface
```kotlin
// data:repository:chat:api
interface ChatRepository {
    // 세션 관리
    fun getAllSessions(): Flow<List<ChatSessionUiState>>
    suspend fun createSession(title: String): Int         // 새 세션 ID 반환
    suspend fun deleteSession(id: Int)

    // 메시지 관리
    fun getMessages(sessionId: Int): Flow<List<ChatMessageUiState>>
    suspend fun saveMessage(sessionId: Int, role: String, content: String): Int
    suspend fun clearSession(sessionId: Int)

    // Claude API 호출
    suspend fun sendToMia(
        sessionId: Int,
        userMessage: String,
        apiKey: String
    ): Flow<MiaResponse>  // Flow로 스트리밍 또는 단일 응답 처리
}

// MiaResponse: sealed class
sealed class MiaResponse {
    data class TextChunk(val text: String) : MiaResponse()
    data class ToolRequest(val name: String, val input: Map<String, Any>) : MiaResponse()
    data class Complete(val finalText: String) : MiaResponse()
    data class Error(val message: String) : MiaResponse()
}
```

#### ChatRepositoryImpl 핵심 로직
```kotlin
// Tool Use 루프 처리
suspend fun sendToMia(sessionId, userMessage, apiKey): Flow<MiaResponse> = flow {

    // 1. DB에서 히스토리 로드
    val history = chatMessageDao.getMessagesSync(sessionId)
    val messages = history.toClaudeMessages() + userMessage

    // 2. Tool Use 루프
    var continueLoop = true
    while (continueLoop) {
        val response = claudeApiService.sendMessage(
            apiKey = apiKey,
            request = buildRequest(messages)
        )

        when (response.stop_reason) {
            "end_turn" -> {
                val text = response.content.filterIsInstance<TextBlock>().text
                emit(MiaResponse.Complete(text))
                continueLoop = false
            }
            "tool_use" -> {
                // tool_use 블록 추출 → emit → ViewModel이 실행
                response.content.filterIsInstance<ToolUseBlock>().forEach { block ->
                    emit(MiaResponse.ToolRequest(block.name, block.input))
                }
                // ViewModel에서 tool_result를 받아 messages에 추가 후 재호출
                // → suspend 방식으로 ViewModel과 협력 (Channel 또는 callback)
                continueLoop = false  // ViewModel이 루프 제어
            }
        }
    }
}
```

> **Tool Use 루프 제어 주체:** Repository는 단일 API 호출만 담당.
> 루프(재호출)는 **ViewModel**이 제어한다.

---

### Layer 4 — ViewModel (MiaViewModel)

#### 상태 설계
```kotlin
// MiaUiState.kt
data class MiaUiState(
    val messages: List<ChatBubble> = emptyList(),
    val isLoading: Boolean = false,
    val isTyping: Boolean = false,    // MIA 응답 중
    val error: String? = null,
    val hasApiKey: Boolean = false
)

sealed class ChatBubble {
    data class UserBubble(val text: String, val timestamp: Long) : ChatBubble()
    data class AiBubble(val text: String, val timestamp: Long, val isStreaming: Boolean = false) : ChatBubble()
    data class ToolBubble(val toolName: String, val status: ToolStatus) : ChatBubble()
}

enum class ToolStatus { RUNNING, DONE, ERROR }
```

#### ViewModel 핵심 로직
```kotlin
@HiltViewModel
class MiaViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val memoRepository: MemoRepository,    // 기존 Repository 재활용!
    private val apiKeyStorage: ApiKeyStorage
) : ViewModel() {

    fun sendMessage(userText: String) {
        viewModelScope.launch {
            // 1. 사용자 메시지 UI에 추가
            // 2. chatRepository.sendToMia() 호출
            // 3. MiaResponse 처리:
            //    - TextChunk → 스트리밍 텍스트 업데이트
            //    - ToolRequest → executeTool() 실행 → 결과를 API에 재전송
            //    - Complete → isTyping = false
            //    - Error → error 상태 업데이트
        }
    }

    // Tool 실행기 (앱의 실제 기능 실행)
    private suspend fun executeTool(name: String, input: Map<String, Any>): String {
        return when (name) {
            "get_all_memos" -> {
                val memos = memoRepository.getMemosSync()
                memos.joinToString("\n") { "[${it.id}] ${it.name}: ${it.content.take(50)}" }
            }
            "create_memo" -> {
                val memo = Memo(
                    name = input["name"] as String,
                    categoryId = (input["categoryId"] as Double).toInt(),
                    content = input["content"] as String
                )
                memoRepository.addMemo(memo)
                "메모 '${memo.name}' 가 생성되었습니다."
            }
            "delete_memo" -> {
                val id = (input["id"] as Double).toInt()
                memoRepository.deleteMemo(id)
                "메모 ID $id 가 삭제되었습니다."
            }
            "search_memos" -> {
                val query = input["query"] as String
                val results = memoRepository.searchMemos(query)
                if (results.isEmpty()) "검색 결과가 없습니다."
                else results.joinToString("\n") { "[${it.id}] ${it.name}" }
            }
            else -> "알 수 없는 도구: $name"
        }
    }
}
```

---

### Layer 5 — UI (MiaScreen)

#### 화면 구성 요소
```
MiaScreen
├── ChatHistoryList          ← LazyColumn (메시지 버블 목록)
│   ├── UserBubble           ← 오른쪽 정렬, 앱 primary 색상
│   ├── AiBubble             ← 왼쪽 정렬, MIA 아바타 포함
│   │   └── MarkdownText     ← Markdown 렌더링 (compose-markdown 라이브러리)
│   └── ToolStatusChip       ← "메모 생성 중..." 인디케이터
├── TypingIndicator          ← MIA 응답 대기 중 애니메이션 (점 3개)
└── InputArea
    ├── TextField            ← 메시지 입력 (shadcn Input 컴포넌트 활용 가능)
    └── SendButton           ← 전송 버튼
```

#### Navigation 통합
```kotlin
// feature:mia:api/MiaRoute.kt
object MiaRoute {
    const val MIA = "mia"
}

// MainActivity.kt NavHost에 추가
composable(MiaRoute.MIA) {
    MiaScreen()
}

// BottomNavItem에 MIA 탭 추가 (메모 목록 / MIA / 설정)
```

#### MIA 시스템 프롬프트
```
You are MIA (Memo Intelligence Assistant), a friendly AI built into the Memozy app.
You help users manage their memos through natural conversation in Korean.

You have access to the following tools to manage memos:
- get_all_memos: View all memos
- search_memos: Search memos by keyword
- create_memo: Create a new memo
- update_memo: Edit an existing memo
- delete_memo: Delete a memo
- get_categories: List available categories

Always respond in Korean unless the user writes in another language.
Be concise and helpful. When using tools, briefly explain what you're doing.
```

---

## 8. API 요청/응답 전체 예시 (Tool Use 흐름)

### 1단계: 사용자 → 앱 → Claude
```json
POST /v1/messages
{
  "model": "claude-haiku-4-5",
  "max_tokens": 4096,
  "system": "You are MIA...",
  "messages": [
    {"role": "user", "content": "오늘 할 일 메모 만들어줘. 내용은 '보고서 작성'이야"}
  ],
  "tools": [{ "name": "create_memo", ... }]
}
```

### 2단계: Claude → 앱 (Tool Use 요청)
```json
{
  "stop_reason": "tool_use",
  "content": [
    {"type": "text", "text": "네, 할 일 메모를 만들어드릴게요!"},
    {
      "type": "tool_use",
      "id": "toolu_abc123",
      "name": "create_memo",
      "input": {"name": "보고서 작성", "content": "보고서 작성", "categoryId": 4}
    }
  ]
}
```

### 3단계: 앱이 Tool 실행 → Claude에 결과 전송
```json
{
  "messages": [
    {"role": "user", "content": "오늘 할 일 메모 만들어줘..."},
    {
      "role": "assistant",
      "content": [
        {"type": "text", "text": "네, 할 일 메모를 만들어드릴게요!"},
        {"type": "tool_use", "id": "toolu_abc123", "name": "create_memo", "input": {...}}
      ]
    },
    {
      "role": "user",
      "content": [
        {"type": "tool_result", "tool_use_id": "toolu_abc123", "content": "메모 '보고서 작성'이 생성되었습니다."}
      ]
    }
  ]
}
```

### 4단계: Claude → 앱 (최종 응답)
```json
{
  "stop_reason": "end_turn",
  "content": [
    {"type": "text", "text": "✅ '보고서 작성' 메모를 할 일 카테고리에 생성했어요!"}
  ]
}
```

---

## 9. 비용 고려사항

| 모델 | Input | Output | 추천 용도 |
|------|-------|--------|-----------|
| claude-haiku-4-5 | $1/1M | $5/1M | 기본 (빠름, 저렴) |
| claude-sonnet-4-6 | $3/1M | $15/1M | 고급 모드 (선택) |

**비용 절감 방법:**
- 시스템 프롬프트 + 도구 정의에 Prompt Caching 적용 (`cache_control: ephemeral`)
- 대화 히스토리 길이 제한 (최근 20개 메시지만 포함)
- Haiku를 기본값으로, 설정에서 Sonnet 선택 가능하게

---

## 10. 구현 순서 (단계별 로드맵)

```
Phase 1: 기반 인프라
  ├── EncryptedSharedPreferences ApiKeyStorage 구현
  ├── DataStore 설정 저장소 구현
  └── 설정 화면에 API 키 입력 UI 추가

Phase 2: Claude API 연동
  ├── Ktor Client + Ktorfit 의존성 추가
  ├── DTO 클래스 설계 (Request/Response)
  └── ClaudeApiService 인터페이스 + 구현체

Phase 3: 채팅 DB
  ├── ChatSession, ChatMessage Entity 추가
  ├── MemoDatabase 버전 4→5 마이그레이션
  └── ChatSessionDao, ChatMessageDao 구현

Phase 4: Repository 레이어
  ├── ChatRepository interface + impl
  ├── Tool Use 루프 로직 구현
  └── MemoRepository에 searchMemos, getMemosSync 추가

Phase 5: ViewModel + UI
  ├── MiaViewModel 구현 (Tool 실행기 포함)
  ├── MiaScreen UI 구현
  ├── Navigation 통합
  └── BottomNav에 MIA 탭 추가
```

---

## 11. 체크리스트 (착수 전 확인)

- [ ] `libs.versions.toml`에 Ktor, Ktorfit 버전 추가 필요
- [ ] `memozy.room` convention 플러그인 확인 (chat 모듈에도 적용)
- [ ] `memozy.hilt` convention 플러그인 모든 신규 모듈에 적용
- [ ] MemoRepository에 `getMemosSync()` (Flow 아닌 suspend) 추가 필요
- [ ] MemoRepository에 `searchMemos(query)` 추가 필요
- [ ] DB Migration 4→5 테스트 필수
- [ ] API 키를 절대 로그에 출력하지 않도록 주의
- [ ] ProGuard 규칙에 Ktor/Ktorfit 클래스 보호 추가

---

*이 문서는 기획/설계 전용입니다. 실제 구현 시 각 Phase별로 세부 사항 조정 필요.*
