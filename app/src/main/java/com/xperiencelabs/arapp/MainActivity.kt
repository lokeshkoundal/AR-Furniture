package com.xperiencelabs.arapp

import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.ar.core.Config
import com.google.ar.core.AugmentedImage
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.AugmentedImageNode
import io.github.sceneview.node.VideoNode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation

class MainActivity : AppCompatActivity() {
    
    private lateinit var sceneView: ArSceneView
    private lateinit var videoNode: VideoNode
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var placeButton: ExtendedFloatingActionButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        sceneView = findViewById<ArSceneView>(R.id.sceneView).apply {
            this.lightEstimationMode = Config.LightEstimationMode.DISABLED
        }
        placeButton = findViewById(R.id.place)
        
        placeButton.setOnClickListener {
            mediaPlayer.start()
        }
        
        // ✅ Load the target image from assets
        val bitmap = BitmapFactory.decodeStream(assets.open("copy.jpg")) // Replace with your image file
        
        // ✅ Initialize MediaPlayer for video playback
        mediaPlayer = MediaPlayer.create(this, R.raw.ad)
        
        // ✅ Create Augmented Image Node (Detects Target Photo Frame)
        val imageNode = AugmentedImageNode(
            engine = sceneView.engine,
            imageName = "copy.jpg",  // Name should match the Augmented Image Database
            bitmap = bitmap,             // Image to track
            widthInMeters = 0.3f,        // Adjust based on the real-world size of the frame
        ).apply {
            onUpdate = { node, augmentedImage ->
                if (augmentedImage.trackingMethod == AugmentedImage.TrackingMethod.FULL_TRACKING) {
                    placeVideo(node,augmentedImage)
                }
            }
            onError = { e->
                Log.e("AugmentedImageNode", e.toString(), e)
                mediaPlayer.pause()
            }
        }
        
        // ✅ Add image node to AR Scene
        sceneView.addChild(imageNode)
    }
    
    // ✅ Places the video on the detected photo frame
    private fun placeVideo(imageNode: AugmentedImageNode, augmentedImage: AugmentedImage) {
        if (!::videoNode.isInitialized) {
            videoNode = VideoNode(
                engine = sceneView.engine,
                scaleToUnits = 0.5f, // Adjust size
                centerOrigin = Position(y = 0f, x = 0f,z = 0f), // Adjust placement
                glbFileLocation = "models/plane.glb", // Flat surface model for video
                player = mediaPlayer,
                onLoaded = { _, _ ->
                    mediaPlayer.start()
                }
            )
            
            
            imageNode.addChild(videoNode) // Attach video to the detected image frame
        }
    }
    
    override fun onPause() {
        super.onPause()
        mediaPlayer.pause()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}
