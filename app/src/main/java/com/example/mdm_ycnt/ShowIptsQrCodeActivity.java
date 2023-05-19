package com.example.mdm_ycnt;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;

public class ShowIptsQrCodeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_ipts_qr_code);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //String deviceId = UniversalFunction.F_getDeviceId();

        //ipts_register
        //Log.e("tttQQ",deviceId);

        ImageView img_qrcode=findViewById(R.id.img_qrcode);
        TextView textView_showID=findViewById(R.id.textView_showID);

        String deviceId = UniversalFunction.F_getDeviceId();

        textView_showID.setText(deviceId);

        String url_domain = UniversalFunction.GetServerIP(getApplicationContext());

        byte[] DeviceIdData = new byte[0];

        try {
            DeviceIdData = deviceId.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String DeviceId_base64 = Base64.encodeToString(DeviceIdData, Base64.DEFAULT);

        String url = url_domain + "iMDS_Register/ipts_register.php?" + DeviceId_base64;//"get_device_qrcode.php?"
        UniversalFunction.F_show_qrcode(img_qrcode,url);

    }

    public boolean onOptionsItemSelected(MenuItem item){

        finish();

        return true;
    }
}