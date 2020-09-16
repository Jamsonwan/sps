package com.qut.sps.aty;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.qut.sps.R;

import java.util.Calendar;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelClickedListener;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.NumericWheelAdapter;

public class SetTimeActivity extends AppCompatActivity {

    // Time changed flag
    private boolean timeChanged = false;

    // Time scrolled flag
    private boolean timeScrolled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_time);

        Button backBtn = (Button) findViewById(R.id.title_back);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        TextView titleText = (TextView) findViewById(R.id.title_name);
        titleText.setText("时间");

        ImageView titleRightImg  = (ImageView) findViewById(R.id.title_right_img);
        titleRightImg.setVisibility(View.GONE);

        final WheelView hours = (WheelView) findViewById(R.id.hour);
        hours.setViewAdapter(new NumericWheelAdapter(this, 0, 23));

        final WheelView mins = (WheelView) findViewById(R.id.mins);
        mins.setViewAdapter(new NumericWheelAdapter(this, 0, 59, "%02d"));
        mins.setCyclic(true);

        final TimePicker picker = (TimePicker) findViewById(R.id.time);
        picker.setIs24HourView(true);


        // set current time
        Calendar c = Calendar.getInstance();
        final int curHours = c.get(Calendar.HOUR_OF_DAY);
        final int curMinutes = c.get(Calendar.MINUTE);

        hours.setCurrentItem(curHours);
        mins.setCurrentItem(curMinutes);

        picker.setCurrentHour(curHours);
        picker.setCurrentMinute(curMinutes);

        Button submit = (Button) findViewById(R.id.title_right_btn);
        submit.setVisibility(View.VISIBLE);
        submit.setText("确定");
        // add listeners
        addChangingListener(mins, "min");
        addChangingListener(hours, "hour");

        OnWheelChangedListener wheelListener = new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                if (!timeScrolled) {
                    timeChanged = true;
                    picker.setCurrentHour(hours.getCurrentItem());
                    picker.setCurrentMinute(mins.getCurrentItem());
                    timeChanged = false;
                }
            }
        };
        hours.addChangingListener(wheelListener);
        mins.addChangingListener(wheelListener);

        OnWheelClickedListener click = new OnWheelClickedListener() {
            @Override
            public void onItemClicked(WheelView wheel, int itemIndex) {
                wheel.setCurrentItem(itemIndex, true);
            }
        };
        hours.addClickingListener(click);
        mins.addClickingListener(click);

        OnWheelScrollListener scrollListener = new OnWheelScrollListener() {
            @Override
            public void onScrollingStarted(WheelView wheel) {
                timeScrolled = true;
            }
            @Override
            public void onScrollingFinished(WheelView wheel) {
                timeScrolled = false;
                timeChanged = true;
                picker.setCurrentHour(hours.getCurrentItem());
                picker.setCurrentMinute(mins.getCurrentItem());
                timeChanged = false;
            }
        };

        hours.addScrollingListener(scrollListener);
        mins.addScrollingListener(scrollListener);

        picker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker  view, int hourOfDay, int minute) {
                if (!timeChanged) {
                    hours.setCurrentItem(hourOfDay, true);
                    mins.setCurrentItem(minute, true);
                }
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String finalHours,finalMins;
                Intent intent=new Intent();

                int hour;
                int min;

                int currentApiVersion = android.os.Build.VERSION.SDK_INT;
                if (currentApiVersion > android.os.Build.VERSION_CODES.LOLLIPOP_MR1){
                    hour = picker.getHour();
                    min = picker.getMinute();
                } else {
                    hour =picker.getCurrentHour();
                    min = picker.getCurrentMinute();
                }

                if(hour<10){
                    finalHours="0"+hour;
                }else{
                    finalHours= String.valueOf(hour);
                }
                if(min < 10){
                    finalMins="0"+min;
                }else{
                    finalMins=String .valueOf(min);
                }
                intent.putExtra("data_return",finalHours+":"+finalMins);
                setResult(RESULT_OK,intent);
                (SetTimeActivity.this).finish();
            }
        });
    }

    private void addChangingListener(final WheelView wheel, final String label) {
        wheel.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
            }
        });
    }
}
