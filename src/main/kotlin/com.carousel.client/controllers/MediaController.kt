package com.carousel.client.controllers

import com.carousel.client.models.MediaActionModel
import com.carousel.client.models.MediaActionModelImpl
import com.carousel.client.models.observables.MediaActionObservable
import tornadofx.*

class MediaController : Controller() {
    private val mediaActionModel: MediaActionModel = MediaActionModelImpl()

    fun pauseAction(error: () -> Unit) {
        mediaActionModel.setPauseAction(error)
    }

    fun playAction(success: (Boolean) -> Unit, error: () -> Unit) {
        mediaActionModel.setPlayAction(success, error)
    }

    fun seekAction(currentTime: Float, error: () -> Unit) {
        mediaActionModel.setSeekAction(currentTime, error)
    }

    fun loadMedia(filename: String, success: () -> Unit, error: () -> Unit) {
        mediaActionModel.loadMediaAction(filename, success, error)
    }

    fun getMediaActionObservable(): MediaActionObservable {
        return mediaActionModel.getMediaActionObservable()
    }

    fun subscribeToMediaActions(error: () -> Unit) {
        mediaActionModel.subscribeToActions {
            runLater(error)
        }
    }
}