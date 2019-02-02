package com.roasloa.smak.Controller.Services

import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.roasloa.smak.Controller.Controller.App
import com.roasloa.smak.Controller.Utilities.*
import org.json.JSONException
import org.json.JSONObject

object AuthService {

//    var isLoggedIn = false
//    var userEmail = ""
//    var authToken = ""

    fun registerUser(email: String, password: String, complete: (Boolean) -> Unit) {

        val jsonBody = JSONObject()
        jsonBody.put("email", email)
        jsonBody.put("password", password)
        val requestBody = jsonBody.toString()

        val registerRequest = object : StringRequest(Method.POST, URL_REGISTER, Response.Listener { response ->
            println(response)
            complete(true)
        }, Response.ErrorListener { error ->
            Log.d("ERROR", "Could not register user: $error")
            complete(false)
        }){
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }

        App.sharedPrefs.requestQueue.add(registerRequest)//.setShouldCache(false)
    }

    fun loginUser(email: String, password: String, complete: (Boolean) -> Unit) {

        val jsonBody = JSONObject()
        jsonBody.put("email", email)
        jsonBody.put("password", password)
        val requestBody = jsonBody.toString()

        val loginRequest = object: JsonObjectRequest(Method.POST, URL_LOGIN, null, Response.Listener{response ->
            println(response)
            //access a value from jsonObject with the following

            try {
                App.sharedPrefs.userEmail = response.getString("user")
                App.sharedPrefs.authToken  = response.getString("token")
                App.sharedPrefs.isLoggedIn  = true
                complete(true)
            }catch (e: JSONException){
                Log.d("JSON", "EXC: " + e.localizedMessage)
                complete(false)
            }

            //this is where we parse the json object

        }, Response.ErrorListener {error ->

            //this is where we deal with our errors
            Log.d("ERROR", "Could not login user: $error")
            complete(false)
        }){
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }
        App.sharedPrefs.requestQueue.add(loginRequest)//.setShouldCache(false)

    }

    fun createUser(name: String, email: String, avatarName: String, avatarColor: String, complete: (Boolean) -> Unit){

        val jsonBody = JSONObject()
        jsonBody.put("name", name)
        jsonBody.put("email", email)
        jsonBody.put("avatarName", avatarName)
        jsonBody.put("avatarColor", avatarColor)
        val requestBody = jsonBody.toString()

        val createRequest = object: JsonObjectRequest(Method.POST, URL_CREATE_USER, null, Response.Listener {response ->

            try {

                UserDataService.name = response.getString("name")
                UserDataService.email = response.getString("email")
                UserDataService.avatarName = response.getString("avatarName")
                UserDataService.avatarColor  = response.getString("avatarColor")
                UserDataService.id  = response.getString("_id")
                complete(true)

            }catch (e: JSONException){
                Log.d("JSON", "EXC " + e.localizedMessage)
                complete(false)
            }



        }, Response.ErrorListener {error->

            Log.d("ERROR", "Could not add user: $error")
            complete(false)
        }){

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String,String>()
                headers.put("Authorization", "Bearer ${App.sharedPrefs.authToken}")
                return headers
            }

        }

        App.sharedPrefs.requestQueue.add(createRequest)//.setShouldCache(false)
    }

    fun findUserByEmail(context: Context, complete: (Boolean) -> Unit){
        val findUserRequest = object: JsonObjectRequest(Method.GET, "$URL_GET_USER${App.sharedPrefs.userEmail}",null, Response.Listener {response ->
            try {
                UserDataService.name = response.getString("name")
                UserDataService.email = response.getString("email")
                UserDataService.avatarName = response.getString("avatarName")
                UserDataService.avatarColor = response.getString("avatarColor")
                UserDataService.id = response.getString("_id")

                val userDataChange = Intent(BROADCAST_USER_DATA_CHANGE)
                LocalBroadcastManager.getInstance(context).sendBroadcast(userDataChange)
                complete(true)
            }catch (e: JSONException){
                Log.d("JSON", "EXC: " + e.localizedMessage)
            }

        }, Response.ErrorListener {error ->
            Log.d("ERROR", "Could not login user: $error")
            complete(false)

        }){
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String,String>()
                headers.put("Authorization", "Bearer ${App.sharedPrefs.authToken}")
                return headers
            }
        }

        App.sharedPrefs.requestQueue.add(findUserRequest)
    }
}