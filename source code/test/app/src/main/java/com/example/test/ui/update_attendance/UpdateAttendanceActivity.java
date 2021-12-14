package com.example.test.ui.update_attendance;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.test.R;
import com.example.test.service.MyDatabaseHelper;
import com.example.test.ui.delete_attendance.DeleteAttendanceActivity;
import com.example.test.ui.set_attendance.SetAttendanceActivity;
import com.example.test.ui.teacher_management.TeacherManagementActivity;
import com.example.test.ui.update_attendance2.UpdateAttendance2Activity;
import com.mysql.jdbc.Connection;

import java.util.Calendar;

public class UpdateAttendanceActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_attendance);

        final Button backButton = findViewById(R.id.back);
        final Button checkButton = findViewById(R.id.check);
        final EditText time = findViewById(R.id.time);
        final EditText date = findViewById(R.id.date);
        final Button clearButton = findViewById(R.id.cancel);
        final Button continueButton = findViewById(R.id.continuing);
        final String name = getIntent().getStringExtra("name");
        final String type = getIntent().getStringExtra("type");
        final int userID = getIntent().getIntExtra("userID",0);
        final String courseName = getIntent().getStringExtra("courseName");

        androidx.appcompat.widget.Toolbar tb = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        getSupportActionBar().setTitle(courseName);

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog dialog = new DatePickerDialog(UpdateAttendanceActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        date.setText(year + "-" + String.format("%02d", monthOfYear+1) + "-" + String.format("%02d", dayOfMonth));
                    }},
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            }
        });

        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                TimePickerDialog dialog = new TimePickerDialog(UpdateAttendanceActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        time.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute) + ":00");
                    }},
                        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),true);
                dialog.show();
            }
        });


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass( UpdateAttendanceActivity.this, TeacherManagementActivity.class);
                intent.putExtra("type", type);
                intent.putExtra("name",name);
                intent.putExtra("userID",userID);
                intent.putExtra("courseName",courseName);
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass( UpdateAttendanceActivity.this, UpdateAttendance2Activity.class);
                intent.putExtra("type", type);
                intent.putExtra("name",name);
                intent.putExtra("userID",userID);
                intent.putExtra("courseName",courseName);
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                time.setText("");
                date.setText("");
            }
        });

        checkButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Connection conn = MyDatabaseHelper.CreateConnection();
                        String result = MyDatabaseHelper.CheckAttendance(conn, time, date, courseName);
                        MyDatabaseHelper.CloseConnection(conn);
                        Looper.prepare();
                        Toast.makeText(getBaseContext(),result,Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                });
                thread.start();
                thread.interrupt();
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Connection conn = MyDatabaseHelper.CreateConnection();
                        String[] atid = MyDatabaseHelper.GetAtid(conn,time, date, courseName);
                        if(atid.length==0)
                        {
                            String result = MyDatabaseHelper.CheckAttendance(conn, time, date, courseName);
                            Looper.prepare();
                            Toast.makeText(getBaseContext(),result,Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }else{
                            Intent intent = new Intent();
                            intent.setClass( UpdateAttendanceActivity.this, UpdateAttendance2Activity.class);
                            intent.putExtra("type", type);
                            intent.putExtra("name",name);
                            intent.putExtra("userID",userID);
                            intent.putExtra("courseName",courseName);
                            intent.putExtra("atid",atid);
                            startActivity(intent);
                            overridePendingTransition(0,0);
                        }
                        MyDatabaseHelper.CloseConnection(conn);

                    }
                });
                thread.start();
                thread.interrupt();


            }
        });
    }

}
