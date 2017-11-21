package cokeman.pl.dozeswitcher;

import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import static android.os.Build.VERSION;
import static android.widget.CompoundButton.OnCheckedChangeListener;
import static cokeman.pl.dozeswitcher.Commands.COMMAND_DUMPSYS_DISABLE;
import static cokeman.pl.dozeswitcher.Commands.COMMAND_DUMPSYS_ENABLE;
import static cokeman.pl.dozeswitcher.Commands.COMMAND_DUMPSYS_GREP_ABOVE_M;
import static cokeman.pl.dozeswitcher.Commands.COMMAND_DUMPSYS_GREP_M;
import static cokeman.pl.dozeswitcher.Commands.COMMAND_SU;

public class MainActivity extends AppCompatActivity {
    private Switch dozeSwitch;
    private TextView dozeReaderTv;
    private TextView sdkVersionNumberTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initControls();
        setStartValues();
        setListeners();
    }

    private void initControls() {
        dozeSwitch = (Switch) findViewById(R.id.switch1);
        dozeReaderTv = (TextView) findViewById(R.id.doze_reader_tv);
        sdkVersionNumberTv = (TextView) findViewById(R.id.text_view);
    }

    private void setListeners() {
        dozeSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton cb, boolean on) {
                switchDoze(on);
            }
        });
    }

    private void setStartValues() {
        boolean status = checkDozeStatus();
        dozeSwitch.setChecked(status);
        changeDozeText(status);
    }

    private boolean checkDozeStatus() {
        try {
            Process process = Runtime.getRuntime().exec("su");
            OutputStream outputStream = process.getOutputStream();

            setVersionName();

            outputStream.write(getCommandBaseOnVersion(VERSION.SDK_INT));
            outputStream.close();

            String output = getStringBuilderOutput(process);

            if (output.contains("true")) {
                return true;
            } else if (output.equals("")) {
                noRootInfoDialog();
            }

        } catch (IOException e) {
            noRootInfoDialog();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    private byte[] getCommandBaseOnVersion(int apiVersion) {
        if (apiVersion > VERSION_CODES.M) {
            return COMMAND_DUMPSYS_GREP_ABOVE_M.getBytes();
        } else if (apiVersion == VERSION_CODES.M) {
            return COMMAND_DUMPSYS_GREP_M.getBytes();
        }
        return "".getBytes();
    }

    private void setVersionName() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            sdkVersionNumberTv.setText(packageInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    private String getStringBuilderOutput(Process process) throws IOException, InterruptedException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        int read;
        char[] buffer = new char[4096];
        StringBuilder output = new StringBuilder();
        while ((read = reader.read(buffer)) > 0) {
            output.append(buffer, 0, read);
        }
        reader.close();
        process.waitFor();
        return output.toString();
    }

    private void noRootInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.missing_root_title);
        builder.setMessage(R.string.missing_root_description);
        builder.setNeutralButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void switchDoze(boolean on) {
        try {
            Process process = Runtime.getRuntime().exec(COMMAND_SU);
            OutputStream out = process.getOutputStream();
            changeDozeText(on);

            if (on) {
                enableDoze(out);
            } else {
                disableDoze(out);
            }

            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void disableDoze(OutputStream out) throws IOException {
        out.write(COMMAND_DUMPSYS_DISABLE.getBytes());
        showToast(getString(R.string.doze_disabled));
    }

    private void enableDoze(OutputStream out) throws IOException {
        out.write(COMMAND_DUMPSYS_ENABLE.getBytes());
        showToast(getString(R.string.doze_enabled));
    }

    private void showToast(String text) {
        Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
    }

    private void changeDozeText(boolean on) {
        String dozeStatus = on ? getString(R.string.enabled) : getString(R.string.disabled);
        dozeReaderTv.setText(String.format(getString(R.string.doze_status), dozeStatus));
    }
}
