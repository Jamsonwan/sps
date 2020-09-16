package com.qut.sps.aty;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.qut.sps.R;

public class ReflectActivity extends AppCompatActivity {

    private EditText textInputEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reflect);


        Button submit = (Button) findViewById(R.id.title_right_btn);
        submit.setText("提交");
        submit.setVisibility(View.VISIBLE);

        ImageView titleImg = (ImageView) findViewById(R.id.title_right_img);
        titleImg.setVisibility(View.GONE);

        TextView titleName = (TextView) findViewById(R.id.title_name);
        titleName.setText("意见反馈");

        Button back = (Button) findViewById(R.id.title_back);
        textInputEditText=(EditText)findViewById(R.id.textInputEditText);

        submit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(textInputEditText.getText().length()==0){
                    textInputEditText.setError("您还没填写反馈内容哦");
                }else if(textInputEditText.getText().length()>200){
                    textInputEditText.setError("不得超过200字");
                }else{
                    Toast.makeText(ReflectActivity.this, "提交成功", Toast.LENGTH_SHORT).show();
                }
            }
        });
        back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                (ReflectActivity.this).finish();
            }
        });
    }
}
