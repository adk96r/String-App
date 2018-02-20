package adk.string;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;

public class OnboardingUsageFragment extends Fragment {

    private Point mScreenSize;
    private StringOverlayBoxView mBox;
    private CameraSource mSource;
    private SurfaceView mSurfaceView;
    private View mOnboardingOverlayView;

    private View mInstructionWrapper;
    private TextView mInstruction1;
    private TextView mInstruction2;
    private TextView mInstruction3;
    private TextView mInstruction4;
    private TextView mInstruction5;
    private View topCard;
    private View bottomCard;
    DecelerateInterpolator mInterpolator;

    private int gap = 500;

    public OnboardingUsageFragment() {
        // Required empty public constructor
    }

    public static OnboardingUsageFragment newInstance(String param1, String param2) {
        return new OnboardingUsageFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_onboarding_usage, container, false);

        mBox = view.findViewById(R.id.box);
        mSurfaceView = view.findViewById(R.id.demo_camera_surface);
        mOnboardingOverlayView = view.findViewById(R.id.onboarding_overlay);

        mInstructionWrapper = view.findViewById(R.id.instr_wrapper);
        mInstruction1 = view.findViewById(R.id.instr1);
        mInstruction2 = view.findViewById(R.id.instr2);
        mInstruction3 = view.findViewById(R.id.instr3);
        mInstruction4 = view.findViewById(R.id.instr4);
        mInstruction5 = view.findViewById(R.id.instr5);
        topCard = view.findViewById(R.id.top_card_onboarding);
        bottomCard = view.findViewById(R.id.bottom_card_onboarding);
        mInterpolator = new DecelerateInterpolator();

        mSurfaceView.post(new Runnable() {
            @Override
            public void run() {

                try {
                    mScreenSize = new Point();
                    getActivity().getWindowManager().getDefaultDisplay().getSize(mScreenSize);

                    mSource = new CameraSource.Builder(getContext(), new NullDetector())
                            .setAutoFocusEnabled(true)
                            .setRequestedFps(60)
                            .setFacing(CameraSource.CAMERA_FACING_BACK)
                            .setRequestedPreviewSize(mScreenSize.y, mScreenSize.x)
                            .build();

                    mSource.start(mSurfaceView.getHolder());

                } catch (Exception ignored) {

                }
            }
        });

        mBox.invertPrimaryPaint(getContext());
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mSource != null) {
            mSource.release();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        mSurfaceView.post(new Runnable() {
            @Override
            public void run() {

                mScreenSize = new Point(Resources.getSystem().getDisplayMetrics().widthPixels,
                        Resources.getSystem().getDisplayMetrics().heightPixels);


                final Rect bounds = new Rect((int) mInstructionWrapper.getX(),
                        (int) mInstructionWrapper.getY(),
                        (int) mInstructionWrapper.getX() + mInstructionWrapper.getWidth(),
                        (int) mInstructionWrapper.getY() + mInstructionWrapper.getHeight());
                final Rect progressive = new Rect();
                ValueAnimator animator = ValueAnimator.ofInt(1000);
                animator.setStartDelay(1500);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float f = ((Integer) animation.getAnimatedValue()) / (float) 1000;
                        progressive.set(bounds.left, bounds.top,
                                bounds.left + (int) (f * bounds.width()),
                                bounds.top + (int) (f * bounds.height()));
                        mBox.setBoxRect(progressive);
                        mBox.invalidate();
                    }
                });
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animation.start();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                animator.setDuration(1500)
                        .setInterpolator(new DecelerateInterpolator());
                animator.start();
            }
        });

    }


    public void showInstruction(int instruction) {

        switch (instruction) {
            case 1: {
                // Hide instruction 2
                toggleInstruction2(false);

                // Show instruction 1
                toggleInstruction1(true);
                break;
            }
            case 2: {

                // Hide instruction 1& 3
                toggleInstruction1(false);
                toggleInstruction3(false);

                // Show instruction 2
                toggleInstruction2(true);

                break;
            }

            case 3: {

                // Hide instruction 2 and 4
                toggleInstruction2(false);
                toggleInstruction4(false);

                //Show instruction 3
                toggleInstruction3(true);
                break;
            }

            case 4: {

                // Hide instruction 3 and 5
                toggleInstruction3(false);
                toggleInstruction5(false);

                // Show instruction 4
                toggleInstruction4(true);
                break;
            }
            case 5: {
                // Hide instruction 4
                toggleInstruction4(false);

                // Show instruction 5
                toggleInstruction5(true);
            }
        }
    }

    private void toggleInstruction1(boolean show) {
        mInstruction1.animate()
                .alpha(show ? 1f : 0f)
                .setStartDelay(show ? gap : 0)
                .setDuration(gap)
                .setInterpolator(mInterpolator)
                .translationY(show ? 0 : 50)
                .start();
    }

    private void toggleInstruction2(boolean show) {
        mInstruction2.animate()
                .alpha(show ? 1f : 0f)
                .setStartDelay(show ? gap : 0)
                .setDuration(gap)
                .setInterpolator(mInterpolator)
                .translationY(show ? 0 : 50)
                .start();
        mOnboardingOverlayView.animate()
                .alpha(show ? 0.8f : 1f)
                .setStartDelay(show ? gap : 0)
                .setDuration(show ? 2 * gap : gap)
                .setInterpolator(mInterpolator)
                .start();
        mBox.animate()
                .alpha(show ? 1f : 0f)
                .setStartDelay(show ? gap : 0)
                .setDuration(gap)
                .setInterpolator(mInterpolator)
                .start();
    }

    private void toggleInstruction3(boolean show) {

        topCard.animate()
                .alpha(show ? 1f : 0f)
                .setDuration(gap)
                .setStartDelay(show ? gap : 0)
                .setInterpolator(mInterpolator)
                .translationY(show ? 0 : -100)
                .start();
        mInstruction3.animate()
                .alpha(show ? 1f : 0f)
                .setStartDelay(show ? gap : 0)
                .setDuration(gap)
                .setInterpolator(mInterpolator)
                .translationY(show ? 0 : -100)
                .start();
    }

    private void toggleInstruction4(boolean show) {

        mInstruction4.animate()
                .alpha(show ? 1 : 0)
                .setStartDelay(show ? gap : 0)
                .setDuration(gap)
                .translationY(show ? 0 : 100)
                .start();

        bottomCard.animate()
                .alpha(show ? 1f : 0f)
                .setStartDelay(show ? gap : 0)
                .setDuration(gap)
                .setInterpolator(mInterpolator)
                .translationY(show ? 0 : 100)
                .start();
    }

    private void toggleInstruction5(boolean show) {
        mInstruction5.animate()
                .alpha(show ? 1 : 0)
                .setStartDelay(show ? gap : 0)
                .setDuration(gap)
                .translationY(show ? 0 : 100)
                .start();
    }

    public void fadeOutAll() {
        toggleInstruction1(false);
        toggleInstruction2(false);
        toggleInstruction3(false);
        toggleInstruction4(false);
        toggleInstruction5(false);
    }

}

class NullDetector extends Detector {

    NullDetector() {
        setProcessor(new NullProcessort());
    }

    @Override
    public SparseArray detect(Frame frame) {
        return null;
    }
}

class NullProcessort implements Detector.Processor {

    @Override
    public void release() {

    }

    @Override
    public void receiveDetections(Detector.Detections detections) {

    }
}
