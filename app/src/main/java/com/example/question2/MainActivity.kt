package com.example.question2

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.question2.databinding.ActivityMainBinding
import com.example.question2.ui.auth.LoginActivity
import com.example.question2.ui.scanner.ScannerFragment
import com.example.question2.ui.viewmodel.AuthViewModel
import com.example.question2.ui.vocabulary.VocabularyFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        if (savedInstanceState == null) {
            replaceFragment(ScannerFragment())
        }

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_scanner -> {
                    replaceFragment(ScannerFragment())
                    true
                }
                R.id.nav_vocabulary -> {
                    replaceFragment(VocabularyFragment())
                    true
                }
                else -> false
            }
        }

        observeAuthStatus()
    }

    private fun observeAuthStatus() {
        authViewModel.userEmail.observe(this) { email ->
            binding.toolbar.subtitle = if (email.isNullOrBlank()) "Guest Mode" else "Logged in: $email"
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sign_out -> {
                authViewModel.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}