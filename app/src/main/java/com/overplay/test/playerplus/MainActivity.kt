package com.overplay.test.playerplus

import android.Manifest
import android.annotation.SuppressLint
import android.hardware.*
import android.location.Location

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.overplay.test.playerplus.databinding.ActivityMainBinding
import com.squareup.seismic.ShakeDetector
import java.util.*

/**
 * Created by Josh Basarte 2022-02-12
 * Main Activity containing the Media Player object.
 */
class MainActivity : AppCompatActivity(), Orientation.Listener, ShakeDetector.Listener  {

    /** UI Binding object */
    private lateinit var binding: ActivityMainBinding
    /** Exoplayer instance */
    private val player by lazy(LazyThreadSafetyMode.NONE) {
        ExoPlayer.Builder(MainActivity@ this)
            .build()
    }
    /** Pitch / Yaw listener */
    private lateinit var mOrientation: Orientation
    /** Holds the value for the the orientatin of the phone when app launches (pitch) */
    var initialPitch : Float? = null
    lateinit var fusedLocationClient : FusedLocationProviderClient

    val builder = LocationSettingsRequest.Builder()
    private lateinit var locationCallback: LocationCallback
    var initalLocation : Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.videoView.player = player
        mOrientation = Orientation(this)

        val sensorManager =  getSystemService(SENSOR_SERVICE) as SensorManager;
        val sd = ShakeDetector(this);

        // A non-zero delay is required for Android 12 and up (https://github.com/square/seismic/issues/24)
        val sensorDelay = SensorManager.SENSOR_DELAY_GAME
        sd.start(sensorManager, sensorDelay)

        /* Listen for player state callbacks */
        player.addListener(object : Player.Listener{
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                Log.d("Tick", "Playback state changed $playbackState")
                if (playbackState == Player.STATE_READY) {
                    runTimer()
                }
            }
        })

        /* LOCATION STUFF */
        fusedLocationClient = FusedLocationProviderClient(this)
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            permissions[Manifest.permission.ACCESS_FINE_LOCATION]?.let {
                if (it) { // sanity check
                    startLocationUpdates()
                }
                /* IMPT!! This has to be called to start buffering */
                // Run this code here so that the media will buffer only after
                // the user has accepted or denied the location permissions.
                player.prepare()
            }
        }
        // TODO: Before you perform the actual permission request, check whether your app already has the permissions, and whether your app needs to show a permission
        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION))

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for (location in locationResult.locations){
                    // Update UI with location data
                    // ...i
                    if (initalLocation == null) {
                        initalLocation = location
                    }else {
                        val distanceTo = location.distanceTo(initalLocation)
                        Log.d("Tick" , "This is the location: $distanceTo")
                        if (distanceTo >= 10) {
                            initalLocation = location
                            player.playWhenReady = false
                            player.seekTo(0)
                            player.playWhenReady = true
                        }
                    }
                    break // Get the first result
                }
            }
        }
    }


    fun runTimer(){
        object : CountDownTimer(4000,1000){
            override fun onFinish() {
                /* Run video after countdown timer finishes */
                player.playWhenReady = true
                /* Hide UI */
                binding.tvCountDown.visibility = View.GONE
            }

            override fun onTick(p0: Long) {
                /* Inspired by the game :D */
                binding.tvCountDown.text = "${Math.ceil(p0.toDouble()/1000).toInt()}"
            }
        }.start()
    }

    override fun onResume() {
        super.onResume()
        /* Hide System UI */
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    override fun onStart() {
        super.onStart()
        initializePlayer() // start playing video
        mOrientation.startListening(this) // listen for pitch/yaw events
    }

    private fun initializePlayer() {
        val mediaItem = MediaItem
            .fromUri(
                "http://commondatastorage.googleapis.com/gtv-videos-bucket/" +
                        "sample/WeAreGoingOnBullrun.mp4"
            ) //  TODO: Load this from strings or something.

        player.setMediaItem(mediaItem)
    }

    override fun onStop() {
        super.onStop()
        // Free resources
        player.release()
        mOrientation.stopListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


//    var initialRoll : Float ? = null
    override fun onOrientationChanged(pitch: Float, roll: Float) {
        if (initialPitch == null)
            initialPitch = pitch
        else {
            adjustVolume(pitch)
        }

        adjustCurrentPlayingTimestamp(pitch, roll)
    }

    /**
     * Adjusts media volume depending on pitch delta of phone
     * from app launch
     * forward pitch = decrease volume
     * backward pitch = increese volume
     */
    private fun adjustVolume(pitch : Float) {
        val delta = (initialPitch!! -pitch)
        if (delta <= -0.6)
            player.volume = player.volume - 0.1f
        else if (delta >= 0.4)
            player.volume = player.volume + 0.1f
    }

    /**
     * This assume a 'sane' user with +/- 1 radian of roll on app launch,
     * otherwise the video will automatically seek forward/backward
     */
    private fun adjustCurrentPlayingTimestamp(pitch : Float, roll : Float) {
//        val delta = (initialRoll!! - roll)
        // Log.d("MAIN", "SEEK Delta $delta , ${roll}, ${player.currentPosition}")
        val delta = (initialPitch!! -pitch)
        if (delta > 0.5 || delta < -0.5)
            return
        if (roll <= -1)
            player.seekTo(player.currentPosition + 100)
        else if (roll >= 1)
            player.seekTo(player.currentPosition - 100)
    }

    override fun hearShake() {
        /* Pause video when 'shake' occurs */
        player.playWhenReady = false
    }

    /**
     * Location-based stuff
    **/

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        builder.addLocationRequest(createLocationRequest())
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener {
            // All location settings are satisfied. The client can initialize
            // location requests here.
            fusedLocationClient.requestLocationUpdates(createLocationRequest(),
                locationCallback,
                Looper.getMainLooper())
        }
    }

    private fun createLocationRequest() = LocationRequest.create()
            .apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
}