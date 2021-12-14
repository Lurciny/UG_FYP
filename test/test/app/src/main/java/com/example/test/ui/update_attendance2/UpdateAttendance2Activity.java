package com.example.test.ui.update_attendance2;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.test.R;
import com.example.test.service.MyDatabaseHelper;
import com.example.test.ui.set_attendance.SetAttendanceActivity;
import com.example.test.ui.teacher_list.TeacherListActivity;
import com.example.test.ui.teacher_management.TeacherManagementActivity;
import com.example.test.ui.update_attendance.UpdateAttendanceActivity;
import com.mysql.jdbc.Connection;

import java.text.ParseException;
import java.util.Calendar;

public class UpdateAttendance2Activity extends AppCompatActivity {

    private String atWay="Touch";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_attendance2);

        final Button backButton = findViewById(R.id.back);
        final String name = getIntent().getStringExtra("name");
        final String type = getIntent().getStringExtra("type");
        final int userID = getIntent().getIntExtra("userID",0);
        final String courseName = getIntent().getStringExtra("courseName");
        final String[] atid = getIntent().getStringArrayExtra("atid");
        final EditText time = findViewById(R.id.time);
        final EditText date = findViewById(R.id.date);
        final EditText remind = findViewById(R.id.remind);
        final EditText content = findViewById(R.id.content);
        final Button clearButton = findViewById(R.id.cancel);
        final  Button submitButton = findViewById(R.id.submit);
        final RadioGroup radioGroup = findViewById(R.id.atDetail);

        androidx.appcompat.widget.Toolbar tb = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        getSupportActionBar().setTitle(courseName);

        content.setVisibility(View.GONE);

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog dialog = new DatePickerDialog(UpdateAttendance2Activity.this, new DatePickerDialog.OnDateSetListener() {
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
                TimePickerDialog dialog = new TimePickerDialog(UpdateAttendance2Activity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        time.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute) + ":00");
                    }},
                        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),true);
                dialog.show();
            }
        });

        remind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                TimePickerDialog dialog = new TimePickerDialog(UpdateAttendance2Activity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        remind.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute) + ":00");;
                    }},
                        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),true);
                dialog.show();
            }
        });


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass( UpdateAttendance2Activity.this, UpdateAttendanceActivity.class);
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
                remind.setText("");
                content.setText("");
            }
        });


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 获取选中的RadioButton的id
                int id = group.getCheckedRadioButtonId();
                // 通过id实例化选中的这个RadioButton
                RadioButton choice = findViewById(id);
                // 获取这个RadioButton的text内容
                String ct = choice.getText().toString();
                if(ct.equals("One-Touch Sign In")) {
                    atWay = "Touch";
                    content.setVisibility(View.GONE);
                }
                else {
                    atWay = "TouchF";
                    content.setVisibility(View.VISIBLE);
                }
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Connection conn = MyDatabaseHelper.CreateConnection();
                        String result = "Plz set the reminder time BEFORE the required time";
                        if(MyDatabaseHelper.CheckTimeInterval(time,remind)) {
                            try {
                                if(!MyDatabaseHelper.CheckInputDT(time,remind,date))
                                    result = "Please enter a time 15 mins LATER than the CURRENT system time";
                                else
                                    result = MyDatabaseHelper.UpdateAttendance(conn, time, date, remind, content,atWay,atid);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        MyDatabaseHelper.CloseConnection(conn);
                        Looper.prepare();
                        Toast.makeText(getBaseContext(),result,Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                });
                thread.start();
                thread.interrupt();

                Intent intent = new Intent();
                intent.setClass( UpdateAttendance2Activity.this, TeacherManagementActivity.class);
                intent.putExtra("type", type);
                intent.putExtra("name",name);
                intent.putExtra("userID",userID);
                intent.putExtra("courseName", courseName);
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });
    }
}
