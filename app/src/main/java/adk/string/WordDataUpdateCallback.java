package adk.string;

import java.util.List;

/**
 * Created by Adk on 12/29/2017.
 * An interface whose onUpdate method is called by a Word object everytime its data is changed.
 */

public interface WordDataUpdateCallback {

    void onUpdate(Word word);

}
