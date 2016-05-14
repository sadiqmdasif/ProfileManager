package com.apache.profilemanager;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class MyProfileService extends Service implements SensorEventListener {
    public Sensor mAccSensor;
    public Sensor mProxSensor;
    private SensorManager mSensorManager;
    private AudioManager mAudioManager;
    private Handler handler;
    private Runnable r;
    private boolean userChangedProfile = true;
    private boolean objectBeforeProx = false;
    private boolean deviceIsInPocket = false;
    private boolean deviceIsNotInPocket = true;
    private boolean deviceIsFaceDown = false;
    private boolean deviceIsPlacedOnSurface = false;
    private float previousX = 0;
    private float previousY = 0;
    private float previousZ = 0;
    private float currentX = 0, currentY = 0, currentZ = 0;
    private float proximityX;
    public MyProfileService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mProxSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        Context context = getApplicationContext();
        handler = new Handler();

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            mSensorManager.registerListener(this, mAccSensor, 5000);
            if (mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null) {
                mSensorManager.registerListener(this, mProxSensor, 5000);
            } else {
                String text = "You do not have Proximity Sensor";
                Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
                toast.show();
            }
        } else {
            String text = "You do not have Accelerometer Sensor ";
            Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
            toast.show();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {

        handler.removeCallbacks(r);
        mSensorManager.unregisterListener(this, mAccSensor);
        mSensorManager.unregisterListener(this, mProxSensor);
        super.onDestroy();
    }


    @Override
    public void onSensorChanged(final SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            r = new Runnable() {
                int count = 0;

                public void run() {

                    handler.postDelayed(this, 5000);

                    if (count == 0) {
                        float x = event.values[0];
                        float y = event.values[1];
                        float z = event.values[2];
                        valueChangedAccelerometer(x, y, z);

                        devicePosition(x, y, z);
                        previousX = x;
                        previousY = y;
                        previousZ = z;

                        mSensorManager.unregisterListener(MyProfileService.this, mAccSensor);
                        count++;

                    } else {

                        mSensorManager.registerListener(MyProfileService.this, mAccSensor, 5000);
                        count = 0;
                        handler.removeCallbacks(this);
                    }
                }
            };
            r.run();
        }
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            proximityX = event.values[0];
            valueChangedProximity(proximityX);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    void valueChangedAccelerometer(float x, float y, float z) {


    }

    void mMessage(String toastMsg) {

        Context context = getApplicationContext();
        String text = toastMsg;
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    void valueChangedProximity(float x) {


        r = new Runnable() {
            int count = 0;

            public void run() {
                handler.postDelayed(this, 5000);

                if (count == 0) {
                    Log.d("Proximity", "" + proximityX);
                    if (proximityX == 0) {
                        count++;
                        objectBeforeProx = true;
                        mSensorManager.unregisterListener(MyProfileService.this, mProxSensor);

                    } else {
                        Log.d("Proximity", "" + proximityX);
                        count++;
                        objectBeforeProx = false;
                        mSensorManager.unregisterListener(MyProfileService.this, mProxSensor);

                    }
                } else {

                    mSensorManager.registerListener(MyProfileService.this, mProxSensor, 5000);
                    count = 0;
                    handler.removeCallbacks(this);
                }
            }
        };
        r.run();
    }

    void devicePosition(float currentX, float currentY, float currentZ) {
        float changeX = Math.abs(previousX - currentX);
        float changeY = Math.abs(previousY - currentY);
        float changeZ = Math.abs(previousZ - currentZ);

        float change = changeX + changeY + changeZ;


        //Log.d("Change",String.valueOf(change)+" "+String.valueOf(changeX)+" "+String.valueOf(changeY)+" "+String.valueOf(changeZ));


        if (changeX > 1 && changeY > 1 && changeZ > 1 && objectBeforeProx == true) {
            changeAudioProfile(2);
            deviceIsInPocket = true;

            deviceIsNotInPocket = false;
            deviceIsFaceDown = false;
            deviceIsPlacedOnSurface = false;
        } else if (changeX > 2 && objectBeforeProx == false) {
            changeAudioProfile(1);
            deviceIsNotInPocket = true;

            deviceIsInPocket = false;
            deviceIsFaceDown = false;
            deviceIsPlacedOnSurface = false;
        } else if (currentX < 1 && currentY < 1 && currentZ < -8.5 && objectBeforeProx == true && deviceIsInPocket == false) {

            changeAudioProfile(0);
            deviceIsFaceDown = true;

            deviceIsInPocket = false;
            deviceIsNotInPocket = false;
            deviceIsPlacedOnSurface = false;
        } else if (currentX < 2 && currentY < 2 && currentZ < -5 && objectBeforeProx == false && deviceIsInPocket == false) {

            changeAudioProfile(1);
            //using when lying down

            deviceIsInPocket = false;
            deviceIsNotInPocket = false;
            deviceIsFaceDown = false;
            deviceIsPlacedOnSurface = false;
        } else if (change < 1 && currentZ > 8.5 && objectBeforeProx == false) {

            changeAudioProfile(2);
            deviceIsPlacedOnSurface = true;

            deviceIsInPocket = false;
            deviceIsNotInPocket = false;
            deviceIsFaceDown = false;

        } else if (change < 1 && currentZ > 8.5 && objectBeforeProx == true) {

            changeAudioProfile(2);
            //mobile is under something

            deviceIsPlacedOnSurface = true;
            deviceIsInPocket = false;
            deviceIsNotInPocket = false;
            deviceIsFaceDown = false;

        }
    }

    void changeAudioProfile(int i) {

        String msg = "";
        switch (i) {
            case 0:
                if (mAudioManager.getRingerMode() != 0) {
                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                    msg = "Silent Profile Activated";
                    mMessage(msg);
                }
                break;
            case 1:
                if (mAudioManager.getRingerMode() != 1) {
                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                    msg = "Vibrate Profile Activated";
                    mMessage(msg);
                }
                break;
            case 2:
                if (mAudioManager.getRingerMode() != 2) {
                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    msg = "Normal Profile Activated";
                    mMessage(msg);
                }
                break;
            default:
                break;
        }


    }


}
