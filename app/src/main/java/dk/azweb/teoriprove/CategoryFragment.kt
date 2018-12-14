package dk.azweb.teoriprove

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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


class CategoryFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val realActivity = (activity as HomeActivity)
        val view =  inflater.inflate(R.layout.fragment_category, container, false)

        val queue = Volley.newRequestQueue(context)
        val url = "http://test.azweb.dk/api/category"
        val postRequest = object : StringRequest(Request.Method.POST, url,
            Response.Listener { response ->
                val category = CategoryModel(response)
                view.categoryList.adapter = CategoryAdapter(category)

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
            realActivity.openedFragment = false
        }




        return view
    }




}


class CategoryAdapter(val datas:CategoryModel):RecyclerView.Adapter<CategoryViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val cell = inflater.inflate(R.layout.category_layout,parent,false)
        return CategoryViewHolder(cell)
    }

    override fun getItemCount(): Int {
        return datas.id!!.size
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val id = datas.id!![position]
        val name = datas.name!![position]

        val view = holder.itemView
        view.categoryName.text = name
        view.openCategory.setOnClickListener {

        }


    }

}



class CategoryViewHolder(v: View):RecyclerView.ViewHolder(v)
