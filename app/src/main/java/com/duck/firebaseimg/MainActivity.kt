package com.duck.firebaseimg

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.duck.firebaseimg.databinding.ActivityMainBinding
import com.google.firebase.storage.FirebaseStorage
import java.io.File


class MainActivity : AppCompatActivity() {

    /*
    //enable viewBinding in app build.gradle(app)
    // it replaces findViewById
    buildFeatures{
    viewBinding = true
    }
 */
    private lateinit var binding: ActivityMainBinding
    private lateinit var ds: FirebaseStorage
    private lateinit var fileuri: Uri
    private lateinit var lastfileReference: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // default file from firebase storage
        lastfileReference = "logoFinal.jpg"

        val getFileFromUser = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                // Handle the returned Uri
                if (uri != null) {
                upload(uri)
                }
         }

        binding.browseFileButton.setOnClickListener {

            downlaodLastFile(lastfileReference)
        }

        binding.uploadButton.setOnClickListener {
            // to get images only use
            //getFileFromUser.launch("image/*")
            getFileFromUser.launch("*/*")
        }
    }

    private fun downlaodLastFile(lastfileReference: String) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val fileRef = storageRef.child(lastfileReference)

        val indexDot = lastfileReference.lastIndexOf(".")
        val index = lastfileReference.indexOf('/')
        val filename = if (index != -1) lastfileReference.substring(index+1, indexDot) else lastfileReference.split(".")[0]
        val extension = lastfileReference.split(".").last()

        binding.txtResults.append("\nls: "+lastfileReference+"\n")
        binding.txtResults.append(filename+"\n")
        binding.txtResults.append(extension+"\n")


        val localFile = File.createTempFile(filename, extension)

        fileRef.getFile(localFile).addOnSuccessListener {
            // File downloaded successfully
            Toast.makeText(applicationContext,"download failed!!!",Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(applicationContext,"download failed!!!",Toast.LENGTH_SHORT).show()
            binding.txtResults.append(it.toString())
        }

    }

    private fun displayImage(imageUri:Uri , imageView: ImageView){
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        val maxSize = 400 // Maximum size of the image in dp

        val width = bitmap.width
        val height = bitmap.height
        val aspectRatio = width.toFloat() / height.toFloat()

        val layoutParams = imageView.layoutParams
        layoutParams.width = maxSize
        layoutParams.height = (maxSize / aspectRatio).toInt()
        imageView.layoutParams = layoutParams

        imageView.setImageBitmap(bitmap)
    }

    private fun getFileExtension(fileName: String): String? {
        val dotIndex = fileName.lastIndexOf(".")
        return if (dotIndex > 0) {
            fileName.substring(dotIndex)
        } else {
            null
        }
    }


    private fun upload(fileuri:Uri){
        // TODO: initialise ds, get reference, putfile
        val file = File(fileuri.path)
        val fileName = file.name
        val mimetype = contentResolver.getType(fileuri)
        //val extension = getFileExtension(fileName)
        /*
        var sampleString = mimetype.toString()
        val index = sampleString.indexOf('/')
        val substring = if (index != -1) sampleString.substring(0, index) else sampleString
        */
        val uploadToFolder = mimetype.toString().split("/")[0]
        val extension = mimetype.toString().split("/").last()
        lastfileReference = "${uploadToFolder}/${fileName}__${System.currentTimeMillis()}.${extension}"

        binding.txtResults.append(fileuri.lastPathSegment)
        binding.txtResults.append("\n"+mimetype)
        binding.txtResults.append("\nfile uri : \n")
        binding.txtResults.append(fileuri.toString())
        binding.txtResults.append("\nname: "+fileName.toString())

        if (mimetype!!.startsWith("image/")){
            displayImage(fileuri,binding.imageView)
        }

        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val fileRef = storageRef.child(lastfileReference)

        val uploadTask = fileRef.putFile(fileuri)

        uploadTask.addOnSuccessListener {
            // Handle successful upload
            Toast.makeText(this, "Upload Successful", Toast.LENGTH_SHORT).show()
        }

        uploadTask.addOnFailureListener {
            // Handle failed upload
            Toast.makeText(this, "Upload Failed", Toast.LENGTH_SHORT).show()
        }
    }



}



    // ////  useless code below ***********************************************************************

    /*
    private fun uploadImage_DontUse() {
        Toast.makeText(applicationContext,"upload",Toast.LENGTH_SHORT).show()
        try {
            val inputStream = contentResolver.openInputStream(imgUri)
            val myBitmap = BitmapFactory.decodeStream(inputStream)
            val stream = ByteArrayOutputStream()
            myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            val bytes = stream.toByteArray()
            sImage = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
            binding.imageView.setImageBitmap(myBitmap)
            inputStream!!.close()



        }catch (ex:java.lang.Exception){
            Toast.makeText(this,ex.message.toString(),Toast.LENGTH_SHORT)
        }

    }

     */
/* its deprecated so use new method as done in this.
    private fun selectImage() {
        val intent = Intent()
        intent.type ="image/ *"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent,100)

    }



    // Receiver
    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == Activity.RESULT_OK){
                val value = it.data?.getStringExtra("input")
            }
        }

 */
