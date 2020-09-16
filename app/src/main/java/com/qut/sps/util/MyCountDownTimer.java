package com.qut.sps.util;

import android.os.CountDownTimer;
import android.widget.Button;

/**
 * Created by 13686 on 2017/8/6.
 */

public class MyCountDownTimer extends CountDownTimer {

    private Button btn_djs;

    public void setBtn_djs(Button btn_djs) {
        this.btn_djs = btn_djs;
    }

    public MyCountDownTimer(long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
    }

    //计时过程
    @Override
    public void onTick(long l) {
        //防止计时过程中重复点击
       // btn_djs.setClickable(false);
        btn_djs.setText(l/1000+"s");

    }

    //计时完毕的方法
    @Override
    public void onFinish() {
        //重新给Button设置文字
        btn_djs.setText("重新获取");
        //设置可点击
        btn_djs.setClickable(true);
    }
}
