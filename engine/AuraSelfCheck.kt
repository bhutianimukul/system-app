package gakseong.engine

// Phase 01's one test file. Plain asserts, no framework, no fixtures:
// `kotlinc engine/*.kt -include-runtime -d engine.jar && java -cp engine.jar gakseong.engine.AuraSelfCheckKt`
//
// Everything here is a decision from DECISIONS.md §4, §5 and §6. When a check fails, the engine is wrong
// unless the decision changed, in which case the decision changes first and this file follows.

private var failures = 0

private fun expect(label: String, expected: Any?, actual: Any?) {
    if (expected == actual) {
        println("ok    $label")
    } else {
        failures++
        println("FAIL  $label")
        println("      expected: $expected")
        println("      actual:   $actual")
    }
}

private fun held(state: HunterState, aura: Int, bonus: Int = 0) = settleDay(state, aura, bonus)

private fun fresh(ordinal: Int = 0, streak: Int = 0, shields: Int = 0, daysAtTier: Int = 0, misses: Int = 0) =
    HunterState(Rank(ordinal), daysHeldAtTier = daysAtTier, streak = streak, shields = shields, consecutiveMisses = misses)

fun main() {
    // ── the ladder itself ───────────────────────────────────────────────────────
    expect("E-III is the floor", "E · III", Rank(0).label)
    expect("tiers descend III → II → I inside a letter", "E · I", Rank(2).label)
    expect("the next letter starts at tier III", "D · III", Rank(3).label)
    expect("S-I is the ceiling", "S · I", Rank(17).label)
    expect("D is Sentinel", "Sentinel", Rank(3).title)
    expect("S is Unbound", "Unbound", Rank(17).title)
    expect("eighteen promotion moments exist", 18, Rank.ALL.size)

    // ── the band (§4) ──────────────────────────────────────────────────────────
    expect("E-III band", Band(200, 600), bandFor(Rank(0)))
    expect("D-III band matches the Home screen", Band(500, 1200), bandFor(Rank(3)))
    expect("the band rises every tier", true, bandFor(Rank(4)).threshold > bandFor(Rank(3)).threshold)

    // ── provability ceilings (§4) ──────────────────────────────────────────────
    expect("sensor-proven pays full", 450, award(450, Provability.SENSOR))
    expect("app-initiated pays medium", 270, award(450, Provability.APP_INITIATED))
    expect("declared pays about a third", 158, award(450, Provability.DECLARED))
    expect("declared never out-pays sensor", true, award(450, Provability.DECLARED) < award(450, Provability.SENSOR))

    // ── where a day lands ─────────────────────────────────────────────────────
    val dIII = fresh(3)
    expect("below the threshold is penalty territory", DayOutcome.BELOW_THRESHOLD, held(dIII, 499).outcome)
    expect("inside the band Rank counts", DayOutcome.IN_BAND, held(dIII, 640).outcome)
    expect("above the cap is Level only", DayOutcome.ABOVE_CAP, held(dIII, 1400).outcome)
    expect("rank credit is clamped to the cap", 1200, held(dIII, 1400).rankCredit)
    expect("overflow is what the cap refused", 200, held(dIII, 1400).overflow)
    expect("a day above the cap is still a day held", 1, held(dIII, 1400).state.streak)
    expect("the bonus raises today's ceiling", 1350, held(dIII, 1400, bonus = 150).rankCredit)

    // ── streak, shields (§5) ──────────────────────────────────────────────────
    expect("a held day advances the streak", 5, held(fresh(3, streak = 4), 640).state.streak)
    expect("a held day advances time at this tier", 3, held(fresh(3, daysAtTier = 2), 640).state.daysHeldAtTier)
    expect("seven consecutive days grant a shield", 1, held(fresh(3, streak = 6), 640).state.shields)
    expect("shields cap at three", 3, held(fresh(3, streak = 13, shields = 3), 640).state.shields)

    val absorbed = held(fresh(3, streak = 9, shields = 2), 100)
    expect("a shield absorbs the miss", true, absorbed.shieldSpent)
    expect("an absorbed miss keeps the streak", 9, absorbed.state.streak)
    expect("an absorbed miss carries no penalty", null, absorbed.penalty)
    expect("an absorbed miss spends the shield", 1, absorbed.state.shields)

    // ── penalties sequence, never stack (§6) ─────────────────────────────────
    val broke = held(fresh(3, streak = 9), 100)
    expect("an unshielded miss breaks the streak", 0, broke.state.streak)
    expect("day one is the streak and nothing else", Penalty.STREAK_BROKEN, broke.penalty)
    expect("day two is silent", null, penaltyFor(2))
    expect("day three debits aura", Penalty.AURA_DEBIT, penaltyFor(3))
    expect("the debit is 200", 200, held(fresh(3, misses = 2), 100).auraDebit)
    expect("day four is silent", null, penaltyFor(4))
    expect("day five warns", Penalty.DEMOTION_WARNING, penaltyFor(5))
    expect("day six is silent", null, penaltyFor(6))
    expect("day seven demotes", Penalty.DEMOTION, penaltyFor(7))
    expect("only one penalty is ever live", 1, (1..7).count { penaltyFor(it) == Penalty.AURA_DEBIT })

    // ── rank transitions ─────────────────────────────────────────────────────
    val demoted = held(fresh(3, misses = 6), 100)
    expect("day seven costs a tier", "E · I", demoted.state.rank.label)
    expect("demotion is reported", true, demoted.demoted)
    expect("the sequence restarts after demotion", 0, demoted.state.consecutiveMisses)
    expect("time at the new tier restarts", 0, demoted.state.daysHeldAtTier)
    expect("E-III cannot fall further", "E · III", held(fresh(0, misses = 6), 100).state.rank.label)

    expect("E-III promotes after seven held days", 7, daysToPromote(Rank(0)))
    expect("D asks for more", 10, daysToPromote(Rank(3)))
    expect("S asks the most", 26, daysToPromote(Rank(15)))
    val promoted = held(fresh(0, daysAtTier = 6, streak = 6), 300)
    expect("the seventh held day promotes", "E · II", promoted.state.rank.label)
    expect("promotion is reported", true, promoted.promoted)
    expect("time at tier restarts on promotion", 0, promoted.state.daysHeldAtTier)
    expect("S-I cannot rise further", "S · I", held(fresh(17, daysAtTier = 25), 4000).state.rank.label)

    println()
    if (failures > 0) error("$failures of the checks above failed")
    println("all checks passed")
}
