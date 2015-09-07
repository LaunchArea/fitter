package co.launcharea.fitter.util;

import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.widget.TextView;

import co.launcharea.fitter.model.Post;
import co.launcharea.fitter.ui.PostDetailAcivity;
import co.launcharea.fitter.ui.ProfileViewActivity;

/**
 * Created by hyungchulkim on 8/6/15.
 */
public class FitterUIUtil {
    public static Intent getPostDetailActivityIntent(Context context, Post post) {
        Intent intent = new Intent(context, PostDetailAcivity.class);
        intent.putExtra(PostDetailAcivity.POST_ID, post.getObjectId());
        return intent;
    }

    public static Intent getProfileViewActivityIntent(Context context, String userId) {
        Intent intent = new Intent(context, ProfileViewActivity.class);
        intent.putExtra(ProfileViewActivity.EXTRA_USER_ID, userId);
        return intent;
    }

    public static void linkifyText(TextView textView) {
        Spannable spannable = new SpannableStringBuilder(textView.getText());
        Linkify.addLinks(spannable, Linkify.WEB_URLS);

        URLSpan[] spans = spannable.getSpans(0, spannable.length(), URLSpan.class);
        for (URLSpan span : spans) {
            int spanStart = spannable.getSpanStart(span); // save postion of current url span
            int spanEnd = spannable.getSpanEnd(span);
            if (spanStart < 0 || spanEnd < 0)
                break;

            String url = span.getURL();

            spannable.removeSpan(span);  // remove prev url span
            spannable.setSpan(new ColoredURLSpan(url), spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        textView.setText(spannable);
        textView.setMovementMethod(FitterLinkMovementMethod.getInstance());
        textView.setClickable(false);
        textView.setLongClickable(false);
    }
}
