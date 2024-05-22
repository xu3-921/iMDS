package com.example.mdm_ycnt;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

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
                String appVersion = UniversalFunction.getAppVersionName(controlAppVersion, getContext());
                setting_control_app_version.setSummary(appVersion);
            }

            Preference setting_device_id_base64 = findPreference("setting_device_id_base64");

            String getDeviceId = UniversalFunction.F_getDeviceId(getContext());
            String encodedString = Base64.getEncoder().encodeToString(getDeviceId.getBytes());
            setting_device_id_base64.setSummary(encodedString);


            Preference setting_iPTS_control_qr_code = findPreference("setting_iPTS_control_qr_code");
            setting_iPTS_control_qr_code.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    Intent intent = new Intent(activity, ShowIptsQrCodeActivity.class);
                    startActivity(intent);

                    return false;
                }
            });

            UniversalFunction.ClickCounter clickCounter = new UniversalFunction.ClickCounter(
                    10,
                    2000,
                    new UniversalFunction.ClickCounter.OnThresholdReachedListener() {
                        @Override
                        public void onThresholdReached() {
                            // 执行动作
                            Map<String, String> signatureInfoMap = new UniversalFunction().getSignatureInfo(activity);

                            String sha256 = signatureInfoMap.get("SHA-256");
                            String subjectDN = signatureInfoMap.get("SubjectDN");

                            String infoText =  sha256 + "\n" + subjectDN;
                            infoText = Base64.getEncoder().encodeToString(infoText.getBytes());

                            int getNum = 6; // X
                            String firstThreeChars = infoText.substring(0, getNum);  // 取得前X個字符
                            String remainingChars = infoText.substring(getNum);      // 取得剩餘的字符

                            String finalText = remainingChars + firstThreeChars; // 將前3個字符換到最後

                            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();

                            Map map = new HashMap();
                            //  設定容錯率 L>M>Q>H 等級越高掃描時間越長,準確率越高
                            map.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
                            //設定字元集
                            map.put(EncodeHintType.CHARACTER_SET,"utf-8");
                            //設定外邊距
                            map.put(EncodeHintType.MARGIN,0);

                            Bitmap qrCodeBitmap = null;

                            try {
                                qrCodeBitmap = barcodeEncoder.encodeBitmap(finalText, BarcodeFormat.QR_CODE,400,400,map);
                            } catch (WriterException e) {
                                e.printStackTrace();
                            }

                            ImageView imageView = new ImageView(activity);
                            imageView.setImageBitmap(qrCodeBitmap);

                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                            builder.setTitle("iMDS")
                                    .setView(imageView)
                                    .setCancelable(false)
                                    .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.dismiss();
                                        }
                                    });

                            AlertDialog alert = builder.create();
                            alert.show();

                            alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.blue));
                            alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.blue));

                        }
                    });

            Preference settingAppVersionName = findPreference("setting_app_version");
            settingAppVersionName.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    return clickCounter.onClick();

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

                                JSONObject getJson = UniversalFunction.check_imds_app_version(getContext());


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
                                            //alertDialogBuilder.setMessage("iMDS無須更新");

                                            alertDialogBuilder.setPositiveButton("完成",((dialog, which) -> {}));

                                            AlertDialog alertDialog = alertDialogBuilder.create();
                                            alertDialog.setCancelable(false);
                                            alertDialog.setCanceledOnTouchOutside(false);

                                            alertDialog.show();

                                            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.blue));
                                            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.blue));


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

                                            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.blue));
                                            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.blue));


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

                                            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.blue));
                                            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.blue));

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

                            JSONObject getJson = UniversalFunction.check_control_app_version(getContext());

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
                                            //alertDialogBuilder.setMessage("APP無須更新");

                                            alertDialogBuilder.setPositiveButton("完成",((dialog, which) -> {}));

                                            AlertDialog alertDialog = alertDialogBuilder.create();
                                            alertDialog.setCancelable(false);
                                            alertDialog.setCanceledOnTouchOutside(false);

                                            alertDialog.show();

                                            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.blue));
                                            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.blue));

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

                                            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.blue));
                                            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.blue));

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

                                            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.blue));
                                            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.blue));

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

            Preference setting_emergency_alert_btn = findPreference("setting_emergency_alert_btn");
            if (setting_emergency_alert_btn instanceof SwitchPreferenceCompat) {
                SwitchPreferenceCompat switchPref = (SwitchPreferenceCompat) setting_emergency_alert_btn;
                switchPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        boolean isSwitchOn = (Boolean) newValue;

                        Intent intent = new Intent(getContext(), EmergencyAlertService.class);

                        if(isSwitchOn) {
                            intent.putExtra("action", "create");
                        } else {
                            intent.putExtra("action", "delete");
                        }

                        getContext().startService(intent);
                        return true; // 返回 true 以保存變更
                    }
                });
            }

            boolean isEmergencyAlertBtnOn = getContext().getSharedPreferences("mdm_ycnt", MODE_PRIVATE)
                    .getBoolean("isEmergencyAlertBtnOn", false);
            if(!isEmergencyAlertBtnOn){
                Preference preferenceCategoryCommon = findPreference("preferenceCategoryCommon");
                preferenceCategoryCommon.setVisible(false);
                setting_emergency_alert_btn.setVisible(false);
            }

        }
    }


}

