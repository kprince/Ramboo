package com.xiaoming.random.widgets;

import android.content.Context;
import android.text.Spannable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/**
 * TextView that allows to insert clickablespans while whole textview is still clickable<br>
 * If a click an a clickablespan occurs, click handler of whole textview will <b>not</b> be invoked
 * In your span onclick handler you first have to check whether {@link ignoreSpannableClick} returns true, if so just return from click handler
 * otherwise call {@link preventNextClick} and handle the click event
 *
 * @author Lukas
 */
public class ClickPreventableTextView extends TextView implements View.OnClickListener {
    private boolean preventClick;
    private OnClickListener clickListener;
    private boolean ignoreSpannableClick;

    public ClickPreventableTextView(Context context) {
        super(context);
    }

    public ClickPreventableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ClickPreventableTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (getMovementMethod() != null)
            getMovementMethod().onTouchEvent(this, (Spannable) getText(), event);
        this.ignoreSpannableClick = true;
        boolean ret = super.onTouchEvent(event);
        this.ignoreSpannableClick = false;
        return ret;
    }

    /**
     * Returns true if click event for a clickable span should be ignored
     *
     * @return true if click event should be ignored
     */
    public boolean ignoreSpannableClick() {
        return ignoreSpannableClick;
    }

    /**
     * Call after handling click event for clickable span
     */
    public void preventNextClick() {
        preventClick = true;
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        this.clickListener = listener;
        super.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (preventClick) {
            preventClick = false;
        } else if (clickListener != null)
            clickListener.onClick(v);
    }
}