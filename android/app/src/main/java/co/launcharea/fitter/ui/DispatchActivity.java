package co.launcharea.fitter.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;

import com.parse.ParseUser;

import co.launcharea.fitter.R;
import co.launcharea.fitter.service.FitterParseCache;

public class DispatchActivity extends Activity {

    private static final long SPLASH_SHOWING_TIME = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        if (ParseUser.getCurrentUser() != null) {
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    long start = SystemClock.elapsedRealtime();
                    FitterParseCache.init(DispatchActivity.this.getApplicationContext());
                    long end = SystemClock.elapsedRealtime();
                    if (end - start < SPLASH_SHOWING_TIME) {
                        try {
                            Thread.sleep(SPLASH_SHOWING_TIME - (end - start));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    finish();
                    DispatchActivity.this.startActivity(new Intent(DispatchActivity.this, MainActivity.class));
                }
            }.execute();
        } else {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                    DispatchActivity.this.startActivity(new Intent(DispatchActivity.this, LoginActivity.class));
                }
            }, SPLASH_SHOWING_TIME);
        }
    }
}
