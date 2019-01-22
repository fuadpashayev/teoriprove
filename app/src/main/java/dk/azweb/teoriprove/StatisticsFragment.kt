package dk.azweb.teoriprove

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.widget.RecyclerView
import android.telephony.TelephonyManager
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.fragment_statistics.view.*
import kotlinx.android.synthetic.main.statistics_layout.view.*


class StatisticsFragment : Fragment() {
    lateinit var DEVICE_ID:String
    lateinit var phoneManager: TelephonyManager
    lateinit var realActivity: HomeActivity
    lateinit var manager: FragmentManager
    var user_id:String? = null
    @SuppressLint("MissingPermission")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        realActivity = (activity as HomeActivity)
        realActivity.actionBar.visibility = View.GONE
        val view =  inflater.inflate(R.layout.fragment_statistics, container, false)
        phoneManager = context!!.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        DEVICE_ID = phoneManager.deviceId
        manager = fragmentManager!!
        user_id = this.arguments!!.getString("user_id")
        val queue = Volley.newRequestQueue(context)
        val url = "http://test.azweb.dk/api/answer/statistics"
        val postRequest = object : StringRequest(Request.Method.POST, url,
            Response.Listener { response ->
                val statistics = StatisticsModel(response)
                if(!statistics.error) {
                    view.statisticsList.adapter = StatisticsAdapter(statistics, realActivity, manager, user_id, context!!)
                }else{
                    view.emptyMessage.visibility = View.VISIBLE
                }
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

            override fun getParams(): MutableMap<String, String?> {
                val params = HashMap<String,String?>()
                params["user_id"] = user_id
                return params
            }
        }
        queue.add(postRequest)




        view.backButton.setOnClickListener {
            activity!!.onBackPressed()
            realActivity.openedFragment = null
        }




        return view
    }




}


class StatisticsAdapter(val datas:StatisticsModel,val realActivity: HomeActivity,val manager: FragmentManager,val user_id:String?,val context: Context):RecyclerView.Adapter<StatisticsViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatisticsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val cell = inflater.inflate(R.layout.statistics_layout,parent,false)
        return StatisticsViewHolder(cell)
    }

    override fun getItemCount(): Int {
        return datas.session_ids!!.size
    }

    override fun onBindViewHolder(holder: StatisticsViewHolder, position: Int) {
        val session_id = datas.session_ids!![position]

        val view = holder.itemView
        view.examText.text = "Exam ${position+1} (${datas.time[position]})"
        view.openResults.setOnClickListener {
            val args = Bundle()
            args.putString("session_id",session_id)
            args.putString("user_id",user_id)
            args.putBoolean("isFromExam",false)
            StatisticsViewDetailedFragment().start(args)
//            val dialog = AlertDialog.Builder(context)
//            dialog.setTitle("Select Statistics View Type")
//            dialog.setMessage("Simple or Detailed?")
//            dialog.setNegativeButton(Html.fromHtml("<font color=\"#3F51B5\">Simple</font>")) { _, _ ->
//                StatisticsViewFragment().start(args)
//            }
//            dialog.setPositiveButton(Html.fromHtml("<font color=\"#3F51B5\">Detailed</font>")) { _, _ ->
//                StatisticsViewDetailedFragment().start(args)
//            }
//            dialog.create().show()
        }


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
        }

    }

}



class StatisticsViewHolder(v: View):RecyclerView.ViewHolder(v)
