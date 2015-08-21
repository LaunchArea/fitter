package co.launcharea.fitter.model;

import android.text.SpannableStringBuilder;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import co.launcharea.fitter.util.FitterParseUtil;

@ParseClassName("Comment")
public class Comment extends ParseObject {
    private SpannableStringBuilder mRichContent;

    public Comment() {

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

    public Post getPost() {
        return (Post) getParseObject("post");
    }

    public void setPost(Post post) {
        put("post", post);
    }

    public SpannableStringBuilder getRichContent() {
        return FitterParseUtil.applyUsernameSpan(getContent());
    }
}