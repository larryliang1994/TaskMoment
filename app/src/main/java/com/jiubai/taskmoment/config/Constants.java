package com.jiubai.taskmoment.config;

import android.os.Environment;

/**
 * 常量
 */
public class Constants {
    public static final String OSSACCESSKEY = "ATqbBKHty4TJNmLY";
    public static final String OSSSCRECTKEY = "3BDRee3e109ZzCO4TrofOypSqv3fDw";
    public static final String BUCKET_NAME = "jiubai-app";
    public static final String HOST_ID = "http://jiubai-app.oss-cn-hangzhou.aliyuncs.com/";
    public static final String API_KEY = "unkDGrSxE8nGEYz7Ne9p0RYG";

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
    public static final String SP_KEY_COMPANY_BACKGROUND = "companyBackground";
    public static final String SP_KEY_PORTRAIT = "portrait";
    public static final String SP_KEY_MID = "mid";
    public static final String SP_KEY_NICKNAME = "nickname";

    public static final String TEMP_FILE_LOCATION = "file:///"
            + Environment.getExternalStorageDirectory() + "/temp.jpg";
    public static final int SIZE_COMPANY_BACKGROUND = 500;
    public static final int SIZE_TASK_IMAGE = 300;
    public static final int SIZE_PORTRAIT = 100;
}
