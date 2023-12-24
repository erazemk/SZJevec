package eu.erazem.szjevec

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import eu.erazem.szjevec.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var activityMainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the content view to the new layout
        setContentView(R.layout.activity_main2)

        // Find the Start button and set its click listener
        val startButton = findViewById<Button>(R.id.startButton)
        startButton.setOnClickListener {
            // Handle the button click to show the camera layout
            showCameraLayout()
        }

        val stopButton = findViewById<Button>(R.id.stopButton)
        stopButton.setOnClickListener {
            hideCameraLayout()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    // Method to show the camera layout
    private fun showCameraLayout() {
        // Find the FrameLayout
        val cameraLayout = findViewById<FrameLayout>(R.id.camera_fragment)

        // Inflate the "fragment_camera.xml" layout
        val inflater = LayoutInflater.from(this)
        val fragmentCameraView = inflater.inflate(R.layout.fragment_camera, null)

        // Clear existing views in the cameraLayout
        cameraLayout.removeAllViews()

        // Add the views from the fragment_camera.xml layout to the cameraLayout
        cameraLayout.addView(fragmentCameraView)

        // Set the visibility of the cameraLayout to VISIBLE
        cameraLayout.visibility = View.VISIBLE
        val startButton = findViewById<Button>(R.id.startButton)
        startButton.visibility = View.INVISIBLE

        val stopButton = findViewById<Button>(R.id.stopButton)
        stopButton.visibility = View.VISIBLE
    }

    private fun hideCameraLayout() {
        // Find the FrameLayout
        val cameraLayout = findViewById<FrameLayout>(R.id.camera_fragment)

        // Inflate the "fragment_camera.xml" layout
        val inflater = LayoutInflater.from(this)
        val fragmentCameraView = inflater.inflate(R.layout.fragment_camera, null)

        // Clear existing views in the cameraLayout
        cameraLayout.removeAllViews()

        // Add the views from the fragment_camera.xml layout to the cameraLayout
        cameraLayout.addView(fragmentCameraView)

        // Set the visibility of the cameraLayout to VISIBLE
        cameraLayout.visibility = View.INVISIBLE
        val startButton = findViewById<Button>(R.id.startButton)
        startButton.visibility = View.VISIBLE

        val stopButton = findViewById<Button>(R.id.stopButton)
        stopButton.visibility = View.INVISIBLE
    }
}
