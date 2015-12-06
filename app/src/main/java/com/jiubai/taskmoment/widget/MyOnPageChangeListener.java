package com.jiubai.taskmoment.widget;

import android.app.Activity;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.common.UtilBox;

public class MyOnPageChangeListener implements SlidingView.OnPageChangeListener{
    private boolean isScrolling = false;
    private Activity activity;

    public MyOnPageChangeListener(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if(!isScrolling){
            UtilBox.setStatusBarTint(activity, R.color.statusBar);
            isScrolling = true;
        } else if (positionOffsetPixels == 0){
//            UtilBox.setStatusBarTint(activity, R.color.titleBar);
            isScrolling = false;

            System.out.println("back");
        }

        /*到达了结束Activity 的条件*/
        if (position == SlidingLayout.POSITION_FINISH) {
            isScrolling = false;

            /*结束当前 */
            activity.finish();
        }
    }

    @Override
    public void onPageSelected(int position) {
        if(isScrolling){
            UtilBox.setStatusBarTint(activity, R.color.titleBar);
            //isScrolling = false;
        }

        System.out.println("selected");
    }
}
