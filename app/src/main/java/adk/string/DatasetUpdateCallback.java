package adk.string;

/**
 * Created by Adk on 1/5/2018.
 * onUpdate is invoked whenever the list of words detected changes. This change would occur when the 
 * user re-draws a box around a set of words which results in new detections and new list of words
 * for the user to choose from.
 */

interface DatasetUpdateCallback {
    void onUpdate();
}
