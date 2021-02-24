package eu.project.heydoc.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import eu.project.heydoc.R
import eu.project.heydoc.firestore.FireStoreClass
import eu.project.heydoc.models.User
import kotlinx.android.synthetic.main.activity_sign_up.*


class SignUpActivity : BaseActivity() {
    private lateinit var auth: FirebaseAuth
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_sign_up)
        auth = FirebaseAuth.getInstance()

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )


        setupActionBar()
    }

    private fun setupActionBar() {

        setSupportActionBar(toolbar_sign_up_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        toolbar_sign_up_activity.setNavigationOnClickListener { onBackPressed() }
        btn_sign_up.setOnClickListener { registerUser() }
    }

    private fun registerUser(){
        val name : String = et_name.text.toString().trim{ it <= ' '}
        val email : String = et_email.text.toString().trim{ it <= ' '}
        val password : String = et_password.text.toString().trim{ it <= ' '}

        if(validateForm(name,email,password)){
            showProgressDialog(resources.getString(R.string.please_wait))
            auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener {
                task ->

                if (task.isSuccessful){
                    val firebaseUser : FirebaseUser = task.result!!.user!!
                    val registeredEmail = firebaseUser.email!!
                    val user = User(firebaseUser.uid,name,registeredEmail)
                    FireStoreClass().registerUser(this,user)
                }else {
                    Log.w("SignUp", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(this, "Nie udało się zalożyć konta", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun userRegisteredSuccess(){
        Toast.makeText(this, "Udana rejestracja, miłego korzystania :) ", Toast.LENGTH_SHORT).show()
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()
    }

    private fun validateForm(name: String, email:String, password:String):Boolean{
        return when{
            TextUtils.isEmpty(name) -> {showErrorSnackBar(resources.getString(R.string.please_enter_name))
                false
            }
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
