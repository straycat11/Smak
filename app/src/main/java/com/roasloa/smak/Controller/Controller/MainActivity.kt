package com.roasloa.smak.Controller.Controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.os.Message
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.roasloa.smak.Controller.Model.Channel
import com.roasloa.smak.Controller.Services.AuthService
import com.roasloa.smak.Controller.Services.MessageService
import com.roasloa.smak.Controller.Services.UserDataService
import com.roasloa.smak.Controller.Utilities.BROADCAST_USER_DATA_CHANGE
import com.roasloa.smak.Controller.Utilities.SOCKET_URL
import com.roasloa.smak.R
import io.socket.client.IO
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : AppCompatActivity() {

    val socket = IO.socket(SOCKET_URL)

    override fun onCreate(savedInstanceState: Bundle?) {



        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        socket.connect()
        socket.on("channelCreated",onNewChannel)



        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()



    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(userDataChangeReceiver,
            IntentFilter(BROADCAST_USER_DATA_CHANGE))
    }


    override fun onDestroy() {
        socket.disconnect()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userDataChangeReceiver)

        super.onDestroy()

    }

    private val userDataChangeReceiver = object: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            if(AuthService.isLoggedIn){
                println("The user logged in status is: ${AuthService.isLoggedIn}")
                userNameNavHeader.text = UserDataService.name
                println("${userNameNavHeader.text} = ${UserDataService.name}")
                userEmailNavHeader.text = UserDataService.email
                val resourceId = resources.getIdentifier(UserDataService.avatarName, "drawable",
                    packageName)
                userImageNavHeader.setImageResource(resourceId)
                userImageNavHeader.setBackgroundColor(UserDataService.returnAvatarColor(UserDataService.avatarColor))
                loginBtnNavHeader.text = "Logout"
            }
        }
    }

    fun loginBtnNavClicked(view: View) {

        if(AuthService.isLoggedIn){
            //log out
            UserDataService.logOut()
            userNameNavHeader.text = ""
            userEmailNavHeader.text = ""
            userImageNavHeader.setImageResource(R.drawable.profiledefault)
            userImageNavHeader.setBackgroundColor(Color.TRANSPARENT)
            loginBtnNavHeader.text = "LOGIN"

        }else{
        val loginActivity = Intent(this, LoginActivity::class.java)
        startActivity(loginActivity)
        }
    }

    fun addChannelClicked(view: View) {
        if(AuthService.isLoggedIn) {
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.add_channel_dialog, null)
            builder.setView(dialogView)
                .setPositiveButton("Add"){dialogInterface, i ->
                    val nameTextField = dialogView.findViewById<EditText>(R.id.addChannelNameTxt)
                    val descTextField = dialogView.findViewById<EditText>(R.id.addChannelDescTxt)

                    val channelName = nameTextField.text.toString()
                    val channelDesc = descTextField.text.toString()


                    socket.emit("newChannel", channelName, channelDesc)

                }.setNegativeButton("Cancel"){dialogInterface, i ->
                }.show()
        }
    }

    fun sendMessageBtnClicked(view: View){
        hideKeyboard()
    }
    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private val onNewChannel = Emitter.Listener {args ->
        runOnUiThread{
            val channelName = args[0] as String
            val channelDescription = args[1] as String
            val channelId = args[2] as String

            val newChannel = Channel(channelName,channelDescription,channelId)
            MessageService.channels.add(newChannel)
        }
    }
    fun hideKeyboard(){
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if(inputManager.isAcceptingText){
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken,0)
        }
    }
}

