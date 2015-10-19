package com.jiubai.taskmoment.other;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.jiubai.taskmoment.config.Config;
import com.jiubai.taskmoment.config.Constants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自用工具盒
 */
public class UtilBox {
    /**
     * 判断是否为电话号码
     *
     * @param number 电话号码
     * @return 是则为true
     */
    public static boolean isTelephoneNumber(String number) {
        Pattern p = Pattern
                .compile("^((13[0-9])|(15[0-9])|(17[0-9])|(18[0-9]))\\d{8}$");
        return p.matcher(number).matches();
    }

    /**
     * 从字符串中截取连续6位数字组合 ([0-9]{" + 6 + "})截取六位数字 进行前后断言不能出现数字 用于从短信中获取动态密码
     *
     * @param str 短信内容
     * @return 截取得到的6位动态密码
     */
    public static String getDynamicPassword(String str) {
        // 6是验证码的位数一般为六位
        Pattern continuousNumberPattern = Pattern.compile("(?<![0-9])([0-9]{" + 6 + "})(?![0-9])");
        Matcher m = continuousNumberPattern.matcher(str);
        String dynamicPassword = "";
        while (m.find()) {
            System.out.print(m.group());
            dynamicPassword = m.group();
        }

        return dynamicPassword;
    }

    /**
     * 重新计算ListView的高度
     *
     * @param listView 需要计算的ListView
     */
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        // 获取ListView对应的Adapter
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
            // listAdapter.getCount()返回数据项的数目
            View listItem = listAdapter.getView(i, null, listView);
            // 计算子项View 的宽高
            listItem.measure(0, 0);
            // 统计所有子项的总高度
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        //listView.getDividerHeight()获取子项间分隔符占用的高度
        // params.height最后得到整个ListView完整显示需要的高度
        listView.setLayoutParams(params);
    }

    /**
     * 重新计算ListView的高度
     *
     * @param gridView     需要计算的ListView
     * @param measureWidth 是否需要重新计算宽度
     */
    public static void setGridViewHeightBasedOnChildren(GridView gridView, boolean measureWidth) {
        // 获取ListView对应的Adapter
        ListAdapter listAdapter = gridView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        int width = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i += 3) {
            // listAdapter.getCount()返回数据项的数目
            View listItem = listAdapter.getView(i, null, gridView);
            // 计算子项View 的宽高
            listItem.measure(0, 0);
            // 统计所有子项的总高度
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                totalHeight += listItem.getMeasuredHeight() + gridView.getVerticalSpacing();
            } else {
                totalHeight += listItem.getMeasuredHeight();
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                width = listItem.getMeasuredWidth() * 3 + gridView.getHorizontalSpacing() * 2;
            } else {
                width = listItem.getMeasuredWidth() * 3;
            }
        }

        ViewGroup.LayoutParams params = gridView.getLayoutParams();
        params.height = totalHeight + ((listAdapter.getCount() - 1));
        if (measureWidth) {
            params.width = width;
        }
        // listView.getDividerHeight()获取子项间分隔符占用的高度
        // params.height最后得到整个ListView完整显示需要的高度
        gridView.setLayoutParams(params);
    }

    /**
     * dp转px
     *
     * @param context 上下文
     * @param dpValue dp值
     * @return px值
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * px转dp
     *
     * @param context 上下文
     * @param pxValue px值
     * @return dp值
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 清除所有用户数据
     *
     * @param context 上下文
     */
    public static void clearAllData(Context context){
        SharedPreferences sp = context.getSharedPreferences(
                Constants.SP_FILENAME, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.apply();

        Config.COMPANY_BACKGROUND = null;
        Config.PORTRAIT = null;
        Config.CID = null;
        Config.COMPANY_NAME = null;
        Config.COOKIE = null;
        Config.MID = null;
        Config.NICKNAME = null;
        Config.RANDOM = null;
    }

    /**
     * 获取5位随机数
     */
    public static void getRandom() {
        int number = (int) (Math.random() * 100000);
        if (number < 90000) {
            number += 10000;
        }
        Config.RANDOM = String.valueOf(number);
    }

    /**
     * 根据路径，解析成图片
     *
     * @param url    图片路径
     * @param width  指定的宽度
     * @param height 指定的宽度
     * @return 图片Bitmap
     */
    public static Bitmap getLocalBitmap(String url, int width, int height) {
        return getBitmapFromFile(new File(url), width, height);
    }

    @SuppressWarnings("deprecation")
    public static Bitmap getBitmapFromFile(File dst, int width, int height) {
        if (null != dst && dst.exists()) {
            BitmapFactory.Options opts;
            if (width > 0 && height > 0) {
                opts = new BitmapFactory.Options();

                //设置inJustDecodeBounds为true后，decodeFile并不分配空间，此时计算原始图片的长度和宽度
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(dst.getPath(), opts);
                // 计算图片缩放比例
                final int minSideLength = Math.min(width, height);
                opts.inSampleSize = computeSampleSize(opts, minSideLength,
                        width * height);

                //这里一定要将其设置回false，因为之前我们将其设置成了true
                opts.inJustDecodeBounds = false;
                opts.inInputShareable = true;
                opts.inPurgeable = true;

                while (true) {
                    try {
                        return BitmapFactory.decodeFile(dst.getPath(), opts);
                    } catch (OutOfMemoryError e) {
                        e.printStackTrace();
                        opts.inSampleSize += 1;
                        System.out.println("error");
                    }
                }
            }
        }

        return null;
    }

    private static int computeSampleSize(BitmapFactory.Options options,
                                         int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options,
                                                int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
                .sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math
                .floor(w / minSideLength), Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    /**
     * 获取屏幕宽度
     *
     * @param context 上下文
     * @return 屏幕宽度
     */
    public static int getWidthPixels(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);

        return dm.widthPixels;
    }

    /**
     * 获取屏幕高度
     *
     * @param context 上下文
     * @return 屏幕高度
     */
    public static int getHeightPixels(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);

        return dm.heightPixels;
    }

    /**
     * 弹出键盘
     *
     * @param view 焦点
     */
    public static void toggleSoftInput(View view, boolean show) {
        InputMethodManager inputManager = (InputMethodManager) view.
                getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (show && !inputManager.isActive(view)) {
            inputManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
                    InputMethodManager.SHOW_IMPLICIT);
        } else {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * 时间戳转换成字符串
     *
     * @param time 时间戳
     * @return 日期字符串
     */
    public static String getDateToString(long time) {
        Date d = new Date(time);
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        return sf.format(d);
    }

    /**
     * 将字符串转为时间戳
     *
     * @param time 日期字符串
     * @return 时间戳
     */
    public static long getStringToDate(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        Date date = new Date();
        try {
            date = sdf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date.getTime();
    }

    /**
     * Bitmap转Bytes
     *
     * @param bm bitmap
     * @return bytes
     */
    public static byte[] bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * 对字符串进行md5加密
     *
     * @param str 需要加密的字符串
     * @return 加密完成的字符串
     */
    private static String getMD5Str(String str) {
        MessageDigest messageDigest = null;

        try {
            messageDigest = MessageDigest.getInstance("MD5");

            messageDigest.reset();

            messageDigest.update(str.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            System.out.println("NoSuchAlgorithmException caught!");
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        byte[] byteArray = messageDigest.digest();

        StringBuilder md5StrBuff = new StringBuilder();

        for (byte aByteArray : byteArray) {
            if (Integer.toHexString(0xFF & aByteArray).length() == 1)
                md5StrBuff.append("0").append(Integer.toHexString(0xFF & aByteArray));
            else
                md5StrBuff.append(Integer.toHexString(0xFF & aByteArray));
        }

        return md5StrBuff.toString();

//        // 16位加密，从第9位到25位
//        return md5StrBuff.substring(8, 24).toUpperCase();
    }

    /**
     * 获取一个唯一的文件名
     *
     * @return 唯一的文件名
     */
    public static String getObjectName() {
        return "task_moment/" + getMD5Str(Calendar.getInstance().getTimeInMillis() + "") + ".jpg";
    }

    /**
     * 压缩一张图片至指定大小
     *
     * @param image 需要压缩的图片
     * @param size  压缩后的最大值(kb)
     * @return 压缩后的图片
     */
    public static Bitmap compressImage(Bitmap image, int size) {

        // 将bitmap放至数组中，意在bitmap的大小(与实际读取的原文件要大)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        byte[] b = baos.toByteArray();
        // 将字节换成KB
        double mid = b.length/1024;

        // 判断bitmap占用空间是否大于允许最大空间  如果大于则压缩 小于则不压缩
        if (mid > size) {
            // 获取bitmap大小 是允许最大大小的多少倍
            double i = mid / size;
            // 开始压缩  此处用到平方根 将宽带和高度压缩掉对应的平方根倍
            // (保持刻度和高度和原bitmap比率一致，压缩后也达到了最大大小占用空间的大小)
            image = zoomImage(image, image.getWidth() / Math.sqrt(i),
                    image.getHeight() / Math.sqrt(i));
        }

        return image;
    }

    private static Bitmap zoomImage(Bitmap image, double newWidth,
                                   double newHeight) {
        // 获取这个图片的宽和高
        float width = image.getWidth();
        float height = image.getHeight();

        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();

        // 计算宽高缩放率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(image, 0, 0, (int) width,
                (int) height, matrix, true);
    }


}
