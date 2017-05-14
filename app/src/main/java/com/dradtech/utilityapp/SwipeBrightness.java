package com.dradtech.utilityapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

public class SwipeBrightness extends Activity {
    //TextViews to show details of volume and brightness
    private TextView tVBrightness,tVVolume;
    //SeekBars to set volume and brightness
    private SeekBar sbVolume,sbBrightness;
    //AudioManager object, that will get and set volume
    private AudioManager audioManager;
    //Variable to store brightness value
    private int brightness;
    //Content resolver used as a handle to the system's settings
    private ContentResolver cResolver;
    //Window object, that will store a reference to the current window
    private Window window;
    int maxVolume=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.swipe_brightness);
        //Suggests an audio stream whose volume should be changed by the hardware volume controls.
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        initializeControls();
    }


    private void initializeControls() {
        //get reference of the UI Controls
        sbVolume = (SeekBar) findViewById(R.id.sbVolume);
        sbBrightness = (SeekBar) findViewById(R.id.sbBrightness);
        tVVolume=(TextView)findViewById(R.id.tVVolume);
        tVBrightness = (TextView) findViewById(R.id.tVBrightness);

        try {

            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            //set max progress according to volume
            sbVolume.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            //get current volume
            sbVolume.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
            //Set the seek bar progress to 1
            sbVolume.setKeyProgressIncrement(1);
            //get max volume
            maxVolume=sbVolume.getMax();
            sbVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                    //Calculate the brightness percentage
                    float perc = (progress /(float)maxVolume)*100;
                    //Set the brightness percentage
                    tVVolume.setText("Volume: "+(int)perc +" %");
                }
            });

        } catch (Exception e) {

        }


        //Get the content resolver
        cResolver = getContentResolver();

        //Get the current window
        window = getWindow();

        //Set the seekbar range between 0 and 255
        sbBrightness.setMax(255);
        //Set the seek bar progress to 1
        sbBrightness.setKeyProgressIncrement(1);

        try
        {
            //Get the current system brightness
            brightness = Settings.System.getInt(cResolver, Settings.System.SCREEN_BRIGHTNESS);
        }
        catch (Settings.SettingNotFoundException e)
        {
            //Throw an error case it couldn't be retrieved
            Log.e("Error", "Cannot access system brightness");
            e.printStackTrace();
        }

        //Set the progress of the seek bar based on the system's brightness
        sbBrightness.setProgress(brightness);

        //Register OnSeekBarChangeListener, so it can actually change values
        sbBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            public void onStopTrackingTouch(SeekBar seekBar)
            {
                //Set the system brightness using the brightness variable value
                Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
                //Get the current window attributes
                WindowManager.LayoutParams layoutpars = window.getAttributes();
                //Set the brightness of this window
                layoutpars.screenBrightness = brightness / (float)255;
                //Apply attribute changes to this window
                window.setAttributes(layoutpars);
            }

            public void onStartTrackingTouch(SeekBar seekBar)
            {
                //Nothing handled here
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                //Set the minimal brightness level
                //if seek bar is 20 or any value below
                if(progress<=20)
                {
                    //Set the brightness to 20
                    brightness=20;
                }
                else //brightness is greater than 20
                {
                    //Set brightness variable based on the progress bar
                    brightness = progress;
                }
                //Calculate the brightness percentage
                float perc = (brightness /(float)255)*100;
                //Set the brightness percentage
                tVBrightness.setText("Brightness: "+(int)perc +" %");
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.M) public void setBrightness(View view, int brightness) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(getApplicationContext())) {
                if (brightness < 46) brightness = 255;
                else if (brightness > 47) brightness = 0;
                ContentResolver cResolver = this.getApplicationContext().getContentResolver();
                Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
            } else {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }

}

