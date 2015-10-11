package com.jiubai.taskmoment.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.UtilBox;
import com.jiubai.taskmoment.adapter.Adpt_Timeline;
import com.jiubai.taskmoment.classes.Comment;
import com.jiubai.taskmoment.classes.Task;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Urls;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 个人情况
 */
public class Aty_PersonalInfo extends AppCompatActivity {
    @Bind(R.id.tv_title)
    TextView tv_title;

    @Bind(R.id.lv_timeline)
    ListView lv;

    @Bind(R.id.iv_portrait)
    ImageView iv_portrait;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.aty_personalinfo);

        ButterKnife.bind(this);

        initView();
    }

    /**
     * 初始化组件
     */
    private void initView() {
        tv_title.setText(getIntent().getStringExtra("name"));

        iv_portrait.setFocusable(true);
        iv_portrait.setFocusableInTouchMode(true);
        iv_portrait.requestFocus();

        refreshTimeline();
    }

    private void refreshTimeline() {
        if (!Config.IS_CONNECTED) {
            Toast.makeText(this, "啊哦，网络好像抽风了~",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                ArrayList<Comment> commentList = new ArrayList<>();
                commentList.add(new Comment("Jack", "123", "How are you?", "8:50"));
                commentList.add(new Comment("Mike", "456", "Jack", "123", "I'm fine, what about you?", "8:50"));
                commentList.add(new Comment("Jack", "123", "Mike", "456", "I'm fine.", "8:50"));

                ArrayList<String> pictureList = new ArrayList<>();

                pictureList.add(Urls.PICTURE_1);
                pictureList.add(Urls.PICTURE_2);
                pictureList.add(Urls.PICTURE_3);
                pictureList.add(Urls.PICTURE_4);
                pictureList.add(Urls.PICTURE_5);
                pictureList.add(Urls.PICTURE_6);
                pictureList.add(Urls.PICTURE_7);
                pictureList.add(Urls.PICTURE_8);
                pictureList.add(Urls.PICTURE_9);

                final List<Task> taskList = new ArrayList<>();
                taskList.add(new Task(Urls.PICTURE_1,
                        "Howell", "S", "这是第一个任务", pictureList, "8:40", commentList));
                taskList.add(new Task(Urls.PICTURE_2,
                        "Leung", "A", "这是第二个任务", pictureList, "9:00", commentList));
                taskList.add(new Task(Urls.PICTURE_3,
                        "Jack", "B", "这是第三个任务", pictureList, "9:10", commentList));
                taskList.add(new Task(Urls.PICTURE_4,
                        "Mike", "C", "这是第四个任务", pictureList, "9:10", commentList));
                taskList.add(new Task(Urls.PICTURE_5,
                        "Susan", "D", "这是第五个任务", pictureList, "9:10", commentList));
                taskList.add(new Task(Urls.PICTURE_6,
                        "Jimmy", "S", "这是第六个任务", pictureList, "9:10", commentList));
                taskList.add(new Task(Urls.PICTURE_7,
                        "Peter", "A", "这是第七个任务", pictureList, "9:10", commentList));

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                lv.setAdapter(new Adpt_Timeline(Aty_PersonalInfo.this, taskList));
                                UtilBox.setListViewHeightBasedOnChildren(lv);
                            }
                        });

                    }
                }, 300);

                Looper.loop();
            }
        }).start();

    }

    @OnClick({R.id.iBtn_back})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iBtn_back:
                finish();
                overridePendingTransition(R.anim.in_left_right, R.anim.out_left_right);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {

            finish();
            overridePendingTransition(R.anim.in_left_right,
                    R.anim.out_left_right);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
