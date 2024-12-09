package com.teta.quimlab.ui.inicio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.net.Uri
import android.os.Bundle
import android.util.Size
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.teta.quimlab.databinding.FragmentInicioBinding
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

class InicioFragment : Fragment() {

    private var _binding: FragmentInicioBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraManager: CameraManager
    private lateinit var cameraDevice: CameraDevice
    private lateinit var cameraCaptureSession: CameraCaptureSession
    private lateinit var previewSize: Size
    private lateinit var imageReader: ImageReader

    private val cameraStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            startCameraPreview()
        }

        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(context, "Permissão para usar a câmera é necessária", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher para seleção de imagem da galeria
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            Toast.makeText(context, "Foto selecionada: $uri", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInicioBinding.inflate(inflater, container, false)
        val root: View = binding.root

        cameraManager = requireContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager

        // Solicitar permissão de câmera
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            openCamera()
        }

        binding.captureButton.setOnClickListener {
            capturePhoto()
        }

        binding.galleryButton.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        return root
    }

    private fun openCamera() {
        try {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val cameraId = cameraManager.cameraIdList.first()
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                previewSize = map?.getOutputSizes(SurfaceTexture::class.java)?.first() ?: Size(640, 480)

                imageReader = ImageReader.newInstance(
                    previewSize.width, previewSize.height, ImageFormat.JPEG, 2
                ).apply {
                    setOnImageAvailableListener({ reader ->
                        val image = reader.acquireLatestImage()
                        val buffer: ByteBuffer = image.planes[0].buffer
                        val bytes = ByteArray(buffer.remaining())
                        buffer.get(bytes)
                        savePhoto(bytes)
                        image.close()
                    }, null)
                }

                cameraManager.openCamera(cameraId, cameraStateCallback, null)
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(context, "Erro de segurança ao acessar a câmera", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Erro ao abrir a câmera", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCameraPreview() {
        val textureView = binding.cameraPreview
        val surfaceTexture = textureView.surfaceTexture
        if (surfaceTexture == null) {
            Toast.makeText(context, "SurfaceTexture não está pronto", Toast.LENGTH_SHORT).show()
            return
        }

        // Ajustar proporção do TextureView para corresponder ao tamanho do preview
        adjustAspectRatio(textureView, previewSize.width, previewSize.height)

        surfaceTexture.setDefaultBufferSize(previewSize.width, previewSize.height)
        val surface = Surface(surfaceTexture)

        val captureRequestBuilder =
            cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                addTarget(surface)
            }

        cameraDevice.createCaptureSession(
            listOf(surface, imageReader.surface),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    cameraCaptureSession = session
                    cameraCaptureSession.setRepeatingRequest(
                        captureRequestBuilder.build(),
                        null,
                        null
                    )
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Toast.makeText(context, "Erro ao configurar a câmera", Toast.LENGTH_SHORT).show()
                }
            },
            null
        )
    }

    private fun capturePhoto() {
        val captureRequestBuilder =
            cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
                addTarget(imageReader.surface)
            }

        cameraCaptureSession.capture(
            captureRequestBuilder.build(),
            object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    super.onCaptureCompleted(session, request, result)
                    Toast.makeText(context, "Foto capturada!", Toast.LENGTH_SHORT).show()
                }
            },
            null
        )
    }

    private fun savePhoto(bytes: ByteArray) {
        val file = File(requireContext().filesDir, "captured_photo.jpg")
        FileOutputStream(file).use { output ->
            output.write(bytes)
            Toast.makeText(context, "Foto salva: ${file.absolutePath}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun adjustAspectRatio(textureView: TextureView, previewWidth: Int, previewHeight: Int) {
        val layoutParams = textureView.layoutParams
        val viewWidth = textureView.width
        val viewHeight = textureView.height

        val aspectRatio = previewHeight.toFloat() / previewWidth
        if (viewWidth > viewHeight * aspectRatio) {
            layoutParams.width = (viewHeight * aspectRatio).toInt()
            layoutParams.height = viewHeight
        } else {
            layoutParams.width = viewWidth
            layoutParams.height = (viewWidth / aspectRatio).toInt()
        }

        textureView.layoutParams = layoutParams
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::cameraDevice.isInitialized) {
            cameraDevice.close()
        }
        _binding = null
    }
}
