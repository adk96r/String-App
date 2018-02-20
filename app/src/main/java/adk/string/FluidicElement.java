package adk.string;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Point;
import android.support.animation.DynamicAnimation;
import android.support.animation.FlingAnimation;
import android.support.animation.SpringAnimation;
import android.support.animation.SpringForce;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

/**
 * ADK started crafting this at EPOCH 1514051586.
 */

public class FluidicElement extends RelativeLayout {

    private static final String TAG = FluidicElement.class.getSimpleName();
    private static final int HORIZONTAL = 0;
    private static final int VERTICAL = 1;
    private static final float FLING_FRICTION = 0.3f;
    private static final float SPRING_DAMPING_RATIO = SpringForce.DAMPING_RATIO_LOW_BOUNCY;
    private static final float SPRING_STIFFNESS = SpringForce.STIFFNESS_VERY_LOW;

    private boolean fluidityX = true;
    private boolean fluidityY = true;
    private SpringForce forceX, forceY;
    private SpringAnimation springX, springY;
    private FlingAnimation flingX, flingY;

    private float minX, minY, maxX, maxY;

    private VelocityTracker mVelocityTracker;
    private float curX, curY, lastTouchX, lastTouchY;
    private FludicPositionChangeListener listener;

    public FluidicElement(Context context) {
        super(context);
        initialiseFluidity(context);
    }

    public FluidicElement(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialiseFluidity(context);
    }

    private void initialiseFluidity(Context context) {

        if (fluidityX) curX = getX();
        if (fluidityY) curY = getY();

        // Do nothing.
        this.listener = new FludicPositionChangeListener() {
            @Override
            public void onPositionChanged(float newX, float newY) {

            }
        };
        listener.onPositionChanged(curX, curY);

        final Point screenSize = new Point();
        ((MainActivity) context).getWindowManager().getDefaultDisplay().getSize(screenSize);

        post(new Runnable() {
            @Override
            public void run() {
                setupFlings();
                setupSprings();
            }
        });
    }

    private void setupFlings() {

        flingX = new FlingAnimation(this, DynamicAnimation.X)
                .setMinValue(minX)
                .setMaxValue(maxX)
                .setFriction(FLING_FRICTION)
                .addUpdateListener(new DynamicAnimation.OnAnimationUpdateListener() {
                    @Override
                    public void onAnimationUpdate(DynamicAnimation animation, float value,
                                                  float velocity) {
                        updateXY(value, curY, "setupFlings-X");
                        if (!withinBounds(curX, HORIZONTAL)) {
                            animation.cancel();
                            springBackIntoBounds(HORIZONTAL);
                        }
                    }
                });

        flingY = new FlingAnimation(this, DynamicAnimation.Y)
                .setMinValue(minY)
                .setMaxValue(maxY)
                .setFriction(FLING_FRICTION)
                .addUpdateListener(new DynamicAnimation.OnAnimationUpdateListener() {
                    @Override
                    public void onAnimationUpdate(DynamicAnimation animation, float value,
                                                  float velocity) {
                        updateXY(curX, value, "setupFlings-Y");
                        if (!withinBounds(curY, VERTICAL)) {
                            animation.cancel();
                            springBackIntoBounds(VERTICAL);
                        }
                    }
                });

    }

    private void setupSprings() {

        forceX = new SpringForce().setStiffness(SPRING_STIFFNESS)
                .setDampingRatio(SPRING_DAMPING_RATIO);

        springX = new SpringAnimation(this, DynamicAnimation.X)
                .setSpring(forceX);

        springX.addUpdateListener(new DynamicAnimation.OnAnimationUpdateListener() {
            @Override
            public void onAnimationUpdate(DynamicAnimation animation, float value, float velocity) {
                updateXY(value, curY, "setupSprings-X");
            }
        });


        forceY = new SpringForce().setStiffness(SpringForce.STIFFNESS_MEDIUM)
                .setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);

        springY = new SpringAnimation(this, DynamicAnimation.Y)
                .setSpring(forceY);

        springY.addUpdateListener(new DynamicAnimation.OnAnimationUpdateListener() {
            @Override
            public void onAnimationUpdate(DynamicAnimation animation, float value, float velocity) {
                updateXY(curX, value, "setupSprings-Y");
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float dX, dY;

        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN: {

                lastTouchX = event.getRawX();
                lastTouchY = event.getRawY();

                if (mVelocityTracker == null) {
                    mVelocityTracker = VelocityTracker.obtain();
                } else {
                    mVelocityTracker.clear();
                }

                mVelocityTracker.addMovement(event);
                return true;
            }

            case MotionEvent.ACTION_MOVE: {

                mVelocityTracker.addMovement(event);

                if (fluidityX) {

                    if (flingX.isRunning()) {
                        flingX.cancel();
                    }

                    dX = event.getRawX() - lastTouchX;
                    curX += dX;
                    setX(curX);
                    lastTouchX = event.getRawX();
                }

                if (fluidityY) {

                    if (flingY.isRunning()) {
                        flingY.cancel();
                    }

                    dY = event.getRawY() - lastTouchY;
                    curY += dY;
                    setY(curY);
                    lastTouchY = event.getRawY();
                }

                updateXY(curX, curY, "onTouch-ACTION_MOVE");
                return true;
            }

            case MotionEvent.ACTION_UP: {

                mVelocityTracker.addMovement(event);

                if (withinBounds(curX, HORIZONTAL)
                        && withinBounds(curY, VERTICAL)) {

                    // Create any flings based on the velocity.
                    mVelocityTracker.computeCurrentVelocity(1000);

                    if (fluidityX) {

                        if (flingY.isRunning()) {
                            flingX.cancel();
                        }

                        float vX = mVelocityTracker.getXVelocity();
                        flingX.setStartValue(curX)
                                .setStartVelocity(2 * vX)
                                .start();
                    }

                    if (fluidityY) {

                        if (flingY.isRunning()) {
                            flingY.cancel();
                        }

                        float vY = mVelocityTracker.getYVelocity();
                        flingY.setStartValue(curY)
                                .setStartVelocity(2 * vY)
                                .start();
                    }

                } else {

                    if (fluidityX && !withinBounds(curX, HORIZONTAL)) {
                        springBackIntoBounds(HORIZONTAL);
                    }

                    if (fluidityY && !withinBounds(curY, VERTICAL)) {
                        springBackIntoBounds(VERTICAL);
                    }
                }

                return true;
            }

            case MotionEvent.ACTION_CANCEL: {
                mVelocityTracker.recycle();
                break;
            }
        }
        return false;
    }

    boolean withinBounds(float value, int mode) {
        switch (mode) {
            case HORIZONTAL:
                return (minX <= value && value <= maxX);
            case VERTICAL:
                return (minY <= value && value <= maxY);
            default:
                return true;
        }
    }

    private void springBackIntoBounds(int mode) {

        switch (mode) {
            case HORIZONTAL: {
                flingX.cancel();
                springX.cancel();
                forceX.setFinalPosition(curX < minX ? minX : maxX);
                springX.setSpring(forceX)
                        .start();
                break;
            }
            case VERTICAL: {
                flingY.cancel();
                springY.cancel();
                forceY.setFinalPosition(curY < minY ? minY : maxY);
                springY.setSpring(forceY)
                        .start();
                break;
            }
        }

    }

    public void setFluidityX(boolean fluidityX) {
        this.fluidityX = fluidityX;
    }

    public void setFluidityY(boolean fluidityY) {
        this.fluidityY = fluidityY;
    }

    public void flowDown() {
        stopMotion();
        animate()
                .y(maxY)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(300)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        updateXY(curX, maxY, "flowDown");
                    }
                })

                .setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        listener.onPositionChanged(curX, getY());
                    }
                }).start();

    }

    public void flowUp() {
        stopMotion();
        flowUp(minY);
    }

    public void flowRight() {
        animate()
                .x(maxX)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(300)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        updateXY(maxX, curY, "flowRight");
                    }
                }).start();
    }

    public void flowLeft() {

        animate()
                .x(minX)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(300)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        updateXY(minX, curY, "flowLeft");
                    }
                }).start();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

    }

    public void addPositionUpdateListener(FludicPositionChangeListener listener) {
        this.listener = listener;
    }

    private void updateXY(float x, float y, @Nullable String origin) {
        curX = x;
        curY = y;
        listener.onPositionChanged(curX, curY);
    }

    private void flowHorizontally(final float x) {
        animate()
                .y(minX < x ? ((x < maxX) ? x : maxX) : minX)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(300)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        updateXY(getX(), curY, "flowHorizontally");
                    }
                })
                .setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        listener.onPositionChanged(getX(), curY);
                    }
                })
                .start();
    }

    private void flowVertically(final float y) {

        animate()
                .y(minY < y ? ((y < maxY) ? y : maxY) : minY)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(300)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        updateXY(curX, getY(), "flowHorizontally");
                    }
                })
                .setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        listener.onPositionChanged(curX, getY());
                    }
                })
                .start();
    }

    void setBounds(final float minX, final float minY, float maxX, float maxY) {
        setBounds(minX, minY, maxX, maxY, true, minX, minY);
    }

    public void setBounds(float minX, float minY, float maxX, float maxY,
                          boolean updateXYCoordinates, float newX, float newY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        if (updateXYCoordinates) {
            flowHorizontally(newX);
            flowVertically(newY);
        }
        setupSprings();
        setupFlings();
    }

    public float getMinX() {
        return minX;
    }

    public float getMinY() {
        return minY;
    }

    public float getMaxX() {
        return maxX;
    }

    public float getMaxY() {
        return maxY;
    }

    public void flowUp(float y) {
        animate()
                .y(minY < y ? ((y < maxY) ? y : maxY) : minY)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(300)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        updateXY(curX, minY, "flowUp");
                    }
                })
                .setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        listener.onPositionChanged(curX, getY());
                    }
                })
                .start();
    }

    private void stopMotion() {
        try {
            flingX.cancel();
            flingY.cancel();
            springX.cancel();
            springY.cancel();
        } catch (Exception e) {
            Log.d(TAG, "Failed to stop the animations - " + e.getMessage());
        }
    }
}