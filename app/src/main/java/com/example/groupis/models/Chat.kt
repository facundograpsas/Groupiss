package com.example.groupis.models

class Chat {

    private var text : String = ""
    private lateinit var username : String
    private lateinit var uid : String
    private var hour : String = ""
    private var day : String = ""
    private var key : String? = ""
    private var timeInMillis : Long? = null

    constructor()
    constructor(text : String, username : String, uid : String, hour : String, day : String, timeInMillis : Long?){
        this.text = text
        this.username = username
        this.uid = uid
        this.hour = hour
        this.day = day
        this.timeInMillis = timeInMillis
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

    fun setDay(day : String){
        this.day = day
    }

    fun getDay() : String{
        return day
    }

    fun setKey(key : String?){
        this.key = key
    }

    fun getKey() : String?{
        return key
    }

    fun getTimeInMillis(): Long?{
        return timeInMillis
    }

    fun setTimeInMillis(timeInMillis: Long?){
        this.timeInMillis = timeInMillis
    }
}