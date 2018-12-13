package dk.azweb.teoriprove

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.os.Handler



class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Handler().postDelayed({
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        },1000)

    }
}
