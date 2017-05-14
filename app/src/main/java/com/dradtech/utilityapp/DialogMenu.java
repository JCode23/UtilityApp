package com.dradtech.utilityapp;

import android.app.Activity;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.dradtech.screenshotclass.Test01Screenshot;
import com.dradtech.searchclass.QuickSearch;

/**
 * Created by siddhant on 4/27/17.
 */
public class DialogMenu extends AppCompatActivity{

    private ImageView lockScreen, swipeBrightness, quickSearch, screenShot;
    private Dialog dialog, screenshotDialog ;
    private Button okBtn;

    protected static final int REQUEST_ENABLE = 0;
    DevicePolicyManager devicePolicyManager;
    ComponentName adminComponent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);

        showAlertDialog(this);
    }


    public void showAlertDialog(final Activity activity) {

        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_menu);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                activity.finish();
            }
        });

        lockScreen = (ImageView) dialog.findViewById(R.id.image01);
        lockScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                adminComponent = new ComponentName(DialogMenu.this, DarClass.class);
                devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

                if (!devicePolicyManager.isAdminActive(adminComponent)) {

                    Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
                    startActivityForResult(intent, REQUEST_ENABLE);
                } else {
                    devicePolicyManager.lockNow();
                }

                dialog.dismiss();
                activity.finish();
                Log.d("Action","lockScreen Image Clicked");
            }
        });

        swipeBrightness = (ImageView) dialog.findViewById(R.id.image02);
        swipeBrightness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DialogMenu.this, BrightnessControlDialog.class);
                startActivity(intent);
                dialog.dismiss();
                activity.finish();
            }
        });

        quickSearch = (ImageView) dialog.findViewById(R.id.image03);
        quickSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DialogMenu.this, QuickSearch.class);
                startActivity(intent);
                dialog.dismiss();
                activity.finish();
            }
        });

        screenShot = (ImageView) dialog.findViewById(R.id.image04);
        screenShot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    Intent intent = new Intent(DialogMenu.this, Test01Screenshot.class);
                    startActivity(intent);
                    dialog.dismiss();
                    activity.finish();
                }
                else{
                    //Toast.makeText(DialogMenu.this, "Sorry this feature is available for Lollipop(Android 5.0) or higher version only !! ", Toast.LENGTH_SHORT).show();
                    screenshotDialog = new Dialog(DialogMenu.this);
                    screenshotDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    screenshotDialog.setContentView(R.layout.screenshot_dialog);
                    screenshotDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    screenshotDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        public void onCancel(DialogInterface dialog) {
                            activity.finish();
                        }
                    });

                    okBtn = (Button) screenshotDialog.findViewById(R.id.okBtn);
                    okBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            screenshotDialog.dismiss();
                        }
                    });

                    screenshotDialog.show();
                }

                //FloatingWidgetService.floatingWidgetService.stopSelf();
            }
        });
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQUEST_ENABLE == requestCode) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    /*

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH){
                    return false;
                }
                // right to left swipe
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    //onLeftSwipe();
                }
                // left to right swipe
                else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    //onRightSwipe();
                }
            } catch (Exception e) {

            }
            return false;
        }
    }              */
}
