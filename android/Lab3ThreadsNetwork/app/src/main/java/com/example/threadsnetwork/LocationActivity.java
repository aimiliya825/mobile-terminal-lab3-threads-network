package com.example.threadsnetwork;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 程序4：使用 LocationManager 获取经纬度进行定位。
 *
 * 步骤：
 * 1) 在 AndroidManifest.xml 中声明 ACCESS_FINE_LOCATION / ACCESS_COARSE_LOCATION；
 * 2) 运行时动态申请定位权限（API 23 以后必须申请）；
 * 3) 通过 getSystemService(Context.LOCATION_SERVICE) 获取 LocationManager；
 * 4) 实现 LocationListener，在 onLocationChanged() 中接收定位结果；
 * 5) requestLocationUpdates() 注册定位监听，把经纬度显示到界面。
 */
public class LocationActivity extends Activity {

    private static final int REQUEST_LOCATION = 100;

    private TextView infoView = null;
    private TextView locationView = null;

    private LocationManager locationManager = null;
    private LocationListener locationListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        infoView = (TextView) findViewById(R.id.infoView);
        locationView = (TextView) findViewById(R.id.locationView);

        // 动态申请定位权限
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
        } else {
            locationManager_init();
        }

        Button btnRefresh = (Button) findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    locationManager_init();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            locationManager_init();
        } else {
            infoView.setText("未授予定位权限，无法获取经纬度！");
        }
    }

    /* locationManager 初始化 */
    private void locationManager_init() {
        if (locationManager != null) {
            return;
        }
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            infoView.setText("当前设备不支持定位服务！");
            return;
        }
        locationListener_init();

        // 第一个参数是定位方案（gps / network），第二个是更新时间间隔（毫秒），
        // 第三个是更新距离（米），第四个是定位监听器
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
                infoView.setText("当前GPS状态：开启，正在等待定位…");
            } else {
                infoView.setText("当前GPS状态：关闭，请在系统设置中打开位置信息。");
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
            }
            // 先显示上一次已知的位置，避免界面一直空白
            Location last = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (last == null) {
                last = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (last != null) {
                showLocation(last);
            }
        } catch (SecurityException e) {
            infoView.setText("缺少定位权限：" + e.getMessage());
        }
    }

    /* 定位监听器初始化 */
    private void locationListener_init() {
        locationListener = new LocationListener() {
            // 位置变化时触发
            public void onLocationChanged(Location location) {
                showLocation(location);
            }

            // GPS 禁用时触发
            public void onProviderDisabled(String provider) {
                infoView.setText("当前GPS状态：禁用\n");
            }

            // GPS 开启时触发
            public void onProviderEnabled(String provider) {
                infoView.setText("当前GPS状态：开启\n");
            }

            // GPS 状态变化时触发
            public void onStatusChanged(String provider, int status, Bundle extras) {
                if (status == LocationProvider.AVAILABLE) {
                    infoView.setText("当前GPS状态：可见的\n");
                } else if (status == LocationProvider.OUT_OF_SERVICE) {
                    infoView.setText("当前GPS状态：服务区外\n");
                } else if (status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
                    infoView.setText("当前GPS状态：暂停服务\n");
                }
            }
        };
    }

    /* 把定位结果显示到界面 */
    private void showLocation(Location location) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String time = sdf.format(new Date(location.getTime()));
        final String info = "定位来源：" + location.getProvider() + "\n"
                + "定位时间：" + time + "\n"
                + "定位精度：" + location.getAccuracy() + " 米";
        final String detail = "经度：" + location.getLongitude() + "\n"
                + "纬度：" + location.getLatitude() + "\n"
                + "海拔：" + location.getAltitude() + " 米";
        infoView.setText(info);
        locationView.setText(detail);
    }
}
