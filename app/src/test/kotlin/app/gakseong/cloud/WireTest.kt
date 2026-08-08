package app.gakseong.cloud

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * §10 has three things that may leave the device. These are the checks that the fourth cannot.
 *
 * The failure this guards is not a crash. It is a package name in Google's logs, which nobody notices and
 * nobody can take back.
 */
class WireTest {

    @Test
    fun `a package name is refused`() {
        listOf(
            "com.instagram.android", "com.google.android.youtube", "in.swiggy.android",
            "app.gakeseong", "com.zhiliaoapp.musically",
        ).forEach {
            assertNotNull("$it should be refused", refusedReason(it))
            assertFalse(permitted(Dimension.RANK_LETTER, it))
        }
    }

    @Test
    fun `a duration is refused in every shape it arrives in`() {
        listOf("41 min", "2h 41m", "3600s", "18:44", "45:00", "120ms", "6 hours").forEach {
            assertNotNull("$it should be refused", refusedReason(it))
        }
    }

    @Test
    fun `anything long enough to carry detail is refused`() {
        assertNotNull(refusedReason("the user opened instagram forty one times before lunch"))
    }

    @Test
    fun `the three things that may leave are not refused`() {
        // §10: guild identity, anonymous feature counts, crash stack traces.
        listOf("D", "S", "true", "false", "SENSOR", "DECLARED", "APP_INITIATED").forEach {
            assertNull("$it should be permitted", refusedReason(it))
        }
    }

    @Test
    fun `the private track is exempt unconditionally`() {
        // §9: not a screen view, not a count, not "a check-in happened", and not tied to the analytics toggle.
        assertTrue(Event.PRIVATE_TRACK_EXEMPT)
    }

    @Test
    fun `no event names anything about what a user did`() {
        // Feature counts, not behaviour. An event called "instagram_opened" would be a package name by another
        // route, and one called "scrolled_three_hours" a duration.
        Event.entries.forEach { e ->
            assertNull("${e.wireName} carries detail", refusedReason(e.wireName))
            assertFalse("${e.wireName} names an app", e.wireName.contains("."))
        }
    }

    @Test
    fun `no event mentions the private track`() {
        Event.entries.forEach {
            assertFalse(it.name.contains("PRIVATE"))
            assertFalse(it.wireName.contains("private"))
        }
    }

    @Test
    fun `dimensions carry no free text`() {
        // Every dimension is an enum, so a caller cannot attach an arbitrary string by writing one.
        assertEquals(Dimension.entries.size, Dimension.entries.map { it.wireName }.distinct().size)
    }
}

/** The release condition, which is the only defence the referral chain actually has. */
class ReferralTest {

    @Test
    fun `three cleared days inside the window releases it`() {
        assertTrue(released(setOf("2026-08-08", "2026-08-10", "2026-08-14"), installedOn = "2026-08-08"))
    }

    @Test
    fun `they do not have to be consecutive`() {
        // Consecutive would punish the inviter for the invitee's bad Tuesday.
        assertTrue(released(setOf("2026-08-08", "2026-08-12", "2026-08-20"), installedOn = "2026-08-08"))
    }

    @Test
    fun `two days is not enough`() {
        assertFalse(released(setOf("2026-08-08", "2026-08-09"), installedOn = "2026-08-08"))
    }

    @Test
    fun `days outside the fourteen day window do not count`() {
        assertFalse(released(setOf("2026-08-08", "2026-08-09", "2026-08-30"), installedOn = "2026-08-08"))
    }

    @Test
    fun `days before the install do not count`() {
        // Otherwise a reinstall on an account with history claims the reward immediately.
        assertFalse(released(setOf("2026-08-01", "2026-08-02", "2026-08-03"), installedOn = "2026-08-08"))
    }

    @Test
    fun `no cleared days is not released`() {
        assertFalse(released(emptySet(), installedOn = "2026-08-08"))
    }

    @Test
    fun `the window crosses a month end`() {
        assertTrue(released(setOf("2026-08-25", "2026-09-01", "2026-09-05"), installedOn = "2026-08-25"))
    }

    @Test
    fun `the window crosses a year end`() {
        assertTrue(released(setOf("2026-12-28", "2027-01-02", "2027-01-08"), installedOn = "2026-12-28"))
    }

    @Test
    fun `february is handled in both a leap year and a common one`() {
        assertEquals("2028-03-06", "2028-02-21".plusDaysIso(14))
        assertEquals("2027-03-07", "2027-02-21".plusDaysIso(14))
    }

    @Test
    fun `the code is pulled out of a play referrer and an app link alike`() {
        assertEquals("7F2K9Q", codeFrom("utm_source=play&gs=7F2K9Q&utm_medium=referral"))
        assertEquals("7F2K9Q", codeFrom("https://gakseong.app/s/7F2K9Q"))
        assertEquals("7F2K9Q", codeFrom("utm_content=7F2K9Q"))
    }

    @Test
    fun `an organic install carries no code`() {
        assertNull(codeFrom(null))
        assertNull(codeFrom(""))
        assertNull(codeFrom("utm_source=google-play&utm_medium=organic"))
    }

    @Test
    fun `the reward is the one the economy names and is capped weekly`() {
        assertEquals(600, REFERRAL_AURA)
        assertEquals(1, REWARDED_PER_WEEK)
    }
}
