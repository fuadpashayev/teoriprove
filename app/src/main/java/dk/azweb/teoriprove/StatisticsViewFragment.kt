package dk.azweb.teoriprove

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.database.DataSetObserver
import android.os.Bundle
import android.support.annotation.Px
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.GridLayoutManager
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
        val session_id = this.arguments!!.getString("session_id")!!
        val url = "http://test.azweb.dk/api/answer/statistics/session"
        val params:MutableMap<String,String?> = HashMap()
        params["user_id"] = user_id
        params["session_id"] = session_id

        Query(activity!!).post(url,params,responseCallBack = object:ResponseCallBack{
            override fun onSuccess(response: String?) {
                val data = QuestionModel(response)
                view.simpleList.adapter = StatisticsSimpleAdapter(data,context!!)
                view.loader.visibility = View.GONE
            }
        })

        view.backButton.setOnClickListener {
            realActivity.openedFragment = "Statistics"
            activity!!.onBackPressed()
        }

        return view
    }

}

class StatisticsSimpleAdapter(val datas:QuestionModel,val context: Context):RecyclerView.Adapter<StatisticsSimpleViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): StatisticsSimpleViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.statistics_view_layout,parent,false)
        val cell = StatisticsSimpleViewHolder(view)

        return cell
    }

    override fun getItemCount(): Int {
        return datas.id!!.size
    }

    override fun onBindViewHolder(holder: StatisticsSimpleViewHolder, position: Int) {
        val id = datas.id!![position]
        val image = datas.image_url!![position]
        val imageHolder = holder.itemView.questionImage
        val isCorrect = checkAnswer(datas.correct_answer[id]!!)
        imageHolder.setBackgroundColor(when(isCorrect){
            null->context.resources.getColor(R.color.colorNull)
            true->context.resources.getColor(R.color.green)
            false->context.resources.getColor(R.color.red)
        })

        Glide.with(context)
                .load(image)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .thumbnail(Glide.with(context).load(R.mipmap.loader))
                .centerCrop()
                .crossFade()
                .into(imageHolder)


    }

    val Int.dp: Int get() = (this * context.resources.displayMetrics.density).toInt()

    fun checkAnswer(answers:ArrayList<Boolean?>):Boolean?{
        val answer1 = if(answers[0]!=null) answers[0] else null
        val answer2 = if(answers[1]!=null) answers[1] else null
        val answer3 = if(answers[2]!=null) answers[2] else null
        val returns:Boolean?
        if(answer1==null || answer2==null || answer3==null)
            returns = null
        else if(answer1 && answer2 && answer3)
            returns = true
        else
            return false
        return returns
    }

}

class StatisticsSimpleViewHolder(v:View):RecyclerView.ViewHolder(v)








