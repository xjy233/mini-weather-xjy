package com.example.xiang.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xiang.app.MyApplication;
import com.example.xiang.bean.City;

import java.util.ArrayList;
import java.util.List;

public class SelectCity extends Activity implements View.OnClickListener{

    private ImageView mBackBtn;
    private ListView  mCityList;
    private TextView mCurrent_city;

    private String CityName; //接受由MainActivity传来的当前城市信息，以更新当前城市

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.select_city);

        initViews();
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.title_back:
//                Intent i = new Intent();
//                i.putExtra("cityCode","101160101");
//                setResult(RESULT_OK, i);
                finish();
                break;
            default:
                break;
        }
    }

    public void initViews(){
        //设置返回按钮的监听
        mBackBtn = (ImageView) findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);

        //更新顶部的当前天气
        Intent intent = this.getIntent();
        CityName = intent.getStringExtra("CityName");
        mCurrent_city = (TextView)findViewById(R.id.title_name);
        mCurrent_city.setText("当前城市："+CityName);

        mCityList = (ListView)findViewById(R.id.city_list);
        //得到返回的ArrayList<City>城市温度信息列表
        List<City> citylist = new ArrayList<>();
        MyApplication myApplication = (MyApplication)getApplication();
        citylist = myApplication.getCityList();
        //把城市名装进data数组,代号放进code数组
        ArrayList<String> data= new ArrayList<>();
        ArrayList<String> code= new ArrayList<>();
        for(City city:citylist){
            data.add(city.getCity());
            code.add(city.getNumber());
        }
        final ArrayList<String> codefin = code;
        final ArrayList<String> cityfin = data;//匿名内部类中必须用final定义参数，所以这里转换一下
        //放入适配器中
        ArrayAdapter<String> adapter =new ArrayAdapter<String>(SelectCity.this,android.R.layout.simple_list_item_1,data);
        mCityList.setAdapter(adapter);
        mCityList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {   //position是data在adadapter中的行数，id是在ListView中的行数
                Toast.makeText(SelectCity.this,"你单击了:"+position,Toast.LENGTH_SHORT).show();

                //返回给MainActivity意图
                Intent intent_code = new Intent();
                intent_code.putExtra("cityCode",codefin.get(position));
                intent_code.putExtra("cityName",cityfin.get(position));
                setResult(RESULT_OK, intent_code);
                finish();
            }
        });


    }

}
