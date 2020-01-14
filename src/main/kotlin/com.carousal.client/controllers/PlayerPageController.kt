package com.carousal.client.controllers

import com.carousal.client.views.playerpage.PlayerPage
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