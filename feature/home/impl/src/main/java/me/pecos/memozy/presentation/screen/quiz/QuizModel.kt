package me.pecos.memozy.presentation.screen.quiz

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)
