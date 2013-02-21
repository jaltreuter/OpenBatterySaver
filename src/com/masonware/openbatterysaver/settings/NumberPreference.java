package com.masonware.openbatterysaver.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.masonware.openbatterysaver.R;

public class NumberPreference extends DialogPreference {
    private NumberPicker picker = null;
    private int minValue, maxValue, increment;
    private String[] displayedValues;
    private String units;
    private int value;

    public NumberPreference(Context ctxt, AttributeSet attrs) {
        super(ctxt, attrs);
        init(attrs);
    }
    
    private void init(AttributeSet attrs) {
    	TypedArray a = getContext().obtainStyledAttributes(attrs,R.styleable.NumberPreference);
    	minValue = a.getInt(R.styleable.NumberPreference_minValue, 0);
    	maxValue = a.getInt(R.styleable.NumberPreference_maxValue, 10);
    	increment = a.getInt(R.styleable.NumberPreference_increment, 1);
    	units = a.getString(R.styleable.NumberPreference_unitLabel);
    	a.recycle();
    }

    @Override
    protected View onCreateDialogView() {
        picker = new NumberPicker(getContext());
        ViewGroup.LayoutParams pickerParams = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        picker.setLayoutParams(pickerParams);
        picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        picker.setFocusable(false);
        if(units == null) {
        	return picker;
        }
        View v = View.inflate(getContext(), R.layout.number_preference_dialog, null);
        TextView tv = (TextView)v.findViewById(R.id.units);
        tv.setText(units);
        tv.setVisibility(View.VISIBLE);
        ViewGroup vg = (ViewGroup)v.findViewById(R.id.picker_holder);
        vg.addView(picker);
        return v;
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        int size = (int)Math.floor(((double)(maxValue - minValue)) / increment) + 1;
        displayedValues = new String[size];
        int defaultDisplayValue = picker.getValue();
        int curValue = (int)minValue;
        for(int i=0; i<size; i++) {
        	int displayedValue = (int)Math.min(curValue, maxValue);
        	displayedValues[i] = Integer.toString(displayedValue);
        	if(displayedValue == value) {
        		defaultDisplayValue = i;
        	}
        	curValue += increment;
        }
        picker.setMinValue(0);
        picker.setMaxValue(size-1);
        picker.setDisplayedValues(displayedValues);
        picker.setWrapSelectorWheel(false);
        picker.setValue(defaultDisplayValue);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
        	value = Integer.parseInt(displayedValues[picker.getValue()]);
            if (callChangeListener(value)) {
                persistInt(value);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return(a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (restoreValue) {
            if (defaultValue == null) {
                value = getPersistedInt(0);
            } else {
            	value = getPersistedInt(Integer.parseInt(defaultValue.toString()));
            }
        } else {
        	value = Integer.parseInt(defaultValue.toString());
        }
    }
}
