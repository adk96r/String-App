package adk.string.Wordnik;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import adk.string.Word;

/**
 * Created by Adk on 12/29/2017.
 * <p>
 * WordnikGrabber uses the Wordnik API to get a word's definition ( when used as a noun, verb, and
 * adjective ) , its pronunciation and its example. After getting the data, the word's data and
 * the respective status is updated.
 * <p>
 * Objects of this class need two parameters -
 * 1) Word: {@link Word} whose data will be sought.
 * 2) mode: An int that represents what information will be queried using the API. The int is
 * one of {@link Word#MODE_NOUN}, {@link Word#MODE_VERB}, {@link Word#MODE_ADJ},
 * {@link Word#MODE_EXAMPLE} or {@link Word#MODE_PRONUNCIATION}.
 */

public class WordnikGrabber extends AsyncTask<Void, Void, Void> {

    private final static String TAG = WordnikGrabber.class.getSimpleName();
    public final static String GET_NOUN = "noun";
    public final static String GET_VERB = "verb";
    public final static String GET_ADJECTIVE = "adjective";
    public final static String GET_PRONUNCIATION = "pronunciation";
    public final static String GET_EXAMPLE = "example";
    private final static String API_KEY = "use_your_own_api_key";

    private Word object;
    private String mode;

    public WordnikGrabber(Word object, String mode) {
        this.object = object;
        this.mode = mode;
    }

    private String getDefinitionEndpoint(String word, String partOfSpeech) {

        return "http://api.wordnik.com/v4/word.json/" + word + "/definitions?" +
                "limit=1" +
                "&partOfSpeech=" + partOfSpeech +
                "&includeRelated=false" +
                "&sourceDictionaries=all" +
                "&useCanonical=false" +
                "&includeTags=false" +
                "&api_key=" + API_KEY;

    }

    private String getExampleEndpoint(String word) {
        return "http://api.wordnik.com/v4/word.json/" + word + "/topExample?"
                + "useCanonical=false"
                + "&api_key=" + API_KEY;
    }

    private String getPronunciationEndpoint(String word) {
        return "http://api.wordnik.com:80/v4/word.json/" + word + "/pronunciations?"
                + "useCanonical=false"
                + "&limit=1"
                + "&api_key=" + API_KEY;
    }

    @Override
    protected Void doInBackground(Void... voids) {

        URL url = null;
        String result = null;

        try {
            switch (mode) {
                case GET_NOUN:
                case GET_VERB:
                case GET_ADJECTIVE: {
                    url = new URL(getDefinitionEndpoint(object.getWord().toLowerCase(), mode));
                    break;
                }
                case GET_EXAMPLE: {
                    url = new URL(getExampleEndpoint(object.getWord().toLowerCase()));
                    break;
                }
                case GET_PRONUNCIATION: {
                    url = new URL(getPronunciationEndpoint(object.getWord().toLowerCase()));
                    break;
                }
            }

            if (url != null) {
                Log.d(TAG, String.valueOf(url));

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);

                Log.d(TAG, "Response code for " + mode + " is " + connection.getResponseCode());

                if (connection.getResponseCode() == 200) {
                    BufferedReader reader =
                            new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    result = parseJSON(reader.readLine(), mode);
                    Log.d(TAG, "Response for " + mode + " is " + result);
                }
            }

        } catch (Exception e) {
            Log.d(TAG, "Exception in the grabber : " + e.getMessage());
        }

        object.setData(mapOperationToMode(mode), result);
        return null;
    }

    private String parseJSON(String json, String mode) throws JSONException {

        switch (mode) {
            case GET_NOUN:
            case GET_VERB:
            case GET_ADJECTIVE: {
                JSONArray array = new JSONArray(json);
                if (array.length() > 0) return array.getJSONObject(0).getString("text");
                else return null;
            }
            case GET_EXAMPLE: {
                JSONObject object = new JSONObject(json);
                try {
                    return object.getString("text");
                } catch (Exception e) {
                    return null;
                }
            }
            case GET_PRONUNCIATION: {
                JSONArray array = new JSONArray(json);
                if (array.length() > 0) return array.getJSONObject(0).getString("raw");
                else return null;
            }
        }
        return null;
    }

    private int mapOperationToMode(String op) {
        switch (op) {
            case GET_NOUN:
                return Word.MODE_NOUN;
            case GET_VERB:
                return Word.MODE_VERB;
            case GET_PRONUNCIATION:
                return Word.MODE_PRONUNCIATION;
            case GET_ADJECTIVE:
                return Word.MODE_ADJ;
            case GET_EXAMPLE:
                return Word.MODE_EXAMPLE;
            default:
                return -1;
        }
    }
}
