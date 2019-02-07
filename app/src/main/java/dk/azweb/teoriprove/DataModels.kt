package dk.azweb.teoriprove

import android.util.Log
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken





class User(data: JSONObject?) {

    var id:String? = null
    var name:String? = null
    var email:String? = null
    var access_token:String? = null
    var payment_type:String? = null

    init {
        id = if(data?.has("id")!!) data.getString("id") else null
        name = if(data.has("name")) data.getString("name") else null
        email = if(data.has("email")) data.getString("email") else null
        access_token = if(data.has("access_token")) data.getString("access_token") else null
        payment_type = if(data.has("payment_type")) data.getString("payment_type").toLowerCase() else null
    }

}

class CategoryModel(dataString: String?){

    var status:String? = null
    var categories:List<HashMap<String,Any>>? = null
    var id:ArrayList<String>? = arrayListOf()
    var name:ArrayList<String>? = arrayListOf()
    var image_url:ArrayList<String>? = arrayListOf()
    var data:JSONObject = JSONObject(dataString)

    init {
        status = if(data.has("status")) data.getString("status") else null
        categories = if(data.has("categories")) JSON().parse(data.getString("categories")) else null

        for(category in categories!!.iterator()){
            id!!.add(category["id"].toString())
            name!!.add(category["name"].toString())
            image_url!!.add(category["image_url"].toString())
        }

    }

}

class QuestionModel(dataString: String?){

    var status:String? = null
    var id:ArrayList<String>? = arrayListOf()
    var text:ArrayList<String>? = arrayListOf()
    var image_url:ArrayList<String>? = arrayListOf()
    var audio_url:ArrayList<String>? = arrayListOf()

    var sub_id = HashMap<String,ArrayList<String>>()
    var sub_audio_url = HashMap<String,ArrayList<String>>()
    var sub_text = HashMap<String,ArrayList<String>>()
    var answer = HashMap<String,ArrayList<String>>()
    var user_answer = HashMap<String,ArrayList<String?>>()
    var correct_answer = HashMap<String,ArrayList<Boolean?>>()

    var questions:List<HashMap<String,Any>>? = null
    var sub_questions:ArrayList<Map<String,String>>? = null
    var data:JSONObject = JSONObject(dataString)

    var question_list = arrayListOf<String>()

    init {
        status = if(data.has("status")) data.getString("status") else null
        questions = if(data.has("questions")) JSON().parse(data.getString("questions")) else if(data.has("results")) JSON().parse(data.getString("results")) else null


        for(question in questions!!.iterator()){
            val parent_id = question["id"].toString()
            id!!.add(question["id"].toString())
            question_list.add(question["id"].toString())
            text!!.add(question["text"].toString())
            image_url!!.add(question["image_url"].toString())
            audio_url!!.add(question["audio_url"].toString())
            sub_questions = question["sub_questions"] as ArrayList<Map<String, String>>?

            sub_id[parent_id] = arrayListOf()
            sub_audio_url[parent_id] = arrayListOf()
            sub_text[parent_id] = arrayListOf()
            answer[parent_id] = arrayListOf()
            user_answer[parent_id] = arrayListOf()
            correct_answer[parent_id] = arrayListOf()

            for(sub_question in sub_questions!!.iterator()){
                val subid = String.format("%f",sub_question["id"])
                sub_id[parent_id]!!.add(subid)
                sub_audio_url[parent_id]!!.add(sub_question["audio_url"]!!.toString())
                sub_text[parent_id]!!.add(sub_question["text"]!!.toString())
                answer[parent_id]!!.add(sub_question["answer"].toString())
                if(sub_question["user_answer"]!=null) {
                    user_answer[parent_id]!!.add(sub_question["user_answer"].toString())
                    correct_answer[parent_id]!!.add(sub_question["answer"].toString()==sub_question["user_answer"].toString())
                }else {
                    user_answer[parent_id]!!.add(null)
                    correct_answer[parent_id]!!.add(null)
                }


            }
        }

    }

}

class StatisticsModel(dataString: String?){

    var status:String? = null
    var results:JSONObject? = null
    var session_ids:ArrayList<String>? = arrayListOf()
    var name:ArrayList<String>? = arrayListOf()
    var data:JSONObject = JSONObject(dataString)
    var time:ArrayList<String> = arrayListOf()
    var error:Boolean = false
    init {
        status = if(data.has("status")) data.getString("status") else null

        if(status!="error") {
            results = if (data.has("results")) data.getJSONObject("results") else null
            for (session_id in results!!.keys()) {
                session_ids!!.add(session_id)
                time.add(JSONObject(results!!.getJSONArray(session_id).getString(0).toString()).getString("time"))
            }
            error=false
        }else error = true

    }

}






class JSON {
    fun parse(json: String):List<HashMap<String,Any>> {
        val type = object : TypeToken<List<HashMap<String, Any>>>(){}.type
        val map = Gson().fromJson<List<HashMap<String,Any>>>(json, type)
        return map
    }

    fun stringify(json: HashMap<String,Boolean>) : String{
        val data = JSONObject(json).toString()
        return data
    }
}