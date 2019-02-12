package dk.azweb.teoriprove

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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
import kotlinx.android.synthetic.main.fragment_statistics_view_detailed.*
import kotlinx.android.synthetic.main.fragment_statistics_view_detailed.view.*
import kotlinx.android.synthetic.main.statistics_view_detailed_layout.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.ArrayList


class StatisticsViewDetailedFragment : Fragment() {
    lateinit var DEVICE_ID: String
    lateinit var phoneManager: TelephonyManager
    lateinit var realActivity: HomeActivity
    var user_id:String? = null
    var isFromExam = false
    @SuppressLint("MissingPermission")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        realActivity = (activity as HomeActivity)
        val view = inflater.inflate(R.layout.fragment_statistics_view_detailed, container, false)
        phoneManager = context!!.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        DEVICE_ID = phoneManager.deviceId
        user_id = this.arguments!!.getString("user_id")
        isFromExam = this.arguments!!.getBoolean("isFromExam",false)
        val isFromCategory = this.arguments!!.getBoolean("isFromCategory")
        if(isFromExam)
            realActivity.openedFragment = "StatisticsFromExam"
        val session_id = this.arguments!!.getString("session_id")!!
        val url = "http://test.azweb.dk/api/answer/statistics/session"
        val params:MutableMap<String,String?>? = HashMap()
        if(user_id!=null)
            params!!["user_id"] = user_id
        params!!["device_id"] = DEVICE_ID
        params["session_id"] = session_id
        Query(activity!!).post(url,params,responseCallBack = object : ResponseCallBack{
            override fun onSuccess(response: String?) {
                val data = QuestionModel(response)
                view.pager.offscreenPageLimit = 3
                view.pager.adapter = StatisticsPagerAdapter(data,examHeader,context!!,view.pager)
                view.loader.visibility = View.GONE
            }
        })


        view.backButton.setOnClickListener {
            val intent = Intent(activity,HomeActivity::class.java)
            if(!isFromExam)
                intent.putExtra("backToStatistics",true)
            startActivity(intent)
            activity?.finish()
            activity?.overridePendingTransition(R.anim.slide_in,R.anim.slide_out)
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
        val view = inflater.inflate(R.layout.statistics_view_detailed_layout,container,false)
        list.addOnPageChangeListener(object:ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(p0: Int) {}
            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}
            override fun onPageSelected(position: Int) {
                val header = "Question ${position+1}"
                examHeader.text = header
            }
        })



        val id = datas.id!![position]
        val text = datas.text!![position]
        val image = datas.image_url!![position]
        val audio = datas.audio_url!![position]
        val questions: ArrayList<HashMap<String,String?>?> = ArrayList()
        var Question1:HashMap<String,String?>? = null
        var Question2:HashMap<String,String?>? = null
        var Question3:HashMap<String,String?>? = null
        var Question4:HashMap<String,String?>? = null
        for(sub in datas.sub_id[id]!!.iterator()){
            val index = datas.sub_id[id]!!.indexOf(sub)
            val sub_id = datas.sub_id[id]!![index]
            val sub_text = datas.sub_text[id]!![index]
            val sub_audio_url = datas.sub_audio_url[id]!![index]
            val answer = datas.answer[id]!![index]
            val user_answer = datas.user_answer[id]!![index]
            val question = hashMapOf("id" to sub_id,"text" to sub_text,"audio" to sub_audio_url,"answer" to answer,"user_answer" to user_answer)
            questions.add(question)
        }
        Question1 = questions[0]!!
        Question2 = questions[1]!!
        if(questions.size>2)
            Question3 = questions[2]!!
        if(questions.size>3)
            Question4 = questions[3]!!
        val examHolder = view

        Glide.with(context)
                .load(image)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .thumbnail(Glide.with(context).load(R.mipmap.loader1))
                .fitCenter()
                .crossFade()
                .into(examHolder.image)
        examHolder.questionText.text = text


        examHolder.question1.text = Question1["text"]
        val user_answer1 = if(Question1["user_answer"]!=null) Question1["user_answer"] else Question1["answer"]
        getState(user_answer1,1,examHolder)
        examHolder.question1area.setBackgroundColor(getBackground(Question1["answer"],Question1["user_answer"]))



        examHolder.question2.text = Question2["text"]
        val user_answer2 = if(Question2["user_answer"]!=null) Question2["user_answer"] else Question2["answer"]
        getState(user_answer2,2,examHolder)
        examHolder.question2area.setBackgroundColor(getBackground(Question2["answer"],Question2["user_answer"]))


        if(Question3!=null) {
            examHolder.question3area.visibility = View.VISIBLE
            examHolder.question3.text = Question3["text"]
            val user_answer3 = if (Question3["user_answer"] != null) Question3["user_answer"] else Question3["answer"]
            getState(user_answer3, 3, examHolder)
            examHolder.question3area.setBackgroundColor(getBackground(Question3["answer"], Question3["user_answer"]))
        }


        if(Question4!=null) {
            examHolder.question4area.visibility = View.VISIBLE
            examHolder.question4.text = Question4["text"]
            val user_answer4 = if (Question4["user_answer"] != null) Question4["user_answer"] else Question4["answer"]
            getState(user_answer4, 4, examHolder)
            examHolder.question4area.setBackgroundColor(getBackground(Question4["answer"], Question4["user_answer"]))
        }


        container.addView(view)
        return view
    }

    fun getState(state:String?,questionNum:Int,holder:View){
        val state = state!!.toDouble().toInt()
        when(state){
            1->{
                when(questionNum){
                    1->holder.radioButton11.isChecked = true
                    2->holder.radioButton21.isChecked = true
                    3->holder.radioButton31.isChecked = true
                    4->holder.radioButton41.isChecked = true
                }
            }
            0->{
                when(questionNum){
                    1->holder.radioButton12.isChecked = true
                    2->holder.radioButton22.isChecked = true
                    3->holder.radioButton32.isChecked = true
                    4->holder.radioButton42.isChecked = true
                }
            }
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







