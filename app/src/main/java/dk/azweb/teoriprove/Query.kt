package dk.azweb.teoriprove

import android.content.Context
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.util.HashMap
import android.view.MotionEvent
import android.text.method.Touch.onTouchEvent
import android.support.v4.view.ViewPager
import android.util.AttributeSet


class Query(val context: Context){
    fun get(url:String,headers:MutableMap<String,String?>?=null,responseCallBack: ResponseCallBack){
        val queue = Volley.newRequestQueue(context)
        queue.add(object : StringRequest(Request.Method.POST, url,
                Response.Listener { response ->
                    responseCallBack.onSuccess(response)
                },
                Response.ErrorListener {
                }
        ) {
            override fun getHeaders(): MutableMap<String, String?>? {
                var inHeader:MutableMap<String,String?> = HashMap()
                if(headers!=null)
                    inHeader = headers
                else
                    inHeader["Accept"] = "application/json; charset=utf-8"
                return inHeader
            }
        })
    }

    fun post(url: String,params:MutableMap<String,String?>?,headers:MutableMap<String,String>?=null,responseCallBack: ResponseCallBack){
        val queue = Volley.newRequestQueue(context)
        queue.add(object : StringRequest(Request.Method.POST, url,
                Response.Listener { response ->
                    responseCallBack.onSuccess(response)
                },
                Response.ErrorListener {

                }
        ) {
            override fun getHeaders(): MutableMap<String, String>? {
                var inHeader:MutableMap<String,String> = HashMap()
                if(headers!=null)
                    inHeader = headers
                else
                    inHeader["Accept"] = "application/json; charset=utf-8"
                return inHeader
            }

            override fun getParams(): MutableMap<String, String?>? {
                return params
            }

        })
    }
}

interface ResponseCallBack {
    fun onSuccess(response: String?)
//    fun onError(error: VolleyError)
}


open class NewViewPager : ViewPager {
    var isPagingEnabled = false


    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return this.isPagingEnabled && super.onTouchEvent(event)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return this.isPagingEnabled && super.onInterceptTouchEvent(event)
    }

    fun isScrollEnabled(b: Boolean) {
        this.isPagingEnabled = b
    }


}