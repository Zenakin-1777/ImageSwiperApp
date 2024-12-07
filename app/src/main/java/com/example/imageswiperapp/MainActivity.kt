package com.example.imageswiperapp

import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var currentImageView: ImageView
    private lateinit var settingsButton: ImageButton
    private lateinit var leftStack: LinearLayout
    private lateinit var rightStack: LinearLayout
    private lateinit var undoButton: ImageButton
    private var currentImageIndex = 0
    private val images = mutableListOf<File>()
    private var lastSwipedImage: File? = null
    private var lastSwipedDirection: String? = null

    private lateinit var sourceFolder: File
    private lateinit var goodFolder: File
    private lateinit var badFolder: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI components
        currentImageView = findViewById(R.id.currentImageView)
        settingsButton = findViewById(R.id.settingsButton)
        leftStack = findViewById(R.id.leftStack)
        rightStack = findViewById(R.id.rightStack)
        undoButton = findViewById(R.id.undoButton)

        // Set up storage directories
        setupDirectories()

        // Load images
        loadImages()

        // Display the first image
        displayImage()

        // Undo button functionality
        undoButton.setOnClickListener {
            undoLastSwipe()
        }
    }

    private fun setupDirectories() {
        // Create folders in the app's private storage
        val appStorage = filesDir
        sourceFolder = File(appStorage, "Source").apply { if (!exists()) mkdir() }
        goodFolder = File(appStorage, "Good").apply { if (!exists()) mkdir() }
        badFolder = File(appStorage, "Bad").apply { if (!exists()) mkdir() }
    }

    private fun loadImages() {
        // Clear and reload images from the Source folder
        images.clear()
        sourceFolder.listFiles { file -> file.isFile && file.extension in listOf("jpg", "jpeg", "png") }?.let {
            images.addAll(it)
        }
        if (images.isEmpty()) {
            Toast.makeText(this, "No images found in the Source folder", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayImage() {
        if (images.isNotEmpty()) {
            val currentFile = images[currentImageIndex]
            Glide.with(this).load(currentFile).into(currentImageView)
        } else {
            currentImageView.setImageResource(R.drawable.placeholder_image) // Placeholder image
        }
    }

    private fun moveImageToFolder(imageFile: File, targetFolder: File) {
        try {
            val targetFile = File(targetFolder, imageFile.name)
            if (imageFile.renameTo(targetFile)) {
                Toast.makeText(this, "Image moved to ${targetFolder.name}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to move image", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error moving file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleSwipe(direction: String) {
        if (images.isNotEmpty()) {
            val currentFile = images[currentImageIndex]
            val targetFolder = if (direction == "left") badFolder else goodFolder

            // Move image to target folder
            moveImageToFolder(currentFile, targetFolder)

            // Save last swipe info for undo
            lastSwipedImage = currentFile
            lastSwipedDirection = direction

            // Remove the image from the list and update index
            images.removeAt(currentImageIndex)
            currentImageIndex %= images.size
            displayImage()
        }
    }

    private fun undoLastSwipe() {
        lastSwipedImage?.let { imageFile ->
            val originalFolder = when (lastSwipedDirection) {
                "left" -> sourceFolder
                "right" -> sourceFolder
                else -> null
            }
            originalFolder?.let {
                moveImageToFolder(imageFile, it)
                images.add(currentImageIndex, imageFile) // Add image back to the list
                displayImage()
            }
        }
    }
}
