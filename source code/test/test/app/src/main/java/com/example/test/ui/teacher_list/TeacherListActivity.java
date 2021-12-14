package com.example.test.ui.teacher_list;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
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
import com.mysql.jdbc.Connection;

public class TeacherListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teacher_list);

        final TableLayout tableLayout = findViewById(R.id.table);
        final Button backButton = findViewById(R.id.back);
        final Button refreshButton = findViewById(R.id.refresh);
        final String name = getIntent().getStringExtra("name");
        final String type = getIntent().getStringExtra("type");
        final int userID = getIntent().getIntExtra("userID",0);

        //Handler处理机制 是用来处理消息的
        final Handler handler = new Handler() {
            //Handler里的handMessage方法
            @Override
            public void handleMessage(Message msg) {
                int i = 0;
                int len = msg.getData().getInt("len");
                String tNameIntent[] = msg.getData().getStringArray("tName");
                String cNameIntent[] = msg.getData().getStringArray("cName");

                TableRow[] tableRow = new TableRow[len];
                TextView[] tName = new TextView[len];
                TextView[] cName = new TextView[len];

                TableRow.LayoutParams tableRowParam = new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT);
                TableRow.LayoutParams textViewParam=new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT);
                textViewParam.setMargins(3,3,3,3);
                TableRow.LayoutParams textViewParamLimit=new TableRow.LayoutParams(
                        40,
                        TableRow.LayoutParams.MATCH_PARENT);
                textViewParamLimit.setMargins(3,3,3,3);

                for(i=0;i<len;i++){
                    tableRow[i] = new TableRow(tableLayout.getContext());
                    {
                        tableRow[i].setLayoutParams(tableRowParam);
                        tableRow[i].setBackgroundColor(Color.parseColor("#3F51B5"));
                        tableRow[i].setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    }

                    tName[i] = new TextView(tableRow[i].getContext());
                    {
                        tName[i].setLayoutParams(textViewParam);
                        tName[i].setBackgroundColor(Color.parseColor("#fafafa"));
                        tName[i].setGravity(Gravity.CENTER);
                        tName[i].setPadding(1,5,1,5);
                        tName[i].setTextColor(Color.parseColor("#6A7177"));
                        tName[i].setSingleLine();
                        tName[i].setEllipsize(TextUtils.TruncateAt.END);
                        tName[i].setTextSize((float) 17.2);
                    }

                    cName[i] = new TextView(tableRow[i].getContext());
                    {
                        cName[i].setLayoutParams(textViewParamLimit);
                        cName[i].setBackgroundColor(Color.parseColor("#fafafa"));
                        cName[i].setGravity(Gravity.CENTER);
                        cName[i].setPadding(1,5,1,5);
                        cName[i].setTextColor(Color.parseColor("#6A7177"));
                        cName[i].setSingleLine();
                        cName[i].setEllipsize(TextUtils.TruncateAt.MIDDLE);
                        cName[i].setTextSize(15);
                    }

                    tName[i].setText(tNameIntent[i]);
                    cName[i].setText(cNameIntent[i]);

                    tableRow[i].addView(tName[i]);
                    tableRow[i].addView(cName[i]);
                    tableLayout.addView(tableRow[i]);
                }

                backButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setClass( TeacherListActivity.this, AdminManagementActivity.class);
                        intent.putExtra("type", type);
                        intent.putExtra("name",name);
                        intent.putExtra("userID",userID);
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
                Bundle bundle = MyDatabaseHelper.TeacherListData(conn);
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
                Intent intent = new Intent(TeacherListActivity.this, TeacherListActivity.class);
                intent.putExtra("type", type);
                intent.putExtra("name",name);
                intent.putExtra("userID",userID);
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });

    }
}
