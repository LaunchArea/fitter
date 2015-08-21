package co.launcharea.fitter.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import co.launcharea.fitter.R;
import co.launcharea.fitter.model.FitLog;
import co.launcharea.fitter.util.StringUtils;

public class FitLogSummaryView extends FrameLayout {
    private ViewGroup mRootView;
    private LayoutInflater mInflater;
    private LinearLayout mContentArea;
    private TextView mTitle;
    private HashMap<String, View> mViewMapping;

    public FitLogSummaryView(Context context) {
        this(context, null);
    }

    public FitLogSummaryView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FitLogSummaryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        mInflater = LayoutInflater.from(getContext());
        mRootView = (ViewGroup) mInflater.inflate(R.layout.view_fitlog_summary_root, null);
        mTitle = (TextView) mRootView.findViewById(R.id.title);
        mContentArea = (LinearLayout) mRootView.findViewById(R.id.content_area);
        mViewMapping = new HashMap<String, View>();
        addView(mRootView);
    }

    public void loadData(FitLog data) {
        mViewMapping.clear();
        mContentArea.removeAllViews();

        mTitle.setText(data.getString(FitLog.EXERCISE));
        for (FitLog each : data.getChildren()) {
            if (each.getChildren().size() > 0) { //superset
                addSuperset(each);
            } else {
                String key = keyOf(null, each);
                View v = mViewMapping.get(key);
                if (v != null) {
                    appendExercise(v, each);
                } else {
                    v = addExercise(each);
                    mViewMapping.put(key, v);
                }
            }
        }
    }

    private String keyOf(FitLog parent, FitLog log) {
        int prefix = parent != null ? parent.getInt(FitLog.INDEX) : 0;
        return prefix + ":" + log.getString(FitLog.EXERCISE);
    }

    private View addExercise(FitLog log) {
        View layout = mInflater.inflate(R.layout.view_fitlog_summary_exercise, null);
        fillExerciseViewData(layout, log);
        mContentArea.addView(layout);
        return layout;
    }

    private void addSuperset(FitLog log) {
        List<FitLog> children = log.getChildren();

        View root = mInflater.inflate(R.layout.view_fitlog_summary_superset, mContentArea);

        fillExerciseViewData(root, log);

        View moreIndicator = root.findViewById(R.id.more_items);
        ViewGroup exLayoutGroup = (ViewGroup) root.findViewById(R.id.ex_list);

        moreIndicator.setVisibility(View.GONE);

        for (int i = 0; i < exLayoutGroup.getChildCount(); i++) {
            exLayoutGroup.getChildAt(i).setVisibility(View.GONE);
        }

        for (int i = 0, j = 0; i < children.size(); i++) {
            FitLog each = children.get(i);
            String key = keyOf(log, each);
            View v = mViewMapping.get(key);
            if (v != null) {
                appendExercise(v, each);
            } else {
                if (j >= exLayoutGroup.getChildCount()) {
                    if (i != children.size()) {
                        moreIndicator.setVisibility(View.VISIBLE);
                    }
                    break;
                }
                View exLayout = exLayoutGroup.getChildAt(j++);
                fillExerciseViewData(exLayout, each);
                exLayout.setVisibility(View.VISIBLE);
                mViewMapping.put(key, exLayout);
            }
        }
    }

    private void fillExerciseViewData(View layout, FitLog log) {
        ((TextView) layout.findViewById(R.id.ex_title)).setText(log.getString(FitLog.EXERCISE));

        TextView descView = (TextView) layout.findViewById(R.id.ex_description);
        String desc = log.getString(FitLog.DESCRIPTION);
        if (desc != null && desc.length() > 0) {
            descView.setText(desc);
            descView.setVisibility(View.VISIBLE);
        } else {
            descView.setVisibility(View.GONE);
        }
    }

    private void appendExercise(View layout, FitLog log) {

        TextView descView = (TextView) layout.findViewById(R.id.ex_description);
        String desc = log.getString(FitLog.DESCRIPTION);
        String existingText = (String) descView.getText();
        int n = StringUtils.countLines(existingText);
        if (n < 3) {
        } else if (n == 3) {
            desc = "...";
        } else {
            desc = "";
        }

        if (desc != null && desc.length() > 0) {
            StringBuffer buffer = new StringBuffer(existingText);
            if (buffer.length() > 0) {
                buffer.append("\n");
            }
            buffer.append(desc);
            descView.setText(buffer.toString());
            descView.setVisibility(View.VISIBLE);
        }
    }
}
