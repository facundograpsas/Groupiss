package com.app.groupis.models

import java.io.Serializable

class User : Serializable{

    private var uid: String? = null
    private var name: String? = null
    private var email: String? = null
    private var nameId: String? = null
    private var pictureRef: String? = null

    constructor()
    constructor(uid: String, name: String, email: String, nameId: String, pictureRef: String?) {

        this.uid = uid
        this.name = name
        this.email = email
        this.nameId = nameId
        this.pictureRef = pictureRef
    }

    fun getUid(): String? {
        return uid
   }

  fun setUid(uid : String?){
   this.uid=uid
  }

  fun getName() : String?{
   return name
  }

  fun setName(name : String?){
   this.name=name
  }

    fun getEmail(): String? {
        return email
    }

    fun setEmail(email: String?) {
        this.email = email
    }

    fun getNameId(): String? {
        return nameId
    }

    fun setNameId(nameId: String?) {
        this.nameId = nameId
    }

    fun setPictureRef(hasPicture: String?) {
        this.pictureRef = hasPicture
    }

    fun getPictureRef(): String? {
        return pictureRef
    }

}

