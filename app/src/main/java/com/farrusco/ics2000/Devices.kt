package com.chaquo.myapplication

import android.util.Log

class Device {
    var _name = ""
    lateinit var _core: Core
    var _id = 0

    fun init(name: String, entity_id: Int, core: Core) {
        _core = core
        _name = name
        _id = entity_id
        Log.d("Device", "$_name : $_id")
    }

    fun turnoff() {
        val cmd = _core.simpleCmd(_id, 0, 0)
        _core.sendCommand(cmd.getCommand())

    }

    fun turnon() {
        val cmd = _core.simpleCmd(_id, 0, 1)
        _core.sendCommand(cmd.getCommand())
    }

    fun getstatus(): Boolean {
        return _core.getLampStatus(_id)
    }

    fun dim( level: Int) {
        if (level < 0 || level > 15){
            return
        }
        val cmd = _core.dim(_id, level)
        _core.sendCommand(cmd.getCommand())
    }
}