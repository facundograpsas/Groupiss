package com.example.groupis.models

class Chat {

    private var text : String = ""
    private lateinit var username : String
    private lateinit var uid : String
    private var hour : String = ""

    constructor()
    constructor(text : String, username : String, uid : String, hour : String){
        this.text = text
        this.username = username
        this.uid = uid
    }

    fun getText() : String{
        return text
    }

    fun setText(text : String){
        this.text=text
    }

    fun getUsername() : String{
        return username
    }

    fun setUsername(username : String){
        this.username = username
    }
    
    fun setUid(uid : String){
        this.uid = uid
    }
    
    fun getUid() : String{
        return uid
    }

    fun setHour(hour : String){
        this.hour = hour
    }

    fun getHour() : String{
        return hour
    }

}