@file:OptIn(ExperimentalTime::class)

package me.riddle.substrate.examples.step00.health

import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import org.junit.jupiter.api.TestInfo
import org.slf4j.LoggerFactory
import java.io.File
import java.net.InetAddress
import kotlin.test.*
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

private const val logSettingNamedTestUpOnHostWithDelay = "Setting up test `{}` on {} ({})"
private const val logTaringDownNamedTestOnHostWithDelay = "Taring down test `{}` on {} ({})"

private const val logExecutingCommand = "Executing command `{}`."
private const val logExecutingCommandWithTimeout = "Executing `{}` command with timeout in {} seconds."
private const val logExecutingCommandWithTimeoutInDirectory = "Executing `{}` command with timeout of {} seconds and in host directory of `{}`."

private const val logExecutedSuccessfullyWithResult = "Command `{}` executed successfully with result=`{}`."


@Suppress("LoggingSimilarMessage")
class ExecuteCommandTest {

    private val testLogger by lazy { LoggerFactory.getLogger(this::class.java) }
    private val command = "echo hello"
    private val expected = "hello"

    private val timeout: Long = 1

    private val timeZone = TimeZone.currentSystemDefault()
    private val timeCommand = "date"

    private val hostCommand = "hostname"
    private val hostName = InetAddress.getLocalHost().hostName

    private val timeStamp = Clock.System.now()

    @BeforeTest
    fun setUp(testInfo: TestInfo) = testLogger.info(logSettingNamedTestUpOnHostWithDelay, testInfo.displayName, hostName, Clock.System.now() - timeStamp)

    @AfterTest
    fun tearDown(testInfo: TestInfo) = testLogger.info(logTaringDownNamedTestOnHostWithDelay, testInfo.displayName, hostName, Clock.System.now() - timeStamp)

    @Test
    fun `Execute trivial 'echo' command test saying 'hello'`() {
        testLogger.info(logExecutingCommand, command)

        val result = (command.executeCommand() ?: fail("Command `$command` failed."))
        assertNotNull(result, "Subprocess execution produced null result and failed.")
        assertEquals(expected, result)

        testLogger.info(logExecutedSuccessfullyWithResult, command, result)
    }

    @Test
    fun `Execute 'hostname' command with timeout of 1 second`() {
        testLogger.info(logExecutingCommandWithTimeout, hostCommand, timeout)

        val hostResult = hostCommand.executeCommand(timeoutSeconds = timeout)
        assertNotNull(hostResult, "Host command execution produced null.")
        assertEquals(hostName, hostResult, "Host command execution result `$hostResult` does not match expected host name `$hostName`.")

        testLogger.info(logExecutedSuccessfullyWithResult, hostCommand, hostResult)
    }

    @Test
    fun `Execute 'date' command with timeout of 1 second in user home directory`() {
        val userHomeDirectory = System.getProperty("user.home")
        testLogger.info(logExecutingCommandWithTimeoutInDirectory, timeCommand, timeout, userHomeDirectory)

        val workingDirectory = File(userHomeDirectory)
        assertNotNull(workingDirectory, "User home directory string is null.")
        assertTrue(
            with(workingDirectory) { exists() && isDirectory && canWrite() },
            "User home directory `$workingDirectory` does not meet: exists, is directory, and is writable."
        )

        val timeResult = timeCommand.executeCommand(workingDir = workingDirectory, timeoutSeconds = timeout)
        assertNotNull(timeResult, "Command `$timeCommand` execution produced null.")

        val normalizedTimeResult = timeResult.replace(
            Regex("""\b(EDT|EST)\b""")
        ) { mapOf("EDT" to "America/New_York", "EST" to "America/New_York")[it.value]!! }

        val defaultUNIXDateResponseFormat = DateTimeComponents.Format {
            dayOfWeek(names = DayOfWeekNames.ENGLISH_ABBREVIATED); char(' ')
            monthName(names = MonthNames.ENGLISH_ABBREVIATED); char(' ')
            day(); char(' ')
            hour(); char(':'); minute(); char(':'); second(); char(' ')
            timeZoneId(); char(' ')
            year()
        }

        val localDateTimeParsed = normalizedTimeResult.let { defaultUNIXDateResponseFormat.parse(it) }.toLocalDateTime()
        val timeDifference = timeStamp - localDateTimeParsed.toInstant(timeZone)
        assertTrue("Time difference between test invocation and returned by the subprocess is larger than a second ($timeDifference).") { timeDifference < 1.seconds }

        testLogger.info("Command `{}` executed successfully with the local time difference of {}.", timeCommand, timeDifference)
    }

    @Test
    fun `Execute a bogus command with timeout of 1 second fails appropriately`() {
        testLogger.info(logExecutingCommandWithTimeout, "bogus", timeout)

        val bogusCommandResult = "bogus".executeCommand(timeoutSeconds = timeout)
        assertNull(bogusCommandResult, "Bogus command execution should have failed.")

        val bogusParameterResult = "cat bogus".executeCommand(File(System.getProperty("user.home")),timeoutSeconds = timeout)
        assertNull(bogusParameterResult, "Bogus parameter command execution should have failed.")
    }

}