package com.xiaoming.random.widgets;

import android.content.Context;
import android.util.AttributeSet;

import com.gc.materialdesign.views.ButtonFloat;

/**
 * Created by XM on 2015/2/28.
 */
public class RandomButtonFloat extends ButtonFloat {
    /*I noticed this behavior for the ButtonFloat too.
    It seems that setAttributes gets not called in the Constructor of ButtonFloat.
    Right now i get around by subclassing it and calling it in the Constructor.
    Maybe this helps here too.

    public class MyButtonFloat extends ButtonFloat
    {
        public MyButtonFloat(final Context context, final AttributeSet attrs)
        {
            super(context, attrs);

            setAttributes(attrs);
        }
    }*/
    public RandomButtonFloat(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAttributes(attrs);
    }
}
