package co.launcharea.fitter.receiver;

import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import co.launcharea.fitter.model.Notification;
import co.launcharea.fitter.model.Post;
import co.launcharea.fitter.ui.MainActivity;
import co.launcharea.fitter.ui.ProfileViewActivity;
import co.launcharea.fitter.util.FitterUIUtil;

/**
 * Created by hyungchulkim on 8/10/15.
 */
public class FitterPushReceiver extends ParsePushBroadcastReceiver {

    public FitterPushReceiver() {

    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        super.onPushReceive(context, intent);

        if (intent == null) {
            Log.e("PUSH", "onPushReceive, intent is null");
            return;
        }

        Intent i = new Intent(MainActivity.NOTIFICATION_BROADCAST);
        context.sendBroadcast(i);

        try {
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            Log.i("PUSH", "Push received: " + json);
        } catch (JSONException e) {
            Log.e("PUSH", "Push message json exception: " + e.getMessage());
        }


    }

    private void setNotificationRead(String notificationId) {
        Notification notification = (Notification) ParseObject.createWithoutData("Notification", notificationId);
        notification.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                ((Notification)parseObject).setRead();
                parseObject.saveEventually();
            }
        });
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        Log.i("PUSH", "onPushOpen" + context);
        try {
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            Log.i("PUSH", "json: " + json);
            setNotificationRead(json.getString("id"));

            switch (json.getInt("type")) {
                case Notification.TYPE_FOLLOWING: {
                    String followerId = json.getString("followerId");
                    Class cls = ProfileViewActivity.class;
                    Intent profileIntent = FitterUIUtil.getProfileViewActivityIntent(context, followerId);
                    profileIntent.putExtras(intent.getExtras());
                    profileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (Build.VERSION.SDK_INT >= 16) {
                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                        stackBuilder.addParentStack(cls);
                        stackBuilder.addNextIntent(profileIntent);
                        stackBuilder.startActivities();
                    } else {
                        profileIntent.addFlags(268435456);
                        profileIntent.addFlags(67108864);
                        context.startActivity(profileIntent);
                    }
                }
                break;
                case Notification.TYPE_COMMENT_ON_POST: {
                    String postId = json.getString("postId");
                    String userId = json.getString("userId");

                    Post post = (Post) ParseObject.createWithoutData("Post", postId);
                    ParseUser user = null;
                    try {
                        post.fetchIfNeeded();
                        user = post.getUser();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    Class cls = ProfileViewActivity.class;
                    Intent postDetailActivityIntent = FitterUIUtil.getPostDetailActivityIntent(context, post);
                    postDetailActivityIntent.putExtras(intent.getExtras());
                    postDetailActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (Build.VERSION.SDK_INT >= 16) {
                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                        stackBuilder.addParentStack(cls);
                        stackBuilder.addNextIntent(postDetailActivityIntent);
                        stackBuilder.startActivities();
                    } else {
                        postDetailActivityIntent.addFlags(268435456);
                        postDetailActivityIntent.addFlags(67108864);
                        context.startActivity(postDetailActivityIntent);
                    }
                }
                break;
                default:
                    super.onPushOpen(context, intent);
                    break;
            }

        } catch (JSONException e) {
            Log.e("PUSH", "Push message json exception: " + e.getMessage());
        }

    }

    @Override
    protected void onPushDismiss(Context context, Intent intent) {
        super.onPushDismiss(context, intent);
        Log.i("PUSH", "onPushDismiss");
        try {
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            Log.i("PUSH", "json: " + json);
        } catch (JSONException e) {
            Log.e("PUSH", "Push message json exception: " + e.getMessage());
        }

    }

    public interface PushReceiveListner {
        void onReceive(Context context, Intent intent);
    }
}
