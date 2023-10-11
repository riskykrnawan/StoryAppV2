package com.example.storyapp.ui.register

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.storyapp.R
import com.example.storyapp.data.Result
import com.example.storyapp.databinding.ActivityRegisterBinding
import com.example.storyapp.helper.SessionPreferences
import com.example.storyapp.helper.Utils
import com.example.storyapp.helper.ViewModelFactory
import com.example.storyapp.ui.login.LoginActivity
import com.google.android.material.snackbar.Snackbar

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sessions")

class RegisterActivity : AppCompatActivity() {

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityRegisterBinding.inflate(layoutInflater)
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            setMyButtonEnable()
        }

        override fun afterTextChanged(s: Editable?) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        playAnimation()

        // viewModel
        val pref = SessionPreferences.getInstance(dataStore)
        val factory = ViewModelFactory.getInstance(this@RegisterActivity.application, pref)
        val registerViewModel: RegisterViewModel by viewModels { factory }

        binding.btnLoginNow.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
        binding.btnRegister.setOnClickListener {
            registerViewModel.register(
                binding.edRegisterName.text.toString(),
                binding.edRegisterEmail.text.toString(),
                binding.edRegisterPassword.text.toString()
            ).observe(this) { result ->
                when (result) {
                    is Result.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }

                    is Result.Success -> {
                        binding.progressBar.visibility = View.GONE
                        Snackbar.make(
                            binding.contextView, R.string.registration_success, Snackbar.LENGTH_LONG
                        ).setAction(R.string.login) {
                            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(intent)
                            finish()
                        }.show()
                    }

                    is Result.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Snackbar.make(
                            binding.contextView,
                            getString(R.string.email_is_invalid, result.error),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }

                    else -> {}
                }
            }
        }
        binding.edRegisterEmail.addTextChangedListener(textWatcher)
        binding.edRegisterPassword.addTextChangedListener(textWatcher)
    }

    private fun setMyButtonEnable() {
        val email = binding.edRegisterEmail.text.toString()
        val password = binding.edRegisterPassword.text.toString()
        binding.btnRegister.isEnabled = Utils.isValidForm(email, password)
    }

    private fun playAnimation() {
        val ivLogo = ObjectAnimator.ofFloat(binding.ivLogo, View.ALPHA, 1f).setDuration(300)
        val btnRegisterNow =
            ObjectAnimator.ofFloat(binding.btnLoginNow, View.ALPHA, 1f).setDuration(300)
        val name =
            ObjectAnimator.ofFloat(binding.textInputLayoutName, View.ALPHA, 1f).setDuration(300)
        val email =
            ObjectAnimator.ofFloat(binding.textInputLayoutEmail, View.ALPHA, 1f).setDuration(300)
        val password =
            ObjectAnimator.ofFloat(binding.textInputLayoutPassword, View.ALPHA, 1f).setDuration(300)
        val btnLogin = ObjectAnimator.ofFloat(binding.btnRegister, View.ALPHA, 1f).setDuration(300)

        AnimatorSet().apply {
            playSequentially(ivLogo, btnRegisterNow, name, email, password, btnLogin)
            start()
        }
    }
}