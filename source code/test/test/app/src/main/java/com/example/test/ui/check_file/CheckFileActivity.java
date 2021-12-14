package com.example.test.ui.check_file;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.test.R;
import com.example.test.service.MyDatabaseHelper;
import com.example.test.ui.check_attendance.CheckAttendanceActivity;
import com.example.test.ui.check_attendance2.CheckAttendance2Activity;
import com.example.test.ui.check_map.CheckMapActivity;
import com.mysql.jdbc.Connection;

import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CheckFileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.check_file);

        final TableLayout tableLayout = findViewById(R.id.table);
        final TextView title = findViewById(R.id.Title);
        final Button backButton = findViewById(R.id.back);
        Button refreshButton = findViewById(R.id.refresh);
        final ImageView picture = findViewById(R.id.picture);
        final String name = getIntent().getStringExtra("name");
        final String type = getIntent().getStringExtra("type");
        final int userID = getIntent().getIntExtra("userID",0);
        final String courseName = getIntent().getStringExtra("courseName");
        final String mid = getIntent().getStringExtra("mid");
        final String titleTime = getIntent().getStringExtra("titleTime");

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
                String sidIntent[] = msg.getData().getStringArray("sid");
                String sNameIntent[]= msg.getData().getStringArray("sName");
                String fNameIntent[] = msg.getData().getStringArray("fName");
                final String fid[] = msg.getData().getStringArray("fid");

                TableRow[] tableRow = new TableRow[len];
                final TextView[] fName = new TextView[len];
                TextView[] sid = new TextView[len];
                TextView[] sName = new TextView[len];

                TableRow.LayoutParams tableRowParam = new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT);
                TableRow.LayoutParams textViewParamLimit=new TableRow.LayoutParams(
                        40,
                        TableRow.LayoutParams.MATCH_PARENT
                );
                textViewParamLimit.setMargins(3,3,3,3);

                for(i=0;i<len;i++){

                    tableRow[i] = new TableRow(tableLayout.getContext());
                    {
                        tableRow[i].setLayoutParams(tableRowParam);
                        tableRow[i].setBackgroundColor(Color.parseColor("#3F51B5"));
                        tableRow[i].setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    }

                    fName[i] = new TextView(tableRow[i].getContext());
                    {
                        fName[i].setLayoutParams(textViewParamLimit);
                        fName[i].setBackgroundColor(Color.parseColor("#fafafa"));
                        fName[i].setGravity(Gravity.CENTER);
                        fName[i].setPadding(1,20,1,20);
                        fName[i].setTextColor(Color.parseColor("#6A7177"));
                        fName[i].setTextSize(15);
                    }

                    sid[i] = new TextView(tableRow[i].getContext());
                    {
                        sid[i].setLayoutParams(textViewParamLimit);
                        sid[i].setBackgroundColor(Color.parseColor("#fafafa"));
                        sid[i].setGravity(Gravity.CENTER);
                        sid[i].setPadding(1,20,1,20);
                        sid[i].setTextColor(Color.parseColor("#6A7177"));
                        sid[i].setTextSize(15);
                    }

                    sName[i] = new TextView(tableRow[i].getContext());
                    {
                        sName[i].setLayoutParams(textViewParamLimit);
                        sName[i].setBackgroundColor(Color.parseColor("#fafafa"));
                        sName[i].setGravity(Gravity.CENTER);
                        sName[i].setPadding(1,20,1,20);
                        sName[i].setTextColor(Color.parseColor("#6A7177"));
                        sName[i].setTextSize(15);
                    }

                    fName[i].setText(fNameIntent[i]);
                    sid[i].setText(sidIntent[i]);
                    sName[i].setText(sNameIntent[i]);


                    tableRow[i].addView(sid[i]);
                    tableRow[i].addView(sName[i]);
                    tableRow[i].addView(fName[i]);
                    tableLayout.addView(tableRow[i]);

                    final int fi = i;
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

                title.setText(titleTime);


                backButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setClass( CheckFileActivity.this, CheckAttendance2Activity.class);
                        intent.putExtra("type", type);
                        intent.putExtra("name",name);
                        intent.putExtra("userID",userID);
                        intent.putExtra("courseName",courseName);
                        intent.putExtra("titleTime",titleTime);
                        intent.putExtra("mid",mid);
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
                Bundle bundle = MyDatabaseHelper.CheckFileListData(conn, Integer.parseInt(mid));
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
                Intent intent = new Intent(CheckFileActivity.this, CheckFileActivity.class);
                intent.putExtra("type", type);
                intent.putExtra("name",name);
                intent.putExtra("userID",userID);
                intent.putExtra("courseName",courseName);
                intent.putExtra("mid",mid);
                intent.putExtra("titleTime",titleTime);
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });
    }
}
