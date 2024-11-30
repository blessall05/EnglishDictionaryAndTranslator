package com.blessall05.translator.view.login

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.blessall05.translator.R
import com.blessall05.translator.databinding.ActivityLoginBinding
import com.blessall05.translator.model.AppData
import com.blessall05.translator.model.DatabaseHelper

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val databaseHelper = DatabaseHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val savedUser = AppData.getSavedUser()
        binding.username.setText(savedUser.first)
        binding.password.setText(savedUser.second)
        if (savedUser.second.isNotEmpty()) {
            binding.rememberPassword.isChecked = true
        }

        // 登录按钮
        binding.buttonLogin.setOnClickListener {
            binding.toolbar.title = getString(R.string.login)
            binding.passwordAgain.visibility = View.GONE
            binding.rememberPassword.visibility = View.VISIBLE
            val username = binding.username.text.toString()
            val password = binding.password.text.toString()
            if (username.isEmpty()) {
                Toast.makeText(
                    this,
                    getString(R.string.username_password_must_not_empty),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if (!databaseHelper.isUserExist(username)) { //用户名不存在
                Toast.makeText(
                    this, getString(R.string.user_not_exist), Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if (!databaseHelper.isPasswordCorrect(username, password)) {// 密码错误
                Toast.makeText(
                    this, getString(R.string.password_incorrect), Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if (binding.rememberPassword.isChecked) {
                AppData.saveUser(username, password)
            }
            Toast.makeText(
                this, getString(R.string.login_success, username), Toast.LENGTH_SHORT
            ).show()
            AppData.setUserId(databaseHelper.getUserId(binding.username.text.toString()))
            finish()
        }

        // 注册按钮
        binding.buttonRegister.setOnClickListener {
            binding.toolbar.title = getString(R.string.register)
            val username = binding.username.text.toString()
            val password = binding.password.text.toString()
            if (binding.passwordAgain.visibility != View.VISIBLE) { // 显示确认密码输入框
                binding.passwordAgain.visibility = View.VISIBLE
                binding.rememberPassword.visibility = View.GONE
                if (password.isNotEmpty()) {// 密码不为空时，将焦点移到确认密码输入框
                    binding.passwordAgain.requestFocus()
                    Toast.makeText(
                        this, getString(R.string.input_password_again), Toast.LENGTH_SHORT
                    ).show()
                }
                return@setOnClickListener
            } else {
                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(
                        this,
                        getString(R.string.username_password_must_not_empty),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                if (password != binding.passwordAgain.text.toString()) {
                    Toast.makeText(
                        this,
                        getString(R.string.password_not_same),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
            }
            if (databaseHelper.isUserExist(binding.username.text.toString())) { //用户名已存在
                Toast.makeText(this, getString(R.string.user_exist), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            databaseHelper.addUser(
                binding.username.text.toString(),
                binding.password.text.toString()
            )
            binding.passwordAgain.visibility = View.GONE
            binding.rememberPassword.visibility = View.VISIBLE
            Toast.makeText(this, getString(R.string.register_success), Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
