package com.carousel.client.controllers

import com.carousel.client.models.GiphyModel
import com.carousel.client.models.ImageDataPayload
import javafx.collections.ObservableList
import javafx.scene.image.Image
import tornadofx.*

class GiphyController : Controller() {
    private val giphyModel = GiphyModel()

    fun getActiveGifList(): ObservableList<Pair<String, Image>> {
        return giphyModel.activeList
    }

    fun pingGiphyRandomId(success: () -> Unit, error: () -> Unit) {
        giphyModel.registerRandomIdCallback(success, error)
    }

    fun retrieveTrendingGifs(error: () -> Unit) {
        giphyModel.retrieveGifURLsTrendingResults(error)
    }

    fun retrieveSearchQueryGifs(query: String, error: () -> Unit) {
        giphyModel.retrieveGifURLsSearchResults(query, error)
    }
}