package com.example.test.ui.set_admin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.test.R;
import com.example.test.service.MyDatabaseHelper;
import com.example.test.ui.admin_management.AdminManagementActivity;
import com.example.test.ui.login.LoginActivity;
import com.example.test.ui.set_attendance.SetAttendanceActivity;
import com.mysql.jdbc.Connection;

public class SetAdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_admin);

        final Button backButton = findViewById(R.id.back);
        final EditText tUsername = findViewById(R.id.tUsername);
        final EditText adPwd = findViewById(R.id.password);
        final Button clearButton = findViewById(R.id.cancel);
        final Button submitButton = findViewById(R.id.submit);
        final String name = getIntent().getStringExtra("name");
        final String type = getIntent().getStringExtra("type");
        final int userID = getIntent().getIntExtra("userID",0);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass( SetAdminActivity.this, AdminManagementActivity.class);
                intent.putExtra("type", type);
                intent.putExtra("name",name);
                intent.putExtra("userID",userID);
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Connection conn = MyDatabaseHelper.CreateConnection();
                        String result = MyDatabaseHelper.SetAdminTeacher(conn, tUsername, adPwd);
                        MyDatabaseHelper.CloseConnection(conn);
                        Looper.prepare();
                        Toast.makeText(getBaseContext(),result,Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                });
                thread.start();
                thread.interrupt();
                Intent intent = new Intent();
                intent.setClass( SetAdminActivity.this, SetAdminActivity.class);
                intent.putExtra("type", type);
                intent.putExtra("name",name);
                intent.putExtra("userID",userID);
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });


        clearButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tUsername.setText("");
                adPwd.setText("");
            }
        });

    }
}
