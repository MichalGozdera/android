package gozdi.eu.missedcallandsmsreminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.SharedPreferences
import android.net.Uri
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.provider.CallLog
import android.widget.Toast
import android.support.v4.content.ContextCompat.getSystemService
import android.media.AudioManager


class ScreenReceiver : BroadcastReceiver() {


    var vibrateFlag = false
    lateinit var vib:Vibrator
    lateinit var context1 :Context
    override fun onReceive(context: Context, intent: Intent) {
        context1=context
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        vib = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val unreadMessagesCount = getUnreadSMSCount(context)
        val missedCallsCount = getUnreadMissedCallsCount(context)

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF) && (unreadMessagesCount > 0 || missedCallsCount > 0) && !am.ringerMode.equals(
                AudioManager.RINGER_MODE_SILENT
            )
        ) {
            vibrateFlag=true
            vibServ.start()

        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)&&vibrateFlag==true) {
            vib.cancel()
            vibServ.interrupt()
            vibrateFlag=false
            SMSReceiver.finito(context)
        }
    }


    private fun getUnreadMissedCallsCount(context: Context): Int {
        val projection = arrayOf(CallLog.Calls.CACHED_NAME, CallLog.Calls.CACHED_NUMBER_LABEL, CallLog.Calls.TYPE)
        val where = CallLog.Calls.TYPE + "=" + CallLog.Calls.MISSED_TYPE + " AND " + CallLog.Calls.IS_READ + "=0"
        val cu = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, projection, where, null, null)
        cu.moveToFirst()
        return cu.getCount()
    }

    private fun getUnreadSMSCount(context: Context): Int {
        val c = context.getContentResolver().query(
            Uri.parse("content://sms/inbox"),
            arrayOf("count(_id)"),
            "read = 0",
            null, null
        )
        c.moveToFirst()
        val unreadMessagesCount = c.getInt(0)
        return unreadMessagesCount
    }

    val vibServ = object : Thread() {
        override fun run() {
            var context=context1
            val preferences = context.getSharedPreferences("gozdi.eu.missedcallandsmsreminder", Context.MODE_PRIVATE)
            val pauseLength = preferences.getInt(context.getString(R.string.pause_length), 500)
            val vibrationLength = preferences.getInt(context.getString(R.string.vibration_length), 2500)
            val repeatsCount = preferences.getInt(context.getString(R.string.number_of_repeats), 5)
            var effect: VibrationEffect = VibrationEffect.createWaveform(longArrayOf(0, vibrationLength.toLong()), 0)
            try {
                for (i in 1..repeatsCount) {
                    vib.vibrate(effect)
                    Thread.sleep(vibrationLength.toLong())
                    vib.cancel()
                    Thread.sleep(pauseLength.toLong())
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

        }
    }

}
