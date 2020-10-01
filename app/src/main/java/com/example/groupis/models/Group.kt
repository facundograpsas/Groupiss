package com.example.groupis.models

import com.google.firebase.database.IgnoreExtraProperties
import de.hdodenhof.circleimageview.CircleImageView
import java.io.Serializable

@IgnoreExtraProperties
class Group : Serializable {

    private var title : String = ""
    private var picture : String? = null
    private var size : Int? = 0
    private var lastMsgTime : Long? = null
    private var totalMessages : Int? = null
    private var color : Int? = null
    private var isWriting : String = "None"
    private var groupId : Int? = null

    constructor()

    constructor(title : String){
        this.title = title
    }

    constructor(title: String, groupId : Int){
        this.title = title
        this.groupId = groupId
    }

    constructor(title :String, picture : String?, size : Int?, lastMsgTime : Long?, totalMessages : Int?, isWriting : String, groupId : Int?){
        this.title = title
        this.picture = picture
        this.size = size
        this.lastMsgTime = lastMsgTime
        this.totalMessages = totalMessages
        this.color = color
        this.isWriting = isWriting
        this.groupId = groupId
    }


    fun getTitle() : String{
        return title
    }

    fun setTitle(title : String){
        this.title = title
    }

    fun getPicture() : String?{
        return picture
    }

    fun setPicture(picture : String){
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

    fun setIsWriting(isWriting : String){
        this.isWriting = isWriting
    }

    fun getIsWriting() : String{
        return isWriting
    }

    fun getGroupId() : Int{
      return groupId!!
    }

    fun setGroupId(groupId: Int?){
        this.groupId = groupId
    }
}