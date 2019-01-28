package com.roasloa.smak.Controller

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.roasloa.smak.Controller.Services.AuthService
import com.roasloa.smak.R
import kotlinx.android.synthetic.main.activity_create_user.*
import java.util.Random

class CreateUserActivity : AppCompatActivity() {

    var userAvatar = "profiledefault"
    var avatarColor = "[0.5,0.5,0.5,1]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
    }

    fun generateUserAvatar(view: View){
        val random = Random()
        val color = random.nextInt(2)
        val avatar = random.nextInt(28)

        userAvatar = if(color == 0) "light$avatar"
        else {
            "dark$avatar"
        }

        val resourceId = resources.getIdentifier(userAvatar, "drawable", packageName)
        createAvatarImage.setImageResource(resourceId)
    }

    fun generateColorClicked(view: View) {
        val random = Random()
        val r = random.nextInt(255)
        val g = random.nextInt(255)
        val b = random.nextInt(255)

        createAvatarImage.setBackgroundColor(Color.rgb(r,g,b))
        val savedR = r.toDouble() / 255
        val savedG = g.toDouble() / 255
        val savedB = b.toDouble() / 255

        avatarColor = "[$savedR, $savedG, $savedB, 1]"
    }
    fun createUserClicked(view: View) {

        val email = createUserEmailTxt.toString()
        val password = createUserPwTxt.text.toString()
        AuthService.registerUser(this,email,password){registerSuccess ->
            if(registerSuccess){
                AuthService.loginUser(this,email,password){loginSuccess ->
                    if(loginSuccess){

                    }

                }
            }

        }

    }

}
