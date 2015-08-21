package co.launcharea.fitter.model;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import co.launcharea.fitter.R;
import co.launcharea.fitter.service.FitterParseCache;
import co.launcharea.fitter.widget.DescriptionInputView;

@ParseClassName("FitLog")
public class FitLog extends ParseObject{

    public static final String FIT_LOG_ID = "fitLogId";
    public static final String INDEX = "index";
    public static final String EXERCISE = "exercise";
    public static final String DESCRIPTION = "description";
    public static final String ELAPSED_TIME = "elapsedTime";
    public static final String DATA1 = "data1";
    public static final String DATA2 = "data2";
    public static final String DATA3 = "data3";
    public static final String DATA4 = "data4";
    public static final String DESCRIPTION_REF = "descriptionRef";


    LinearLayout mLayout = null;
    private FitLog mParentFitLog = null;
    private LinkedList<FitLog> children = new LinkedList<FitLog>();
    private Context mContext = null;
    private AlertDialog mExDialog = null;
    private AlertDialog mSuperDialog = null;

    private Button.OnClickListener mRepsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final FitLog fitLog = (FitLog)v.getTag();
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            LinearLayout layout = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.dialog_superset_reps_picker, null);
            builder.setView(layout);
            builder.setMessage("Select reps for \"" + fitLog.getString(EXERCISE) + "\"");

            NumberPicker numberPicker = (NumberPicker)layout.findViewById(R.id.numberPicker1);
            setNumberPickerColor(numberPicker, mContext.getResources().getDrawable(R.drawable.numberpicker_divider));
            numberPicker.setMinValue(1);
            numberPicker.setMaxValue(30);
            numberPicker.setValue(Integer.parseInt(fitLog.getDescription().substring(1)));
            numberPicker.setWrapSelectorWheel(false);
            builder.setPositiveButton(mContext.getString(R.string.action_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    fitLog.put(DESCRIPTION, "X" + ((NumberPicker)((AlertDialog) dialog).findViewById(R.id.numberPicker1)).getValue());
                    fitLog.getParentFitLog().refreshView(true);
                }
            });
            builder.show();
        }
    };

    public FitLog() {

    }

    public FitLog(int fitLogId, int index) {
        put("user", ParseUser.getCurrentUser());
        put(FIT_LOG_ID, fitLogId);
        put(INDEX, index);
    }

    static public void setNumberPickerColor(NumberPicker numberPicker, Drawable drawable) {
        Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    pf.set(numberPicker, drawable);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    static public FitLog buildHierarchy(List<FitLog> fitLogs) throws Exception {
        FitLog rootFitLog = null;
        Iterator<FitLog> it = fitLogs.iterator();
        while (it.hasNext()) {
            FitLog fitLog = it.next();
            fitLog.children.clear();
            if (fitLog.getInt(INDEX) == 0) {
                rootFitLog = fitLog;
            }
        }
        if (rootFitLog == null) {
            Exception e = new Exception();
            throw e;
        }

        it = fitLogs.iterator();
        while (it.hasNext()) {
            FitLog fitLog = it.next();
            if (fitLog.getInt(INDEX) == 0) {
                continue;
            } else {
                rootFitLog.addChild(fitLog);
            }
        }
        return rootFitLog;
    }

    public static FitLog fromJsonArray(String str) throws Exception {
        List<FitLog> list = new LinkedList<FitLog>();
        JSONArray array = new JSONArray(str);
        for (int i = 0; i < array.length(); i++) {
            JSONObject each = array.getJSONObject(i);
            list.add(fromJsonObject(each));
        }
        return buildHierarchy(list);
    }

    private static FitLog fromJsonObject(JSONObject jo) {
        FitLog log = new FitLog();
        log.put(FIT_LOG_ID, jo.optInt(FIT_LOG_ID));
        log.put(INDEX, jo.optInt(INDEX));
        log.put(EXERCISE, jo.optString(EXERCISE));
        log.put(DESCRIPTION, jo.optString(DESCRIPTION));
        log.put(ELAPSED_TIME, jo.optInt(ELAPSED_TIME));
        log.put(DESCRIPTION_REF, ParseObject.createWithoutData("Description", jo.optString(DESCRIPTION_REF)));
        log.put(DATA1, jo.optDouble(DATA1));
        log.put(DATA2, jo.optDouble(DATA2));
        log.put(DATA3, jo.optDouble(DATA3));
        log.put(DATA4, jo.optDouble(DATA4));
        return log;
    }


    private static boolean isSameExercise(FitLog fitLog1, FitLog fitLog2) {
        String exercise1 = fitLog1.getString(EXERCISE);
        String exercise2 = fitLog2.getString(EXERCISE);
        String desc1 = fitLog1.getParseObject(DESCRIPTION_REF).getObjectId();
        String desc2 = fitLog2.getParseObject(DESCRIPTION_REF).getObjectId();

        return exercise1.compareTo(exercise2) == 0 && desc1.compareTo(desc2) == 0;
    }

    private static LinkedList<FitLog> getDescList(FitLog fitLog) {
        LinkedList<FitLog> linkedList = new LinkedList<FitLog>();
        ListIterator<FitLog> it = fitLog.getParentFitLog().children.listIterator();
        while (it.next() != fitLog);
        it.previous();

        while (it.hasNext()) {
            FitLog nextFitLog = it.next();
            if (nextFitLog.getInt(INDEX) % 100 != 0 && isSameExercise(nextFitLog, fitLog)) {
                linkedList.add(nextFitLog);
            } else {
                break;
            }
        }
        return linkedList;
    }

    private static FitLog getLastFitLog(FitLog fitLog) {
        return getDescList(fitLog).getLast();
    }

    public FitLog getNewFitLog(int fitLogId) {
        FitLog newFitLog = new FitLog(fitLogId, getInt(INDEX));

        newFitLog.put(EXERCISE, getString(EXERCISE) == null ? "" : getString(EXERCISE));
        newFitLog.put(DESCRIPTION, getString(DESCRIPTION) == null ? "" : getString(DESCRIPTION));
        newFitLog.put(ELAPSED_TIME, getInt(ELAPSED_TIME));
        if (has(DESCRIPTION_REF)) {
            newFitLog.put(DESCRIPTION_REF, getParseObject(DESCRIPTION_REF));
            newFitLog.put(DATA1, getDouble(DATA1));
            newFitLog.put(DATA2, getDouble(DATA2));
            newFitLog.put(DATA3, getDouble(DATA3));
            newFitLog.put(DATA4, getDouble(DATA4));
        }

        return newFitLog;
    }

    public boolean validate() {
        if (children.isEmpty()) {
            return false;
        }

        Iterator<FitLog> it = children.iterator();
        while (it.hasNext()) {
            FitLog fitLog = it.next();
            if(fitLog.getInt(INDEX) % 100 == 0 && fitLog.children.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public FitLog getParentFitLog() {
        return mParentFitLog;
    }

    public void setParentFitLog(FitLog fitLog) {
        mParentFitLog = fitLog;
    }

    public int getNextIndex() {
        int index = getInt(INDEX);
        if (index == 0) {
            if (children.isEmpty()) {
                return 100;
            } else {
                return (children.getLast().getInt(INDEX) / 100) * 100 + 100;
            }
        } else if (index % 100 == 0) {
            if (children.isEmpty()) {
                return index + 1;
            } else {
                return children.getLast().getInt(INDEX) + 1;
            }
        } else {
            return 0;
        }
    }

    private void recalculateIndex() {
        int parentIndex = getInt(INDEX);
        int nextIndex = parentIndex == 0 ? 100 : parentIndex + 1;
        Iterator<FitLog> it = children.iterator();
        while (it.hasNext()) {
            FitLog fitLog = it.next();
            if (parentIndex == 0) {
                if (fitLog.getInt(INDEX) % 100 == 0) {
                    fitLog.put(INDEX, nextIndex);
                    fitLog.recalculateIndex();
                } else {
                    fitLog.put(INDEX, nextIndex + 1);
                }
                nextIndex += 100;
            } else {
                fitLog.put(INDEX, nextIndex);
                nextIndex++;
            }
        }
    }

    public void addChild(FitLog fitLog) {
        int index = fitLog.getInt(INDEX);

        ListIterator<FitLog> it = children.listIterator();
        while (it.hasNext()) {
            FitLog parent = it.next();
            if (parent.getInt(INDEX) == (index / 100) * 100) {
                parent.addChild(fitLog);
                return;
            }
        }

        it = children.listIterator();
        while(it.hasNext()) {
            FitLog parent = it.next();
            if (parent.getInt(INDEX) == index) {
                it.add(fitLog);
                recalculateIndex();
                return;
            }
        }

        children.addLast(fitLog);
        recalculateIndex();
    }

    public String toString() {
        String fitLogTree = getInt(INDEX) == 0 ? "FitLog of " + getParseUser("user").toString() + " : [" + getInt(FIT_LOG_ID) + "]\n" : "";

        Iterator<FitLog> it = children.iterator();
        while (it.hasNext()) {
            FitLog fitLog = it.next();
            if (fitLog.children.isEmpty()) {
                fitLogTree += " - " + fitLog.getInt(INDEX) + " " + fitLog.getString(EXERCISE) + " " + fitLog.getDescription() + " " + fitLog.getInt(ELAPSED_TIME) + "sec\n";
            } else {
                fitLogTree += " - Super / Cycle : " + fitLog.getString(EXERCISE) + " " + fitLog.getInt(ELAPSED_TIME) + "sec\n";
                fitLogTree += " - " + fitLog.toString();
            }
        }
        return fitLogTree;
    }

    public LinkedList<FitLog> getList() {
        LinkedList<FitLog> ret = new LinkedList<FitLog>();
        if (getInt(INDEX) != 0) {
            return ret;
        }
        ret.addLast(this);
        Iterator<FitLog> it = children.iterator();
        while (it.hasNext()) {
            FitLog fitLog = it.next();
            ret.addLast(fitLog);
            if (!fitLog.children.isEmpty()) {
                Iterator<FitLog> eit = fitLog.children.iterator();
                while (eit.hasNext()) {
                    ret.addLast(eit.next());
                }
            }
        }
        return ret;
    }

    public View createView(Context context, Boolean editable) {
        mContext = context;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        int index = getInt(INDEX);

        if (index == 0) {
            mLayout = (LinearLayout) inflater.inflate(R.layout.view_fitlog, null);
            ((TextView) mLayout.findViewById(R.id.fitLogName)).setText(getString(EXERCISE));
            if (getInt(ELAPSED_TIME) != 0) {
                ((TextView) mLayout.findViewById(R.id.fitLogTime)).setText("Time : " + getInt(ELAPSED_TIME) + "sec");
            } else {
                mLayout.findViewById(R.id.fitLogTime).setVisibility(View.GONE);
            }
            if (editable) {
                mLayout.findViewById(R.id.fitLogAddEx).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final FitLog fitLog = (FitLog) v.getTag();
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                        LinkedList<String> items = new LinkedList<String>();
                        items.add(mContext.getString(R.string.item_add_to) + "\"" + fitLog.getString(EXERCISE) + "\"");

                        Iterator<FitLog> it = fitLog.children.iterator();
                        while (it.hasNext()) {
                            FitLog child = it.next();
                            if (child.getInt(INDEX) % 100 == 0) {
                                items.add(mContext.getString(R.string.item_add_to) + "\"" + child.getString(EXERCISE) + "\"");
                            }
                        }
                        items.add(mContext.getString(R.string.item_add_to) + mContext.getString(R.string.item_new_superset));

                        builder.setItems(items.toArray(new String[items.size()]), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                addExercise(fitLog, which);
                            }
                        });
                        builder.show();
                    }
                });
                mLayout.findViewById(R.id.fitLogAddEx).setTag(this);
            } else {
                mLayout.findViewById(R.id.fitLogAddEx).setVisibility(View.GONE);
            }
            refreshView(editable);
            return mLayout;
        } else if (index % 100 == 0) {
            mLayout = (LinearLayout) inflater.inflate(R.layout.view_superset, null);
            if (editable) {
                mLayout.findViewById(R.id.superTitle).setOnClickListener(mRepsListener);
                mLayout.findViewById(R.id.superTitle).setTag(this);
            }
            ((TextView) mLayout.findViewById(R.id.superName)).setText(getString(EXERCISE));
            ((TextView) mLayout.findViewById(R.id.superReps)).setText(getDescription());
            if (getInt(ELAPSED_TIME) != 0) {
                ((TextView) mLayout.findViewById(R.id.superTime)).setText("Time : " + getInt(ELAPSED_TIME) + "sec");
            } else {
                mLayout.findViewById(R.id.superTime).setVisibility(View.GONE);
            }

            ListIterator<FitLog> it = children.listIterator();
            while (it.hasNext()) {
                FitLog child = it.next();
                child.setParentFitLog(this);
                LinearLayout childLayout = (LinearLayout)child.createView(mContext, editable);
                ((LinearLayout) mLayout.findViewById(R.id.superEx)).addView(childLayout);
                while (it.hasNext()) {
                    FitLog nextFitLog = it.next();
                    if (isSameExercise(child, nextFitLog)) {
                        RelativeLayout descLayout = (RelativeLayout)LayoutInflater.from(mContext).inflate(R.layout.view_desc, null);
                        ((TextView) descLayout.findViewById(R.id.exDesc)).setText(nextFitLog.getDescription());
                        ((LinearLayout)childLayout.findViewById(R.id.exDescLayout)).addView(descLayout);
                        nextFitLog.setParentFitLog(this);
                    } else {
                        it.previous();
                        break;
                    }
                }
            }
            return mLayout;
        } else {
            LinearLayout exLayout = (LinearLayout) inflater.inflate(R.layout.view_exercise, null);
            ((TextView) exLayout.findViewById(R.id.exName)).setText(getString(EXERCISE));
            RelativeLayout descLayout = (RelativeLayout)LayoutInflater.from(mContext).inflate(R.layout.view_desc, null);
            ((TextView) descLayout.findViewById(R.id.exDesc)).setText(getDescription());
            ((LinearLayout)exLayout.findViewById(R.id.exDescLayout)).addView(descLayout);
            if (editable) {
                exLayout.findViewById(R.id.fitLogAddDesc).setTag(this);
                exLayout.findViewById(R.id.fitLogAddDesc).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FitLog lastFitLog = getLastFitLog((FitLog) v.getTag());
                        addExercise(lastFitLog, 0);
                    }
                });
                exLayout.findViewById(R.id.fitLogAddDesc).setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        final LinkedList<FitLog> list = getDescList((FitLog) v.getTag());
                        showEditFitLogSelectionDialog(list);
                        return false;
                    }
                });
            }

            exLayout.findViewById(R.id.exAddGuideText).setVisibility(editable ? View.VISIBLE : View.GONE);

            if (getInt(ELAPSED_TIME) != 0) {
                ((TextView) exLayout.findViewById(R.id.exTime)).setText("Time : " + getInt(ELAPSED_TIME) + "sec");
            } else {
                exLayout.findViewById(R.id.exTime).setVisibility(View.GONE);
            }
            return exLayout;
        }
    }

    private void showEditFitLogSelectionDialog(final List<FitLog> list) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        LinkedList<String> items = new LinkedList<String>();
        for (FitLog each : list) {
            items.add(mContext.getString(R.string.item_modify) + each.getDescription());
        }

        builder.setItems(items.toArray(new String[items.size()]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FitLog selectedFitlog = list.get(which);
                showEditExDialog(selectedFitlog);
            }
        });
        builder.show();
    }

    private String getDescription() {
        if (has(DESCRIPTION_REF)) {
            ParseObject ref = getParseObject(DESCRIPTION_REF);
            if (ref.getObjectId() != null && ref.getObjectId().length() > 0) {
                ExDescription exDesc = FitterParseCache.getExDescription(ref.getObjectId());
                if (exDesc != null) {
                    return exDesc.toString(getDouble(DATA1), getDouble(DATA2), getDouble(DATA3), getDouble(DATA4));
                }
            }
        }
        return getString(DESCRIPTION);
    }

    private void refreshView(Boolean editable) {
        int index = getInt(INDEX);

        LinearLayout subLayout = null;
        if (index == 0) {
            subLayout = (LinearLayout) mLayout.findViewById(R.id.fitLogSubLayout);
        } else if (index % 100 == 0) {
            subLayout = (LinearLayout) mLayout.findViewById(R.id.superEx);
        }

        subLayout.removeAllViews();

        ListIterator<FitLog> it = children.listIterator();
        while (it.hasNext()) {
            FitLog child = it.next();
            child.setParentFitLog(this);
            if (child.getInt(INDEX) % 100 == 0) {
                subLayout.addView(child.createView(mContext, editable));
            } else {
                LinearLayout childLayout = (LinearLayout)child.createView(mContext, editable);
                subLayout.addView(childLayout);
                while (it.hasNext()) {
                    FitLog nextFitLog = it.next();
                    if (nextFitLog.getInt(INDEX) % 100 != 0 && isSameExercise(child, nextFitLog)) {
                        RelativeLayout descLayout = (RelativeLayout)LayoutInflater.from(mContext).inflate(R.layout.view_desc, null);
                        ((TextView) descLayout.findViewById(R.id.exDesc)).setText(nextFitLog.getDescription());
                        ((LinearLayout)childLayout.findViewById(R.id.exDescLayout)).addView(descLayout);
                        nextFitLog.setParentFitLog(this);
                    } else {
                        it.previous();
                        break;
                    }
                }
            }
        }
    }

    private void showExDialog(FitLog parent, final String newSuperName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        LinearLayout dialogLayout = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.dialog_exercise, null);
        builder.setView(dialogLayout);
        builder.setPositiveButton(mContext.getString(R.string.action_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // do nothing
            }
        });
        builder.setNegativeButton(mContext.getString(R.string.action_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // do nothing
            }
        });

        mExDialog = builder.create();

        String previousTemplateId = null;
        if (parent.getInt(INDEX) % 100 != 0) {
            ((EditText) dialogLayout.findViewById(R.id.exName)).setText(parent.getString(EXERCISE));
            dialogLayout.findViewById(R.id.exName).setEnabled(false);
            dialogLayout.findViewById(R.id.description_template_spinner).setEnabled(false);
            previousTemplateId = parent.getParseObject(DESCRIPTION_REF).getObjectId();
        }
        ((EditText) dialogLayout.findViewById(R.id.exTime)).setText("" + 0);


        // Prepare input view
        final DescriptionInputView descInputView = (DescriptionInputView) dialogLayout.findViewById(R.id.description_input_view);
        if (previousTemplateId != null) {
            descInputView.setPrevSelection(DATA1, parent.getDouble(DATA1));
            descInputView.setPrevSelection(DATA2, parent.getDouble(DATA2));
            descInputView.setPrevSelection(DATA3, parent.getDouble(DATA3));
            descInputView.setPrevSelection(DATA4, parent.getDouble(DATA4));
        }
        // Prepare data for spinner
        final List<ExDescription> listExDesc = FitterParseCache.listExDescriptions();
        List<String> exDescLabels = new LinkedList<>();
        int selectedPosition = -1;
        for (int i = 0; i < listExDesc.size(); i++) {
            ExDescription each = listExDesc.get(i);
            exDescLabels.add(each.getDescription());
            if (previousTemplateId != null && each.getObjectId().equalsIgnoreCase(previousTemplateId)) {
                selectedPosition = i;
            }
        }

        // Init spinner
        final Spinner spinner = (Spinner) dialogLayout.findViewById(R.id.description_template_spinner);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, android.R.id.text1);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAdapter.add(mContext.getString(R.string.hint_description_template_select));
        spinnerAdapter.addAll(exDescLabels);
        spinner.setAdapter(spinnerAdapter);
        if (selectedPosition != -1) {
            spinner.setSelection(selectedPosition + 1);
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    descInputView.loadData(null);
                } else {
                    ExDescription template = listExDesc.get(position - 1);
                    descInputView.loadData(template);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                descInputView.loadData(null);
            }
        });

        mExDialog.show();

        // override onClickListener not to be dismissed right after OK clicked.
        // to get button from dialog, dialog must be shown first.
        // if not working, dialog.setOnshowListener should be used.
        Button positiveButton = mExDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        positiveButton.setTag(parent);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FitLog parent = (FitLog) v.getTag();
                String exName = ((EditText) mExDialog.findViewById(R.id.exName)).getText().toString();
                String exDesc = ((EditText) mExDialog.findViewById(R.id.exDesc)).getText().toString();
                String exTime = ((EditText) mExDialog.findViewById(R.id.exTime)).getText().toString();
                String descId = descInputView.getDescriptionTemplateId();
                if (exName.compareTo("") == 0) {
                    Toast.makeText(mContext, "exercise name null", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (descId == null) {
                    Toast.makeText(mContext, "Please choose description template", Toast.LENGTH_SHORT).show();
                    return;
                }


                FitLog newSuper = null;
                FitLog newEx = null;
                if (newSuperName != null) {
                    newSuper = new FitLog(parent.getInt(FIT_LOG_ID), parent.getNextIndex());
                    newSuper.put(EXERCISE, newSuperName);
                    newSuper.put(DESCRIPTION, "X1");
                    newEx = new FitLog(parent.getInt(FIT_LOG_ID), newSuper.getNextIndex());
                } else if (parent.getInt(INDEX) == 0) {
                    newEx = new FitLog(parent.getInt(FIT_LOG_ID), parent.getNextIndex() + 1);
                } else if (parent.getInt(INDEX) % 100 == 0) {
                    newEx = new FitLog(parent.getInt(FIT_LOG_ID), parent.getNextIndex());
                } else {
                    // parent.getInt(INDEX) % 100 != 0
                    newEx = new FitLog(parent.getInt(FIT_LOG_ID), parent.getInt(INDEX));
                    parent = parent.getParentFitLog();
                }

                newEx.put(EXERCISE, exName);
                newEx.put(DESCRIPTION_REF, ParseObject.createWithoutData("Description", descId));
                newEx.put(DATA1, descInputView.getData(DATA1));
                newEx.put(DATA2, descInputView.getData(DATA2));
                newEx.put(DATA3, descInputView.getData(DATA3));
                newEx.put(DATA4, descInputView.getData(DATA4));

                if (exTime.compareTo("") == 0) {
                    newEx.put(ELAPSED_TIME, 0);
                } else {
                    newEx.put(ELAPSED_TIME, Integer.parseInt(exTime));
                }

                if (newSuper != null) {
                    parent.addChild(newSuper);
                }
                parent.addChild(newEx);
                parent.refreshView(true);
                mExDialog.cancel();
            }
        });
    }


    private void showEditExDialog(final FitLog target) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        LinearLayout dialogLayout = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.dialog_exercise, null);
        builder.setView(dialogLayout);

        dialogLayout.findViewById(R.id.exName).setEnabled(false);
        dialogLayout.findViewById(R.id.description_template_spinner).setEnabled(false);
        String previousTemplateId = target.getParseObject(DESCRIPTION_REF).getObjectId();

        ((EditText) dialogLayout.findViewById(R.id.exTime)).setText("" + 0);


        // Prepare input view
        final DescriptionInputView descInputView = (DescriptionInputView) dialogLayout.findViewById(R.id.description_input_view);
        descInputView.setPrevSelection(DATA1, target.getDouble(DATA1));
        descInputView.setPrevSelection(DATA2, target.getDouble(DATA2));
        descInputView.setPrevSelection(DATA3, target.getDouble(DATA3));
        descInputView.setPrevSelection(DATA4, target.getDouble(DATA4));

        // Prepare data for spinner
        final List<ExDescription> listExDesc = FitterParseCache.listExDescriptions();
        List<String> exDescLabels = new LinkedList<>();
        int selectedPosition = -1;
        for (int i = 0; i < listExDesc.size(); i++) {
            ExDescription each = listExDesc.get(i);
            exDescLabels.add(each.getDescription());
            if (previousTemplateId != null && each.getObjectId().equalsIgnoreCase(previousTemplateId)) {
                selectedPosition = i;
            }
        }

        // Init spinner
        final Spinner spinner = (Spinner) dialogLayout.findViewById(R.id.description_template_spinner);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, android.R.id.text1);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAdapter.add(mContext.getString(R.string.hint_description_template_select));
        spinnerAdapter.addAll(exDescLabels);
        spinner.setAdapter(spinnerAdapter);
        if (selectedPosition != -1) {
            spinner.setSelection(selectedPosition + 1);
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    descInputView.loadData(null);
                } else {
                    ExDescription template = listExDesc.get(position - 1);
                    descInputView.loadData(template);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                descInputView.loadData(null);
            }
        });

        builder.setPositiveButton(mContext.getString(R.string.action_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                target.put(DATA1, descInputView.getData(DATA1));
                target.put(DATA2, descInputView.getData(DATA2));
                target.put(DATA3, descInputView.getData(DATA3));
                target.put(DATA4, descInputView.getData(DATA4));
                target.getParentFitLog().refreshView(true);
            }
        });
        builder.setNegativeButton(mContext.getString(R.string.action_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        builder.show();
    }

    private void addExercise(final FitLog parent, int which) {
        // parent can be root or exercise
        int superCount = 0;
        if (which == 0) {
            // root fitlog or adding description
            showExDialog(parent, null);
            return;
        } else {
            Iterator<FitLog> it = parent.children.iterator();
            while (it.hasNext()) {
                FitLog child = it.next();
                if (child.getInt(INDEX) % 100 == 0 && ++superCount == which) {
                    // superset selected
                    showExDialog(child, null);
                    return;
                }
            }

            // new superset
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            LinearLayout dialogLayout = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.dialog_superset_name, null);
            builder.setView(dialogLayout);

            ((EditText) dialogLayout.findViewById(R.id.supersetName)).setText(mContext.getString(R.string.label_superset) + which);
            builder.setPositiveButton(mContext.getString(R.string.action_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // do nothing
                }
            });
            builder.setNegativeButton(mContext.getString(R.string.action_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // do nothing
                }
            });

            mSuperDialog = builder.create();
            mSuperDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            mSuperDialog.show();

            // override onClickListener not to be dismissed right after OK clicked.
            // to get button from dialog, dialog must be shown first.
            // if not working, dialog.setOnshowListener should be used.
            Button positiveButton = mSuperDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String supersetName = ((EditText)mSuperDialog.findViewById(R.id.supersetName)).getText().toString();
                    if (!supersetName.isEmpty()) {
                        showExDialog(parent, supersetName);
                        mSuperDialog.dismiss();
                    }
                }
            });
        }
    }

    public List<FitLog> getChildren() {
        return children;
    }

    public JSONArray toJsonArray() throws JSONException {
        JSONArray array = new JSONArray();
        for (FitLog each : getList()) {
            JSONObject jo = each.toJSONObject();
            array.put(jo);
        }
        return array;
    }

    private JSONObject toJSONObject() throws JSONException {
        JSONObject jo = new JSONObject();
        jo.put(FIT_LOG_ID, getInt(FIT_LOG_ID));
        jo.put(INDEX, getInt(INDEX));
        jo.put(EXERCISE, getString(EXERCISE));
        jo.put(DESCRIPTION, getDescription());
        jo.put(ELAPSED_TIME, getInt(ELAPSED_TIME));
        if (has(DESCRIPTION_REF)) {
            jo.put(DESCRIPTION_REF, getParseObject(DESCRIPTION_REF).getObjectId());
            jo.put(DATA1, getDouble(DATA1));
            jo.put(DATA2, getDouble(DATA2));
            jo.put(DATA3, getDouble(DATA3));
            jo.put(DATA4, getDouble(DATA4));
        }
        return jo;
    }
}

