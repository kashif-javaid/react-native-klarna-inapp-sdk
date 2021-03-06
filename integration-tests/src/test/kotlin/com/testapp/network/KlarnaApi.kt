package com.testapp.network

import com.google.gson.Gson
import com.testapp.model.Session
import com.testapp.model.SessionInfoRequest
import com.testapp.model.SessionInfoResponse
import com.testapp.model.SessionRequest
import com.testapp.model.SessionResponse
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

internal object KlarnaApi {

    private val client by lazy { OkHttpClient.Builder().build() }
    private val gson by lazy { Gson() }

    private val sessionRequestUrl = "https://www.klarna.com/demo/klarna-api/sessions/"
    private val sessionInfoUrl = "https://www.klarna.com/demo/klarna-api/sessions/session-get/"

    fun getSessionInfo(sessionRrequest: SessionRequest): SessionInfoResponse? {
        try {
            val sessionResponse = createSession(sessionRrequest)
            return sessionResponse?.let {
                val sessionInfo = getSessionInfo(SessionInfoRequest(
                        "payments/v1/sessions/${it.sessionId}",
                        sessionRrequest.country
                ))
                sessionInfo
            }
        } catch (t: Throwable){
            return null
        }
    }

    private fun createSession(sessionRrequest: SessionRequest): SessionResponse? {
        try {
            val request = Request.Builder().apply {
                url(sessionRequestUrl)
                method("POST", RequestBody.create(MediaType.parse("application/json"), gson.toJson(sessionRrequest)))
            }.build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful && response.body() != null) {
                return gson.fromJson(response.body()!!.string(), SessionResponse::class.java)
            }
            return null
        } catch (t: Throwable){
            return null
        }
    }

    private fun getSessionInfo(sessionInfoRequest: SessionInfoRequest): SessionInfoResponse? {
        try {
            val request = Request.Builder().apply {
                url(sessionInfoUrl)
                method("POST", RequestBody.create(MediaType.parse("application/json"), gson.toJson(sessionInfoRequest)))
            }.build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful && response.body() != null) {
                val bodyText = response.body()!!.string().replace("\\n", "").replace(" ", "")
                val bodyObject = JSONObject(bodyText)
                val sessionObject = JSONObject(bodyObject.get("session").toString())
                return SessionInfoResponse(gson.fromJson(sessionObject.toString(), Session::class.java))
            }
            return null
        } catch (t: Throwable){
            return null
        }
    }
}