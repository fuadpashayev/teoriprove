package dk.azweb.teoriprove

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.category_layout.view.*
import kotlinx.android.synthetic.main.fragment_category.*
import kotlinx.android.synthetic.main.fragment_category.view.*
import kotlinx.android.synthetic.main.fragment_exam.view.*
import kotlinx.android.synthetic.main.question_layout.view.*
import org.json.JSONObject
import java.util.zip.Inflater


class ExamFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val realActivity = (activity as HomeActivity)
        val view =  inflater.inflate(R.layout.fragment_exam, container, false)

        val queue = Volley.newRequestQueue(context)
        val url = "http://test.azweb.dk/api/question/random"
        val postRequest = object : StringRequest(Request.Method.POST, url,
                Response.Listener { response ->
                    val question = QuestionModel(response)
                    view.questionList.adapter = QuestionAdapter(question)

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
        }
        queue.add(postRequest)

        return view
    }

    inner class customSwitch:Switch(context){

    }



    class QuestionAdapter(val datas:QuestionModel):RecyclerView.Adapter<QuestionViewHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val cell = inflater.inflate(R.layout.question_layout,parent,false)

            return QuestionViewHolder(cell)
        }

        override fun getItemCount(): Int {
            return datas.id!!.size
        }

        override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
            val id = datas.id!![position]
            val text = datas.text!![position]
            val image = datas.image_url!![position]
            val audio = datas.audio_url!![position]

            val examHolder = holder.itemView

            examHolder.questionText.text = text


        }

    }




    class QuestionViewHolder(v:View):RecyclerView.ViewHolder(v)




}


