package com.pizza.giros.burger.goodstreetfood.Activiy

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.pizza.giros.burger.goodstreetfood.R

class LoginActivity : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var EnterTextView: TextView
    private lateinit var EnterTextView2: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_acitivity)
        FirebaseApp.initializeApp(this)

        // Инициализация Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Получение ссылок на элементы макета
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        EnterTextView = findViewById(R.id.EnterTextView)
        EnterTextView2 = findViewById(R.id.enterTextView2)

        EnterTextView.setOnClickListener {
            loginUser()
        }

        EnterTextView2.setOnClickListener {
            // Переход на активность регистрации (например, RegisterActivity)
            startActivity(Intent(this, RegisterActivity::class.java))
        }

    }
    override fun onStart() {
        super.onStart()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Пользователь уже залогинен, переход к MainActivity
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }
    }

    private fun loginUser() {
        val email = editTextEmail.text.toString()
        val password = editTextPassword.text.toString()

        // Проверка, что email и пароль не пустые
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Введите email и пароль", Toast.LENGTH_SHORT).show()
            return
        }

        // Использование Firebase Authentication для входа пользователя
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Вход успешен
                    val user: FirebaseUser? = auth.currentUser
                    if (task.isSuccessful) {
                        // Вход успешен
                        if (user != null) {

                            // Проверка для суперпользователя
                            val superUserEmail = "xchaos228@gmail.com"
                            if (email == superUserEmail) {
                                // Если входит суперпользователь, переход на GPSActivity
                                startActivity(Intent(this, MainActivity::class.java))
                            } else {
                                // Вход для обычного пользователя
                                startActivity(Intent(this, MainActivity::class.java))
                            }
                            finish()
                        }
                    }
                } else {
                    // Пользователь не существует или неверный пароль
                    Toast.makeText(this, "Неверный Email или пароль", Toast.LENGTH_SHORT).show()
                }
            }
    }


}