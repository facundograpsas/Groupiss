package com.example.groupis.models

import de.hdodenhof.circleimageview.CircleImageView

class Group {

    private lateinit var title : String
    private var picture : CircleImageView? = null
    private var size : Int? = 0

    constructor()

    constructor(title :String, picture : CircleImageView?){
        this.title = title
        this.picture = picture
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


}