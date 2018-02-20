package adk.string;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class HistoryActivity extends Activity {

    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Toolbar back.
        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Toolbar title.
        ((TextView) findViewById(R.id.title_text_view)).setText(R.string.history);

        // Text to speech.
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                tts.setLanguage(Locale.ENGLISH);
            }
        });

        // Get the words from the shared preferences.
        List<SimpleWord> words = HistoryController.getSimpleHistory(this);

        if (words.size() == 0) {
            // Nothing available, show a snack bar.
            Snackbar.make(findViewById(R.id.history_relative_layout), "Nothing to show.",
                    Snackbar.LENGTH_LONG)
                    .setActionTextColor(ContextCompat.getColor(this, R.color.colorAccent))
                    .setAction("DISMISS", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Do nothing.
                        }
                    }).show();
        } else {
            // Show the words in the recycler view.
            HistoryAdapter mHistoryAdapter = new HistoryAdapter(this, words, tts);
            RecyclerView mRecyclerView = findViewById(R.id.history_recycler_view);
            mRecyclerView.setAdapter(mHistoryAdapter);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            //DividerItemDecoration decoration = new DividerItemDecoration(this, linearLayoutManager.getOrientation());
            mRecyclerView.setLayoutManager(linearLayoutManager);
            //mRecyclerView.addItemDecoration(decoration);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tts.shutdown();
    }
}

class HistoryAdapter extends RecyclerView.Adapter<HistoryItem> {

    private List<SimpleWord> words;
    private Activity activity;
    private LayoutInflater inflater;
    private int mExpandedPosition;
    private TextToSpeech tts;
    private HistoryItem mExpandedHolder;
    private ViewGroup.LayoutParams layoutParams;

    HistoryAdapter(Activity activity, List<SimpleWord> words, TextToSpeech tts) {
        this.activity = activity;
        this.words = words;
        inflater = activity.getLayoutInflater();
        mExpandedPosition = -1;
        this.tts = tts;
    }

    @Override
    public HistoryItem onCreateViewHolder(ViewGroup parent, int viewType) {
        return new HistoryItem(inflater.inflate(R.layout.history_word_recyler_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final HistoryItem holder, @SuppressLint("RecyclerView") final int position) {

        final SimpleWord simpleWord = words.get(position);
        setData(holder, simpleWord);

        // By default, only primary view is shown
        holder.itemView.post(new Runnable() {
            @Override
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        holder.expandedHeight = holder.itemView.getHeight()
                                + Utility.dpToPixels(activity, 16);
                        holder.collapsedHeight = holder.expandedHeight
                                - holder.mSecondaryView.getHeight()
                                - Utility.dpToPixels(activity, 16);

                        // Set the height of the item view to match the height of the primary view.
                        setHeight(holder.itemView, holder.collapsedHeight);
                    }
                });
            }
        });

        // Set the expand listener.
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mExpandedPosition == -1) {
                    // No card opened yet.
                    mExpandedPosition = position;
                    mExpandedHolder = holder;
                    expand(mExpandedHolder);
                } else if (mExpandedPosition == position) {
                    // User tapped the same card. So close it.
                    collapse(mExpandedHolder);
                    mExpandedPosition = -1;
                    mExpandedHolder = null;
                } else {
                    // User tapped a new card. So close the current one and open the new one.
                    collapse(mExpandedHolder);
                    mExpandedPosition = position;
                    mExpandedHolder = holder;
                    expand(mExpandedHolder);
                }
            }
        });


        // Pronounce the word upon pressing the FAB.
        holder.mTtsFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tts.speak(simpleWord.getWord(), TextToSpeech.QUEUE_FLUSH,
                        null, "word");
            }
        });

        holder.mOpenInChromeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(Utility.CHROME_DICT_BASE_LINK + simpleWord.getWord())));
            }
        });

    }

    private void setData(HistoryItem holder, SimpleWord simpleWord) {

        // Set the data.
        holder.word.setText(simpleWord.getWord());

        holder.h1.setText(R.string.noun);
        holder.h2.setText(R.string.verb);
        holder.h3.setText(R.string.adj);
        holder.h4.setText(R.string.example);

        holder.m1.setText(simpleWord.getNoun());
        holder.m2.setText(simpleWord.getVerb());
        holder.m3.setText(simpleWord.getAdjective());
        holder.m4.setText(simpleWord.getExample());

        // Set the visibility.
        if (simpleWord.getNoun() == null) {
            holder.h1.setVisibility(GONE);
            holder.m1.setVisibility(GONE);
        }
        if (simpleWord.getVerb() == null) {
            holder.h2.setVisibility(GONE);
            holder.m2.setVisibility(GONE);
        }
        if (simpleWord.getAdjective() == null) {
            holder.h3.setVisibility(GONE);
            holder.m3.setVisibility(GONE);
        }
        if (simpleWord.getExample() == null) {
            holder.h4.setVisibility(GONE);
            holder.m4.setVisibility(GONE);
        }

        // Make sure there is something in position 1.
        if (simpleWord.getNoun() == null) {
            if (simpleWord.getVerb() == null) {
                if (simpleWord.getAdjective() == null) {
                    holder.m1.setVisibility(VISIBLE);
                    holder.m1.setText("Nothing found.");
                } else {
                    // Show the adjective.
                    holder.h1.setVisibility(VISIBLE);
                    holder.h1.setText(R.string.adj);
                    holder.m1.setVisibility(VISIBLE);
                    holder.m1.setText(simpleWord.getAdjective());

                    holder.h3.setVisibility(GONE);
                    holder.m3.setVisibility(GONE);
                }
            } else {
                // Show the verb.
                holder.h1.setVisibility(VISIBLE);
                holder.h1.setText(R.string.verb);
                holder.m1.setVisibility(VISIBLE);
                holder.m1.setText(simpleWord.getVerb());

                holder.h2.setVisibility(GONE);
                holder.m2.setVisibility(GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    private ValueAnimator sizeChangeAnimator(int start, final int end, final View... views) {

        final ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                for (int i = 0; i < views.length; i++) {
                    setHeight(views[i], (Integer) animation.getAnimatedValue());
                }
            }
        });

        animator.setDuration(350);
        return animator;
    }

    private void expand(final HistoryItem historyItem) {

        historyItem.mSecondaryView.post(new Runnable() {
            @Override
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ValueAnimator animator = sizeChangeAnimator(historyItem.itemView.getHeight(),
                                historyItem.expandedHeight, historyItem.itemView);
                        historyItem.mTtsFAB.setEnabled(true);
                        historyItem.mTtsFAB.animate()
                                .scaleX(1)
                                .scaleY(1)
                                .setDuration(350)
                                .start();
                        animator.start();
                    }
                });
            }
        });

        ValueAnimator separatorAnimator = sizeChangeAnimator(0, Utility.dpToPixels(activity, 8),
                historyItem.mTopSeparator, historyItem.mBottomSeparator);
        separatorAnimator.start();

    }

    private void collapse(final HistoryItem historyItem) {

        ValueAnimator animator = sizeChangeAnimator(historyItem.itemView.getHeight(),
                historyItem.collapsedHeight, historyItem.itemView);

        historyItem.mTtsFAB.animate()
                .scaleX(0)
                .scaleY(0)
                .setDuration(350)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                historyItem.mTtsFAB.setEnabled(false);
                            }
                        });
                    }
                })
                .start();

        animator.start();

        ValueAnimator separatorAnimator = sizeChangeAnimator(historyItem.mTopSeparator.getHeight(), 0,
                historyItem.mTopSeparator, historyItem.mBottomSeparator);
        separatorAnimator.start();
    }

    private void setHeight(View view, int height) {
        layoutParams = view.getLayoutParams();
        layoutParams.height = height;
        view.setLayoutParams(layoutParams);
    }

}

class HistoryItem extends RecyclerView.ViewHolder {

    View mTopSeparator;
    View mBottomSeparator;
    View mSecondaryView;
    FloatingActionButton mTtsFAB;
    Button mOpenInChromeBtn;

    int collapsedHeight;
    int expandedHeight;

    TextView word;
    TextView m1;
    TextView m2;
    TextView m3;
    TextView m4;
    TextView h1;
    TextView h2;
    TextView h3;
    TextView h4;


    HistoryItem(View itemView) {
        super(itemView);
        mTopSeparator = itemView.findViewById(R.id.top_separator);
        mBottomSeparator = itemView.findViewById(R.id.bottom_separator);
        mSecondaryView = itemView.findViewById(R.id.secondary_view);
        word = itemView.findViewById(R.id.history_word);
        m1 = itemView.findViewById(R.id.history_meaning_1);
        m2 = itemView.findViewById(R.id.history_meaning_2);
        m3 = itemView.findViewById(R.id.history_meaning_3);
        m4 = itemView.findViewById(R.id.history_meaning_4);
        h1 = itemView.findViewById(R.id.history_hint_1);
        h2 = itemView.findViewById(R.id.history_hint_2);
        h3 = itemView.findViewById(R.id.history_hint_3);
        h4 = itemView.findViewById(R.id.history_hint_4);
        mTtsFAB = itemView.findViewById(R.id.history_tts_fab);
        mOpenInChromeBtn = itemView.findViewById(R.id.history_open_in_chrome);
    }
}