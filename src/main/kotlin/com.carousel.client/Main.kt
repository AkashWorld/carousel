package com.carousel.client

import com.carousel.client.views.ApplicationView
import com.carousel.client.views.intropage.IntroPageStyles
import com.carousel.client.views.playerpage.fileloader.FileLoaderStyles
import com.carousel.client.views.playerpage.mediaplayer.MediaPlayerStyles
import com.carousel.client.views.playerpage.chatfeed.ChatFeedStyles
import com.carousel.client.views.utilities.UtilityStyles
import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory
import tornadofx.*

class Application :
    App(
        ApplicationView::class,
        ChatFeedStyles::class,
        FileLoaderStyles::class,
        MediaPlayerStyles::class,
        IntroPageStyles::class,
        UtilityStyles::class
    ) {
    init {
        SvgImageLoaderFactory.install()
    }
}

fun main(args: Array<String>) {
    launch<Application>()
}

