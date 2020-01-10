import com.carousal.client.controllers.ChatController
import org.junit.jupiter.api.Test

class TokenizerTest {
    @Test
    fun shouldSuccessfullyTokenizeEmojisAndString() {
        val input = ":wink: I think :eggplant::pepe:    this is great"
        val chatController = ChatController()

        val result = chatController.tokenizeMessage(input)

        val tokens = listOf(":wink:", " I think ", ":eggplant:", ":pepe:", "    this is great")
        assert(result == tokens)
    }

    @Test
    fun shouldTokenizeStringWithSpareTokens() {
        val input = ":wink: I think :eggplant:::pepe:    this is great"
        val chatController = ChatController()

        val result = chatController.tokenizeMessage(input)

        val tokens = listOf(":wink:", " I think ", ":eggplant:", "::", "pepe", ":    this is great")
        assert(result == tokens)
    }

    @Test
    fun shouldParseEmptyColons() {
        val input = ":: : ::::"
        val chatController = ChatController()

        val result = chatController.tokenizeMessage(input)

        val tokens = listOf("::", " ", ": ", "::", "::")
        assert(result == tokens)
    }

    @Test
    fun shouldReturnEmptyListIfEmpty() {
        val input = ""
        val chatController = ChatController()

        val result = chatController.tokenizeMessage(input)

        val tokens = listOf<String>()
        assert(result == tokens)
    }
}