package dk.azweb.teoriprove

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.View
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_home.*
import org.json.JSONObject

class HomeActivity : AppCompatActivity() {
    lateinit var sharedpreferences: SharedPreferences
    val Preference = "session"
    var user:User?=null
    var category:CategoryModel?=null
    val manager = supportFragmentManager
    var openedFragment = false
    val fragmentTag = "CATEGORY"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        openedFragment = false
        supportActionBar!!.hide()
        sharedpreferences = getSharedPreferences(Preference, Context.MODE_PRIVATE)
        val TOKEN = sharedpreferences.getString("token",null)

        Profile(this).getProfile(TOKEN,object:ServerCallback{
            override fun onSuccess(result: JSONObject?) {
                user = User(result)
                //name.text = user?.name
            }
            override fun onError(error: VolleyError) {
                logout()
            }
        })

        logout.setOnClickListener {
            logout()
        }

        categories.setOnClickListener {
            CategoryFragment().start()
        }

        startTest.setOnClickListener {
            ExamFragment().start()
        }

    }




    fun logout(){
        val editor = sharedpreferences.edit()
        editor.remove("token")
        editor.apply()
        finish()
    }


    fun Fragment.start(){
        val transaction = manager.beginTransaction()
        val fragment = this
        var currentTag = manager!!.fragments.toString()
        currentTag = Regex(".*[\\[|,](.*)Fragment.*").replace(currentTag,"$1").trim()
        var newTag = fragment.toString()
        newTag = Regex("(.*)Fragment.*").replace(newTag,"$1")
        if(newTag != currentTag) {
            transaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
            transaction.replace(R.id.content, fragment,fragmentTag)
            transaction.addToBackStack(null)
            transaction.commit()
            openedFragment = true
        }

    }




    override fun onBackPressed() {
        if(openedFragment)
            super.onBackPressed()
        openedFragment = false
    }

    fun View.show(){
        this.visibility = View.VISIBLE
    }
    fun View.hide(){
        this.visibility = View.GONE
    }
}
