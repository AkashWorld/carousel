package com.carousal.client.views

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXDialog
import com.jfoenix.controls.JFXDialogLayout
import javafx.scene.layout.StackPane
import javafx.scene.text.Text

class ViewUtils {
    companion object {
        fun showErrorDialog(message: String, stackPane: StackPane) {
            showErrorDialog("Error", message, stackPane)
        }

        fun showErrorDialog(header: String, message: String, stackPane: StackPane) {
            val layout = JFXDialogLayout()
            val dialog = JFXDialog(stackPane, layout, JFXDialog.DialogTransition.CENTER, true)
            layout.setHeading(Text(header))
            layout.setBody(Text(message))
            val closeButton = JFXButton("Okay")
            closeButton.setOnAction { dialog.close() }
            layout.setActions(closeButton)
            dialog.show()
        }
    }
}