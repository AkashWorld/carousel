package fx

import client.Styles
import client.controllers.ConnectController
import client.playerpage.mediaplayer.getMillisecondsToHHMMSS
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

    @Test
    fun shouldRetrieveNonNullBackgroundURI() {
        val result = Styles.getRandomBackground()

        assert(result != null)
    }

    @Test
    fun shouldValidateAlphanumeric() {
        val connectController = ConnectController()
        assert(connectController.validateUsername("LoneHunt"))
        assert(!connectController.validateUsername("Lone Hunt"))
        assert(connectController.validateUsername("LoneHunt123"))
        assert(!connectController.validateUsername("LoneHunt  123"))
        assert(connectController.validateUsername("lonehunt"))
        assert(!connectController.validateUsername("    "))
        assert(connectController.validateUsername("123432423432"))
        assert(!connectController.validateUsername("LoneHunt#"))
        assert(!connectController.validateUsername("^^^"))
        assert(!connectController.validateUsername("***"))
        assert(!connectController.validateUsername("  *"))
    }
}