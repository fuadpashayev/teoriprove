package dk.azweb.teoriprove

import android.content.Context
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.category_layout.view.*
import kotlinx.android.synthetic.main.fragment_category.*
import kotlinx.android.synthetic.main.fragment_category.view.*
import org.json.JSONObject
import java.util.zip.Inflater
import android.graphics.drawable.ColorDrawable
import android.support.v4.app.FragmentManager
import android.text.Html
import android.widget.ImageView


class CategoryFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val realActivity = (activity as HomeActivity)
        realActivity.actionBar.visibility = View.GONE
        val view =  inflater.inflate(R.layout.fragment_category, container, false)
        val manager = fragmentManager
        val queue = Volley.newRequestQueue(context)
        val url = "http://test.azweb.dk/api/category"
        val postRequest = object : StringRequest(Request.Method.POST, url,
            Response.Listener { response ->
                val category = CategoryModel(response)

                val layoutManager = GridLayoutManager(context,3)
                view.categoryList.layoutManager = layoutManager
                view.categoryList.adapter = CategoryAdapter(category,view,realActivity,manager!!,context!!)

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




        view.backButton.setOnClickListener {
            activity!!.onBackPressed()
        }




        return view
    }




}


class CategoryAdapter(val datas:CategoryModel,val realView:View,val realActivity: HomeActivity,val manager: FragmentManager,val context: Context):RecyclerView.Adapter<CategoryViewHolder>(){

    val selected = arrayListOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val cell = inflater.inflate(R.layout.category_layout,parent,false)
        return CategoryViewHolder(cell)
    }

    override fun getItemCount(): Int {
        return datas.id!!.size
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val id = datas.id!![position].toDouble().toInt()
        val name = datas.name!![position]

        val view = holder.itemView
        val check = view.checkBox
        val box = view.openCategory
        view.categoryName.text = name
        view.selectBox.setOnClickListener {
            check.toggleCheck(id)
            box.toggleBackground()
        }


        realView.startArea.setOnClickListener {
            val queue = Volley.newRequestQueue(context)
            val url = "http://test.azweb.dk/api/category/questions"
            val postRequest = object : StringRequest(Request.Method.POST, url,
                    Response.Listener { response ->

                        ExamFragment().start(response)

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
                    params["categories"] = selected.toString()
                    return params
                }
            }
            queue.add(postRequest)

        }



    }

    fun ConstraintLayout.toggleBackground(){
        val color = (this.background as ColorDrawable).color
        when(color){
            -1->this.setBackgroundColor(context.resources.getColor(R.color.selected))
            else->this.setBackgroundColor(context.resources.getColor(R.color.white))
        }

    }

    fun ImageView.toggleCheck(id:Int){
        val state = this.visibility
        when(state){
            View.VISIBLE->{
                this.visibility = View.GONE
                selected.remove(id)
            }
            View.GONE->{
                this.visibility = View.VISIBLE
                selected.add(id)
            }
        }
        if(selected.size>0){
            realView.startArea.visibility = View.VISIBLE
            realView.selectedText.text = Html.fromHtml("<font color=\"#069688\">${selected.size}</font> category selected")
        }else realView.startArea.visibility = View.GONE

    }


    fun Fragment.start(args:String?=null){
        val transaction = manager.beginTransaction()
        val fragment = this
        if(args!=null) {
            val data = Bundle()
            data.putString("data", args)
            data.putBoolean("fromCategory",true)
            fragment.arguments = data
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





class CategoryViewHolder(v: View):RecyclerView.ViewHolder(v)
