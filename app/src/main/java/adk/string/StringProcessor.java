package adk.string;

import android.os.Build;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Crafted by Adk at 1949 09012018.
 * <p>
 * StringProcessor class is build using the {@link Detector.Processor} and hence receives detections
 * from a detector. These detections are used to generate word objects, which are used as the data
 * for the word adapter used in the recognized words recycler view.
 */
public class StringProcessor implements Detector.Processor<TextBlock> {

    private final static String TAG = StringProcessor.class.getSimpleName();
    private WordAdapter mWordAdapter;
    private WordDataUpdateCallback mWordDataUpdateCallback;
    private HistoryController mHistoryController;

    StringProcessor(WordAdapter mWordAdapter, WordDataUpdateCallback mWordDataUpdateCallback,
                    HistoryController mHistoryController) {
        this.mWordAdapter = mWordAdapter;
        this.mWordDataUpdateCallback = mWordDataUpdateCallback;
        this.mHistoryController = mHistoryController;
    }

    @Override
    public void release() {

    }

    /**
     * Receives the detections from the {@link GestureDrivenStringDetector} and updates the words
     * adapter.
     *
     * @param detections Detections received.
     */
    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {

        List<String> recognizedWords = new ArrayList<>();
        SparseArray<TextBlock> textBlocks = detections.getDetectedItems();
        int totalDetections = textBlocks.size();

        // If there are any detections, extract individual words from them.
        if (totalDetections > 0) {
            for (int i = 0; i < totalDetections; i++) {
                if (textBlocks.get(i) != null && textBlocks.get(i).getValue() != null) {
                    Utility.merge(recognizedWords, getWordsInBlock(textBlocks.get(i)));
                }
            }

            // Since the user would draw the box over a word, place the largest word first in the
            // list, so that it is queried first.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                recognizedWords.sort(new Comparator<String>() {
                    @Override
                    public int compare(String first, String second) {
                        return second.length() - first.length();
                    }
                });
            }

            List<Word> words = new ArrayList<>(recognizedWords.size());
            for (String word : recognizedWords) {

                // Check if the word already exists in the history.
                Word alreadyPresentWord = mHistoryController.hasWord(word);

                if (alreadyPresentWord != null) {
                    alreadyPresentWord.getDataSequentially();
                    words.add(alreadyPresentWord);
                } else {
                    //words.add(new Word(word, mWordDataUpdateCallback, true));
                    words.add(new Word(word, mWordDataUpdateCallback, true));
                }
            }

            // If the size of the list is more than 4; trim it down [EXPERIMENTAL}
            //if (words.size() > 6) {
            //    words = words.subList(0, 6);
            //}

            Log.d(TAG, "Detected " + words.size() + " word(s).");
            // Update the adapter's word list.
            mWordAdapter.setWordList(words);
        }

    }


    /**
     * Returns a list of strings each representing a word in the given text block.
     *
     * @param textBlock TextBlock having various lines seperated by "\n".
     * @return List of words in the given textblock.
     */
    private List<String> getWordsInBlock(TextBlock textBlock) {
        List<String> words = new ArrayList<>(0);
        if (textBlock.getValue().contains("\n")) {
            for (String line : textBlock.getValue().split("\n")) {
                Utility.merge(words, getWordsInLine(line));
            }
        } else {
            Utility.merge(words, getWordsInLine(textBlock.getValue()));
        }
        return words;
    }

    /**
     * Returns a list of words in the given line. Any unwanted characters are used to split the
     * line, eg:  "hello.world"  -  will be interpreted as "hello" and "world".
     *
     * @param line String having words in it.
     * @return List of strings, each representing a word in the given string.
     */
    private List<String> getWordsInLine(String line) {
        List<String> words = new ArrayList<>(0);
        for (final String word : line.split("[.,; +)(]")) {
            if (!word.equals("")) {
                words.add(clean(word));
            }
        }
        return words;
    }

    /**
     * Returns a trimmed string.
     *
     * @param s String to trim.
     * @return Trimmed string.
     */
    private String clean(String s) {
        return s.trim();
    }


}
