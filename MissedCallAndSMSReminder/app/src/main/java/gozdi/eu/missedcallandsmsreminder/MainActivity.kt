package gozdi.eu.missedcallandsmsreminder

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.content.SharedPreferences
import android.util.Log
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.CompoundButton


class MainActivity : Activity() {
    lateinit var preferences: SharedPreferences
    lateinit var prefEditor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fin = intent
        if (fin.hasExtra("finish")) {
            finish()
        }


        if (!checkPermissions()) {
            finish()
        }


        setContentView(R.layout.activity_main)
        preferences = this.getSharedPreferences("gozdi.eu.missedcallandsmsreminder", Context.MODE_PRIVATE)
        prefEditor = preferences.edit()
        setupScreen()
    }

    private fun setupScreen() {
        setupGlobalSwitch()
        setupRepeatsCounter()
        setupVibrationLength()
        setupPauseLength()
    }

    private fun setupPauseLength() {
        val lengthBar = findViewById(R.id.pauseLength) as SeekBar
        val lengthTV = findViewById(R.id.pauseLengthText) as TextView
        val storedLength = preferences.getInt(getString(R.string.pause_length), 500)
        lengthTV.setText("${getString(R.string.pause_length)} ${storedLength}ms")
        lengthBar.progress = storedLength / 500
        lengthBar.setOnSeekBarChangeListener((object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (seekBar != null) {
                    prefEditor.putInt(getString(R.string.pause_length), seekBar.progress * 500).commit()
                }
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                lengthTV.setText("${getString(R.string.pause_length)} ${progress * 500}ms")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

        }))
    }

    private fun setupVibrationLength() {
        val lengthBar = findViewById(R.id.vibrationLength) as SeekBar
        val lengthTV = findViewById(R.id.vibLegthText) as TextView
        val storedLength = preferences.getInt(getString(R.string.vibration_length), 2500)
        lengthTV.setText("${getString(R.string.vibration_length)} ${storedLength}ms")
        lengthBar.progress = storedLength / 500
        lengthBar.setOnSeekBarChangeListener((object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (seekBar != null) {
                    prefEditor.putInt(getString(R.string.vibration_length), seekBar.progress * 500).commit()
                }
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                lengthTV.setText("${getString(R.string.vibration_length)} ${progress * 500}ms")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

        }))
    }

    private fun setupRepeatsCounter() {
        val repeatsBar = findViewById(R.id.repeatsSet) as SeekBar
        val repeatsCounter = findViewById(R.id.repeatsCount) as TextView
        val storedCounter = preferences.getInt(getString(R.string.number_of_repeats), 5)
        repeatsCounter.setText("${getString(R.string.number_of_repeats)} ${storedCounter}")
        repeatsBar.progress = storedCounter
        repeatsBar.setOnSeekBarChangeListener((object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (seekBar != null) {
                    prefEditor.putInt(getString(R.string.number_of_repeats), seekBar.progress).commit()
                }
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                repeatsCounter.setText("${getString(R.string.number_of_repeats)} ${progress}")

            }
        }))
    }

    private fun setupGlobalSwitch() {
        val globalSwitch = findViewById(R.id.enabled) as Switch
        val globalFlagTextView = findViewById(R.id.globalFlag) as TextView
        var globalFlagString: String

        val isEnabled = preferences.getBoolean(getString(R.string.enabled), true)
        globalSwitch.isChecked = isEnabled
        globalFlagString = if (isEnabled) "Service enabled" else "Service disabled"
        globalFlagTextView.setText(globalFlagString)


        globalSwitch.setOnCheckedChangeListener((object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                prefEditor.putBoolean(getString(R.string.enabled), isChecked).commit()
                globalFlagString = if (isChecked) "Service enabled" else "Service disabled"
                globalFlagTextView.setText(globalFlagString)
            }

        }))
    }

    var permissions = arrayOf(
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_PHONE_STATE
    )

    private fun checkPermissions(): Boolean {
        var result: Int
        val listPermissionsNeeded = ArrayList<String>()
        for (p in permissions) {
            result = ContextCompat.checkSelfPermission(this, p)
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p)
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), 10)
            return false
        }
        return true
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissionsList: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            10 -> {
                if (grantResults.size > 0) {
                    var permissionsDenied = ""
                    for (per in permissionsList) {
                        if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                            permissionsDenied += "\n" + per

                        }

                    }
                }
                return
            }
        }
    }
}
