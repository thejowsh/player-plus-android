package com.overplay.test.playerplus

import android.hardware.*
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.overplay.test.playerplus.databinding.ActivityMainBinding
import java.util.*


/**
 * Created by Josh Basarte 2022-02-12
 * Main Activity containing the Media Player object.
 */
class MainActivity : AppCompatActivity(), Orientation.Listener {

    /** UI Binding object */
    private lateinit var binding: ActivityMainBinding
    /** Exoplayer instance */
    private val player by lazy(LazyThreadSafetyMode.NONE) {
        ExoPlayer.Builder(MainActivity@ this)
            .build()
    }
    /** Pitch / Yaw listener */
    private lateinit var mOrientation: Orientation


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.videoView.player = player
        mOrientation = Orientation(this)
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
        player.playWhenReady = true // TODO: Per requirement, call play() after 4s after media is load
    }


    override fun onStop() {
        super.onStop()
        player.release()
        mOrientation.stopListening()
    }

    var initialPitch : Float? = null
    override fun onOrientationChanged(pitch: Float, roll: Float) {
        if (initialPitch == null)
            initialPitch = pitch
        else {
            adjustVolume(pitch)
        }
    }

    private fun adjustVolume(pitch : Float) {
        val delta = (initialPitch!! -pitch)
        Log.d("MAIN", "Delta $delta , ${player.volume}")
        if (delta <= -0.4)
            player.volume = player.volume - 0.01f
        else if (delta >= 0.4)
            player.volume = player.volume + 0.01f
    }
}