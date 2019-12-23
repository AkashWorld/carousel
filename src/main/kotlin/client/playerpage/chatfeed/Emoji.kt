package client.playerpage.chatfeed

import com.vdurmont.emoji.EmojiParser
import javafx.scene.image.Image
import org.slf4j.LoggerFactory

interface Emoji {
    fun getEmojiFromAlias(alias: String): Image?
}

enum class EmojiType {
    TWITTER,
    CUSTOM
}

class EmojiLoader : Emoji {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    val emojiCache = mutableMapOf<String, Image>()
    val customEmoji = mapOf(
        ":pepe:" to "pepe"
    )

    private fun getEmojiPath(emojiName: String, emojiType: EmojiType): String? {
        if (emojiType == EmojiType.TWITTER) {
            return this::class.java.classLoader.getResource("twemoji/svg/$emojiName.svg")?.toString()
        } else if (emojiType == EmojiType.CUSTOM) {
            return this::class.java.classLoader.getResource("emoji/$emojiName.svg")?.toString()
        }
        return null
    }

    override fun getEmojiFromAlias(alias: String): Image? {
        if (emojiCache.contains(alias)) {
            return emojiCache[alias]
        }
        var path: String? = null
        val unicode = EmojiParser.parseToUnicode(alias)
        if (unicode == alias) {
            if (customEmoji.containsKey(alias)) {
                val emojiFilename = customEmoji[alias]
                path = if (emojiFilename != null) getEmojiPath(
                    emojiFilename, EmojiType.CUSTOM
                ) else null
            }
        } else {
            /**
             * Twitter emoji filename is HEX Codepoint
             */
            val chars = unicode.toCharArray()
            val emojiFilename = Character.codePointAt(chars, 0x0).toString(16)
            path = getEmojiPath(emojiFilename, EmojiType.TWITTER)
        }
        if (path == null) {
            return null
        }
        return try {
            val image = Image(path, 20.0, 20.0, true, true, true)
            emojiCache[alias] = image
            image
        } catch (e: Exception) {
            logger.error(e.message, e.cause)
            null
        }
    }
}