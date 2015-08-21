package co.launcharea.fitter.model;

import android.text.SpannableStringBuilder;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import org.json.JSONException;
import org.json.JSONObject;

import co.launcharea.fitter.util.FitterParseUtil;

/**
 * Created by hyungchulkim on 8/6/15.
 */
@ParseClassName("Notification")
public class Notification extends ParseObject {
    public final static int TYPE_FOLLOWING                = 1;
    public final static int TYPE_LIKE_ON_MY_POST          = 2;
    public final static int TYPE_COMMENT_ON_POST          = 3;
    public final static int TYPE_COMMENT_ON_MY_COMMENT    = 4;
    public final static int TYPE_MENTION_ON_A_POST        = 5;
    public final static int TYPE_MENTION_ON_A_COMMENT     = 6;

    public Notification() {

    }

    public int getType() {
        return getInt("type");
    }

    public String getContent() {
        return getString("content");
    }

    public boolean getRead() {
        return getBoolean("read");
    }

    public void setRead() {
        put("read", true);
    }

    public JSONObject getPayload() {
        return getJSONObject("payload");
    }

    public String getUserId()  {
        String userId = null;
        try {
            userId = getPayload().getString("userId");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return userId;
    }

    private String _getPostId() {
        String postId = null;
        try {
            postId = getPayload().getString("postId");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return postId;
    }

    public String getPostId() {
        switch(getType()) {
            case Notification.TYPE_FOLLOWING:
                return null;
            case Notification.TYPE_LIKE_ON_MY_POST:
                return _getPostId();
            case Notification.TYPE_COMMENT_ON_POST:
                return _getPostId();
            case Notification.TYPE_COMMENT_ON_MY_COMMENT:
                return _getPostId();
            case Notification.TYPE_MENTION_ON_A_POST:
                return _getPostId();
            case Notification.TYPE_MENTION_ON_A_COMMENT:
                return _getPostId();
            default:
                return _getPostId();
        }
    }

    public SpannableStringBuilder getRichContent() {
        return FitterParseUtil.applyUsernameSpan(getContent());
    }
}
