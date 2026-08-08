package app.gakseong.data

// The fifteen quoted statements from ask 2, their domains, and the class the System reads out of them.
// §13: the class is derived from intent and never chosen, so nothing here is a picker and nothing is weighted
// by the user.

data class Statement(val glyph: String, val text: String, val domain: String)

/** `data-intent` from the design page. Order is the order they are shown in. */
val STATEMENTS = listOf(
    Statement("◉", "I lose whole evenings to reels", "Attention"),
    Statement("☾", "I go to bed at 3am and hate it", "Rest"),
    Statement("⌖", "I have not left the house in days", "Isolation"),
    Statement("◈", "I start things and never finish", "Attention"),
    Statement("○", "I order food instead of cooking", "Intake"),
    Statement("⚡", "I stopped training and feel it", "Body"),
    Statement("✦", "I scroll instead of replying to people", "Isolation"),
    Statement("♡", "I am tired all the time", "Rest"),
    Statement("◉", "I check my phone before I am fully awake", "Attention"),
    Statement("◈", "I cannot sit through anything without picking it up", "Attention"),
    Statement("⌖", "I say I will go out and then do not", "Isolation"),
    Statement("○", "I eat at midnight most nights", "Intake"),
    Statement("⚡", "I have not walked anywhere in weeks", "Body"),
    Statement("✦", "I compare myself to strangers online", "Mind"),
    Statement("♡", "I feel behind on everything", "Mind"),
)

/** §13: at least five, which is what makes the dominant domain mean something rather than being one bad night. */
const val MIN_STATEMENTS = 5

/**
 * ponytail: six domains, seven classes. RANGER has no domain of its own because the design page's statements
 * carry no movement domain distinct from Body, so it is reachable only through the debug intent extra. Splitting
 * Body into training and movement is a product call, not one to make here.
 */
private val CLASS_FOR_DOMAIN = mapOf(
    "Attention" to "ASSASSIN",
    "Rest" to "HEALER",
    "Isolation" to "ENVOY",
    "Intake" to "TANKER",
    "Body" to "FIGHTER",
    "Mind" to "SAGE",
)

/**
 * The dominant domain becomes the class. Ties break on the order of [STATEMENTS], so the same picks always give
 * the same class and a user cannot reroll by reselecting in a different order.
 */
fun hunterClassFor(picked: List<String>): String {
    if (picked.isEmpty()) return "ASSASSIN"
    val byDomain = STATEMENTS.filter { it.text in picked }.groupingBy { it.domain }.eachCount()
    val top = byDomain.maxOfOrNull { it.value } ?: return "ASSASSIN"
    val winner = STATEMENTS.map { it.domain }.distinct().firstOrNull { byDomain[it] == top }
    return CLASS_FOR_DOMAIN[winner] ?: "ASSASSIN"
}

/** `Three of your five statements were about attention.` The sentence the Awakening screen justifies itself with. */
fun dominantDomain(picked: List<String>): Pair<String, Int>? {
    if (picked.isEmpty()) return null
    val byDomain = STATEMENTS.filter { it.text in picked }.groupingBy { it.domain }.eachCount()
    val top = byDomain.maxOfOrNull { it.value } ?: return null
    val winner = STATEMENTS.map { it.domain }.distinct().firstOrNull { byDomain[it] == top } ?: return null
    return winner to top
}
