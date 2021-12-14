package com.example.test.ui.update_admin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.test.R;
import com.example.test.service.MyDatabaseHelper;
import com.example.test.ui.admin_management.AdminManagementActivity;
import com.example.test.ui.login.LoginActivity;
import com.mysql.jdbc.Connection;

public class UpdateAdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_admin);

        final Button backButton = findViewById(R.id.back);
        final String name = getIntent().getStringExtra("name");
        final String type = getIntent().getStringExtra("type");
        final int userID = getIntent().getIntExtra("userID",0);
        final EditText username = findViewById(R.id.username);
        final EditText password = findViewById(R.id.password);
        final Button clearButton = findViewById(R.id.cancel);
        final Button submitButton = findViewById(R.id.submit);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass( UpdateAdminActivity.this, AdminManagementActivity.class);
                intent.putExtra("type", type);
                intent.putExtra("name",name);
                intent.putExtra("userID",userID);
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                username.setText("");
                password.setText("");
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Connection conn = MyDatabaseHelper.CreateConnection();
                        String result = MyDatabaseHelper.UpdateAdmin(conn, username, password);
                        MyDatabaseHelper.CloseConnection(conn);
                        Looper.prepare();
                        Toast.makeText(getBaseContext(), Html.fromHtml(result),Toast.LENGTH_LONG).show();
                        Looper.loop();
                    }
                });

                if( username.getText().toString().length() < 5 || password.getText().toString().length() < 6)
                {
                    String errorToast = "At least 5 characters for Username;   at least 6 characters for Password.";
                    Toast.makeText(getBaseContext(), Html.fromHtml(errorToast),Toast.LENGTH_LONG).show();
                    Intent intent = new Intent();
                    intent.setClass( UpdateAdminActivity.this, UpdateAdminActivity.class);
                    intent.putExtra("type", type);
                    intent.putExtra("name",name);
                    intent.putExtra("userID",userID);
                    startActivity(intent);
                    overridePendingTransition(0,0);
                }else{
                    thread.start();
                    thread.interrupt();

                    Intent intent = new Intent();
                    intent.setClass(UpdateAdminActivity.this, LoginActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0,0);
                }
            }
        });
    }


}
