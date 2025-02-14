package com.msn.smartswitch.Models


import java.net.InetAddress


object AppConstant {
    const val TAG = "NewSmartSwitch"

    @JvmStatic
    var serverAddress: InetAddress? = null
    var selectedPath: ArrayList<String> = ArrayList()

}
