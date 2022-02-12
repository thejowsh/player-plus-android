package com.overplay.test.playerplus

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.WindowCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.overplay.test.playerplus.databinding.ActivityMainBinding

/**
 * Created by Josh Basarte 2022-02-12
 * Main Activity containing the Media Player object.
 */
class MainActivity : AppCompatActivity() {

    private val player by lazy(LazyThreadSafetyMode.NONE) {
        ExoPlayer.Builder(MainActivity@this)
            .build()
    }

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.videoView.player = player
    }

    override fun onResume() {
        super.onResume()
        /* Hide System UI */
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    private fun initializePlayer() {
        val mediaItem = MediaItem
            .fromUri("http://commondatastorage.googleapis.com/gtv-videos-bucket/"+
                    "sample/WeAreGoingOnBullrun.mp4") //  TODO: Load this from strings or something.

        player.setMediaItem(mediaItem)
        player.playWhenReady = true // TODO: Per requirement, call play() after 4s after media is loaded
    }


    override fun onStop() {
        super.onStop()
        player.release()
    }
}