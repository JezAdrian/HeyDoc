package eu.project.heydoc.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import eu.project.heydoc.R
import eu.project.heydoc.firestore.FireStoreClass
import eu.project.heydoc.models.User
import kotlinx.android.synthetic.main.activity_sign_in.*



class SignInActivity : BaseActivity() {
    private lateinit var auth: FirebaseAuth
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_sign_in)
        auth = FirebaseAuth.getInstance()

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )


        setupActionBar()
    }

    fun signInSuccess(user: User){
        hideProgressDialog()
        startActivity(Intent(this,MainActivity::class.java))
        finish()

    }


    private fun setupActionBar() {

        setSupportActionBar(toolbar_sign_in_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        toolbar_sign_in_activity.setNavigationOnClickListener { onBackPressed() }
        btn_loggin.setOnClickListener { loginUser() }
    }


    private fun loginUser(){
        val email : String = et_log_email.text.toString().trim{ it <= ' '}
        val password : String = et_log_password.text.toString().trim{ it <= ' '}
        if(validateForm(email,password)){
            showProgressDialog(resources.getString(R.string.please_wait))
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) {
                        task ->
                        hideProgressDialog()
                        if (task.isSuccessful) {
                            FireStoreClass().signInUser(this)
                        } else {
                            Log.w("Loggin", "createUserWithEmail:failure", task.exception)
                            Toast.makeText(this, "Logowanie nieudane.", Toast.LENGTH_SHORT).show()
                        }

                    }
        }}

    private fun validateForm( email:String, password:String):Boolean{
        return when{
            TextUtils.isEmpty(email) -> {showErrorSnackBar(resources.getString(R.string.please_enter_email))
                false
            }
            TextUtils.isEmpty(password) -> {showErrorSnackBar(resources.getString(R.string.please_enter_password))
                false
            }else ->{
                true
            }

        }
    }
}
