package adk.string;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

public class WordAdapter extends RecyclerView.Adapter<WordHolder> {

    private static final String TAG = Word.class.getSimpleName();
    private TextView mRecognizedWordTextView;
    private List<Word> wordList;
    private Activity activity;
    private DatasetUpdateCallback mDatasetUpdateCallback;
    private WordDataUpdateCallback mWordDataUpdateCallback;
    private EditText mManualWordEditText;

    WordAdapter(Activity activity, @NonNull List<Word> wordList,
                WordDataUpdateCallback mWordDataUpdateCallback,
                TextView mRecognizedWordTextView, EditText mManualWordEditText) {

        this.mRecognizedWordTextView = mRecognizedWordTextView;
        this.activity = activity;
        this.wordList = wordList;
        this.mWordDataUpdateCallback = mWordDataUpdateCallback;
        this.mManualWordEditText = mManualWordEditText;
    }

    @Override
    public WordHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new WordHolder(activity.getLayoutInflater().inflate(R.layout.recognized_word_layout,
                parent, false));
    }

    @Override
    public void onBindViewHolder(final WordHolder holder, int position) {


        holder.wordView.setText(wordList.get(position).getWord().toUpperCase());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mRecognizedWordTextView.setText(wordList.get(holder.getAdapterPosition()).getWord());
                mManualWordEditText.setText(wordList.get(holder.getAdapterPosition()).getWord());
                mWordDataUpdateCallback.onUpdate(wordList.get(holder.getAdapterPosition()));

            }
        });

        // If only one word has been detected, directly show it instead of waiting for the user to
        // select it.
        if (getItemCount() == 1) {
            holder.itemView.performClick();
        }
    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }

    /**
     * When the user draws a new overlay box and the detector detects a new set of words, any
     * pending tasks for the old words are stopped using the {@link Word#stopAllConnections()},
     * and the word list is updated. This is followed by a call to the data set update callback
     * {@link DatasetUpdateCallback#onUpdate()}.
     *
     * @param newWordList List having the next set of detected words.
     */
    void setWordList(final List<Word> newWordList) {

        // Cancel any running Async Tasks.
        stopAllConnections();

        // Update the word list.
        this.wordList = newWordList;

        // Invoke the callback.
        mDatasetUpdateCallback.onUpdate();
    }

    /**
     * Stops all current requests for all the words in the {@link #wordList}.
     */
    void stopAllConnections(){
        for (Word word: wordList){
            word.stopAllConnections();
        }
    }

    void searchMeaningsForWords() {
        for (Word word : wordList) {
            word.getDataSequentially();
        }
    }

    /**
     * This callback performs some tasks when the dataset for the adapter is updated in the
     * {@link #setWordList(List)}.
     *
     * @param mDatasetUpdateCallback DatasetUpdateCallback object anonymously initialised in an
     *                               inner class inside the {@link MainActivity#addSoulToViews()}.
     */
    void setDatasetUpdateCallback(DatasetUpdateCallback mDatasetUpdateCallback) {
        this.mDatasetUpdateCallback = mDatasetUpdateCallback;
    }

}

class WordHolder extends RecyclerView.ViewHolder {

    TextView wordView;

    WordHolder(View itemView) {
        super(itemView);
        wordView = itemView.findViewById(R.id.recycler_view_recognized_word);

    }
}
