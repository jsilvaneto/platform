package com.platform.app.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

data class CepAddress(
    val street: String = "",
    val neighborhood: String = "",
    val city: String = "",
    val state: String = ""
)

@Singleton
class CepLookupService @Inject constructor() {

    suspend fun lookupCep(rawCep: String): CepAddress? = withContext(Dispatchers.IO) {
        val cleanCep = rawCep.replace(Regex("[^0-9]"), "")
        if (cleanCep.length != 8) return@withContext null

        try {
            val url = URL("https://viacep.com.br/ws/$cleanCep/json/")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.requestMethod = "GET"
            conn.setRequestProperty("Accept", "application/json")

            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val response = reader.readText()
                reader.close()

                val json = JSONObject(response)
                if (json.optBoolean("erro", false)) {
                    return@withContext null
                }

                CepAddress(
                    street = json.optString("logradouro", ""),
                    neighborhood = json.optString("bairro", ""),
                    city = json.optString("localidade", ""),
                    state = json.optString("uf", "")
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
