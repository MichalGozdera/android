package cokeman.pl.dozeswitcher;

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
    private TextView lifecycle;
    private TextView sdk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dozeSwitch = (Switch) findViewById(R.id.switch1);
        dozeReader = (TextView) findViewById(R.id.dozeReader);
        lifecycle = (TextView) findViewById(R.id.textView2);
        sdk = (TextView) findViewById(R.id.textView3);
        lifecycle.setText("OnCreate");
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

    @Override
    protected void onRestart() {
        startupStatus();
        super.onRestart();
        lifecycle.setText("OnRestart");
    }

    @Override
    protected void onStart() {
        startupStatus();
        super.onStart();
        lifecycle.setText("OnStart");
    }

    @Override
    protected void onResume() {
        startupStatus();
        super.onResume();
        lifecycle.setText("OnResume");
    }

    private Boolean checkDozeStatus() {

        try {
            // Executes the command.
            Process process = Runtime.getRuntime().exec("su");
            OutputStream out = process.getOutputStream();
            if(getSDK()>23&&getSDK()<26) {
                sdk.setText("Nougat");
                out.write("dumpsys deviceidle | grep mDeepEnabled\n".getBytes());
            }
            else if(getSDK()==23){
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
            } else {
                return false;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Integer getSDK() {
        try {
            // Executes the command.
            Process process = Runtime.getRuntime().exec("su");
            OutputStream out = process.getOutputStream();
            out.write("getprop ro.build.version.sdk\n".getBytes());
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

           String s= output.toString().replaceAll("\\D","");
           return Integer.parseInt(s);

        } catch (IOException e) {
            throw new RuntimeException(e);
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
