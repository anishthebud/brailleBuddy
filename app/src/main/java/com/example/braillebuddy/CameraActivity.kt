package com.example.braillebuddy

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CameraActivity : AppCompatActivity() {

    private lateinit var cameraImage: ImageView
    private lateinit var captureImageBtn : Button
    private lateinit var textView : TextView

    private lateinit var takePictureLauncher : ActivityResultLauncher<Uri>

    private var currentPhotoPath: String? = null

    private lateinit var requestPermissionsLauncher : ActivityResultLauncher<String>


    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        cameraImage =  findViewById(R.id.cameraPreview)
        captureImageBtn = findViewById(R.id.captureButton)

        requestPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            isGranted ->
            if (isGranted) {
                captureImage()
            } else {
                Toast.makeText(this, "CameraPermission", Toast.LENGTH_SHORT).show()
            }
        }

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) {
            success ->
            if(success){
                currentPhotoPath?.let { path ->
                    val bitmap = BitmapFactory.decodeFile(path)
                    cameraImage.setImageBitmap(bitmap)
                    recognizeText(bitmap)
                }
            }
        }

        captureImageBtn.setOnClickListener {
            requestPermissionsLauncher.launch(android.Manifest.permission.CAMERA)
        }


    }

    private fun createImageFile() : File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply{
            currentPhotoPath = absolutePath
        }


    }

    private fun captureImage(){
        val photoFile : File? = try {
            createImageFile()
        } catch(ex: IOException) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            null
        }
        photoFile?.also {
            val photoUri: Uri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", it)
            takePictureLauncher.launch(photoUri)
        }
    }

    private fun recognizeText(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image).addOnSuccessListener {
            ocrText ->
            textView.text = ocrText.text
        }

    }


}