package com.example.test.ui.student_list;

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
import com.example.test.ui.check_attendance2.CheckAttendance2Activity;
import com.example.test.ui.check_file.CheckFileActivity;
import com.example.test.ui.check_map.CheckMapActivity;
import com.example.test.ui.student_management.StudentManagementActivity;
import com.example.test.ui.teacher_management.TeacherManagementActivity;
import com.mysql.jdbc.Connection;

public class StudentListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_list);

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
                final String sidIntent[] = msg.getData().getStringArray("sid");
                String sNameIntent[]= msg.getData().getStringArray("sName");
                String timesIntent[] = msg.getData().getStringArray("times");

                TableRow[] tableRow = new TableRow[len];
                TextView[] times = new TextView[len];
                TextView[] sid = new TextView[len];
                TextView[] sName = new TextView[len];
                TextView[] location = new TextView[len];

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

                    times[i] = new TextView(tableRow[i].getContext());
                    {
                        times[i].setLayoutParams(textViewParamLimit);
                        times[i].setBackgroundColor(Color.parseColor("#fafafa"));
                        times[i].setGravity(Gravity.CENTER);
                        times[i].setPadding(1,20,1,20);
                        times[i].setTextColor(Color.parseColor("#6A7177"));
                        times[i].setTextSize(15);
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

                    times[i].setText(timesIntent[i]);
                    sid[i].setText(sidIntent[i]);
                    sName[i].setText(sNameIntent[i]);

                    tableRow[i].addView(sid[i]);
                    tableRow[i].addView(sName[i]);
                    tableRow[i].addView(times[i]);
                    tableRow[i].addView(location[i]);
                    tableLayout.addView(tableRow[i]);

                    final int fi = i;
                    tableRow[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent();
                            intent.putExtra("lat",new String[]{"0"});
                            intent.putExtra("lon",new String[]{"0"});
                            intent.putExtra("type",type);
                            intent.putExtra("name",sidIntent[fi]);
                            intent.putExtra("state",new String[]{"0"});
                            intent.putExtra("atTime",new String[]{"0"});
                            intent.putExtra("atDate",new String[]{"0"});
                            intent.putExtra("cName",courseName);
                            intent.setClass( StudentListActivity.this, CheckMapActivity.class);
                            startActivity(intent);
                            overridePendingTransition(0,0);
                        }
                    });
                }

                backButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setClass( StudentListActivity.this, TeacherManagementActivity.class);
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
                Bundle bundle = MyDatabaseHelper.StudentListData(conn, courseName);
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
                Intent intent = new Intent(StudentListActivity.this, StudentListActivity.class);
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
