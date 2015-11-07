package com.jiubai.taskmoment.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.ui.Aty_CheckPicture;
import com.jiubai.taskmoment.ui.Aty_PersonalTimeline;
import com.jiubai.taskmoment.view.RippleView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**
 * 他人个人信息适配器
 */
public class Adpt_PersonalInfo extends BaseAdapter{

    private ArrayList<String> itemList;
    private Context context;
    private String mid;
    private String nickname;

    public Adpt_PersonalInfo(Context context, String mid, String nickname) {
        if (itemList == null) {
            itemList = new ArrayList<>();
        }

        itemList.clear();

        itemList.add("昵称");
        itemList.add("ta发布的任务");
        itemList.add("ta参与的任务");
        itemList.add("ta审核的任务");

        this.context = context;
        this.mid = mid;
        this.nickname = nickname;
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
            if (!"".equals(nickname) && !"null".equals(nickname)) {
                tv_nickname.setText(nickname);
            }

            final ImageView iv_portrait = (ImageView) convertView.findViewById(R.id.iv_portrait);
            ImageLoader.getInstance().displayImage(
                    Urls.MEDIA_CENTER_PORTRAIT + mid + ".jpg", iv_portrait);

            iv_portrait.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ArrayList<String> picture = new ArrayList<String>();
                    picture.add(Urls.MEDIA_CENTER_PORTRAIT + mid + ".jpg");

                    Intent intent = new Intent(context, Aty_CheckPicture.class);
                    intent.putExtra("pictureList", picture);
                    intent.putExtra("fromWhere", "net");

                    context.startActivity(intent);
                    ((Activity)context).overridePendingTransition(
                            R.anim.zoom_in_quick, R.anim.scale_stay);
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
                                // ta审核的任务
                                intent.putExtra("isAudit", true);
                            } else if (position == 2) {
                                // ta发布的任务
                                intent.putExtra("isInvolved", true);
                            }
                            intent.putExtra("mid", mid);
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
}
