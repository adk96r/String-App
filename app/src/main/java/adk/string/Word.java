package adk.string;

import android.os.AsyncTask;
import android.util.Log;

import adk.string.Wordnik.WordnikGrabber;

/**
 * Each object of the Word class holds the details of a word recognized by the camera detector.
 * The details include the word itself, its pronunciation, the meanings of the word when used as
 * a verb, noun, adjective, and an example for the word. These details are queried from the Wordnik
 * API using the {@link adk.string.Wordnik.WordnikGrabber}.
 * <p>
 * As soon as the object is created for a detected word, 5 wordnik grabbers are initialised and
 * executed to gather the various details. The reference to the word object is passed for
 * updation of data when available.
 * <p>
 * Everytime the word's data is updated by any of the grabbers, the UI is also updated
 * by a call to the callback {@link #mDataUpdateCallback}.
 * <p>
 * The 3 different status are -
 * 200 - Data available.
 * 400 - Data not found.
 * 600 - Data has been queried by the API, but no result yet.
 */
public class Word {

    private final static String TAG = Word.class.getSimpleName();
    final static int STATUS_NOT_STARTED = 0;
    final static int STATUS_AVAILABLE = 200;
    final static int STATUS_NOT_AVAILABLE = 400;
    final static int STATUS_SEARCHING = 600;
    public final static int MODE_NOUN = 0;
    public final static int MODE_VERB = 1;
    public final static int MODE_ADJ = 2;
    public final static int MODE_PRONUNCIATION = 3;
    public final static int MODE_EXAMPLE = 4;
    private int currentMode;

    private WordDataUpdateCallback mDataUpdateCallback;
    private WordnikGrabber nounGrabber, verbGrabber, proGrabber, adjGrabber, exGrabber;
    private String word;
    private String noun;
    private String verb;
    private String adjective;
    private String pronunciation;
    private int statusNoun, statusVerb, statusAdj, statusPronunciataion, statusExample;
    private String example;

    public Word(String word, WordDataUpdateCallback dataUpdateCallback, boolean queryMeaning) {

        this.word = word;
        this.mDataUpdateCallback = dataUpdateCallback;
        noun = verb = pronunciation = example = null;

        // Blank state.
        setStatus(MODE_NOUN, STATUS_NOT_STARTED);
        setStatus(MODE_VERB, STATUS_NOT_STARTED);
        setStatus(MODE_ADJ, STATUS_NOT_STARTED);
        setStatus(MODE_PRONUNCIATION, STATUS_NOT_STARTED);
        setStatus(MODE_EXAMPLE, STATUS_NOT_STARTED);
        currentMode = 0;

        if (queryMeaning) getDataSequentially();
    }

    public Word(SimpleWord simpleWord, WordDataUpdateCallback dataUpdateCallback, boolean queryMeaning) {

        this.word = simpleWord.getWord();
        this.mDataUpdateCallback = dataUpdateCallback;

        noun = simpleWord.getNoun();
        verb = simpleWord.getVerb();
        adjective = simpleWord.getAdjective();
        pronunciation = simpleWord.getPronunciation();
        example = simpleWord.getExample();

        // Restore the state of the saved simple word.
        statusNoun = simpleWord.getStatusNoun();
        statusVerb = simpleWord.getStatusVerb();
        statusAdj = simpleWord.getStatusAdj();
        statusPronunciataion = simpleWord.getStatusPronuncitaion();
        statusExample = simpleWord.getStatusExample();
        currentMode = 0;

        if (queryMeaning) getDataSequentially();
    }

    void getDataSequentially() {

        switch (currentMode) {
            case MODE_PRONUNCIATION: {
                if (statusPronunciataion == STATUS_NOT_STARTED || statusPronunciataion == STATUS_SEARCHING) {
                    proGrabber = new WordnikGrabber(this, WordnikGrabber.GET_PRONUNCIATION);
                    proGrabber.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    currentMode += 1;
                    getDataSequentially();
                }
                break;
            }
            case MODE_NOUN: {

                if (statusNoun == STATUS_NOT_STARTED || statusNoun == STATUS_SEARCHING) {
                    nounGrabber = new WordnikGrabber(this, WordnikGrabber.GET_NOUN);
                    nounGrabber.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    currentMode += 1;
                    getDataSequentially();
                }
                break;
            }
            case MODE_VERB: {
                if (statusVerb == STATUS_NOT_STARTED || statusVerb == STATUS_SEARCHING) {
                    verbGrabber = new WordnikGrabber(this, WordnikGrabber.GET_VERB);
                    verbGrabber.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    currentMode += 1;
                    getDataSequentially();
                }
                break;
            }
            case MODE_ADJ: {
                if (statusAdj == STATUS_NOT_STARTED || statusAdj == STATUS_SEARCHING) {
                    adjGrabber = new WordnikGrabber(this, WordnikGrabber.GET_ADJECTIVE);
                    adjGrabber.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    currentMode += 1;
                    getDataSequentially();
                }
                break;
            }
            case MODE_EXAMPLE: {
                if (statusExample == STATUS_NOT_STARTED || statusExample == STATUS_SEARCHING) {
                    exGrabber = new WordnikGrabber(this, WordnikGrabber.GET_EXAMPLE);
                    exGrabber.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    currentMode += 1;
                    getDataSequentially();
                }
                break;
            }
        }

        currentMode += 1;
    }

    /**
     * Stops all the grabbers.
     */
    void stopAllConnections() {
        stopGrabber(proGrabber);
        stopGrabber(nounGrabber);
        stopGrabber(verbGrabber);
        stopGrabber(adjGrabber);
        stopGrabber(exGrabber);
    }

    /**
     * Stops an async task.
     *
     * @param grabber Async task to stop.
     */
    private void stopGrabber(WordnikGrabber grabber) {
        try {
            grabber.cancel(true);
        } catch (Exception ignored) {
        }
    }

    /**
     * Sets the status for the fields.
     *
     * @param mode   The field of the data.
     * @param status The status.
     */
    private void setStatus(int mode, int status) {
        switch (mode) {
            case MODE_NOUN: {
                statusNoun = status;
                break;
            }
            case MODE_VERB: {
                statusVerb = status;
                break;
            }
            case MODE_ADJ: {
                statusAdj = status;
                break;
            }
            case MODE_PRONUNCIATION: {
                statusPronunciataion = status;
                break;
            }
            case MODE_EXAMPLE: {
                statusExample = status;
                break;
            }
        }
    }

    public void setData(int mode, String data) {

        Log.d(TAG, "Setting " + mode + " for " + word + " : " + data);

        switch (mode) {
            case MODE_NOUN: {
                noun = data;
                break;
            }
            case MODE_VERB: {
                verb = data;
                break;
            }
            case MODE_ADJ: {
                adjective = data;
                break;
            }
            case MODE_PRONUNCIATION: {
                pronunciation = data;
                break;
            }
            case MODE_EXAMPLE: {
                example = data;
                break;
            }
        }

        if (data == null) setStatus(mode, STATUS_NOT_AVAILABLE);
        else setStatus(mode, STATUS_AVAILABLE);

        getDataSequentially();
        mDataUpdateCallback.onUpdate(this);
    }

    String getData(int mode) {
        switch (mode) {
            case MODE_NOUN:
                return noun;
            case MODE_VERB:
                return verb;
            case MODE_ADJ:
                return adjective;
            case MODE_PRONUNCIATION:
                return pronunciation;
            case MODE_EXAMPLE:
                return example;
            default:
                return null;
        }
    }

    boolean hasData(int mode) {
        switch (mode) {
            case MODE_NOUN:
                return statusNoun == STATUS_AVAILABLE;
            case MODE_VERB:
                return statusVerb == STATUS_AVAILABLE;
            case MODE_ADJ:
                return statusAdj == STATUS_AVAILABLE;
            case MODE_PRONUNCIATION:
                return statusPronunciataion == STATUS_AVAILABLE;
            case MODE_EXAMPLE:
                return statusExample == STATUS_AVAILABLE;
            default:
                return false;
        }
    }

    int getStatus(int mode) {
        switch (mode) {
            case MODE_NOUN:
                return statusNoun;
            case MODE_VERB:
                return statusVerb;
            case MODE_ADJ:
                return statusAdj;
            case MODE_PRONUNCIATION:
                return statusPronunciataion;
            case MODE_EXAMPLE:
                return statusExample;
            default:
                return STATUS_NOT_AVAILABLE;
        }
    }

    int getOverallStatus() {
        if (statusNoun == STATUS_NOT_STARTED || statusVerb == STATUS_SEARCHING || statusAdj == STATUS_SEARCHING ||
                statusNoun == STATUS_NOT_STARTED || statusVerb == STATUS_NOT_STARTED || statusAdj == STATUS_NOT_STARTED) {
            return STATUS_SEARCHING;
        } else if (statusNoun == STATUS_NOT_AVAILABLE && statusVerb == STATUS_NOT_AVAILABLE && statusAdj == STATUS_NOT_AVAILABLE) {
            return STATUS_NOT_AVAILABLE;
        } else {
            return STATUS_AVAILABLE;
        }
    }

    public String getWord() {
        return word;
    }

}
