package com.jiubai.taskmoment.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
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
import com.jiubai.taskmoment.bean.Comment;
import com.jiubai.taskmoment.bean.Member;
import com.jiubai.taskmoment.bean.Task;
import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;
import com.jiubai.taskmoment.config.Urls;
import com.jiubai.taskmoment.common.UtilBox;
import com.jiubai.taskmoment.presenter.IUploadImagePresenter;
import com.jiubai.taskmoment.view.activity.PersonalTimelineActivity;
import com.jiubai.taskmoment.view.activity.TaskInfoActivity;
import com.jiubai.taskmoment.view.fragment.TimelineFragment;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Timeline的适配器
 */
public class TimelineAdapter extends BaseAdapter {
    public static ArrayList<Task> taskList;
    public CommentAdapter commentAdapter;
    private Context context;
    private IUploadImagePresenter uploadImagePresenter;
    public static boolean init = false; // true表示正在初始化

    public TimelineAdapter(Context context) {
        this.context = context;
        if (taskList == null) {
            taskList = new ArrayList<>();
        }

        taskList.clear();

        init = true;
    }

    public TimelineAdapter(Context context, boolean isRefresh,
                           String response, IUploadImagePresenter uploadImagePresenter) {
        this.context = context;

        if (isRefresh) {
            taskList = new ArrayList<>();
            taskList.clear();
        }

        if (uploadImagePresenter != null) {
            this.uploadImagePresenter = uploadImagePresenter;
        }

        try {
            JSONObject taskJson = new JSONObject(response);

            if (!"null".equals(taskJson.getString("info"))) {
                JSONArray taskArray = taskJson.getJSONArray("info");

                for (int i = 0; i < taskArray.length(); i++) {

                    JSONObject obj = new JSONObject(taskArray.getString(i));

                    String id = obj.getString("id");

                    String mid = obj.getString("mid");
                    String portraitUrl = Urls.MEDIA_CENTER_PORTRAIT + mid + ".jpg";

                    String nickname = obj.getString("show_name");

                    char p1 = obj.getString("p1").charAt(0);
                    String grade = (p1 - 48) == 1 ? "S" : String.valueOf((char) (p1 + 15));

                    String desc = obj.getString("comments");
                    String executor = obj.getString("ext1");
                    String supervisor = obj.getString("ext2");
                    String auditor = obj.getString("ext3");

                    int taskState;

                    ArrayList<String> pictures;

                    // 如果有任务附图上传中，先显示本地图片
                    if (id.equals(TimelineFragment.taskID) && TimelineFragment.pictureList != null
                            && !TimelineFragment.pictureList.isEmpty()) {
                        pictures = TimelineFragment.pictureList;
                        taskState = Task.SENDING;
                    } else {
                        pictures = decodePictureList(obj.getString("works"));
                        taskState = Task.SUCCESS;
                    }

                    ArrayList<Comment> comments
                            = decodeCommentList(id, obj.getString("member_comment"));

                    long deadline = Long.valueOf(obj.getString("time1")) * 1000;
                    long publish_time = Long.valueOf(obj.getString("time2")) * 1000;
                    long create_time = Long.valueOf(obj.getString("create_time")) * 1000;

                    String audit_result = obj.getString("p2");

                    taskList.add(new Task(id, portraitUrl, nickname, mid, grade, desc,
                            executor, supervisor, auditor,
                            pictures, comments, deadline, publish_time, create_time,
                            audit_result, taskState));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
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

    @SuppressLint({"InflateParams", "SetTextI18n"})
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (taskList == null || taskList.isEmpty()) {
            return null;
        }

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

        ImageLoader.getInstance().displayImage(
                UtilBox.getThumbnailImageName(task.getPortraitUrl(),
                        UtilBox.dip2px(context, 45),
                        UtilBox.dip2px(context, 45))
                        + "?t=" + Config.TIME, holder.iv_portrait);

        holder.iv_portrait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PersonalTimelineActivity.class);
                intent.putExtra("mid", task.getMid());
                context.startActivity(intent);
                ((Activity) context).overridePendingTransition(
                        R.anim.in_right_left, R.anim.scale_stay);
            }
        });

        holder.tv_nickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PersonalTimelineActivity.class);
                intent.putExtra("mid", task.getMid());
                context.startActivity(intent);
                ((Activity) context).overridePendingTransition(
                        R.anim.in_right_left, R.anim.scale_stay);
            }
        });

        holder.tv_desc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.tv_desc.setBackgroundColor(
                        ContextCompat.getColor(context, R.color.gray));

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        holder.tv_desc.setBackgroundColor(
                                ContextCompat.getColor(context, R.color.transparent));
                    }
                }, 100);

                Intent intent = new Intent(context, TaskInfoActivity.class);
                intent.putExtra("task", task);
                intent.putExtra("taskPosition", position);

                context.startActivity(intent);
                ((Activity) context).overridePendingTransition(
                        R.anim.in_right_left, R.anim.scale_stay);
            }
        });

        holder.gv_picture.setAdapter(new TimelinePictureAdapter(context, task.getPictures()));
        UtilBox.setGridViewHeightBasedOnChildren(holder.gv_picture, true);

        if (task.getComments() == null || task.getComments().isEmpty()) {
            holder.lv_comment.setVisibility(View.GONE);
        } else {
            holder.lv_comment.setVisibility(View.VISIBLE);
            commentAdapter = new CommentAdapter(context, task.getComments(), "timeline");
            holder.lv_comment.setAdapter(commentAdapter);
            UtilBox.setListViewHeightBasedOnChildren(holder.lv_comment);
        }

        holder.btn_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] location = new int[2];
                holder.btn_comment.getLocationOnScreen(location);
                int y = location[1];

                TimelineFragment.showCommentWindow(context, task.getId(),
                        "", "", y + UtilBox.dip2px(context, 15));
            }
        });

        if (Config.MID.equals(task.getAuditor()) && "1".equals(task.getAuditResult())) {
            holder.btn_audit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TimelineFragment.showAuditWindow(context, task.getId());
                }
            });
        } else {
            holder.btn_audit.setVisibility(View.GONE);
        }

        // 设置执行者、监督者、审核者
        for (int i = 1; i < MemberAdapter.memberList.size() - 1; i++) {
            Member member = MemberAdapter.memberList.get(i);

            if (task.getExecutor().equals(member.getMid())) {
                String executor;
                if (!"null".equals(member.getName()) && !"".equals(member.getName())) {
                    executor = member.getName();
                } else {
                    executor = member.getMobile().substring(0, 3)
                            + "****" + member.getMobile().substring(7);
                }
                holder.tv_executor.setText(context.getResources().getString(R.string._executor) + executor);
            }

            if (task.getSupervisor().equals(member.getMid())) {
                String supervisor;
                if (!"null".equals(member.getName()) && !"".equals(member.getName())) {
                    supervisor = member.getName();
                } else {
                    supervisor = member.getMobile().substring(0, 3)
                            + "****" + member.getMobile().substring(7);
                }
                holder.tv_supervisor.setText(context.getResources().getString(R.string._supervisor) + supervisor);
            }

            if (task.getAuditor().equals(member.getMid())) {
                String auditor;
                if (!"null".equals(member.getName()) && !"".equals(member.getName())) {
                    auditor = member.getName();
                } else {
                    auditor = member.getMobile().substring(0, 3)
                            + "****" + member.getMobile().substring(7);
                }
                holder.tv_auditor.setText(context.getResources().getString(R.string._auditor) + auditor);
            }
        }

        holder.tv_deadline.setText(context.getResources().getString(R.string._deadline)
                + UtilBox.getDateToString(task.getDeadline(), UtilBox.DATE_TIME));

        holder.tv_startTime.setText(context.getResources().getString(R.string._startTime)
                + UtilBox.getDateToString(task.getStartTime(), UtilBox.DATE_TIME));

        holder.tv_publishTime.setText(context.getResources().getString(R.string._publishTime)
                + UtilBox.getDateToString(task.getCreateTime(), UtilBox.DATE_TIME));

        switch (task.getSendState()) {
            case Task.SUCCESS:
                holder.tv_sendState.setVisibility(View.GONE);
                break;

            case Task.SENDING:
                holder.tv_sendState.setVisibility(View.VISIBLE);
                holder.tv_sendState.setText("发送中...");
                break;

            case Task.FAILED:
                holder.tv_sendState.setVisibility(View.VISIBLE);

                holder.tv_sendState.setText("重新发送");
                holder.tv_sendState.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TimelineAdapter.taskList.get(position).setSendState(Task.SENDING);
                        holder.tv_sendState.setText("发送中...");

                        if (uploadImagePresenter != null) {
                            uploadImagePresenter.doUploadImages(TimelineFragment.pictureList, Constants.DIR_TASK);
                        }
                    }
                });
                break;
        }

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
                tv_grade.setTextColor(ContextCompat.getColor(context, R.color.S));
                break;

            case "A":
                tv_grade.setTextColor(ContextCompat.getColor(context, R.color.A));
                break;

            case "B":
                tv_grade.setTextColor(ContextCompat.getColor(context, R.color.B));
                break;

            case "C":
                tv_grade.setTextColor(ContextCompat.getColor(context, R.color.C));
                break;

            case "D":
                tv_grade.setTextColor(ContextCompat.getColor(context, R.color.D));
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

        if (pictures != null && !"null".equals(pictures)) {
            try {
                JSONArray jsonArray = new JSONArray(pictures);

                for (int i = 0; i < jsonArray.length(); i++) {
                    pictureList.add(jsonArray.getString(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return pictureList;
    }

    /**
     * 将json解码成list
     *
     * @param comments 评论json
     * @return 图片List
     */
    private ArrayList<Comment> decodeCommentList(String taskID, String comments) {
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
                        Comment comment = new Comment(taskID,
                                sender, object.getString("send_id"),
                                object.getString("content"),
                                Long.valueOf(object.getString("create_time")) * 1000);

                        commentList.add(comment);
                    } else {
                        Comment comment = new Comment(taskID,
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

    public static class ViewHolder {

        public ImageView iv_portrait;
        public TextView tv_nickname;
        public TextView tv_grade;
        public TextView tv_desc;
        public GridView gv_picture;
        public ListView lv_comment;
        public Button btn_comment;
        public Button btn_audit;
        public TextView tv_executor;
        public TextView tv_supervisor;
        public TextView tv_auditor;
        public TextView tv_deadline;
        public TextView tv_startTime;
        public TextView tv_publishTime;
        public TextView tv_sendState;

        public ViewHolder(View itemView) {
            iv_portrait = (ImageView) itemView.findViewById(R.id.iv_item_portrait);
            tv_nickname = (TextView) itemView.findViewById(R.id.tv_item_nickname);
            tv_grade = (TextView) itemView.findViewById(R.id.tv_item_grade);
            tv_desc = (TextView) itemView.findViewById(R.id.tv_item_desc);
            gv_picture = (GridView) itemView.findViewById(R.id.gv_item_picture);
            lv_comment = (ListView) itemView.findViewById(R.id.lv_item_comment);
            btn_comment = (Button) itemView.findViewById(R.id.btn_item_comment);
            btn_audit = (Button) itemView.findViewById(R.id.btn_item_audit);
            tv_executor = (TextView) itemView.findViewById(R.id.tv_executor);
            tv_supervisor = (TextView) itemView.findViewById(R.id.tv_supervisor);
            tv_auditor = (TextView) itemView.findViewById(R.id.tv_auditor);
            tv_deadline = (TextView) itemView.findViewById(R.id.tv_deadline);
            tv_startTime = (TextView) itemView.findViewById(R.id.tv_startTime);
            tv_publishTime = (TextView) itemView.findViewById(R.id.tv_publishTime);
            tv_sendState = (TextView) itemView.findViewById(R.id.tv_sendState);
        }
    }
}