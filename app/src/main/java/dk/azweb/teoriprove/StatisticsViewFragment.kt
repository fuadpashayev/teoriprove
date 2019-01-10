package dk.azweb.teoriprove

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.database.DataSetObserver
import android.os.Bundle
import android.support.annotation.Px
import android.support.v4.app.Fragment
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.rm.rmswitch.RMTristateSwitch
import kotlinx.android.synthetic.main.fragment_statistics_view.*
import kotlinx.android.synthetic.main.fragment_statistics_view.view.*
import kotlinx.android.synthetic.main.statistics_view_layout.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.ArrayList


class StatisticsViewFragment : Fragment() {
    lateinit var DEVICE_ID: String
    lateinit var phoneManager: TelephonyManager
    lateinit var realActivity: HomeActivity
    lateinit var user_id:String
    @SuppressLint("MissingPermission")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        realActivity = (activity as HomeActivity)
        val view = inflater.inflate(R.layout.fragment_statistics_view, container, false)
        phoneManager = context!!.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        DEVICE_ID = phoneManager.deviceId
        user_id = this.arguments!!.getString("user_id")!!
        val isFromExam = this.arguments!!.getBoolean("isFromExam")
        val isFromCategory = this.arguments!!.getBoolean("isFromCategory")
        val session_id = this.arguments!!.getString("session_id")!!
        val queue = Volley.newRequestQueue(context)
        val url = "http://test.azweb.dk/api/answer/statistics/session"
        val postRequest = object : StringRequest(Request.Method.POST, url,
                Response.Listener { response ->
                    Log.d("------response",response.toString())
                    val data = QuestionModel(response)
                    view.pager.adapter = StatisticsPagerAdapter(data,examHeader,context!!,view.pager)
                    view.loader.visibility = View.GONE
                },
                Response.ErrorListener {
                    Log.d("-------Error", "error")
                }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Accept"] = "application/json"
                return headers
            }

            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String,String>()
                params["user_id"] = user_id
                params["session_id"] = session_id
                return params
            }
        }
        queue.add(postRequest)



        view.backButton.setOnClickListener {
            activity!!.onBackPressed()
            if(isFromExam)
                activity!!.onBackPressed()
            if(isFromCategory)
                activity!!.onBackPressed()

            realActivity.openedFragment = "Statistics"
        }

        return view
    }

}

class StatisticsPagerAdapter(val datas:QuestionModel,val examHeader:TextView,val context: Context,val list: ViewPager):PagerAdapter(){
    override fun isViewFromObject(view: View, Object: Any): Boolean {
        return view==Object
    }

    override fun getCount(): Int {
        return datas.id!!.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.statistics_view_layout,container,false)
        val num = list.addOnPageChangeListener(object:ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(p0: Int) {

            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {

            }

            override fun onPageSelected(position: Int) {
                val header = "Question ${position+1}"
                examHeader.text = header
            }
        })



        val id = datas.id!![position]
        val text = datas.text!![position]
        val image = datas.image_url!![position]
        val audio = datas.audio_url!![position]
        val questions: ArrayList<HashMap<String, String?>> = ArrayList()
        for(sub in datas.sub_id[id]!!.iterator()){
            val index = datas.sub_id[id]!!.indexOf(sub)
            val sub_id = datas.sub_id[id]!![index]
            val sub_text = datas.sub_text[id]!![index]
            val sub_audio_url = datas.sub_audio_url[id]!![index]
            val answer = datas.answer[id]!![index]
            Log.d("-----answers",datas.user_answer.toString())
            val user_answer = datas.user_answer[id]!![index]
            val question = hashMapOf("id" to sub_id,"text" to sub_text,"audio" to sub_audio_url,"answer" to answer,"user_answer" to user_answer)
            questions.add(question)
        }
        val Question1 = questions[0]
        val Question2 = questions[1]
        val Question3 = questions[2]
        val examHolder = view

        Glide.with(context)
                .load(image)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .thumbnail(Glide.with(context).load(R.mipmap.loader))
                .fitCenter()
                .crossFade()
                .into(examHolder.image)
        examHolder.questionText.text = text


        examHolder.question1.text = Question1["text"]
        examHolder.question2.text = Question2["text"]
        examHolder.question3.text = Question3["text"]

        val user_answer1 = if(Question1["user_answer"]!=null) Question1["user_answer"] else Question1["answer"]
        val user_answer2 = if(Question2["user_answer"]!=null) Question2["user_answer"] else Question2["answer"]
        val user_answer3 = if(Question3["user_answer"]!=null) Question3["user_answer"] else Question3["answer"]

        examHolder.switch1.state = getState(user_answer1)
        examHolder.switch2.state = getState(user_answer2)
        examHolder.switch3.state = getState(user_answer3)

        examHolder.question1area.setBackgroundColor(getBackground(Question1["answer"],Question1["user_answer"]))
        examHolder.question2area.setBackgroundColor(getBackground(Question2["answer"],Question2["user_answer"]))
        examHolder.question3area.setBackgroundColor(getBackground(Question3["answer"],Question3["user_answer"]))


        container.addView(view)
        return view
    }//

    fun getState(state:String?):Int{
        val state = state!!.toDouble().toInt()
        return when(state){
            0->RMTristateSwitch.STATE_LEFT
            1->RMTristateSwitch.STATE_RIGHT
            else -> RMTristateSwitch.STATE_MIDDLE
        }
    }

    fun getBackground(answer:String?,user_answer:String?):Int{
        return if(user_answer==null)
            context.resources.getColor(R.color.nullBackground)
        else if(answer==user_answer)
            context.resources.getColor(R.color.trueBackground)
        else context.resources.getColor(R.color.falseBackground)
    }

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
        container.removeView(view as View)
    }



}







