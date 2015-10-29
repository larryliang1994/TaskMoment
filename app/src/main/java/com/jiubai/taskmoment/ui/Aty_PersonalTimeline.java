package com.jiubai.taskmoment.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.adapter.Adpt_Member;
import com.jiubai.taskmoment.adapter.Adpt_PersonalTimeline;
import com.jiubai.taskmoment.classes.Comment;
import com.jiubai.taskmoment.classes.Member;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.net.VolleyUtil;
import com.jiubai.taskmoment.other.UtilBox;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.view.BorderScrollView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 个人情况
 */
public class Aty_PersonalTimeline extends AppCompatActivity {
    @Bind(R.id.tv_title)
    TextView tv_title;

    @Bind(R.id.iv_portrait)
    ImageView iv_portrait;

    @Bind(R.id.tv_loading)
    TextView tv_loading;

    @Bind(R.id.tv_personal_nickname)
    TextView tv_nickname;

    @Bind(R.id.iv_companyBackground)
    ImageView iv_companyBackground;

    private static LinearLayout ll_comment, ll_audit;
    private static Space space;
    private static BorderScrollView sv;
    private static ListView lv;
    private static Adpt_PersonalTimeline adapter;
    public static boolean commentWindowIsShow = false, auditWindowIsShow = false;
    private View footerView;
    private String mid;
    private String name;
    private String mobile;
    private String request_type;
    private boolean isBottomRefreshing = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.aty_personal_timeline);

        ButterKnife.bind(this);

        mid = getIntent().getStringExtra("mid");

        String rt = getIntent().getStringExtra("request_type");
        request_type = rt == null ? "0" : rt;

        if (Adpt_Member.memberList == null || Adpt_Member.memberList.isEmpty()) {
            UtilBox.getMember(this, new UtilBox.GetMemberCallBack() {
                @Override
                public void successCallback() {
                    getUserInfo();
                    initView();
                }

                @Override
                public void failedCallback() {
                }
            });
        } else {
            getUserInfo();
            initView();
        }
    }

    /**
     * 初始化组件
     */
    private void initView() {

        if (!"null".equals(name) && !"".equals(name)) {
            tv_title.setText(name);
            tv_nickname.setText(name);
        } else {
            tv_title.setText(mobile);
            tv_nickname.setText(mobile);
        }

        iv_portrait.setFocusable(true);
        iv_portrait.setFocusableInTouchMode(true);
        iv_portrait.requestFocus();
        ImageLoader.getInstance()
                .displayImage(Constants.HOST_ID + "task_moment/" + mid + ".jpg", iv_portrait);

        lv = (ListView) findViewById(R.id.lv_personal);
        ll_comment = (LinearLayout) findViewById(R.id.ll_comment);
        ll_audit = (LinearLayout) findViewById(R.id.ll_audit);
        space = (Space) findViewById(R.id.space);

        footerView = LayoutInflater.from(this).inflate(R.layout.load_more, null);
        lv.addFooterView(footerView);

        sv = (BorderScrollView) findViewById(R.id.sv_personal);

        sv.setOnBorderListener(new BorderScrollView.OnBorderListener() {

            @Override
            public void onTop() {
            }

            @Override
            public void onBottom() {
                // 有footerView并且不是正在加载
                if (lv.getFooterViewsCount() > 0 && !isBottomRefreshing) {
                    isBottomRefreshing = true;

                    // 参数应为最后一条任务的时间减1秒
                    refreshTimeline("loadMore", (Adpt_PersonalTimeline.taskList
                            .get(Adpt_PersonalTimeline.taskList.size() - 1)
                            .getCreate_time() / 1000 - 1) + "");
                }
            }
        });

        final TextView tv_addBackground = (TextView) findViewById(R.id.tv_add_background);

        if (Config.COMPANY_BACKGROUND != null) {
            ImageLoader.getInstance().displayImage(
                    Config.COMPANY_BACKGROUND, iv_companyBackground, new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String s, View view) {
                        }

                        @Override
                        public void onLoadingFailed(String s, View view, FailReason failReason) {
                        }

                        @Override
                        public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                            tv_addBackground.setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadingCancelled(String s, View view) {
                        }
                    });
        }

        refreshTimeline("refresh", Calendar.getInstance(Locale.CHINA).getTimeInMillis() / 1000 + "");
    }

    /**
     * 获取时间线
     *
     * @param type         类别 refresh或loadMore
     * @param request_time 需要获取哪个时间之后的数据
     */
    private void refreshTimeline(final String type, String request_time) {
        if (!Config.IS_CONNECTED) {
            Toast.makeText(this, R.string.cant_access_network,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String[] key = {"len", "cid", "create_time", "mid", "shenhe"};
        String[] value = {"2", Config.CID, request_time, mid, request_type};

        VolleyUtil.requestWithCookie(Urls.GET_TASK_LIST, key, value,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        JSONObject responseJson;
                        try {
                            responseJson = new JSONObject(response);

                            String responseStatus = responseJson.getString("status");

                            if ("1".equals(responseStatus) || "900001".equals(responseStatus)) {

                                if ("refresh".equals(type)) {
                                    adapter = new Adpt_PersonalTimeline(
                                            Aty_PersonalTimeline.this, true, response);
                                    tv_loading.setVisibility(View.GONE);
                                } else {
                                    adapter = new Adpt_PersonalTimeline(
                                            Aty_PersonalTimeline.this, false, response);
                                }

                                new Handler().post(new Runnable() {
                                    @Override
                                    public void run() {

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                lv.setAdapter(adapter);
                                                UtilBox.setListViewHeightBasedOnChildren(lv);

                                                if ("loadMore".equals(type)) {
                                                    isBottomRefreshing = false;
                                                }
                                            }
                                        });
                                    }
                                });

                            } else if ("900900".equals(responseStatus)) {
                                // 没有更多了，就去掉footerView
                                if ("refresh".equals(type)) {
                                    tv_loading.setVisibility(View.GONE);
                                } else {
                                    lv.removeFooterView(footerView);

                                    UtilBox.setListViewHeightBasedOnChildren(lv);

                                    isBottomRefreshing = false;
                                }

                                Toast.makeText(Aty_PersonalTimeline.this,
                                        responseJson.getString("info"),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                if ("refresh".equals(type)) {
                                    tv_loading.setVisibility(View.GONE);
                                } else {
                                    isBottomRefreshing = false;
                                }

                                Toast.makeText(Aty_PersonalTimeline.this,
                                        responseJson.getString("info"),
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                        if ("refresh".equals(type)) {
                            tv_loading.setVisibility(View.GONE);
                        } else {
                            isBottomRefreshing = false;
                        }

                        volleyError.printStackTrace();
                        Toast.makeText(Aty_PersonalTimeline.this,
                                R.string.usual_error,
                                Toast.LENGTH_SHORT).show();
                    }
                }

        );
    }

    /**
     * 从memberList中读取用户信息
     */
    private void getUserInfo() {
        for (int i = 1; i < Adpt_Member.memberList.size() - 1; i++) {
            Member member = Adpt_Member.memberList.get(i);
            if (mid.equals(member.getMid())) {
                name = member.getName();
                mobile = member.getMobile();
                break;
            }
        }
    }

    /**
     * 弹出评论窗口
     *
     * @param context    上下文
     * @param position   任务位置
     * @param taskID     任务ID
     * @param receiver   接收者
     * @param receiverID 接收者ID
     * @param y          所点击的组件的y坐标
     */
    public static void showCommentWindow(final Context context,
                                         final int position, final String taskID,
                                         final String receiver, final String receiverID,
                                         final int y) {
        commentWindowIsShow = true;

        if (auditWindowIsShow) {
            ll_audit.setVisibility(View.GONE);

            auditWindowIsShow = false;
        }

        ll_comment.setVisibility(View.VISIBLE);
        final EditText edt_content = (EditText) ll_comment.findViewById(R.id.edt_comment_content);
        edt_content.requestFocus();

        UtilBox.setViewParams(space, 1, UtilBox.dip2px(context, 360 + 56));

        // 弹出键盘
        UtilBox.toggleSoftInput(ll_comment, true);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                int keyBoardHeight = UtilBox.dip2px(context, 360);
                int viewHeight = UtilBox.getHeightPixels(context) - y;

                int finalScroll = keyBoardHeight - viewHeight
                        + sv.getScrollY() + UtilBox.dip2px(context, 56);

                sv.smoothScrollTo(0, finalScroll);
            }
        });

        if (!"".equals(receiver)) {
            edt_content.setHint("回复" + receiver + ":");
        } else {
            edt_content.setHint("评论");
        }
        edt_content.setText(null);

        Button btn_send = (Button) ll_comment.findViewById(R.id.btn_comment_send);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Config.IS_CONNECTED) {
                    Toast.makeText(context,
                            R.string.cant_access_network,
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                ll_comment.setVisibility(View.GONE);

                UtilBox.setViewParams(space, 0, 0);

                UtilBox.toggleSoftInput(ll_comment, false);

                String[] key = {"oid", "pmid", "content"};
                String[] value = {taskID, receiverID, edt_content.getText().toString()};

                VolleyUtil.requestWithCookie(Urls.SEND_COMMENT, key, value,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);

                                    if (!"900001".equals(jsonObject.getString("status"))) {
                                        System.out.println(response);

                                        Toast.makeText(context,
                                                R.string.usual_error,
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        if (!"".equals(receiver)) {
                                            Adpt_PersonalTimeline.taskList.get(position)
                                                    .getComments().add(
                                                    new Comment(taskID, position,
                                                            Config.NICKNAME, Config.MID,
                                                            receiver, receiverID,
                                                            edt_content.getText().toString(),
                                                            Calendar.getInstance(Locale.CHINA)
                                                                    .getTimeInMillis()));
                                        } else {
                                            Adpt_PersonalTimeline.taskList.get(position)
                                                    .getComments().add(
                                                    new Comment(taskID, position,
                                                            Config.NICKNAME, Config.MID,
                                                            edt_content.getText().toString(),
                                                            Calendar.getInstance(Locale.CHINA)
                                                                    .getTimeInMillis()));
                                        }
                                        adapter.notifyDataSetChanged();
                                        UtilBox.setListViewHeightBasedOnChildren(lv);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                volleyError.printStackTrace();
                                Toast.makeText(context,
                                        R.string.usual_error,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    /**
     * 弹出审核窗口
     */
    public static void showAuditWindow() {
        auditWindowIsShow = true;

        if (commentWindowIsShow) {
            UtilBox.setViewParams(space, 1, 0);
            ll_comment.setVisibility(View.GONE);
            commentWindowIsShow = false;
        }

        ll_audit.setVisibility(View.VISIBLE);

        ((RadioButton) ll_audit.findViewById(R.id.rb_complete)).setChecked(true);

        Button btn_send = (Button) ll_audit.findViewById(R.id.btn_audit_send);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_audit.setVisibility(View.GONE);
            }
        });
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

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
