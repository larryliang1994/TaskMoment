package com.jiubai.taskmoment.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jiubai.taskmoment.R;
import com.jiubai.taskmoment.other.UtilBox;
import com.jiubai.taskmoment.classes.Task;
import com.jiubai.taskmoment.ui.Aty_PersonalInfo;
import com.jiubai.taskmoment.ui.Frag_Timeline;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Timeline中RecyclerView的适配器
 */
public class Adpt_Timeline extends BaseAdapter {
    public List<Task> taskList;
    private Context context;

    public Adpt_Timeline(Context context, List<Task> taskList) {
        this.context = context;

        this.taskList = new ArrayList<>();
        this.taskList = taskList;

//        taskList = new ArrayList<>();
//
//        JSONObject taskJson = new JSONObject(response);
//
//        try {
//            if (!"null".equals(taskJson.getString("info"))) {
//                JSONArray taskArray = taskJson.getJSONArray("info");
//
//                for (int i = 0; i < taskArray.length(); i++) {
//                    JSONObject obj = new JSONObject(taskArray.getString(i));
//                    Task task = new Task();
//                    taskList.add(task);
//                }
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public int getCount() {
        return taskList.size();
    }

    @Override
    public Object getItem(int position) {
        return taskList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_timeline, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Task task = taskList.get(position);
        holder.tv_nickname.setText(task.getNickname());
        holder.tv_grade.setText(task.getGrade());
        setGradeColor(holder.tv_grade, task.getGrade());
        holder.tv_desc.setText(task.getDesc());
        holder.tv_date.setText(task.getDate());

        ImageLoader loader = ImageLoader.getInstance();
        loader.displayImage(task.getPortraitUrl(), holder.iv_portrait);

        holder.iv_portrait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Aty_PersonalInfo.class);
                intent.putExtra("name", "Leung_Howell");
                context.startActivity(intent);
                ((Activity) context).overridePendingTransition(
                        R.anim.in_right_left, R.anim.out_right_left);
            }
        });

        holder.tv_nickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Aty_PersonalInfo.class);
                intent.putExtra("name", "Leung_Howell");
                context.startActivity(intent);
                ((Activity) context).overridePendingTransition(
                        R.anim.in_right_left, R.anim.out_right_left);
            }
        });

        holder.gv_picture.setAdapter(new Adpt_TimelinePicture(context, task.getPictures()));
        UtilBox.setGridViewHeightBasedOnChildren(holder.gv_picture, true);

        holder.lv_comment.setAdapter(new Adpt_Comment(context, task.getComments()));
        UtilBox.setListViewHeightBasedOnChildren(holder.lv_comment);

        holder.btn_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Frag_Timeline.showCommentWindow(null, null, null, null);
            }
        });

        holder.btn_audit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Frag_Timeline.showAuditWindow(null, null);
            }
        });

        return convertView;
    }

    /**
     * 设置任务等级的颜色
     *
     * @param tv_grade 需要设置的TextView
     * @param grade    级别
     */
    private void setGradeColor(TextView tv_grade, String grade) {
        switch (grade) {
            case "S":
                tv_grade.setTextColor(context.getResources().getColor(R.color.S));
                break;

            case "A":
                tv_grade.setTextColor(context.getResources().getColor(R.color.A));
                break;

            case "B":
                tv_grade.setTextColor(context.getResources().getColor(R.color.B));
                break;

            case "C":
                tv_grade.setTextColor(context.getResources().getColor(R.color.C));
                break;

            case "D":
                tv_grade.setTextColor(context.getResources().getColor(R.color.D));
                break;
        }
    }

    public static class ViewHolder {

        public ImageView iv_portrait;
        public TextView tv_nickname;
        public TextView tv_grade;
        public TextView tv_desc;
        public GridView gv_picture;
        public TextView tv_date;
        public ListView lv_comment;
        public Button btn_comment;
        public Button btn_audit;

        public ViewHolder(View itemView) {
            iv_portrait = (ImageView) itemView.findViewById(R.id.iv_item_portrait);
            tv_nickname = (TextView) itemView.findViewById(R.id.tv_item_nickname);
            tv_grade = (TextView) itemView.findViewById(R.id.tv_item_grade);
            tv_desc = (TextView) itemView.findViewById(R.id.tv_item_desc);
            gv_picture = (GridView) itemView.findViewById(R.id.gv_item_picture);
            tv_date = (TextView) itemView.findViewById(R.id.tv_item_date);
            lv_comment = (ListView) itemView.findViewById(R.id.lv_item_comment);
            btn_comment = (Button) itemView.findViewById(R.id.btn_item_comment);
            btn_audit = (Button) itemView.findViewById(R.id.btn_item_audit);
        }
    }
}
