package com.roasloa.smak.Controller.Controller

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.roasloa.smak.Controller.Services.AuthService
import com.roasloa.smak.R
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }


    fun loginLoginClicked(view: View){
        val email = loginEmailTxt.text.toString()
        val password = loginPwTxt.text.toString()

        AuthService.loginUser(this,email,password){loginSuccess->
            if(loginSuccess){
                AuthService.findUserByEmail(this){findSuccess->
                    if(findSuccess){
                        finish()
                    }
                }
            }

        }
    }


    fun loginCreateUserClicked(view: View){
        val createUserIntent = Intent(this, CreateUserActivity::class.java)
        startActivity(createUserIntent)
        finish()
    }

}
