package client.models

import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableStringValue

class ClientContext(
    private var serverAddress: SimpleStringProperty = SimpleStringProperty(),
    private var token: String? = null
) {

    fun getServerAddress(): ObservableStringValue {
        return serverAddress
    }

    fun setServerAddress(serverAddress: String) {
        this.serverAddress.value = serverAddress
    }

    fun setToken(token: String) {
        this.token = token
    }
}