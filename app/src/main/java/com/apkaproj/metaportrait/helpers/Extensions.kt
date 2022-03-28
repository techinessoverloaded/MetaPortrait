package com.apkaproj.metaportrait.helpers

import android.content.Context
import android.view.View
import android.widget.Toast

fun Context.displayToast(message : String?)
{
    Toast.makeText(this,message,Toast.LENGTH_LONG).show()
}

fun View.show()
{
    this.visibility = View.VISIBLE
}

fun View.hide()
{
    this.visibility = View.GONE
}