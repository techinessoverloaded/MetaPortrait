package com.apkaproj.metaportrait.helpers

import android.content.Context
import android.view.View
import android.widget.Toast

fun Context.displayToast(message: String?)
{
    Toast.makeText(this,message,Toast.LENGTH_LONG).show()
}

fun View.show()
{
    visibility = View.VISIBLE
}

fun View.hide()
{
    visibility = View.GONE
}

fun View.isVisible(): Boolean
{
    return visibility == View.VISIBLE
}

fun View.isGone(): Boolean
{
    return visibility == View.GONE
}