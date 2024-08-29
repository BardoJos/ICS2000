package com.chaquo.myapplication

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.net.HttpURLConnection
import java.net.URL

class Core {

    val baseUrl = "https://trustsmartcloud2.com/ics2000_api/"
    var aes: String? = null
    var mac: String = ""
    var email: String = ""
    var password: String = ""
    var json = JsonArray()
    var homeId: Int = -1
    private var connected: Boolean = false

    //private val devices: MutableList<Device> = mutableListOf()
    val devices: MutableMap<String, Device> = HashMap()

    fun loginUser() {
        Log.d("loginUser", "Logging in user")
        val url = "$baseUrl/account.php"
        val params = getParams("login") + "&device_unique_id=android&platform=Android"
        val req = URL("$url?$params").openConnection() as HttpURLConnection
        req.requestMethod = "GET"
        if (req.responseCode == 200) {
            val resp = Gson().fromJson(req.inputStream.reader(), JsonObject::class.java)
            aes = resp["homes"].asJsonArray[0].asJsonObject["aes_key"].asString
            homeId = resp["homes"].asJsonArray[0].asJsonObject["home_id"].asInt
            if (aes != null) {
                Log.d("loginUser", "Successfully got AES key")
                connected = true
            }
        }
    }

    fun isConnected(): Boolean {
        return connected
    }

    fun pullDevices() {
        val JsonNull: String? = null
        val url = "$baseUrl/gateway.php"
        val params = getParams("sync") + "&home_id=$homeId"
        val req = URL("$url?$params").openConnection() as HttpURLConnection
        val resp = Gson().fromJson(req.inputStream.reader(), JsonArray::class.java)
        //val deviceTypes = DeviceType.values().map { it.name }

        json = JsonArray()
        for (device in resp) {
            val elem = JsonObject()
            for (key in (device as JsonObject).keySet()) {
                if (device[key].isJsonNull) {
                    elem.addProperty(key, JsonNull)
                } else {
                    var str = device[key].asString
                    if (key == "data" || key == "status") {
                        str = Crypto.decrypt(str, aes!!)
                        elem.add(key, JsonParser.parseString(str))
                    } else {
                        elem.addProperty(key, str)
                    }
                }
            }
            if (elem.size() > 0) {
                json.add(elem)
            }
        }
    }

    fun sendCommand(command: String) {
        val url = "$baseUrl/command.php"
        val params = getParams("add") + "&device_unique_id=$homeId&command=$command"
        val req = URL("$url?$params").openConnection() as HttpURLConnection
    }

    fun dim(entity: Int, level: Int): Command {
        val cmd = simpleCmd(entity, 1, level)
        return cmd
    }

    private fun constraintInt(inp: Int, min_val: Int, max_val: Int): Int {
        if (inp < min_val) {
            return min_val
        }
        if (inp > max_val) {
            return max_val
        }
        return inp
    }

    fun zigbeeColorTemp(entity: Int, colorTemp: Int) {
        val constrainedColorTemp = constraintInt(colorTemp, 0, 600)
        val cmd = simpleCmd(entity, 9, constrainedColorTemp)
        sendCommand(cmd.getCommand())
    }

    fun zigbeeDim(entity: Int, dimLvl: Int) {
        val constrainedDimLvl = constraintInt(dimLvl, 1, 254)
        val cmd = simpleCmd(entity, 4, constrainedDimLvl)
        sendCommand(cmd.getCommand())
    }

    fun zigbeeSwitch(entity: Int, power: Boolean) { // Replace Entity with actual type
        val cmd = simpleCmd(entity, 3, if (power) 1 else 0)
        sendCommand(cmd.getCommand())
    }

    fun zigbeeSocket(entity: Int, power: Boolean) {
        val cmd = simpleCmd(entity, 3, if (power) 1 else 0)
        sendCommand(cmd.getCommand())
    }

    fun getDeviceCheck(entity: Int): List<Any> {
        val url = "$baseUrl/entity.php"

        val params = getParams("check") + "&entity_id=$entity"
        val req = URL("$url?$params").openConnection() as HttpURLConnection
        val arr = Gson().fromJson(req.inputStream.reader(), JsonArray::class.java).toList()

        if (arr.size == 4) {
            try {
                val dcrpt = Crypto.decrypt(arr[3].toString(), aes!!)
                if (JsonObject().getAsJsonObject(dcrpt).has("module")) {
                    val module = JsonObject().getAsJsonObject(dcrpt)["module"]
                    if (JsonObject().getAsJsonObject(module.toString()).has("functions")) {
                        val functions = JsonObject().getAsJsonObject(module.toString())["functions"]
                        return functions.toString().toList()
                    }
                    //return dcrpt.getJSONObject("module").getJSONArray("functions").toList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return emptyList()
    }
    /*

inner class YourClassName {
    private val baseUrl = "your_base_url"
    private lateinit var _email: String
    private lateinit var mac: String
    private lateinit var _password: String
    private lateinit var _homeId: String
    private lateinit var aes: Any // Replace with actual type
    private val commandList = mutableListOf<Command>()


    fun getDeviceCheck(entity: Int): List<Any> {
        val url = "$baseUrl/entity.php"
        val params = mapOf(
            "action" to "check",
            "email" to _email,
            "mac" to mac.replace(":", ""),
            "password_hash" to _password,
            "entity_id" to entity.toString()
        )
        val resp = khttp.get(url, params = params)
        val arr = JSONObject(resp.text).toList()
        if (arr.size == 4) {
            try {
                val dcrpt = JSONObject(decrypt(arr[3] as String, aes))
                if (dcrpt.has("module") && dcrpt.getJSONObject("module").has("functions")) {
                    return dcrpt.getJSONObject("module").getJSONArray("functions").toList()
                }
            } catch (e: TypeError) {
                // Handle TypeError
            } catch (e: JSONException) {
                // Handle JSONDecodeError
            }
        }
        return emptyList()
    }

}
*/

    fun simpleCmd(entity: Int, function: Int, value: Int): Command {
        val cmd = Command()
        cmd.setMac(mac)
        cmd.setType(128)
        cmd.setMagic()
        cmd.setEntityId(entity)
        cmd.setData(
            "{\"module\":{\"id\":${entity},\"function\":${function},\"value\":${value}}}".toByteArray(),
            aes!!
        )
        return cmd
    }

    private fun getParams(action: String): String {
        val params =
            "action=$action&email=$email&mac=${mac.replace(":", "")}&password_hash=$password"
        return params
    }

    fun getDeviceStatus(entity: Int): List<Any> {
        //val entries = TextUtils.join(",", entity)
        val url = "$baseUrl/entity.php"
        val params = getParams("get-multiple") + "&entity_id=[$entity]"
        val req = URL("$url?$params").openConnection() as HttpURLConnection
        val arr = Gson().fromJson(req.inputStream.reader(), JsonArray::class.java).asList()
        //val arr = JSONObject(resp.text).toList()
        if (arr.size == 1 && arr[0] is JsonObject) {
            val obj = arr[0] as JsonObject
            if (obj.has("status") && obj["status"] != null) {
                val dcrpt = Crypto.decrypt(obj["status"].asString, aes!!)
                if (JsonParser.parseString(dcrpt).asJsonObject.has("module")) {
                    return JsonParser.parseString(dcrpt).asJsonObject["module"].asJsonObject["functions"].asString.toList()
                }
            }
        }
        return emptyList()
    }

    fun getLampStatus(entity: Int): Boolean {
        val status = getDeviceStatus(entity)
        return if (status.isNotEmpty()) {
            status[0] == 1
        } else {
            false
        }
    }

    enum class DeviceType(val id: Int) {
        SWITCH(1),
        DIMMER(2),
        ACTUATOR(3),
        MOTION_SENSOR(4),
        CONTACT_SENSOR(5),
        DOORBELL_ACDB_7000A(6),
        WALL_CONTROL_1_CHANNEL(7),
        WALL_CONTROL_2_CHANNEL(8),
        REMOTE_CONTROL_1_CHANNEL(9),
        REMOTE_CONTROL_2_CHANNEL(10),
        REMOTE_CONTROL_3_CHANNEL(11),
        REMOTE_CONTROL_16_CHANNEL(12),
        REMOTE_CONTROL_AYCT_202(13),
        CHIME(14),
        DUSK_SENSOR(15),
        ARC_REMOTE(16),
        ARC_CONTACT_SENSOR(17),
        ARC_MOTION_SENSOR(18),
        ARC_SMOKE_SENSOR(19),
        ARC_SIREN(20),
        DOORBELL_ACDB_7000B(21),
        AWMT(22),
        SOMFY_ACTUATOR(23),
        LIGHT(24),
        WALL_SWITCH_AGST_8800(25),
        WALL_SWITCH_AGST_8802(26),
        BREL_ACTUATOR(27),
        CONTACT_SENSOR_2(28),
        ARC_KEYCHAIN_REMOTE(29),
        ARC_ACTION_BUTTON(30),
        ARC_ROTARY_DIMMER(31),
        ZIGBEE_UNKNOWN_DEVICE(32),
        ZIGBEE_SWITCH(33),
        ZIGBEE_DIMMER(34),
        ZIGBEE_RGB(35),
        ZIGBEE_TUNABLE(36),
        ZIGBEE_MULTI_PURPOSE_SENSOR(37),
        ZIGBEE_LOCK(38),
        ZIGBEE_LIGHT_LINK_REMOTE(39),
        ZIGBEE_LIGHT(40),
        ZIGBEE_SOCKET(41),
        ZIGBEE_LEAKAGE_SENSOR(42),
        ZIGBEE_SMOKE_SENSOR(43),
        ZIGBEE_CARBON_MONOXIDE_SENSOR(44),
        ZIGBEE_TEMPERATURE_AND_HUMIDITY_SENSOR(45),
        ZIGBEE_LIGHT_GROUP(46),
        ZIGBEE_FIREANGEL_SENSOR(47),
        CAMERA_MODULE(48),
        LOCATION_MODULE(49),
        SYSTEM_MODULE(50),
        SECURITY_MODULE(53),
        ENERGY_MODULE(238),
        WEATHER_MODULE(244)
    }
}


