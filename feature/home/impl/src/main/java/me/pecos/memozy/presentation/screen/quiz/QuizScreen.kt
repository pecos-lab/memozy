package me.pecos.memozy.presentation.screen.quiz

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wanted.android.wanted.design.actions.button.WantedButton
import com.wanted.android.wanted.design.actions.button.config.WantedButtonDefaults
import com.wanted.android.wanted.design.util.ButtonType
import com.wanted.android.wanted.design.util.ButtonVariant
import me.pecos.memozy.feature.core.resource.R
import me.pecos.memozy.presentation.theme.LocalAppColors

@Composable
fun QuizScreen(
    viewModel: QuizViewModel,
    onBack: () -> Unit
) {
    val quizState by viewModel.quizState.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val selectedAnswer by viewModel.selectedAnswer.collectAsState()
    val correctCount by viewModel.correctCount.collectAsState()
    val isFinished by viewModel.isFinished.collectAsState()
    val memoTitle by viewModel.memoTitle.collectAsState()
    val colors = LocalAppColors.current

    Scaffold(containerColor = colors.screenBackground) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = colors.topbarTitle)
                }
                Text(
                    text = stringResource(R.string.quiz_title),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.topbarTitle
                )
            }

            if (memoTitle.isNotBlank()) {
                Text(
                    text = memoTitle,
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(start = 16.dp, bottom = 12.dp),
                    maxLines = 1
                )
            }

            when (val state = quizState) {
                is QuizState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = colors.chipText)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(stringResource(R.string.quiz_generating), color = colors.textSecondary, fontSize = 14.sp)
                        }
                    }
                }

                is QuizState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.message, color = colors.textSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(16.dp))
                            WantedButton(
                                text = stringResource(R.string.quiz_retry),
                                buttonDefault = WantedButtonDefaults.getDefault(type = ButtonType.ASSISTIVE, variant = ButtonVariant.OUTLINED)
                                    .copy(contentColor = colors.chipText),
                                onClick = { viewModel.retry() }
                            )
                        }
                    }
                }

                is QuizState.Ready -> {
                    if (isFinished) {
                        // 결과 화면
                        QuizResultScreen(
                            totalCount = state.questions.size,
                            correctCount = correctCount,
                            onRetry = { viewModel.retry() },
                            onBack = onBack,
                            colors = colors
                        )
                    } else {
                        // 퀴즈 진행
                        val question = state.questions[currentIndex]
                        val totalCount = state.questions.size

                        // Progress
                        LinearProgressIndicator(
                            progress = { (currentIndex + 1).toFloat() / totalCount },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(4.dp)),
                            color = colors.chipText,
                            trackColor = colors.chipBackground,
                        )
                        Text(
                            text = "${currentIndex + 1} / $totalCount",
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp)
                        ) {
                            // 문제
                            Text(
                                text = question.question,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textTitle,
                                lineHeight = 26.sp
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // 선택지
                            question.options.forEachIndexed { idx, option ->
                                val isSelected = selectedAnswer == idx
                                val isCorrect = idx == question.correctIndex
                                val hasAnswered = selectedAnswer >= 0

                                val bgColor by animateColorAsState(
                                    when {
                                        hasAnswered && isCorrect -> Color(0xFF4CAF50).copy(alpha = 0.12f)
                                        hasAnswered && isSelected && !isCorrect -> Color(0xFFE24B4A).copy(alpha = 0.12f)
                                        else -> colors.cardBackground
                                    }, label = "optionBg"
                                )
                                val borderColor by animateColorAsState(
                                    when {
                                        hasAnswered && isCorrect -> Color(0xFF4CAF50)
                                        hasAnswered && isSelected && !isCorrect -> Color(0xFFE24B4A)
                                        isSelected -> colors.chipText
                                        else -> colors.cardBorder
                                    }, label = "optionBorder"
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(bgColor)
                                        .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
                                        .clickable(enabled = !hasAnswered) { viewModel.selectAnswer(idx) }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = option,
                                        fontSize = 15.sp,
                                        color = colors.textTitle,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (hasAnswered && isCorrect) {
                                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(22.dp))
                                    } else if (hasAnswered && isSelected && !isCorrect) {
                                        Icon(Icons.Default.Cancel, null, tint = Color(0xFFE24B4A), modifier = Modifier.size(22.dp))
                                    }
                                }
                            }

                            // 해설
                            if (selectedAnswer >= 0 && question.explanation.isNotBlank()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.chipBackground)
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.quiz_explanation),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.chipText
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = question.explanation,
                                        fontSize = 14.sp,
                                        color = colors.textBody,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }

                        // 다음 버튼
                        if (selectedAnswer >= 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            WantedButton(
                                text = if (currentIndex + 1 >= state.questions.size) stringResource(R.string.quiz_show_result)
                                       else stringResource(R.string.quiz_next),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                buttonDefault = WantedButtonDefaults.getDefault(type = ButtonType.ASSISTIVE, variant = ButtonVariant.OUTLINED)
                                    .copy(contentColor = colors.chipText),
                                onClick = { viewModel.nextQuestion() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizResultScreen(
    totalCount: Int,
    correctCount: Int,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    colors: me.pecos.memozy.presentation.theme.AppColors
) {
    val score = if (totalCount > 0) (correctCount * 100) / totalCount else 0

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = if (score >= 80) "🎉" else if (score >= 50) "💪" else "📚",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "${score}%",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    score >= 80 -> Color(0xFF4CAF50)
                    score >= 50 -> Color(0xFFFFA726)
                    else -> Color(0xFFE24B4A)
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$correctCount / $totalCount",
                fontSize = 18.sp,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when {
                    score >= 80 -> "훌륭해요! 잘 기억하고 있어요"
                    score >= 50 -> "좋아요! 조금 더 복습해볼까요?"
                    else -> "다시 한번 메모를 읽어보세요!"
                },
                fontSize = 14.sp,
                color = colors.textBody,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            WantedButton(
                text = stringResource(R.string.quiz_retry),
                modifier = Modifier.fillMaxWidth(),
                buttonDefault = WantedButtonDefaults.getDefault(type = ButtonType.ASSISTIVE, variant = ButtonVariant.OUTLINED)
                    .copy(contentColor = colors.chipText),
                onClick = onRetry
            )
            Spacer(modifier = Modifier.height(12.dp))
            WantedButton(
                text = stringResource(R.string.quiz_back),
                modifier = Modifier.fillMaxWidth(),
                buttonDefault = WantedButtonDefaults.getDefault(type = ButtonType.ASSISTIVE, variant = ButtonVariant.OUTLINED)
                    .copy(contentColor = colors.textTitle),
                onClick = onBack
            )
        }
    }
}
