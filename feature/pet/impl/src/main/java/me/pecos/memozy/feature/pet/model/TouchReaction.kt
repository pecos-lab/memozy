package me.pecos.memozy.feature.pet.model

/**
 * Touch reaction system — different responses based on condition level.
 * LOW: minimal response, sulky
 * MEDIUM: warm smile, friendly
 * HIGH: ecstatic, praise shower
 */
object TouchReaction {

    fun getReaction(personality: String, condition: Condition): ReactionData {
        val key = personality.uppercase()
        return reactions[key]?.get(condition) ?: defaultReactions[condition]!!
    }

    data class ReactionData(
        val emoji: String,
        val dialogue: List<String>,
        val bounceScale: Float,    // 1.0 = no bounce, 1.3 = big bounce
        val showHearts: Boolean
    )

    private val reactions: Map<String, Map<Condition, ReactionData>> = mapOf(
        "ACTIVE" to mapOf(
            Condition.LOW to ReactionData(
                emoji = "\uD83D\uDE12",
                dialogue = listOf("...", "*yawn*", "Meh."),
                bounceScale = 1.02f,
                showHearts = false
            ),
            Condition.MEDIUM to ReactionData(
                emoji = "\uD83D\uDE0A",
                dialogue = listOf("Oh, hey!", "Wanna play?", "That tickles~"),
                bounceScale = 1.1f,
                showHearts = false
            ),
            Condition.HIGH to ReactionData(
                emoji = "\uD83D\uDE06",
                dialogue = listOf("YOU'RE THE BEST!!", "I LOVE YOU!!", "LET'S GOOO!", "You're amazing!!!"),
                bounceScale = 1.25f,
                showHearts = true
            )
        ),
        "SHY" to mapOf(
            Condition.LOW to ReactionData(
                emoji = "\uD83D\uDE1F",
                dialogue = listOf("*flinch*", "...", "*looks away*"),
                bounceScale = 1.01f,
                showHearts = false
            ),
            Condition.MEDIUM to ReactionData(
                emoji = "\u263A\uFE0F",
                dialogue = listOf("O-oh... hi...", "*small smile*", "That's... nice"),
                bounceScale = 1.08f,
                showHearts = false
            ),
            Condition.HIGH to ReactionData(
                emoji = "\uD83E\uDD7A",
                dialogue = listOf("I-I really like you...!", "*hugs tight*", "D-don't stop...!", "You make me so happy...!"),
                bounceScale = 1.18f,
                showHearts = true
            )
        ),
        "PLAYFUL" to mapOf(
            Condition.LOW to ReactionData(
                emoji = "\uD83D\uDE44",
                dialogue = listOf("Not in the mood...", "*rolls over*", "Whatever."),
                bounceScale = 1.02f,
                showHearts = false
            ),
            Condition.MEDIUM to ReactionData(
                emoji = "\uD83D\uDE1C",
                dialogue = listOf("Hehe, that tickles!", "Again again!", "Poke~"),
                bounceScale = 1.12f,
                showHearts = false
            ),
            Condition.HIGH to ReactionData(
                emoji = "\uD83E\uDD29",
                dialogue = listOf("HAHAHA YES!!", "You're SO fun!!", "Best human EVER!!", "MORE MORE MORE!"),
                bounceScale = 1.3f,
                showHearts = true
            )
        ),
        "PROUD" to mapOf(
            Condition.LOW to ReactionData(
                emoji = "\uD83D\uDE12",
                dialogue = listOf("Don't touch me.", "Hmph.", "*swats hand away*"),
                bounceScale = 1.0f,
                showHearts = false
            ),
            Condition.MEDIUM to ReactionData(
                emoji = "\uD83D\uDE0F",
                dialogue = listOf("I'll allow it.", "Not bad.", "*slight nod*"),
                bounceScale = 1.06f,
                showHearts = false
            ),
            Condition.HIGH to ReactionData(
                emoji = "\uD83D\uDE0A",
                dialogue = listOf("...I suppose you've earned this.", "F-fine, I like it!", "You're... acceptable.", "Don't tell anyone I smiled."),
                bounceScale = 1.15f,
                showHearts = true
            )
        ),
        "GENTLE" to mapOf(
            Condition.LOW to ReactionData(
                emoji = "\uD83D\uDE22",
                dialogue = listOf("*weak smile*", "I'm okay...", "*nuzzles softly*"),
                bounceScale = 1.03f,
                showHearts = false
            ),
            Condition.MEDIUM to ReactionData(
                emoji = "\u2764\uFE0F",
                dialogue = listOf("That's so warm~", "I love when you do that", "*purrs*"),
                bounceScale = 1.1f,
                showHearts = false
            ),
            Condition.HIGH to ReactionData(
                emoji = "\uD83E\uDD70",
                dialogue = listOf("You're the kindest person ever~", "I'm so lucky to have you!", "*cuddles*", "Never let go~!"),
                bounceScale = 1.2f,
                showHearts = true
            )
        )
    )

    private val defaultReactions: Map<Condition, ReactionData> = mapOf(
        Condition.LOW to ReactionData("\uD83D\uDE10", listOf("..."), 1.0f, false),
        Condition.MEDIUM to ReactionData("\uD83D\uDE0A", listOf("Hey~"), 1.1f, false),
        Condition.HIGH to ReactionData("\uD83D\uDE06", listOf("Yay!!"), 1.25f, true)
    )
}
