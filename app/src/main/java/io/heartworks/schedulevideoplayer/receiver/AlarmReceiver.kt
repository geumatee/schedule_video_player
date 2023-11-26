package io.heartworks.schedulevideoplayer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, p1: Intent?) {
        context?.sendBroadcast(Intent("play"))
    }
}