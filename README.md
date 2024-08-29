# ICS2000
Extract data from ICS2000 and send command to device.

This code is converted from python source because debugging within AndroidStudio is difficult.
It is not the complete projecten and you must use the commands with a thread.

The function pullDevices extract all data and decrypt the 'data' and 'status'. The extracted data is in a JSON format.

        core = Core()

        core.mac = "00:00:00:00:00:00"
        core.email = "your@email"
        core.password = "ics2000_password"

        icsLoader = ICS2000Loader()
        icsLoader!!.start()

        internal inner class ICS2000Loader() : Thread() {
        override fun run() {
            try {
                core.loginUser()
                if (core.isConnected()){
                    core.pullDevices()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                //messDialog?.dismissDialog()
            }
            finishedHandler.post { stopThread() }
        }
    }

    fun stopThread() {
        val x = core.json
    }
