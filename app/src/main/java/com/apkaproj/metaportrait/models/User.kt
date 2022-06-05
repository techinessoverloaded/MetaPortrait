package com.apkaproj.metaportrait.models

import java.io.Serializable

data class User(var name : String, var email : String, var userId : String, var tempKey : String) : Serializable
{
    constructor() : this("","","", "")
}