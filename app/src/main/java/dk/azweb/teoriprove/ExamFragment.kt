package dk.azweb.teoriprove

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.fragment_exam.view.*
import kotlinx.android.synthetic.main.question_layout.view.*
import android.support.v7.widget.LinearLayoutManager
import android.telephony.TelephonyManager
import android.text.Html
import android.widget.*
import com.android.volley.VolleyError
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.fragment_exam.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap


class ExamFragment : Fragment() {
    lateinit var realActivity:HomeActivity
    lateinit var DEVICE_ID:String
    lateinit var phoneManager:TelephonyManager
    @SuppressLint("MissingPermission")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view =  inflater.inflate(R.layout.fragment_exam, container, false)
        realActivity = (activity as HomeActivity)
        phoneManager = context!!.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        DEVICE_ID = phoneManager.deviceId
        val data = this.arguments!!.getString("data")
        val isFromCategory = this.arguments!!.getBoolean("fromCategory")
        if(isFromCategory)
            realActivity.isFromCategory = true
        val question = QuestionModel(data)
        val list = view.questionList
        val layoutManager = listLayoutManager(context!!)
        val manager = fragmentManager
        layoutManager.setScrollEnabled(false)
        layoutManager.setDirection()
        list.layoutManager = layoutManager
        list.adapter = QuestionAdapter(question,context!!,object:QuestionAdapter.DataLoadedListener{
            override fun onLoadFinished() {
                Handler().postDelayed({
                    loader.visibility = View.GONE
                },500)
            }

        },list,view.examHeader,realActivity,activity!!,view.finishExamSession,manager!!,isFromCategory)




        view.backButton.setOnClickListener {
            val dialog = AlertDialog.Builder(activity)
            dialog.setTitle("Exit from exam")
            dialog.setMessage("Are you sure to exit from exam?")
            dialog.setNegativeButton(Html.fromHtml("<font color=\"#3F51B5\">Cancel</font>")) { _, _ ->  }
            dialog.setPositiveButton(Html.fromHtml("<font color=\"#3F51B5\">Exit</font>")) { _, _ ->
                val intent = Intent(activity,HomeActivity::class.java)
                startActivity(intent)
                activity?.finish()
                activity?.overridePendingTransition(R.anim.slide_in,R.anim.slide_out)
            }
            dialog.create().show()

        }



        return view
    }


    inner class listLayoutManager(context: Context) : LinearLayoutManager(context) {
        private var isScrollEnabled = true
        private var HORIZONTAL = LinearLayoutManager.HORIZONTAL
        private var VERTICAL = LinearLayoutManager.VERTICAL

        fun setScrollEnabled(flag: Boolean) {
            this.isScrollEnabled = flag
        }

        override fun canScrollVertically(): Boolean {
            return isScrollEnabled && super.canScrollVertically()
        }

        override fun canScrollHorizontally(): Boolean {
            return isScrollEnabled && super.canScrollHorizontally()
        }

        fun setDirection(orientation: Int = this.HORIZONTAL) {
            this.orientation = orientation
        }

    }




    class QuestionAdapter(val datas:QuestionModel, val context:Context, private val listener:DataLoadedListener, private val list:RecyclerView, val examHeader:TextView,val realActivity: HomeActivity,val activity: FragmentActivity,val finishExamSession:ImageView,val manager:FragmentManager,val isFromCategory:Boolean):RecyclerView.Adapter<QuestionViewHolder>(){
        val session = HashMap<String,Boolean>()
        val result = HashMap<String,Boolean>()
        val session_id:String = generateSessionId()
        var user:User?=null
        lateinit var DEVICE_ID:String
        lateinit var phoneManager:TelephonyManager
        lateinit var sharedpreferences: SharedPreferences
        var question_list = datas.question_list
        val Preference = "session"
        lateinit var examHolder:View
        var ended = false
        @SuppressLint("MissingPermission")
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val cell = inflater.inflate(R.layout.question_layout,parent,false)
            phoneManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            sharedpreferences = context.getSharedPreferences(Preference, Context.MODE_PRIVATE)
            val TOKEN = sharedpreferences.getString("token",null)
            DEVICE_ID = phoneManager.deviceId


            Profile(context).getProfile(TOKEN){
                    user = User(it)
            }

            finishExamSession.setOnClickListener {
                finishExam(viewType)
            }
            return QuestionViewHolder(cell)
        }

        fun Fragment.start(args:Bundle?=null){
            val transaction = manager.beginTransaction()
            val fragment = this
            if(args!=null)
                fragment.arguments = args
            var currentTag = manager.fragments.toString()
            currentTag = Regex(".*[\\[|,](.*)Fragment.*").replace(currentTag,"$1").trim()
            var newTag = fragment.toString()
            newTag = Regex("(.*)Fragment.*").replace(newTag,"$1")
            transaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
            transaction.replace(R.id.content, fragment,newTag)
            transaction.addToBackStack(null)
            transaction.commit()
            realActivity.openedFragment = newTag
            realActivity.actionBar.visibility = View.GONE



        }

        override fun getItemCount(): Int {
            return datas.id!!.size
        }

        override fun getItemViewType(position: Int): Int {
            return position
        }


        override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
            val header = "Question ${position+1}"
            examHeader.text = header
            val id = datas.id!![position]
            val text = datas.text!![position]
            val image = datas.image_url!![position]
            val audio = datas.audio_url!![position]
            val questions:ArrayList<HashMap<String,String>?> = ArrayList()
            var Question1:HashMap<String,String>? = null
            var Question2:HashMap<String,String>? = null
            var Question3:HashMap<String,String>? = null
            var Question4:HashMap<String,String>? = null
            for(sub in datas.sub_id[id]!!.iterator()){
                val index = datas.sub_id[id]!!.indexOf(sub)
                val sub_id = datas.sub_id[id]!![index]
                val sub_text = datas.sub_text[id]!![index]
                val sub_audio_url = datas.sub_audio_url[id]!![index]
                val question = hashMapOf("id" to sub_id,"text" to sub_text,"audio" to sub_audio_url)
                questions.add(question)
            }
            Question1 = questions[0]!!
            Question2 = questions[1]!!
            if(questions.size>2)
                Question3 = questions[2]!!
            if(questions.size>3)
                Question4 = questions[3]!!

            examHolder = holder.itemView
            val listened = arrayListOf<String>()


            examHolder.questionText.text = text
            Handler().postDelayed({
                Glide.with(context)
                        .load(image)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(examHolder.image)
                examHolder.image.scaleType = ImageView.ScaleType.CENTER_CROP
                playAudio(audio, object : AudioListener {
                    override fun onCompleted() {
                        examHolder.question1.text = Question1["text"]
                        examHolder.question1.show()
                        if (!listened.contains("audio1")) {
                            listened.add("audio1")
                            playAudio(Question1["audio"]!!, object : AudioListener {
                                override fun onCompleted() {
                                    examHolder.answer1.show()
                                }
                            })
                        }
                        examHolder.answer1.setOnCheckedChangeListener { _, answer1id ->
                            val id = Question1["id"]!!
                            session[id] = checkState(answer1id,position)
                            Handler().postDelayed({
                                examHolder.question2.text = Question2["text"]
                                examHolder.question2.show()
                                Handler().postDelayed({
                                    examHolder.questionScroll.fullScroll(View.FOCUS_DOWN)
                                }, 100)
                                if (!listened.contains("audio2")) {
                                    listened.add("audio2")
                                    playAudio(Question2["audio"]!!, object : AudioListener {
                                        override fun onCompleted() {
                                            examHolder.answer2.show()
                                        }
                                    })
                                }
                            }, 300)
                            examHolder.answer2.setOnCheckedChangeListener { _, answer2id ->
                                val id = Question2["id"]!!
                                session[id] = checkState(answer2id,position)
                                Handler().postDelayed({
                                    if (Question3 != null) {
                                        examHolder.question3.text = Question3["text"]
                                        examHolder.question3.show()
                                        Handler().postDelayed({
                                            examHolder.questionScroll.fullScroll(View.FOCUS_DOWN)
                                        }, 100)
                                        if (!listened.contains("audio3")) {
                                            listened.add("audio3")
                                            playAudio(Question3["audio"]!!, object : AudioListener {
                                                override fun onCompleted() {
                                                    examHolder.answer3.show()
                                                }
                                            })
                                        }
                                    } else checkOrFinishExam(position, examHolder)

                                }, 300)

                                examHolder.answer3.setOnCheckedChangeListener { _, answer3id ->
                                    val id = Question3!!["id"]!!
                                    session[id] = checkState(answer3id,position)
                                    Handler().postDelayed({
                                        if (Question4 != null) {
                                            examHolder.question4.text = Question4["text"]
                                            examHolder.question4.show()
                                            Handler().postDelayed({
                                                examHolder.questionScroll.fullScroll(View.FOCUS_DOWN)
                                            }, 100)
                                            if (!listened.contains("audio4")) {
                                                listened.add("audio4")
                                                playAudio(Question4["audio"]!!, object : AudioListener {
                                                    override fun onCompleted() {
                                                        examHolder.answer4.show()
                                                    }
                                                })
                                            }
                                        } else checkOrFinishExam(position, examHolder)

                                    }, 300)
                                    examHolder.answer4.setOnCheckedChangeListener { _, answer4id ->
                                        val id = Question4!!["id"]!!
                                        session[id] = checkState(answer4id,position)
                                        checkOrFinishExam(position, examHolder)
                                    }
                                }


                            }
                        }
                    }
                })

                listener.onLoadFinished()

            },500)


//            examHolder.finishExam.setOnClickListener{
//                finishExam(position)
//            }

            examHolder.nextExam.setOnClickListener {
                finishExam(position,"nextExam")
            }

            examHolder.nextPage.setOnClickListener {
                list.scrollToPosition(position+1)
            }

        }

        fun checkOrFinishExam(position: Int,examHolder:View){
            if(position==0) {
//                examHolder.finishExam.show()
                examHolder.nextExam.show()
                if(ended)
                    finishExam(position,"nextExam",true)
                else
                    finishExam(position,"nextExam")
                ended = true
            }else {
                finishExamSession.show()
                examHolder.nextPage.show()
            }
            Handler().postDelayed({
                examHolder.questionScroll.fullScroll(View.FOCUS_DOWN)
            }, 100)
        }

        fun checkState(state:Int,position: Int):Boolean{
            if(position==0 && ended)
                examHolder.nextExam.show()
            return when(state){
                R.id.radioButton11,
                R.id.radioButton21,
                R.id.radioButton31->true
                R.id.radioButton12,
                R.id.radioButton22,
                R.id.radioButton32->false
                else->false
            }
        }

        fun finishExam(position: Int,type:String = "finish",endAgain:Boolean=false){
            if(type=="finish" && position==0){
                val args = Bundle()
                args.putString("session_id",session_id)
                args.putString("user_id",user?.id)
                args.putString("device_id",DEVICE_ID)
                args.putBoolean("isFromExam",true)
                args.putBoolean("isFromCategory",isFromCategory)
                sendAnswers{
                    StatisticsViewDetailedFragment().start(args)
                }
            }else if((type!="finish" || position<0) && !endAgain){
                val dialog = AlertDialog.Builder(activity)
                var additionalText = ""
                if(type=="nextExam")
                    additionalText = " and go to next exam session"
                dialog.setTitle("Finish current exam session$additionalText")
                dialog.setMessage("Are you sure to finish current exam session$additionalText?")
                dialog.setNegativeButton(Html.fromHtml("<font color=\"#3F51B5\">Cancel</font>")) { _, _ ->
                    examHolder.nextExam.hide()
                }
                dialog.setNeutralButton(Html.fromHtml("<font color=\"#3F51B5\">Finish Exam</font>")) { _, _ ->
                    finishExam(position,"finish")
                }
                dialog.setPositiveButton(Html.fromHtml("<font color=\"#3F51B5\">Next Exam</font>")) { _, _ ->
                    sendAnswers {
                        if(user != null && user?.payment_type!="free")
                            openExam(2)
                        else{
                            val itemNames:ArrayList<String> = if(user!=null && user?.payment_type=="free")
                                arrayListOf("Payment Plan")
                            else
                                arrayListOf("Sign In","Payment Plan")

                            val items = itemNames.toArray(arrayOfNulls<String>(itemNames.size))
                            val dialog = AlertDialog.Builder(context)
                            dialog.setTitle("Select to continue")
                            dialog.setCancelable(true)
                            dialog.setNegativeButton(Html.fromHtml("<font color=\"#CE2828\">Cancel</font>")){_,_->}
                            dialog.setItems(items){_,item->
                                when (item) {
                                    0 -> {
                                        if(itemNames.size==2) openSign()
                                        else openUrl()
                                    }
                                    1 -> {
                                        if(itemNames.size==1) openUrl()
                                    }
                                }
                            }
                            dialog.create().show()
                        }
                    }
                }
                dialog.create().show()
            }

        }

        fun sendAnswers(callBack:()->Unit){
            val data = HashMap<String,String?>()
            data["session_id"] = session_id
            if(user?.id!=null)
                data["user_id"] = user?.id
            data["device_id"] = DEVICE_ID
            data["answers"] = JSONObject(session).toString()
            data["question_list"] = JSONArray(question_list).toString()
            Answer(context).sendAnswer(data,object:ServerCallback{
                override fun onSuccess(result: JSONObject?) {
                  callBack()
                }
                override fun onError(error: VolleyError) {
                    Log.d("----error",error.toString()+" - ")
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

        fun openExam(test_id:Int=1){
            val url = "http://test.azweb.dk/api/category/$test_id"
            Log.d("------exam opened",url)
            Query(context).get(url,responseCallBack = object:ResponseCallBack{
                override fun onSuccess(response: String?) {
                    val args = Bundle()
                    args.putString("data", response)
                    ExamFragment().start(args)
                }
            })
        }


        fun playAudio(url:String,listener:AudioListener?=null){

            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(url)
            try {
                mediaPlayer.prepare()
            }catch (e:Exception){

            }

            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener {
                listener?.onCompleted()
            }

        }

        interface AudioListener{
            fun onCompleted()
        }
        interface DataLoadedListener{
            fun onLoadFinished()
        }

        val Int.dp: Int get() = (this * context.resources.displayMetrics.density).toInt()


        fun View.show(){
            this.visibility = View.VISIBLE
        }
        fun View.hide(){
            this.visibility = View.GONE
        }

        companion object {
            private val ALLOWED_CHARACTERS = "0123456789qwertyuiopasdfghjklzxcvbnm"
        }

        private fun generateSessionId(sizeOfRandomString: Int = 15): String {
            val random = Random()
            val sb = StringBuilder(sizeOfRandomString)
            for (i in 0 until sizeOfRandomString)
                sb.append(ALLOWED_CHARACTERS[random.nextInt(ALLOWED_CHARACTERS.length)])
            return sb.toString()
        }

    }






    class QuestionViewHolder(v:View):RecyclerView.ViewHolder(v)

    class Answer(val context:Context){
        fun sendAnswer(data:HashMap<String,String?>?,serverCallback: ServerCallback){
            val queue = Volley.newRequestQueue(context)
            val url = "http://test.azweb.dk/api/answer"
            var message: JSONObject?
            queue.add(object : StringRequest(Request.Method.POST, url,
                    Response.Listener { response ->
                        message = JSONObject(response)
                        serverCallback.onSuccess(message)
                    },
                    Response.ErrorListener {
                        serverCallback.onError(it)
                    }
            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Accept"] = "application/json"
                    return headers
                }

                override fun getParams(): HashMap<String, String?> {
                    return data!!
                }

            })
        }
    }



    interface ServerCallback {
        fun onSuccess(result: JSONObject?)
        fun onError(error: VolleyError)
    }



}
