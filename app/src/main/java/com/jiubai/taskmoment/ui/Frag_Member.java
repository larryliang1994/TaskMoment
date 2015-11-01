package com.jiubai.taskmoment.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.jiubai.taskmoment.adapter.Adpt_Member;
import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.other.UtilBox;
import com.jiubai.taskmoment.classes.Member;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.net.VolleyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * 成员管理
 */
public class Frag_Member extends Fragment {
    private SwipeRefreshLayout srl;
    private ListView lv_member;
    private Adpt_Member adpt_member;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_member, container, false);

        initView(view);

        return view;
    }

    /**
     * 初始化界面
     */
    @SuppressLint({"JavascriptInterface", "SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void initView(View view) {
        srl = (SwipeRefreshLayout) view.findViewById(R.id.swipe_member);
        lv_member = (ListView) view.findViewById(R.id.lv_member);

        srl.setColorSchemeResources(R.color.primary);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshListView();
            }
        });

        srl.setEnabled(true);

        lv_member.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
                if (position == adpt_member.getCount() - 1) {
                    @SuppressLint("InflateParams")
                    final View contentView = getActivity().getLayoutInflater()
                            .inflate(R.layout.dialog_input, null);

                    final MaterialDialog dialog = new MaterialDialog(getActivity());
                    dialog.setPositiveButton("添加", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new Handler().post(new Runnable() {
                                @Override
                                public void run() {
                                    if (!Config.IS_CONNECTED) {
                                        Toast.makeText(getActivity(),
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
                                                    String result = createMember(response);
                                                    if (result != null && "成功".equals(result)) {
                                                        dialog.dismiss();
                                                        refreshListView();

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
                                                    Toast.makeText(getActivity(),
                                                            R.string.usual_error,
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
                            .setTitle("添加成员")
                            .setCanceledOnTouchOutside(true)
                            .show();

                    contentView.requestFocus();

                    //UtilBox.toggleSoftInput(contentView.findViewById(R.id.edt_input), true);
                } else if (!adpt_member.isEmpty && position != 0) {

                    final MaterialDialog dialog = new MaterialDialog(getActivity());
                    dialog.setTitle("删除")
                            .setMessage("真的要删除该成员吗？")
                            .setPositiveButton("真的", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (!Config.IS_CONNECTED) {
                                        Toast.makeText(getActivity(),
                                                R.string.cant_access_network,
                                                Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    dialog.dismiss();

                                    Member member = ((Member) adpt_member.getItem(position));
                                    String[] key = {"id", "mid"};
                                    String[] value = {member.getId(), member.getMid()};

                                    VolleyUtil.requestWithCookie(Urls.DELETE_MEMBER, key, value,
                                            new Response.Listener<String>() {
                                                @Override
                                                public void onResponse(String response) {
                                                    deleteCheck(response);
                                                }
                                            },
                                            new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError volleyError) {
                                                    Toast.makeText(getActivity(),
                                                            R.string.usual_error,
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

            }
        });

        // 到达顶端才能下拉刷新
        lv_member.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0)
                    srl.setEnabled(true);
                else
                    srl.setEnabled(false);
            }
        });

        // 延迟执行才能使旋转进度条显示出来
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshListView();
            }
        }, 100);
    }

    /**
     * 添加成员
     *
     * @param result 请求结果
     * @return 返回的信息内容
     */
    private String createMember(String result) {
        try {
            JSONObject json = new JSONObject(result);
            String status = json.getString("status");

            if ("1".equals(status) || "900001".equals(status)) {
                Toast.makeText(getActivity(), "添加成功", Toast.LENGTH_SHORT).show();
            }

            return json.getString("info");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 刷新ListView
     */
    private void refreshListView() {
        if (!Config.IS_CONNECTED) {
            Toast.makeText(getActivity(),
                    R.string.cant_access_network,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        srl.setRefreshing(true);

        VolleyUtil.requestWithCookie(Urls.GET_MEMBER + Config.CID, null, null,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        showMember(response);
                        UtilBox.setListViewHeightBasedOnChildren(lv_member);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        srl.setRefreshing(false);
                        Toast.makeText(getActivity(),
                                R.string.usual_error_refresh,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 显示成员
     *
     * @param response 通信返回的json
     */
    private void showMember(String response) {
        try {
            JSONObject responseJson = new JSONObject(response);

            String responseStatus = responseJson.getString("status");

            if ("1".equals(responseStatus) || "900001".equals(responseStatus)) {
                adpt_member = new Adpt_Member(getActivity(), response);
                lv_member.setAdapter(adpt_member);
            } else {
                Toast.makeText(getActivity(),
                        R.string.usual_error_refresh,
                        Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                srl.setRefreshing(false);
            }
        }, 1000);
    }

    /**
     * 检查删除返回的json
     *
     * @param response 通信返回的json
     */
    private void deleteCheck(String response) {
        try {
            JSONObject json = new JSONObject(response);
            String status = json.getString("status");

            if ("1".equals(status) || "900001".equals(status)) {
                refreshListView();
                Toast.makeText(getActivity(), "删除成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(),
                        R.string.usual_error,
                        Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
