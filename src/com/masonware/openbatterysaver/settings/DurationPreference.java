package com.masonware.openbatterysaver.settings;

import com.masonware.openbatterysaver.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

public class DurationPreference extends DialogPreference {
    private int lastMajor = 0;
    private int lastMinor = 0;
    private DurationPicker picker = null;
    private int units;

    public DurationPreference(Context ctxt, AttributeSet attrs) {
        super(ctxt, attrs);
        init(attrs);
    }
    
    private void init(AttributeSet attrs) {
    	TypedArray a = getContext().obtainStyledAttributes(attrs,R.styleable.DurationPreference);
    	units = a.getInteger(R.styleable.DurationPreference_units, 1);
    	a.recycle();
    }

    @Override
    protected View onCreateDialogView() {
        picker = new DurationPicker(getContext());
        ViewGroup.LayoutParams pickerParams = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        picker.setLayoutParams(pickerParams);
        View v = View.inflate(getContext(), R.layout.duration_preference_dialog, null);
        TextView tv = (TextView)v.findViewById(R.id.units);
        tv.setText(getUnitString(units));
        tv.setVisibility(View.VISIBLE);
        ViewGroup vg = (ViewGroup)v.findViewById(R.id.picker_holder);
        vg.addView(picker);
        return v;
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        picker.setCurrentHour(lastMajor);
        picker.setCurrentMinute(lastMinor);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            lastMajor = picker.getCurrentHour();
            lastMinor = picker.getCurrentMinute();
            String summary = getValueString();
            int time = getValueMillis(lastMajor, lastMinor, units);

            if (callChangeListener(time)) {
                persistInt(time);
                setSummary(summary);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return(a.getInt(index, 0));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        int time;
        if (restoreValue) {
            if (defaultValue==null) {
                time = getPersistedInt(0);
            } else {
                time = getPersistedInt((Integer)defaultValue);
            }
        } else {
            time = (Integer)defaultValue;
        }
        lastMajor = getMajor(time, units);
        lastMinor = getMinor(time, units);
        setSummary(getValueString());
    }

    private static int getMajor(int millis, int units) {
    	switch(units) {
    	case 0:
//            return millis / (1000 * 60 * 60);
    		return millis / 3600000;
    	case 1:
//    		return millis / (1000 * 60);
    		return millis / 60000;
    	default:
    		return 0;
    	}
    }

    private static int getMinor(int millis, int units) {
    	switch(units) {
    	case 0:
            return (millis / (1000 * 60)) % 60;
    	case 1:
    		return (millis / 1000) % 60;
    	default:
    		return 0;
    	}
    }
    
    private static String getUnitString(int units) {
    	switch(units) {
    	case 0:
    		return "hh:mm";
    	case 1:
    		return "mm:ss";
    	default:
    		return "";
    	}
    }
    
    private static int getValueMillis(int major, int minor, int units) {
    	int majorMultiplier = 0;
    	int minorMultiplier = 0;
    	switch(units) {
    	case 0:
    		majorMultiplier = 60 * 60 * 1000;
    		minorMultiplier = 60 * 1000;
    		break;
    	case 1:
    		majorMultiplier = 60 * 1000;
    		minorMultiplier = 1000;
    		break;
    	}
    	return (majorMultiplier * major) + (minorMultiplier * minor);
    }
    
    private String getValueString() {
    	String first = String.valueOf(lastMajor);
        if(first.length() == 1) {
        	first = "0" + first;
        }
        String second = String.valueOf(lastMinor);
        if(second.length() == 1) {
        	second = "0" + second;
        }
        return first + ":" + second;
    }
}
