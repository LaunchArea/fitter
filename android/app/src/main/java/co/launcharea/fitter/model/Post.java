package co.launcharea.fitter.model;

import android.text.SpannableStringBuilder;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONException;

import co.launcharea.fitter.util.FitterParseUtil;

@ParseClassName("Post")
public class Post extends ParseObject {

    public Post() {

    }

    public String getContent() {
        return getString("content");
    }

    public void setContent(String content) {
        put("content", content);
    }

    public ParseUser getUser() {
        return getParseUser("user");
    }

    public void setUser(ParseUser user) {
        put("user", user);
    }

    public Number getFitLogId() {
        return getNumber("fitLogId");
    }

    public void setFitLogId(int fitLogId) {
        put("fitLogId", fitLogId);
    }

    public String getRawFitLogSummary() {
        return getString("fitLogSummary");
    }

    public FitLog getFitLogSummary() {
        try {
            String raw = getRawFitLogSummary();
            if (raw == null || raw.length() == 0)
                return null;
            return FitLog.fromJsonArray(raw);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setFitLogSummary(String fitLogSummary) {
        put("fitLogSummary", fitLogSummary);
    }

    public Comment getLastComment() {
        return (Comment) getParseObject("lastComment");
    }

    public SpannableStringBuilder getRichContent() {
        return FitterParseUtil.applyUsernameSpan(getContent());
    }
}
