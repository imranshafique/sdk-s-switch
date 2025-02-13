package com.msn.smartswitch.Models


import java.net.InetAddress


object AppConstant {
    const val TAG = "NewSmartSwitch"

    @JvmStatic
    var serverAddress: InetAddress? = null
      var listOfSelectedItemsFilesLength: ArrayList<Long> = ArrayList()
    var listOfSelectedItemsFileNames: ArrayList<String> = ArrayList()
    var selectedPath: ArrayList<String> = ArrayList()

}
