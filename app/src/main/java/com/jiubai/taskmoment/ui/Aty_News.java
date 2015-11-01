package com.jiubai.taskmoment.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.adapter.Adpt_News;
import com.jiubai.taskmoment.adapter.Adpt_Timeline;
import com.jiubai.taskmoment.classes.News;
import com.jiubai.taskmoment.classes.Task;
import com.jiubai.taskmoment.other.UtilBox;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 新消息页面
 */
public class Aty_News extends AppCompatActivity {
    @Bind(R.id.tv_title)
    TextView tv_title;

    @Bind(R.id.lv_news)
    ListView lv;

    private Adpt_News adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UtilBox.setStatusBarTint(this, R.color.titleBar);

        setContentView(R.layout.aty_news);

        ButterKnife.bind(this);

        initView();
    }

    /**
     * 初始化所有view
     */
    private void initView() {
        tv_title.setText("消息");

        showNewsList();
    }

    /**
     * 显示最新消息
     */
    private void showNewsList() {
        ArrayList<News> newsList = new ArrayList<>();

        Task task1 = Adpt_Timeline.taskList.get(0);
        if(task1.getPictures().isEmpty()) {
            newsList.add(new News(task1.getPortraitUrl(), task1.getNickname(), "123", task1.getDesc(),
                    task1.getCreate_time(), null, task1.getId()));
        } else {
            newsList.add(new News(task1.getPortraitUrl(), task1.getNickname(), "123", task1.getDesc(),
                    task1.getCreate_time(), task1.getPictures().get(0), task1.getId()));
        }

        Task task2 = Adpt_Timeline.taskList.get(1);
        if(task2.getPictures().isEmpty()) {
            newsList.add(new News(task2.getPortraitUrl(), task2.getNickname(), "123", task2.getDesc(),
                    task2.getCreate_time(), null, task2.getId()));
        } else {
            newsList.add(new News(task2.getPortraitUrl(), task2.getNickname(), "123", task2.getDesc(),
                    task2.getCreate_time(), task2.getPictures().get(0), task2.getId()));
        }

        adapter = new Adpt_News(this, newsList);
        lv.setAdapter(adapter);
    }

    @OnClick({R.id.iBtn_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iBtn_back:
                finish();
                overridePendingTransition(R.anim.in_left_right,
                        R.anim.out_left_right);
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
