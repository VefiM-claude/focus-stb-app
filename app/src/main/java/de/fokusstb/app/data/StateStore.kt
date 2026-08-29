package de.fokusstb.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persists the same shape the prototype kept under one localStorage key
 * ("fokus-stb-v1"): task status, ratings, notes, the Merkliste queue,
 * weekday availability, weight/pace overrides, renamed Gebiete, self-rating
 * history, total focused minutes and the profile. Nothing leaves the device.
 */
class StateStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("fokus-stb-v1", Context.MODE_PRIVATE)
    private val KEY = "state"

    fun load(): PersistedState? {
        val raw = prefs.getString(KEY, null) ?: return null
        return try {
            parse(JSONObject(raw))
        } catch (e: Exception) {
            null
        }
    }

    fun save(state: PersistedState) {
        prefs.edit().putString(KEY, serialize(state).toString()).apply()
    }

    fun clear() {
        prefs.edit().remove(KEY).apply()
    }

    private fun serialize(s: PersistedState): JSONObject = JSONObject().apply {
        put("status", JSONObject(s.status))
        put("ratings", JSONObject(s.ratings))
        put("notes", JSONObject().apply {
            s.notes.forEach { (catId, notes) ->
                put(catId, JSONArray().apply {
                    notes.forEach { n ->
                        put(JSONObject().apply { put("id", n.id); put("at", n.at); put("text", n.text) })
                    }
                })
            }
        })
        put("queue", JSONArray(s.queue))
        put("days", JSONArray().apply {
            s.days.forEach { d ->
                put(JSONObject().apply { put("d", d.d); put("label", d.label); put("on", d.on); put("min", d.min) })
            }
        })
        put("nightBudget", s.nightBudget)
        put("weight", JSONObject(s.weight))
        put("pace", JSONObject(s.pace))
        put("catNames", JSONObject(s.catNames))
        put("history", JSONArray().apply {
            s.history.forEach { h -> put(JSONObject().apply { put("d", h.d); put("v", h.v) }) }
        })
        put("doneMin", s.doneMin)
        put("profile", JSONObject().apply {
            put("name", s.profile.name); put("anrede", s.profile.anrede); put("gender", s.profile.gender)
        })
    }

    private fun parse(o: JSONObject): PersistedState {
        val status = mutableMapOf<String, String>()
        o.optJSONObject("status")?.let { obj ->
            obj.keys().forEach { k -> status[k] = obj.getString(k) }
        }
        val ratings = mutableMapOf<String, Double>()
        o.optJSONObject("ratings")?.let { obj ->
            obj.keys().forEach { k -> ratings[k] = obj.getDouble(k) }
        }
        val notes = mutableMapOf<String, List<Note>>()
        o.optJSONObject("notes")?.let { obj ->
            obj.keys().forEach { k ->
                val arr = obj.getJSONArray(k)
                notes[k] = (0 until arr.length()).map { i ->
                    val n = arr.getJSONObject(i)
                    Note(id = n.optLong("id"), at = n.optString("at"), text = n.optString("text"))
                }
            }
        }
        val queue = mutableListOf<String>()
        o.optJSONArray("queue")?.let { arr -> (0 until arr.length()).forEach { queue.add(arr.getString(it)) } }
        val days = o.optJSONArray("days")?.let { arr ->
            (0 until arr.length()).map { i ->
                val d = arr.getJSONObject(i)
                DayAvailability(d.getString("d"), d.optString("label"), d.getBoolean("on"), d.getInt("min"))
            }
        } ?: PersistedState.defaultDays()
        val weight = mutableMapOf<String, String>()
        o.optJSONObject("weight")?.let { obj -> obj.keys().forEach { k -> weight[k] = obj.getString(k) } }
        val pace = mutableMapOf<String, String>()
        o.optJSONObject("pace")?.let { obj -> obj.keys().forEach { k -> pace[k] = obj.getString(k) } }
        val catNames = mutableMapOf<String, String>()
        o.optJSONObject("catNames")?.let { obj -> obj.keys().forEach { k -> catNames[k] = obj.getString(k) } }
        val history = o.optJSONArray("history")?.let { arr ->
            (0 until arr.length()).map { i ->
                val h = arr.getJSONObject(i)
                HistoryPoint(h.getString("d"), h.getInt("v"))
            }
        } ?: PersistedState.defaultHistory()
        val profileObj = o.optJSONObject("profile")
        val profile = Profile(
            name = profileObj?.optString("name", "") ?: "",
            anrede = profileObj?.optString("anrede", "du") ?: "du",
            gender = profileObj?.optString("gender", "keine") ?: "keine",
        )
        return PersistedState(
            status = status, ratings = ratings, notes = notes, queue = queue, days = days,
            nightBudget = o.optInt("nightBudget", 90), weight = weight, pace = pace, catNames = catNames,
            history = history, doneMin = o.optInt("doneMin", 0), profile = profile,
        )
    }
}
