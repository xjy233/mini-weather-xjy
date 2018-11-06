package com.example.xiang.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xiang.bean.TodayWeather;
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

public class MainActivity extends Activity implements View.OnClickListener{
    private static final int UPDATE_TODAY_WEATHER = 1;
    private static final int UPDATE_Visibility_ProgressBar = 2;
    private static final int UPDATE_Visibility_UpdateBtn = 3;
    private ImageView mUpdateBtn;

    private ImageView mCitySelect;

    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQualityTv, temperatureTv, climateTv, windTv, city_name_Tv;
    private ImageView weatherImg, pmImg;

    private ProgressBar mprogressBar;

    private String CodeCity="101010100",CityName = "";   //想办法通过SharedPreferences存储起来

    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg){
            int isvisibility;
            switch (msg.what){
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather) msg.obj);
                    break;
                case UPDATE_Visibility_ProgressBar:
                    isvisibility = (int)msg.obj;
                    if(isvisibility == 1){
                        mprogressBar.setVisibility(View.VISIBLE);
                    }else{
                        mprogressBar.setVisibility(View.INVISIBLE);
                    }
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

        SharedPreferences sharedPreferences = getSharedPreferences("recently_info", MODE_PRIVATE);
        //getString()第二个参数为缺省值，如果preference中不存在该key，将返回缺省值
        CodeCity = sharedPreferences.getString("CodeCity", "101010100");
        CityName = sharedPreferences.getString("CityName", "北京");
        queryWeatherCode(CodeCity);

        mprogressBar.setVisibility(View.INVISIBLE);  //初始进度条控件不可见
        mUpdateBtn.setVisibility(View.INVISIBLE);
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

        initView();

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
                try{
//                    mprogressBar.setVisibility(View.VISIBLE);
//                    mUpdateBtn.setVisibility(View.INVISIBLE);
                    hidden_image("ProgressBar",1);
                    hidden_image("UpdateBtn",0);
                    Thread.sleep(4000);  //线程睡眠4S，主要是为了显示动画
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
                    todayWeather = parseXML(responseStr);
                    if (todayWeather != null){
                        Log.d("myWeather",todayWeather.toString());

                        Message msg =new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj=todayWeather;
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

    private TodayWeather parseXML(String xmldate){
        TodayWeather todayWeather = null;
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
            while (eventType != XmlPullParser.END_DOCUMENT){
                switch (eventType){
                    //判断当前事件是否为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    //判断当前事件是否为标签元素开始事件
                    case XmlPullParser.START_TAG:
                        if (xmlPullParser.getName().equals("resp")){
                            todayWeather = new TodayWeather();
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
                            }else if(xmlPullParser.getName().equals("fengli") && fengliCount == 0){
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli(xmlPullParser.getText());
                                fengliCount++;
                            }else if(xmlPullParser.getName().equals("date") && dateCount == 0){
                                eventType = xmlPullParser.next();
                                todayWeather.setDate(xmlPullParser.getText());
                                dateCount++;
                            }else if(xmlPullParser.getName().equals("high") && highCount == 0){
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            }else if(xmlPullParser.getName().equals("low") && lowCount == 0){
                                eventType = xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            }else if(xmlPullParser.getName().equals("type") && typeCount == 0){
                                eventType = xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
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
        return todayWeather;
    }

    void updateTodayWeather(TodayWeather todayWeather){
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
        Toast.makeText(MainActivity.this,"更新成功！",Toast.LENGTH_SHORT).show();
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



}
