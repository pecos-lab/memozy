package me.pecos.memozy.presentation.screen.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.pecos.memozy.data.datasource.remote.ai.AIApiService
import me.pecos.memozy.data.repository.MemoRepository
import org.json.JSONArray
import javax.inject.Inject

sealed class QuizState {
    data object Loading : QuizState()
    data class Ready(val questions: List<QuizQuestion>) : QuizState()
    data class Error(val message: String) : QuizState()
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MemoRepository,
    private val aiApiService: AIApiService
) : ViewModel() {

    private val memoId: Int = savedStateHandle.get<String>("memoId")?.toIntOrNull() ?: -1

    private val _quizState = MutableStateFlow<QuizState>(QuizState.Loading)
    val quizState: StateFlow<QuizState> = _quizState

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex

    private val _selectedAnswer = MutableStateFlow(-1)
    val selectedAnswer: StateFlow<Int> = _selectedAnswer

    private val _correctCount = MutableStateFlow(0)
    val correctCount: StateFlow<Int> = _correctCount

    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished

    val memoTitle = MutableStateFlow("")

    init {
        generateQuiz()
    }

    private fun generateQuiz() {
        viewModelScope.launch {
            _quizState.value = QuizState.Loading
            try {
                val memo = repository.getMemoById(memoId)
                    ?: throw Exception("메모를 찾을 수 없습니다")

                memoTitle.value = memo.name
                val content = "${memo.name}\n\n${memo.content}".take(3000)

                val prompt = """다음 메모 내용을 바탕으로 5개의 객관식 퀴즈를 만들어줘.

규칙:
- 메모 내용에서 핵심 개념을 테스트하는 문제를 만들어
- 각 문제는 4개의 선택지를 가져
- 정답은 하나만 있어야 해
- 해설은 왜 그 답이 맞는지 간결하게 설명해
- 반드시 아래 JSON 배열 형식으로만 출력하고 다른 텍스트는 출력하지 마

출력 형식:
[{"question":"문제","options":["A","B","C","D"],"correctIndex":0,"explanation":"해설"}]

메모 내용:
$content"""

                val response = aiApiService.generateContent(prompt)
                val questions = parseQuizResponse(response)

                if (questions.isEmpty()) {
                    _quizState.value = QuizState.Error("퀴즈를 생성할 수 없습니다")
                } else {
                    _quizState.value = QuizState.Ready(questions)
                }
            } catch (e: Exception) {
                _quizState.value = QuizState.Error(e.message ?: "퀴즈 생성 실패")
            }
        }
    }

    fun selectAnswer(index: Int) {
        if (_selectedAnswer.value >= 0) return // already answered
        _selectedAnswer.value = index

        val state = _quizState.value
        if (state is QuizState.Ready) {
            val question = state.questions[_currentIndex.value]
            if (index == question.correctIndex) {
                _correctCount.value++
            }
        }
    }

    fun nextQuestion() {
        val state = _quizState.value
        if (state is QuizState.Ready) {
            val nextIdx = _currentIndex.value + 1
            if (nextIdx >= state.questions.size) {
                _isFinished.value = true
            } else {
                _currentIndex.value = nextIdx
                _selectedAnswer.value = -1
            }
        }
    }

    fun retry() {
        _currentIndex.value = 0
        _selectedAnswer.value = -1
        _correctCount.value = 0
        _isFinished.value = false
        generateQuiz()
    }

    private fun parseQuizResponse(response: String): List<QuizQuestion> {
        return try {
            // JSON 배열 부분만 추출
            val jsonStr = response.trim().let { raw ->
                val start = raw.indexOf('[')
                val end = raw.lastIndexOf(']')
                if (start >= 0 && end > start) raw.substring(start, end + 1) else raw
            }
            val array = JSONArray(jsonStr)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                val options = obj.getJSONArray("options")
                QuizQuestion(
                    question = obj.getString("question"),
                    options = (0 until options.length()).map { options.getString(it) },
                    correctIndex = obj.getInt("correctIndex"),
                    explanation = obj.optString("explanation", "")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
