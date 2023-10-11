package com.example.storyapp.ui.add_story

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.storyapp.R
import com.example.storyapp.data.Result
import com.example.storyapp.databinding.ActivityAddStoryBinding
import com.example.storyapp.helper.SessionPreferences
import com.example.storyapp.helper.Utils.rotateFile
import com.example.storyapp.helper.Utils.uriToFile
import com.example.storyapp.helper.ViewModelFactory
import com.example.storyapp.ui.home.HomeActivity
import com.example.storyapp.ui.login.LoginActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.File


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("sessions")

class AddStoryActivity : AppCompatActivity() {
    private var _activityAddStoryBinding: ActivityAddStoryBinding? = null
    private val binding get() = _activityAddStoryBinding

    private var getFile: File? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lat: Float? = null
    private var lon: Float? = null

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            setMyButtonEnable()
        }

        override fun afterTextChanged(s: Editable?) {}
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERA_X_RESULT) {
            val myFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.data?.getSerializableExtra("picture", File::class.java)
            } else {
                @Suppress("DEPRECATION") it.data?.getSerializableExtra("picture")
            } as? File

            val isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) as Boolean

            myFile?.let { file ->
                rotateFile(file, isBackCamera)
                getFile = file
                binding?.ivPreview?.setImageBitmap(BitmapFactory.decodeFile(file.path))
            }
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg = result.data?.data as Uri
            selectedImg.let { uri ->
                val myFile = uriToFile(uri, this@AddStoryActivity)
                getFile = myFile
                binding?.ivPreview?.setImageURI(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _activityAddStoryBinding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        playAnimation()

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        // viewModel
        val pref = SessionPreferences.getInstance(dataStore)
        val factory = ViewModelFactory.getInstance(this@AddStoryActivity.application, pref)
        val addStoryViewModel: AddStoryViewModel by viewModels { factory }

        checkToken(pref)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setSupportActionBar(binding?.toolbarMain)
        supportActionBar?.title = getString(R.string.create_story)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding?.buttonCamera?.setOnClickListener {
            val intent = Intent(this@AddStoryActivity, CameraActivity::class.java)
            launcherIntentCameraX.launch(intent)
        }

        binding?.buttonGallery?.setOnClickListener {
            val intent = Intent()
            intent.action = ACTION_GET_CONTENT
            intent.type = "image/*"
            val chooser = Intent.createChooser(intent, "Choose a Picture")
            launcherIntentGallery.launch(chooser)
        }

        binding?.switchLocation?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                getMyLastLocation()
            }
        }

        binding?.edAddDescription?.addTextChangedListener(textWatcher)

        if (binding?.buttonAdd?.isEnabled == true) {
            binding?.buttonAdd?.setOnClickListener {
                getFile?.let { file ->
                    addStoryViewModel.postStory(
                        file, binding?.edAddDescription?.text.toString(), lat, lon
                    ).observe(this) { result ->
                        when (result) {
                            is Result.Loading -> {
                                binding?.progressBar?.visibility = View.VISIBLE
                            }

                            is Result.Success -> {
                                binding?.progressBar?.visibility = View.GONE
                                val intent = Intent(this@AddStoryActivity, HomeActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                startActivity(intent)
                                finish()
                            }

                            is Result.Error -> {
                                binding?.progressBar?.visibility = View.GONE
                                Toast.makeText(
                                    this,
                                    getString(R.string.error) + result.error,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkToken(pref: SessionPreferences) {
        val token = runBlocking { pref.getToken().first() }
        if (token == "") {
            startActivity(Intent(this@AddStoryActivity, LoginActivity::class.java))
            finish()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this, getString(R.string.permission_not_granted), Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setMyButtonEnable() {
        val description = binding?.edAddDescription?.text.toString()
        binding?.buttonAdd?.isEnabled = getFile != null && description.isNotEmpty()
    }

    override fun onResume() {
        super.onResume()
        setMyButtonEnable()
    }

    private fun playAnimation() {
        val tvPreview = ObjectAnimator.ofFloat(binding?.tvPreview, View.ALPHA, 1f).setDuration(300)
        val ivPreview = ObjectAnimator.ofFloat(binding?.ivPreview, View.ALPHA, 1f).setDuration(300)
        val buttonCamera =
            ObjectAnimator.ofFloat(binding?.buttonCamera, View.ALPHA, 1f).setDuration(300)
        val buttonGallery =
            ObjectAnimator.ofFloat(binding?.buttonGallery, View.ALPHA, 1f).setDuration(300)
        val switchLocation =
            ObjectAnimator.ofFloat(binding?.switchLocation, View.ALPHA, 1f).setDuration(300)
        val edAddDescription =
            ObjectAnimator.ofFloat(binding?.textInputLayoutDescription, View.ALPHA, 1f)
                .setDuration(300)
        val buttonAdd = ObjectAnimator.ofFloat(binding?.buttonAdd, View.ALPHA, 1f).setDuration(300)

        AnimatorSet().apply {
            val previewTogether = AnimatorSet().apply {
                playTogether(tvPreview, ivPreview)
            }
            val btnTogether = AnimatorSet().apply {
                playTogether(buttonCamera, buttonGallery, switchLocation)
            }

            playSequentially(previewTogether, btnTogether, edAddDescription, buttonAdd)
            start()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                // Precise location access granted.
//                    getMyLastLocation()
            }

            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                // Only approximate location access granted.
//                    getMyLastLocation()
            }
        }
    }

    private fun getMyLastLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) && checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            fusedLocationClient.lastLocation.addOnSuccessListener {
                if (it != null) {
                    lat = it.latitude.toFloat()
                    lon = it.longitude.toFloat()
                } else {
                    Toast.makeText(
                        this@AddStoryActivity,
                        "Location is not found. Try Again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this, permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val CAMERA_X_RESULT = 200
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}