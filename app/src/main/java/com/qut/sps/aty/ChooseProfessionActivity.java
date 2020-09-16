package com.qut.sps.aty;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.qut.sps.R;
import com.qut.sps.adapter.ProfessionAdapter;
import com.qut.sps.adapter.ProfessionData;

import java.util.ArrayList;
import java.util.List;

public class ChooseProfessionActivity extends AppCompatActivity {
    private Button back;
    private ListView proflistView;
    private List<ProfessionData> profList=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_profession);

        initView();
        init();
        initProfData();
    }

    private void init() {
        back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                (ChooseProfessionActivity.this).finish();
            }
        });
        ProfessionAdapter adapter=new ProfessionAdapter(ChooseProfessionActivity.this,R.layout.profession_data,profList);
        proflistView.setAdapter(adapter);

        proflistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ProfessionData profess=profList.get(i);
                Intent intent=new Intent();
                intent.putExtra("data_return",profess.getName());
                setResult(RESULT_OK,intent);
                finish();
            }
        });
    }

    private void initView() {

        back=(Button)findViewById(R.id.title_back);
        proflistView=(ListView)findViewById(R.id.list_prof);
        TextView titleName = (TextView) findViewById(R.id.title_name);
        titleName.setText("职业");
        ImageView titleImg = (ImageView) findViewById(R.id.title_right_img);
        titleImg.setVisibility(View.GONE);
    }

    /**
     * 添加数据
     */
    private void initProfData() {
        ProfessionData p1=new ProfessionData("生产/工艺/制造",R.drawable.img6);
        profList.add(p1);
        ProfessionData p2=new ProfessionData("计算机/互联网/通信",R.drawable.img6);
        profList.add(p2);
        ProfessionData p3=new ProfessionData("医疗/护理/制药",R.drawable.img6);
        profList.add(p3);
        ProfessionData p4=new ProfessionData("金融/银行/投资",R.drawable.img6);
        profList.add(p4);
        ProfessionData p5=new ProfessionData("商业/服务业/个体经营",R.drawable.img6);
        profList.add(p5);
        ProfessionData p6=new ProfessionData("文化/广告/传媒",R.drawable.img6);
        profList.add(p6);
        ProfessionData p7=new ProfessionData("律师/法务",R.drawable.img6);
        profList.add(p7);
        ProfessionData p8=new ProfessionData("教育/培训",R.drawable.img6);
        profList.add(p8);
        ProfessionData p9=new ProfessionData("模特",R.drawable.img6);
        profList.add(p9);
        ProfessionData p10=new ProfessionData("街舞达人",R.drawable.img6);
        profList.add(p10);
        ProfessionData p11=new ProfessionData("学生",R.drawable.img6);
        profList.add(p11);
        ProfessionData p12=new ProfessionData("其他职业",R.drawable.img6);
        profList.add(p12);
    }

}
