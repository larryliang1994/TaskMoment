package com.jiubai.taskmoment.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.bean.Member;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.widget.RippleView;
import com.jiubai.taskmoment.net.VolleyUtil;
import com.jiubai.taskmoment.common.UtilBox;
import com.jiubai.taskmoment.view.activity.PersonalTimelineActivity;
import com.jiubai.taskmoment.view.fragment.MemberFragment;
import com.jiubai.taskmoment.zxing.activity.CaptureActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * 成员管理的ListView适配器
 */
public class MemberAdapter extends BaseAdapter {
    public static List<Member> memberList;
    private Context context;
    public static boolean isEmpty = false;
    private Fragment fragment;

    public MemberAdapter(Context context, String memberInfo, Fragment fragment) {
        try {
            this.context = context;

            this.fragment = fragment;

            memberList = new ArrayList<>();
            memberList.add(new Member("", "", "", ""));

            JSONObject memberJson = new JSONObject(memberInfo);

            if (!"null".equals(memberJson.getString("info"))) {
                isEmpty = false;
                JSONArray memberArray = memberJson.getJSONArray("info");

                for (int i = 0; i < memberArray.length(); i++) {
                    JSONObject obj = new JSONObject(memberArray.getString(i));
                    memberList.add(new Member(obj.getString("real_name"),
                            obj.getString("mobile"), obj.getString("id"), obj.getString("mid")));
                }
            } else {
                isEmpty = true;
                memberList.add(new Member("暂无成员", "", "", ""));
            }

            memberList.add(new Member("", "", "", ""));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public interface GetMemberCallBack {
        void successCallback();

        void failedCallback();
    }

    /**
     * 获取成员列表
     */
    public static void getMember(final Context context, final GetMemberCallBack callBack) {
        if (MemberAdapter.memberList != null && MemberAdapter.memberList.isEmpty()) {
            callBack.successCallback();
            return;
        }

        if (!Config.IS_CONNECTED) {
            callBack.failedCallback();
            Toast.makeText(context, R.string.cant_access_network,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        VolleyUtil.requestWithCookie(Urls.GET_MEMBER + Config.CID, null, null,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject responseJson = new JSONObject(response);

                            String responseStatus = responseJson.getString("status");

                            if (Constants.SUCCESS.equals(responseStatus)) {
                                MemberAdapter.memberList = new ArrayList<>();
                                MemberAdapter.memberList.add(new Member("", "", "", ""));

                                JSONObject memberJson = new JSONObject(response);

                                if (!"null".equals(memberJson.getString("info"))) {
                                    JSONArray memberArray = memberJson.getJSONArray("info");

                                    for (int i = 0; i < memberArray.length(); i++) {
                                        JSONObject obj
                                                = new JSONObject(memberArray.getString(i));
                                        Member member = new Member(
                                                obj.getString("real_name"),
                                                obj.getString("mobile"),
                                                obj.getString("id"),
                                                obj.getString("mid"));

                                        MemberAdapter.memberList.add(member);
                                    }

                                    MemberAdapter.memberList.add(new Member("", "", "", ""));
                                }

                                callBack.successCallback();
                            } else {
                                callBack.failedCallback();
                                Toast.makeText(context,
                                        "获取成员列表失败",
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
                        callBack.failedCallback();
                        Toast.makeText(context,
                                "获取成员列表失败",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getCount() {
        return memberList.size();
    }

    @Override
    public Object getItem(int position) {
        return memberList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (position == 0) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_head, null);
            TextView tv = (TextView) convertView.findViewById(R.id.tv_item_head);
            tv.setText("成员管理");
        } else if (position == memberList.size() - 1) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_footer, null);
            TextView tv = (TextView) convertView.findViewById(R.id.tv_item_footer);
            tv.setText("添加成员");

            RippleView rv = (RippleView) convertView.findViewById(R.id.rv_item_footer);
            rv.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
                @Override
                public void onComplete(RippleView rippleView) {
                    final View contentView = ((Activity) context).getLayoutInflater()
                            .inflate(R.layout.dialog_input, null);

                    final MaterialDialog dialog = new MaterialDialog(context);
                    dialog.setPositiveButton("添加", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new Handler().post(new Runnable() {
                                @Override
                                public void run() {
                                    if (!Config.IS_CONNECTED) {
                                        Toast.makeText(context,
                                                R.string.cant_access_network,
                                                Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    String mobile = ((EditText) contentView
                                            .findViewById(R.id.edt_input))
                                            .getText().toString();

                                    if (!UtilBox.isTelephoneNumber(mobile)) {
                                        TextView tv = (TextView) contentView
                                                .findViewById(R.id.tv_input);
                                        tv.setVisibility(View.VISIBLE);
                                        tv.setText("请输入11位手机号");

                                        return;
                                    }

                                    String[] key = {"mobile", "cid"};
                                    String[] value = {mobile, Config.CID};

                                    VolleyUtil.requestWithCookie(Urls.ADD_MEMBER, key, value,
                                            new Response.Listener<String>() {
                                                @Override
                                                public void onResponse(String response) {
                                                    String result = createMemberCheck(response);
                                                    if (result != null && "成功".equals(result)) {
                                                        dialog.dismiss();
                                                    } else if (result != null) {
                                                        TextView tv = (TextView) contentView
                                                                .findViewById(R.id.tv_input);
                                                        tv.setVisibility(View.VISIBLE);
                                                        tv.setText(result);
                                                    }
                                                }
                                            },
                                            new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError volleyError) {
                                                    Toast.makeText(context,
                                                            "创建失败，请重试",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            });
                        }
                    });
                    dialog.setNegativeButton("扫码", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();

                            fragment.startActivityForResult(new Intent(context, CaptureActivity.class),
                                    Constants.CODE_QR_ADD_MEMBER);
                        }
                    });

                    dialog.setContentView(contentView)
                            .setCanceledOnTouchOutside(true)
                            .show();

                    contentView.requestFocus();
                }
            });
        } else {
            // TODO 使用传统的ViewHolder判空有问题，但必须使用，否则有性能隐患
            ViewHolder holder;
            convertView = LayoutInflater.from(context).inflate(R.layout.item_body_member, null);

            holder = new ViewHolder();
            holder.tv = (TextView) convertView.findViewById(R.id.tv_item_body_member);
            holder.btn = (Button) convertView.findViewById(R.id.btn_item_body_member);
            holder.rv = (RippleView) convertView.findViewById(R.id.rv_item_body_member);

            final Member member = (Member) getItem(position);

            if (!Config.MID.equals(Config.COMPANY_CREATOR)) {
                holder.btn.setVisibility(View.GONE);
            } else {
                holder.btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!Config.COMPANY_CREATOR.equals(Config.MID)) {
                            Toast.makeText(context, "只有创建者可以移除成员",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        } else if (Config.COMPANY_CREATOR.equals(member.getMid())) {
                            Toast.makeText(context, "不能移除公司创建者",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        final MaterialDialog dialog = new MaterialDialog(context);
                        dialog.setMessage("真的要移除该成员吗？")
                                .setPositiveButton("真的", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (!Config.IS_CONNECTED) {
                                            Toast.makeText(context,
                                                    R.string.cant_access_network,
                                                    Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        dialog.dismiss();

                                        String[] key = {"id", "mid"};
                                        String[] value = {member.getId(), member.getMid()};

                                        VolleyUtil.requestWithCookie(Urls.DELETE_MEMBER, key, value,
                                                new Response.Listener<String>() {
                                                    @Override
                                                    public void onResponse(String response) {
                                                        deleteCheck(position, response);
                                                    }
                                                },
                                                new Response.ErrorListener() {
                                                    @Override
                                                    public void onErrorResponse(VolleyError volleyError) {
                                                        Toast.makeText(context,
                                                                "删除失败，请重试",
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                })
                                .setNegativeButton("假的", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                    }
                                })
                                .setCanceledOnTouchOutside(true)
                                .show();
                    }
                });
            }

            holder.rv.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
                @Override
                public void onComplete(RippleView rippleView) {
                    Intent intent = new Intent(context, PersonalTimelineActivity.class);
                    intent.putExtra("mid", member.getMid());
                    context.startActivity(intent);
                    ((Activity) context).overridePendingTransition(
                            R.anim.in_right_left, R.anim.scale_stay);
                }
            });

            if (!"null".equals(member.getName()) && !"".equals(member.getName())) {
                holder.tv.setText(member.getName());
            } else {
                holder.tv.setText(member.getMobile());
            }

            if (isEmpty) {
                holder.btn.setVisibility(View.GONE);
            }
        }

        return convertView;
    }

    /**
     * 检查添加成员返回的json
     *
     * @param result 请求结果
     * @return 返回的信息内容
     */
    private String createMemberCheck(String result) {
        try {
            JSONObject json = new JSONObject(result);
            String status = json.getString("status");

            if (Constants.SUCCESS.equals(status)) {
                Toast.makeText(context, "添加成功", Toast.LENGTH_SHORT).show();

                // 插到倒数第二个位置
                memberList.add(memberList.size() - 1, new Member(json.getString("real_name"),
                        json.getString("mobile"), json.getString("id"), json.getString("mid")));

                notifyDataSetChanged();
                UtilBox.setListViewHeightBasedOnChildren(
                        MemberFragment.lv_member);
            }

            return json.getString("info");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 检查删除返回的json
     *
     * @param position 需要删除的成员的Position
     * @param response 通信返回的json
     */
    private void deleteCheck(int position, String response) {
        try {
            JSONObject json = new JSONObject(response);
            String status = json.getString("status");

            if ("1".equals(status) || "900001".equals(status)) {
                memberList.remove(position);

                notifyDataSetChanged();
                UtilBox.setListViewHeightBasedOnChildren(MemberFragment.lv_member);
                Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, json.getString("info"), Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    class ViewHolder {
        RippleView rv;
        TextView tv;
        Button btn;
    }
}