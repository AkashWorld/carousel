package client.models

import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableStringValue
import okhttp3.OkHttpClient

class ClientContext(
    private var serverAddress: SimpleStringProperty = SimpleStringProperty(),
    private var token: String
) {
    val client = OkHttpClient()

    fun getServerAddress(): ObservableStringValue {
        return serverAddress
    }

    fun getToken(): String {
        return this.token
    }

    fun setServerAddress(serverAddress: String) {
        this.serverAddress.value = serverAddress
    }

    fun setToken(token: String) {
        this.token = token
    }
}