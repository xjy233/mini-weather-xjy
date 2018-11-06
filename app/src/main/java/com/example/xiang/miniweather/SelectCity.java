package com.example.xiang.miniweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xiang.app.MyApplication;
import com.example.xiang.bean.City;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.xiang.miniweather.PinYin;


public class SelectCity extends Activity implements View.OnClickListener{

    private ImageView mBackBtn;
    private ListView  mCityList;
    private TextView mCurrent_city;
    private SearchView msearchview;
    private EditText mEditText;

    private String CityName; //接受由MainActivity传来的当前城市信息，以更新当前城市

    //把城市名装进data数组,代号放进code数组
    private ArrayList<String> CityNameList= new ArrayList<>();
    private Map<String,String> nameToCode = new HashMap<>();
    //城市名到拼音
    private Map<String,String> nameToPinyin = new HashMap<>();

    private ArrayAdapter<String> adapter;

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

    public void Searchview(){
        /****************下面是新加的搜索框*******************/

        //msearchview = (SearchView) findViewById(R.id.search);
        msearchview.setIconified(false); //需要点击搜索图标，才展开搜索框
        msearchview.setQueryHint("请输入城市名称或拼音");

        msearchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText)) { //搜索栏不空时，执行搜索
                    if (CityNameList != null) { //清空上次搜索结果
                        CityNameList.clear();
                    }
                    //遍历 nameToPinyin 的键值（它包含所有城市名）
                    for (String str : nameToPinyin.keySet()){
                        if (str.contains(newText)||nameToPinyin.get(str).contains(newText)) {
                            CityNameList.add(str);
                        }
                    }
                    adapter.notifyDataSetChanged();

                }
                return true;
            }
        });
    }

    public void UseEditText(){
        /****************下面是使用editTxet实现搜索框*******************/
        TextWatcher mTextWatcher = new TextWatcher(){
            private CharSequence temp;
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                temp = charSequence;
                Log.d("myapp","beforeTextChanged:"+temp) ;
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if (!TextUtils.isEmpty(charSequence)) { //搜索栏不空时，执行搜索
                    if (CityNameList != null) { //清空上次搜索结果
                        CityNameList.clear();
                    }
                    //遍历 nameToPinyin 的键值（它包含所有城市名）
                    for (String str : nameToPinyin.keySet()){
                        if (str.contains(charSequence)||nameToPinyin.get(str).contains(charSequence)) {
                            CityNameList.add(str);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
                Log.d("myapp","onTextChanged:"+charSequence) ;
            }
            @Override
            public void afterTextChanged(Editable editable){
                Log.d("myapp","afterTextChanged:") ;
            }
        };
        mEditText = (EditText)findViewById(R.id.search_edit);
        mEditText.addTextChangedListener(mTextWatcher);
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

        String strNamePinyin;
        for(City city:citylist){
            CityNameList.add(city.getCity());//初始状态包含全部城市
            strNamePinyin = PinYin.converterToSpell(city.getCity());
            //城市名解析成拼音
            nameToCode.put(city.getCity(),city.getNumber()); //城市名到城市编码
            nameToPinyin.put(city.getCity(),strNamePinyin); //城市名到拼音
        }

        //final Map<String,String> fnameToCode = nameToCode;//匿名内部类中必须用final定义参数，所以这里转换一下
        //放入适配器中
        adapter =new ArrayAdapter<String>(SelectCity.this,android.R.layout.simple_list_item_1,CityNameList);
        mCityList.setAdapter(adapter);
        mCityList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {   //position是data在adadapter中的行数，id是在ListView中的行数
                Toast.makeText(SelectCity.this,"你单击了:"+position,Toast.LENGTH_SHORT).show();

                //返回给MainActivity意图
                Intent intent_code = new Intent();
                String return_name = CityNameList.get(position);
                intent_code.putExtra("cityCode",nameToCode.get(return_name));
                intent_code.putExtra("cityName",return_name);
                setResult(RESULT_OK, intent_code);
                finish();
            }
        });
        UseEditText();

    }

}
