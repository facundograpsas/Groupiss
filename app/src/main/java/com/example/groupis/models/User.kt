package com.example.groupis.models

import java.io.Serializable

class User : Serializable{

 private var uid : String? = null
 private var name : String? = null
 private var email : String? = null
 private var nameId : String? = null

  constructor()
  constructor(uid: String, name: String, email:String, nameId : String){

    this.uid = uid
    this.name = name
    this.email = email
    this.nameId = nameId
   }

   fun getUid() : String?{
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

  fun getEmail() : String?{
   return email
  }

  fun setEmail(email : String?){
   this.email=email
  }

  fun getNameId() : String?{
   return nameId
  }

  fun setNameId(nameId : String?){
   this.nameId=nameId
  }

 }

