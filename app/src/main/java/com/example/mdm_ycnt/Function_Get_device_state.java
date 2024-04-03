package com.example.mdm_ycnt;

import static android.content.Context.MODE_PRIVATE;

import static java.lang.String.format;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.format.Formatter;
import android.util.Log;

import androidx.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class Function_Get_device_state {

    //全部的內存
    public static String F_getTotalMemory(Context context){
        File dataFileDir = Environment.getDataDirectory();
        //String dataMemory = getMemoryInfo(dataFileDir);
        StatFs stat = new StatFs(dataFileDir.getPath());
        long blockSize = stat.getBlockSize();	// 获得一个扇区的大小
        long totalBlocks = stat.getBlockCount();	// 获得扇区的总数
        //long availableBlocks = stat.getAvailableBlocks();	// 获得可用的扇区数量
        //總空間
        String totalMemory =  Formatter.formatFileSize(context, totalBlocks * blockSize);
        //可用空間
        //String availableMemory = Formatter.formatFileSize(context, availableBlocks * blockSize);
        return totalMemory;
    };

    //未使用的內存
    public static String F_getAvailableMemory(Context context){
        File dataFileDir = Environment.getDataDirectory();
        //String dataMemory = getMemoryInfo(dataFileDir);
        StatFs stat = new StatFs(dataFileDir.getPath());
        long blockSize = stat.getBlockSize();	// 获得一个扇区的大小
        //long totalBlocks = stat.getBlockCount();	// 获得扇区的总数
        long availableBlocks = stat.getAvailableBlocks();	// 获得可用的扇区数量
        //總空間
        //String totalMemory =  Formatter.formatFileSize(context, totalBlocks * blockSize);
        //可用空間
        String availableMemory = Formatter.formatFileSize(context, availableBlocks * blockSize);

        return availableMemory;
    };

    //全部的RAM
    public static String F_getTotalRAM(Context context) {
        long size = 0;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo outInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(outInfo);
        size = outInfo.totalMem;
        return Formatter.formatFileSize(context, size);
    }

    //未使用的RAM
    public static String F_getAvailableRAM(Context context) {
        long size = 0;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo outInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(outInfo);
        size = outInfo.availMem;

        //Log.e("RAM_return",Formatter.formatFileSize(context, size));
        return Formatter.formatFileSize(context, size);
    }

    //開機累計時間
    public static String F_get_BootTime() {
        long time= SystemClock.elapsedRealtime()/1000;//秒
        return String.valueOf(time);
    }

    //系統版本號
    public static String F_getAndroidVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    //手機型號
    public static String F_getPhoneModel() {
        return android.os.Build.MODEL;
    }

    //手機廠商
    public static String F_getPhoneManufacturers() {
        return android.os.Build.BRAND;
    }

    //無線MAC
    public static String F_getLocalMacAddress_wlan0() {
        String Mac=null;

        String path="sys/class/net/wlan0/address";
        FileInputStream fis_name=null;

        try{
            fis_name = new FileInputStream(path);
            byte[] buffer_name = new byte[8192];
            int byteCount_name = fis_name.read(buffer_name);

            if(byteCount_name>0) {
                Mac = new String(buffer_name, 0, byteCount_name, "utf-8");
            }

            if(Mac.length()==0 || Mac==null){
                Mac= "error_wlan0_null";
            }

        }catch(Exception e){
            Mac= "error_wlan0";
            //e.printStackTrace();

        }finally {
            try {
                if(fis_name!=null){
                    fis_name.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return Mac.trim().toUpperCase();

    }

    //有線MAC
    public static String F_getLocalMacAddress_eth0() {

        String Mac=null;
        String path="sys/class/net/eth0/address";
        FileInputStream fis_name=null;
        try{
            fis_name = new FileInputStream(path);
            byte[] buffer_name = new byte[8192];
            int byteCount_name = fis_name.read(buffer_name);

            if(byteCount_name>0) {
                Mac = new String(buffer_name, 0, byteCount_name, "utf-8");
            }

            if(Mac.length()==0||Mac==null){
                Mac= "error_eth0_null";
            }
        }catch(Exception e){
            Mac= "error_eth0";
            //e.printStackTrace();
        }finally {
            try {
                if (fis_name!=null){
                    fis_name.close();
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Mac.trim().toUpperCase();
    }

    //IP
    public static String F_getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String F_getDHCPstatus(Context mContext){
        String str=null;
        try {
            WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifiManager.getConnectionInfo();
            if(null!=wifiManager){
                DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
                // ipaddr 172.20.161.205
                // gateway 172.20.160.1
                // netmask 255.255.254.0
                // dns1 172.16.2.15
                // dns2 172.16.2.16
                // DHCP server 172.20.160.1
                // lease 14400 seconds

                String gateway=intToIp(dhcpInfo.gateway);
                String netmask=intToIp(dhcpInfo.netmask);
                String dns1=intToIp(dhcpInfo.dns1);
                String dns2=intToIp(dhcpInfo.dns2);
                str=("gateway:"+gateway+"///"+"netmask:"+netmask+"///"+"dns1:"+dns1+"///"+"dns2:"+dns2);
                //String macAddress = info.getMacAddress();
                //tv_wifi_mac.setText(macAddress.toUpperCase());
                //tring ipAddress = intToIp(dhcpInfo.ipAddress);
                //tv_ip_address.setText(ipAddress.toString());
                //v_rj45_mac.setText("");
            }

        }catch (Exception e){
            Log.i("NQ","Exception:"+e.getLocalizedMessage());
        }

        return str;
    }

    public static String intToIp(int paramInt) {
        return (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "." + (0xFF & paramInt >> 16) + "."
                + (0xFF & paramInt >> 24);
    }

    public static String F_get_TimeSwitchPower_state(Context mContext){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        Boolean TimeSwitch_open = prefs.getBoolean("setting_TimeSwitch_open", false);
        Boolean TimeSwitch_close = prefs.getBoolean("setting_TimeSwitch_close", false);

        String isTimeSwitch="";

        if(TimeSwitch_open && TimeSwitch_close){
            isTimeSwitch="on|on";
        }else if(TimeSwitch_open && !TimeSwitch_close){
            isTimeSwitch="on|off";
        } else if(!TimeSwitch_open && TimeSwitch_close){
            isTimeSwitch="off|on";
        } else if(!TimeSwitch_open && !TimeSwitch_close){
            isTimeSwitch="off|off";
        }
        return isTimeSwitch;
    }

    public static String F_get_volume(Context mContext){
        AudioManager mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        float MaxVolume=mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float Volume=mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        return format("%.01f", Volume / MaxVolume * 100);
    }

    public static String F_get_deviceName(Context mContext){
        String name = Settings.Global.getString(mContext.getContentResolver(), Settings.Global.DEVICE_NAME);
        return name;

    }

    public String getControlAppVersion(Context mContext){

        String G_control_app_package_name = mContext.getSharedPreferences("mdm_ycnt", MODE_PRIVATE)
                .getString("control_app_package_name", "no_val");

        if(!G_control_app_package_name.equals("no_val")){
            try {

            PackageManager pm = mContext.getPackageManager();
                PackageInfo pInfo = pm.getPackageInfo(G_control_app_package_name, 0);
                String version = pInfo.versionName;

                return version;

            } catch (PackageManager.NameNotFoundException e) {
                return null;
            }

        }else {
            return null;
        }

    }


    //30秒一次
    /*Log.e("ttt","全部的RAM:"+ F_getTotalRAM());
      Log.e("ttt","未使用的RAM:"+ F_getAvailableRAM());s
      //Log.e("ttt", "開機累計時間:"+F_getOpenTime());

      //開起來第一次
      //Log.e("ttt", "系統版本號："+F_getAndroidVersion());
      //Log.e("ttt", "手機型號："+F_getPhoneModel());
      //Log.e("ttt", "手機廠商："+F_getPhoneManufacturers());
      //Log.e("ttt", "無線MAC："+F_getLocalMacAddress_wlan0());
      //Log.e("ttt", "有線MAC："+F_getLocalMacAddress_eth0());
      //Log.e("ttt", "IP："+ F_getLocalIpAddress());

      Log.e("ttt", "AndroidID："+ F_getAndroidID());*/

}
