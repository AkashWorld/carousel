package client.models

enum class ContentType {
    IMAGE,
    MESSAGE
}

class Content(
    private val contentType: ContentType,
    private val content: Any
) {

}

class Message(
    private val user: String,
    private val tokens: List<Content>
) {
    fun getUsername(): String {
        return user
    }

    fun getTokens(): List<Content> {
        return tokens
    }
}