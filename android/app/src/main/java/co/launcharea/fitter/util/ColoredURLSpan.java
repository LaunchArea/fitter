package co.launcharea.fitter.util;

import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.URLSpan;

import co.launcharea.fitter.R;

public class ColoredURLSpan extends URLSpan {
    public ColoredURLSpan(String url) {
        super(url);
    }

    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.bgColor = mIsPressed ? Color.rgb(0x00, 0x96, 0x88) : 0;
        ds.setColor(mIsPressed ? Color.BLUE : Color.rgb(0x00, 0x96, 0x88));
    }

    public boolean mIsPressed = false;
}