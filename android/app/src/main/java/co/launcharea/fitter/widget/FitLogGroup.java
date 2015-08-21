package co.launcharea.fitter.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.parse.ParseQuery;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import co.launcharea.fitter.model.FitLog;

public class FitLogGroup extends FrameLayout implements View.OnLayoutChangeListener {
    WeakReference<OnDataChangedListener> mListenerReference;
    private int mChildrenSize = 0;
    private FitLog mRootFitLog;
    private int mFitLogId;
    public FitLogGroup(Context context) {
        super(context);
    }

    public FitLogGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FitLogGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initializeView(int newFitLogId, String workoutName, String referenceId, boolean editable, View button) {
        mFitLogId = newFitLogId;
        if (referenceId == null) {
            mRootFitLog = new FitLog(mFitLogId, 0);
        } else {
            ParseQuery<FitLog> refQuery = new ParseQuery<FitLog>("FitLog");
            refQuery.whereEqualTo("objectId", referenceId);
            ParseQuery<FitLog> query = new ParseQuery<FitLog>("FitLog");
            query.whereMatchesKeyInQuery("user", "user", refQuery);
            query.whereMatchesKeyInQuery("fitLogId", "fitLogId", refQuery);
            query.addAscendingOrder(FitLog.INDEX);
            try {
                List<FitLog> list = query.find();
                List<FitLog> newList = new LinkedList<FitLog>();
                Iterator<FitLog> it = list.iterator();
                while (it.hasNext()) {
                    newList.add(it.next().getNewFitLog(newFitLogId));
                }

                mRootFitLog = FitLog.buildHierarchy(newList);
            } catch (Exception e) {
                //TODO
                e.printStackTrace();
            }
        }
        mRootFitLog.put(FitLog.EXERCISE, workoutName);
        addView(mRootFitLog.createView(getContext(), editable));
        setVisibility(View.VISIBLE);
        button.setVisibility(View.GONE);

        this.addOnLayoutChangeListener(this);
    }

    public void loadView(Number fitLogId, boolean editable, FitLog fitlog) {
        if (fitLogId == null || fitlog == null) {
            return;
        }
        mFitLogId = (int) fitLogId;
        mRootFitLog = fitlog;
        removeAllViews();
        addView(mRootFitLog.createView(getContext(), editable));
        setVisibility(View.VISIBLE);
    }

    public void setOnDataChangedListener(OnDataChangedListener listener) {
        mListenerReference = new WeakReference<OnDataChangedListener>(listener);
    }

    public FitLog getFitlog() {
        return mRootFitLog;
    }

    public int getFitlogId() {
        return mFitLogId;
    }

    public boolean hasData() {
        return mFitLogId != 0 && mRootFitLog != null && mChildrenSize > 0;
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

        int size = mRootFitLog.getChildren().size();
        if (size != mChildrenSize) {
            mChildrenSize = size;
            OnDataChangedListener listener = mListenerReference.get();
            if (listener == null) {
                return;
            }
            listener.onDataChanged();
        }
    }

    public interface OnDataChangedListener {
        void onDataChanged();
    }
}
