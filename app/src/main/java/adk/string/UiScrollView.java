package adk.string;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

/**
 * Created by Adk on 12/26/2017.
 * UiScrollView is built using {@link ScrollView}. In this view, none of the touch events are
 * intercepted and all of them are sent to a parent view which can be set using the
 * {@link #setParent(View)}.
 */
public class UiScrollView extends ScrollView {

    private View parent;

    public UiScrollView(Context context) {
        super(context);
    }

    public UiScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        parent.onTouchEvent(ev);
        return false;
    }

    public void setParent(View parent) {
        this.parent = parent;
    }
}
