package com.example.test.ui.history_attendance;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.test.R;
import com.example.test.service.MyDatabaseHelper;
import com.example.test.ui.check_attendance.CheckAttendanceActivity;
import com.example.test.ui.check_map.CheckMapActivity;
import com.example.test.ui.received_message.ReceivedMessageActivity;
import com.example.test.ui.student_management.StudentManagementActivity;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HistoryAttendanceActivity extends AppCompatActivity {
    final int IMAGE_CODE = 0;
    String path;//调取文件路径

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendance_list);

        final TableLayout tableLayout = findViewById(R.id.table);
        final Button backButton = findViewById(R.id.back);
        final Button refreshButton = findViewById(R.id.refresh);
        final ImageView picture = findViewById(R.id.picture);
        final String name = getIntent().getStringExtra("name");
        final String type = getIntent().getStringExtra("type");
        final int userID = getIntent().getIntExtra("userID",0);
        final String courseName = getIntent().getStringExtra("courseName");

        androidx.appcompat.widget.Toolbar tb = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        getSupportActionBar().setTitle(courseName);

        picture.setVisibility(View.GONE);

        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                picture.setVisibility(View.GONE);
            }});

                //Handler处理机制 是用来处理消息的
        final Handler handler = new Handler() {
            //Handler里的handMessage方法
            @Override
            public void handleMessage(Message msg) {
                int i = 0;
                int len = msg.getData().getInt("len");
                String timeIntent[] = msg.getData().getStringArray("time");
                String dateIntent[] = msg.getData().getStringArray("date");
                final String atTimeIntent[] = msg.getData().getStringArray("atTime");
                final String atDateIntent[] = msg.getData().getStringArray("atDate");
                String fNameIntent[]= msg.getData().getStringArray("fName");
                String atTypeIntent[]= msg.getData().getStringArray("atType");
                final String latIntent[]= msg.getData().getStringArray("lat");
                final String lonIntent[]= msg.getData().getStringArray("lon");
                final String state[]= msg.getData().getStringArray("state");
                final String fid[]= msg.getData().getStringArray("fid");

                TableRow[] tableRow = new TableRow[len];
                TextView[] time = new TextView[len];
                final TextView[] fName = new TextView[len];
                TextView[] atType = new TextView[len];
                TextView[] location = new TextView[len];

                TableRow.LayoutParams tableRowParam = new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT);
                TableRow.LayoutParams textViewParam=new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT);
                textViewParam.setMargins(3,3,3,3);
                TableRow.LayoutParams textViewParamLimit=new TableRow.LayoutParams(
                        40, TableRow.LayoutParams.MATCH_PARENT);
                textViewParamLimit.setMargins(3,3,3,3);

                for(i=0;i<len;i++){
                    Log.e("stateGet",i+": "+state[i]);
                    tableRow[i] = new TableRow(tableLayout.getContext());
                    {
                        tableRow[i].setLayoutParams(tableRowParam);
                        tableRow[i].setBackgroundColor(Color.parseColor("#3F51B5"));
                        tableRow[i].setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    }

                    time[i] = new TextView(tableRow[i].getContext());
                    {
                        time[i].setLayoutParams(textViewParamLimit);
                        time[i].setBackgroundColor(Color.parseColor("#fafafa"));
                        time[i].setGravity(Gravity.CENTER);
                        time[i].setPadding(1,5,1,5);
                        time[i].setTextColor(Color.parseColor("#6A7177"));
                        time[i].setTextSize(15);
                    }

                    fName[i] = new TextView(tableRow[i].getContext());
                    {
                        fName[i].setLayoutParams(textViewParamLimit);
                        fName[i].setBackgroundColor(Color.parseColor("#fafafa"));
                        fName[i].setGravity(Gravity.CENTER);
                        fName[i].setPadding(1,5,1,5);
                        fName[i].setTextColor(Color.parseColor("#6A7177"));
                        fName[i].setSingleLine();
                        fName[i].setEllipsize(TextUtils.TruncateAt.MIDDLE);
                        fName[i].setTextSize(15);
                    }

                    atType[i] = new TextView(tableRow[i].getContext());
                    {
                        atType[i].setLayoutParams(textViewParamLimit);
                        atType[i].setBackgroundColor(Color.parseColor("#fafafa"));
                        atType[i].setGravity(Gravity.CENTER);
                        atType[i].setPadding(1,5,1,5);
                        atType[i].setTextColor(Color.parseColor("#6A7177"));
                        atType[i].setSingleLine();
                        atType[i].setEllipsize(TextUtils.TruncateAt.MIDDLE);
                        atType[i].setTextSize(15);
                    }

                    location[i] = new TextView(tableRow[i].getContext());
                    {
                        location[i].setLayoutParams(textViewParamLimit);
                        location[i].setBackgroundColor(Color.parseColor("#fafafa"));
                        location[i].setGravity(Gravity.CENTER);
                        location[i].setPadding(1,5,1,5);
                        location[i].setTextColor(Color.parseColor("#6A7177"));
                        location[i].setText("☞");
                        location[i].setTextSize(15);
                    }

                    time[i].setText(dateIntent[i] +"\n" + timeIntent[i]);
                    atType[i].setText(atTypeIntent[i]);
                    fName[i].setText(fNameIntent[i]);
                    tableRow[i].addView(time[i]);
                    tableRow[i].addView(atType[i]);
                    tableRow[i].addView(fName[i]);
                    tableRow[i].addView(location[i]);
                    tableLayout.addView(tableRow[i]);

                    final int fi = i;
                    location[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent ii = new Intent();
                            ii.setClass(HistoryAttendanceActivity.this, CheckMapActivity.class);
                            ii.putExtra("type", type);
                            ii.putExtra("lat",new String[]{latIntent[fi]});
                            ii.putExtra("lon",new String[]{lonIntent[fi]});
                            ii.putExtra("name",name);
                            ii.putExtra("cName",courseName);
                            ii.putExtra("state",new String[]{state[fi]});
                            ii.putExtra("atTime",new String[]{atTimeIntent[fi]});
                            ii.putExtra("atDate",new String[]{atDateIntent[fi]});
                            startActivity(ii);
                            overridePendingTransition(0, 0);
                        }
                    });

                    fName[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Thread thread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Connection conn = MyDatabaseHelper.CreateConnection();
                                    String sql = "select * from file where fid = "+fid[fi];
                                    try {
                                        // 创建用来执行sql语句的对象
                                        java.sql.Statement statement = conn.createStatement();
                                        // 执行sql查询语句并获取查询信息
                                        ResultSet rs = statement.executeQuery(sql);
                                        rs.last();
                                        int i = 0;

                                        int check = rs.getRow();
                                        rs.beforeFirst();

                                        while (rs.next()) {
                                            InputStream ipath = rs.getBinaryStream("f_image");
                                            try{
                                                if(!fName[i].equals("-")) {
                                                    Bitmap bitmap = BitmapFactory.decodeStream(ipath);
                                                    picture.setImageBitmap(bitmap);    //设置Bitmap
                                                }
                                            }catch(Exception e){
                                                e.printStackTrace();
                                            }
                                            i++;
                                        }
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                    MyDatabaseHelper.CloseConnection(conn);
                                }});
                            thread.start();
                            thread.interrupt();
                            picture.setVisibility(View.VISIBLE);
                        }});
                }

                backButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setClass( HistoryAttendanceActivity.this, StudentManagementActivity.class);
                        intent.putExtra("type", type);
                        intent.putExtra("name",name);
                        intent.putExtra("userID",userID);
                        intent.putExtra("courseName",courseName);
                        startActivity(intent);
                        overridePendingTransition(0,0);
                    }
                });
            }
        };

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Connection conn= MyDatabaseHelper.CreateConnection();
                Bundle bundle = MyDatabaseHelper.StuAtdcListData(conn, courseName,userID);
                Message message = Message.obtain();
                message.setData(bundle);
                handler.sendMessage(message);
                MyDatabaseHelper.CloseConnection(conn);
            }
        });

        thread.start();
        thread.interrupt();

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(HistoryAttendanceActivity.this, HistoryAttendanceActivity.class);
                intent.putExtra("type", type);
                intent.putExtra("name",name);
                intent.putExtra("userID",userID);
                intent.putExtra("courseName",courseName);
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });
    }

    //重写onActivityResult以获得你需要的信息（返回图片路径）
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {        //此处的 RESULT_OK 是系统自定义得一个常量
            Log.e("TAG", "ActivityResult resultCode error");
            return;
        }


        Bitmap bm = null;
        //外界的程序访问ContentProvider所提供数据 可以通过ContentResolver接口
        ContentResolver resolver = getContentResolver();
        //此处的用于判断接收的Activity是不是你想要的那个
        if (requestCode == IMAGE_CODE) {
            try {
                Uri originalUri = data.getData();        //获得图片的uri
                bm = MediaStore.Images.Media.getBitmap(resolver, originalUri);        //显得到bitmap图片
                //这里开始的第二部分，获取图片的路径：
                String[] proj = {MediaStore.Images.Media.DATA};
                //好像是android多媒体数据库的封装接口，具体的看Android文档
                Cursor cursor = managedQuery(originalUri, proj, null, null, null);
                //按我个人理解 这个是获得用户选择的图片的索引值
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                //将光标移至开头 ，这个很重要，不小心很容易引起越界
                cursor.moveToFirst();
                //最后根据索引值获取图片路径
                path = cursor.getString(column_index);
            } catch (IOException e) {
                Log.e("TAG", e.toString());
            }
        }
    }

}
