package com.jiubai.taskmoment.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.net.VolleyUtil;
import com.jiubai.taskmoment.ui.Aty_PersonalTimeline;
import com.jiubai.taskmoment.view.RippleView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * 个人中心适配器
 */
public class Adpt_UserInfo extends BaseAdapter {
    private ArrayList<String> itemList;
    private Context context;
    private Fragment fragment;

    public Adpt_UserInfo(Context context, Fragment fragment) {
        if (itemList == null) {
            itemList = new ArrayList<>();
        }

        itemList.clear();

        itemList.add("昵称");
        itemList.add("我发布的任务");
        itemList.add("我参与的任务");
        itemList.add("我审核的任务");

        this.context = context;
        this.fragment = fragment;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (position == 0) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_userinfo_head, null);

            final TextView tv_nickname = ((TextView) convertView.findViewById(R.id.tv_nickname));
            if (!"".equals(Config.NICKNAME) && !"null".equals(Config.NICKNAME)) {
                tv_nickname.setText(Config.NICKNAME);
            }

            tv_nickname.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showNicknameDialog(tv_nickname, tv_nickname.getText().toString());
                }
            });

            ImageView iv_portrait = (ImageView) convertView.findViewById(R.id.iv_portrait);

            if (Config.PORTRAIT != null) {
                ImageLoader.getInstance().displayImage(Config.PORTRAIT+ "?t=" + Config.TIME, iv_portrait);
            } else {
                iv_portrait.setImageResource(R.drawable.portrait_default);
            }

            iv_portrait.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] items = {"更换头像"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);

                            intent.setType("image/*");
                            intent.putExtra("crop", "true");
                            intent.putExtra("scale", true);
                            intent.putExtra("return-data", true);
                            intent.putExtra("outputFormat",
                                    Bitmap.CompressFormat.JPEG.toString());
                            intent.putExtra("noFaceDetection", true);

                            // 裁剪框比例
                            intent.putExtra("aspectX", 1);
                            intent.putExtra("aspectY", 1);

                            // 输出值
                            intent.putExtra("outputX", 255);
                            intent.putExtra("outputY", 255);

                            fragment.startActivityForResult(
                                    intent, Constants.CODE_CHOOSE_PORTRAIT);

                            ((Activity) context).overridePendingTransition(
                                    R.anim.in_right_left, R.anim.out_right_left);
                        }
                    })
                            .setCancelable(true);

                    Dialog dialog = builder.create();
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.show();
                }
            });
        } else {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_body, null);
            ((TextView) convertView.findViewById(R.id.tv_item_body))
                    .setText(itemList.get(position));

            ((RippleView) convertView.findViewById(R.id.rv_item_body)).setOnRippleCompleteListener(
                    new RippleView.OnRippleCompleteListener() {
                        @Override
                        public void onComplete(RippleView rippleView) {
                            Intent intent = new Intent(context, Aty_PersonalTimeline.class);
                            if (position == 3) {
                                // 我审核的任务
                                intent.putExtra("isAudit", true);
                            } else if (position == 2) {
                                // 我发布的任务
                                intent.putExtra("isInvolved", true);
                            }
                            intent.putExtra("mid", Config.MID);
                            context.startActivity(intent);
                            ((Activity) context).overridePendingTransition(
                                    R.anim.in_right_left, R.anim.out_right_left);
                        }
                    });

            // 去掉最后一条分割线
            if (position == getCount() - 1) {
                convertView.findViewById(R.id.iv_item_divider).setVisibility(View.GONE);
            }
        }

        return convertView;
    }

    private void showNicknameDialog(final TextView tv_nickname, final String nickname) {
        final View contentView = ((Activity) context).getLayoutInflater()
                .inflate(R.layout.dialog_input, null);

        TextInputLayout til = (TextInputLayout) contentView.findViewById(R.id.til_input);
        til.setHint(context.getResources().getString(R.string.nickname));

        final EditText et_nickname = ((EditText) contentView
                .findViewById(R.id.edt_input));
        et_nickname.setHint(R.string.nickname);
        et_nickname.setText(nickname);
        et_nickname.setInputType(EditorInfo.TYPE_CLASS_TEXT);
        et_nickname.requestFocus();

        final MaterialDialog dialog = new MaterialDialog(context);
        dialog.setPositiveButton("完成", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (!Config.IS_CONNECTED) {
                            Toast.makeText(context, R.string.cant_access_network,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        final String newNickname = et_nickname.getText().toString();

                        if (newNickname.isEmpty() || newNickname.length() == 0) {
                            TextView tv = (TextView) contentView
                                    .findViewById(R.id.tv_input);
                            tv.setVisibility(View.VISIBLE);
                            tv.setText("昵称不能为空");

                            return;
                        } else if (newNickname.getBytes().length > 24) {
                            TextView tv = (TextView) contentView
                                    .findViewById(R.id.tv_input);
                            tv.setVisibility(View.VISIBLE);
                            tv.setText("昵称过长");

                            return;
                        } else if (newNickname.equals(nickname)) {
                            return;
                        }

                        String[] key = {"real_name"};
                        String[] value = {newNickname};

                        VolleyUtil.requestWithCookie(Urls.UPDATE_USER_INFO, key, value,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        try {
                                            JSONObject responseObject = new JSONObject(response);
                                            String status = responseObject.getString("status");
                                            if ("1".equals(status) || "900001".equals(status)) {
                                                dialog.dismiss();

                                                tv_nickname.setText(newNickname);

                                                // 发送更新昵称广播
                                                Intent intent = new Intent(Constants.ACTION_CHANGE_NICKNAME);
                                                intent.putExtra("nickname", newNickname);
                                                context.sendBroadcast(intent);

                                                SharedPreferences sp = context.getSharedPreferences(
                                                        Constants.SP_FILENAME, Context.MODE_PRIVATE);
                                                SharedPreferences.Editor editor = sp.edit();
                                                editor.putString(
                                                        Constants.SP_KEY_NICKNAME, newNickname);
                                                editor.apply();

                                                Toast.makeText(context, "修改成功",
                                                        Toast.LENGTH_SHORT).show();
                                            } else {
                                                String info = responseObject.getString("info");

                                                TextView tv = (TextView) contentView
                                                        .findViewById(R.id.tv_input);
                                                tv.setVisibility(View.VISIBLE);
                                                tv.setText(info);
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

                                        Toast.makeText(context, "修改失败，请重试",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
            }
        });
        dialog.setNegativeButton("取消", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.setContentView(contentView)
                .setCanceledOnTouchOutside(true)
                .show();
    }
}
