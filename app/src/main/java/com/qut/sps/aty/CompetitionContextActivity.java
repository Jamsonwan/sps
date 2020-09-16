package com.qut.sps.aty;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.qut.sps.R;
import com.qut.sps.util.Constant;
import com.qut.sps.util.HttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CompetitionContextActivity extends AppCompatActivity {

    public static final String NAME = "name";

    public static final String START_TIME = "start_time";

    public static final String END_TIME = "end_time";

    public static final String PLACE = "place";

    public static final String TEL = "tel";

    public static final String DESCRIPTION = "description";

    public static final String IMAGE_URL = "image_url";

    public static final String USER_ID = "user_id";

    private TextView user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_competition_context);

        Intent intent = getIntent();
        String competition_name = intent.getStringExtra(NAME);
        String start = intent.getStringExtra(START_TIME);
        String end = intent.getStringExtra(END_TIME);
        String place = intent.getStringExtra(PLACE);
        String tel = intent.getStringExtra(TEL);
        String description = intent.getStringExtra(DESCRIPTION);
        String imageurl = intent.getStringExtra(IMAGE_URL);
        String userId = intent.getStringExtra(USER_ID);

        initTitleView();

        TextView title_name = (TextView) findViewById(R.id.title_name);
        user = (TextView) findViewById(R.id.competition_context_user);
        TextView start_time = (TextView) findViewById(R.id.competition_context_start);
        TextView end_time = (TextView) findViewById(R.id.competition_context_end);
        TextView cplace = (TextView) findViewById(R.id.competition_context_place);
        TextView cdescription = (TextView) findViewById(R.id.competition_context_description);
        TextView ctel = (TextView) findViewById(R.id.competition_context_tel);
        final LinearLayout layout = (LinearLayout) findViewById(R.id.competition_context_layout);

        Glide.with(this).asDrawable().load(Constant.SPS_SOURCE_URL+imageurl).into(new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                layout.setBackground(resource);
            }
        });
        title_name.setText(competition_name);

        getNickName(userId);

        user.setText(userId);
        start_time.setText(start);
        end_time.setText(end);
        cplace.setText(place);
        cdescription.setText(description);
        ctel.setText(tel);
    }

    private void initTitleView() {
        Button titleBack = (Button) findViewById(R.id.title_back);
        titleBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        ImageView titleImage = (ImageView) findViewById(R.id.title_right_img);
        titleImage.setVisibility(View.GONE);
    }

    private void getNickName(String userId) {
        String url = HttpUtil.SPS_URL+"GetUserInfoServlet";
        RequestBody requestBody = new FormBody.Builder()
                .add("id",userId)
                .build();
        HttpUtil.sendOkHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                if (!TextUtils.isEmpty(responseData)){
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        final String nickName = jsonObject.getString("nickName");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                user.setText(nickName);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

}
