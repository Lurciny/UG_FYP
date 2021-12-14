package com.example.test.ui.check_map;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.example.test.R;
import com.example.test.service.MyDatabaseHelper;
import com.mysql.jdbc.Connection;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

//监听定位和定位变化
public class CheckMapActivity extends AppCompatActivity implements LocationSource, AMapLocationListener {

    private static final String TAG = "mapNow-latitude";
    //显示地图需要的变量
    private MapView mapView;//地图控件
    private AMap aMap;//地图对象

    //定位需要的声明
    private AMapLocation privLocation;
    private AMapLocationClient mLocationClient = null;//定位发起端
    private AMapLocationClientOption mLocationOption = null;//定位参数
    private OnLocationChangedListener mListener = null;//定位监听器

    //标识，用于判断是否只显示一次定位信息和用户重新定位
    private boolean isFirstLoc = true;
    double[] latitude = new double[2];
    double[] longitude = new double[2];
    String[] state = new String[2];
    int i;
    private double distance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.check_map);

        //显示地图
        mapView = (MapView) findViewById(R.id.map);
        //必须要写
        mapView.onCreate(savedInstanceState);
        //获取地图对象
        aMap = mapView.getMap();



        //119 google瓦片覆盖实现卫星地图标记
//        TileOverlay googleTileOverlay;
//        final String url = "https://mt3.google.cn/maps/vt?lyrs=y@194&hl=en-US&gl=cn&x=%d&y=%d&z=%d";
//        TileOverlayOptions tileOverlayOptions = new TileOverlayOptions().tileProvider(new UrlTileProvider(256, 256) {
//            @Override
//            public URL getTileUrl(int x, int y, int zoom) {
//                try {
//                    return new URL(String.format(url, x, y, zoom));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                return null;
//            }
//        });
//        tileOverlayOptions.diskCacheEnabled(true)
//                .diskCacheDir("/storage/emulated/0/amap/OMCcache")
//                .diskCacheSize(100000)
//                .memoryCacheEnabled(true)
//                .memCacheSize(100000)
//                .zIndex(-9999);
//        googleTileOverlay = aMap.addTileOverlay(tileOverlayOptions);

        //设置显示定位按钮 并且可以点击
        UiSettings settings = aMap.getUiSettings();
        //设置定位监听
        aMap.setLocationSource(this);
        // 是否显示定位按钮
        settings.setMyLocationButtonEnabled(true);
        // 是否可触发定位并显示定位层
        aMap.setMyLocationEnabled(true);
        // 119设置卫星地图
        aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
        //地图改成英文版本的
        //aMap.setMapLanguage(AMap.ENGLISH);

        //119比例尺控件
        settings.setScaleControlsEnabled(true);
        settings.setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_LEFT);


        //定位的小图标 默认是蓝点 这里自定义
        MyLocationStyle myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式
        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        // 设置边框的颜色
        myLocationStyle.strokeColor(Color.TRANSPARENT);
        // 设置边框的填充色
        myLocationStyle.radiusFillColor(Color.TRANSPARENT);
        // 设置定位点图片
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.location)));
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        //aMap.getUiSettings().setMyLocationButtonEnabled(true);设置默认定位按钮是否显示，非必需设置。
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        // 因为模式问题，所以要手动调用

        //开始定位
        initLoc();
    }

    //定位
    private void initLoc() {
        //119持续定位
        //在activity中启动自定义本地服务LocationService
        getApplicationContext().startService(new Intent(this, CheckMapActivity.class));

        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(this);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //出行模式
        mLocationOption.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.Transport);
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //测试：设置定位语言为英文
        mLocationOption.setGeoLanguage(AMapLocationClientOption.GeoLanguage.EN);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);

        //设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }

    //定位回调函数
    @Override
    public void onLocationChanged(final AMapLocation amapLocation) {

        final String type = getIntent().getStringExtra("type");
        final String name = getIntent().getStringExtra("name");
        final String cName = getIntent().getStringExtra("cName");
        final String[] lat = getIntent().getStringArrayExtra("lat");
        final String[] lon = getIntent().getStringArrayExtra("lon");
        final String[] st = getIntent().getStringArrayExtra("state");
        final String[] atTime = getIntent().getStringArrayExtra("atTime");
        final String[] atDate = getIntent().getStringArrayExtra("atDate");
        final String[] sNameTo = new String[atTime.length];
        final String[] cNameTo = new String[atTime.length];
        for(i = 0;i<atTime.length;i++){
            sNameTo[i] = name;
            cNameTo[i] = cName;
        }

        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消息
                amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见官方定位类型表
                amapLocation.getLatitude();//获取纬度
                amapLocation.getLongitude();//获取经度
                amapLocation.getAccuracy();//获取精度信息
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(amapLocation.getTime());
                df.format(date);//定位时间
                amapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                amapLocation.getCountry();//国家信息
                amapLocation.getProvince();//省信息
                amapLocation.getCity();//城市信息
                amapLocation.getDistrict();//城区信息
                amapLocation.getStreet();//街道信息
                amapLocation.getStreetNum();//街道门牌号信息
                amapLocation.getCityCode();//城市编码
                amapLocation.getAdCode();//地区编码
                amapLocation.getAoiName();//获取当前定位点的AOI信息
                amapLocation.getBearing();//获取方向角信息
                amapLocation.getSpeed();//获取速度信息  单位：米/秒
                amapLocation.getLocationType();//查看是什么类型的点

                // 119test
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude()));
                markerOptions.visible(true);
                markerOptions.period(60);
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.location));
                drawLines(amapLocation);//一边定位一边连线
                distance += distance;
                //点击定位按钮 能够将地图的中心移动到定位点
                BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.transparent));
                markerOptions.icon(bitmapDescriptor);
                aMap.addMarker(markerOptions);
                mListener.onLocationChanged(amapLocation);


                {
                    //Handler处理机制 是用来处理消息的
                    final Handler handler = new Handler() {
                        //Handler里的handMessage方法
                        @Override
                        public void handleMessage(Message msg) {
                            String latT[] = msg.getData().getStringArray("lat");
                            String lonT[] = msg.getData().getStringArray("lon");
                            String stateT[] = msg.getData().getStringArray("state");
                            final String cNameT[] = msg.getData().getStringArray("cName");
                            final String sNameT[] = msg.getData().getStringArray("sName");
                            final String atDateT[] = msg.getData().getStringArray("atDate");
                            final String atTimeT[] = msg.getData().getStringArray("atTime");

                           //非学生都是自己get值的, 学生都是传值过来的
                            // 学生的值一般要自己mark了才对，所以不用改..吧
                            if(latT!=null){
                                latitude = String2Double(latT);
                                longitude = String2Double(lonT);
                                state = stateT;
                                aMap.clear(true);

                                //添加历史图钉
                                try{
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    for(i=0; i<latitude.length; i++) {
                                        Log.i("test",sNameT[i]+"\n"+cNameT[i]+": "+atDateT[i]+" "+atTimeT[i]);
                                        String currentDT = sdf.format(System.currentTimeMillis());
                                        String inputDT = atDateT[i] + " " + atTimeT[i];
                                        Date d1 = sdf.parse(currentDT);
                                        Date d2 = sdf.parse(inputDT);

                                        //如果是在CheckAttendance2后面的话
                                        //需要判断老师的定位是否在学生附近
                                        //以及是否在签到时间内
                                        if ((d1.getTime() - d2.getTime()) / (60 * 1000) <= 15
                                                && (d1.getTime() - d2.getTime()) / (60 * 1000) >=0
                                                && type.equals("mid")) {
                                            distance = AMapUtils.calculateLineDistance(new LatLng(latitude[i],
                                                    longitude[i]), new LatLng(amapLocation.getLatitude(),
                                                    amapLocation.getLongitude()));
                                            if(distance<25) {
                                                aMap.addMarker(getMarkerOptions1(latitude[i], longitude[i]))
                                                        .setTitle(sNameT[i]+"\n"+cNameT[i]+": "+atDateT[i]+" "+atTimeT[i]);
                                                Log.e("lat1", String.valueOf(latitude[i]));
                                            }else if(latitude[i]!=0){
                                                Log.e("lat2", String.valueOf(latitude[i]));
                                                aMap.addMarker(getMarkerOptions0(latitude[i], longitude[i]))
                                                        .setTitle(sNameT[i]+"\n"+cNameT[i]+": "+atDateT[i]+" "+atTimeT[i]);
                                            }
                                        }
                                        //当前一个页面是
                                        // 显示学生的历史签到记录 和 学生查看自己的签到记录 时
                                        //无需判断老师定位和是否在签到时间内
                                        else{
                                                if(state[i].equals("1")) {
                                                    aMap.addMarker(getMarkerOptions1(latitude[i], longitude[i]))
                                                            .setTitle(sNameT[i]+"\n"+cNameT[i]+": "+atDateT[i]+" "+atTimeT[i]);
                                                }else{
                                                    aMap.addMarker(getMarkerOptions0(latitude[i], longitude[i]))
                                                            .setTitle(sNameT[i]+"\n"+cNameT[i]+": "+atDateT[i]+" "+atTimeT[i]);
                                                }
                                        }
                                    }
                                }catch (ParseException e) {
                                    Log.e(TAG, String.valueOf(e));
                                }
                            }
                            super.handleMessage(msg);
                        }
                    };


                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Connection conn = MyDatabaseHelper.CreateConnection();
                                while(conn != null){
                                    try {
                                        Thread.sleep(2000);
                                        Message message = Message.obtain();
                                        Bundle bundle = new Bundle();
                                        if(type.equals("Student")){
                                            bundle.putStringArray("lat",lat);
                                            bundle.putStringArray("lon", lon);
                                            bundle.putStringArray("state", st);
                                            bundle.putStringArray("atTime",atTime);
                                            bundle.putStringArray("atDate", atDate);
                                            bundle.putStringArray("sName",sNameTo);
                                            bundle.putStringArray("cName", cNameTo);
                                        }else {
                                            bundle = MyDatabaseHelper.getLatLonState(conn, type, name);
                                        }
                                        message.setData(bundle);
                                        handler.sendMessage(message);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                MyDatabaseHelper.CloseConnection(conn);
                            }
                        });
                        thread.start();
                }

                // 如果不设置标志位，此时再拖动地图时，它会不断将地图移动到当前的位置
                if (isFirstLoc) {
                    //设置缩放级别
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(300));
                    //将地图移动到定位点
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude()), 19));

                    //获取定位信息
                    StringBuffer buffer = new StringBuffer();
                    buffer.append(amapLocation.getPoiName());
                    Toast.makeText(getApplicationContext(), buffer.toString(), Toast.LENGTH_LONG).show();
                    isFirstLoc = false;
                }

                //获取定位时间
                Date dateC = new Date(amapLocation.getTime());
                df.format(dateC);
                privLocation = amapLocation;

            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + amapLocation.getErrorCode() + ", errInfo:"
                        + amapLocation.getErrorInfo());

                Toast.makeText(getApplicationContext(), "Fail to locate", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * 绘制运动路线
     *
     * @param curLocation
     */
    public void drawLines(AMapLocation curLocation) {

        if (null == privLocation) {
            return;
        }
        PolylineOptions options = new PolylineOptions();
        //上一个点的经纬度
        options.add(new LatLng(privLocation.getLatitude(), privLocation.getLongitude()));
        //当前的经纬度
        options.add(new LatLng(curLocation.getLatitude(), curLocation.getLongitude()));
        options.width(10).geodesic(true).color(Color.parseColor("#8B3A3A"));
        aMap.addPolyline(options);
        //距离的计算
        distance = AMapUtils.calculateLineDistance(new LatLng(privLocation.getLatitude(),
                privLocation.getLongitude()), new LatLng(curLocation.getLatitude(),
                curLocation.getLongitude()));
        //Log.e("DDDDDDDDD", String.valueOf(distance));
    }


    //测试：自定义一个历史定位图钉，并且设置图标
    private MarkerOptions getMarkerOptions0(double lat, double lon) {
        //设置图钉选项
        MarkerOptions options = new MarkerOptions();
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.present));
        options.position(new LatLng(lat,lon));
        options.period(60);
        return options;
    }

    //测试：自定义一个历史定位图钉，并且设置图标
    private MarkerOptions getMarkerOptions1(double lat, double lon) {
        //设置图钉选项
        MarkerOptions options = new MarkerOptions();
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.absent));
        options.position(new LatLng(lat,lon));
        options.period(60);
        return options;
    }

    private double[] String2Double(String[] s) {
        int n;
        double[] d = new double[s.length];
        for (n = 0; n < s.length; n++) {
            d[n] = Double.valueOf(s[n]);
        }
        return d;
    }

        //激活定位
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;

    }

    //停止定位
    @Override
    public void deactivate() {
        mListener = null;
    }


    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLocationClient.stopLocation();//停止定位
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

}
