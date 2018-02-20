package adk.string;

/**
 * Created by ADK on 1/10/18.
 * SimpleWord represents the pure state of a word without any WordnikGrabbers. Since the app saves
 * the words searched by the user in history ( as a string in shared preferences ), it needs a way
 * to convert the List of Word objects to a string.
 * <p>
 * The app uses {@link com.google.gson.Gson} for this conversion, but for a better efficiency, the
 * app only saves the states of the words (only the definitions and statuses). Thus, a new list of
 * SimpleWord are generated, from the List of word, which is converted to a JSON string for saving.
 */

public class SimpleWord {

    private String word;
    private String noun;
    private String verb;
    private String adjective;
    private String pronunciation;
    private String example;
    private int statusNoun, statusVerb, statusAdj, statusPronuncitaion, statusExample;

    SimpleWord(Word object) {

        word = object.getWord();

        noun = object.getData(Word.MODE_NOUN);
        verb = object.getData(Word.MODE_VERB);
        adjective = object.getData(Word.MODE_ADJ);
        pronunciation = object.getData(Word.MODE_PRONUNCIATION);
        example = object.getData(Word.MODE_EXAMPLE);

        statusNoun = object.getStatus(Word.MODE_NOUN);
        statusVerb = object.getStatus(Word.MODE_VERB);
        statusAdj = object.getStatus(Word.MODE_ADJ);
        statusPronuncitaion = object.getStatus(Word.MODE_PRONUNCIATION);
        statusExample = object.getStatus(Word.MODE_EXAMPLE);
    }

    public String getWord() {
        return word;
    }

    String getNoun() {
        return noun;
    }

    String getVerb() {
        return verb;
    }

    String getAdjective() {
        return adjective;
    }

    String getPronunciation() {
        return pronunciation;
    }

    String getExample() {
        return example;
    }

    int getStatusNoun() {
        return statusNoun;
    }

    int getStatusVerb() {
        return statusVerb;
    }

    int getStatusAdj() {
        return statusAdj;
    }

    int getStatusPronuncitaion() {
        return statusPronuncitaion;
    }

    int getStatusExample() {
        return statusExample;
    }

    boolean hasData() {
        // Return true only if there is data other than the example.
        return noun != null || verb != null || adjective != null;
    }
}
