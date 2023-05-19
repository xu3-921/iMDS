package com.example.mdm_ycnt;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.json.JSONException;
import org.json.JSONObject;

public class SettingsActivity extends AppCompatActivity {

    private static Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }


        activity = SettingsActivity.this;
    }

    public boolean onOptionsItemSelected(MenuItem item){

        //Intent intent = new Intent(this,MainActivity.class);
        //startActivity(intent);
        finish();

        return true;
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            SharedPreferences pref = getContext().getSharedPreferences("mdm_ycnt", MODE_PRIVATE);

            String control_from_ipts = pref.getString("control_from_ipts","F");

            if(control_from_ipts.equals("T")){
                Preference setting_device_manufacturer = findPreference("setting_other");
                setting_device_manufacturer.setVisible(true);
            }

            String deviceManufacturerName = pref.getString("device_manufacturer_name", "未知");
            Preference setting_device_manufacturer = findPreference("setting_device_manufacturer");
            setting_device_manufacturer.setSummary(deviceManufacturerName);

            String deviceModelName = pref.getString("device_model_name", "未知");
            Preference setting_device_model = findPreference("setting_device_model");
            setting_device_model.setSummary(deviceModelName);


            String controlAppVersion = pref.getString("control_app_package_name", "未安裝");

            Preference setting_control_app_version = findPreference("setting_control_app_version");

            if(controlAppVersion.equals("otherDevicePackageName")){
                findPreference("setting_update_control_app").setVisible(false);;
                setting_control_app_version.setVisible(false);
            }
            else{
                String appVersion = UniversalFunction.getAppVersionName(controlAppVersion);
                setting_control_app_version.setSummary(appVersion);
            }



            Preference setting_iPTS_control_qr_code = findPreference("setting_iPTS_control_qr_code");
            setting_iPTS_control_qr_code.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {


                    Intent intent = new Intent(activity, ShowIptsQrCodeActivity.class);
                    startActivity(intent);

                    return false;
                }
            });

            Preference setting_update_iMDS_app = findPreference("setting_update_iMDS_app");
            setting_update_iMDS_app.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    ProgressDialog loadingPd;

                    loadingPd = new ProgressDialog(getContext() , R.style.BlueLoadingAlertDialogStyle);
                    loadingPd.setMessage("確認中...");
                    loadingPd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    loadingPd.setCancelable(false);
                    loadingPd.setCanceledOnTouchOutside(false);
                    loadingPd.show();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            try {

                                String isLastVersion = null;

                                JSONObject getJson = UniversalFunction.check_imds_app_version();


                                if(getJson == null){
                                    isLastVersion = "error";
                                }else {
                                    isLastVersion = getJson.getString("is_last_version");
                                }

                                //isLastVersion-val
                                //newest_ver:最新版
                                //safe_ver:安全範圍內 但不是最新
                                //higher_than_max_ver:版本過高
                                //lower_than_min_ver:版本過低
                                //unregistered:未註冊
                                //error:錯誤

                                if(isLastVersion.equals("newest_ver")){

                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            loadingPd.dismiss();

                                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
                                            alertDialogBuilder.setTitle("iMDS為最新版本");
                                            alertDialogBuilder.setMessage("iMDS無須更新");

                                            alertDialogBuilder.setPositiveButton("完成",((dialog, which) -> {}));

                                            AlertDialog alertDialog = alertDialogBuilder.create();
                                            alertDialog.setCancelable(false);
                                            alertDialog.setCanceledOnTouchOutside(false);

                                            alertDialog.show();


                                        }
                                    });

                                }
                                else if(isLastVersion.equals("unregistered") || isLastVersion.equals("error")){

                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            loadingPd.dismiss();

                                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
                                            alertDialogBuilder.setTitle("發生錯誤");
                                            alertDialogBuilder.setMessage("請重新嘗試");
                                            alertDialogBuilder.setPositiveButton("確認",((dialog, which) -> {}));

                                            AlertDialog alertDialog = alertDialogBuilder.create();
                                            alertDialog.setCancelable(false);
                                            alertDialog.setCanceledOnTouchOutside(false);

                                            alertDialog.show();


                                        }
                                    });

                                }
                                else{
                                    String isForcedUpdating = getJson.getString("is_forced_updating");
                                    String lastVersionName = getJson.getString("package_version_name");
                                    String packagePath = getJson.getString("ad_mdm_device_control_app_package_path");
                                    String appName = getJson.getString("ad_mdm_device_app_name");


                                    String setTitle = null;
                                    String setMessage = null;


                                    if(isLastVersion.equals("safe_ver")){
                                        setTitle = "請問是否更新iMDS";
                                        setMessage = "iMDS最新版本為"+lastVersionName;

                                    }else if(isLastVersion.equals("higher_than_max_ver")){
                                        setTitle = "請更新iMDS";
                                        setMessage = "iMDS建議版本為"+lastVersionName;

                                    }else if(isLastVersion.equals("lower_than_min_ver")){
                                        setTitle = "請更新iMDS";
                                        setMessage = "iMDS最新版本為"+lastVersionName;
                                    }

                                    String alertTitle = setTitle;
                                    String alertMessage = setMessage;

                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            loadingPd.dismiss();

                                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
                                            alertDialogBuilder.setTitle(alertTitle);
                                            alertDialogBuilder.setMessage(alertMessage);

                                            if(isForcedUpdating.equals("F")){
                                                alertDialogBuilder.setNegativeButton("取消",((dialog, which) -> {}));
                                            }

                                            alertDialogBuilder.setPositiveButton("更新",((dialog, which) -> {
                                                //更新

                                                String serverIp = UniversalFunction.GetServerIP(getContext());
                                                String defaultPath = String.valueOf(getContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS));
                                                String subPath = "/Apk";

                                                String downloadUrl = serverIp + packagePath + appName + "_" + lastVersionName + ".apk";
                                                String apkName = appName + "_" + lastVersionName + ".apk";


                                                UniversalFunction.downloadApp(downloadUrl ,apkName ,defaultPath ,subPath ,false ,getContext() ,activity);

                                            }));

                                            AlertDialog alertDialog = alertDialogBuilder.create();
                                            alertDialog.setCancelable(false);
                                            alertDialog.setCanceledOnTouchOutside(false);

                                            alertDialog.show();

                                        }
                                    });
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }



                        }
                    }).start();

                    return false;
                }
            });


            Preference setting_update_control_app = findPreference("setting_update_control_app");
            setting_update_control_app.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    ProgressDialog loadingPd;

                    loadingPd = new ProgressDialog(getContext() , R.style.BlueLoadingAlertDialogStyle);
                    loadingPd.setMessage("確認中...");
                    loadingPd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    loadingPd.setCancelable(false);
                    loadingPd.setCanceledOnTouchOutside(false);
                    loadingPd.show();

                    new  Thread(new Runnable() {
                        @Override
                        public void run() {

                            //newest_ver:最新版
                            //higher_than_max_ver:版本過高
                            //lower_than_min_ver:版本過低
                            //error:錯誤

                            JSONObject getJson = UniversalFunction.check_control_app_version();

                            try {

                                String isLastVersion = null;

                                if(getJson == null){
                                    isLastVersion = "error";
                                }else{

                                    isLastVersion = getJson.getString("is_last_version");
                                }

                                if(isLastVersion.equals("newest_ver")){

                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            loadingPd.dismiss();

                                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
                                            alertDialogBuilder.setTitle("APP為最新版本");
                                            alertDialogBuilder.setMessage("APP無須更新");

                                            alertDialogBuilder.setPositiveButton("完成",((dialog, which) -> {}));

                                            AlertDialog alertDialog = alertDialogBuilder.create();
                                            alertDialog.setCancelable(false);
                                            alertDialog.setCanceledOnTouchOutside(false);

                                            alertDialog.show();

                                        }
                                    });
                                }
                                else if(isLastVersion.equals("error")){
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            loadingPd.dismiss();

                                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
                                            alertDialogBuilder.setTitle("發生錯誤");
                                            alertDialogBuilder.setMessage("請重新嘗試");

                                            alertDialogBuilder.setPositiveButton("完成",((dialog, which) -> {}));

                                            AlertDialog alertDialog = alertDialogBuilder.create();
                                            alertDialog.setCancelable(false);
                                            alertDialog.setCanceledOnTouchOutside(false);

                                            alertDialog.show();

                                        }
                                    });

                                }
                                else if(isLastVersion.equals("higher_than_max_ver") || isLastVersion.equals("lower_than_min_ver")){


                                    String lastVersionName = getJson.getString("package_version_name");
                                    String packagePath = getJson.getString("device_control_app_package_path");
                                    String appName = getJson.getString("device_app_name");

                                    String setTitle = null;
                                    String setMessage = null;
                                    String setIsForcedUpdating = null;


                                   if(isLastVersion.equals("higher_than_max_ver")){
                                        setTitle = "請更新控制APP";
                                        setMessage = "控制APP建議版本為"+lastVersionName;
                                        setIsForcedUpdating = "T";

                                    }else if(isLastVersion.equals("lower_than_min_ver")){
                                        setTitle = "請問是否更新控制APP";
                                        setMessage = "控制APP最新版本為"+lastVersionName;
                                        setIsForcedUpdating = "F";
                                    }

                                    String alertTitle = setTitle;
                                    String alertMessage = setMessage;
                                    String isForcedUpdating = setIsForcedUpdating;

                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            loadingPd.dismiss();

                                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
                                            alertDialogBuilder.setTitle(alertTitle);
                                            alertDialogBuilder.setMessage(alertMessage);

                                            if(isForcedUpdating.equals("F")){
                                                alertDialogBuilder.setNegativeButton("取消",((dialog, which) -> {}));
                                            }

                                            alertDialogBuilder.setPositiveButton("更新",((dialog, which) -> {
                                                //更新

                                                String serverIp = UniversalFunction.GetServerIP(getContext());
                                                String defaultPath = String.valueOf(getContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS));
                                                String subPath = "/Apk";

                                                String downloadUrl = serverIp + packagePath + appName + "_" + lastVersionName + ".apk";
                                                String apkName = appName + "_" + lastVersionName + ".apk";


                                                UniversalFunction.downloadApp(downloadUrl ,apkName ,defaultPath ,subPath ,false ,getContext() ,activity);

                                            }));

                                            AlertDialog alertDialog = alertDialogBuilder.create();
                                            alertDialog.setCancelable(false);
                                            alertDialog.setCanceledOnTouchOutside(false);

                                            alertDialog.show();

                                        }
                                    });

                                }


                            } catch (JSONException e) {

                                e.printStackTrace();
                            }

                        }
                    }).start();




                    return false;
                }
            });





        }
    }


}
