package com.jiubai.taskmoment.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
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

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.view.RippleView;
import com.nostra13.universalimageloader.core.ImageLoader;

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
                ImageLoader.getInstance().displayImage(Config.PORTRAIT, iv_portrait);
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

                        }
                    });

            if (position == getCount() - 1) {
                convertView.findViewById(R.id.iv_item_body).setVisibility(View.GONE);
            }
        }

        return convertView;
    }

    private void showNicknameDialog(final TextView tv_nickname, String nickname) {
        final View contentView = ((Activity) context).getLayoutInflater()
                .inflate(R.layout.dialog_input, null);

        final EditText et_nickname = ((EditText) contentView
                .findViewById(R.id.edt_input));
        et_nickname.setText(nickname);
        et_nickname.setHint(R.string.nickname);
        et_nickname.setInputType(EditorInfo.TYPE_CLASS_TEXT);

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
                        }

                        // 到时候删掉这两行
                        tv_nickname.setText(newNickname);
                        dialog.dismiss();

//                        String[] key = {"mobile", "cid"};
//                        String[] value = {newNickname, Config.CID};

//                        VolleyUtil.requestWithCookie(Urls.CHANGE_NICKNAME, key, value,
//                                new Response.Listener<String>() {
//                                    @Override
//                                    public void onResponse(String response) {
//                                        try {
//                                            JSONObject responseObject = new JSONObject(response);
//                                            String status = responseObject.getString("status");
//                                            if ("1".equals(status) || "900001".equals(status)) {
//                                                dialog.dismiss();
//
//                                                tv_nickname.setText(newNickname);
//
//                                                Toast.makeText(context, "修改成功",
//                                                        Toast.LENGTH_SHORT).show();
//                                            } else {
//                                                String info = responseObject.getString("info");
//
//                                                TextView tv = (TextView) contentView
//                                                        .findViewById(R.id.tv_input);
//                                                tv.setVisibility(View.VISIBLE);
//                                                tv.setText(info);
//                                            }
//                                        } catch (JSONException e) {
//                                            e.printStackTrace();
//                                        }
//                                    }
//                                },
//                                new Response.ErrorListener() {
//                                    @Override
//                                    public void onErrorResponse(VolleyError volleyError) {
//                                        Toast.makeText(context, R.string.usual_error,
//                                                Toast.LENGTH_SHORT).show();
//                                    }
//                                });
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
