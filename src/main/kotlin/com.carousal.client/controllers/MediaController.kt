package com.carousal.client.controllers

import com.carousal.client.models.MediaActionModel
import com.carousal.client.models.MediaActionModelImpl
import com.carousal.client.models.observables.MediaActionObservable
import tornadofx.*

class MediaController : Controller() {
    private val mediaActionModel: MediaActionModel = MediaActionModelImpl()

    fun pauseAction(error: () -> Unit) {
        mediaActionModel.setPauseAction(error)
    }

    fun playAction(error: () -> Unit) {
        mediaActionModel.setPlayAction(error)
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