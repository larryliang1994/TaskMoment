package com.jiubai.taskmoment.adapter;

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
import com.jiubai.taskmoment.classes.Comment;
import com.jiubai.taskmoment.classes.Task;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.other.UtilBox;
import com.jiubai.taskmoment.ui.Aty_PersonalTimeline;
import com.jiubai.taskmoment.ui.Aty_TaskInfo;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 个人时间线适配器
 */
public class Adpt_PersonalTimeline extends BaseAdapter {
    public static ArrayList<Task> taskList;
    public Adpt_Comment commentAdapter;
    private Context context;

    public Adpt_PersonalTimeline(Context context, ArrayList<Task> taskList) {
        this.context = context;
        Adpt_PersonalTimeline.taskList = taskList;
    }

    public Adpt_PersonalTimeline(Context context, boolean isRefresh, String response) {
        this.context = context;

        if (isRefresh) {
            taskList = new ArrayList<>();
        }

        try {
            JSONObject taskJson = new JSONObject(response);

            if (!"null".equals(taskJson.getString("info"))) {
                JSONArray taskArray = taskJson.getJSONArray("info");

                for (int i = 0; i < taskArray.length(); i++) {
                    JSONObject obj = new JSONObject(taskArray.getString(i));

                    String id = obj.getString("id");

                    String mid = obj.getString("mid");
                    String portraitUrl = Constants.HOST_ID + "task_moment/" + mid + ".jpg";

                    String nickname = obj.getString("show_name");

                    char p1 = obj.getString("p1").charAt(0);
                    String grade = (p1 - 48) == 1 ? "S" : String.valueOf((char) (p1 + 15));

                    String desc = obj.getString("comments");
                    String executor = obj.getString("ext1");
                    String supervisor = obj.getString("ext2");
                    String auditor = obj.getString("ext3");

                    ArrayList<String> pictures = decodePictureList(obj.getString("works"));
                    ArrayList<Comment> comments
                            = decodeCommentList(taskList.size(), id, obj.getString("member_comment"));

                    long deadline = Long.valueOf(obj.getString("time1")) * 1000;
                    long publish_time = Long.valueOf(obj.getString("time2")) * 1000;
                    long create_time = Long.valueOf(obj.getString("create_time")) * 1000;

                    String audit_result = obj.getString("p2");

                    Task task = new Task(id, portraitUrl, nickname, mid, grade, desc,
                            executor, supervisor, auditor,
                            pictures, comments, deadline, publish_time, create_time, audit_result);
                    taskList.add(task);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
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

    /**
     * 将json解码成list
     *
     * @param pictures 图片Json
     * @return 图片list
     */
    private ArrayList<String> decodePictureList(String pictures) {
        ArrayList<String> pictureList = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(pictures);

            for (int i = 0; i < jsonArray.length(); i++) {
                pictureList.add(Constants.HOST_ID + jsonArray.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return pictureList;
    }

    /**
     * 将json解码成list
     *
     * @param comments 评论json
     * @return 图片List
     */
    private ArrayList<Comment> decodeCommentList(int taskPosition, String taskID, String comments) {
        ArrayList<Comment> commentList = new ArrayList<>();

        if (!"".equals(comments) && !"null".equals(comments)) {
            try {

                JSONArray jsonArray = new JSONArray(comments);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = new JSONObject(jsonArray.getString(i));

                    String sender = "null".equals(object.getString("send_real_name")) ?
                            object.getString("send_mobile") : object.getString("send_real_name");

                    String receiver = "null".equals(object.getString("receiver_real_name")) ?
                            object.getString("receiver_mobile") : object.getString("receiver_real_name");

                    if ("null".equals(receiver)) {
                        Comment comment = new Comment(taskID, taskPosition,
                                sender, object.getString("send_id"),
                                object.getString("content"),
                                Long.valueOf(object.getString("create_time")) * 1000);

                        commentList.add(comment);
                    } else {
                        Comment comment = new Comment(taskID, taskPosition,
                                sender, object.getString("send_id"),
                                receiver, object.getString("receiver_id"),
                                object.getString("content"),
                                Long.valueOf(object.getString("create_time")) * 1000);

                        commentList.add(comment);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return commentList;
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

        final Task task = taskList.get(position);
        holder.tv_nickname.setText(task.getNickname());
        holder.tv_grade.setText(task.getGrade());
        setGradeColor(holder.tv_grade, task.getGrade());
        holder.tv_desc.setText(task.getDesc());
        holder.tv_date.setText(UtilBox.getDateToString(task.getCreate_time(), UtilBox.DATE));

        ImageLoader loader = ImageLoader.getInstance();
        loader.displayImage(task.getPortraitUrl(), holder.iv_portrait);

        holder.iv_portrait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Aty_PersonalTimeline.class);
                intent.putExtra("mid", task.getMid());
                context.startActivity(intent);
                ((Activity) context).overridePendingTransition(
                        R.anim.in_right_left, R.anim.out_right_left);
            }
        });

        holder.tv_nickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Aty_PersonalTimeline.class);
                intent.putExtra("mid", task.getMid());
                context.startActivity(intent);
                ((Activity) context).overridePendingTransition(
                        R.anim.in_right_left, R.anim.out_right_left);
            }
        });

        holder.tv_desc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Aty_TaskInfo.class);
                intent.putExtra("taskID", task.getId());

                context.startActivity(intent);
                ((Activity) context).overridePendingTransition(
                        R.anim.in_right_left, R.anim.out_right_left);
            }
        });

        holder.gv_picture.setAdapter(new Adpt_TimelinePicture(context, task.getPictures()));
        UtilBox.setGridViewHeightBasedOnChildren(holder.gv_picture, true);

        if (task.getComments() == null || task.getComments().isEmpty()) {
            holder.lv_comment.setVisibility(View.GONE);
        } else {
            holder.lv_comment.setVisibility(View.VISIBLE);
            commentAdapter = new Adpt_Comment(context, task.getComments(), "personal");
            holder.lv_comment.setAdapter(commentAdapter);
            UtilBox.setListViewHeightBasedOnChildren(holder.lv_comment);
        }

        holder.btn_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] location = new int[2];
                holder.btn_comment.getLocationOnScreen(location);
                int y = location[1];

                Aty_PersonalTimeline.showCommentWindow(context, position, task.getId(),
                        "", "", y + UtilBox.dip2px(context, 15));
            }
        });

        if (Config.MID.equals(task.getAuditor()) && "1".equals(task.getAuditResult())) {
            holder.btn_audit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Aty_PersonalTimeline.showAuditWindow(context, task.getId());
                }
            });
        } else {
            holder.btn_audit.setVisibility(View.GONE);
        }

        return convertView;
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
