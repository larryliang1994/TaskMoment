package com.jiubai.taskmoment.style;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.text.style.ClickableSpan;

import com.jiubai.taskmoment.R;

/**
 * 可点击的文字
 */
public abstract class ClickableText extends ClickableSpan{
    @SuppressWarnings("ResourceType")
    @Override
    public void updateDrawState(@NonNull TextPaint ds) {
        ds.setColor(Color.parseColor("#3f51b5"));
    }
}
