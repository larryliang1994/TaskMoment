package com.jiubai.taskmoment.config;

import android.os.Environment;

/**
 * 常量
 */
public class Constants {
    public static final int CODE_ADD_COMPANY = 1;
    public static final int CODE_CHANGE_COMPANY = 2;
    public static final int CODE_MULTIPLE_PICTURE = 3;
    public static final int CODE_CHECK_PICTURE = 4;
    public static final int CODE_CHOOSE_PICTURE = 5;
    public static final int CODE_PUBLISH_TASK = 6;
    public static final int CODE_CHECK_TASK = 7;
    public static final int CODE_CHOOSE_PORTRAIT = 8;
    public static final int CODE_CHOOSE_COMPANY_BACKGROUND = 9;

    public static final String SP_FILENAME = "config";
    public static final String SP_KEY_COOKIE = "cookie";
    public static final String SP_KEY_COMPANY_NAME = "companyName";
    public static final String SP_KEY_COMPANY_ID = "companyId";
    public static final String SP_KEY_COMPANY_CREATOR = "companyCreator";
    public static final String SP_KEY_COMPANY_BACKGROUND = "companyBackground";
    public static final String SP_KEY_PORTRAIT = "portrait";
    public static final String SP_KEY_MID = "mid";
    public static final String SP_KEY_NICKNAME = "nickname";
    public static final String SP_KEY_TIME = "time";

    public static final String NAMESPACE = "taskmoment";
    public static final String DIR_PORTRAIT = "portrait/";
    public static final String DIR_BACKGROUND = "background/";
    public static final String DIR_TASK = "task/";

    public static final String ACTION_NEWS = "com.jiubai.action.news";
    public static final String ACTION_CHANGE_NICKNAME = "com.jiubai.action.change_nickname";
    public static final String ACTION_CHANGE_PORTRAIT = "com.jiubai.action.change_portrait";
    public static final String ACTION_DELETE_TASK = "com.jiubai.action.delete_task";
    public static final String ACTION_SEND_COMMENT = "com.jiubai.action.send_comment";
    public static final String ACTION_CHANGE_BACKGROUND = "com.jiubai.action.change_background";

    public static final String TEMP_FILE_LOCATION = "file:///"
            + Environment.getExternalStorageDirectory() + "/temp.jpg";
    public static final int SIZE_COMPANY_BACKGROUND = 500;
    public static final int SIZE_TASK_IMAGE = 300;
    public static final int SIZE_PORTRAIT = 100;

    public static final int REQUEST_TIMEOUT = 10000;
}
