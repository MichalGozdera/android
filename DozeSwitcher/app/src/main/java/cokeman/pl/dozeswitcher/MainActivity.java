package cokeman.pl.dozeswitcher;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    private Switch dozeSwitch;
    private TextView dozeReader;
    private TextView sdkVersionNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dozeSwitch = (Switch) findViewById(R.id.switch1);
        dozeReader = (TextView) findViewById(R.id.dozeReader);
        sdkVersionNumber = (TextView) findViewById(R.id.textView3);

        startupStatus();
        dozeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton cb, boolean on) {
                switchDoze(on);
            }
        });
    }

    private void startupStatus() {
        dozeSwitch.setChecked(checkDozeStatus());
        changeDozeText(checkDozeStatus());
    }

    private Boolean checkDozeStatus() {
        try {
            Process process = Runtime.getRuntime().exec("su");
            OutputStream out = process.getOutputStream();
            if (getSDKVersionNumber() > 23) {
                out.write("dumpsys deviceidle | grep mDeepEnabled\n".getBytes());
                if (getSDKVersionNumber() < 26) {
                    sdkVersionNumber.setText("Nougat");
                } else {
                    sdkVersionNumber.setText("Oreo");
                }
            } else if (getSDKVersionNumber() == 23) {
                sdkVersionNumber.setText("Marshmallow");
                out.write("dumpsys deviceidle | grep mEnabled\n".getBytes());
            }
            out.close();
            StringBuffer output = getStringBuffer(process);
            String s = output.toString();
            if (s.contains("true")) {
                return true;
            } else if (s.equals("")) {
                noRootInfo();
                return false;
            } else {
                return false;
            }
        } catch (IOException e) {
            noRootInfo();
            return false;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    private StringBuffer getStringBuffer(Process process) throws IOException, InterruptedException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));

        int read;
        char[] buffer = new char[4096];
        StringBuffer output = new StringBuffer();
        while ((read = reader.read(buffer)) > 0) {
            output.append(buffer, 0, read);
        }
        reader.close();
        process.waitFor();
        return output;
    }

    private void noRootInfo() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setTitle("Missing root");
        builder1.setMessage("Please root your device first");
        builder1.setCancelable(false);
        builder1.setNeutralButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    private Integer getSDKVersionNumber() {
        try {
            Process process = Runtime.getRuntime().exec("getprop ro.build.version.sdk\n");
            StringBuffer output = getStringBuffer(process);
            String s = output.toString().replaceAll("\\D", "");
            return Integer.parseInt(s);
        } catch (IOException e) {
            throw new RuntimeException();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private void switchDoze(boolean on) {
        try {
            Process process = Runtime.getRuntime().exec("su");
            OutputStream out = process.getOutputStream();
            changeDozeText(on);
            if (on) {
                out.write("dumpsys deviceidle enable\n".getBytes());
                Toast.makeText(MainActivity.this, "Doze enabled", Toast.LENGTH_SHORT).show();
            } else {
                out.write("dumpsys deviceidle disable\n".getBytes());
                Toast.makeText(MainActivity.this, "Doze disabled", Toast.LENGTH_SHORT).show();
            }
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void changeDozeText(boolean on) {
        String dozeStatus = on ? "enabled" : "disabled";
        dozeReader.setText("Doze " +dozeStatus);
    }
}
