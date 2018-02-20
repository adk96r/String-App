package adk.string;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Adk on 1/12/18.
 * HistoryController takes care of everything related to the history of Words, which involves
 * saving, retrieving (in lists of either {@link SimpleWord} or {@link Word}, and checking if
 * a word belongs to history and adding words to history.
 */

class HistoryController {

    private static final String TAG = adk.string.HistoryController.class.getSimpleName();
    private boolean historyEnabled;
    private List<Word> history;

    /**
     * Returns a List of {@link Word} generated from the list of {@link SimpleWord} stored in the
     * shared preferences as history.
     *
     * @param context                 Context to get the shared preferences.
     * @param mWordDataUpdateCallback {@link WordDataUpdateCallback} whose onUpdate(Word) will be
     *                                called when the word's data is updated.
     * @return List of {@link Word}.
     */
    static List<Word> loadHistory(Context context, WordDataUpdateCallback mWordDataUpdateCallback) {

        List<Word> history = new ArrayList<>(0);
        for (SimpleWord simpleWord : getSimpleHistory(context)) {
            history.add(new Word(simpleWord, mWordDataUpdateCallback, false));
        }

        return history;
    }

    /**
     * Returns a List of {@link SimpleWord} stored in the shared preferences as history.
     *
     * @param context Context to get the shared preferences.
     * @return List of {@link SimpleWord}.
     */
    static List<SimpleWord> getSimpleHistory(Context context) {

        SharedPreferences preferences = context.getSharedPreferences("Preferences", MODE_PRIVATE);
        List<SimpleWord> simpleWords;

        // Get the old history JSON.
        String simpleWordHistoryJSON = preferences.getString("HISTORY", null);

        if (simpleWordHistoryJSON == null) {
            // Setup a blank one if not already present.
            preferences.edit()
                    .putString("HISTORY", new Gson().toJson(new ArrayList<SimpleWord>(0)))
                    .apply();
            simpleWords = new ArrayList<>(0);
        } else {
            // Convert the words into
            simpleWords = new Gson().fromJson(simpleWordHistoryJSON,
                    new TypeToken<List<SimpleWord>>() {
                    }.getType());
        }

        return simpleWords;
    }

    /**
     * Return a controller which does various tasks related to the history of the words searched by
     * the user.
     *
     * @param context                 Context to get the shared preferences.
     * @param mWordDataUpdateCallback Callback that will be executed when a word's data is changed.
     */
    HistoryController(Context context, WordDataUpdateCallback mWordDataUpdateCallback) {

        // Should words be saved to the history.
        historyEnabled = context.getSharedPreferences("Preferences", MODE_PRIVATE)
                .getBoolean("SAVE_HISTORY", true);

        // Get the history.
        history = loadHistory(context, mWordDataUpdateCallback);

    }

    /**
     * Checks if the given word is already present in the temporary history. If present, remove the
     * old occurrence and re-add it at the front and return true. If not present add the word, just
     * add the word at the front and return false;
     *
     * @param wordToAdd Word to add.
     * @return true if already present; false otherwise.
     */
    boolean addToHistory(Word wordToAdd) {

        history.add(0, wordToAdd);
        for (int i = 1; i < history.size(); i++) {
            if (history.get(i).getWord().equals(wordToAdd.getWord())) {
                history.remove(i);
                return false;
            }
        }
        return true;
    }

    /**
     * Checks the temporary history for a {@link Word} with the same word member as the given
     * word argument, and returns the word if found. Returns null otherwise.
     *
     * @param word String that should be equal to the value of the member {@link Word#word}.
     * @return {@link Word} if found; null otherwise.
     */
    Word hasWord(String word) {
        for (Word historicWord : history) {
            if (historicWord.getWord().equalsIgnoreCase(word)) return historicWord;
        }
        return null;
    }

    /**
     * @return True if words searched have to be saved to the history.
     */
    boolean isHistoryEnabled() {
        return historyEnabled;
    }

    /**
     * Saves the history in the shared preferences.
     *
     * @param context Context to get the shared preferences.
     */
    void saveHistory(Context context) {

        List<SimpleWord> simpleWords = new ArrayList<>(history.size());
        for (Word word : history) {
            simpleWords.add(new SimpleWord(word));
        }

        SharedPreferences preferences = context.getSharedPreferences("Preferences", MODE_PRIVATE);
        preferences.edit()
                .putString("HISTORY", new Gson().toJson(simpleWords))
                .apply();

        Log.d(TAG, "Saved " + history.size() + " word(s) in the shared prefs.");
    }

    int numberOfWords() {
        return history.size();
    }
}


