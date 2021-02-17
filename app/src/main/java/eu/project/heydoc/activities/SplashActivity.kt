package eu.project.heydoc.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import eu.project.heydoc.R
import eu.project.heydoc.firestore.FireStoreClass
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : BaseActivity() {

    /**
     * This function is auto created by Android when the Activity Class is created.
     */
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)


        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // place to change the font
        val typeface: Typeface =
            Typeface.createFromAsset(assets, "carbon bl.ttf")
        tv_app_name.typeface = typeface


        Handler().postDelayed({

            //val currentUserID = FireStoreClass().getCurrentUserId()
            //if (currentUserID != null){
                //startActivity(Intent(this@SplashActivity, MainActivity::class.java))
           //}else{
                startActivity(Intent(this@SplashActivity, IntroActivity::class.java))
            //}
            finish()
        }, 2500)
    }
}