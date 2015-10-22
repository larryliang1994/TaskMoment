package com.jiubai.taskmoment.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.classes.News;
import com.jiubai.taskmoment.other.ClickableText;
import com.jiubai.taskmoment.other.UtilBox;
import com.jiubai.taskmoment.ui.Aty_PersonalInfo;
import com.jiubai.taskmoment.ui.Aty_TaskInfo;
import com.jiubai.taskmoment.view.RippleView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息列表适配器
 */
public class Adpt_News extends BaseAdapter {
    private Context context;
    private List<News> newsList;

    public Adpt_News(Context context, List<News> newsList) {
        if (newsList != null) {
            this.newsList = newsList;
        } else {
            this.newsList = new ArrayList<>();
        }
        this.context = context;
    }

    @Override
    public int getCount() {
        return newsList.size();
    }

    @Override
    public Object getItem(int position) {
        return newsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_news, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ((RippleView) convertView.findViewById(R.id.rv_item_news))
                .setOnRippleCompleteListener(
                        new RippleView.OnRippleCompleteListener() {
                            @Override
                            public void onComplete(RippleView rippleView) {
                                Intent intent = new Intent(context, Aty_TaskInfo.class);
                                intent.putExtra("taskID", newsList.get(position).getTaskID());

                                context.startActivity(intent);
                                ((Activity) context).overridePendingTransition(
                                        R.anim.in_right_left, R.anim.out_right_left);
                            }
                        }
                );

        final News news = newsList.get(position);

        if ("null".equals(news.getPortrait())) {
            holder.iv_portrait.setImageResource(R.drawable.portrait_default);
        } else {
            ImageLoader.getInstance().displayImage(news.getPortrait(), holder.iv_portrait);
        }

        holder.tv_content.setText(news.getContent());
        holder.tv_time.setText(UtilBox.getDateToString(news.getCreate_time(), UtilBox.TIME));

        if (!"null".equals(news.getPicture())) {
            ImageLoader.getInstance().displayImage(news.getPicture(), holder.iv_picture);
        }

        // 发送者
        final SpannableString senderSpan = new SpannableString(news.getSender());
        senderSpan.setSpan(senderSpan, 0, news.getSender().length(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        holder.tv_sender.setText(senderSpan);

        return convertView;
    }

    class ViewHolder {
        ImageView iv_portrait;
        TextView tv_sender;
        TextView tv_content;
        TextView tv_time;
        ImageView iv_picture;

        public ViewHolder(View itemView) {
            iv_portrait = (ImageView) itemView.findViewById(R.id.iv_portrait);
            tv_sender = (TextView) itemView.findViewById(R.id.tv_sender);
            tv_content = (TextView) itemView.findViewById(R.id.tv_content);
            tv_time = (TextView) itemView.findViewById(R.id.tv_time);
            iv_picture = (ImageView) itemView.findViewById(R.id.iv_picture);
        }
    }
}
