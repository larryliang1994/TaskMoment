package com.jiubai.taskmoment.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.aliyun.mbaas.oss.callback.SaveCallback;
import com.aliyun.mbaas.oss.model.OSSException;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.jiubai.taskmoment.adapter.Adpt_Timeline;
import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.UtilBox;
import com.jiubai.taskmoment.classes.Comment;
import com.jiubai.taskmoment.classes.Task;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.net.OssUtil;
import com.jiubai.taskmoment.net.VolleyUtil;
import com.jiubai.taskmoment.view.BorderScrollView;

import java.util.ArrayList;
import java.util.List;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;

/**
 * 时间线（任务圈）
 */
public class Frag_Timeline extends Fragment
        implements View.OnClickListener {

    private SwipeRefreshLayout srl;
    private static ListView lv;
    public static LinearLayout ll_comment;
    private static LinearLayout ll_audit;
    private Adpt_Timeline adapter;
    private ImageView iv_companyBackground;

    public static boolean commentWindowIsShow = false, auditWindowIsShow = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_timeline, container, false);

        initView(view);
        return view;
    }

    /**
     * 初始化界面
     */
    @SuppressLint({"JavascriptInterface", "SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void initView(View view) {
        ImageView iv_portrait = (ImageView) view.findViewById(R.id.iv_portrait);
        iv_portrait.setFocusable(true);
        iv_portrait.setFocusableInTouchMode(true);
        iv_portrait.requestFocus();
        iv_portrait.setOnClickListener(this);

        srl = (SwipeRefreshLayout) view.findViewById(R.id.swipe_timeline);
        srl.setColorSchemeResources(R.color.primary);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshTimeline();
            }
        });

        srl.setEnabled(true);

        lv = (ListView) view.findViewById(R.id.lv_timeline);

        View footView = LayoutInflater.from(getActivity()).inflate(R.layout.load_more, null);
        lv.addFooterView(footView);
        lv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (commentWindowIsShow) {
                    ll_comment.setVisibility(View.GONE);
                    commentWindowIsShow = false;

                    // 关闭键盘
                    UtilBox.toggleSoftInput(ll_comment, false);
                }

                if (auditWindowIsShow) {
                    ll_audit.setVisibility(View.GONE);

                    auditWindowIsShow = false;
                }
                return false;
            }
        });

        BorderScrollView sv = (BorderScrollView) view.findViewById(R.id.sv_timeline);
        sv.setOnBorderListener(new BorderScrollView.OnBorderListener() {

            @Override
            public void onTop() {
                // may be done multi times, u should control it
                //Toast.makeText(getActivity(), "has reached top", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBottom() {
                // may be done multi times, u should control it
                //Toast.makeText(getActivity(), "has reached bottom", Toast.LENGTH_SHORT).show();
            }
        });

        ll_comment = (LinearLayout) view.findViewById(R.id.ll_comment);
        ll_audit = (LinearLayout) view.findViewById(R.id.ll_audit);

        iv_companyBackground = (ImageView) view.findViewById(R.id.iv_companyBackground);
        iv_companyBackground.setOnClickListener(this);
//        ImageLoader.getInstance().displayImage(
//                Constants.HOST_ID + Config.COMPANY_BACKGROUND, iv_companyBackground);

        // 延迟执行才能使旋转进度条显示出来
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshTimeline();
            }
        }, 500);
    }

    /**
     * 刷新时间线
     */
    private void refreshTimeline() {
        if (!Config.IS_CONNECTED) {
            Toast.makeText(getActivity(), "啊哦，网络好像抽风了~",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        srl.setRefreshing(true);

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

                adapter = new Adpt_Timeline(getActivity(), taskList);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                lv.setAdapter(adapter);
                                UtilBox.setListViewHeightBasedOnChildren(lv);
                                srl.setRefreshing(false);
                            }
                        });

                    }
                }, 1000);

                Looper.loop();
            }
        }).start();

    }

    @SuppressWarnings("unused")
    public static void showCommentWindow(Context context, int position, String receiver) {
        commentWindowIsShow = true;

        if (auditWindowIsShow) {
            ll_audit.setVisibility(View.GONE);

            auditWindowIsShow = false;
        }

        ll_comment.setVisibility(View.VISIBLE);
        EditText edt_content = (EditText) ll_comment.findViewById(R.id.edt_comment_content);
        edt_content.requestFocus();

        // 弹出键盘
        UtilBox.toggleSoftInput(ll_comment, true);

//        if (position != -1) {
//            lv.setSelection(position);
//        }

        if (receiver != null) {
            edt_content.setHint("回复" + receiver + ":");
        } else {
            edt_content.setHint("评论");
        }
        edt_content.setText(null);

        Button btn_send = (Button) ll_comment.findViewById(R.id.btn_comment_send);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_comment.setVisibility(View.GONE);

                String[] key = {};
                String[] value = {};

                VolleyUtil.requestWithCookie(Urls.SEND_COMMENT, key, value,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String s) {

                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {

                            }
                        });
            }
        });
    }

    @SuppressWarnings("unused")
    public static void showAuditWindow(Context context) {
        auditWindowIsShow = true;

        if (commentWindowIsShow) {
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_companyBackground:
                String[] items = {"更换公司封面"};

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                getActivity(), MultiImageSelectorActivity.class);

                        // 是否显示调用相机拍照
                        intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);

                        // 最大图片选择数量
                        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, 1);

                        // 设置模式 (支持 单选/MultiImageSelectorActivity.MODE_SINGLE 或者
                        // 多选/MultiImageSelectorActivity.MODE_MULTI)
                        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE,
                                MultiImageSelectorActivity.MODE_SINGLE);

                        startActivityForResult(intent, Constants.CODE_CHOOSE_PICTURE);

                        getActivity().overridePendingTransition(
                                R.anim.in_right_left, R.anim.out_right_left);
                    }
                })
                        .setCancelable(true);

                Dialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
                break;

            case R.id.iv_portrait:
                Intent intent = new Intent(getActivity(), Aty_PersonalInfo.class);
                intent.putExtra("name", "Leung_Howell");
                startActivity(intent);
                getActivity().overridePendingTransition(
                        R.anim.in_right_left, R.anim.out_right_left);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.CODE_CHOOSE_PICTURE:

                if (resultCode == Activity.RESULT_OK) {
                    final ArrayList<String> path = data.getStringArrayListExtra(
                            MultiImageSelectorActivity.EXTRA_RESULT);

                    if (path != null && path.size() != 0) {
                        if (!Config.IS_CONNECTED) {
                            Toast.makeText(getActivity(), "啊哦，网络好像抽风了~",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Bitmap bitmap = BitmapFactory.decodeFile(path.get(0));

                        iv_companyBackground.setImageBitmap(bitmap);

                        final String objectName = UtilBox.getObjectName();

                        OssUtil.uploadImage(UtilBox.compressImage(bitmap, 500), objectName,
                                new SaveCallback() {
                                    @Override
                                    public void onSuccess(String s) {
                                        System.out.println("upload success! trying to set..");
                                    }

                                    @Override
                                    public void onProgress(String s, int i, int i1) {

                                    }

                                    @Override
                                    public void onFailure(String s, OSSException e) {
                                        System.out.println("failed..");
                                        e.printStackTrace();

                                        Toast.makeText(getActivity(), "Oops..好像出错了，再试一次？",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });


                    }
                }
                break;
        }
    }
}
