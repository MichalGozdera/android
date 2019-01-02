package gozdi.eu.missedcallandsmsreminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import android.content.Context.POWER_SERVICE


class SMSReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val pref = context.getSharedPreferences("gozdi.eu.missedcallandsmsreminder", Context.MODE_PRIVATE)
        val flag = pref.getBoolean(context.getString(R.string.enabled), true)
        if(!flag){
            finito(context)
        }
        val screenLock = (context.getSystemService(POWER_SERVICE) as PowerManager).newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG:ScreenON"
        )
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            screenLock.acquire()
            Thread.sleep(100)
            screenLock.release()
        }
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        context.applicationContext.registerReceiver(screenOffReceiver, filter)

    }
    companion object {
        val screenOffReceiver = ScreenReceiver()
        fun finito(context: Context) {
            val fin = Intent(context, MainActivity::class.java)
            fin.putExtra("finish", true)
            fin.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.applicationContext.unregisterReceiver(screenOffReceiver)
            context.startActivity(fin)
        }
    }
}
