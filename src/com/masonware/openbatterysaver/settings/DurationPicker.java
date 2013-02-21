package com.masonware.openbatterysaver.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TimePicker;

public class DurationPicker extends TimePicker {
	
	public DurationPicker(Context context) {
        super(context);
        init();
    }

    public DurationPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DurationPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    
    private void init() {
    	setIs24HourView(true);
    	setCurrentHour(0);
    	setCurrentMinute(15);
    }
    
}
