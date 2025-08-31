package me.riddle.substrate.examples.step00.health

import org.slf4j.LoggerFactory
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class HealthProbeTest {

    private val healthTestLogger by lazy { LoggerFactory.getLogger(this::class.java) }


    @Test
    fun `HealthProbe call returns a HealthStatus object - Happy path test`() {

        with(HealthProbe.call("1.0.0")) {
            val now = Clock.System.now()

            assertEquals("ok", status, "FixMe: For now we're always 'ok' - status='ok'.")
            assertEquals("health-v1.0.0", version, "Prefix is 'health' and version is '1.0.0'.")
            assertEquals(7, gitSha.length, "Git SHA should be 7 characters long.")
            assertNotEquals("main", gitBranch, "Git branch should NOT be 'main'")
            assertContains(gitRoot.toPath().toString().split(File.separator),"substrate",  "Git root should contain 'substrate'.")
            assertTrue(startedAt < now, "Started at ($startedAt) should be less than now ($now).")
            assertTrue(upTime > Duration.ZERO, "Up time should be greater than zero.")
            assertTrue(upTime <  now - startedAt, "Started at + up time should be before now.")

            healthTestLogger.info("HealthProbe passing times: startedAt=$startedAt, upTime=$upTime, now=$now, duration=${now - startedAt}, drift=${now - startedAt - upTime}.")
        }
    }

}