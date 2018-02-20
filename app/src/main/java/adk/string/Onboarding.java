package adk.string;

import android.graphics.Point;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class Onboarding extends FragmentActivity {

    ViewPager mViewPager;
    VPAdapter mVPAdapter;
    TextView prev, next;
    int currentInstruction = 0;
    int maxInstruction = 5;

    OnboardingUsageFragment usageFragment;

    View dot1, dot2;
    int screenWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        mViewPager = findViewById(R.id.view_pager);
        dot1 = findViewById(R.id.onboarding_pi1);
        dot2 = findViewById(R.id.onboarding_pi2);
        prev = findViewById(R.id.prev_btn);
        next = findViewById(R.id.next_btn);

        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        screenWidth = point.x;

        usageFragment = new OnboardingUsageFragment();

        mVPAdapter = new VPAdapter(getSupportFragmentManager(), usageFragment);
        mViewPager.setAdapter(mVPAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                animateDot(position, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int positionOffset) {


                if (mViewPager.getCurrentItem() == 0) {
                    currentInstruction = 0;
                    usageFragment.fadeOutAll();
                    prev.setEnabled(true);
                    prev.setText("Skip");
                    prev.animate()
                            .alpha(1)
                            .setDuration(300)
                            .start();
                } else if (mViewPager.getCurrentItem() == 1) {
                    currentInstruction = 1;
                    usageFragment.showInstruction(currentInstruction);
                    prev.animate()
                            .alpha(0)
                            .setDuration(300)
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            prev.setText("Previous");
                                            prev.setEnabled(false);
                                        }
                                    });
                                }
                            })
                            .start();
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (currentInstruction < maxInstruction) {
                    currentInstruction += 1;
                } else {
                    // Tutorial is over.
                    goBack();
                }

                if (mViewPager.getCurrentItem() == 0) {
                    mViewPager.setCurrentItem(1, true);
                    currentInstruction = 1;
                }

                if (currentInstruction == 2) {
                    prev.animate()
                            .alpha(1.0f)
                            .setDuration(300)
                            .start();
                    prev.setEnabled(true);
                }

                if (currentInstruction == 5) {
                    // Tutorial is done
                    next.setText("Start");
                } else {
                    next.setText("Next");
                }

                usageFragment.showInstruction(currentInstruction);
            }
        });

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("Checks", "Ins " + currentInstruction);
                if (currentInstruction == 0) {
                    // Skip the tutorial.
                    goBack();
                } else {
                    currentInstruction -= 1;

                    if (currentInstruction == 1) {
                        prev.animate()
                                .alpha(0.0f)
                                .setDuration(300)
                                .start();
                        prev.setEnabled(false);
                    }

                    next.setText("Next");

                    usageFragment.showInstruction(currentInstruction);
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
    }

    private void goBack() {
        super.onBackPressed();
    }

    private void animateDot(int position, float offset) {

        float v = 0.5f * offset / screenWidth;
        float s = v / 2;

        float alphaHighlighted = within(0.5f, 1 - v, 1);
        float alphaDimmed = within(0.5f, 0.5f + v, 1);

        float scaleHighlighted = within(0.75f, 1 - s, 1);
        float scaleDimmed = within(0.75f, 0.75f + s, 1);

        if (position == 0) {
            scaleAlpha(dot1, dot2, alphaHighlighted, scaleHighlighted, alphaDimmed, scaleDimmed);
        } else if (position == 1) {
            scaleAlpha(dot2, dot1, alphaHighlighted, scaleHighlighted, alphaDimmed, scaleDimmed);
        }

    }

    private void scaleAlpha(View dot1, View dot2, float alphaHighlighted, float scaleHighlighted,
                            float alphaDimmed, float scaleDimmed) {
        dot1.setAlpha(alphaHighlighted);
        dot1.setScaleX(scaleHighlighted);
        dot1.setScaleY(scaleHighlighted);

        dot2.setAlpha(alphaDimmed);
        dot2.setScaleX(scaleDimmed);
        dot2.setScaleY(scaleDimmed);
    }

    private float within(float min, float val, float max) {
        if (val < min) return min;
        else if (max < val) return max;
        else return val;
    }
}

class VPAdapter extends FragmentPagerAdapter {

    private OnboardingUsageFragment usageFragment;

    VPAdapter(FragmentManager fm, OnboardingUsageFragment usageFragment) {
        super(fm);
        this.usageFragment = usageFragment;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0: {
                return new OnboardAppFragment();
            }
            case 1: {
                return usageFragment;
            }

        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
