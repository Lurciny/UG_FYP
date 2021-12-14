package com.example.test.ui.submit_a_file;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.test.R;
import com.example.test.service.MyDatabaseHelper;
import com.example.test.ui.student_management.StudentManagementActivity;
import com.mysql.jdbc.Connection;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class SubmitAFileActivity extends AppCompatActivity {

    // From openCamera
    public static final int TAKE_PHOTO = 1;
    String path;//调取文件路径
    String flow;
    final int IMAGE_CODE = 0;
    int writeflag = 0;//判断储存权限是否获取

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.submit_file);

        final Button backButton = findViewById(R.id.back);
        final Button submitButton = findViewById(R.id.submit);
        final Button selectButton = findViewById(R.id.selectFile);
        final ImageView picture = findViewById(R.id.picture);
        final String name = getIntent().getStringExtra("name");
        final String type = getIntent().getStringExtra("type");
        final int userID = getIntent().getIntExtra("userID",0);
        final String courseName = getIntent().getStringExtra("courseName");
        final String IMAGE_TYPE = "image/*";
        final int IMAGE_CODE = 0;


        if (!ActivityCompat.shouldShowRequestPermissionRationale(SubmitAFileActivity.this, Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(SubmitAFileActivity.this, new String[]{Manifest.permission.CAMERA}, 0);
        }
        //ActivityCompat.requestPermissions(OpenCameraActivity.this, new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},1);


        androidx.appcompat.widget.Toolbar tb = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        getSupportActionBar().setTitle(courseName);

            selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getAlbum = new Intent(Intent.ACTION_PICK);
                getAlbum.setType(IMAGE_TYPE);
                startActivityForResult(getAlbum, IMAGE_CODE);
            }
        });

        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    Bitmap bitmap = getLocalBitmap(path.toString()); //从本地取图片
                    picture.setImageBitmap(bitmap);    //设置Bitmap
                }catch(Exception e){
                    e.printStackTrace();
                }

                //将图片转化成二进制流
                try {
                    flow = String.valueOf(readStream(path));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Connection conn= MyDatabaseHelper.CreateConnection();
                        //Bundle bundle = MyDatabaseHelper.TeaAtdcList2Data(conn, 2);
                        //Message message = Message.obtain();
                        //message.setData(bundle);
                        try{
                            Boolean flag = MyDatabaseHelper.StorePicture(conn,path,userID,courseName);//如果不行的话就让Helper里面这个方程抛一个error
                            String displayToast = "<font color='#A52A2A'>You are not allow to submit file now!</font>";
                            if(flag)
                                displayToast = "<font color='#006400'>Submit the file successfully!</font>";
                            Looper.prepare();
                            Toast.makeText(SubmitAFileActivity.this, Html.fromHtml(displayToast),Toast.LENGTH_LONG).show();
                            Looper.loop();
                        }catch (Exception e){
                            Toast toast = Toast.makeText(SubmitAFileActivity.this,"Please at least choose one picture!",Toast.LENGTH_LONG);
                            toast.show();
                            Intent intent = new Intent();
                            intent.setClass(SubmitAFileActivity.this, StudentManagementActivity.class);
                            intent.putExtra("type", type);
                            intent.putExtra("name", name);
                            intent.putExtra("userID", userID);
                            intent.putExtra("courseName", courseName);
                            startActivity(intent);
                            overridePendingTransition(0, 0);
                            e.printStackTrace();
                        }
                        MyDatabaseHelper.CloseConnection(conn);
                    }
                });
                thread.start();
                thread.interrupt();

            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(  SubmitAFileActivity.this, StudentManagementActivity.class);
                intent.putExtra("type", type);
                intent.putExtra("name",name);
                intent.putExtra("userID",userID);
                intent.putExtra("courseName",courseName);
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });
    }

    /**
     * 加载本地图片
     * http://bbs.3gstdy.com
     * @param url
     * @return
     */
    public static Bitmap getLocalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }//end of 显示本地图片

    /**
     * 照片转byte二进制
     * @param imagepath 需要转byte的照片路径
     * @return 已经转成的byte
     * @throws Exception
     */
    public static byte[] readStream(String imagepath) throws Exception {
        FileInputStream fs = new FileInputStream(imagepath);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while (-1 != (len = fs.read(buffer))) {
            outStream.write(buffer, 0, len);
        }
        outStream.close();
        fs.close();
        return outStream.toByteArray();
    }

    //重写onActivityResult以获得你需要的信息（返回图片路径）
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {        //此处的 RESULT_OK 是系统自定义得一个常量
            Log.e("TAG", "ActivityResult resultCode error");
            return;
        }


        Bitmap bm = null;
        //外界的程序访问ContentProvider所提供数据 可以通过ContentResolver接口
        ContentResolver resolver = getContentResolver();
        //此处的用于判断接收的Activity是不是你想要的那个
        if (requestCode == IMAGE_CODE) {
            try {
                Uri originalUri = data.getData();        //获得图片的uri
                bm = MediaStore.Images.Media.getBitmap(resolver, originalUri);        //显得到bitmap图片
                //这里开始的第二部分，获取图片的路径：
                String[] proj = {MediaStore.Images.Media.DATA};
                //好像是android多媒体数据库的封装接口，具体的看Android文档
                Cursor cursor = managedQuery(originalUri, proj, null, null, null);
                //按我个人理解 这个是获得用户选择的图片的索引值
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                //将光标移至开头 ，这个很重要，不小心很容易引起越界
                cursor.moveToFirst();
                //最后根据索引值获取图片路径
                path = cursor.getString(column_index);
            } catch (IOException e) {
                Log.e("TAG", e.toString());
            }
        }
    }
}
