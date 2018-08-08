package com.matchmore.colorapp

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.*
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import io.matchmore.sdk.Matchmore
import io.matchmore.sdk.api.models.Publication
import io.matchmore.sdk.api.models.Subscription
import java.util.HashMap

class MainActivity : AppCompatActivity() {

    private val API_KEY = "YOUR-API-KEY-HERE";
    private val TAG = "result"
    private val TOPIC = "test_color"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!Matchmore.isConfigured())
        Matchmore.config(this,API_KEY, false);

        val range = findViewById(R.id.range) as SeekBar
        val submit = findViewById(R.id.submit) as Button
        var range_progress = range.progress;

        val colorPicker = findViewById(R.id.color_picker) as Spinner
        val cl = arrayListOf<String>()
        cl.add("Yellow")
        cl.add("Blue")
        cl.add("Red")
        val adapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, cl)
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
        colorPicker!!.setAdapter(adapter)


        setupDevice()
        submit.setOnClickListener{
            addPub()
        }

        addSub()
        checkLocationPermission()
    }

    private fun setupDevice() {
        var lastColor = "Orange";
        val l = findViewById(R.id.mainLayout) as ConstraintLayout

        Matchmore.instance.apply {
                // Start fetching matches
                matchMonitor.addOnMatchListener { matches, _ ->
                    Log.i(TAG, "Matches found: ${matches.size}")

                    matches.map { match ->
                        val c1 = match.publication!!.properties["color"].toString()
                        Log.i(TAG, "Color: ${c1}")

                            //lastColor = m[m.size - 1].properties["color"].toString()
                            if (c1 == "Red") {
                                l.setBackgroundColor(Color.RED)
                            }
                            if (c1 == "Yellow") {
                                l.setBackgroundColor(Color.YELLOW)
                            }
                            if (c1 == "Blue") {
                                l.setBackgroundColor(Color.BLUE)
                            }
                        }
                    }
                    matchMonitor.startPollingMatches()
                }

        }

    fun addPub() {
        Matchmore.instance.apply {
            val publication = Publication(TOPIC, 200.0, 600.0)
            val map = HashMap<String, Any>()
            val colorPicker = findViewById(R.id.color_picker) as Spinner
            map["color"] = colorPicker.getSelectedItem()
            publication.properties = map
            createPublicationForMainDevice(publication, { result ->
                Log.i(TAG, "Publication created ${result.topic}")
                Log.d(TAG, "Publication's properties ${result.properties}")
            }, Throwable::printStackTrace)
        }
    }

    fun addSub() {
        Matchmore.instance.apply {
            val subscription = Subscription(TOPIC, 200.0, 600.0)
            // subscription.selector = "color = 'red'"
                createSubscriptionForMainDevice(subscription, { result ->
                    Log.i(TAG, "Subscription created ${result.topic}")
                }, Throwable::printStackTrace)
        }
    }

    private fun checkLocationPermission() {
        val permissionListener = object : PermissionListener {
            @SuppressLint("MissingPermission")
            override fun onPermissionGranted() {
                Matchmore.instance.apply {
                    startUpdatingLocation()
                    startRanging()
                }
            }

            override fun onPermissionDenied(deniedPermissions: ArrayList<String>) {
                Toast.makeText(this@MainActivity, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setDeniedMessage("Permission denied")
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
                .check()
    }
}
