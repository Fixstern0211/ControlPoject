package com.octopus.controlproject

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * @Author： zh
 * @Date： 25/11/20
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
        val action: String? = intent.action
        Log.e("TAG", "onReceive: $action")
        if (Intent.ACTION_BOOT_COMPLETED == action) {
            val mBootIntent = Intent(context, MainActivity::class.java)
            // automatically start the app
            mBootIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context!!.startActivity(mBootIntent)
            Log.e("TAG", "onReceive: FLAG_ACTIVITY_NEW_TASK")
        }
    }

}