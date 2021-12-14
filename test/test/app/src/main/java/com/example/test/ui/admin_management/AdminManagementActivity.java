package com.example.test.ui.admin_management;

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
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test.R;
import com.example.test.service.MyDatabaseHelper;
import com.example.test.ui.admin_list.AdminListActivity;
import com.example.test.ui.course_management.CourseManagementActivity;
import com.example.test.ui.delete_admin.DeleteAdminActivity;
import com.example.test.ui.login.LoginActivity;
import com.example.test.ui.set_admin.SetAdminActivity;
import com.example.test.ui.teacher_list.TeacherListActivity;
import com.example.test.ui.update_admin.UpdateAdminActivity;
import com.mysql.jdbc.Connection;

public class AdminManagementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_management);

        final TextView adminText = findViewById(R.id.adminText);
        final TableRow adminFunc = findViewById(R.id.adminFunc);
        final Button backButton = findViewById(R.id.back);
        final Button adminButton = findViewById(R.id.admin);
        final Button teacherButton = findViewById(R.id.teacher);
        final Button courseButton = findViewById(R.id.course);
        final Button setButton = findViewById(R.id.setAdmin);
        final Button deleteButton = findViewById(R.id.deleteAdmin);
        final Button updateButton = findViewById(R.id.updateAdmin);
        final String name = getIntent().getStringExtra("name");
        final String type = getIntent().getStringExtra("type");
        final int userID = getIntent().getIntExtra("userID",0);

        adminText.setVisibility(View.GONE);
        adminFunc.setVisibility(View.GONE);

        //Handler处理机制 是用来处理消息的
        final Handler handler = new Handler() {
            //Handler里的handMessage方法
            @Override
            public void handleMessage(Message msg) {
                String adminType = (String)msg.obj;

                //Toast.makeText(AdminManagementActivity.this,adminType+": "+name,Toast.LENGTH_SHORT).show();

                androidx.appcompat.widget.Toolbar tb = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(tb);
                getSupportActionBar().setTitle(adminType+": "+name);

                if(adminType.equals("Administrator"))
                {
                    adminText.setVisibility(View.VISIBLE);
                    adminFunc.setVisibility(View.VISIBLE);
                }

                backButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setClass( AdminManagementActivity.this, LoginActivity.class);
                        startActivity(intent);
                        overridePendingTransition(0,0);
                    }
                });

                adminButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setClass( AdminManagementActivity.this, AdminListActivity.class);
                        intent.putExtra("type", type);
                        intent.putExtra("name",name);
                        intent.putExtra("userID",userID);
                        startActivity(intent);
                        overridePendingTransition(0,0);
                    }
                });

                teacherButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setClass( AdminManagementActivity.this, TeacherListActivity.class);
                        intent.putExtra("type", type);
                        intent.putExtra("name",name);
                        intent.putExtra("userID",userID);
                        startActivity(intent);
                        overridePendingTransition(0,0);
                    }
                });

                courseButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setClass( AdminManagementActivity.this, CourseManagementActivity.class);
                        intent.putExtra("type", type);
                        intent.putExtra("name",name);
                        intent.putExtra("userID",userID);
                        startActivity(intent);
                        overridePendingTransition(0,0);
                    }
                });

                setButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setClass( AdminManagementActivity.this, SetAdminActivity.class);
                        intent.putExtra("type", type);
                        intent.putExtra("name",name);
                        intent.putExtra("userID",userID);
                        startActivity(intent);
                        overridePendingTransition(0,0);
                    }
                });

                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setClass( AdminManagementActivity.this, DeleteAdminActivity.class);
                        intent.putExtra("type", type);
                        intent.putExtra("name",name);
                        intent.putExtra("userID",userID);
                        startActivity(intent);
                        overridePendingTransition(0,0);
                    }
                });

                updateButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setClass( AdminManagementActivity.this, UpdateAdminActivity.class);
                        intent.putExtra("type", type);
                        intent.putExtra("name",name);
                        intent.putExtra("userID",userID);
                        startActivity(intent);
                        overridePendingTransition(0,0);
                    }
                });
        }};

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Connection conn= MyDatabaseHelper.CreateConnection();
                String adminType = MyDatabaseHelper.GetAdminType(conn,userID);
                Message message = Message.obtain();
                message.obj = adminType;
                handler.sendMessage(message);
                MyDatabaseHelper.CloseConnection(conn);
            }
        });

        thread.start();
        thread.interrupt();

    }


}
