package com.jiubai.taskmoment.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
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
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.aliyun.mbaas.oss.callback.SaveCallback;
import com.aliyun.mbaas.oss.model.OSSException;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.jiubai.taskmoment.adapter.Adpt_Timeline;
import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.classes.Comment;
import com.jiubai.taskmoment.other.UtilBox;
import com.jiubai.taskmoment.classes.Task;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.net.OssUtil;
import com.jiubai.taskmoment.net.VolleyUtil;
import com.jiubai.taskmoment.view.BorderScrollView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * 时间线（任务圈）
 */
public class Frag_Timeline extends Fragment
        implements View.OnClickListener {

    private SwipeRefreshLayout srl;
    private static ListView lv;
    public static LinearLayout ll_comment;
    private static LinearLayout ll_audit;
    private static Adpt_Timeline adapter;
    private static BorderScrollView sv;
    public static Space space;
    private boolean isBottomRefleshing = false;
    private ImageView iv_companyBackground;
    private ImageView iv_news_portrait;
    private Uri imageUri = Uri.parse(Constants.TEMP_FILE_LOCATION); // 用于存放背景图
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

        if (!"".equals(Config.NICKNAME) && !"null".equals(Config.NICKNAME)) {
            ((TextView) view.findViewById(R.id.tv_timeline_nickname)).setText(Config.NICKNAME);
        }

        if (Config.PORTRAIT != null) {
            ImageLoader.getInstance().displayImage(Config.PORTRAIT, iv_portrait);
        } else {
            iv_portrait.setImageResource(R.drawable.portrait_default);
        }

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

        final View footView = LayoutInflater.from(getActivity()).inflate(R.layout.load_more, null);

        lv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (commentWindowIsShow) {
                    UtilBox.setViewParams(space, 0, 0);

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

        sv = (BorderScrollView) view.findViewById(R.id.sv_timeline);
        sv.setOnBorderListener(new BorderScrollView.OnBorderListener() {

            @Override
            public void onTop() {
                // may be done multi times, u should control it
                //Toast.makeText(getActivity(), "has reached top", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBottom() {
                if (!isBottomRefleshing) {
                    isBottomRefleshing = true;

                    //lv.addFooterView(footView);


                    isBottomRefleshing = false;
                }

                // may be done multi times, u should control it
                //Toast.makeText(getActivity(), "has reached bottom", Toast.LENGTH_SHORT).show();
            }
        });

        space = (Space) view.findViewById(R.id.space);

        ll_comment = (LinearLayout) view.findViewById(R.id.ll_comment);
        ll_audit = (LinearLayout) view.findViewById(R.id.ll_audit);

        LinearLayout ll_news = (LinearLayout) view.findViewById(R.id.ll_news);
        ll_news.setOnClickListener(this);

        GradientDrawable newsBgShape = (GradientDrawable) ll_news.getBackground();
        newsBgShape.setColor(getResources().getColor(R.color.news));

        iv_companyBackground = (ImageView) view.findViewById(R.id.iv_companyBackground);
        iv_companyBackground.setOnClickListener(this);
        if (Config.COMPANY_BACKGROUND != null) {
            ImageLoader.getInstance().displayImage(
                    Config.COMPANY_BACKGROUND, iv_companyBackground);
        }

        iv_news_portrait = (ImageView) view.findViewById(R.id.iv_news_portrait);
        ImageLoader.getInstance().displayImage(Config.PORTRAIT, iv_news_portrait);

        Aty_Main.toolbar.findViewById(R.id.iBtn_publish).setOnClickListener(this);

        if (Adpt_Timeline.taskList != null && !Adpt_Timeline.taskList.isEmpty()) {
            adapter = new Adpt_Timeline(getActivity(), Adpt_Timeline.taskList);
            lv.setAdapter(adapter);
        }

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
            Toast.makeText(getActivity(),
                    R.string.cant_access_network,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        srl.setRefreshing(true);

        String request_time = Calendar.getInstance(Locale.CHINA).getTimeInMillis() + "";

        String[] key = {"len", "cid", "create_time"};
        String[] value = {"30", Config.CID, request_time};

        VolleyUtil.requestWithCookie(Urls.GET_TASK_LIST, key, value,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        JSONObject responseJson;
                        try {
                            responseJson = new JSONObject(response);

                            String responseStatus = responseJson.getString("status");

                            if ("1".equals(responseStatus) || "900001".equals(responseStatus)) {

                                adapter = new Adpt_Timeline(getActivity(), response);
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

                            } else {
                                srl.setRefreshing(false);
                                System.out.println(responseJson);
                                Toast.makeText(getActivity(),
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
                        srl.setRefreshing(false);
                        volleyError.printStackTrace();
                        Toast.makeText(getActivity(),
                                "Oops...好像出错了，再试一次吧？",
                                Toast.LENGTH_SHORT).show();
                    }
                }

        );
    }

    /**
     * 弹出评论窗口
     *
     * @param context    上下文
     * @param taskID     任务ID
     * @param sender     发送者
     * @param receiver   接收者
     * @param receiverID 接收者ID
     */
    @SuppressWarnings("unused")
    public static void showCommentWindow(final Context context, final int position,
                                         final String taskID, String sender,
                                         final String receiver, final String receiverID, final int y) {
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
                                            Adpt_Timeline.taskList.get(position).getComments().add(
                                                    new Comment(taskID, position,
                                                            Config.NICKNAME, Config.MID,
                                                            receiver, receiverID,
                                                            edt_content.getText().toString(),
                                                            Calendar.getInstance(Locale.CHINA)
                                                                    .getTimeInMillis()));
                                        } else {
                                            Adpt_Timeline.taskList.get(position).getComments().add(
                                                    new Comment(taskID, position,
                                                            Config.NICKNAME, Config.MID,
                                                            edt_content.getText().toString(),
                                                            Calendar.getInstance(Locale.CHINA)
                                                                    .getTimeInMillis()));
                                        }
                                        adapter.notifyDataSetChanged();
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
     *
     * @param sender   发送者
     * @param senderID 发送者ID
     */
    @SuppressWarnings("unused")
    public static void showAuditWindow(String sender, String senderID) {
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_companyBackground:
                String[] items = {"更换公司封面"};

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);

                        intent.setType("image/*");
                        intent.putExtra("crop", "true");
                        intent.putExtra("scale", true);
                        intent.putExtra("return-data", false);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        intent.putExtra("output", imageUri);
                        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                        intent.putExtra("noFaceDetection", true);

                        int standardWidth = UtilBox.getWidthPixels(getActivity());
                        int standardHeight = UtilBox.dip2px(getActivity(), 270);

                        // 裁剪框比例
                        intent.putExtra("aspectX", standardWidth);
                        intent.putExtra("aspectY", standardHeight);

                        // 输出值
                        intent.putExtra("outputX", standardWidth);
                        intent.putExtra("outputY", standardHeight);

                        startActivityForResult(intent, Constants.CODE_CROP_PICTURE);

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

            case R.id.iBtn_publish:
                startActivityForResult(
                        new Intent(getActivity(), Aty_TaskPublish.class),
                        Constants.CODE_PUBLISH_TASK);
                getActivity().overridePendingTransition(R.anim.in_right_left,
                        R.anim.out_right_left);
                break;

            case R.id.ll_news:
                startActivity(new Intent(getActivity(), Aty_News.class));
                getActivity().overridePendingTransition(R.anim.in_right_left,
                        R.anim.out_right_left);
                break;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.CODE_CROP_PICTURE:
                if (resultCode == Activity.RESULT_OK) {
                    if (!Config.IS_CONNECTED) {
                        Toast.makeText(getActivity(),
                                R.string.cant_access_network,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (imageUri != null) {
                        try {
                            final Bitmap bitmap = BitmapFactory.decodeStream(
                                    getActivity().getContentResolver().openInputStream(imageUri));
                            iv_companyBackground.setImageBitmap(bitmap);

                            final String objectName = "task_moment/" + Config.CID + ".jpg";

                            OssUtil.uploadImage(
                                    UtilBox.compressImage(bitmap, Constants.SIZE_COMPANY_BACKGROUND),
                                    objectName,
                                    new SaveCallback() {
                                        @Override
                                        public void onSuccess(String objectKey) {
                                            Config.COMPANY_BACKGROUND = Constants.HOST_ID + objectKey;

                                            SharedPreferences sp = getActivity()
                                                    .getSharedPreferences("config",
                                                            Context.MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sp.edit();
                                            editor.putString(Constants.SP_KEY_COMPANY_BACKGROUND,
                                                    Config.COMPANY_BACKGROUND);
                                            editor.apply();
                                        }

                                        @Override
                                        public void onProgress(String objectKey, int i, int i1) {

                                        }

                                        @Override
                                        public void onFailure(String objectKey, OSSException e) {
                                            System.out.println(objectKey + " failed..");
                                            e.printStackTrace();

                                            Toast.makeText(getActivity(),
                                                    R.string.usual_error,
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });

//                            String[] key = {};
//                            String[] value = {};
//
//                            VolleyUtil.requestWithCookie(Urls.UPLOAD_COMPANY_BACKGROUND,
//                                    key, value,
//                                    new Response.Listener<String>() {
//                                        @Override
//                                        public void onResponse(String response) {
//
//                                        }
//                                    },
//                                    new Response.ErrorListener() {
//                                        @Override
//                                        public void onErrorResponse(VolleyError volleyError) {
//                                            volleyError.printStackTrace();
//
//                                            Toast.makeText(getActivity(),
//                                                    R.string.usual_error,
//                                                    Toast.LENGTH_SHORT).show();
//                                        }
//                                    });
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;

            case Constants.CODE_PUBLISH_TASK:
                if (resultCode == Activity.RESULT_OK) {
                    String grade = data.getStringExtra("grade");
                    String content = data.getStringExtra("content");
                    long create_time = data.getLongExtra("create_time", 0);

                    ArrayList<String> pictureList = data.getStringArrayListExtra("pictureList");
                    pictureList.remove(pictureList.size() - 1);

                    adapter.taskList.add(0, new Task("0",
                            Constants.HOST_ID + "task_moment/" + Config.MID + ".jpg",
                            "Howell", grade, content,
                            null, null, null,
                            pictureList, null, 0, 0, create_time, "1"));

                    adapter.notifyDataSetChanged();
                }
                break;
        }
    }
}
