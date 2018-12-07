package dk.azweb.teoriprove

import org.json.JSONArray
import org.json.JSONObject
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken





class User(data: JSONObject?) {

    var id:String? = null
    var name:String? = null
    var email:String? = null
    var access_token:String? = null

    init {
        id = if(data?.has("id")!!) data.getString("id") else null
        name = if(data.has("name")) data.getString("name") else null
        email = if(data.has("email")) data.getString("email") else null
        access_token = if(data.has("access_token")) data.getString("access_token") else null
    }

}

class Category(dataString: String?){

    var status:String? = null
    var categories:List<HashMap<String,Any>>? = null
    var id:ArrayList<String>? = arrayListOf()
    var name:ArrayList<String>? = arrayListOf()
    var data:JSONObject
    init {
        data = JSONObject(dataString)
        status = if(data.has("status")) data.getString("status") else null
        categories = if(data.has("categories")) JSON().parse(data.getString("categories")) else null

        for(category in categories!!.iterator()){
            id!!.add(category.get("id").toString())
            name!!.add(category.get("name").toString())
        }

    }

    val categorya = object : HashMap<String,Any>(){

    }


}


class JSON {
    fun parse(json: String):List<HashMap<String,Any>> {
        val type = object : TypeToken<List<HashMap<String, Any>>>(){}.type
        val map = Gson().fromJson<List<HashMap<String,Any>>>(json, type)
        return map
    }
}