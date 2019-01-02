package gozdi.eu.missedcallandsmsreminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.net.Uri

import android.os.Vibrator
import android.util.Log
import android.provider.CallLog
import android.widget.Toast

import android.media.AudioManager
import android.os.PowerManager
import android.os.VibrationEffect


class ScreenReceiver : BroadcastReceiver() {
    var vibrateFlag = false
    override fun onReceive(context: Context, intent: Intent) {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val unreadMessagesCount = getUnreadSMSCount(context)
        val missedCallsCount = getUnreadMissedCallsCount(context)
        val preferences =
            context.getSharedPreferences("gozdi.eu.missedcallandsmsreminder", Context.MODE_PRIVATE)
        val pauseLength = preferences.getInt(context.getString(R.string.pause_length), 500).toLong()
        val vibrationLength = preferences.getInt(context.getString(R.string.vibration_length), 2500).toLong()
        val repeatsCount = preferences.getInt(context.getString(R.string.number_of_repeats), 5)
        val pattern = Array<Long>(repeatsCount * 2 + 1, { i -> 0 })
        val aplitudePattern = Array<Int>(repeatsCount * 2 + 1, { i -> 0 })
        for (i in 1..repeatsCount) {
            pattern.set(i * 2 - 1, vibrationLength)
            aplitudePattern.set(i * 2 - 1, 255)
            pattern.set(i * 2, pauseLength)
        }
        val vib = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val effect = VibrationEffect.createWaveform(pattern.toLongArray(),aplitudePattern.toIntArray(),-1)
        val screenLock = (context.getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG:ScreenON"
        )
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF) && (unreadMessagesCount > 0 || missedCallsCount > 0) && !am.ringerMode.equals(
                AudioManager.RINGER_MODE_SILENT) && vibrateFlag==false
        ) {
            screenLock.acquire(repeatsCount * (vibrationLength + pauseLength))
            vibrateFlag = true
            vib.vibrate(effect)

        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF) && vibrateFlag == true) {
            vib.cancel()
            vibrateFlag = false
            if(screenLock.isHeld){
                screenLock.release()
            }
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

}
