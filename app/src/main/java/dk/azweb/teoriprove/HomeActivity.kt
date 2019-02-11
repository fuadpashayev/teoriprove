package dk.azweb.teoriprove

import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.constraint.ConstraintLayout
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.question_layout.view.*
import kotlinx.android.synthetic.main.test_layout.view.*
import org.json.JSONObject
import java.util.*
import java.util.zip.Inflater
import kotlin.collections.ArrayList

class HomeActivity : AppCompatActivity() {
    lateinit var sharedpreferences: SharedPreferences
    val Preference = "session"
    var user:User?=null
    var category:CategoryModel?=null
    val manager = supportFragmentManager
    var openedFragment:String? = null
    val fragmentTag = "CATEGORY"
    var connectedMessage = true
    var isFromCategory = false
    var stopped = false
    lateinit var actionBar:ConstraintLayout
    override fun onPause() {
        super.onPause()
        stopped = true
    }
    override fun onResume() {
        super.onResume()
        if(stopped && openedFragment==null) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            this.finish()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        openedFragment = null
        supportActionBar!!.hide()
        sharedpreferences = getSharedPreferences(Preference, Context.MODE_PRIVATE)
        actionBar = ActionBar
        val backToStatistics = intent?.getBooleanExtra("backToStatistics",false)
        if(backToStatistics!!){
            val args = Bundle()
            args.putString("user_id", user?.id)
            StatisticsFragment().start(args)
        }
        val TOKEN = sharedpreferences.getString("token",null)
        if(!checkPermission())
            requestPermission()
        checkInternetConnection(networkStatus)
        Profile(this).getProfile(TOKEN,object:ServerCallback{
            override fun onSuccess(result: JSONObject?) {
                user = User(result)
                //name.text = user?.name
            }
            override fun onError(error: VolleyError) {
                logout()
            }
        })


        val items = arrayListOf("Categories","Statistics")
        val Items = items.toArray(arrayOfNulls<String>(items.size))
        openMenu.setOnClickListener {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("Menu")
            dialog.setCancelable(true)
            dialog.setItems(Items) { _ , item ->
                when(item){
                    0->{
                        CategoryFragment().start()
                    }
                    1->{
                        val args = Bundle()
                        args.putString("user_id", user?.id)
                        StatisticsFragment().start(args)
                    }
                }
            }
            dialog.create().show()
        }

        val url = "http://test.azweb.dk/api/category"
        Query(this).get(url,responseCallBack = object:ResponseCallBack{
            override fun onSuccess(response: String?) {
                val data = CategoryModel(response)
                val adapter = TestAdapter(data,this@HomeActivity,manager,this@HomeActivity,user)
                testList.adapter = adapter
            }

        })

    }





    fun logout(){
        val editor = sharedpreferences.edit()
        editor.remove("token")
        editor.apply()
    }


    fun Fragment.start(args:Bundle?=null){
        val transaction = manager.beginTransaction()
        val fragment = this
        if(args!=null) {
            args.putBoolean("fromCategory",false)
            fragment.arguments = args
        }
        var currentTag = manager!!.fragments.toString()
        currentTag = Regex(".*[\\[|,](.*)Fragment.*").replace(currentTag,"$1").trim()
        var newTag = fragment.toString()
        newTag = Regex("(.*)Fragment.*").replace(newTag,"$1")
        if(newTag != currentTag) {
            transaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
            transaction.replace(R.id.content, fragment,fragmentTag)
            transaction.addToBackStack(null)
            transaction.commit()
            openedFragment = newTag
        }

    }




//    override fun onBackPressed() {
//        when(openedFragment){
//            null -> return
//            "Statistics","Category","CategoryExaming","Examing","StatisticsFromExam"-> {
//                val intent = Intent(this,HomeActivity::class.java)
//                startActivity(intent)
//                this.finish()
//            }
//            "Exam"->{
//                val dialog = AlertDialog.Builder(this)
//                dialog.setTitle("Exit from exam")
//                dialog.setMessage("Are you sure to exit from exam?")
//                dialog.setNegativeButton(Html.fromHtml("<font color=\"#3F51B5\">Cancel</font>")) { _, _ ->  }
//                dialog.setPositiveButton(Html.fromHtml("<font color=\"#3F51B5\">Exit</font>")) { _, _ ->
//                    val intent = Intent(this@HomeActivity,HomeActivity::class.java)
//                    startActivity(intent)
//                    this.finish()
//                    openedFragment = null
//                    actionBar.show()
//                }
//                dialog.create().show()
//            }
//            "StatisticsView","StatisticsViewDetailed"-> {
//                val intent = Intent(this,HomeActivity::class.java)
//                intent.putExtra("backToStatistics",true)
//                startActivity(intent)
//                this.finish()
//            }
//        }
//    }

    override fun onBackPressed() {

    }
    fun View.show(){
        this.visibility = View.VISIBLE
    }
    fun View.hide(){
        this.visibility = View.GONE
    }


    fun checkInternetConnection(networkStatusText: TextView){
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                netWorkMessage(isInternetActive(),networkStatusText)
            }
        }, 0, 3000)
    }

    fun netWorkMessage(status:Boolean, networkStatusText: TextView){
        if(status){
            runOnUiThread {
                if(!connectedMessage) {
                    networkStatusText.text = "Connected"
                    networkStatusText.setBackgroundColor(resources.getColor(R.color.green))
                    networkStatusText.show()
                    connectedMessage = true
                    Handler().postDelayed({
                        networkStatus.hide()
                    }, 1500)
                }else{
                    networkStatusText.hide()
                }
            }

        }else{
            runOnUiThread {
                networkStatusText.text = "No Internet Connection"
                networkStatusText.setBackgroundColor(resources.getColor(R.color.red))
                networkStatusText.show()
                connectedMessage=false
            }
        }
    }
    fun isInternetActive():Boolean{
        val connection = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if(connection.getNetworkInfo(0).state == android.net.NetworkInfo.State.CONNECTED ||
                connection.getNetworkInfo(0).state == android.net.NetworkInfo.State.CONNECTING ||
                connection.getNetworkInfo(1).state == android.net.NetworkInfo.State.CONNECTING ||
                connection.getNetworkInfo(1).state == android.net.NetworkInfo.State.CONNECTED) {
            return true
        }else if(connection.getNetworkInfo(0).state == android.net.NetworkInfo.State.DISCONNECTED ||
                connection.getNetworkInfo(1).state == android.net.NetworkInfo.State.DISCONNECTED){
            return false
        }
        return false
    }


    fun checkPermission():Boolean{
        val phoneState = ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_PHONE_STATE)
        return phoneState == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermission(){
        ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.READ_PHONE_STATE),1)
    }
}

class TestAdapter(val data:CategoryModel,val context:Context,val manager:FragmentManager,val realActivity: HomeActivity,val user: User?):RecyclerView.Adapter<TestViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): TestViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.test_layout,parent,false)
        val cell = TestViewHolder(view)

        return cell
    }

    override fun getItemCount(): Int {
        return data.id!!.size
    }

    override fun onBindViewHolder(holder: TestViewHolder, position: Int) {
        val id = data.id!![position].toDouble().toInt()
        val image = data.image_url!![position]
        val name = data.name!![position]
        val testHolder = holder.itemView.openTest
        val imageHolder = holder.itemView.backgroundImage
        testHolder.testName.text = name

        Glide.with(context)
                .load(image)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageHolder)
        if(id==1)
            testHolder.freeText.visibility = View.VISIBLE
        else
            testHolder.freeText.visibility = View.GONE
        Log.d("-------user",user?.payment_type.toString()+" -")
        testHolder.openTest.setOnClickListener {
            if(id==1 || (user!=null && user.payment_type!="free")) {
               openExam()
            }else{
                val itemNames:ArrayList<String> = if(user!=null && user.payment_type=="free")
                    arrayListOf("Payment Plan","Continue with Free")
                else
                    arrayListOf("Sign In","Payment Plan","Continue with Free")

                val items = itemNames.toArray(arrayOfNulls<String>(itemNames.size))
                val dialog = AlertDialog.Builder(context)
                dialog.setTitle("Select to continue")
                dialog.setCancelable(true)
                dialog.setNegativeButton(Html.fromHtml("<font color=\"#CE2828\">Cancel</font>")){_,_->}
                dialog.setItems(items){_,item->
                    when (item) {
                        0 -> {
                            if(itemNames.size==3) openSign()
                            else openUrl()
                        }

                        1 -> {
                            if(itemNames.size==3) openUrl()
                            else openExam()
                        }

                        2 -> {
                           if(itemNames.size==3)
                               openExam()
                        }
                    }
                }
                dialog.create().show()

            }
        }

    }

    fun openExam(){
        val url = "http://test.azweb.dk/api/category/1"
        Query(context).get(url,responseCallBack = object:ResponseCallBack{
            override fun onSuccess(response: String?) {
                val args = Bundle()
                args.putString("data", response)
                ExamFragment().start(args)
                realActivity.ActionBar.visibility = View.GONE
            }
        })
    }

    fun openSign(){
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("loggedIn", false)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.applicationContext.startActivity(intent)
    }

    fun openUrl(){
        val urlString = "http://test.azweb.dk/pricing"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlString))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.`package` = "com.android.chrome"
        try {
            context.startActivity(intent)
        }
        catch (ex: ActivityNotFoundException) {
            intent.`package` = null
            context.startActivity(intent)
        }
    }

    fun Fragment.start(args:Bundle?=null){
        val transaction = manager.beginTransaction()
        val fragment = this
        if(args!=null) {
            args.putBoolean("fromCategory",false)
            fragment.arguments = args
        }
        var currentTag = manager.fragments.toString()
        currentTag = Regex(".*[\\[|,](.*)Fragment.*").replace(currentTag,"$1").trim()
        var newTag = fragment.toString()
        newTag = Regex("(.*)Fragment.*").replace(newTag,"$1")
        if(newTag != currentTag) {
            transaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
            transaction.replace(R.id.content, fragment,newTag)
            transaction.addToBackStack(null)
            transaction.commit()
            realActivity.openedFragment = newTag

        }

    }

}
class TestViewHolder(v:View):RecyclerView.ViewHolder(v)
