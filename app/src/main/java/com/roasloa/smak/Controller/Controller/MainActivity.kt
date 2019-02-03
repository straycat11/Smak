package com.roasloa.smak.Controller.Controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import com.roasloa.smak.Controller.Adapters.MessageAdapter
import com.roasloa.smak.Controller.Model.Channel
import com.roasloa.smak.Controller.Model.Message
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
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : AppCompatActivity() {

    val socket = IO.socket(SOCKET_URL)
    lateinit var channelAdapter: ArrayAdapter<Channel>
    lateinit var messageAdapter: MessageAdapter
    var selectedChannel : Channel? = null

    private fun setupAdapter(){
        channelAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1, MessageService.channels)
        channel_list.adapter = channelAdapter

        messageAdapter = MessageAdapter(this, MessageService.messages)
        messageListView.adapter = messageAdapter
        val layoutManager = LinearLayoutManager(this)
        messageListView.layoutManager = layoutManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {



        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        socket.connect()
        socket.on("channelCreated",onNewChannel)
        socket.on("messageCreated",onNewMessage)





        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        setupAdapter()

        channel_list.setOnItemClickListener{ _, _, i, _ ->
            selectedChannel = MessageService.channels[i]
            drawer_layout.closeDrawer(GravityCompat.START)
            updateWithChannel()
        }

        if(App.sharedPrefs.isLoggedIn){
            AuthService.findUserByEmail(this){}
        }


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
        override fun onReceive(context: Context, intent: Intent?) {
            if(App.sharedPrefs.isLoggedIn){
                println("The user logged in status is: ${App.sharedPrefs.isLoggedIn}")
                userNameNavHeader.text = UserDataService.name
                println("${userNameNavHeader.text} = ${UserDataService.name}")
                userEmailNavHeader.text = UserDataService.email
                val resourceId = resources.getIdentifier(UserDataService.avatarName, "drawable",
                    packageName)
                userImageNavHeader.setImageResource(resourceId)
                userImageNavHeader.setBackgroundColor(UserDataService.returnAvatarColor(UserDataService.avatarColor))
                loginBtnNavHeader.text = "Logout"

                MessageService.getChannels{complete->
                    if(complete){
                        if(MessageService.channels.count() > 0){
                            selectedChannel = MessageService.channels[0]
                            channelAdapter.notifyDataSetChanged()
                            updateWithChannel()
                        }

                    }

                }
            }
        }
    }


    fun updateWithChannel(){
        mainChannelName.text = "#${selectedChannel?.name}"
        if(selectedChannel != null){
            MessageService.getMessages(selectedChannel!!.id){complete ->
                if(complete){
                    messageAdapter.notifyDataSetChanged()
                    if(messageAdapter.itemCount > 0){
                        messageListView.smoothScrollToPosition(messageAdapter.itemCount - 1)
                    }
                }
            }
        }
    }
    fun loginBtnNavClicked(view: View) {

        if(App.sharedPrefs.isLoggedIn){
            //log out
            UserDataService.logOut()
            channelAdapter.notifyDataSetChanged()
            messageAdapter.notifyDataSetChanged()
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
        if(App.sharedPrefs.isLoggedIn) {
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.add_channel_dialog, null)
            builder.setView(dialogView)
                .setPositiveButton("Add"){ _, _ ->
                    val nameTextField = dialogView.findViewById<EditText>(R.id.addChannelNameTxt)
                    val descTextField = dialogView.findViewById<EditText>(R.id.addChannelDescTxt)

                    val channelName = nameTextField.text.toString()
                    val channelDesc = descTextField.text.toString()


                    socket.emit("newChannel", channelName, channelDesc)

                }.setNegativeButton("Cancel"){ _, _ ->
                }.show()
        }
    }

    fun sendMessageBtnClicked(view: View){
        if(App.sharedPrefs.isLoggedIn && messageTextField.text.isNotEmpty() && selectedChannel != null){
            val userId = UserDataService.id
            val channelId = selectedChannel!!.id
            socket.emit("newMessage",messageTextField.text.toString(),userId,channelId, UserDataService.name, UserDataService.avatarName, UserDataService.avatarColor)
            messageTextField.text.clear()
            hideKeyboard()
        }
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
        if(App.sharedPrefs.isLoggedIn){
            runOnUiThread{
                val channelName = args[0] as String
                val channelDescription = args[1] as String
                val channelId = args[2] as String

                val newChannel = Channel(channelName,channelDescription,channelId)
                MessageService.channels.add(newChannel)
                channelAdapter.notifyDataSetChanged()
                messageAdapter.notifyDataSetChanged()

            }
        }

    }

    private val onNewMessage = Emitter.Listener { args ->
        if(App.sharedPrefs.isLoggedIn){
            runOnUiThread {
                val channelId = args[2] as String
                if(channelId == selectedChannel?.id){
                    val messageBody = args[0] as String
                    val userName = args[3] as String
                    val userAvatar = args[4] as String
                    val userAvatarColor = args[5] as String
                    val id = args[6] as String
                    val timeStamp = args[7] as String

                    val newMessage = Message(messageBody,userName,channelId,userAvatar,userAvatarColor,id,timeStamp)
                    MessageService.messages.add(newMessage)
                    messageAdapter.notifyDataSetChanged()
                    messageListView.smoothScrollToPosition(messageAdapter.itemCount - 1)
                }
            }
        }
    }
    fun hideKeyboard(){
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if(inputManager.isAcceptingText){
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken,0)
        }
    }
}

