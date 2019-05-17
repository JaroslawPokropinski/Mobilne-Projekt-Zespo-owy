package com.example.mobilneprojekt

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.mobilneprojekt.services.ServiceBuilder
import com.example.mobilneprojekt.services.UserCredentialsDTO
import com.example.mobilneprojekt.services.UserDTO
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.user_registration.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences = getSharedPreferences("com.herokuapp.mobilne-projekt", Context.MODE_PRIVATE)
        if (preferences.getString("token","") != ""){
            reroute()
            return
        }
        setContentView(R.layout.activity_main)

        usernameLog.visibility = View.VISIBLE
        passLog.visibility = View.VISIBLE
        buttonLog.visibility = View.VISIBLE
        regLog.visibility = View.VISIBLE

        regLog.setOnClickListener {
            showRegisterLayout()
        }

        registerUserBt.setOnClickListener {
            val user = UserDTO(name.text.toString(), surname.text.toString(), dateOfBirth.text.toString(), pesel.text.toString(), username.text.toString(), password.text.toString())
            val call = ServiceBuilder.getUserService().register(user)
            call.enqueue(object : Callback<Void>{
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("OCL","Fail to register User!")
                }

                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    showLoginLayout()
                }

            })


        }

        buttonLog.setOnClickListener {
            val user = UserCredentialsDTO(usernameLog.text.toString(), passLog.text.toString())
            val call = ServiceBuilder.getUserService().login(user)
            call.enqueue(object : Callback<String>{
                override fun onFailure(call: Call<String>, t: Throwable) {
                    fail()
                }

                override fun onResponse(call: Call<String>, response: Response<String>) {
                    reroute()
                }
            })

        }

    }

    private fun showRegisterLayout(){
        registration_layout.visibility = View.VISIBLE
        usernameLog.visibility = View.GONE
        passLog.visibility = View.GONE
        buttonLog.visibility = View.GONE
        regLog.visibility = View.GONE
    }

    private fun showLoginLayout(){
        registration_layout.visibility = View.GONE
        usernameLog.visibility = View.VISIBLE
        passLog.visibility = View.VISIBLE
        buttonLog.visibility = View.VISIBLE
        regLog.visibility = View.VISIBLE
    }

    fun fail(){
        Toast.makeText(this,"Either Username or Password are invalid!", Toast.LENGTH_SHORT).show()
    }

    fun reroute(){
        Toast.makeText(this, "Logged In", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, ListOfCars::class.java)
        startActivity(intent)
    }
}

// Rafal Marcin Mlodzieniak
//MM


//Z mojej strony to tyle - Andrzej


//VK