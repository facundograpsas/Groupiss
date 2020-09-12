package com.example.groupis.models

class User{

 private var uid : String? = null
 private var name : String? = null
 private var email : String? = null
 private var username : String? = null

  constructor()
  constructor(uid: String, name: String, email:String, username : String){

    this.uid = uid
    this.name = name
    this.email = email
    this.username = username
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

  fun getUsername() : String?{
   return username
  }

  fun setUsername(username : String?){
   this.username=username
  }

 }

