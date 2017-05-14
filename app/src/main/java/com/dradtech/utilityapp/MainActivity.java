package com.dradtech.utilityapp;

import android.Manifest;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    /*  Permission request code to draw over other apps  */
    private static final int DRAW_OVER_OTHER_APP_PERMISSION_REQUEST_CODE = 1222;

    protected static final int WRITE_SETTINGS_CODE = 23;

    private CheckBox overlayId, lockDeviceId, screenshotId, brightnessControlId;

    protected static final int LOCK_DEVICE_REQUEST_CODE = 11;
    DevicePolicyManager devicePolicyManager;
    ComponentName adminComponent;

    private static final int REQUEST_EXTERNAL_STORAGE = 0;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    static final Integer WRITE_EXTERNAL_STORAGE_PERMISSION = 0x3;

    public static final String MyPREFERENCES = "MyPrefs" ;
    SharedPreferences sharedpreferences;

    private boolean isScreenshot = false;
    private boolean isOverlay = false;
    private boolean isLocalDevice = false;
    private boolean isBrightnessControl = false;

    private TextView txtHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.myToolBar);
        setSupportActionBar(toolbar);

        txtHeader = (TextView) findViewById(R.id.toolbarTitle);
        txtHeader.setText(R.string.app_name);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //Overlay checkbox onCheckedListener
        overlayId = (CheckBox) findViewById(R.id.overlayId);
        overlayId.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    try{
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(MainActivity.this)) {
                            //If the draw over permission is not available open the settings screen
                            //to grant the permission.

                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:" + getPackageName()));
                            startActivityForResult(intent, DRAW_OVER_OTHER_APP_PERMISSION_REQUEST_CODE);
                        }
                        else {
                            Toast.makeText(MainActivity.this, "Overlay permission is already granted.", Toast.LENGTH_SHORT).show();
                            if( (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)){
                                saveOverlayPref();
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });

        sharedpreferences = getSharedPreferences(MyPREFERENCES,Context.MODE_PRIVATE);
        isOverlay = sharedpreferences.getBoolean("isOverlay", false);

        if(isOverlay){
            overlayId.setChecked(true);
        }

        //lockDevice checkbox onCheckedListener
        lockDeviceId = (CheckBox) findViewById(R.id.lockDeviceId);
        lockDeviceId.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    try{
                        adminComponent = new ComponentName(MainActivity.this, DarClass.class);
                        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                        if (!devicePolicyManager.isAdminActive(adminComponent)) {

                            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
                            startActivityForResult(intent, LOCK_DEVICE_REQUEST_CODE);
                        } else {
                            Toast.makeText(MainActivity.this, "Screen Lock permission is already granted.", Toast.LENGTH_SHORT).show();
                            if((Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)){
                                saveLocalDevicePref();
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });

        sharedpreferences = getSharedPreferences(MyPREFERENCES,Context.MODE_PRIVATE);
        isLocalDevice = sharedpreferences.getBoolean("isLocalDevice", false);

        if(isLocalDevice){
            lockDeviceId.setChecked(true);
        }

        //screenshot checkbox onCheckedListener
        screenshotId = (CheckBox) findViewById(R.id.screenshotId);
        screenshotId.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    //verifyStoragePermissions(MainActivity.this);
                    try{
                        askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,WRITE_EXTERNAL_STORAGE_PERMISSION);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });

        sharedpreferences = getSharedPreferences(MyPREFERENCES,Context.MODE_PRIVATE);
        isScreenshot = sharedpreferences.getBoolean("isScreenshot", false);

        if(isScreenshot){
            screenshotId.setChecked(true);
        }

        //brightness control checkbox onCheckedListener
        brightnessControlId = (CheckBox) findViewById(R.id.brightnessId);
        brightnessControlId.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    try{
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(getApplicationContext())) {
                            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivityForResult(intent, WRITE_SETTINGS_CODE);
                        }else{
                            Toast.makeText(MainActivity.this, "Brightness control permission is already granted.", Toast.LENGTH_SHORT).show();
                            if( (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)){
                                saveBrightnessControlPref();
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });

        sharedpreferences = getSharedPreferences(MyPREFERENCES,Context.MODE_PRIVATE);
        isBrightnessControl = sharedpreferences.getBoolean("isBrightnessControl", false);

        if(isBrightnessControl){
            brightnessControlId.setChecked(true);
        }
    }

    /*  start floating widget service  */
    public void createFloatingWidget(View view) {
        //Check if the application has draw over other apps permission or not?
        //This permission is by default available for API<23. But for API > 23
        //you have to ask for the permission in runtime.

        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            //If the draw over permission is not available open the settings screen
            //to grant the permission.
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, DRAW_OVER_OTHER_APP_PERMISSION_REQUEST_CODE);

        } else{
            //If permission is granted start floating widget service
            //startFloatingWidgetService();
            if(overlayId.isChecked() && lockDeviceId.isChecked() && screenshotId.isChecked()){
                startFloatingWidgetService();
            }else{
                Toast.makeText(this, "Please select all required setting options",Toast.LENGTH_SHORT).show();
            }
        }               */

        if(overlayId.isChecked() && lockDeviceId.isChecked() && screenshotId.isChecked() && brightnessControlId.isChecked()){
            startFloatingWidgetService();
        }else{
            Toast.makeText(this, "Please select all required setting options",Toast.LENGTH_SHORT).show();
        }
    }


    /*  Start Floating widget service and finish current activity */
    private void startFloatingWidgetService() {
        startService(new Intent(MainActivity.this, FloatingWidgetService.class));
        finish();
    }

      @Override
      protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == DRAW_OVER_OTHER_APP_PERMISSION_REQUEST_CODE) {
              //Check if the permission is granted or not.
              if (resultCode == RESULT_OK) {
                  //If permission granted start floating widget service
                  //startFloatingWidgetService();
              }
              else{
                  //Permission is not available then display toast
                  //Toast.makeText(this,getResources().getString(R.string.draw_other_app_permission_denied), Toast.LENGTH_SHORT).show();

                  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(MainActivity.this)) {
                      //If the draw over permission is not available open the settings screen
                      //to grant the permission
                      Toast.makeText(this,
                              getResources().getString(R.string.draw_other_app_permission_denied),
                              Toast.LENGTH_SHORT).show();
                      overlayId.setChecked(false);
                  }else {
                      overlayId.setChecked(true);
                      saveOverlayPref();
                  }

                  //overlayId.setChecked(false);
              }

          }

          if(requestCode == LOCK_DEVICE_REQUEST_CODE){
              //Check if the permission is granted or not.
              if (resultCode == RESULT_OK) {
                  //If permission granted start floating widget service
                  //startFloatingWidgetService();
                  adminComponent = new ComponentName(MainActivity.this, DarClass.class);
                  devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                  if (!devicePolicyManager.isAdminActive(adminComponent)) {
                  } else {
                      lockDeviceId.setChecked(true);
                      saveLocalDevicePref();
                  }
              }
              else{
                  //Permission is not available then display toast
                  Toast.makeText(this,
                          getResources().getString(R.string.device_lock_permission_denied),
                          Toast.LENGTH_SHORT).show();

                  lockDeviceId.setChecked(false);
              }
          }

          if (requestCode == WRITE_SETTINGS_CODE) {
              //Check if the permission is granted or not.
              if (resultCode == RESULT_OK) {
                  //If permission granted start floating widget service
                  //startFloatingWidgetService();
              }
              else{
                  //Permission is not available then display toast
                  //Toast.makeText(this,getResources().getString(R.string.draw_other_app_permission_denied), Toast.LENGTH_SHORT).show();

                  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(getApplicationContext())) {
                      //If the draw over permission is not available open the settings screen
                      //to grant the permission
                      Toast.makeText(this,
                              getResources().getString(R.string.write_settings_permission_denied),
                              Toast.LENGTH_SHORT).show();
                      brightnessControlId.setChecked(false);
                  }else {
                      brightnessControlId.setChecked(true);
                      saveBrightnessControlPref();
                  }
              }

          }
              super.onActivityResult(requestCode, resultCode, data);
      }

    public void lockOrUnlock(){
    }


    public void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission

        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }

    }

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {
                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            }
        }
        else {
                Toast.makeText(this, "Screenshot permission is already granted.", Toast.LENGTH_SHORT).show();
                if( (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)){
                    saveScreenshotPref();
                }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED){
            switch (requestCode) {
                case 3:
                    ActivityCompat.requestPermissions(
                            this,
                            PERMISSIONS_STORAGE,
                            REQUEST_EXTERNAL_STORAGE
                    );

                    saveScreenshotPref();
                    break;
            }

            //Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
        }else{
            //Permission is not available then display toast
            Toast.makeText(this,
                    getResources().getString(R.string.write_external_permission_denied),
                    Toast.LENGTH_SHORT).show();
            //Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            screenshotId.setChecked(false);
        }
    }

    public void saveScreenshotPref(){
        sharedpreferences = getSharedPreferences(MyPREFERENCES,Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("isScreenshot", true);
        editor.commit();
        Log.e("saveScreenshotPref","save in sharedPreference!!");
    }

    public void saveOverlayPref(){
        sharedpreferences = getSharedPreferences(MyPREFERENCES,Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("isOverlay", true);
        editor.commit();
        Log.e("saveOverlayPref","save in sharedPreference!!");
    }

    public void saveLocalDevicePref(){
        sharedpreferences = getSharedPreferences(MyPREFERENCES,Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("isLocalDevice", true);
        editor.commit();
        Log.e("saveLocalDevicePref","save in sharedPreference!!");
    }

    public void saveBrightnessControlPref(){
        sharedpreferences = getSharedPreferences(MyPREFERENCES,Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("isBrightnessControl", true);
        editor.commit();
        Log.e("saveBrightnessPref","save in sharedPreference!!");
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
