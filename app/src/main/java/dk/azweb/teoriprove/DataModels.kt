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

class CategoryModel(dataString: String?){

    var status:String? = null
    var categories:List<HashMap<String,Any>>? = null
    var id:ArrayList<String>? = arrayListOf()
    var name:ArrayList<String>? = arrayListOf()
    var data:JSONObject = JSONObject(dataString)

    init {
        status = if(data.has("status")) data.getString("status") else null
        categories = if(data.has("categories")) JSON().parse(data.getString("categories")) else null

        for(category in categories!!.iterator()){
            id!!.add(category["id"].toString())
            name!!.add(category["name"].toString())
        }

    }

}

class QuestionModel(dataString: String?){

    var status:String? = null
    var id:ArrayList<String>? = arrayListOf()
    var text:ArrayList<String>? = arrayListOf()
    var image_url:ArrayList<String>? = arrayListOf()
    var audio_url:ArrayList<String>? = arrayListOf()

    var questions:List<HashMap<String,Any>>? = null
    var data:JSONObject = JSONObject(dataString)

    init {
        status = if(data.has("status")) data.getString("status") else null
        questions = if(data.has("questions")) JSON().parse(data.getString("questions")) else null

        for(question in questions!!.iterator()){
            id!!.add(question["id"].toString())
            text!!.add(question["text"].toString())
            image_url!!.add(question["image_url"].toString())
            audio_url!!.add(question["audio_url"].toString())
        }

    }

}


class JSON {
    fun parse(json: String):List<HashMap<String,Any>> {
        val type = object : TypeToken<List<HashMap<String, Any>>>(){}.type
        val map = Gson().fromJson<List<HashMap<String,Any>>>(json, type)
        return map
    }
}