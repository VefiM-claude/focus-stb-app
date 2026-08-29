package de.fokusstb.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object LernplanLoader {

    fun load(context: Context): LernplanData {
        val text = context.assets.open("lernplan.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        val root = JSONObject(text)

        val exam = root.getJSONArray("exam").map { obj ->
            ExamDay(date = obj.getString("d"), label = obj.getString("n"))
        }

        val cats = root.getJSONArray("cats").map { obj ->
            Category(id = obj.getString("id"), name = obj.getString("name"), short = obj.getString("short"))
        }

        val topics = root.getJSONArray("topics").map { obj ->
            Topic(
                id = obj.getString("id"),
                name = obj.getString("name"),
                ref = obj.optString("ref", ""),
                cat = obj.getString("cat"),
                taskIds = obj.optJSONArray("tasks")?.mapStrings() ?: emptyList(),
            )
        }

        val tasks = root.getJSONArray("tasks").map { obj ->
            PlanTask(
                id = obj.getString("id"),
                title = obj.getString("title"),
                status = obj.optString("status", "offen"),
                date = obj.optString("date", ""),
                time = obj.optString("time", ""),
                type = obj.optString("type", "Lernblock"),
                min = obj.optInt("min", 0),
                topicIds = obj.optJSONArray("topics")?.mapStrings() ?: emptyList(),
                klausur = obj.optString("klausur", ""),
                notes = obj.optString("notes", ""),
            )
        }

        return LernplanData(
            today = root.getString("today"),
            exam = exam,
            cats = cats,
            topics = topics,
            tasks = tasks,
        )
    }

    private inline fun <T> JSONArray.map(transform: (JSONObject) -> T): List<T> =
        (0 until length()).map { transform(getJSONObject(it)) }

    private fun JSONArray.mapStrings(): List<String> = (0 until length()).map { getString(it) }
}
