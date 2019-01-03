package gozdi.eu.missedcallandsmsreminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import android.content.Context.POWER_SERVICE
import android.util.Log


class SMSReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val pref = context.getSharedPreferences("gozdi.eu.missedcallandsmsreminder", Context.MODE_PRIVATE)
        val flag = pref.getBoolean(context.getString(R.string.enabled), true)
        if (!flag) {
            finito(context)
        }
        val screenLock = (context.getSystemService(POWER_SERVICE) as PowerManager).newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG:ScreenON"
        )
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            screenLock.acquire(500)
            if (screenLock.isHeld) {
                screenLock.release()
            }
        } else if (intent.getAction().equals("android.intent.action.EVENT_REMINDER")) {
            screenLock.acquire(500)
            if (screenLock.isHeld) {
                screenLock.release()
            }
            flagScreenOffRegistered=false
            finito(context)
        }
        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")||intent.getAction().equals("android.intent.action.PHONE_STATE")){
            val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
            context.applicationContext.registerReceiver(screenOffReceiver, filter)
            flagScreenOffRegistered = true
        }

    }

    companion object {
        val screenOffReceiver = ScreenReceiver()
        var flagScreenOffRegistered = false
        fun finito(context: Context) {
            val fin = Intent(context, MainActivity::class.java)
            fin.putExtra("finish", true)
            fin.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if(flagScreenOffRegistered){
                context.applicationContext.unregisterReceiver(screenOffReceiver)
            }
            context.startActivity(fin)
        }
    }
}
