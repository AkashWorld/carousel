package com.carousel.client.controllers

import com.carousel.client.views.playerpage.PlayerPage
import tornadofx.*

class PlayerPageController: Controller() {
    private val playerPageView: PlayerPage by inject()

    fun navigateToFileLoaderPage() {
        playerPageView.navigateToFileLoader()
    }

    fun exitToIntroPage() {
        playerPageView.navigateToIntroPage()
    }
}