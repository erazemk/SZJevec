package eu.erazem.szjevec.fragment

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import eu.erazem.szjevec.GestureRecognizerHelper
import eu.erazem.szjevec.MainViewModel
import eu.erazem.szjevec.OverlayView
import eu.erazem.szjevec.R
import eu.erazem.szjevec.databinding.FragmentCameraBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CameraFragment : Fragment(), GestureRecognizerHelper.GestureRecognizerListener {

    private var _fragmentCameraBinding: FragmentCameraBinding? = null
    private var defaultNumResults = 1
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraFacing = CameraSelector.LENS_FACING_BACK
    private var isCameraRunning = false

    private lateinit var backgroundExecutor: ExecutorService
    private lateinit var gestureRecognizerHelper: GestureRecognizerHelper

    private val viewModel: MainViewModel by activityViewModels()
    private val gestureRecognizerResultAdapter: GestureRecognitionAdapter by lazy {
        GestureRecognitionAdapter().apply {
            updateAdapterSize(defaultNumResults)
        }
    }
    private val fragmentCameraBinding
        get() = _fragmentCameraBinding!!

    override fun onResume() {
        super.onResume()
        if (!PermissionFragment.hasPermissions(requireContext())) {
            Navigation.findNavController(requireActivity(), R.id.fragment_container)
                .navigate(R.id.action_camera_to_permissions)
        }

        backgroundExecutor.execute {
            if (gestureRecognizerHelper.isClosed()) {
                gestureRecognizerHelper.setupGestureRecognizer()
            }
        }

        if (isVisible) {
            startCamera()
        }
    }

    override fun onPause() {
        super.onPause()
        if (this::gestureRecognizerHelper.isInitialized) {
            viewModel.setMinHandDetectionConfidence(gestureRecognizerHelper.minHandDetectionConfidence)
            viewModel.setMinHandTrackingConfidence(gestureRecognizerHelper.minHandTrackingConfidence)
            viewModel.setMinHandPresenceConfidence(gestureRecognizerHelper.minHandPresenceConfidence)
            viewModel.setDelegate(gestureRecognizerHelper.currentDelegate)
            backgroundExecutor.execute { gestureRecognizerHelper.clearGestureRecognizer() }
        }
        stopCamera()
    }

    override fun onDestroyView() {
        _fragmentCameraBinding = null
        super.onDestroyView()

        backgroundExecutor.shutdown()
        backgroundExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentCameraBinding = FragmentCameraBinding.inflate(inflater, container, false)
        return fragmentCameraBinding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        backgroundExecutor = Executors.newSingleThreadExecutor()

        with(fragmentCameraBinding.recyclerviewResults) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = gestureRecognizerResultAdapter
        }

        fragmentCameraBinding.viewFinder.post { startCamera() }

        backgroundExecutor.execute {
            gestureRecognizerHelper = GestureRecognizerHelper(
                context = requireContext(),
                runningMode = RunningMode.LIVE_STREAM,
                minHandDetectionConfidence = viewModel.currentMinHandDetectionConfidence,
                minHandTrackingConfidence = viewModel.currentMinHandTrackingConfidence,
                minHandPresenceConfidence = viewModel.currentMinHandPresenceConfidence,
                currentDelegate = viewModel.currentDelegate,
                gestureRecognizerListener = this
            )
        }

        val buttonLeft = view.findViewById<ImageView>(R.id.buttonTopLeft)
        val overlayInfo = view.findViewById<View>(R.id.overlayInfo)
        val buttonClose = overlayInfo.findViewById<ImageView>(R.id.buttonClose)
        val buttonRight = view.findViewById<ImageView>(R.id.buttonTopRight)
        val overlayAlphabet = view.findViewById<View>(R.id.overlayAlphabet)
        val buttonCloseAlphabet = overlayAlphabet.findViewById<ImageView>(R.id.buttonClose)
        val buttonSwitch = view.findViewById<ImageView>(R.id.buttonSwitchCamera)
        val overlayView = view.findViewById<OverlayView>(R.id.overlay)
        overlayView.setCameraFacingFront(isCameraFacingFront())

        buttonLeft.setOnClickListener {
            overlayInfo.visibility = View.VISIBLE
            buttonRight.visibility = View.GONE
            buttonLeft.visibility = View.GONE
            buttonSwitch.visibility = View.GONE
        }

        buttonRight.setOnClickListener {
            overlayAlphabet.visibility = View.VISIBLE
            buttonRight.visibility = View.GONE
            buttonLeft.visibility = View.GONE
            buttonSwitch.visibility = View.GONE
        }

        buttonClose.setOnClickListener {
            overlayInfo.visibility = View.GONE
            buttonLeft.visibility = View.VISIBLE
            buttonRight.visibility = View.VISIBLE
            buttonSwitch.visibility = View.VISIBLE
        }

        buttonCloseAlphabet.setOnClickListener {
            overlayAlphabet.visibility = View.GONE
            buttonLeft.visibility = View.VISIBLE
            buttonRight.visibility = View.VISIBLE
            buttonSwitch.visibility = View.VISIBLE
        }

        buttonSwitch.setOnClickListener {
            cameraFacing = if (cameraFacing == CameraSelector.LENS_FACING_BACK) {
                CameraSelector.LENS_FACING_FRONT
            } else {
                CameraSelector.LENS_FACING_BACK
            }
            overlayView.setCameraFacingFront(isCameraFacingFront())
            rebindCamera()
        }

    }

    private fun isCameraFacingFront(): Boolean {
        return cameraFacing == CameraSelector.LENS_FACING_BACK
    }

    private fun rebindCamera() {
        cameraProvider?.unbindAll()
        bindCameraUseCases()
    }

    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(
            {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases()
            }, ContextCompat.getMainExecutor(requireContext())
        )
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {

        val cameraProvider =
            cameraProvider ?: throw IllegalStateException("Camera initialization failed.")
        val cameraSelector = CameraSelector.Builder().requireLensFacing(cameraFacing).build()
        preview = Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
            .build()

        imageAnalyzer = ImageAnalysis.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(fragmentCameraBinding.viewFinder.display.rotation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
            .also { it.setAnalyzer(backgroundExecutor) { image -> recognizeHand(image) } }

        cameraProvider.unbindAll()

        try {
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            preview?.setSurfaceProvider(fragmentCameraBinding.viewFinder.surfaceProvider)
        } catch (_: Exception) {
        }
    }

    private fun recognizeHand(imageProxy: ImageProxy) {
        gestureRecognizerHelper.recognizeLiveStream(imageProxy = imageProxy)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        imageAnalyzer?.targetRotation = fragmentCameraBinding.viewFinder.display.rotation
    }

    override fun onResults(
        resultBundle: GestureRecognizerHelper.ResultBundle
    ) {
        activity?.runOnUiThread {
            if (_fragmentCameraBinding != null) {
                val gestureCategories = resultBundle.results.first().gestures()
                if (gestureCategories.isNotEmpty()) {
                    gestureRecognizerResultAdapter.updateResults(gestureCategories.first())
                } else {
                    gestureRecognizerResultAdapter.updateResults(emptyList())
                }

                fragmentCameraBinding.overlay.setResults(
                    resultBundle.results.first(),
                    resultBundle.inputImageHeight,
                    resultBundle.inputImageWidth,
                    RunningMode.LIVE_STREAM
                )

                fragmentCameraBinding.overlay.invalidate()
            }
        }
    }

    override fun onError(error: String, errorCode: Int) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            gestureRecognizerResultAdapter.updateResults(emptyList())

            if (errorCode == GestureRecognizerHelper.GPU_ERROR) {
                BaseOptions.builder().setDelegate(Delegate.CPU)
            }
        }
    }

    private fun startCamera() {
        isCameraRunning = true
        setUpCamera()
    }

    private fun stopCamera() {
        isCameraRunning = false
        cameraProvider?.unbindAll()
    }

}
