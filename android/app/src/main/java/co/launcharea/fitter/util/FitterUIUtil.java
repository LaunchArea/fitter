package co.launcharea.fitter.util;

import android.content.Context;
import android.content.Intent;

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
}
