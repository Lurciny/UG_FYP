package com.example.test.ui.received_message;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.test.R;
import com.example.test.service.MyDatabaseHelper;
import com.example.test.ui.admin_list.AdminListActivity;
import com.example.test.ui.admin_management.AdminManagementActivity;
import com.example.test.ui.check_attendance.CheckAttendanceActivity;
import com.example.test.ui.student_management.StudentManagementActivity;
import com.mysql.jdbc.Connection;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ReceivedMessageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_list);

        final TableLayout tableLayout = findViewById(R.id.table);
        final Button backButton = findViewById(R.id.back);
        final Button refreshButton = findViewById(R.id.refresh);
        final String name = getIntent().getStringExtra("name");
        final String type = getIntent().getStringExtra("type");
        final int userID = getIntent().getIntExtra("userID",0);
        final String courseName = getIntent().getStringExtra("courseName");

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
                String timeIntent[] = msg.getData().getStringArray("time");
                String dateIntent[] = msg.getData().getStringArray("date");
                String messageIntent[]= msg.getData().getStringArray("message");

                TableRow[] tableRow = new TableRow[len];
                TextView[] time = new TextView[len];
                TextView[] message = new TextView[len];

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
                        time[i].setPadding(1, 5, 1, 5);
                        time[i].setTextColor(Color.parseColor("#6A7177"));
                        time[i].setTextSize(15);
                    }

                    message[i] = new TextView(tableRow[i].getContext());
                    {
                        message[i].setLayoutParams(textViewParamLimit);
                        message[i].setBackgroundColor(Color.parseColor("#fafafa"));
                        message[i].setGravity(Gravity.CENTER);
                        message[i].setPadding(1, 5, 1, 5);
                        message[i].setTextColor(Color.parseColor("#6A7177"));
                        message[i].setTextSize((float) 17.2);
                    }

                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String currentDT = sdf.format(System.currentTimeMillis());
                        String inputDT = dateIntent[i]+" "+timeIntent[i];
                        Date d1 = sdf.parse(currentDT);
                        Date d2 = sdf.parse(inputDT);
                        if((d2.getTime() - d1.getTime())/(24*3600*1000) < 0) {
                            time[i].setBackgroundColor(Color.parseColor("#d1dcd7"));
                            message[i].setBackgroundColor(Color.parseColor("#d1dcd7"));
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    time[i].setText(dateIntent[i] + " " + timeIntent[i]);
                    message[i].setText(messageIntent[i]);
                    tableRow[i].addView(time[i]);
                    tableRow[i].addView(message[i]);
                    tableLayout.addView(tableRow[i]);


                }

                backButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setClass( ReceivedMessageActivity.this, StudentManagementActivity.class);
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
                Bundle bundle = MyDatabaseHelper.MessageListData(conn, courseName);
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
                Intent intent = new Intent(ReceivedMessageActivity.this, ReceivedMessageActivity.class);
                intent.putExtra("type", type);
                intent.putExtra("name",name);
                intent.putExtra("userID",userID);
                intent.putExtra("courseName",courseName);
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });

    }
}
