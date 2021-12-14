package com.example.test.ui.student_management;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.example.test.R;
import com.example.test.service.MyDatabaseHelper;
import com.example.test.ui.course_management.CourseManagementActivity;
import com.example.test.ui.history_attendance.HistoryAttendanceActivity;
import com.example.test.ui.received_message.ReceivedMessageActivity;
import com.example.test.ui.submit_a_file.SubmitAFileActivity;
import com.mysql.jdbc.Connection;

public class StudentManagementActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_management);

        final Button submitButton = findViewById(R.id.SubmitAFile);
        final Button receivedMessageButton = findViewById(R.id.ReceiveMessage);
        final Button takeAttendanceButton = findViewById(R.id.TakeAttendance);
        final Button historyAttendanceButton = findViewById(R.id.HistoryAttendance);
        final Button backButton = findViewById(R.id.back);
        final String type = getIntent().getStringExtra("type");
        final String name = getIntent().getStringExtra("name");
        final int userID = getIntent().getIntExtra("userID", 0);
        final String courseName = getIntent().getStringExtra("courseName");


        androidx.appcompat.widget.Toolbar tb = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        getSupportActionBar().setTitle(courseName);

        takeAttendanceButton.setOnClickListener(new View.OnClickListener() {

            Handler handler = new Handler();

            @Override
            public void onClick(View v) {

                //声明定位回调监听器
                AMapLocationListener mLocationListener = new AMapLocationListener() {

                    public String lat = "";
                    public String lon = "";

                    @Override
                    public void onLocationChanged(AMapLocation amapLocation) {
                        if (amapLocation != null) {
                            if (amapLocation.getErrorCode() == 0) {
                                lat = String.valueOf(amapLocation.getLatitude());
                                lon = String.valueOf(amapLocation.getLongitude());

                                Bundle bundle = new Bundle();
                                Message message = Message.obtain();
                                bundle.putString("lat", lat);
                                bundle.putString("lon", lon);
                                message.setData(bundle);
                                handler.sendMessage(message);
//                                String displayToast = "<font color='#3d3d3d'>You have taken ONE-TOUCH attendance successfully ;-)</font>";
//                                Toast.makeText(StudentManagementActivity.this, Html.fromHtml(displayToast),Toast.LENGTH_LONG).show();
                            } else {
                                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                                Log.e("AmapError", "location Error, ErrCode:"
                                        + amapLocation.getErrorCode() + ", errInfo:"
                                        + amapLocation.getErrorInfo());
                                String displayToast = "<font color='#b44427'>Failed to take ONE-TOUCH attendance!</font>";
                                Toast.makeText(StudentManagementActivity.this, Html.fromHtml(displayToast), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                };

                AMapLocationClient mLocationClient = new AMapLocationClient(getApplicationContext());
                mLocationClient.setLocationListener(mLocationListener);

                AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
                //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
                mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
                //设置是否返回地址信息（默认返回地址信息）
                mLocationOption.setNeedAddress(true);
                //获取一次定位结果：该方法默认为false。
                mLocationOption.setOnceLocation(true);
                //设置是否允许模拟位置,默认为false，不允许模拟位置
                mLocationOption.setMockEnable(false);
                mLocationOption.setGeoLanguage(AMapLocationClientOption.GeoLanguage.EN);

                //给定位客户端对象设置定位参数
                mLocationClient.setLocationOption(mLocationOption);
                //启动定位
                mLocationClient.startLocation();

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final Connection conn = MyDatabaseHelper.CreateConnection();
                        //建立消息循环的步骤
                        Looper.prepare();//1、初始化Looper
                        handler = new Handler() {//2、绑定handler到CustomThread实例的Looper对象
                            public void handleMessage(Message msg) {//3、定义处理消息的方法
                                String lat = msg.getData().getString("lat");
                                String lon = msg.getData().getString("lon");
                                String displayToast = MyDatabaseHelper.StoreLocation(conn, type, lat, lon, courseName, userID);
                                Toast.makeText(StudentManagementActivity.this, Html.fromHtml(displayToast), Toast.LENGTH_LONG).show();
                            }
                        };
                        Looper.loop();//4、启动消息循环

                        MyDatabaseHelper.CloseConnection(conn);
                    }
                });
                thread.start();
                thread.interrupt();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(StudentManagementActivity.this, SubmitAFileActivity.class);
                intent.putExtra("type", type);
                intent.putExtra("name", name);
                intent.putExtra("userID", userID);
                intent.putExtra("courseName", courseName);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        receivedMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(StudentManagementActivity.this, ReceivedMessageActivity.class);
                intent.putExtra("type", type);
                intent.putExtra("name", name);
                intent.putExtra("userID", userID);
                intent.putExtra("courseName", courseName);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        historyAttendanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(StudentManagementActivity.this, HistoryAttendanceActivity.class);
                intent.putExtra("type", type);
                intent.putExtra("name", name);
                intent.putExtra("userID", userID);
                intent.putExtra("courseName", courseName);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(StudentManagementActivity.this, CourseManagementActivity.class);
                intent.putExtra("type", type);
                intent.putExtra("name", name);
                intent.putExtra("userID", userID);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }
}
