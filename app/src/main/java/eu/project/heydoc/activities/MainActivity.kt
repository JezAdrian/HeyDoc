package eu.project.heydoc.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.projemanag.activities.BaseActivity
import eu.project.heydoc.R

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}