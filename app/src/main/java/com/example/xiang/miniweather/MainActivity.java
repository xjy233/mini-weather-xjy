package com.example.xiang.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.xiang.bean.TodayWeather;
import com.example.xiang.bean.frist2days;
import com.example.xiang.bean.second2days;
import com.example.xiang.util.NetUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener,ViewPager.OnPageChangeListener{
    private static final int UPDATE_TODAY_WEATHER = 1;
    private static final int UPDATE_Visibility_ProgressBar = 4;
    private static final int UPDATE_Visibility_UpdateBtn = 3;
    private ImageView mUpdateBtn;
    private ImageView mlocaltion;private String mlocal_adrr;
    private ImageView mCitySelect;

    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQualityTv, temperatureTv, climateTv, windTv, city_name_Tv;
    private TextView temperatureTv_1, climateTv_1, windTv_1,weekTv_1;
    private TextView temperatureTv_2, climateTv_2, windTv_2,weekTv_2;
    private TextView temperatureTv_3, climateTv_3, windTv_3,weekTv_3;
    private TextView temperatureTv_4, climateTv_4, windTv_4,weekTv_4;
    private ImageView weatherImg, pmImg,weatherImg_1,weatherImg_2,weatherImg_3,weatherImg_4;

    ArrayList weather_views;ArrayList climates;

    private ProgressBar mprogressBar;

    private String CodeCity="101010100",CityName = "";   //想办法通过SharedPreferences存储起来

    //BDAbstractLocationListener为7.2版本新增的Abstract类型的监听接口
    //原有BDLocationListener接口暂时同步保留。具体介绍请参考后文中的说明
    public LocationClient mLocationClient = null;
    private MyLocationListener myListener = new MyLocationListener();



    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg){
            int isvisibility;
            List list = new ArrayList();
            switch (msg.what){   //当每一个case都不存在break时，匹配成功后，从当前case开始，依次返回后续所有case的返回值。
                case UPDATE_TODAY_WEATHER:
                    list = (ArrayList)msg.obj;
                    updateTodayWeather((ArrayList) list);
                    break;
                case UPDATE_Visibility_ProgressBar:
                    isvisibility = (int)msg.obj;
                    if(isvisibility == 1){
                        mprogressBar.setVisibility(View.VISIBLE);
                    }else{
                        mprogressBar.setVisibility(View.INVISIBLE);
                    }
                    break;
                case UPDATE_Visibility_UpdateBtn:
                    isvisibility = (int)msg.obj;
                    if(isvisibility == 1){
                        mUpdateBtn.setVisibility(View.VISIBLE);
                    }else{
                        mUpdateBtn.setVisibility(View.INVISIBLE);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    void initView(){
        city_name_Tv = (TextView) findViewById(R.id.title_city_name);
        cityTv = (TextView) findViewById(R.id.city);
        timeTv = (TextView) findViewById(R.id.time);
        humidityTv = (TextView) findViewById(R.id.humidity);
        weekTv = (TextView) findViewById(R.id.week_today);
        pmDataTv = (TextView) findViewById(R.id.pm_data);
        pmQualityTv = (TextView) findViewById(R.id.pm2_5_quality );
        pmImg = (ImageView) findViewById(R.id.pm2_5_img);
        temperatureTv = (TextView) findViewById(R.id.temperature );
        climateTv = (TextView) findViewById(R.id.climate);
        windTv = (TextView) findViewById(R.id.wind);
        weatherImg = (ImageView) findViewById(R.id.weather_img);
        mprogressBar = (ProgressBar)findViewById(R.id.progressBar);
        mUpdateBtn = (ImageView)findViewById(R.id.title_update_btn);
        mlocaltion = (ImageView)findViewById(R.id.title_location);

        weatherImg_1 = (ImageView)views.get(0).findViewById(R.id.tianqi_image_1);
        weatherImg_2 = (ImageView)views.get(0).findViewById(R.id.tianqi_image_2);
        weatherImg_3 = (ImageView)views.get(1).findViewById(R.id.tianqi_image_3);
        weatherImg_4 = (ImageView)views.get(1).findViewById(R.id.tianqi_image_4);

        temperatureTv_1 = (TextView)views.get(0).findViewById(R.id.temperature_1);climateTv_1 = (TextView)views.get(0).findViewById(R.id.weather_1);
        windTv_1 = (TextView)views.get(0).findViewById(R.id.wind_1);weekTv_1 = (TextView)views.get(0).findViewById(R.id.xinqi_1);
        temperatureTv_2 = (TextView)views.get(0).findViewById(R.id.temperature_2);climateTv_2 = (TextView)views.get(0).findViewById(R.id.weather_2);
        windTv_2 = (TextView)views.get(0).findViewById(R.id.wind_2);weekTv_2 = (TextView)views.get(0).findViewById(R.id.xinqi_2);
        temperatureTv_3 = (TextView)views.get(1).findViewById(R.id.temperature_3);climateTv_3 = (TextView)views.get(1).findViewById(R.id.weather_3);
        windTv_3 = (TextView)views.get(1).findViewById(R.id.wind_3);weekTv_3 = (TextView)views.get(1).findViewById(R.id.xinqi_3);
        temperatureTv_4 = (TextView)views.get(1).findViewById(R.id.temperature_4);climateTv_4 = (TextView)views.get(1).findViewById(R.id.weather_4);
        windTv_4 = (TextView)views.get(1).findViewById(R.id.wind_4);weekTv_4 = (TextView)views.get(1).findViewById(R.id.xinqi_4);

        weather_views = new ArrayList<ImageView>();
        weather_views.add(weatherImg);
        weather_views.add(weatherImg_1);
        weather_views.add(weatherImg_2);
        weather_views.add(weatherImg_3);
        weather_views.add(weatherImg_4);

        SharedPreferences sharedPreferences = getSharedPreferences("recently_info", MODE_PRIVATE);
        //getString()第二个参数为缺省值，如果preference中不存在该key，将返回缺省值
        CodeCity = sharedPreferences.getString("CodeCity", "101010100");
        CityName = sharedPreferences.getString("CityName", "北京");
        Log.d("myWeather","CodeCity:"+CodeCity);

        queryWeatherCode(CodeCity);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_info);

        mUpdateBtn = (ImageView)findViewById(R.id.title_update_btn);
        mUpdateBtn.setOnClickListener(this);

        if(NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE){
            Log.d("myWeather","网络ok");
            Toast.makeText(MainActivity.this, "网络OK", Toast.LENGTH_SHORT).show();
        }else {
            Log.d("myWeather","网络挂了");
            Toast.makeText(MainActivity.this, "网络挂了", Toast.LENGTH_SHORT).show();
        }

        mCitySelect = (ImageView) findViewById(R.id.title_city_manager);
        mCitySelect.setOnClickListener(this);

        initViews_pagerView();
        initView();
        mlocaltion.setOnClickListener(this);
        //声明LocationClient类
        mLocationClient = new LocationClient(getApplicationContext());
        //注册监听函数
        mLocationClient.registerLocationListener(myListener);

        configurat_parameter();

        //发起定位
        mLocationClient.start();



    }

    //输入：butn_name：（ProgressBar或者UpdateBtn），isVisibility：//1可见，0不可见
    private void hidden_image(final String butn_name,final int isVisibility){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Message msg =new Message();
                    if (butn_name.equals("ProgressBar")){
                        msg.what = UPDATE_Visibility_ProgressBar;
                        if(isVisibility == 1){
                            msg.obj=1;
                        }else{
                            msg.obj=0;
                        }
                    }else if (butn_name.equals("UpdateBtn")){
                        msg.what = UPDATE_Visibility_UpdateBtn;
                        if(isVisibility == 1){
                            msg.obj=1;
                        }else{
                            msg.obj=0;
                        }
                    }

                    mHandler.sendMessage(msg);
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void queryWeatherCode(String cityCode){
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey="+cityCode;
        Log.d("myWeather",address);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con = null;
                TodayWeather todayWeather = null;
                List list = new ArrayList();
                try{
//                    mprogressBar.setVisibility(View.VISIBLE);
//                    mUpdateBtn.setVisibility(View.INVISIBLE);
                    hidden_image("ProgressBar",1);
                    hidden_image("UpdateBtn",0);
                    Thread.sleep(2000);  //线程睡眠2S，主要是为了显示动画
                    URL url = new URL(address);
                    con = (HttpURLConnection)url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    InputStream in = con.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String str;
                    while((str=reader.readLine()) != null){
                        response.append(str);
                        Log.d("myWeather",str);
                    }
                    String responseStr = response.toString();
                    Log.d("myWeather",responseStr);
                    list = parseXML(responseStr);
                    if (list != null){
                        Log.d("myWeather",list.toString());

                        Message msg =new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj=list;
                        mHandler.sendMessage(msg);
                    }
//                    mprogressBar.setVisibility(View.INVISIBLE);  //因为在子线程中所以ui操作并不会执行，应该通过下面调用消息机制
//                    mUpdateBtn.setVisibility(View.VISIBLE);
                    hidden_image("ProgressBar",0);
                    hidden_image("UpdateBtn",1);
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(con != null){
                        con.disconnect();
                    }
                }
            }
        }).start();
    }

    private ArrayList parseXML(String xmldate){
        TodayWeather todayWeather = null;

        //新增以后四天的天气信息
        frist2days frist2days = null;
        second2days second2days = null;

        List list = new ArrayList();

        int fengxiangCount=0;
        int fengliCount =0;
        int dateCount=0;
        int highCount =0;
        int lowCount=0;
        int typeCount =0;
        try {
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldate));
            int eventType = xmlPullParser.getEventType();
            Log.d("myWeather","parseXML");
            Log.d("eventType",xmlPullParser.toString());
            while (eventType != XmlPullParser.END_DOCUMENT){
                switch (eventType){
                    //判断当前事件是否为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    //判断当前事件是否为标签元素开始事件
                    case XmlPullParser.START_TAG:
                        if (xmlPullParser.getName().equals("resp")){
                            todayWeather = new TodayWeather();
                            frist2days = new frist2days();
                            second2days = new second2days();
                        }
                        if (todayWeather != null){
                            if (xmlPullParser.getName().equals("city")){
                                eventType = xmlPullParser.next();
                                todayWeather.setCity(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("updatetime")){
                                eventType = xmlPullParser.next();
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("shidu")){
                                eventType = xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("wendu")){
                                eventType = xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("pm25")){
                                eventType = xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("quality")){
                                eventType = xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0){
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                fengxiangCount++;
                            }else if(xmlPullParser.getName().equals("fengli")){
                                eventType = xmlPullParser.next();
                                if(fengliCount == 0){
                                    todayWeather.setFengli(xmlPullParser.getText());
                                }else if(fengliCount == 1){
                                    frist2days.setFengli_1(xmlPullParser.getText());
                                }else if(fengliCount == 2){
                                    frist2days.setFengli_2(xmlPullParser.getText());
                                }else if(fengliCount == 3){
                                    second2days.setFengli_3(xmlPullParser.getText());
                                }else if(fengliCount == 4){
                                    second2days.setFengli_4(xmlPullParser.getText());
                                }
                                fengliCount++;
                            }else if(xmlPullParser.getName().equals("date")){
                                eventType = xmlPullParser.next();
                                if(dateCount == 0){
                                    todayWeather.setDate(xmlPullParser.getText());
                                }else if(dateCount == 1){
                                    frist2days.setWeek_1(xmlPullParser.getText());
                                }else if(dateCount == 2){
                                    frist2days.setWeek_2(xmlPullParser.getText());
                                }else if(dateCount == 3){
                                    second2days.setWeek_3(xmlPullParser.getText());
                                }else if(dateCount == 4){
                                    second2days.setWeek_4(xmlPullParser.getText());
                                }
                                dateCount++;
                            }else if(xmlPullParser.getName().equals("high")){
                                eventType = xmlPullParser.next();
                                if(highCount == 0){
                                    todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());
                                }else if(highCount == 1){
                                    frist2days.setHigh_1(xmlPullParser.getText().substring(2).trim());
                                }else if(highCount == 2){
                                    frist2days.setHigh_2(xmlPullParser.getText().substring(2).trim());
                                }else if(highCount == 3){
                                    second2days.setHigh_3(xmlPullParser.getText().substring(2).trim());
                                }else if(highCount == 4){
                                    second2days.setHigh_4(xmlPullParser.getText().substring(2).trim());
                                }
                                highCount++;
                            }else if(xmlPullParser.getName().equals("low")){
                                eventType = xmlPullParser.next();
                                if(lowCount == 0){
                                    todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                                }else if(lowCount == 1){
                                    frist2days.setLow_1(xmlPullParser.getText().substring(2).trim());
                                }else if(lowCount == 2){
                                    frist2days.setLow_2(xmlPullParser.getText().substring(2).trim());
                                }else if(lowCount == 3){
                                    second2days.setLow_3(xmlPullParser.getText().substring(2).trim());
                                }else if(lowCount == 4){
                                    second2days.setLow_4(xmlPullParser.getText().substring(2).trim());
                                }
                                lowCount++;
                            }else if(xmlPullParser.getName().equals("type")){
                                eventType = xmlPullParser.next();
                                if(typeCount == 0){
                                    todayWeather.setType(xmlPullParser.getText());
                                }else if(typeCount == 1){
                                    frist2days.setWeather_1(xmlPullParser.getText());
                                }else if(typeCount == 2){
                                    frist2days.setWeather_2(xmlPullParser.getText());
                                }else if(typeCount == 3){
                                    second2days.setWeather_3(xmlPullParser.getText());
                                }else if(typeCount == 4){
                                    second2days.setWeather_4(xmlPullParser.getText());
                                }
                                typeCount++;
                            }
                        }
                        break;
                    //判断当前事件是否为标签元素结束事件
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType = xmlPullParser.next();
            }
        }catch (XmlPullParserException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        list.add(todayWeather);
        list.add(frist2days);
        list.add(second2days);
        return (ArrayList) list;
    }

    void updateTodayWeather(ArrayList List){
        TodayWeather todayWeather = (TodayWeather)List.get(0);
        frist2days frist2days = (frist2days)List.get(1);
        second2days second2days = (second2days)List.get(2);
        city_name_Tv.setText(todayWeather.getCity()+"天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime()+ "发布");
        humidityTv.setText("湿度："+todayWeather.getShidu());
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getLow()+"~"+todayWeather.getHigh());
        climateTv.setText(todayWeather.getType());
        windTv.setText("风力:"+todayWeather.getFengli());

        /************下面是更新后四天天气************/
        weekTv_1.setText(frist2days.getWeek_1());temperatureTv_1.setText(frist2days.getLow_1()+"~"+frist2days.getHigh_1());
        climateTv_1.setText(frist2days.getWeather_1());windTv_1.setText("风力:"+frist2days.getFengli_1());
        weekTv_2.setText(frist2days.getWeek_2());temperatureTv_2.setText(frist2days.getLow_2()+"~"+frist2days.getHigh_2());
        climateTv_2.setText(frist2days.getWeather_2());windTv_2.setText("风力:"+frist2days.getFengli_2());
        weekTv_3.setText(second2days.getWeek_3());temperatureTv_3.setText(second2days.getLow_3()+"~"+second2days.getHigh_3());
        climateTv_3.setText(second2days.getWeather_3());windTv_3.setText("风力:"+second2days.getFengli_3());
        weekTv_4.setText(second2days.getWeek_4());temperatureTv_4.setText(second2days.getLow_4()+"~"+second2days.getHigh_4());
        climateTv_4.setText(second2days.getWeather_4());windTv_4.setText("风力:"+second2days.getFengli_4());

        climates = new ArrayList<String>();
        climates.add(todayWeather.getType());
        climates.add(frist2days.getWeather_1());
        climates.add(frist2days.getWeather_2());
        climates.add(second2days.getWeather_3());
        climates.add(second2days.getWeather_4());

        /************下面是更新所有天气图标************/
        String str;
        for(int i=0;i<5;i++){
            str = (String)climates.get(i);
            if(str.contains("晴")){
                ((ImageView)weather_views.get(i)).setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.biz_plugin_weather_qing));
            }else if(str.contains("云") || str.contains("阴")){
                ((ImageView)weather_views.get(i)).setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.biz_plugin_weather_duoyun));
            }else {
                ((ImageView)weather_views.get(i)).setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.biz_plugin_weather_xiaoyu));
            }
        }

        Toast.makeText(MainActivity.this,"更新成功！",Toast.LENGTH_SHORT).show();
//        update_btn();
    }

    //更新前两日的信息
    void updateFrist2daysWeather(frist2days frist2days){

    }

    //更新后两日的信息
    void updateSecond2daysWeather(frist2days frist2days){

    }

    private void parseXMLtest(String xmldate){
        try{
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldate));
            int eventType = xmlPullParser.getEventType();
            Log.d("myWeather","parseXML");
            while(eventType != XmlPullParser.END_DOCUMENT){
                switch (eventType){
                    //判断当前事件是否为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    //判断当前事件是否为标签元素开始事件
                    case XmlPullParser.START_TAG:
                        if (xmlPullParser.getName().equals("city")){
                            eventType = xmlPullParser.next();
                            Log.d("myWeather","city:   "+xmlPullParser.getText());
                        }else if (xmlPullParser.getName().equals("updatetime")){
                            eventType = xmlPullParser.next();
                            Log.d("myWeather","updatetime:   "+xmlPullParser.getText());
                        }
                        break;
                    //判断当前事件是否为标签元素结束事件
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType = xmlPullParser.next();
            }
        }catch (XmlPullParserException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void onClick(View view){

        if(view.getId() == R.id.title_city_manager){
            Intent i = new Intent(this, SelectCity.class);
            i.putExtra("CityName",CityName);
            //startActivity(i);
            startActivityForResult(i,1);
        }

        if(view.getId() == R.id.title_location){
            Toast.makeText(MainActivity.this,"您目前的位置："+ mlocal_adrr, Toast.LENGTH_SHORT).show();
        }

        if(view.getId() == R.id.title_update_btn){
            SharedPreferences sharedPreferences = getSharedPreferences("config",MODE_PRIVATE);
            String cityCode = sharedPreferences.getString("main_city_code",CodeCity);
            Log.d("myWeather",cityCode);
            //点击更新按钮之后原更新按钮不可见，进度条可见，注意下面两种方法是错误的，如果是用直接用前一二行会直接执行，因为时间太短了，看不到动画效果，而三四句会造成开的
            //线程过多，5个线程在一起会造成错乱，谁先谁后随机，所以应该把它们全放在queryWeatherCode线程里保持同步
//            mprogressBar.setVisibility(View.VISIBLE);
//            mUpdateBtn.setVisibility(View.INVISIBLE);
//            hidden_image("ProgressBar",1);
//            hidden_image("UpdateBtn",0);
//            update_pregross();

//            SystemClock.sleep(5000);  //整个系统睡眠，这里是错误的，应该放在线程里，使线程睡眠

            if(NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE){
                Log.d("myWeather","网络ok");
                queryWeatherCode(cityCode);
//                mprogressBar.clearAnimation();
//                mprogressBar.setVisibility(View.INVISIBLE);
//                mUpdateBtn.setVisibility(View.VISIBLE);
//                hidden_image("ProgressBar",0);
//                hidden_image("UpdateBtn",1);
            }else {
                Log.d("myWeather","网络挂了");
                Toast.makeText(MainActivity.this, "网络挂了", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String newCityCode= data.getStringExtra("cityCode");
            String newCityName= data.getStringExtra("cityName");
            Log.d("myWeather", "选择的城市代码为"+newCityCode);

            if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
            Log.d("myWeather", "网络OK");
            queryWeatherCode(newCityCode);
            }else{
            Log.d("myWeather", "网络挂了");
            Toast.makeText(MainActivity.this, "网络挂了！", Toast.LENGTH_LONG).show();
            }

            //改变城市代码的值
            CodeCity = newCityCode;
            CityName = newCityName;
            //获取sharedPreferences对象
            SharedPreferences sharedPreferences = getSharedPreferences("recently_info", MODE_PRIVATE);
            //获取editor对象
            SharedPreferences.Editor editor = sharedPreferences.edit();//获取编辑器
            //存储键值对
            editor.putString("CityName",CityName);
            editor.putString("CodeCity",CodeCity);
            //提交
            editor.commit();//提交修改

        }
    }

    public void update_btn(){
        mprogressBar.setVisibility(View.INVISIBLE);
        mUpdateBtn.setVisibility(View.VISIBLE);
    }
    public void update_pregross(){
        mprogressBar.setVisibility(View.VISIBLE);
        mUpdateBtn.setVisibility(View.INVISIBLE);
    }

    //配置定位SDK参数
    public void configurat_parameter(){
        LocationClientOption option = new LocationClientOption();
        //可选，是否需要地址信息，默认为不需要，即参数为false
        //如果开发者需要获得当前点的地址信息，此处必须为true
        option.setIsNeedAddress(true);

        option.setOpenGps(true);  //使用GPS

        //mLocationClient为第二步初始化过的LocationClient对象
        //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        //更多LocationClientOption的配置，请参照类参考中LocationClientOption类的详细说明
        mLocationClient.setLocOption(option);
    }

    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location){
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取地址相关的结果信息
            //更多结果信息获取说明，请参照类参考中BDLocation类中的说明

            String addr = location.getAddrStr();    //获取详细地址信息
            String country = location.getCountry();    //获取国家
            String province = location.getProvince();    //获取省份
            String city = location.getCity();    //获取城市
            String district = location.getDistrict();    //获取区县
            String street = location.getStreet();    //获取街道信息
            mlocal_adrr = addr;
            Log.d("myWeather_1","addr是"+city+addr);
        }
    }

/**************下面是下滑框的代码*******************/
    private ViewPagerAdapter vpAdapter;
    private ViewPager vp;
    private List<View> views;

    private ImageView[] dots;
    private int[] ids = {R.id.iv4,R.id.iv5};

    void initDots(){
        dots = new ImageView[views.size()];
        for(int i=0;i<views.size();i++){
            dots[i]=(ImageView) findViewById(ids[i]);
        }
    }

    private void initViews_pagerView(){
        LayoutInflater layoutInflater = LayoutInflater.from(this);  //通过LayoutInflater来动态加载这些视图
        views = new ArrayList<View>();
        views.add(layoutInflater.inflate(R.layout.frist2days,null));
        views.add(layoutInflater.inflate(R.layout.second2days,null));
        vpAdapter = new ViewPagerAdapter(views,this);
        vp = (ViewPager) findViewById(R.id.viewpager2);
        vp.setAdapter(vpAdapter);
        vp.setOnPageChangeListener(this);
        initDots();
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {
        for(int a=0;a<ids.length;a++){
            if(a ==i){
                dots[a].setImageResource(R.drawable.page_indicator_focused);
            }else{
                dots[a].setImageResource(R.drawable.page_indicator_unfocused);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

}
