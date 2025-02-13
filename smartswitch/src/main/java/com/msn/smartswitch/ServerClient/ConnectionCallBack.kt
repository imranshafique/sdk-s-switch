package com.msn.smartswitch.ServerClient

interface ConnectionCallBack {

    fun onSuccess()
    fun onFailure(reason: String)
}