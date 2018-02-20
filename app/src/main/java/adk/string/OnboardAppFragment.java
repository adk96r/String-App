package adk.string;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

public class OnboardAppFragment extends Fragment {

    public View mText;

    public OnboardAppFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.onboard_app_fragment, container, false);
        mText = view.findViewById(R.id.text_part);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        mText.post(new Runnable() {
            @Override
            public void run() {

                try {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            mText.animate()
                                    .translationY(0)
                                    .setDuration(800)
                                    .alpha(1.0f)
                                    .setStartDelay(200)
                                    .setInterpolator(new DecelerateInterpolator())
                                    .start();

                        }
                    });
                } catch (NullPointerException ignored){

                }
            }
        });
    }
}
