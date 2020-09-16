package com.qut.sps.aty;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.qut.sps.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;

public class AddressActivity extends Activity implements OnWheelChangedListener
{
    private JSONObject mJsonObj;

    private WheelView mProvince;

    private WheelView mCity;

    private WheelView mArea;

    private Button save;

    private String[] mProvinceDatas;

    private Map<String, String[]> mCitisDatasMap = new HashMap<String, String[]>();

    private Map<String, String[]> mAreaDatasMap = new HashMap<String, String[]>();

    private String mCurrentProviceName;

    private String mCurrentCityName;

    private String mCurrentAreaName ="";

    private Button back;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        initView();

        initJsonData();

        initDatas();

        init();
    }

    private void initView() {
        back = findViewById(R.id.title_back);
        TextView titleText = findViewById(R.id.title_name);
        titleText.setText("地址");
        ImageView titleImage = findViewById(R.id.title_right_img);
        titleImage.setVisibility(View.GONE);
        mProvince = findViewById(R.id.id_province);
        mCity =  findViewById(R.id.id_city);
        mArea =  findViewById(R.id.id_area);
        save=findViewById(R.id.save);

    }


    private void init() {
        back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                (AddressActivity.this).finish();
            }
        });

        mProvince.setViewAdapter(new ArrayWheelAdapter<>(this, mProvinceDatas));

        mProvince.addChangingListener(this);

        mCity.addChangingListener(this);

        mArea.addChangingListener(this);

        mProvince.setVisibleItems(5);
        mCity.setVisibleItems(5);
        mArea.setVisibleItems(5);

        updateCities();
        updateAreas();

        save.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String str;
                str=mCurrentProviceName + mCurrentCityName + mCurrentAreaName;
                Intent intent=new Intent();
                intent.putExtra("data_return",str);
                setResult(RESULT_OK,intent);
                finish();
            }
        });
    }
    /**
     * 更新县级地区
     */
    private void updateAreas() {
        int pCurrent = mCity.getCurrentItem();
        mCurrentCityName = mCitisDatasMap.get(mCurrentProviceName)[pCurrent];
        String[] areas = mAreaDatasMap.get(mCurrentCityName);

        if (areas == null)
        {
            areas = new String[] { "" };
        }
        mArea.setViewAdapter(new ArrayWheelAdapter<>(this, areas));
        mArea.setCurrentItem(0);
    }

    /**
     *更新市级
     */
    private void updateCities() {
        int pCurrent = mProvince.getCurrentItem();
        mCurrentProviceName = mProvinceDatas[pCurrent];
        String[] cities = mCitisDatasMap.get(mCurrentProviceName);
        if (cities == null)
        {
            cities = new String[] { "" };
        }
        mCity.setViewAdapter(new ArrayWheelAdapter<>(this, cities));
        mCity.setCurrentItem(0);
        updateAreas();
    }

    /**
     * 初始化省市县信息
     */
    private void initDatas() {
        try
        {
            JSONArray jsonArray = mJsonObj.getJSONArray("citylist");
            mProvinceDatas = new String[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject jsonP = jsonArray.getJSONObject(i);
                String province = jsonP.getString("p");
                mProvinceDatas[i] = province;
                JSONArray jsonCs = null;
                try
                {
                    jsonCs = jsonP.getJSONArray("c");
                } catch (Exception e1)
                {
                    continue;
                }
                String[] mCitiesDatas = new String[jsonCs.length()];
                for (int j = 0; j < jsonCs.length(); j++)
                {
                    JSONObject jsonCity = jsonCs.getJSONObject(j);
                    String city = jsonCity.getString("n");
                    mCitiesDatas[j] = city;
                    JSONArray jsonAreas = null;
                    try
                    {
                        jsonAreas = jsonCity.getJSONArray("a");
                    } catch (Exception e)
                    {
                        continue;
                    }

                    String[] mAreasDatas = new String[jsonAreas.length()];
                    for (int k = 0; k < jsonAreas.length(); k++)
                    {
                        String area = jsonAreas.getJSONObject(k).getString("s");
                        mAreasDatas[k] = area;
                    }
                    mAreaDatasMap.put(city, mAreasDatas);
                }

                mCitisDatasMap.put(province, mCitiesDatas);
            }

        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        mJsonObj = null;
    }

    /**
     * 解析json文件的json数据
     */
    private void initJsonData() {
        try
        {

            StringBuilder sb = new StringBuilder();
            InputStream is = getResources().getAssets().open("city.json");
            int len = -1;
            byte[] buf = new byte[1024];
            while ((len = is.read(buf)) != -1)
            {

                sb.append(new String(buf, 0, len, "gbk"));
            }
            is.close();
            mJsonObj = new JSONObject(sb.toString());
        } catch (IOException | JSONException e)
        {
            e.printStackTrace();
        }
    }

    /**
     *实现当前滚轮功能更新
     */

    @Override
    public void onChanged(WheelView wheel, int oldValue, int newValue) {
        if (wheel == mProvince)
        {
            updateCities();
        }else if (wheel == mCity)
        {
            updateAreas();
        }else if (wheel == mArea)
        {
            try{
                mCurrentAreaName = mAreaDatasMap.get(mCurrentCityName)[newValue];

            }catch(Exception e){
                e.printStackTrace();
            }

        }
    }

}
