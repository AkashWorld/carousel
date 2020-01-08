package com.carousal.client.views

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXDialog
import com.jfoenix.controls.JFXDialogLayout
import javafx.scene.layout.StackPane
import javafx.scene.text.Text

class ViewUtils {
    companion object {
        fun showErrorDialog(message: String, stackPane: StackPane) {
            val layout = JFXDialogLayout()
            val dialog = JFXDialog(stackPane, layout, JFXDialog.DialogTransition.CENTER, true)
            layout.setHeading(Text("Error"))
            layout.setBody(Text(message))
            val closeButton = JFXButton("Okay")
            closeButton.setOnAction { dialog.close() }
            layout.setActions(closeButton)
            dialog.show()
        }
    }
}