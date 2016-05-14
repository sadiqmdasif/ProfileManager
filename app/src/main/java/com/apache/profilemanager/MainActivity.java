package com.apache.profilemanager;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

public class MainActivity extends Activity {

    Switch aSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences prefs = getSharedPreferences("com.apache.profilemanager", MODE_PRIVATE);
        boolean switchState = prefs.getBoolean("service_status", false);

        aSwitch = (Switch) findViewById(R.id.switchProfile);

        aSwitch.setChecked(switchState);

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = getSharedPreferences("com.apache.profilemanager", MODE_PRIVATE).edit();

                if (isChecked) {
                    editor.putBoolean("service_status", aSwitch.isChecked());
                    editor.commit();

                    startService(new Intent(getBaseContext(), MyProfileService.class));
                } else {
                    editor.putBoolean("service_status", aSwitch.isChecked());
                    editor.commit();
                    stopService(new Intent(getBaseContext(), MyProfileService.class));
                }
            }
        });

    }


}
