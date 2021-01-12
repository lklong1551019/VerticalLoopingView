package com.example.verticalloopingview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.verticalloopingview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var mainActivityBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainActivityBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainActivityBinding.root)

        val comments = mutableListOf<String>()
        for (i in 0..10) {
            comments.add("Comment $i")
        }
        mainActivityBinding.loopingView.setData(comments, 0)
    }
}