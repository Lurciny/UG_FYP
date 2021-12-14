package com.example.test.ui.check_attendance;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.test.R;
import com.example.test.service.MyDatabaseHelper;
import com.example.test.ui.check_attendance2.CheckAttendance2Activity;
import com.example.test.ui.received_message.ReceivedMessageActivity;
import com.example.test.ui.student_management.StudentManagementActivity;
import com.example.test.ui.teacher_management.TeacherManagementActivity;
import com.mysql.jdbc.Connection;

public class CheckAttendanceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.check_attendance);

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
                String reminderIntent[]= msg.getData().getStringArray("reminder");
                String atTypeIntent[]= msg.getData().getStringArray("atType");
                String stateIntent[]= msg.getData().getStringArray("state");
                String midIntent[] = msg.getData().getStringArray("mid");
                String titleTimeIntent[] = msg.getData().getStringArray("titleTime");

                TableRow[] tableRow = new TableRow[len];
                TextView[] time = new TextView[len];
                TextView[] reminder = new TextView[len];
                TextView[] atType = new TextView[len];
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
                    final String mid = midIntent[i];
                    final String titleTime = titleTimeIntent[i];

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

                    reminder[i] = new TextView(tableRow[i].getContext());
                    {
                        reminder[i].setLayoutParams(textViewParamLimit);
                        reminder[i].setBackgroundColor(Color.parseColor("#fafafa"));
                        reminder[i].setGravity(Gravity.CENTER);
                        reminder[i].setPadding(1,5,1,5);
                        reminder[i].setTextColor(Color.parseColor("#6A7177"));
                        reminder[i].setTextSize(15);
                    }

                    atType[i] = new TextView(tableRow[i].getContext());
                    {
                        atType[i].setLayoutParams(textViewParamLimit);
                        atType[i].setBackgroundColor(Color.parseColor("#fafafa"));
                        atType[i].setGravity(Gravity.CENTER);
                        atType[i].setPadding(1,5,1,5);
                        atType[i].setTextColor(Color.parseColor("#6A7177"));
                        atType[i].setTextSize(15);
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

                    time[i].setText(dateIntent[i] +" " + timeIntent[i]);
                    reminder[i].setText(reminderIntent[i]);
                    atType[i].setText(atTypeIntent[i]);
                    state[i].setText(stateIntent[i]);

                    tableRow[i].addView(time[i]);
                    tableRow[i].addView(reminder[i]);
                    tableRow[i].addView(atType[i]);
                    tableRow[i].addView(state[i]);
                    tableLayout.addView(tableRow[i]);

                    tableRow[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent();
                            intent.setClass( CheckAttendanceActivity.this, CheckAttendance2Activity.class);
                            intent.putExtra("type", type);
                            intent.putExtra("name",name);
                            intent.putExtra("userID",userID);
                            intent.putExtra("courseName",courseName);;
                            intent.putExtra("mid",mid);
                            intent.putExtra("titleTime",titleTime);
                            startActivity(intent);
                            overridePendingTransition(0,0);
                        }
                    });
                }

            }
        };

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Connection conn= MyDatabaseHelper.CreateConnection();
                Bundle bundle = MyDatabaseHelper.TeaAtdcListData(conn, courseName);
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
                intent.setClass( CheckAttendanceActivity.this, TeacherManagementActivity.class);
                intent.putExtra("type", type);
                intent.putExtra("name",name);
                intent.putExtra("userID",userID);
                intent.putExtra("courseName",courseName);
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(CheckAttendanceActivity.this, CheckAttendanceActivity.class);
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
