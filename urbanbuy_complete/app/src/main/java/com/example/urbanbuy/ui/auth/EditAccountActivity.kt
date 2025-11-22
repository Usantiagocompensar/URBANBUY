package com.example.urbanbuy.ui.auth

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.urbanbuy.databinding.ActivityEditAccountBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class EditAccountActivity : AppCompatActivity() {

    private lateinit var b: ActivityEditAccountBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityEditAccountBinding.inflate(layoutInflater)
        setContentView(b.root)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Mostrar email actual
        b.etEmail.setText(user.email)

        // Guardar cambios
        b.btnSave.setOnClickListener {
            val newEmail = b.etEmail.text.toString().trim()
            val newPass = b.etPassword.text.toString().trim()
            val currentPass = b.etCurrentPassword.text.toString().trim()

            // Verificar reautenticación
            if (currentPass.isEmpty()) {
                Toast.makeText(this, "Ingresa tu contraseña actual", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val credential = EmailAuthProvider.getCredential(user.email!!, currentPass)

            user.reauthenticate(credential).addOnCompleteListener { reauth ->
                if (reauth.isSuccessful) {

                    // Cambiar correo
                    if (newEmail.isNotEmpty()) {
                        user.updateEmail(newEmail).addOnSuccessListener {
                            Toast.makeText(this, "Correo actualizado", Toast.LENGTH_SHORT).show()
                        }
                    }

                    // Cambiar contraseña
                    if (newPass.isNotEmpty()) {
                        user.updatePassword(newPass).addOnSuccessListener {
                            Toast.makeText(this, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                        }
                    }

                } else {
                    Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // ELIMINAR CUENTA
        b.btnDeleteAccount.setOnClickListener {
            val currentPass = b.etCurrentPassword.text.toString().trim()

            if (currentPass.isEmpty()) {
                Toast.makeText(this, "Ingresa tu contraseña actual para eliminar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val credential = EmailAuthProvider.getCredential(user.email!!, currentPass)

            user.reauthenticate(credential).addOnCompleteListener { reauth ->
                if (reauth.isSuccessful) {
                    user.delete().addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(this, "Cuenta eliminada", Toast.LENGTH_SHORT).show()
                            auth.signOut()
                            finish()
                        } else {
                            Toast.makeText(this, "Error: ${it.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

