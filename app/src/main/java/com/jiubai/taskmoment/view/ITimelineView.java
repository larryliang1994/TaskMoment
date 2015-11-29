package com.jiubai.taskmoment.view;

import com.jiubai.taskmoment.classes.News;

/**
 * Created by howell on 2015/11/29.
 * PullTimelineView接口
 */
public interface ITimelineView {
    void onPullTimelineResult(int result, String type, String info);
    void onGetNewsResult(int result, News news);
    void onSetSwipeRefreshVisibility(int visibility);
}
