package com.example.pc_anonymous.aplicacionfinalkotlink

import android.app.AlertDialog
import android.content.Context

fun showAlertDialog(context: Context, init: AlertDialog.Builder.() -> Unit) {
    val builder = AlertDialog.Builder(context)
    builder.init()
    builder.show()
}
