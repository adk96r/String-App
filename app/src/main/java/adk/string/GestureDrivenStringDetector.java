package adk.string;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

/**
 * Crafted by Adk on a midnight.
 * GestureDrivenStringDetector is the detector used by {@link MainActivity#mCameraSource} to detect
 * {@link TextBlock} in the camera frames. The {@link #detect(Frame)} is automatically called when
 * the camera is on (preview is on). If the camera stops, it isn't invoked.
 * <p>
 * The user has to draw an {@link StringOverlayBoxView} box around the word on the screen. Upon doing,
 * all the words detected in the box are shown to the user. The user can choose any one of them to
 * get its meaning. The words are detected in the incoming frames only when the user's finger is on
 * the screen.
 * <p>
 * The value of {@link #oldFrame} is continuously updated to a new fresh frame, from the camera, in
 * the {@link #detect(Frame)}. If the preview is paused by the user, which sets {@link #useOldFrame}
 * to true, the old frame's is no longer updated as {@link #detect(Frame)} is no longer invoked.
 * Thus, if the user draws any overlay boxes, the old oldframe is used for detecting words. This
 * is done using {@link #detectWordsInFrame(Frame)} inside the {@link #onTouch(View, MotionEvent)}.
 */

class GestureDrivenStringDetector extends Detector<TextBlock> implements View.OnTouchListener {

    private StringProcessor mStringProcessor;
    private StringOverlayBoxView stringOverlayView;
    private WordAdapter mWordAdapter;
    private Boolean detectingWords;
    private boolean wordAvailable;
    private Rect stringOverlayRect;
    private TextRecognizer mTextRecognizer;
    private SparseArray<TextBlock> emptySparseArray;
    private FluidicElement mFluidicElement;
    private boolean useOldFrame;
    private Frame oldFrame;


    GestureDrivenStringDetector(Context context, StringProcessor mStringProcessor,
                                StringOverlayBoxView stringOverlayBoxView,
                                Processor<TextBlock> processor, WordAdapter mWordAdapter,
                                FluidicElement mFluidicElement) {

        this.mStringProcessor = mStringProcessor;
        this.stringOverlayView = stringOverlayBoxView;
        this.stringOverlayRect = new Rect(0, 0, 0, 0);
        this.mTextRecognizer = new TextRecognizer.Builder(context).build();
        this.mWordAdapter = mWordAdapter;
        this.emptySparseArray = new SparseArray<>(0);
        this.detectingWords = false;
        this.mFluidicElement = mFluidicElement;
        this.useOldFrame = false;
        this.oldFrame = null;

        this.setProcessor(processor);
        this.stringOverlayView.setBoxRect(stringOverlayRect);

    }

    @Override
    @SuppressWarnings("ClickableViewAccessibility")
    public boolean onTouch(View v, MotionEvent event) {

        // Get the touch point.
        Point currentPoint = new Point((int) event.getX(), (int) event.getY());

        switch (event.getAction()) {

            // User has started to draw the overlay box. So start detecting the words every time
            // the box's dimensions change.
            case MotionEvent.ACTION_DOWN: {
                // If the finger is put on the screen, push down the fluidic element and
                // setup the overlay.
                mFluidicElement.flowDown();
                //detectingWords = true;
                wordAvailable = false;
                Utility.setOverlayStartingPoint(stringOverlayRect, currentPoint);
                break;
            }

            // Update the overlay box's dimensions and redraw it.
            case MotionEvent.ACTION_MOVE: {
                Utility.setOverlayEndingPoint(stringOverlayRect, currentPoint);
                stringOverlayView.invalidate();
                break;
            }

            // User has completed drawing the box. Stop detecting the words and get the meanings
            // for the words detected before.
            case MotionEvent.ACTION_UP: {
                Utility.setOverlayEndingPoint(stringOverlayRect, currentPoint);
                mWordAdapter.searchMeaningsForWords();
                //detectingWords = false;
                wordAvailable = true;

                // If old frame is being used, manually detect the new words on the old frame.
                if (useOldFrame) {
                    Detections<TextBlock> detections = new Detections<>(detectWordsInFrame(oldFrame),
                            oldFrame.getMetadata(), true);
                    mStringProcessor.receiveDetections(detections);
                }


                break;
            }
        }

        // Return true as we've handled the event.
        return true;
    }

    /**
     * Returns a SparseArray of all the TextBlocks in the given frame, if a box is available for
     * detection.
     *
     * @param frame Frame in which words have to be detected.
     * @return SparseArray of the detected TextBlocks.
     */
    @Override
    public SparseArray<TextBlock> detect(Frame frame) {

        oldFrame = frame;

        /*
        // Scan and detect the words only if an overlay box is available.
        if (detectingWords) {
            return detectWordsInFrame(frame);
        }
        */

        if (wordAvailable) {
            wordAvailable = false;
            return detectWordsInFrame(frame);
        }


        return emptySparseArray;
    }

    /**
     * Start using the old frame for any detections if {@link #useOldFrame} is true. If false, the
     * latest frames from the camera are used.
     *
     * @param useOldFrame Boolean.
     */
    void pausePreview(boolean useOldFrame) {
        this.useOldFrame = useOldFrame;
    }

    /**
     * Returns a SparseArray of all the TextBlocks detected in the {@link #stringOverlayRect}
     * region of the given frame.
     *
     * @param frame Frame which is cropped and scanned for detections.
     * @return SparseArray of all the detected TextBlocks.
     */
    private SparseArray<TextBlock> detectWordsInFrame(Frame frame) {

        // Crop the frame and convert it into a bitmap.
        final Bitmap croppedFrame = Utility.getCroppedBitmapFromFrame(frame, stringOverlayRect);

        // If null, return empty detections.
        if (croppedFrame == null) {
            return emptySparseArray;
        }

        // Else, pass a rotated bitmap to the text recognizer.
        return mTextRecognizer.detect(new Frame.Builder()
                .setBitmap(Utility.rotateBitmap(croppedFrame, 90))
                .build());

    }


}
