package com.navigatenative;


import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MyModule extends ReactContextBaseJavaModule {
    private static ReactApplicationContext reactContext;


    private static final String DURATION_SHORT_KEY = "SHORT";
    private static final String DURATION_LONG_KEY = "LONG";
    public static WritableArray applist;
    public  static  long send;
    public  static  long res;
    public  static Uri uri;
    public  static  String icon;
    public static WritableArray applistexone;


    MyModule(ReactApplicationContext context) {
        super(context);
        reactContext = context;
    }
    @Override
    public String getName() {
        return "ToastExample";
    }

     @ReactMethod
    public  void  navigateAndroid (){
         Intent intent = new Intent(reactContext,DrawerActivty.class);
       if(intent.resolveActivity(reactContext.getPackageManager()) != null){
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                reactContext.startActivity(intent);
            }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @ReactMethod
    public  void  loadAppUsage(Callback errorCallback,Callback succesCallback){
        if(getGrantStatus()){

            UsageStatsManager usm = (UsageStatsManager) reactContext.getSystemService(Context.USAGE_STATS_SERVICE);
            List<UsageStats> appList =  usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,System.currentTimeMillis() - 1000*3600*24,  System.currentTimeMillis());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                appList = appList.stream().filter(app -> app.getTotalTimeInForeground() > 0).collect(Collectors.toList());
            }

            if (appList.size() > 0) {
                Map<String, UsageStats> mySortedMap = new TreeMap<>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getPackageName(), usageStats);
                }
                Log.d("applist", appList.toString());
                getAppUsage(mySortedMap,errorCallback,succesCallback);
            }
        }else {
            succesCallback.invoke("izin reddedildi");
        }


    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)

    public  void getAppUsage(Map<String , UsageStats> mySortedMap,Callback errorCallback ,Callback succesCalback  ){

        ArrayList<Object> appList = new ArrayList<>();
        List<UsageStats> usageStatsList = new ArrayList<>(mySortedMap.values());

        WritableArray usageAppListEx = new WritableNativeArray();


        Collections.sort(usageStatsList , (z1 , z2)-> Long.compare(z1.getTotalTimeInForeground(),z2.getTotalTimeInForeground()));


        long totalTime = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            totalTime = usageStatsList.stream().map(UsageStats::getTotalTimeInForeground).mapToLong(Long::longValue).sum();
        }

        for (UsageStats usageStats : usageStatsList){


            try {
                WritableMap info = new WritableNativeMap();
                String packageName = usageStats.getPackageName();


                String[] packageNames = packageName.split("\\.");
                String appName = packageNames[packageNames.length-1].trim();

                if(isAppInfoAvailable(usageStats)){
                    ApplicationInfo ai = reactContext.getApplicationContext().getPackageManager().getApplicationInfo(packageName , 0);
                        icon = reactContext.getApplicationContext().getPackageManager().getApplicationIcon(ai).toString();
                        appName = reactContext.getApplicationContext().getPackageManager().getApplicationLabel(ai).toString();

                        int uid= ai.uid;





                     uri = Uri.parse("android.resource://" +packageName + "/mipmap/ic_launcher/ic_launcher.png"  );







                    Log.d("name" , appName);
                   send   = TrafficStats.getUidTxBytes(uid);
                   res = TrafficStats.getUidRxBytes(uid);
                }


                String usageDuration = getDurationBreakdown(usageStats.getTotalTimeInForeground());
                int usagePercantage = (int) (usageStats.getTotalTimeInForeground() * 100 / totalTime);

                if (getReactApplicationContext().getPackageManager().getLaunchIntentForPackage(usageStats.getPackageName()) != null){


                    info.putString("appName" , appName);
                    info.putString("usageDuration" , usageDuration);
                    info.putInt("usagePercantage" ,usagePercantage);
                    info.putString("trafffic" ,Long.toString(send) );
                    info.putString("traffficRes" ,Long.toString(res) );
                    info.putString("packageName" , uri.toString());
                }



                usageAppListEx.pushMap(info);


            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

       //Converting the Object to JSONString

        applistexone=usageAppListEx;

        succesCalback.invoke(applistexone);



    }



    private boolean isAppInfoAvailable(UsageStats usageStats) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                reactContext.getApplicationContext().getPackageManager().getApplicationInfo(usageStats.getPackageName(), 0);
            }
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


    private String getDurationBreakdown(long millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        return (hours + " h " +  minutes + " m " + seconds + " s");
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private boolean getGrantStatus() {
        AppOpsManager appOps = (AppOpsManager) reactContext.getApplicationContext()
                .getSystemService(Context.APP_OPS_SERVICE);

        int mode = appOps.checkOpNoThrow(appOps.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), reactContext.getApplicationContext().getPackageName());

        if (mode == AppOpsManager.MODE_DEFAULT) {
            return (reactContext.getApplicationContext().checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        } else {
            return (mode == appOps.MODE_ALLOWED);
        }
    }


    public WritableMap toJSON(String appName , int usagePercentage , String usageDuration)  {

        WritableMap map  = Arguments.createMap();


        map.putString("appName", appName);
        map.putInt("usagePercentage", usagePercentage);
        map.putString("usageDuration", usageDuration);




        return map;
    }




    @ReactMethod
    public void show(Callback errorCallback, Callback successCallback) {
        try {
            //get a list of installed apps.
            PackageManager pm = getReactApplicationContext().getPackageManager();
            List<ApplicationInfo> packages = pm.getInstalledApplications(0);

            WritableArray app_list = new WritableNativeArray();
            int cnt = 0;
            for (ApplicationInfo packageInfo : packages) {
                try {

                    if (getReactApplicationContext().getPackageManager().getLaunchIntentForPackage(packageInfo.packageName) != null) {
                        WritableMap info = new WritableNativeMap();
                        info.putString("name", packageInfo.loadLabel(pm).toString());
                        info.putString("packagename", packageInfo.packageName);


                        //cheak exist  or not


                        app_list.pushMap(info);
                    }
                } catch (Exception ex) {
                    System.err.println("Exception: " + ex.getMessage());
                }
            }
            applist = app_list;
            successCallback.invoke(applist);

        } catch (Exception e) {
            errorCallback.invoke(e.getMessage());
        }
    }






}






















