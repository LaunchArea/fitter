package co.launcharea.fitter.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.ParseQuery;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import co.launcharea.fitter.R;
import co.launcharea.fitter.model.FitLog;

public class WorkoutListAdapter extends BaseAdapter implements ListView.OnItemClickListener {
    private Activity mActivity;
    private boolean mSaved;
    private List<FitLog> mList = new LinkedList<>();
    private AlertDialog mDialog;


    public WorkoutListAdapter(Activity activity, boolean saved) {
        mActivity = activity;
        mSaved = saved;
    }

    public void setList(List<FitLog> list) {
        mList = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.item_list_workout, null);
        ((TextView) view.findViewById(R.id.workoutName)).setText(mList.get(position).getString("exercise"));
        ((TextView) view.findViewById(R.id.workoutOwner)).setText(mActivity.getString(R.string.label_owner) + " : " + mList.get(position).getParseUser("user").getUsername());
        Date date = mList.get(position).getCreatedAt();
        SimpleDateFormat dateFormat = new SimpleDateFormat(mActivity.getString(R.string.format_date));
        ((TextView) view.findViewById(R.id.workoutDate)).setText(dateFormat.format(date));

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LinearLayout dialogLayout = (LinearLayout) LayoutInflater.from(mActivity).inflate(R.layout.dialog_workout_selector, null);
        builder.setView(dialogLayout);

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(mActivity.getString(R.string.format_date));
        ((EditText) dialogLayout.findViewById(R.id.workoutName)).setText(mActivity.getString(R.string.label_workout) + " " + dateFormat.format(cal.getTime()));

        dialogLayout.addView(getView(position, null, null), 2);

        builder.setPositiveButton(mActivity.getString(R.string.action_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // do nothing
            }
        });
        builder.setNegativeButton(mActivity.getString(R.string.action_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // do nothing
            }
        });

        Intent intent = new Intent();
        LinearLayout scrollView = (LinearLayout) dialogLayout.findViewById(R.id.workoutScroll);
        ParseQuery<FitLog> query = new ParseQuery<FitLog>("FitLog");
        query.whereEqualTo(FitLog.FIT_LOG_ID, mList.get(position).getInt(FitLog.FIT_LOG_ID));
        query.addAscendingOrder(FitLog.INDEX);
        try {
            List<FitLog> list = query.find();
            FitLog rootFitLog = FitLog.buildHierarchy(list);
            View workoutView = rootFitLog.createView(mActivity, false);
            workoutView.findViewById(R.id.fitLogName).setVisibility(View.GONE);
            scrollView.addView(workoutView);
            intent.putExtra(PostingActivity.EXTRA_REFERENCE_ID, mList.get(position).getObjectId());
        } catch (Exception e) {
            //TODO
            e.printStackTrace();
        }

        mDialog = builder.create();
        mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        mDialog.show();

        // override onClickListener not to be dismissed right after OK clicked.
        // to get button from dialog, dialog must be shown first.
        // if not working, dialog.setOnshowListener should be used.
        Button positiveButton = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setTag(intent);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = (Intent) v.getTag();
                String workoutName = ((EditText) mDialog.findViewById(R.id.workoutName)).getText().toString();
                if (!workoutName.isEmpty()) {
                    intent.putExtra(PostingActivity.EXTRA_WORKOUT_NAME, workoutName);
                    mActivity.setResult(Activity.RESULT_OK, intent);
                    mActivity.finish();
                    mDialog.dismiss();
                }
            }
        });
    }
}