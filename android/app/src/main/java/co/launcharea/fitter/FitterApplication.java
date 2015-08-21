package co.launcharea.fitter;

import android.app.Application;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.parse.Parse;
import com.parse.ParseCrashReporting;
import com.parse.ParseObject;

import co.launcharea.fitter.model.Comment;
import co.launcharea.fitter.model.FitLog;
import co.launcharea.fitter.model.Notification;
import co.launcharea.fitter.model.Post;
import co.launcharea.fitter.model.Relation;
import co.launcharea.fitter.util.FitterParseUtil;


/**
 * Created by jack on 15. 7. 16.
 */
public class FitterApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        configureParse();
        configureLibraries();
    }

    private void configureParse() {
        ParseCrashReporting.enable(this);
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "9WkSIEg3RGxlrs8sDqER6997an77C08nqRLxNygh", "u7NyTxyaZ220YP7gz8PzumCbOWVoq2T9jTDI4hvF");
        FitterParseUtil.initializeInstallation();

        ParseObject.registerSubclass(Post.class);
        ParseObject.registerSubclass(Relation.class);
        ParseObject.registerSubclass(Comment.class);
        ParseObject.registerSubclass(FitLog.class);
        ParseObject.registerSubclass(Notification.class);
    }

    private void configureLibraries() {
        //Create image options.
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        //Create a config with those options.
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(options)
                .build();

        ImageLoader.getInstance().init(config);
    }
}
