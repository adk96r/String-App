package adk.string;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Crafted by Adk on a midnight.
 * <p>
 * This view is rendered as a box on the screen with rounder corners. The user draws this box
 * by touching the screen (ACTION_DOWN), dragging across the diagonal of the box (ACTION_MOVE)
 * and finally lifting the finger up (ACTION_UP). The update to the view during the finger
 * movement is controlled from the {@link GestureDrivenStringDetector#onTouch(View, MotionEvent)}.
 */
public class StringOverlayBoxView extends View {

    private Rect rect;
    private Paint centerRectPaint, cornerArcPaint;
    private int cornerArcGap = 15;

    public StringOverlayBoxView(Context context) {
        super(context);
        init(context);
    }

    public StringOverlayBoxView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * Initialises the paints used to draw the center box and the corner arcs.
     *
     * @param context Context for getting color resources.
     */
    private void init(Context context) {
        rect = new Rect(0, 0, 0, 0);

        centerRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerRectPaint.setColor(ContextCompat.getColor(context, R.color.white));
        centerRectPaint.setStyle(Paint.Style.STROKE);
        centerRectPaint.setStrokeWidth(4);

        cornerArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cornerArcPaint.setColor(ContextCompat.getColor(context, R.color.colorAccent));
        cornerArcPaint.setStyle(Paint.Style.STROKE);
        cornerArcPaint.setStrokeWidth(4);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBoxWithCornerArcs(canvas);
    }

    /**
     * Update the box's rect with the given rect.
     *
     * @param boxRect Rect that represents the new box.
     */
    public void setBoxRect(Rect boxRect) {
        this.rect = boxRect;
    }

    public void invertPrimaryPaint(Context context) {
        centerRectPaint.setColor(ContextCompat.getColor(context, R.color.black));
    }

    /**
     * Draws the box with rounded corners and arcs outside the corners.
     *
     * @param canvas Canvas onto which the box is drawn.
     */
    private void drawBoxWithCornerArcs(Canvas canvas) {

        // Main Box
        canvas.drawRoundRect(Utility.orientF(rect), cornerArcGap / 2, cornerArcGap / 2, centerRectPaint);

        //canvas.drawRoundRect(bigger(Utility.orientF(rect)), cornerArcGap / 2, cornerArcGap / 2, cornerArcPaint);

        // Don't draw if the width is too small.
        if (Math.abs(rect.width()) < 2 * cornerArcGap) return;

        // Corner Arcs
        Rect cRect = Utility.orient(rect);

        canvas.drawArc((float) cRect.left - cornerArcGap, (float) cRect.top - cornerArcGap,
                (float) cRect.left + cornerArcGap, (float) cRect.top + cornerArcGap,
                180, 90, false, cornerArcPaint);
        canvas.drawLine(cRect.left, cRect.top - cornerArcGap, cRect.left + cornerArcGap, cRect.top - cornerArcGap, cornerArcPaint);
        canvas.drawLine(cRect.left - cornerArcGap, cRect.top, cRect.left - cornerArcGap, cRect.top + cornerArcGap, cornerArcPaint);

        // Top Right
        canvas.drawArc((float) cRect.right - cornerArcGap, (float) cRect.top - cornerArcGap,
                (float) cRect.right + cornerArcGap, (float) cRect.top + cornerArcGap,
                270, 90, false, cornerArcPaint);
        canvas.drawLine(cRect.right - cornerArcGap, cRect.top - cornerArcGap, cRect.right, cRect.top - cornerArcGap, cornerArcPaint);
        canvas.drawLine(cRect.right + cornerArcGap, cRect.top, cRect.right + cornerArcGap, cRect.top + cornerArcGap, cornerArcPaint);

        // Bottom Right
        canvas.drawArc((float) cRect.right - cornerArcGap, (float) cRect.bottom - cornerArcGap,
                (float) cRect.right + cornerArcGap, (float) cRect.bottom + cornerArcGap,
                0, 90, false, cornerArcPaint);
        canvas.drawLine(cRect.left, cRect.bottom + cornerArcGap, cRect.left + cornerArcGap, cRect.bottom + cornerArcGap, cornerArcPaint);
        canvas.drawLine(cRect.right + cornerArcGap, cRect.bottom, cRect.right + cornerArcGap, cRect.bottom - cornerArcGap, cornerArcPaint);

        // Bottom Left
        canvas.drawArc((float) cRect.left - cornerArcGap, (float) cRect.bottom - cornerArcGap,
                (float) cRect.left + cornerArcGap, (float) cRect.bottom + cornerArcGap,
                90, 90, false, cornerArcPaint);
        canvas.drawLine(cRect.right - cornerArcGap, cRect.bottom + cornerArcGap, cRect.right, cRect.bottom + cornerArcGap, cornerArcPaint);
        canvas.drawLine(cRect.left - cornerArcGap, cRect.bottom, cRect.left - cornerArcGap, cRect.bottom - cornerArcGap, cornerArcPaint);

    }
}
