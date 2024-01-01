package eu.erazem.szjevec

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import eu.erazem.szjevec.databinding.ActivityMainBinding
import eu.erazem.szjevec.fragment.CameraFragment

class MainActivity : AppCompatActivity() {

    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var navController: NavController

    private lateinit var startButton: Button
    private lateinit var stopButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        // Initialize NavController
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.navController

        // Find the Start button and set its click listener
        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)

        startButton.setOnClickListener {
            // Navigate to CameraFragment when the Start button is clicked
            navController.navigate(R.id.camera_fragment)
            stopButton.visibility = View.VISIBLE
            startButton.visibility = View.INVISIBLE
        }

        // Find the Stop button and set its click listener
        stopButton.setOnClickListener {
            // Navigate back to the previous fragment or handle it accordingly
            navController.navigateUp()
            stopButton.visibility = View.INVISIBLE
            startButton.visibility = View.VISIBLE
        }

        // Initially hide the Start button and show the Stop button
        startButton.visibility = View.INVISIBLE
        stopButton.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}

