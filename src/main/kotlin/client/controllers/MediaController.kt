package client.controllers

import client.models.MediaActionModel
import client.models.MediaActionModelImpl
import client.models.MediaActionObservable
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

    fun getMediaActionObservable(error: () -> Unit): MediaActionObservable {
        return mediaActionModel.subscribeToActions(error)
    }
}