package com.carousal.client

import client.views.ApplicationView
import client.views.intropage.IntroPageStyles
import client.views.playerpage.FileLoaderStyles
import client.views.playerpage.mediaplayer.MediaPlayerStyles
import client.views.playerpage.chatfeed.ChatFeedStyles
import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory
import tornadofx.*

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
    launch<Application>()
}

