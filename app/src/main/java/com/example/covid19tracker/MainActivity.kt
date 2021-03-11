package com.example.covid19tracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import androidx.lifecycle.LiveData
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    lateinit var stateAdapter: StateAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        // Inflate header for COVID 19 cases from item_header.xml
        list.addHeaderView(LayoutInflater.from(this).inflate(R.layout.item_header, list, false))
        fetchResults()
    }
    // ROBO POJO Generator
    private fun fetchResults() {
        GlobalScope.launch {
            val response = withContext(Dispatchers.IO)
            { Client.api.execute() }
            if(response.isSuccessful) {
                val data = Gson().fromJson(response.body?.string(), Response::class.java)
                launch(Dispatchers.Main) {
                    bindCombinedData(data.statewise[0])
                    bindStateWiseData(data.statewise.subList(1,data.statewise.size))
                }
            }
        }
    }
    // Function for combining data statewise in LisView
    private fun bindStateWiseData(subList: List<StatewiseItem>) {
        stateAdapter = StateAdapter(subList)
        list.adapter = stateAdapter
    }
    // Function for combining all the data in the container
    private fun bindCombinedData(data: StatewiseItem?) {
        val lastUpdatedTime: String? = data?.lastupdatedtime
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        val lastUpdatedTv = findViewById<TextView>(R.id.lastUpdatedTv)
        lastUpdatedTv.text = "Last Updated\n ${getTimeAgo(simpleDateFormat.parse(lastUpdatedTime))}"
        val confirmedTv = findViewById<TextView>(R.id.confirmedTv)
        val activeTv = findViewById<TextView>(R.id.activeTv)
        val recoveredTv = findViewById<TextView>(R.id.recoveredTv)
        val deceasedTv = findViewById<TextView>(R.id.deceasedTv)

        if (data != null) {
            confirmedTv.text = data.confirmed
        }
        if (data != null) {
            activeTv.text = data.active
        }
        if (data != null) {
            recoveredTv.text = data.recovered
        }
        if (data != null) {
            deceasedTv.text = data.deaths
        }


    }
    // Function for time format
    fun getTimeAgo(past: Date): String {
        val now = Date()
        val seconds = TimeUnit.MILLISECONDS.toSeconds(now.time - past.time)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(now.time - past.time)
        val hours = TimeUnit.MILLISECONDS.toHours(now.time - past.time)

        return when {
            seconds < 60 -> {
                "Few seconds ago"
            }
            minutes < 60 -> {
                "$minutes minutes ago"
            }
            hours < 24 -> {
                "$hours hours ${minutes % 60} min ago"
            }
            else -> {
                SimpleDateFormat("dd/MM/yy, hh:mm a").format(past).toString()
            }
        }
    }
}