package co.launcharea.fitter.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.TableRow;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import co.launcharea.fitter.R;
import co.launcharea.fitter.model.ExDescription;
import co.launcharea.fitter.model.FitLog;

/**
 * Created by jack on 15. 8. 20.
 */
public class DescriptionInputView extends FrameLayout {
    Map<String, Double> mapSelectedValue = new HashMap<>();
    Map<String, Double> mapPrevSelections = new HashMap<>();
    private LayoutInflater mInflater;
    private ViewGroup mRootView;
    private TableRow mContentArea;
    private ExDescription mDescriptionTemplate;

    public DescriptionInputView(Context context) {
        this(context, null);
    }

    public DescriptionInputView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DescriptionInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        mInflater = LayoutInflater.from(getContext());
        mRootView = (ViewGroup) mInflater.inflate(R.layout.view_exercise_description_input, null);
        mContentArea = (TableRow) mRootView.findViewById(R.id.content_area);
        addView(mRootView);
        mRootView.setVisibility(View.GONE);
    }

    public void loadData(ExDescription description) {
        mDescriptionTemplate = description;
        mContentArea.removeAllViews();
        if (description == null) {
            mRootView.setVisibility(View.GONE);
        } else {
            for (int i = 0; i < description.numItems(); i++) {
                ExDescription.Item item = description.getItem(i);
                if (item.type == ExDescription.TYPE_TIME) {
                    View v = mInflater.inflate(R.layout.view_exercise_description_time_picker, null);
                    bindTimeData(v, item);
                    mContentArea.addView(v);
                } else {
                    View v = mInflater.inflate(R.layout.view_exercise_description_number_picker, null);
                    bindNumberData(v, item);
                    mContentArea.addView(v);
                }

            }
            mRootView.setVisibility(View.VISIBLE);
        }
    }

    private void bindNumberData(View v, final ExDescription.Item item) {
        final NumberPicker picker = (NumberPicker) v.findViewById(R.id.data_picker);
        FitLog.setNumberPickerColor(picker, getResources().getDrawable(R.drawable.numberpicker_divider));
        final List<String> displayValues = new LinkedList<>();
        final List<Double> values = new LinkedList<>();

        Double prev = mapPrevSelections.get(item.columnName);

        boolean isFloating = ((double) ((int) item.diff)) != item.diff;

        double value = item.min;
        int defaultPosition = 0;
        while (value <= item.max) {
            values.add(value);
            if (isFloating) {
                displayValues.add(String.format("%.1f%s", value, item.label));
            } else {
                displayValues.add(String.format("%d%s", (int) value, item.label));
            }


            if (prev != null) {
                if (value < prev) {
                    defaultPosition++;
                }
            } else {
                if (value < item.defaultValue) {
                    defaultPosition++;
                }
            }
            value = value + item.diff;
        }

        picker.setDisplayedValues(displayValues.toArray(new String[]{}));
        picker.setWrapSelectorWheel(false);
        picker.setMinValue(0);
        picker.setMaxValue(displayValues.size() - 1);
        picker.setValue(defaultPosition);
        mapSelectedValue.put(item.columnName, values.get(defaultPosition));

        picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mapSelectedValue.put(item.columnName, values.get(newVal));
            }
        });
    }

    private void bindTimeData(View v, final ExDescription.Item item) {

        final NumberPicker minPicker = (NumberPicker) v.findViewById(R.id.min_picker);
        FitLog.setNumberPickerColor(minPicker, getResources().getDrawable(R.drawable.numberpicker_divider));
        minPicker.setMinValue(0);
        minPicker.setMaxValue(60);
        minPicker.setWrapSelectorWheel(true);
        minPicker.setValue(10);

        final NumberPicker secPicker = (NumberPicker) v.findViewById(R.id.sec_picker);
        FitLog.setNumberPickerColor(secPicker, getResources().getDrawable(R.drawable.numberpicker_divider));
        secPicker.setMinValue(0);
        secPicker.setMaxValue(59);
        secPicker.setWrapSelectorWheel(true);
        secPicker.setValue(0);
        secPicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                return String.format("%02d", value);
            }
        });


        Double prev = mapPrevSelections.get(item.columnName);
        if (prev != null) {
            int time = prev.intValue();
            minPicker.setValue(time / 60);
            secPicker.setValue(time % 60);
        }

        mapSelectedValue.put(item.columnName, Double.valueOf(minPicker.getValue() * 60 + secPicker.getValue()));
        NumberPicker.OnValueChangeListener listener = new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mapSelectedValue.put(item.columnName, Double.valueOf(minPicker.getValue() * 60 + secPicker.getValue()));
            }
        };
        minPicker.setOnValueChangedListener(listener);
        secPicker.setOnValueChangedListener(listener);
    }

    public String getDescriptionTemplateId() {
        if (mDescriptionTemplate == null) {
            return null;
        } else {
            return mDescriptionTemplate.getObjectId();
        }
    }

    public Double getData(String columnName) {
        Double data = mapSelectedValue.get(columnName);
        if (data == null) {
            return Double.valueOf(0);
        } else {
            return data;
        }
    }

    public void setPrevSelection(String columnName, double data) {
        mapPrevSelections.put(columnName, data);
    }
}