package co.launcharea.fitter.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

/**
 * Created by jack on 15. 7. 30.
 */
public class BaseActionBarActivity extends ActionBarActivity {
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void showProgress(boolean show) {
        if (mProgress == null) {
            mProgress = new ProgressDialog(this);
            mProgress.setCanceledOnTouchOutside(false);
            mProgress.setCancelable(false);
        }

        if (show) {
            mProgress.show();
        } else {
            mProgress.hide();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mProgress != null) {
            mProgress.dismiss();
        }
    }

    public void onViewCreated(BaseFragment fragment) {

    }
}
