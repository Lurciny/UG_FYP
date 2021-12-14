package com.example.test.ui.admin_list;

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
import com.example.test.ui.admin_management.AdminManagementActivity;
import com.example.test.ui.check_attendance.CheckAttendanceActivity;
import com.google.android.material.resources.TextAppearance;
import com.mysql.jdbc.Connection;

public class AdminListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_list);

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
                String aidIntent[] = msg.getData().getStringArray("aid");
                String aNameIntent[] = msg.getData().getStringArray("aName");
                String aTypeIntent[]= msg.getData().getStringArray("aType");

                TableRow[] tableRow = new TableRow[len];
                TextView[] aid = new TextView[len];
                TextView[] aName = new TextView[len];
                TextView[] aType = new TextView[len];

                TableRow.LayoutParams tableRowParam = new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT);
                TableRow.LayoutParams textViewParam=new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.MATCH_PARENT);
                textViewParam.setMargins(3,3,3,3);
                TableRow.LayoutParams textViewParamLimit=new TableRow.LayoutParams(
                        40, TableRow.LayoutParams.MATCH_PARENT
                        );
                textViewParamLimit.setMargins(3,3,3,3);

                for(i=0;i<len;i++){


                    tableRow[i] = new TableRow(tableLayout.getContext());
                    {
                        tableRow[i].setLayoutParams(tableRowParam);
                        tableRow[i].setBackgroundColor(Color.parseColor("#3F51B5"));
                        tableRow[i].setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    }

                    aid[i] = new TextView(tableRow[i].getContext());
                    {
                        aid[i].setLayoutParams(textViewParam);
                        aid[i].setBackgroundColor(Color.parseColor("#fafafa"));
                        aid[i].setGravity(Gravity.CENTER);
                        aid[i].setPadding(1,5,1,5);
                        aid[i].setTextColor(Color.parseColor("#6A7177"));
                        aid[i].setTextSize(17);
                    }

                    aName[i] = new TextView(tableRow[i].getContext());
                    {
                        aName[i].setLayoutParams(textViewParam);
                        aName[i].setBackgroundColor(Color.parseColor("#fafafa"));
                        aName[i].setGravity(Gravity.CENTER);
                        aName[i].setPadding(1,5,1,5);
                        aName[i].setTextColor(Color.parseColor("#6A7177"));
                        aName[i].setSingleLine();
                        aName[i].setEllipsize(TextUtils.TruncateAt.MIDDLE);
                        aName[i].setTextSize((float) 17.2);
                    }

                    aType[i] = new TextView(tableRow[i].getContext());
                    {
                        aType[i].setLayoutParams(textViewParamLimit);
                        aType[i].setBackgroundColor(Color.parseColor("#fafafa"));
                        aType[i].setGravity(Gravity.CENTER);
                        aType[i].setPadding(1,5,1,5);
                        aType[i].setTextColor(Color.parseColor("#6A7177"));
                        aType[i].setTextSize(15);
                    }

                    aid[i].setText(aidIntent[i]);
                    aName[i].setText(aNameIntent[i]);
                    aType[i].setText(aTypeIntent[i]);
                    tableRow[i].addView(aid[i]);
                    tableRow[i].addView(aName[i]);
                    tableRow[i].addView(aType[i]);
                    tableLayout.addView(tableRow[i]);
                }

                backButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setClass( AdminListActivity.this, AdminManagementActivity.class);
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
                Bundle bundle = MyDatabaseHelper.AdminListData(conn);
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
                Intent intent = new Intent(AdminListActivity.this, AdminListActivity.class);
                intent.putExtra("type", type);
                intent.putExtra("name",name);
                intent.putExtra("userID",userID);
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });

    }
}
