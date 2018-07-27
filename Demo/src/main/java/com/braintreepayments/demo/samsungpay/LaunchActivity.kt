package com.braintreepayments.demo.samsungpay

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View

class LaunchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)
    }

    fun launchJavaDemo(v: View) {
        val intent = Intent(this, MainActivity::class.java)

        startActivity(intent)
    }

    fun launchKotlinDemo(v: View) {
        val intent = Intent(this, MainKotlinActivity::class.java)

        startActivity(intent)
    }
}