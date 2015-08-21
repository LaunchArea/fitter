package co.launcharea.fitter.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

public class BaseFragment extends Fragment {
    private BaseActionBarActivity mActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof BaseActionBarActivity) {
            mActivity = ((BaseActionBarActivity) activity);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mActivity != null) {
            mActivity.onViewCreated(this);
        }
    }
}
