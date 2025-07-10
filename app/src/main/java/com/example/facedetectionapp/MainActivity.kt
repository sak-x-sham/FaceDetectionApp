package com.example.facedetectionapp

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val faceBtn = findViewById<Button>(R.id.button)

        val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val bitmap = result.data?.extras?.get("data") as? Bitmap
                detectFace(bitmap)
            }
        }

        faceBtn.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 123 && resultCode == RESULT_OK) {
            val extras  = data?.extras
            val bitmap = extras?.get("data") as? Bitmap
            detectFace(bitmap)
        }
    }

    private fun detectFace(bitmap: Bitmap?) {
        if (bitmap == null) {
            Toast.makeText(this, "Bitmap is null", Toast.LENGTH_SHORT).show()
            return
        }

        val highAccuracyOpts = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()

        val detector = FaceDetection.getClient(highAccuracyOpts)
        val image = InputImage.fromBitmap(bitmap, 0)

        detector.process(image)
            .addOnSuccessListener { faces ->
                if (faces.isEmpty()) {
                    Toast.makeText(this, "No Face Detected!", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                var resultText = ""
                var i = 1
                for (face in faces) {
                    resultText += "Face #$i\n" +
                            "Smile: ${face.smilingProbability?.times(100)?.toDouble()?.format(2)}%" +
                            "Left Eye: ${face.leftEyeOpenProbability?.times(100)?.toDouble()?.format(2)}%\n" +
                            "Right Eye: ${face.rightEyeOpenProbability?.times(100)?.toDouble()?.format(2)}%\n\n"
                    i++
                }

//                Toast.makeText(this, resultText.trim(), Toast.LENGTH_LONG).show()
                AlertDialog.Builder(this)
                    .setTitle("Detection Result")
                    .setMessage(resultText.trim())
                    .setPositiveButton("OK", null)
                    .show()

            }
            .addOnFailureListener {
                Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun Double.format(digits: Int) = "%.${digits}f".format(this)

}