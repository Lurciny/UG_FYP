package com.example.test.service;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.mysql.jdbc.Connection;

import java.io.File;
import java.io.FileInputStream;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MyDatabaseHelper {

    private static final String TAG = "MyDbHelper";
    private static String username;
    private static String password;


    public static String getUsername(){
        return username;
    }

    public static String getPassword(){
        return password;
    }

    public static Connection CreateConnection() {
//                // 反复尝试连接，直到连接成功后退出循环(可用于刷新match到的数据)
//                while (!Thread.interrupted()) {
//                    try {
//                        Thread.sleep(100);  // 每隔0.1秒尝试连接
//                    } catch (InterruptedException e) {
//                        Log.e(TAG, e.toString());
//                    }

                // 2.设置好IP/端口/数据库名/用户名/密码等必要的连接信息
                String url = "jdbc:mysql://rm-bp12zfmlg3zj68i3d5o.mysql.rds.aliyuncs.com:3306/exercise"; // 构建连接mysql的字符串
                String user = "admin123456";
                String password = "Admin123456";
                Connection conn;
                // 3.连接JDBC
                try {
                    conn = (Connection) DriverManager.getConnection(url, user, password);
                    Log.i(TAG, "远程连接成功!");
                    return conn;
                } catch (SQLException e) {
                    Log.e(TAG, "远程连接失败!");
                    return null;
                }

            }

    public static void CloseConnection(Connection conn) {
        try {
            conn.close();
            Log.e(TAG,"关闭连接成功");
        } catch (SQLException e) {
            Log.e(TAG, "关闭连接失败");
        }
    }

    public static String StoreLocation(Connection conn, String inputType, String latitude, String longitude,String cName, int userID) {

        String r = "<font color='#b44427'>Failed to take ONE-TOUCH attendance!</font>";
        if (conn != null) {
            String insert_o_sql = "";
            String oid_sql ="";
            String atid = "";
            String at_type ="";
            String atid_sql="";
            String updateOid_sql="";
            String updateAtid_sql="";
            String oid = "1";
            switch (inputType) {
                case "Student":
                    insert_o_sql = "insert into `location`(`o_latitude`, `o_longitude`, `o_state`, `o_date`, `o_time`) " +
                            "values("+latitude+","+longitude+", 'present', CURRENT_DATE, CURRENT_TIME)";
                    oid_sql = "select Max(oid) as mo from location";
                    atid_sql ="SELECT atid, at_type FROM `attendance`, `course`, `learn`WHERE(`attendance`.`eid`= `learn`.`eid`" +
                            " AND `learn`.`cid`= `course`.`cid` AND `learn`.`sid`= "+userID+" AND `course`.`c_name`= '"+cName+
                            "' AND(NOW() - str_to_date(CONCAT(at_date, ' ', at_time), '%Y-%m-%d %H:%i:%s') >= 0" +
                            " AND NOW() - str_to_date(CONCAT(at_date, ' ', at_time), '%Y-%m-%d %H:%i:%s') <= 1500))" +
                            " ORDER BY NOW() - str_to_date(CONCAT(at_date, ' ', at_time), '%Y-%m-%d %H:%i:%s') ASC limit 1";
                    break;
                case "Teacher":
                case "Administrator":
                    insert_o_sql = "";
                    break;
            }

            try {
                // 创建用来执行sql语句的对象
                java.sql.Statement statement = conn.createStatement();
                ResultSet rss = statement.executeQuery(atid_sql);
                rss.last();
                int check = rss.getRow();
                rss.beforeFirst();

                while (rss.next()) {
                    atid = rss.getString("atid");
                    at_type = rss.getString("at_type");
                }
                rss.close();

                if (check != 0) {
                    statement.executeUpdate(insert_o_sql);
                    ResultSet rs = statement.executeQuery(oid_sql);
                    rs.last();
                    rs.beforeFirst();
                    while (rs.next()) {
                        oid = rs.getString("mo");
                    }
                    rs.close();

                    if(Double.valueOf(latitude)<22.352344&&Double.valueOf(latitude)>22.347264
                    &&Double.valueOf(longitude)<113.52466&&Double.valueOf(longitude)>113.516914)
                    {
                        updateOid_sql = "UPDATE `exercise`.`attendance` SET oid = "+oid+" WHERE atid = "+atid;
                        statement.executeUpdate(updateOid_sql);

                        if(at_type.equals("Touch"))
                        {
                            updateAtid_sql = "UPDATE `exercise`.`attendance`, location SET state = 1, at_FDT = CONCAT(o_date, ' ', o_time) WHERE( location.oid = attendance.oid" +
                                    " and location.`oid`= "+oid+" and  `atid`= "+atid+")";
                            statement.executeUpdate(updateAtid_sql);
                            r = "<font color='#3d3d3d'>You have taken ONE-TOUCH attendance successfully ;-)</font>";
                        }else{
                            updateAtid_sql = "UPDATE `exercise`.`attendance`, location SET at_FDT = CONCAT(o_date, ' ', o_time) WHERE( location.oid = attendance.oid" +
                                    " and location.`oid`= "+oid+" and  `atid`= "+atid+")";
                            statement.executeUpdate(updateAtid_sql);
                            r = "<font color='#3d3d3d'>You have taken TOUCH attendance successfully, plz SUBMIT file ;-)</font>";
                        }
                    }else{
                        r = "<font color='#3d3d3d'>You are not in UIC :-| </font>";
                    }
                }

            } catch (SQLException e) {
                Log.e(TAG, String.valueOf(e));
            }
        }
        return r;
    }

    public static int UserID(Connection conn, String intentName, String inputType) {
        int intentID = 0;
        if (conn != null) {
            String sql = "";
            switch (inputType) {
                case "Student":
                    sql = "select sid from student where stu_name= '" + intentName + "'";
                    break;
                case "Teacher":
                    sql = "select tid from teacher where tea_name= '" + intentName +  "'";
                    break;
                case "Administrator":
                    sql = "select aid from admin,a_member,teacher WHERE "+
                            "(`a_member`.`a_username` = `teacher`.`tea_username` and `teacher`.`tea_name`  ='" + intentName + "')" +
                            "or (`admin`.`ad_username` = `a_member`.`a_username` and `admin`.`a_name` = '" + intentName + "') ";
                    break;
            }


            try {
                // 创建用来执行sql语句的对象
                java.sql.Statement statement = conn.createStatement();
                // 执行sql查询语句并获取查询信息
                ResultSet rs = statement.executeQuery(sql);
                rs.last();
                int check = rs.getRow();
                rs.beforeFirst();

                while (rs.next()) {
                    switch (inputType) {
                        case "Student":
                            intentID = rs.getInt("sid");
                            break;
                        case "Teacher":
                            intentID = rs.getInt("tid");
                            break;
                        case "Administrator":
                            intentID = rs.getInt("aid");
                            break;
                    }
                }
                if (check != 0) {
                    rs.close();
                }

            } catch (SQLException e) {
                Log.e(TAG, String.valueOf(e));
            }
        }
        return intentID;
    }

    public static String RemindStuTakeAT(Connection conn,String cName, int userID){
        String r = "Do not need to remind now!";
        if (conn != null) {
        String check_sql = "SELECT atid FROM `attendance`, `course`, `learn` " +
                "WHERE(`attendance`.`eid`= `learn`.`eid` AND `learn`.`cid`= `course`.`cid` AND " +
                "`learn`.`sid`= "+userID+" AND `course`.`c_name`= '"+cName+
                "' AND (NOW()  - str_to_date(CONCAT(at_date, ' ', at_time), '%Y-%m-%d %H:%i:%s') < 0)" +
                "AND (NOW()  - str_to_date(CONCAT(at_date, ' ', at_time), '%Y-%m-%d %H:%i:%s') > -235959) )";
        try {
            java.sql.Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(check_sql);
            rs.last();
            int check = rs.getRow();
            int i = 0;
            String atid[] = new String[check];
            rs.beforeFirst();
            while (rs.next()) {
                atid[i] = rs.getString("atid");
                i++;
            }
            rs.close();
            SimpleDateFormat format3 = new SimpleDateFormat("HH:mm:ss");
            String timeC = format3.format(Calendar.getInstance().getTime());
            for(i = 0;i<check;i++) {
                String update_sql = "UPDATE message,attendance set m_time = '" + timeC + "', reminder = '" + timeC +
                        "' where (attendance.mid= message.mid and attendance.atid = " + atid[i] + ")";
                statement.executeUpdate(update_sql);
            }
            r = "Take all reminder successfully!";
        } catch (SQLException e) {
            Log.e(TAG, String.valueOf(e));
        }
        };
        return r;
    }

    public static String GetAdminType(Connection conn, int userID) {

        String adminType = "";
        if (conn != null) {
            String sql = "select a_type from a_member WHERE aid = " + userID;
            try {
                java.sql.Statement statement = conn.createStatement();
                ResultSet rs = statement.executeQuery(sql);
                rs.last();
                rs.beforeFirst();
                while (rs.next()) {
                    adminType = rs.getString("a_type");
                }
            } catch (SQLException e) {
                Log.e(TAG, String.valueOf(e));
            }
        }
        return adminType;
    }

    public static String LoginCheck(Connection conn, String inputType, EditText inputUname, EditText inputPwd) {
        String intentName = "";
        if (conn != null) {
            String sql = "";
            switch (inputType) {
                case "Student":
                    sql = "select * from student where stu_username= '" + inputUname.getText().toString() + "' and stu_password = '" + inputPwd.getText().toString() + "'";
                    break;
                case "Teacher":
                    sql = "select * from teacher where tea_username= '" + inputUname.getText().toString() + "' and tea_password = '" + inputPwd.getText().toString() + "'";
                    break;
                case "Administrator":
                    sql = "SELECT a_username, tea_name,  a_name,  a_type FROM a_member, teacher, admin where" +
                            "((a_username= `teacher`.`tea_username` and `teacher`.`tea_type`= 'AdminTeacher'" +
                            "and `teacher`.`tea_username`= '" + inputUname.getText().toString() + "' and `teacher`.`tea_password`= '" + inputPwd.getText().toString() + "') " +
                            "or(a_username= `admin`.`ad_username`" +
                            "and`admin`.`ad_username`= '" + inputUname.getText().toString() + "' and `admin`.`ad_password`= '" + inputPwd.getText().toString() + "'))";
                    break;
            }
            try {
                // 创建用来执行sql语句的对象
                java.sql.Statement statement = conn.createStatement();
                // 执行sql查询语句并获取查询信息
                ResultSet rs = statement.executeQuery(sql);
                rs.last();
                int check = rs.getRow();
                rs.beforeFirst();

                while (rs.next()) {
                    switch (inputType) {
                        case "Student":
                            intentName = rs.getString("stu_name");
                            break;
                        case "Teacher":
                            intentName = rs.getString("tea_name");
                            break;
                        case "Administrator":
                            if (rs.getString("a_type").equals("Administrator"))
                                intentName = rs.getString("a_name");
                            else
                                intentName = rs.getString("tea_name");
                            break;
                    }
                }
                if (check != 0) {
                    rs.close();
                }

            } catch (SQLException e) {
                Log.e(TAG, String.valueOf(e));
            }
        }
        return intentName;
    }

    public static String SetAdminTeacher(Connection conn, EditText tUsername, EditText adPwd) {

        String r ="Your input is incorrect. Please check if input 'Teacher Username' and 'Admin Password' is available";

        if (conn != null) {

            String checkInput = "SELECT count(*) as exist FROM admin,teacher WHERE (tea_type = 'Teacher' " +
                    "and tea_username = '"+tUsername.getText().toString()+"' and ad_password = '"+adPwd.getText().toString()+"')";

            String update_sql = "UPDATE `teacher` SET `teacher`.`tea_type`= 'AdminTeacher' where tea_username = '"+tUsername.getText().toString()+"'";

            String insert_sql = "INSERT INTO `a_member`(`a_username` , `a_type` ) " +
                    "SELECT `tea_username` , `tea_type`  FROM `teacher` WHERE `tea_username` = '"+ tUsername.getText().toString()+"'";

            String check_sql = "SELECT tea_name FROM teacher WHERE tea_username = '"+tUsername.getText().toString()+"'";

            String res = "0";

            try {
                java.sql.Statement statement = conn.createStatement();

                ResultSet rr = statement.executeQuery(checkInput);
                rr.last();
                rr.beforeFirst();

                while (rr.next()) {
                    res = rr.getString("exist");
                }
                rr.close();

                if(!res.equals("0")) {

                    statement.executeUpdate(update_sql);
                    statement.executeUpdate(insert_sql);
                    ResultSet rs = statement.executeQuery(check_sql);
                    rs.last();
                    rs.beforeFirst();

                    while (rs.next()) {
                        String t_name = rs.getString("tea_name");
                        r = "Set teacher " + t_name + " as Admin Teacher now!";
                    }
                    rs.close();
                }
            } catch (SQLException e) {
                Log.e(TAG, String.valueOf(e));
            }
        }
        return r;
    }

    public static String SetAttendance(Connection conn, EditText time, EditText date, EditText remind, EditText content, String atType, String cName) {

        String r ="Your input is incorrect. Please check if all inputs are available";

        if (conn != null) {
            String eid_sql = "SELECT `learn`.`eid` AS e  FROM `course`, `learn` WHERE " +
                    "(`course`.`cid` = `learn`.`cid` and `course`.`c_name` = '"+cName+"')";

            String sendMsg ="Plz take one-touch attendance in "+date.getText().toString()+" "+
                    time.getText().toString();

            if(atType.equals("TouchF"))
            sendMsg ="Plz take touch+file attendance in "+date.getText().toString()+" "+
                    time.getText().toString()+", and "+content.getText().toString();

            String msg_insert_sql = "INSERT INTO `message`( `m_date`, `m_time` , `m_message` )" +
                    " VALUES ('"+date.getText().toString()+"', '"+remind.getText().toString()+"', '"+sendMsg+"')";

            String mid_sql = "SELECT MAX(`mid`) AS m FROM `message`";

            String at_insert_sql = "";

            try {
                java.sql.Statement statement = conn.createStatement();
                statement.executeUpdate(msg_insert_sql);
                java.sql.Statement statemente = conn.createStatement();
                ResultSet rse = statemente.executeQuery(eid_sql);
                rse.last();

                int i = 0;
                int t = rse.getRow();
                String[] eid = new String[t];
                rse.beforeFirst();

                while (rse.next()) {
                    eid[i] = rse.getString("e");
                    i++;
                }
                rse.close();


                java.sql.Statement statementm = conn.createStatement();
                ResultSet rsm = statementm.executeQuery(mid_sql);
                rsm.last();
                String mid = "1";
                rsm.beforeFirst();
                while (rsm.next()) {
                    mid = rsm.getString("m");
                }
                rsm.close();

                for(i = 0;i<t;i++) {
                    at_insert_sql = "INSERT INTO `attendance` " +
                            "(`at_date`,`at_time`,`reminder`,`at_type`,`state`,`eid`,`fid`,`oid`,`mid`,`at_FDT`) VALUES " +
                            "('" + date.getText().toString() + "','" + time.getText().toString() + "','" + remind.getText().toString() + "','" + atType + "',0," + eid[i] + ",1,1," + mid + ",'0001-01-01 00:00:00')";
                    statementm.executeUpdate(at_insert_sql);
                }

                r = "You have set an attendance for students!";

            } catch (SQLException e) {
                Log.e(TAG, String.valueOf(e));
            }
        }
        return r;
    }

    public static String DeleteAdminTeacher(Connection conn, EditText tUsername, EditText adPwd) {

        String r ="Your input is incorrect. Please check if input 'Teacher Username' and 'Admin Password' is available";

        if (conn != null) {

            String checkInput = "SELECT count(*) as exist FROM admin,teacher WHERE (tea_type = 'AdminTeacher' " +
                    "and tea_username = '"+tUsername.getText().toString()+"' and ad_password = '"+adPwd.getText().toString()+"')";

            String update_sql = "UPDATE `teacher` SET `teacher`.`tea_type`= 'Teacher' where tea_username = '"+tUsername.getText().toString()+"'";

            String delete_sql = "delete from `a_member` where a_username = '"+tUsername.getText().toString()+"'";

            String check_sql = "SELECT tea_name FROM teacher WHERE tea_username = '"+tUsername.getText().toString()+"'";

            String res = "0";

            try {
                java.sql.Statement statement = conn.createStatement();

                ResultSet rr = statement.executeQuery(checkInput);
                rr.last();
                rr.beforeFirst();

                while (rr.next()) {
                    res = rr.getString("exist");
                }
                rr.close();

                if(!res.equals("0")) {

                    statement.executeUpdate(update_sql);
                    statement.executeUpdate(delete_sql);
                    ResultSet rs = statement.executeQuery(check_sql);
                    rs.last();
                    rs.beforeFirst();

                    while (rs.next()) {
                        String t_name = rs.getString("tea_name");
                        r = "Authority of admin teacher for " + t_name + " has been revoked!";
                    }
                    rs.close();
                }
            } catch (SQLException e) {
                Log.e(TAG, String.valueOf(e));
            }
        }
        return r;
    }

    public static String CheckAttendance(Connection conn, EditText time, EditText date, String cName) {

        String r = "There is NO any attendance set in this time!";

        if (conn != null) {
            String sql = "SELECT atid FROM `attendance`, `course`, `learn` " +
                    "WHERE(`attendance`.`eid`= `learn`.`eid` AND `learn`.`cid`= `course`.`cid`" +
                    " AND `course`.`c_name`= '"+cName+"' AND `at_time`= '"+time.getText().toString()+
                    "' AND `at_date`= '"+date.getText().toString()+"')";

            try {
                java.sql.Statement statement = conn.createStatement();
                ResultSet rs = statement.executeQuery(sql);
                rs.last();

                //int i = 0;
                int t = rs.getRow();
                //String[] atid = new String[t];
                rs.beforeFirst();

//                while (rs.next()) {
//                    atid[i] = rs.getString("atid");
//                    i++;
//                }
                rs.close();
                if(t!=0)
                    r = "There is an attendance in input time!";

            } catch (SQLException e) {
                Log.e(TAG, String.valueOf(e));
            }
        }
        return r;

    }

    public static String DeleteAttendance(Connection conn, EditText time, EditText date, String cName) {

        String r = "There is NO any attendance set in this time!";

        if (conn != null) {
            String sql = "SELECT atid, mid FROM `attendance`, `course`, `learn` " +
                    "WHERE(`attendance`.`eid`= `learn`.`eid` AND `learn`.`cid`= `course`.`cid`" +
                    " AND `course`.`c_name`= '"+cName+"' AND `at_time`= '"+time.getText().toString()+
                    "' AND `at_date`= '"+date.getText().toString()+"')";

            String dSql = "";
            String mSql = "";

            try {
                java.sql.Statement statement = conn.createStatement();
                ResultSet rs = statement.executeQuery(sql);
                rs.last();

                int i = 0;
                int t = rs.getRow();
                String[] atid = new String[t];
                String mid = "";
                rs.beforeFirst();
                if(t!=0) {
                    r = "You have deleted this attendance!";
                    while (rs.next()) {
                        atid[i] = rs.getString("atid");
                        mid = rs.getString("mid");
                        i++;
                    }
                    for (i = 0; i < t; i++) {
                        dSql = "DELETE FROM `exercise`.`attendance` WHERE `atid`=" + atid[i];
                        statement.executeUpdate(dSql);
                    }
                    mSql = "DELETE FROM `exercise`.`message` WHERE `mid`=" + mid;
                    statement.executeUpdate(mSql);
                }
                rs.close();

            } catch (SQLException e) {
                Log.e(TAG, String.valueOf(e));
            }
        }
        return r;

    }

    public static String UpdateAttendance(Connection conn, EditText time, EditText date, EditText remind, EditText content, String atType, String[] atid) {

        String r ="Your input is incorrect. Please check if all inputs are available";

        if (conn != null) {

            String sendMsg ="[Reminder in "+date.getText().toString()+" "+remind.getText().toString()+
                    "]Plz take one-touch attendance in "+date.getText().toString()+" "+ time.getText().toString();

            if(atType.equals("TouchF"))
                sendMsg ="[Reminder in "+date.getText().toString()+" "+remind.getText().toString()+
                        "]Plz take touch+file attendance in "+date.getText().toString()+" "+time.getText().toString()+
                        ", and "+content.getText().toString();

            String mid_sql = "SELECT mid FROM `attendance` WHERE `atid` =" +atid[1];

            String at_update_sql = "";

            try {
                java.sql.Statement statement = conn.createStatement();

                java.sql.Statement statementm = conn.createStatement();
                ResultSet rsm = statementm.executeQuery(mid_sql);
                rsm.last();
                String mid = "1";
                rsm.beforeFirst();
                while (rsm.next()) {
                    mid = rsm.getString("mid");
                }
                rsm.close();
                String msg_update_sql = "UPDATE `exercise`.`message` SET `m_message`='"+sendMsg+
                        "',`m_date`='"+date.getText().toString()+"',`m_time`='"+time.getText().toString()+
                        "' WHERE `mid`="+mid;
                statement.executeUpdate(msg_update_sql);

                int i;
                for(i = 0;i<atid.length;i++) {
                    at_update_sql = "UPDATE `exercise`.`attendance` SET `at_date`='"+date.getText().toString()+
                            "',`at_time`='"+time.getText().toString()+"',`reminder`='"+remind.getText().toString()+
                            "',`at_type`='"+atType+"',`state`=0,`at_FDT`='0001-01-01 00:00:00',`fid`=1,`oid`=1 WHERE `mid`="+mid;
                    statementm.executeUpdate(at_update_sql);
                }

                r = "Successfully updated!";

            } catch (SQLException e) {
                Log.e(TAG, String.valueOf(e));
            }
        }
        return r;
    }

    public static String UpdateAdmin(Connection conn, EditText username, EditText password) {

        String r ="Your input is incorrect. Please check if input 'Admin Username' and 'Admin Password' is available";

        if (conn != null) {
            String update_sql = "UPDATE `exercise`.`admin` SET `ad_username`='"+username.getText().toString()+
                    "',`ad_password`='"+password.getText().toString()+"' WHERE `adid`=1;";
            String update2_sql = "UPDATE `exercise`.`a_member` SET `a_username`='"+username.getText().toString()+
                   "' WHERE `aid`=1;";
            String check_sql = "SELECT `ad_username` , `ad_password`  FROM `admin` ";
            try {
                java.sql.Statement statement = conn.createStatement();
                statement.executeUpdate(update_sql);
                statement.executeUpdate(update2_sql);
                ResultSet rs = statement.executeQuery(check_sql);
                rs.last();
                rs.beforeFirst();

                while (rs.next()) {
                    String uName = rs.getString("ad_username");
                    String pwd = rs.getString("ad_password");
                    r = "Username: "+uName+"\tPassword: "+pwd+"\n Please login again!";
                }
                rs.close();

            } catch (SQLException e) {
                Log.e(TAG, String.valueOf(e));
            }
        }
        return r;
    }

    public static String[] CourseName(Connection conn, int userID, String inputType) {
        String[] course = new String[8];
        if (conn != null) {
            String sql = "";
            switch (inputType) {
                case "Student":
                    sql = "select c_name from `course`, `learn` where (`learn`.`sid` =" + userID +" and `learn`.`cid` = `course`.`cid` )";
                    break;
                case "Teacher":
                    sql = "select c_name from `course` WHERE `tid` = "+userID;
                    break;
                case "Administrator":
                    sql = "select c_name from `course`";
                    break;
            }

            try {
                // 创建用来执行sql语句的对象
                java.sql.Statement statement = conn.createStatement();
                // 执行sql查询语句并获取查询信息
                ResultSet rs = statement.executeQuery(sql);
                rs.last();
                int check = rs.getRow();
                rs.beforeFirst();

                course = new String[check];
                int i = 0;

                while (rs.next()) {
                    course[i] = rs.getString("c_name");
                    i++;
                }
                if (check != 0) {
                    rs.close();
                }

            } catch (SQLException e) {
                Log.e(TAG, String.valueOf(e));
            }
        }
        return course;
    }

    public static Bundle getLatLonState(Connection conn, String inputType, String name) {
        Bundle bundle = new Bundle();

        if (conn != null) {
            String sql = "";
            switch (inputType) {
                case "Teacher":
                    case "Administrator":
                        sql = "SELECT o_latitude, o_longitude, state, c_name, stu_name, at_date, at_time" +
                                " from location, learn, student, attendance, course " +
                            "where  (attendance.oid = location.oid and attendance.eid = learn.eid" +
                                " and learn.cid = course.cid and student.sid = learn.sid and attendance.state = 1 " +
                            "and stu_username = '"+name+"')";
                    break;
                case  "mid":
                        sql = "SELECT stu_username, stu_name, state, o_latitude, o_longitude, c_name, at_date, at_time" +
                            " FROM  `student`, `attendance`, `learn`, `location`, `course`" +
                            " WHERE ( `attendance`.`oid` = `location`.`oid` and `attendance`.`eid`= `learn`.`eid`" +
                                " and learn.cid = course.cid and `learn`.`sid` = `student`.`sid` and `mid` = "+name+")";
                    break;
            }

            try {
                // 创建用来执行sql语句的对象
                java.sql.Statement statement = conn.createStatement();
                // 执行sql语句
                ResultSet rs = statement.executeQuery(sql);

                rs.last();
                int check = rs.getRow();
                rs.beforeFirst();

                String[] latitude = new String[check];
                String[] longitude = new String[check];
                String[] state  = new String[check];
                String[] cName = new String[check];
                String[] sName = new String[check];
                String[] atDate  = new String[check];
                String[] atTime = new String[check];
                int i=0;

                while (rs.next()) {
                    latitude[i] = rs.getString("o_latitude");
                    longitude[i] = rs.getString("o_longitude");
                    state[i] = rs.getString("state");
                    cName[i] = rs.getString("c_name");
                    sName[i] = rs.getString("stu_name");
                    atDate[i] = rs.getString("at_date");
                    atTime[i] = rs.getString("at_time");
                    i++;
                }
                bundle.putInt("len" ,i);
                bundle.putStringArray("lat" , latitude);
                bundle.putStringArray("lon" , longitude);
                bundle.putStringArray("state" , state);
                bundle.putStringArray("cName" , cName);
                bundle.putStringArray("sName" , sName);
                bundle.putStringArray("atDate" , atDate);
                bundle.putStringArray("atTime" , atTime);
            } catch (SQLException e) {
                Log.e(TAG, String.valueOf(e));
            }
        }
        return bundle;
    }

    public static String[] GetAtid(Connection conn, EditText time, EditText date, String cName) {

        String[] atid = new String[0];

        if (conn != null) {
            String sql = "SELECT atid FROM `attendance`, `course`, `learn` " +
                    "WHERE(`attendance`.`eid`= `learn`.`eid` AND `learn`.`cid`= `course`.`cid`" +
                    " AND `course`.`c_name`= '"+cName+"' AND `at_time`= '"+time.getText().toString()+
                    "' AND `at_date`= '"+date.getText().toString()+"')";

            try {
                java.sql.Statement statement = conn.createStatement();
                ResultSet rs = statement.executeQuery(sql);
                rs.last();

                int i = 0;
                int t = rs.getRow();
                atid = new String[t];
                rs.beforeFirst();

                while (rs.next()) {
                    atid[i] = rs.getString("atid");
                    i++;
                }
                rs.close();

            } catch (SQLException e) {
                Log.e(TAG, String.valueOf(e));
            }
        }
        return atid;

    }

    public static Bundle AdminListData(Connection conn) {
        Bundle bundle = new Bundle();
        if (conn != null) {
            String sql = "SELECT aid, `a_name` , tea_name, a_type FROM `admin` , `teacher`, `a_member`" +
                    "where (`ad_username` = `a_username` or `tea_username` = `a_username`)";
            try {
                // 创建用来执行sql语句的对象
                java.sql.Statement statement = conn.createStatement();
                // 执行sql查询语句并获取查询信息
                ResultSet rs = statement.executeQuery(sql);
                rs.last();
                int i = 0;
                Boolean existA = false;
                int check = rs.getRow();
                String[] aid = new String[check];
                String[] aName = new String[check];
                String[] aType = new String[check];
                rs.beforeFirst();

                while (rs.next()) {
                    if(rs.getString("a_type").equals("AdminTeacher")) {
                        aid[i] = rs.getString("aid");
                        aName[i] = rs.getString("tea_name");
                        aType[i] = rs.getString("a_type");
                        i++;
                    }else {
                        if(!existA){
                            aid[i] = rs.getString("aid");
                            aName[i] = rs.getString("a_name");
                            aType[i] = rs.getString("a_type");
                            i++;
                            existA = true;
                        }
                    }
                }
                bundle.putInt("len" ,i);
                bundle.putStringArray("aid" , aid);
                bundle.putStringArray("aName" , aName);
                bundle.putStringArray("aType",aType);
                if (check != 0) {
                    rs.close();
                }

            } catch (SQLException e) {
                Log.e(TAG, String.valueOf(e));
            }
        }
        return bundle;
    }

    public static Bundle TeacherListData(Connection conn) {
        Bundle bundle = new Bundle();
        if (conn != null) {
            String sql = "SELECT c_name, tea_name from `teacher`, `course` WHERE `teacher`.`tid` = `course`.`tid` ";
            try {
                // 创建用来执行sql语句的对象
                java.sql.Statement statement = conn.createStatement();
                // 执行sql查询语句并获取查询信息
                ResultSet rs = statement.executeQuery(sql);
                rs.last();
                int i = 0;

                int check = rs.getRow();
                String[] tName = new String[check];
                String[] cName = new String[check];
                rs.beforeFirst();

                while (rs.next()) {
                    tName[i] = rs.getString("tea_name");
                    cName[i] = rs.getString("c_name");
                    i++;
                }
                bundle.putInt("len" ,i);
                bundle.putStringArray("tName" , tName);
                bundle.putStringArray("cName",cName);
                if (check != 0) {
                    rs.close();
                }

            } catch (SQLException e) {
                Log.e(TAG, String.valueOf(e));
            }
        }
        return bundle;
    }

    public static Bundle StudentListData(Connection conn, String courseName) {
        Bundle bundle = new Bundle();
        if (conn != null) {
            String sql = "SELECT stu_username, stu_name, COUNT(state) AS total," +
                    "COUNT(CASE WHEN state= 1 THEN state END) AS finished " +
                    "FROM `student`, `attendance`, `learn`, `course` " +
                    "WHERE(`attendance`.`eid`= `learn`.`eid` and `learn`.`sid`= `student`.`sid` " +
                    "and `learn`.`cid` = `course`.`cid` and c_name = '"+courseName+"') GROUP BY student.sid";
            try {
                // 创建用来执行sql语句的对象
                java.sql.Statement statement = conn.createStatement();
                // 执行sql查询语句并获取查询信息
                ResultSet rs = statement.executeQuery(sql);
                rs.last();
                int i = 0;

                int check = rs.getRow();
                String[] sName = new String[check];
                String[] sid = new String[check];
                String[] times = new String[check];
                rs.beforeFirst();

                while (rs.next()) {
                    sid[i] = rs.getString("stu_username");
                    sName[i] = rs.getString("stu_name");
                    times[i] = rs.getString("finished")+"/"+rs.getString("total");
                    i++;
                }
                bundle.putInt("len" ,i);
                bundle.putStringArray("sName" , sName);
                bundle.putStringArray("sid",sid);
                bundle.putStringArray("times" , times);

                if (check != 0) {
                    rs.close();
                }

            } catch (SQLException e) {
                Log.e(TAG, String.valueOf(e));
            }
        }
        return bundle;
    }

    public static Bundle MessageListData(Connection conn, String courseName) {
        Bundle bundle = new Bundle();
        if (conn != null) {
            String sql = "SELECT m_message, m_date, m_time FROM `message`, `attendance`, `course`, `learn` " +
                    "WHERE(`attendance`. `mid` = `message`. `mid` and `learn`. `eid` = `attendance`. `eid` " +
                    "and `learn`. `cid` = `course`. `cid` and  `c_name` = '" + courseName+ "' ) GROUP BY message.mid desc";
            try {
                // 创建用来执行sql语句的对象
                java.sql.Statement statement = conn.createStatement();
                // 执行sql查询语句并获取查询信息
                ResultSet rs = statement.executeQuery(sql);
                rs.last();
                int i = 0;

                int check = rs.getRow();
                String[] time = new String[check];
                String[] date = new String[check];
                String[] message = new String[check];
                rs.beforeFirst();

                while (rs.next()) {
                    time[i] = rs.getString("m_time");
                    date[i] = rs.getString("m_date");
                    message[i] = rs.getString("m_message");
                    i++;
                }
                bundle.putInt("len" ,i);
                bundle.putStringArray("time" , time);
                bundle.putStringArray("date", date);
                bundle.putStringArray("message", message);
                if (check != 0) {
                    rs.close();
                }

            } catch (SQLException e) {
                Log.e(TAG, String.valueOf(e));
            }
        }
        return bundle;
    }

    public static Bundle CheckAtdcListData(Connection conn, int mid) {
        Bundle bundle = new Bundle();
        if (conn != null) {
            String sql = "SELECT stu_username, stu_name, at_FDT, state,  o_latitude, o_longitude" +
                    " FROM  `student`, `attendance`, `learn`, `location`" +
                    " WHERE ( `attendance`.`oid` = `location`.`oid` and `attendance`.`eid`= `learn`.`eid` and `learn`.`sid` = `student`.`sid` and `mid` = "+mid+")";
            try {
                // 创建用来执行sql语句的对象
                java.sql.Statement statement = conn.createStatement();
                // 执行sql查询语句并获取查询信息
                ResultSet rs = statement.executeQuery(sql);
                rs.last();
                int i = 0;

                int check = rs.getRow();
                String[] sName = new String[check];
                String[] sid = new String[check];
                String[] time = new String[check];
                String[] state = new String[check];
                String[] lat = new String[check];
                String[] lon = new String[check];
                rs.beforeFirst();

                while (rs.next()) {
                        sid[i] = rs.getString("stu_username");
                        sName[i] = rs.getString("stu_name");
                        time[i] = rs.getDate("at_FDT").toString() + "\n" + rs.getTime("at_FDT").toString();
                        state[i] = rs.getString("state");
                        lat[i] = rs.getString("o_latitude");
                        lon[i] = rs.getString("o_longitude");
                        if (state[i].equals("0"))
                            state[i] = "Absence";
                        else
                            state[i] = "Presence";
                        i++;
                }
                bundle.putInt("len" ,i);
                bundle.putStringArray("sName" , sName);
                bundle.putStringArray("sid",sid);
                bundle.putStringArray("time" , time);
                bundle.putStringArray("state",state);
                bundle.putStringArray("lat",lat);
                bundle.putStringArray("lon",lon);
                if (check != 0) {
                    rs.close();
                }

            } catch (SQLException e) {
                Log.e(TAG, String.valueOf(e));
            }
        }
        return bundle;
    }

    public static Bundle CheckFileListData(Connection conn, int mid) {
        Bundle bundle = new Bundle();
        if (conn != null) {
            String sql = "SELECT stu_username, stu_name, f_name, attendance.fid as ff  FROM  `student`, `attendance`, `learn`, `file`" +
                    "WHERE (`attendance`.`eid`= `learn`.`eid` and `learn`.`sid` = `student`.`sid`" +
                    "and `attendance`.`fid` = `file`.`fid` and `mid` = "+mid+")";
            try {
                // 创建用来执行sql语句的对象
                java.sql.Statement statement = conn.createStatement();
                // 执行sql查询语句并获取查询信息
                ResultSet rs = statement.executeQuery(sql);
                rs.last();
                int i = 0;

                int check = rs.getRow();
                String[] sName = new String[check];
                String[] sid = new String[check];
                String[] fName = new String[check];
                String[] fid = new String[check];
                rs.beforeFirst();

                while (rs.next()) {
                    sid[i] = rs.getString("stu_username");
                    sName[i] = rs.getString("stu_name");
                    fName[i] = rs.getString("f_name");
                    fid[i] = rs.getString("ff");
                    i++;
                }
                bundle.putInt("len" ,i);
                bundle.putStringArray("sName" , sName);
                bundle.putStringArray("sid",sid);
                bundle.putStringArray("fName" , fName);
                bundle.putStringArray("fid" , fid);
                if (check != 0) {
                    rs.close();
                }

            } catch (SQLException e) {
                Log.e(TAG, String.valueOf(e));
            }
        }
        return bundle;
    }

    public static Bundle StuAtdcListData(Connection conn, String courseName, int userID) {
        Bundle bundle = new Bundle();
        if (conn != null) {
            String sql = "SELECT `at_type`, `f_name`, `attendance`.`fid` as af," +
                    " `state`, `o_date` , `o_time` , `o_longitude` , `o_latitude`, at_date, at_time " +
                    "FROM `attendance`, `learn`, `course`, `file`, `location` " +
                    "WHERE (`attendance`.`eid` = `learn`.`eid` and `attendance`.`fid` = `file`.`fid` and" +
                    "       `attendance`.`oid` = `location`.`oid` and" +
                    "       `attendance`.`state` = 1 and" +
                    "       `learn`.`cid` = `course`.`cid` and" +
                    "       `course`.`c_name` ='" +courseName+"' and `learn`.`sid` = '"+ userID +"')";
            try {
                // 创建用来执行sql语句的对象
                java.sql.Statement statement = conn.createStatement();
                // 执行sql查询语句并获取查询信息
                ResultSet rs = statement.executeQuery(sql);
                rs.last();
                int i = 0;

                int check = rs.getRow();
                String[] time = new String[check];
                String[] date = new String[check];
                String[] atTime = new String[check];
                String[] atDate = new String[check];
                String[] fName = new String[check];
                String[]  atType = new String[check];
                String[] lat = new String[check];
                String[]  lon = new String[check];
                String[] state = new String[check];
                String[] fid = new String[check];
                rs.beforeFirst();

                while (rs.next()) {
                    time[i] = rs.getString("o_time");
                    date[i] = rs.getString("o_date");
                    atTime[i] = rs.getString("at_time");
                    atDate[i] = rs.getString("at_date");
                    lat[i] = rs.getString("o_latitude");
                    lon[i] = rs.getString("o_longitude");
                    fName[i] = rs.getString("f_name");
                    atType[i] = rs.getString("at_type");
                    state[i] = rs.getString("state");
                    fid[i] = rs.getString("af");
                    i++;
                }
                bundle.putInt("len" ,i);
                bundle.putStringArray("time" , time);
                bundle.putStringArray("date", date);
                bundle.putStringArray("atTime" , atTime);
                bundle.putStringArray("atDate", atDate);
                bundle.putStringArray("lat" , lat);
                bundle.putStringArray("lon", lon);
                bundle.putStringArray("fName", fName);
                bundle.putStringArray("atType",atType);
                bundle.putStringArray("state",state);
                bundle.putStringArray("fid",fid);
                if (check != 0) {
                    rs.close();
                }

            } catch (SQLException e) {
                Log.e(TAG, String.valueOf(e));
            }
        }
        return bundle;
    }

    public static Bundle TeaAtdcListData(Connection conn, String courseName) {
        Bundle bundle = new Bundle();
        if (conn != null) {
            String sql = "SELECT CONCAT(at_date, ' ', at_time)  AS titleTime, at_type, at_date, at_time, reminder, COUNT(state) AS total, mid," +
                    "COUNT(CASE WHEN state = 1 THEN state END) AS finished " +
                    "FROM `attendance`, `learn`, `course`, `file`, `location`" +
                    "WHERE (`attendance`.`eid` = `learn`.`eid` and `attendance`.`fid` = `file`.`fid` and" +
                    "       `attendance`.`oid` = `location`.`oid` and" +
                    "       `learn`.`cid` = `course`.`cid` and `course`.`c_name` ='" +courseName+"') group by `attendance`.`mid`";

            try {
                // 创建用来执行sql语句的对象
                java.sql.Statement statement = conn.createStatement();
                // 执行sql查询语句并获取查询信息
                ResultSet rs = statement.executeQuery(sql);
                rs.last();
                int i = 0;

                int check = rs.getRow();
                String[] time = new String[check];
                String[] date = new String[check];
                String[]  atType = new String[check];
                String[] reminder = new String[check];
                String[] total = new String[check];
                String[] finished = new String[check];
                String[] state = new String[check];
                String[] mid = new String[check];
                String[] titleTime = new String[check];
                rs.beforeFirst();

                while (rs.next()) {
                    time[i] = rs.getString("at_time");
                    date[i] = rs.getString("at_date");
                    atType[i] = rs.getString("at_type");
                    reminder[i] = rs.getString("reminder");
                    total[i] = rs.getString("total");
                    finished[i] = rs.getString("finished");
                    state[i] = finished[i]+"/"+total[i];
                    mid[i] = rs.getString("mid");
                    titleTime[i] = rs.getString("titleTime");
                    i++;
                }

                bundle.putInt("len" ,i);
                bundle.putStringArray("time" , time);
                bundle.putStringArray("date", date);
                bundle.putStringArray("atType",atType);
                bundle.putStringArray("reminder", reminder);
                bundle.putStringArray("state", state);
                bundle.putStringArray("mid", mid);
                bundle.putStringArray("titleTime", titleTime);
                if (check != 0) {
                    rs.close();
                }

            } catch (SQLException e) {
                Log.e(TAG, String.valueOf(e));
            }
        }
        return bundle;
    }

    public static boolean CheckTimeInterval(EditText time, EditText remind){
        Time t1 = Time.valueOf(time.getText().toString());
        Time t2 = Time.valueOf(remind.getText().toString());
        Log.e("cti<=24", String.valueOf(((t1.getTime() - t2.getTime())/(3600*1000))));
        Log.e("cti>=0", String.valueOf(((t1.getTime() - t2.getTime())/(60*1000))));
        if(((t1.getTime() - t2.getTime())/(3600*1000)) <= 24 && ((t1.getTime() - t2.getTime())/(60*1000)) >= 0)
            return true;
        else
            return false;
    }

    public static boolean StorePicture(Connection conn, String strFile, int userID, String cName) throws Exception {
        boolean written = false;
        PreparedStatement ps = null;

        if (conn == null || strFile == null)
            written = false;
        else {
            int id = 0;
            File file = new File(strFile);
            //不抛出exception版本
            /*FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }*/
            //抛出exception版本
            FileInputStream fis = null;
            fis = new FileInputStream(file);

            String atid = "";
            String at_type ="";
            try {
                //找到合法范围内的atid：在规定时间段内的15分钟（只有一个）
                String atid_sql ="SELECT atid, at_type FROM `attendance`, `course`, `learn`WHERE(`attendance`.`eid`= `learn`.`eid`" +
                        " AND `learn`.`cid`= `course`.`cid` AND `learn`.`sid`= "+userID+" AND `course`.`c_name`= '"+cName+
                        "' AND(NOW() - str_to_date(CONCAT(at_date, ' ', at_time), '%Y-%m-%d %H:%i:%s') >= 0" +
                        " AND NOW() - str_to_date(CONCAT(at_date, ' ', at_time), '%Y-%m-%d %H:%i:%s') <= 1500))" +
                        " ORDER BY NOW() - str_to_date(CONCAT(at_date, ' ', at_time), '%Y-%m-%d %H:%i:%s') ASC limit 1";
                Log.e("atidsql",atid_sql);
                java.sql.Statement statement = conn.createStatement();
                ResultSet rss = statement.executeQuery(atid_sql);
                rss.last();
                int check = rss.getRow();
                rss.beforeFirst();
                Log.e("check", String.valueOf(check));
                while (rss.next()) {
                    atid = rss.getString("atid");
                    at_type = rss.getString("at_type");
                    Log.e("atid attype",atid+" "+at_type);
                }
                rss.close();
                //只用点击，不用交文件
                if(at_type.equals("Touch") || check==0)
                    written = false;
                else{
                    //要交文件

                    //插入文件
                    ps = conn.prepareStatement("SELECT MAX(fid) FROM file");
                    ResultSet rs = ps.executeQuery();
                    SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat st = new SimpleDateFormat("HH:mm:ss");
                    String currentD = sd.format(System.currentTimeMillis());
                    String currentT = st.format(System.currentTimeMillis());
                    String currentDT = currentD + " " + currentT;

                    if(rs != null) {
                        while(rs.next()) {
                            id = rs.getInt(1)+1;
                        }
                    } else { return written; }
                    rs.close();

                    ps = conn.prepareStatement("insert into file values (?,?,?,?,?)");
                    ps.setInt(1, id);
                    ps.setString(2, file.getName());
                    ps.setString(3,currentD);
                    ps.setString(4,currentT);
                    ps.setBinaryStream(5, fis, (int) file.length());
                    ps.executeUpdate();

                    //插入fid,完成签到时间[当前时间]（at_DFT）,attendance提交状态（state=1）到atid所在attendance
                    String updateFid_sql = "UPDATE `exercise`.`attendance` SET state = 1, at_FDT = '"+currentDT+"', `fid`= "+id+" WHERE `atid`= "+atid;
                    Log.e("udfs",updateFid_sql);
                    statement.executeUpdate(updateFid_sql);
                    written = true;
                }



            } catch (SQLException e) {
                written = false;
                System.out.println("SQLException: "+ e.getMessage());
                System.out.println("SQLState: "+ e.getSQLState());
                System.out.println("VendorError: "+ e.getErrorCode());
                e.printStackTrace();
            }

        }
        return written;
    }

    public static boolean CheckInputDT(EditText time, EditText remind, EditText date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Time t1 = Time.valueOf(time.getText().toString());
        Time t2 = Time.valueOf(remind.getText().toString());
        String currentDT = sdf.format(System.currentTimeMillis());
        String inputDT = date.getText().toString()+" "+remind.getText().toString();
        Date d1 = sdf.parse(currentDT);
        Date d2 = sdf.parse(inputDT);
        Log.e("cidt<=24", String.valueOf(((t1.getTime() - t2.getTime())/(3600*1000))));
        Log.e("cidt>=0", String.valueOf(((t1.getTime() - t2.getTime())/(60*1000))));
        Log.e("cidt>15", String.valueOf((d2.getTime() - d1.getTime())/(60*1000) > 15));
        if(((t1.getTime() - t2.getTime())/(3600*1000)) <= 24 &&
                ((t1.getTime() - t2.getTime())/(60*1000)) >= 0 &&
                (d2.getTime() - d1.getTime())/(60*1000) > 15)
            return true;
        return false;
    }

}
