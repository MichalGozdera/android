package cokeman.pl.dozeswitcher;

import android.content.DialogInterface;
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
    private TextView sdk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dozeSwitch = (Switch) findViewById(R.id.switch1);
        dozeReader = (TextView) findViewById(R.id.dozeReader);
        sdk = (TextView) findViewById(R.id.textView3);

        startupStatus();
        dozeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton cb, boolean on) {
                if (on) {
                    switchDoze(true);
                    dozeReader.setText("Doze enabled");
                } else {
                    switchDoze(false);
                    dozeReader.setText("Doze disabled");
                }
            }
        });
    }

    private void startupStatus() {

        if (checkDozeStatus()) {
            dozeReader.setText("Doze enabled");
            dozeSwitch.setChecked(true);
        } else {
            dozeReader.setText("Doze disabled");
            dozeSwitch.setChecked(false);
        }
    }

    private Boolean checkDozeStatus() {

        try {
            // Executes the command.
            Process process = Runtime.getRuntime().exec("su");
            OutputStream out = process.getOutputStream();
            if (getSDK() > 23 && getSDK() < 26) {
                sdk.setText("Nougat");
                out.write("dumpsys deviceidle | grep mDeepEnabled\n".getBytes());
            }
            else if (getSDK() > 25) {
                sdk.setText("Oreo");
                out.write("dumpsys deviceidle | grep mDeepEnabled\n".getBytes());
            }else if (getSDK() == 23) {
                sdk.setText("Marshmallow");
                out.write("dumpsys deviceidle | grep mEnabled\n".getBytes());
            }
            out.close();
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

            String s = output.toString();
            if (s.contains("true")) {
                return true;
            } else if (s.equals("")) {
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
                return false;
            } else {
                return false;
            }
        } catch (IOException e) {
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
            return false;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Integer getSDK() {
        try {
            // Executes the command.
            Process process = Runtime.getRuntime().exec("getprop ro.build.version.sdk\n");
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
            // Executes the command.
            Process process = Runtime.getRuntime().exec("su");
            OutputStream out = process.getOutputStream();
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
}
