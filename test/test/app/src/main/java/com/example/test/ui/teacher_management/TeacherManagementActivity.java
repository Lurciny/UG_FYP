package com.example.test.ui.teacher_management;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.test.R;
import com.example.test.service.MyDatabaseHelper;
import com.example.test.ui.check_attendance.CheckAttendanceActivity;
import com.example.test.ui.course_management.CourseManagementActivity;
import com.example.test.ui.delete_attendance.DeleteAttendanceActivity;
import com.example.test.ui.set_attendance.SetAttendanceActivity;
import com.example.test.ui.student_list.StudentListActivity;
import com.example.test.ui.student_management.StudentManagementActivity;
import com.example.test.ui.update_attendance.UpdateAttendanceActivity;
import com.mysql.jdbc.Connection;


public class TeacherManagementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teacher_management);

        final Button setButton = findViewById(R.id.SetAttendance);
        final Button deleteButton= findViewById(R.id.DeleteAttendance);
        final Button updateButton = findViewById(R.id.UpdateAttendance);
        final Button checkButton = findViewById(R.id.CheckAttendance);
        final Button remindButton = findViewById(R.id.RemindStudent);
        final Button studentButton = findViewById(R.id.StudentList);
        final Button backButton = findViewById(R.id.back);
        final String type = getIntent().getStringExtra("type");
        final String name = getIntent().getStringExtra("name");
        final String courseName = getIntent().getStringExtra("courseName");
        final int userID = getIntent().getIntExtra("userID",0);
        androidx.appcompat.widget.Toolbar tb = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        getSupportActionBar().setTitle(courseName);

        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass( TeacherManagementActivity.this, SetAttendanceActivity.class);
                intent.putExtra("type", type);
                intent.putExtra("name",name);
                intent.putExtra("userID",userID);
                intent.putExtra("courseName",courseName);
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass( TeacherManagementActivity.this, DeleteAttendanceActivity.class);
                intent.putExtra("type", type);
                intent.putExtra("name",name);
                intent.putExtra("userID",userID);
                intent.putExtra("courseName",courseName);
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass( TeacherManagementActivity.this, UpdateAttendanceActivity.class);
                intent.putExtra("type", type);
                intent.putExtra("name",name);
                intent.putExtra("userID",userID);
                intent.putExtra("courseName",courseName);
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });


        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass( TeacherManagementActivity.this, CheckAttendanceActivity.class);
                intent.putExtra("type", type);
                intent.putExtra("name",name);
                intent.putExtra("userID",userID);
                intent.putExtra("courseName",courseName);
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });

        remindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        final Connection conn = MyDatabaseHelper.CreateConnection();
                        String displayToast = MyDatabaseHelper.RemindStuTakeAT(conn,courseName,userID);
                        Looper.prepare();
                        Toast.makeText(TeacherManagementActivity.this, Html.fromHtml(displayToast),Toast.LENGTH_LONG).show();
                        Looper.loop();
                        MyDatabaseHelper.CloseConnection(conn);
                    }
                });
                thread.start();
                thread.interrupt();
            }
        });


        studentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass( TeacherManagementActivity.this, StudentListActivity.class);
                intent.putExtra("type", type);
                intent.putExtra("name",name);
                intent.putExtra("userID",userID);
                intent.putExtra("courseName",courseName);
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass( TeacherManagementActivity.this, CourseManagementActivity.class);
                intent.putExtra("type", type);
                intent.putExtra("name",name);
                intent.putExtra("userID",userID);
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });

    }
}
