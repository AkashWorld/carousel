package com.carousal.client

import com.carousal.client.views.ApplicationView
import com.carousal.client.views.intropage.IntroPageStyles
import com.carousal.client.views.playerpage.fileloader.FileLoaderStyles
import com.carousal.client.views.playerpage.mediaplayer.MediaPlayerStyles
import com.carousal.client.views.playerpage.chatfeed.ChatFeedStyles
import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory
import tornadofx.*
import java.util.logging.Level
import java.util.logging.Logger

class Application :
    App(
        ApplicationView::class,
        ChatFeedStyles::class,
        FileLoaderStyles::class,
        MediaPlayerStyles::class,
        IntroPageStyles::class
    ) {
    init {
        SvgImageLoaderFactory.install()
    }
}

fun main(args: Array<String>) {
    Logger.getGlobal().level = Level.OFF
    launch<Application>()
}

