package co.launcharea.fitter.util;

import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import co.launcharea.fitter.model.Notification;

/**
 * Created by hyungchulkim on 8/5/15.
 */
public class FitterParseUtil {
    public static void initializeInstallation () {
        subscribeBroadcastChannel();
        addUserRelationWithInstallation();
    }

    public static boolean subscribeBroadcastChannel() {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        ArrayList<String> channels = (ArrayList<String>)installation.get("channels");

        if (channels != null) {
            channels = getBroadcastChannel();
            installation.put("channels", channels);
            installation.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Log.d("PARSE", "subscribeBroadcastChannel save success");
                    } else {
                        Log.e("PARSE", "subscribeBroadcastChannel save fail, " + e.toString());
                    }
                }
            });
            return true;
        }
        return false;
    }

    public static boolean addUserRelationWithInstallation() {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();

        ParseUser user = (ParseUser)installation.get("user");
        if (user == null) {
            if (ParseUser.getCurrentUser() == null) {
                Log.d("PARSE", "currentUser is not set yet.");
                return false;
            }
            installation.put("user", ParseUser.getCurrentUser());
            installation.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Log.d("PARSE", "installation-user save success");
                    } else {
                        Log.e("PARSE", "installation-user save fail, " + e.toString());
                    }
                }
            });

            return true;
        }

        return false;
    }

    public static void getNotificationBadge(FindCallback<Notification> callback) {
        Date date = (Date)ParseUser.getCurrentUser().get("lastSeenNotification");
        if (date == null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            try {
                date = sdf.parse("01/08/2015");
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
            ParseUser.getCurrentUser().put("lastSeenNotification", date);
        }
        ParseQuery<Notification> query = new ParseQuery<Notification>("Notification").whereGreaterThan("createdAt", date).whereEqualTo("read", false);
        query.findInBackground(callback);
    }

    private static ArrayList<String> getBroadcastChannel() {
        ArrayList<String> broadcastChannel = new ArrayList<String>();
        broadcastChannel.add("");
        return broadcastChannel;
    }

    public static SpannableStringBuilder applyUsernameSpan(String input) {

        Pattern pattern = Pattern.compile("\"<@.*?>\"");
        Matcher matcher = pattern.matcher(input);
        SpannableStringBuilder builder = new SpannableStringBuilder();
        int cutOffset = 0;
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            builder.append(input.substring(cutOffset, start));
            int spanStart = builder.length();

            CharSequence atUsername = input.substring(start + 2, end - 2);
            builder.append(atUsername);

            builder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), spanStart, spanStart + atUsername.length(), 0);

            cutOffset = end;
        }
        builder.append(input.substring(cutOffset, input.length()));
        return builder;
    }
}
