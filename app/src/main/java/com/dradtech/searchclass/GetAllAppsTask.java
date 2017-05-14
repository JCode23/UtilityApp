package com.dradtech.searchclass;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by siddhant on 4/29/17.
 */
public class GetAllAppsTask extends AsyncTask<Void, Void, List<ApplicationInfo>> {

    private QuickSearch activity;
    private List<ApplicationInfo> apps;
    private PackageManager packageManager;

    public GetAllAppsTask(QuickSearch activity, List<ApplicationInfo> apps, PackageManager pm) {
        this.activity = activity;
        this.apps = apps;
        this.packageManager = pm;
    }

    @Override
    protected List<ApplicationInfo> doInBackground(Void... params) {
        apps = checkForLaunchIntent(packageManager.getInstalledApplications(PackageManager.GET_META_DATA));
        return apps;
    }

    private List<ApplicationInfo> checkForLaunchIntent(List<ApplicationInfo> list) {
        ArrayList<ApplicationInfo> applist = new ArrayList<>();
        for (ApplicationInfo applicationInfo : list) {
            try {
                if (packageManager.getLaunchIntentForPackage(applicationInfo.packageName) != null) {
                    applist.add(applicationInfo);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return applist;
    }

    @Override
    protected void onPostExecute(List<ApplicationInfo> list) {
        super.onPostExecute(list);
        activity.callBackDataFromAsynctask(list);
    }
}

