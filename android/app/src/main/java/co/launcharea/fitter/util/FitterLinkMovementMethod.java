package co.launcharea.fitter.util;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.MotionEvent;
import android.widget.TextView;

public class FitterLinkMovementMethod extends LinkMovementMethod {
    static FitterLinkMovementMethod sInstance = null;

    public static FitterLinkMovementMethod getInstance() {
        if (sInstance == null) {
            sInstance = new FitterLinkMovementMethod();
        }
        return sInstance;
    }

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer,
                                MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP ||
                action == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            ColoredURLSpan[] link = buffer.getSpans(off, off, ColoredURLSpan.class);

            if (link.length != 0) {
                if (action == MotionEvent.ACTION_UP) {
                    link[0].onClick(widget);
                    link[0].mIsPressed = false;
                } else if (action == MotionEvent.ACTION_DOWN) {
                    Selection.setSelection(buffer,
                            buffer.getSpanStart(link[0]),
                            buffer.getSpanEnd(link[0]));
                            link[0].mIsPressed = true;
                }
                return true;
            } else {
                Selection.removeSelection(buffer);
                return false;
            }
        } else if (action == MotionEvent.ACTION_CANCEL) {
            ColoredURLSpan[] links = buffer.getSpans(0, buffer.length(), ColoredURLSpan.class);
            for (ColoredURLSpan link : links) {
                link.mIsPressed = false;
            }
        }
        widget.invalidate();
        return false;
    }
}