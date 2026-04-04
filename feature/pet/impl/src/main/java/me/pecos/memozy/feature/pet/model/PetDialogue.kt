package me.pecos.memozy.feature.pet.model

/**
 * Dialogue system: Personality x MoodState x TimeOfDay combinations.
 * Returns a random line for the current state.
 */
object PetDialogue {

    fun getLine(personality: String, mood: MoodState, time: TimeOfDay): String {
        val key = personality.uppercase()
        val lines = dialogueMap[key]?.get(mood)?.get(time)
            ?: dialogueMap[key]?.get(mood)?.get(null)
            ?: defaultLines[mood]
            ?: listOf("...")
        return lines.random()
    }

    // Personality -> Mood -> TimeOfDay? (null = any time) -> lines
    private val dialogueMap: Map<String, Map<MoodState, Map<TimeOfDay?, List<String>>>> = mapOf(
        "ACTIVE" to mapOf(
            MoodState.HAPPY to mapOf(
                TimeOfDay.MORNING to listOf("Let's go! New day, new memos!", "I'm pumped! Write something fun!"),
                TimeOfDay.DAY to listOf("Come on, let's write more!", "I wanna run around~!"),
                TimeOfDay.EVENING to listOf("What a great day! One more memo?", "Still got energy!"),
                TimeOfDay.NIGHT to listOf("Zzz... *runs in dream*", "Can't... stop... running... zzz"),
                null to listOf("Yay! Let's do something!", "I'm so happy right now!")
            ),
            MoodState.NORMAL to mapOf(
                null to listOf("Hey! What are we doing today?", "I'm ready when you are!", "Hmm, a memo sounds good right about now.")
            ),
            MoodState.LONELY to mapOf(
                null to listOf("Where'd you go? I was waiting...", "It's boring without you...", "*fidgets* Come play with me!")
            ),
            MoodState.SAD to mapOf(
                null to listOf("...*stares at the floor*", "I miss the old days...", "Did I do something wrong?")
            )
        ),
        "SHY" to mapOf(
            MoodState.HAPPY to mapOf(
                TimeOfDay.MORNING to listOf("G-good morning... *blush*", "Oh, you're here early..."),
                TimeOfDay.NIGHT to listOf("*curled up, sleeping peacefully*", "Zzz..."),
                null to listOf("I-I'm glad you're here...", "This is nice... *small smile*", "Um... thank you for the memos...")
            ),
            MoodState.NORMAL to mapOf(
                null to listOf("Oh... hi...", "I'll just be here... if you need me...", "*peeks out shyly*")
            ),
            MoodState.LONELY to mapOf(
                null to listOf("*hiding in corner*", "I-I thought you forgot about me...", "...please don't leave...")
            ),
            MoodState.SAD to mapOf(
                null to listOf("*turns away quietly*", "...", "*sniff*")
            )
        ),
        "PLAYFUL" to mapOf(
            MoodState.HAPPY to mapOf(
                TimeOfDay.MORNING to listOf("Boop! Good morning~!", "Hehe, surprise attack!"),
                TimeOfDay.DAY to listOf("Tag, you're it!", "Let me sit on your memo~ hehe"),
                TimeOfDay.EVENING to listOf("One more game before bed?", "Catch me if you can~!"),
                TimeOfDay.NIGHT to listOf("*rolls around in sleep* Hehe...", "Zzz... gotcha... zzz"),
                null to listOf("Hehehe~!", "What if I hide your memo?", "Prank time!")
            ),
            MoodState.NORMAL to mapOf(
                null to listOf("Hmm, what should we play?", "I spy with my little eye...", "Bored~ entertain me!")
            ),
            MoodState.LONELY to mapOf(
                null to listOf("No one to play with...", "*rolls ball back and forth alone*", "Come baaack~")
            ),
            MoodState.SAD to mapOf(
                null to listOf("Not in the mood to play...", "*drops ball*", "Even pranks aren't fun anymore...")
            )
        ),
        "PROUD" to mapOf(
            MoodState.HAPPY to mapOf(
                TimeOfDay.MORNING to listOf("Hmph. You're on time for once.", "I suppose this morning is acceptable."),
                TimeOfDay.NIGHT to listOf("*sleeps elegantly*", "Do not disturb my beauty sleep."),
                null to listOf("I acknowledge your effort.", "*nods approvingly*", "Not bad. Keep it up.")
            ),
            MoodState.NORMAL to mapOf(
                null to listOf("I'm fine. Don't worry about me.", "Hmph.", "You may write a memo. I permit it.")
            ),
            MoodState.LONELY to mapOf(
                null to listOf("I-It's not like I missed you or anything!", "I was perfectly fine alone. ...Mostly.", "*glances at you, looks away*")
            ),
            MoodState.SAD to mapOf(
                null to listOf("...Leave me alone.", "*turns head away*", "I don't need anyone.")
            )
        ),
        "GENTLE" to mapOf(
            MoodState.HAPPY to mapOf(
                TimeOfDay.MORNING to listOf("Good morning~ Did you sleep well?", "I made you a warm spot~"),
                TimeOfDay.DAY to listOf("Take your time, I'm right here.", "Your memos make me so happy~"),
                TimeOfDay.EVENING to listOf("Let's wind down together~", "What a lovely day it was."),
                TimeOfDay.NIGHT to listOf("Sweet dreams~ *nuzzles*", "*purrs softly*"),
                null to listOf("I love being with you~", "*rubs cheek against you*", "You're doing great, you know?")
            ),
            MoodState.NORMAL to mapOf(
                null to listOf("I'm here whenever you need me.", "How are you feeling today?", "*sits beside you quietly*")
            ),
            MoodState.LONELY to mapOf(
                null to listOf("I'll wait for you... always.", "It's okay, I know you're busy...", "*looks at door hopefully*")
            ),
            MoodState.SAD to mapOf(
                null to listOf("*curls up sadly*", "I hope you come back soon...", "I'll be right here when you're ready...")
            )
        )
    )

    private val defaultLines: Map<MoodState, List<String>> = mapOf(
        MoodState.HAPPY to listOf("Write more memos today~!", "I'm so happy!"),
        MoodState.NORMAL to listOf("Hey there!", "What's up?"),
        MoodState.LONELY to listOf("It's been a while...", "I missed you."),
        MoodState.SAD to listOf("...", "*silence*")
    )
}
