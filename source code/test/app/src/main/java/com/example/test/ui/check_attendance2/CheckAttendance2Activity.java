package com.example.test.ui.check_attendance2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.test.R;
import com.example.test.service.MyDatabaseHelper;
import com.example.test.ui.check_attendance.CheckAttendanceActivity;
import com.example.test.ui.check_file.CheckFileActivity;
import com.example.test.ui.check_map.CheckMapActivity;
import com.example.test.ui.teacher_management.TeacherManagementActivity;
import com.mysql.jdbc.Connection;

public class CheckAttendance2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.check_attendance2);

        final TableLayout tableLayout = findViewById(R.id.table);
        final TextView title = findViewById(R.id.Title);
        final Button backButton = findViewById(R.id.back);
        final Button refreshButton = findViewById(R.id.refresh);
        final Button fileButton = findViewById(R.id.file);
        final Button mapButton = findViewById(R.id.Map);
        final String name = getIntent().getStringExtra("name");
        final String type = getIntent().getStringExtra("type");
        final int userID = getIntent().getIntExtra("userID",0);
        final String courseName = getIntent().getStringExtra("courseName");
        final String mid = getIntent().getStringExtra("mid");
        final String titleTime = getIntent().getStringExtra("titleTime");

        androidx.appcompat.widget.Toolbar tb = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        getSupportActionBar().setTitle(courseName);

        //Handler处理机制 是用来处理消息的
        final Handler handler = new Handler() {
            //Handler里的handMessage方法
            @Override
            public void handleMessage(Message msg) {
                int i = 0;
                int len = msg.getData().getInt("len");
                String sidIntent[] = msg.getData().getStringArray("sid");
                String sNameIntent[]= msg.getData().getStringArray("sName");
                String timeIntent[] = msg.getData().getStringArray("time");
                String stateIntent[]= msg.getData().getStringArray("state");
                final String latIntent[]= msg.getData().getStringArray("lat");
                final String lonIntent[]= msg.getData().getStringArray("lon");

                TableRow[] tableRow = new TableRow[len];
                TextView[] time = new TextView[len];
                TextView[] sid = new TextView[len];
                TextView[] sName = new TextView[len];
                TextView[] state = new TextView[len];

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

                    time[i] = new TextView(tableRow[i].getContext());
                    {
                        time[i].setLayoutParams(textViewParamLimit);
                        time[i].setBackgroundColor(Color.parseColor("#fafafa"));
                        time[i].setGravity(Gravity.CENTER);
                        time[i].setPadding(1,5,1,5);
                        time[i].setTextColor(Color.parseColor("#6A7177"));
                        time[i].setTextSize(15);
                    }

                    sid[i] = new TextView(tableRow[i].getContext());
                    {
                        sid[i].setLayoutParams(textViewParamLimit);
                        sid[i].setBackgroundColor(Color.parseColor("#fafafa"));
                        sid[i].setGravity(Gravity.CENTER);
                        sid[i].setPadding(1,5,1,5);
                        sid[i].setTextColor(Color.parseColor("#6A7177"));
                        sid[i].setTextSize(15);
                    }

                    sName[i] = new TextView(tableRow[i].getContext());
                    {
                        sName[i].setLayoutParams(textViewParamLimit);
                        sName[i].setBackgroundColor(Color.parseColor("#fafafa"));
                        sName[i].setGravity(Gravity.CENTER);
                        sName[i].setPadding(1,5,1,5);
                        sName[i].setTextColor(Color.parseColor("#6A7177"));
                        sName[i].setTextSize(15);
                    }

                    state[i] = new TextView(tableRow[i].getContext());
                    {
                        state[i].setLayoutParams(textViewParamLimit);
                        state[i].setBackgroundColor(Color.parseColor("#fafafa"));
                        state[i].setGravity(Gravity.CENTER);
                        state[i].setPadding(1,5,1,5);
                        state[i].setTextColor(Color.parseColor("#6A7177"));
                        state[i].setTextSize(15);
                    }

                    time[i].setText(timeIntent[i]);
                    sid[i].setText(sidIntent[i]);
                    sName[i].setText(sNameIntent[i]);
                    state[i].setText(stateIntent[i]);


                    tableRow[i].addView(sid[i]);
                    tableRow[i].addView(sName[i]);
                    tableRow[i].addView(time[i]);
                    tableRow[i].addView(state[i]);
                    tableLayout.addView(tableRow[i]);
                }

                title.setText(titleTime);

                mapButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.putExtra("lat",latIntent);
                        intent.putExtra("lon",lonIntent);
                        intent.putExtra("type","mid");
                        intent.putExtra("name",mid);
                        intent.putExtra("state",new String[]{"0"});
                        intent.putExtra("atTime",new String[]{"0"});
                        intent.putExtra("atDate",new String[]{"0"});
                        intent.putExtra("cName",courseName);
                        intent.setClass( CheckAttendance2Activity.this, CheckMapActivity.class);
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
                Bundle bundle = MyDatabaseHelper.CheckAtdcListData(conn, Integer.parseInt(mid));
                Message message = Message.obtain();
                message.setData(bundle);
                handler.sendMessage(message);
                MyDatabaseHelper.CloseConnection(conn);
            }
        });

        thread.start();
        thread.interrupt();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass( CheckAttendance2Activity.this, CheckAttendanceActivity.class);
                intent.putExtra("type", type);
                intent.putExtra("name",name);
                intent.putExtra("userID",userID);
                intent.putExtra("courseName",courseName);
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });

        fileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass( CheckAttendance2Activity.this, CheckFileActivity.class);
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



        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(CheckAttendance2Activity.this, CheckAttendance2Activity.class);
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
