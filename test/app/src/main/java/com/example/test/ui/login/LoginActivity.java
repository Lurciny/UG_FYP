package com.example.test.ui.login;

import com.example.test.service.MyDatabaseHelper;

import android.app.Activity;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Looper;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test.R;
import com.example.test.ui.admin_management.AdminManagementActivity;
import com.example.test.ui.course_management.CourseManagementActivity;
import com.mysql.jdbc.Connection;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private String inputType="Student";



    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_NETWORK_STATE",
            "android.permission.ACCESS_WIFI_STATE",
            "android.permission.CHANGE_WIFI_STATE",
            "android.permission.INTERNET",
            "android.permission.READ_PHONE_STATE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.ACCESS_LOCATION_EXTRA_COMMANDS",
            "android.permission.BLUETOOTH",
            "android.permission.BLUETOOTH_ADMIN"};


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

         final EditText inputUname =  findViewById(R.id.username);
         final EditText inputPwd = findViewById(R.id.password);
         final Button loginButton = findViewById(R.id.login);
         final RadioGroup type =  findViewById(R.id.type);

        try {
            //检测是否有写的权限
            int permissionWrite = ActivityCompat.checkSelfPermission(this,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permissionWrite != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
            int permissionLocate = ActivityCompat.checkSelfPermission(this,
                    "android.permission.ACCESS_COARSE_LOCATION");
            if (permissionLocate != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }



        //查找用户
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread thread =  new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Connection conn = MyDatabaseHelper.CreateConnection();
                        String intentName = MyDatabaseHelper.LoginCheck(conn,inputType,inputUname,inputPwd);
                        int intentID = MyDatabaseHelper.UserID(conn,intentName,inputType);
                        if(intentName != "") {
                            Intent intent = new Intent();
                            if (inputType.equals("Teacher")) {
                                intent.setClass(LoginActivity.this, CourseManagementActivity.class);
                            } else if (inputType.equals("Student")) {
                                intent.setClass(LoginActivity.this, CourseManagementActivity.class);
                            } else {
                                intent.setClass(LoginActivity.this, AdminManagementActivity.class);
                            }
                            //携带字符串数据向另一个页面传值
                            intent.putExtra("name",intentName);
                            intent.putExtra("type", inputType);
                            intent.putExtra("userID",intentID);
                            startActivity(intent);
                            overridePendingTransition(R.anim.enter_anim,R.anim.exit_anim);

                        }else{
                            Looper.prepare();
                            String displayToast = "<font color='#A52A2A'>WRONG username / password / type \nof user: "+ inputUname.getText().toString()+"</font>";
                            Toast.makeText(LoginActivity.this, Html.fromHtml(displayToast),Toast.LENGTH_LONG).show();
                            Looper.loop();
                        }
                        MyDatabaseHelper.CloseConnection(conn);
                    }
                });
                thread.start();
                thread.interrupt();
            }
        });

        // 每输入一个字符就会执行一次：LoginFormState
        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    inputUname.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    inputPwd.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });


        // ViewModel check loginResult
        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                if (loginResult.getError() != null) {
                    Looper.prepare();
                    Toast.makeText(LoginActivity.this,"Wrong name/password/identity "+ inputUname.getText().toString(),Toast.LENGTH_SHORT).show();
                    Looper.loop();
                    return;
                }
                if (loginResult.getSuccess() != null) {
                    return;
                }
                setResult(Activity.RESULT_OK);

                //Complete and destroy login activity once successful
                //finish();
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(inputUname.getText().toString(),
                        inputPwd.getText().toString());
            }
        };

        inputUname.addTextChangedListener(afterTextChangedListener);
        inputPwd.addTextChangedListener(afterTextChangedListener);

        inputPwd.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(inputUname.getText().toString(),
                            inputPwd.getText().toString());
                }
                return false;
            }
        });

        type.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 获取选中的RadioButton的id
                int id = group.getCheckedRadioButtonId();
                // 通过id实例化选中的这个RadioButton
                RadioButton choice = (RadioButton) findViewById(id);
                // 获取这个RadioButton的text内容
                    inputType = choice.getText().toString();
            }
        });
    }
}
