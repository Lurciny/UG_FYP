package com.example.test.ui.course_management;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.test.R;
import com.example.test.service.MyDatabaseHelper;
import com.example.test.ui.admin_management.AdminManagementActivity;
import com.example.test.ui.login.LoginActivity;
import com.example.test.ui.student_management.StudentManagementActivity;
import com.example.test.ui.teacher_management.TeacherManagementActivity;

import com.mysql.jdbc.Connection;

public class CourseManagementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.course_management);
        final Button backButton = findViewById(R.id.back);
        final LinearLayout linearLayout = findViewById(R.id.linearLayout);

        final String name = getIntent().getStringExtra("name");
        final String type = getIntent().getStringExtra("type");
        final int userID = getIntent().getIntExtra("userID", 0);

        //Toast.makeText(CourseManagementActivity.this, type + ": " + name, Toast.LENGTH_SHORT).show();

        androidx.appcompat.widget.Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        getSupportActionBar().setTitle(type + ": " + name);


        //Handler处理机制 是用来处理消息的
        final Handler handler = new Handler() {
            //Handler里的handMessage方法
            @Override
            public void handleMessage(Message msg) {
                final String[] course = (String[]) msg.obj;
                int i;

                Button[] courseButton = new Button[course.length];

                LinearLayout.LayoutParams buttonParam=new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        300);
                buttonParam.setMargins(5,5,5,5);

                for (i = 0;i<course.length;i++)
                {
                    courseButton[i] = new Button(linearLayout.getContext());
                    {
                        courseButton[i].setLayoutParams(buttonParam);
                        courseButton[i].setBackgroundColor(Color.parseColor("#CB0B7131"));
                        courseButton[i].setGravity(Gravity.CENTER);
                        courseButton[i].setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        courseButton[i].setTextColor(Color.parseColor("#FFFFFF"));
                        courseButton[i].setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                        courseButton[i].setTextSize(20);
                    }

                    courseButton[i].setText(course[i]);
                    final int temp = i;
                    courseButton[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent();
                            if (type.equals("Student"))
                                intent.setClass(CourseManagementActivity.this, StudentManagementActivity.class);
                            else
                                intent.setClass(CourseManagementActivity.this, TeacherManagementActivity.class);
                            intent.putExtra("name", name);
                            intent.putExtra("type", type);
                            intent.putExtra("userID",userID);
                            intent.putExtra("courseName", course[temp]);
                            startActivity(intent);
                            overridePendingTransition(0,0);
                        }
                    });
                    courseButton[i].setVisibility(View.VISIBLE);
                    linearLayout.addView(courseButton[i]);
                }

                backButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        if(type.equals("Student") | type.equals("Teacher"))
                            intent.setClass(CourseManagementActivity.this, LoginActivity.class);
                        else {
                            intent.setClass(CourseManagementActivity.this, AdminManagementActivity.class);
                            intent.putExtra("name", name);
                            intent.putExtra("type", type);
                            intent.putExtra("userID",userID);
                        }
                        startActivity(intent);
                        overridePendingTransition(0,0);
                    }
                });
                backButton.setVisibility(View.VISIBLE);

            }
        };

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String[] course;
                Connection conn= MyDatabaseHelper.CreateConnection();
                course = MyDatabaseHelper.CourseName(conn, userID, type);
                Message message = Message.obtain();
                message.obj = course;
                handler.sendMessage(message);
                MyDatabaseHelper.CloseConnection(conn);
            }
        });

        thread.start();
        thread.interrupt();

    }
}
