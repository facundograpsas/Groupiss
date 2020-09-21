package com.example.groupis.models

import com.google.firebase.database.IgnoreExtraProperties
import de.hdodenhof.circleimageview.CircleImageView

@IgnoreExtraProperties
class Group {

    private var title : String = ""
    private var picture : CircleImageView? = null
    private var size : Int? = 0
    private var lastMsgTime : Long? = null
    private var totalMessages : Int? = null
    private var color : Int? = null

    constructor()

    constructor(title : String){
        this.title = title
    }

    constructor(title :String, picture : CircleImageView?, size : Int?, lastMsgTime : Long?, totalMessages : Int?){
        this.title = title
        this.picture = picture
        this.size = size
        this.lastMsgTime = lastMsgTime
        this.totalMessages = totalMessages
        this.color = color
    }


    fun getTitle() : String{
        return title
    }

    fun setTitle(title : String){
        this.title = title
    }

    fun getPicture() : CircleImageView?{
        return picture
    }

    fun setPicture(picture : CircleImageView){
        this.picture = picture
    }

    fun addUserSize(){
        this.size = size?.plus(1)
    }

    fun getSize() : Int?{
        return size
    }

    fun setUserSize(size : Int){
        this.size = size
    }

    fun getLastMsgTime(): Long? {
        return lastMsgTime
    }

    fun setLastMsgTime(lastMsgTime : Long?){
        this.lastMsgTime = lastMsgTime
    }

    fun setTotalMessages(totalMessages : Int?){
        this.totalMessages = totalMessages
    }

    fun getTotalMessages() : Int?{
        return totalMessages
    }

    fun setColor(color : Int?){
        this.color = color
    }

    fun getColor() : Int?{
        return color
    }


}