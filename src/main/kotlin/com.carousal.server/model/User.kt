package com.carousal.server.model

import com.carousal.server.datafetchers.ChatFeedPublisher
import com.carousal.server.datafetchers.MediaActionPublisher
import com.carousal.server.datafetchers.UserActionPublisher

class User constructor(val username: String, var isReady: Boolean, var media: Media?) {
    private var userActionPublisher: UserActionPublisher? = null
    private var mediaActionPublisher: MediaActionPublisher? = null
    private var chatFeedPublisher: ChatFeedPublisher? = null

    fun setUserActionPublisher(userActionPublisher: UserActionPublisher) {
        this.userActionPublisher = userActionPublisher
    }

    fun setMediaActionPublisher(mediaActionPublisher: MediaActionPublisher) {
        this.mediaActionPublisher = mediaActionPublisher
    }

    fun setChatFeedPublisher(chatFeedPublisher: ChatFeedPublisher) {
        this.chatFeedPublisher = chatFeedPublisher
    }

    fun getMediaActionPublisher(): MediaActionPublisher? {
        return mediaActionPublisher
    }

    fun getChatFeedPublisher(): ChatFeedPublisher? {
        return chatFeedPublisher
    }

    fun getUserActionPublisher(): UserActionPublisher? {
        return userActionPublisher
    }
}