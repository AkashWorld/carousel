package fx

import client.playerpage.MediaPlayerControls
import client.playerpage.getMillisecondsToHHMMSS
import org.junit.jupiter.api.Test

class UtilTests {
    @Test
    fun shouldConvert1MinMilisecondsToHHMMSS() {
        val result = getMillisecondsToHHMMSS(60000)

        assert(result == "1:00")
    }

    @Test
    fun shouldConvert2HoursMilisecondsToHHMMSS() {
        val result = getMillisecondsToHHMMSS(7200000)

        assert(result == "2:00:00")
    }

    @Test
    fun shouldConvertRandonHoursMilisecondsToHHMMSS() {
        val result = getMillisecondsToHHMMSS(4435353)

        assert(result == "1:13:55")
    }
}