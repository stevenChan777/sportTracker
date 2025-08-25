package com.example.myruns5

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import java.io.File

/**
 * Activity to manage user profile information including name, email, phone, gender, class year,
 * major, and profile photo.
 */
class Profile : AppCompatActivity() {
    private lateinit var profileImageView: ImageView
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var genderRadioGroup: RadioGroup
    private lateinit var classEditText: EditText
    private lateinit var majorEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var changePhotoButton: Button

    private lateinit var myViewModel: MyViewModel
    private lateinit var tempImgUri: Uri
    private val tempImgFileName = "temp_img.jpg"
    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private lateinit var sharedPreferences: SharedPreferences
    private val pref = "MyRunsPrefs"
    private var pathLine: String? = "..."
    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)

        val toolbar = findViewById<Toolbar>(R.id.myToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "MyRuns5"

        // Initializes and sets up view components from the layout.
        nameEditText = findViewById(R.id.ProfilenameEditText)
        emailEditText = findViewById(R.id.ProfileemailEditText)
        phoneEditText = findViewById(R.id.ProfilephoneEditText)
        genderRadioGroup = findViewById(R.id.ProfilegenderRadioGroup)
        classEditText = findViewById(R.id.ProfileclassEditText)
        majorEditText = findViewById(R.id.ProfilemajorEditText)
        saveButton = findViewById(R.id.ProfilesaveButton)
        cancelButton = findViewById(R.id.ProfilecancelButton)
        changePhotoButton = findViewById(R.id.ProfilechangePhotoButton)
        profileImageView = findViewById(R.id.profileImageView)
        textView = findViewById(R.id.Profiletext_view)
        sharedPreferences = getSharedPreferences(pref, Context.MODE_PRIVATE)

        val tempImgFile = File(getExternalFilesDir(null),
            tempImgFileName)
        tempImgUri = FileProvider.getUriForFile(this,
            "com.xd.MyRuns4", tempImgFile)

        // Handles the results from camera or gallery selection.
        changePhotoButton.setOnClickListener {
            showImagePickerOptions()
        }
        cameraResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val bitmap = Util.getBitmap(this, tempImgUri)
                myViewModel.userImage.value = bitmap
            }
        }

        // Sets up the ViewModel and observes changes to the user image.
        myViewModel = ViewModelProvider(this).get(MyViewModel::class.java)
        myViewModel.userImage.observe(this, { it ->
            profileImageView.setImageBitmap(it)
        })

        // Save any changes made
        saveButton.setOnClickListener {
            saveProfile()
            finish()
        }

        //Cancels any changes made and deletes the temporary image file if it exists.
        cancelButton.setOnClickListener {
            val tempImgFile = File(tempImgUri.path)
            if (tempImgFile.exists()) {
                tempImgFile.delete()
            }
            finish()
        }

        if(tempImgFile.exists()) {
            val bitmap = Util.getBitmap(this, tempImgUri)
            profileImageView.setImageBitmap(bitmap)
        }
        if (savedInstanceState != null) {
            textView.setText(pathLine)
        }
        loadProfile()
    }

    // Saves the user profile information to shared preferences.
    private fun saveProfile() {
        val editor = sharedPreferences.edit()
        editor.putString("name", nameEditText.text.toString())
        editor.putString("email", emailEditText.text.toString())
        editor.putString("phone", phoneEditText.text.toString())
        editor.putString("major", majorEditText.text.toString())

        val classYear = classEditText.text.toString().toIntOrNull()
        if (classYear != null) {
            editor.putInt("classYear", classYear)
        } else {
            editor.remove("classYear")
        }

        val selectedGenderId = genderRadioGroup.checkedRadioButtonId
        val gender = if (selectedGenderId == R.id.maleRadioButton) 1 else 0
        editor.putInt("gender", gender)

        editor.apply()
    }

    // Loads the user profile information from shared preferences.
    private fun loadProfile() {
        val name = sharedPreferences.getString("name", "")
        val email = sharedPreferences.getString("email", "")
        val phone = sharedPreferences.getString("phone", "")
        val classYear = if (sharedPreferences.contains("classYear")) {
            sharedPreferences.getInt("classYear", -1).toString()
        } else {
            ""
        }
        val major = sharedPreferences.getString("major", "")
        val gender = sharedPreferences.getInt("gender", -1)

        nameEditText.setText(name)
        emailEditText.setText(email)
        phoneEditText.setText(phone)
        classEditText.setText(classYear.toString())
        majorEditText.setText(major)

        when (gender) {
            0 -> genderRadioGroup.check(R.id.femaleRadioButton)
            1 -> genderRadioGroup.check(R.id.maleRadioButton)
            else -> genderRadioGroup.clearCheck()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (this::textView.isInitialized) {
            outState.putString("pathLine", textView.text.toString())
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if (this::textView.isInitialized) {
            textView.text = savedInstanceState.getString("pathLine", "")
        }
    }

    // Provides options to either open the camera or select an image from the gallery.
    private fun showImagePickerOptions() {
        val options = arrayOf("Open camera", "Select from gallery")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select profile image")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> takePhotoFromCamera()
                1 -> selectPhotoFromGallery()
            }
        }
        builder.show()
    }

    // Launches the camera to take a photo.
    private fun takePhotoFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, tempImgUri)
        cameraResult.launch(intent)
    }

    // Opens the gallery for the user to select an image.
    private fun selectPhotoFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        cameraResult.launch(intent)
    }
}

