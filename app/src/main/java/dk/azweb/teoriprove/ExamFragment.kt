package dk.azweb.teoriprove

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.PointF
import android.media.MediaPlayer
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
import com.rm.rmswitch.RMTristateSwitch
import kotlinx.android.synthetic.main.fragment_exam.view.*
import kotlinx.android.synthetic.main.question_layout.view.*
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSmoothScroller
import android.telephony.PhoneStateListener
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
            activity!!.onBackPressed()
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
        @SuppressLint("MissingPermission")
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val cell = inflater.inflate(R.layout.question_layout,parent,false)
            phoneManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            sharedpreferences = context.getSharedPreferences(Preference, Context.MODE_PRIVATE)
            val TOKEN = sharedpreferences.getString("token",null)
            DEVICE_ID = phoneManager.deviceId


            Profile(context).getProfile(TOKEN,object: dk.azweb.teoriprove.ServerCallback {
                override fun onSuccess(result: JSONObject?) {
                    user = User(result)
                }
                override fun onError(error: VolleyError) {}
            })

            finishExamSession.setOnClickListener {
                finishExam()
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
            if(newTag != currentTag) {
                transaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
                transaction.replace(R.id.content, fragment,newTag)
                transaction.addToBackStack(null)
                transaction.commit()
                realActivity.openedFragment = newTag
                realActivity.actionBar.visibility = View.GONE

            }

        }

        override fun getItemCount(): Int {
            return datas.id!!.size
        }



        override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
            val header = "Question ${position+1}"
            examHeader.text = header
            val id = datas.id!![position]
            val text = datas.text!![position]
            val image = datas.image_url!![position]
            val audio = datas.audio_url!![position]
            val questions:ArrayList<HashMap<String,String>> = ArrayList()
            for(sub in datas.sub_id[id]!!.iterator()){
                val index = datas.sub_id[id]!!.indexOf(sub)
                val sub_id = datas.sub_id[id]!![index]
                val sub_text = datas.sub_text[id]!![index]
                val sub_audio_url = datas.sub_audio_url[id]!![index]
                val question = hashMapOf("id" to sub_id,"text" to sub_text,"audio" to sub_audio_url)
                questions.add(question)
            }
            val Question1 = questions[0]
            val Question2 = questions[1]
            val Question3 = questions[2]
            val examHolder = holder.itemView
            val listened = arrayListOf<String>()


            examHolder.questionText.text = text
            Handler().postDelayed({
                Glide.with(context)
                        .load(image)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(examHolder.image)
                examHolder.image.scaleType = ImageView.ScaleType.CENTER_CROP
                playAudio(audio,object:AudioListener{
                    override fun onCompleted() {
                        examHolder.question1.text = Question1["text"]
                        examHolder.question1.show()
                        if(!listened.contains("audio1")) {
                            listened.add("audio1")
                            playAudio(Question1["audio"]!!,object:AudioListener{
                                override fun onCompleted() {
                                    examHolder.answer1.show()
                                }
                            })
                        }
                        examHolder.answer1.setOnCheckedChangeListener { _, answer1id ->
                            val id = Question1["id"]!!
                            session[id] = checkState(answer1id)
                            Log.d("------session",session.toString())
                            Handler().postDelayed({
                                examHolder.question2.text = Question2["text"]
                                examHolder.question2.show()
                                Handler().postDelayed({
                                    examHolder.questionScroll.fullScroll(View.FOCUS_DOWN)
                                },100)
                                if(!listened.contains("audio2")) {
                                    listened.add("audio2")
                                    playAudio(Question2["audio"]!!,object:AudioListener{
                                        override fun onCompleted() {
                                            examHolder.answer2.show()
                                        }
                                    })
                                }
                            },300)
                            examHolder.answer2.setOnCheckedChangeListener{_,answer2id->
                                val id = Question2["id"]!!
                                session[id] = checkState(answer2id)
                                Log.d("------session",session.toString())
                                Handler().postDelayed({
                                    examHolder.question3.text = Question3["text"]
                                    examHolder.question3.show()
                                    Handler().postDelayed({
                                        examHolder.questionScroll.fullScroll(View.FOCUS_DOWN)
                                    },100)
                                    if(!listened.contains("audio3")) {
                                        listened.add("audio3")
                                        playAudio(Question3["audio"]!!,object:AudioListener{
                                            override fun onCompleted() {
                                                examHolder.answer3.show()
                                            }
                                        })
                                    }
                                },300)
                                examHolder.answer3.setOnCheckedChangeListener{_,answer3id->
                                    val id = Question3["id"]!!
                                    session[id] = checkState(answer3id)
                                    Log.d("------session",session.toString())
                                    Handler().postDelayed({
                                        examHolder.questionScroll.fullScroll(View.FOCUS_DOWN)
                                    },100)

                                    if(position==24)
                                        examHolder.finishExam.show()
                                    else
                                        examHolder.nextPage.show()

                                    if(position==0)
                                        finishExamSession.show()
                                }
                            }
                        }
                    }
                })

                listener.onLoadFinished()

            },500)


            examHolder.finishExam.setOnClickListener{
                finishExam()
            }

            examHolder.nextPage.setOnClickListener {
                list.scrollToPosition(position+1)
            }

        }

        fun checkState(state:Int):Boolean{
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

        fun finishExam(){
            val dialog = AlertDialog.Builder(activity)
            dialog.setTitle("End exam session")
            dialog.setMessage("Are you sure to end exam session?")
            dialog.setNegativeButton(Html.fromHtml("<font color=\"#3F51B5\">Cancel</font>")) { _, _ ->  }
            dialog.setPositiveButton(Html.fromHtml("<font color=\"#3F51B5\">Finish</font>")) { _, _ ->
                val data = HashMap<String,String?>()
                data["session_id"] = session_id
                data["user_id"] = user?.id
                data["device_id"] = DEVICE_ID
                data["answers"] = JSONObject(session).toString()
                data["question_list"] = JSONArray(question_list).toString()
                Answer(context).sendAnswer(data,object:ServerCallback{
                    override fun onSuccess(result: JSONObject?) {
                        val args = Bundle()
                        args.putString("session_id",session_id)
                        args.putString("user_id",user!!.id)
                        args.putBoolean("isFromExam",true)
                        args.putBoolean("isFromCategory",isFromCategory)
                        StatisticsViewDetailedFragment().start(args)
                    }
                    override fun onError(error: VolleyError) {
                        Log.d("----error",error.networkResponse.statusCode.toString()+" - ")
                    }
                })
            }
            dialog.create().show()
        }


        fun playAudio(url:String,listener:AudioListener?=null){

            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(url)
            try {
                mediaPlayer.prepare()
            }catch (e:Exception){
                e.printStackTrace()
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


