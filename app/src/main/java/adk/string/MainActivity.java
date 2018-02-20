package adk.string;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;

import java.util.ArrayList;
import java.util.Locale;

import static android.view.View.GONE;

public class MainActivity extends Activity {

    private static String TAG = MainActivity.class.getSimpleName();

    // Core
    private CameraSource mCameraSource;
    private GestureDrivenStringDetector mGestureDrivenStringDetector;
    private WordDataUpdateCallback mWordDataUpdateCallback;
    private TextToSpeech tts;
    private Context context;

    // Word
    private TextView mWordTextView;
    private TextView mWordPronunciationTextView;
    private TextView mWordStatus;
    private TextView mWordDefNoun;
    private TextView mWordDefVerb;
    private TextView mWordDefAdj;
    private TextView mWordExample;
    private FloatingActionButton mWordPronunciationVoiceFAB;
    private TextView mNounHintTextView;
    private TextView mVerbHintTextView;
    private TextView mAdjectiveHintTextView;
    private TextView mExampleHintTextView;

    // UI Views
    private FluidicElement mFluidicElement;
    private UiScrollView mScrollView;
    private LinearLayout mUiLinearLayout;
    private View mSecondaryCardsGroup;
    private SurfaceView mSurfaceView;
    private TextView mHintTextView;
    private StringOverlayBoxView mStringOverlayBox;
    private RecyclerView mRecognizedWordsRecyclerView;
    private WordAdapter mWordAdapter;
    private View mBackgroundLayerView;
    private EditText mManualWordEditText;
    private CardView mRecognizedWordsCardView;
    private CardView mManualSearchCardView;
    private Button mGoogleSearchBtn;
    private ImageButton pausePreviewBtn;

    // Logic
    private boolean animateRecognizedWordsCard;
    private boolean previewPaused = false;
    float left, top, right, bottom;
    private Size screenSize = null;
    private HistoryController historyController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        context = getApplicationContext();

        // If the app is being run for the first time, on board the user.
        onboard();

        setContentView(R.layout.activity_main);
        initiatePrimary();
        initiateViews();
        initiateCore(context);
        addSoulToViews();

        // Check if CAMERA permission is granted.
        if (cameraPermissionOffered()) {
            initiateCamera();
        }
    }

    private void onboard() {
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        if (preferences.getBoolean("FIRST_RUN", true)) {
            preferences.edit()
                    .putBoolean("FIRST_RUN", false)
                    .apply();
            startActivity(new Intent(context, Onboarding.class));
        }
    }

    private void initiatePrimary() {

        // Update the UI when a word's data is available.
        mWordDataUpdateCallback = getWordDataUpdateCallback();

        // Setup the history controller.
        historyController = new HistoryController(context, mWordDataUpdateCallback);

    }

    /**
     * Create the references to the UI objects.
     */
    private void initiateViews() {

        // Get the screen dimensions.
        screenSize = Utility.getScreenDimensions(this);

        // Get the surface for live preview.
        mSurfaceView = findViewById(R.id.surface_view);

        // A view for a background effect
        mBackgroundLayerView = findViewById(R.id.background_layer_view);

        // View over live preview onto which boxes can be drawn to select text.
        mStringOverlayBox = findViewById(R.id.detection_marker_overlay);

        // Manual override card view.
        mSecondaryCardsGroup = findViewById(R.id.secondary_cards_group);

        // Manual word edit text.
        mManualWordEditText = findViewById(R.id.manual_search_edit_text);

        // Recognized words recycler view.
        mRecognizedWordsRecyclerView = findViewById(R.id.recognized_words_recycler_view);

        // Fluidic element wrapping the scrollview.
        mFluidicElement = findViewById(R.id.UI_fluidic_element);

        // Scroll view that wraps the UI.
        mScrollView = findViewById(R.id.UI_scroll_view);

        // The linear layout which houses all the UI components ( and is fluidized ).
        mUiLinearLayout = findViewById(R.id.UI_linear_layout);

        // Hint ( Swipe diagonally )
        mHintTextView = findViewById(R.id.hint_text_view);

        // Hold the recognized word.
        mWordTextView = findViewById(R.id.recognized_word);

        // Holds the pronunciation.
        mWordPronunciationTextView = findViewById(R.id.word_quick_pronunciation);

        // Shows the status ( eg - Searching the meaning )
        mWordStatus = findViewById(R.id.word_status);

        // Shows the definition of the word when used as a noun.
        mWordDefNoun = findViewById(R.id.noun_text_view);

        // Shows the definition of the word when used as a verb.
        mWordDefVerb = findViewById(R.id.verb_text_view);

        // Shows the definition of the word when used as an adjective.
        mWordDefAdj = findViewById(R.id.adj_text_view);

        // Shows the definition of the word when used as a noun.
        mWordExample = findViewById(R.id.example_text_view);

        // A FAB to listen the pronunciation.
        mWordPronunciationVoiceFAB = findViewById(R.id.word_pronunciation_fab);

        // Button to pause the preview.
        pausePreviewBtn = findViewById(R.id.pause_preview_btn);

        // Hint views.
        mNounHintTextView = findViewById(R.id.hint_noun_text_view);
        mVerbHintTextView = findViewById(R.id.hint_verb_text_view);
        mAdjectiveHintTextView = findViewById(R.id.hint_adj_text_view);
        mExampleHintTextView = findViewById(R.id.hint_example_text_view);

        // Shows all the recognized words.
        mRecognizedWordsCardView = findViewById(R.id.recognized_words_card_view);

        // The card view which houses the manual search components.
        mManualSearchCardView = findViewById(R.id.manual_search_card);

        // Button to google the recognized word.
        mGoogleSearchBtn = findViewById(R.id.google_search_btn);

    }

    /**
     * The real shit.
     *
     * @param context Context to be used by the gesture detector and the recycler view's layout
     *                manager.
     */
    private void initiateCore(final Context context) {

        // Make the background transparent.
        mBackgroundLayerView.setAlpha(0.0f);

        // Pass all the touch events on the scrollview to its parent, fluidic element.
        mScrollView.setParent(mFluidicElement);

        // Setup the recycler view.
        mWordAdapter = new WordAdapter(this, new ArrayList<Word>(0),
                mWordDataUpdateCallback, mWordTextView, mManualWordEditText);
        mRecognizedWordsRecyclerView.setAdapter(mWordAdapter);

        mRecognizedWordsRecyclerView.setLayoutManager(new LinearLayoutManager(context,
                LinearLayoutManager.HORIZONTAL, false));

        // Setup a StringProcessor for the StringDetector.
        StringProcessor mStringProcessor = new StringProcessor(mWordAdapter,
                mWordDataUpdateCallback, historyController);

        // Setup the GestureDrivenStringDetector.
        mGestureDrivenStringDetector = new GestureDrivenStringDetector(context, mStringProcessor,
                mStringOverlayBox, mStringProcessor, mWordAdapter, mFluidicElement);

        // Phone says the word shown on the main word card, if the pronunciation FAB is clicked.
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                tts.setLanguage(Locale.ENGLISH);
            }
        });

        // Init the camera source.
        mCameraSource = getCameraSource(mGestureDrivenStringDetector);


    }

    /**
     * Soul of the UI.
     */
    private void addSoulToViews() {

        // Listen for the overlay boxes.
        mSurfaceView.setOnTouchListener(mGestureDrivenStringDetector);

        //  Add search IME to the manual edit text.
        mManualWordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                mFluidicElement.flowDown();
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    manualSearch(textView);
                    return true;
                }
                return false;
            }
        });

        // Push the meaning card view down if the user taps the manual wordy search edit text.
        mManualWordEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFluidicElement.flowDown();
            }
        });

        // Push the meaning card view down if the user taps the recognized words card view.
        mRecognizedWordsCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFluidicElement.flowDown();
            }
        });


        // All these have to be done when the list of recognized words change.
        mWordAdapter.setDatasetUpdateCallback(new DatasetUpdateCallback() {
            @Override
            public void onUpdate() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        // Enable the recognized words card view animations.
                        animateRecognizedWordsCard = true;

                        // Notify the adapter to re populate the items.
                        mWordAdapter.notifyDataSetChanged();

                        // Hide the hint message.
                        mHintTextView.animate()
                                .alpha(mWordAdapter.getItemCount() == 0 ? 1.0f : 0.0f)
                                .setDuration(50)
                                .start();

                        // Bring down the recognized words card view.
                        mRecognizedWordsCardView.animate()
                                .translationY(0)
                                .scaleX(1.0f)
                                .setDuration(200)
                                .setInterpolator(new DecelerateInterpolator())
                                .start();

                    }
                });
            }
        });

        // Called every time the fluidic element's position is changed.
        mFluidicElement.addPositionUpdateListener(new FludicPositionChangeListener() {
            @Override
            public void onPositionChanged(float newX, float newY) {

                float r = (bottom - newY) / (bottom / 2);
                if (r > 0.8) r = 0.8f;
                if (r < 0) r = 0;

                // Make the background progressively dark
                mBackgroundLayerView.setAlpha(r);

                // Also move the manual and the recognized words card views
                float dY = -(bottom - newY) / 2;
                mSecondaryCardsGroup.setTranslationY(dY);

                // If the recognized words card is not hidden, animate its motion too.
                if (animateRecognizedWordsCard) {
                    mRecognizedWordsCardView.setTranslationZ(mManualSearchCardView.getTranslationZ() - 1);
                    mRecognizedWordsCardView.setTranslationY(dY / 6);
                }


            }
        });

        // Pronounce the word when the FAB is clicked.
        mWordPronunciationVoiceFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tts.isSpeaking()) tts.stop();
                tts.speak(mWordTextView.getText().toString(), TextToSpeech.QUEUE_FLUSH,
                        null, "word");
            }
        });
    }

    /**
     * Setup the camera.
     */
    private void initiateCamera() {
        mCameraSource = getCameraSource(mGestureDrivenStringDetector);
    }

    /**
     * Sets up the initial UI and the initial states for the buttons.
     */
    private void setupUI() {

        mBackgroundLayerView.setAlpha(0.0f);
        mFluidicElement.setFluidityX(false);
        mFluidicElement.setY(screenSize.getHeight());
        mFluidicElement.setX(0);
        mFluidicElement.setScaleX(0.75f);
        mFluidicElement.setScaleX(0.75f);

        // Pause button should not be set on startup.
        pausePreviewBtn.setBackground(ContextCompat.getDrawable(context,
                R.drawable.button_background_color));
        pausePreviewBtn.setImageResource(R.drawable.freeze_btn_dynamic);


        // Do these once the fluidic element has been rendered.
        mFluidicElement.post(new Runnable() {
            @Override
            public void run() {

                // Initially set it below the screen (so that when the bounds are set it appears
                // to be coming from the bototm of the screen.
                mFluidicElement.layout(0, 0, mFluidicElement.getWidth(), mUiLinearLayout.getHeight());

                // Set the bounds for the fluidic element.
                int offset = (int) ((128 * context.getResources().getDisplayMetrics().density) + 0.5f);
                left = 0;
                right = 0;
                top = screenSize.getHeight() - mUiLinearLayout.getHeight();
                bottom = screenSize.getHeight() - offset;
                mFluidicElement.setBounds(left, top, right, bottom);

                DecelerateInterpolator interpolator = new DecelerateInterpolator();

                // Push the fluidic element down.
                mFluidicElement.flowDown();

                // Animate the fluidic element's summoning.
                mFluidicElement.animate()
                        .scaleY(1.0f)
                        .alpha(1.0f)
                        .setDuration(600)
                        .setInterpolator(interpolator)
                        .start();

                // Animate the X at a faster rate than the Y.
                mFluidicElement.animate()
                        .scaleX(1.0f)
                        .setDuration(300)
                        .setInterpolator(interpolator)
                        .start();

                // Fade in the cards on the top.
                mSecondaryCardsGroup.animate()
                        .alpha(1.0f)
                        .setDuration(400)
                        .setInterpolator(interpolator)
                        .start();

            }
        });
    }

    /**
     * Check if the permission to use the camera has been given. If yes start the core. If no, show
     * a snack bar and ask user for the permissions.
     */
    private boolean cameraPermissionOffered() {

        int PERMISSION_CAMERA = 0;

        // In case the permission is not granted ...
        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                PermissionChecker.PERMISSION_GRANTED) {

            // If app is running on API 23+, provide an explanation if needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    // Show the snack bar with a link to the app's settings page.
                    Snackbar.make(getWindow().getDecorView(), "No permission to use camera.", Snackbar.LENGTH_LONG)
                            .setAction("Grant", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivity(intent);
                                }
                            })
                            .setActionTextColor(ContextCompat.getColor(context, R.color.colorAccent))
                            .show();
                } else {
                    // Ask for the permissions.
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA);
                }
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * Is invoked after the user has granted or denied the permissions.
     *
     * @param requestCode  int code with which permissions were requested.
     * @param permissions  String[] of the requested permissions.
     * @param grantResults int[] of the permission request results.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            if (permissions[i].equals(Manifest.permission.CAMERA)) {
                if (grantResults[i] == PermissionChecker.PERMISSION_GRANTED) {
                    // Since the permission has been granted, start the camera.
                    initiateCamera();
                }
            }
        }
    }

    /**
     * Upon restart, re initiate the camera source as it would be null-ed during onStop().
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        mCameraSource = getCameraSource(mGestureDrivenStringDetector);
    }

    /**
     * Starts the camera when the app starts / restarts and re-initialises the history from the
     * shared preferences.
     */
    @Override
    protected void onStart() {
        super.onStart();
        startCamera();
        initiatePrimary();
        setupUI();
    }

    /**
     * Stop the preview and null-ify the camera source ( because if the app re-starts, the surface
     * would be a new one and hence cannot be used with the old camera source. ) Also updates the
     * current history.
     */
    @Override
    protected void onStop() {
        super.onStop();
        mCameraSource.stop();
        mCameraSource = null;

        // Save history in the shared preferences.
        historyController.saveHistory(context);

        // Stop all pending word requests.
        mWordAdapter.stopAllConnections();
    }

    /**
     * Return a camera source that uses the {@link #mGestureDrivenStringDetector}. Check if the
     * permission to use the camera is available.
     *
     * @param detector A GestureDrivenStringDetector
     * @return CameraSource
     */
    private CameraSource getCameraSource(GestureDrivenStringDetector detector) {

        int cameraFPS = 60;
        return new CameraSource.Builder(getApplicationContext(), detector)
                .setRequestedPreviewSize(screenSize.getHeight(), screenSize.getWidth())
                .setAutoFocusEnabled(true)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(cameraFPS)
                .build();
    }

    /**
     * Start the live preview, only if the surface view has been rendered.
     */
    private void startCamera() {
        mSurfaceView.post(new Runnable() {
            @Override
            public void run() {
                try {
                    mCameraSource.start(mSurfaceView.getHolder());
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, e.getMessage());
                }
            }
        });
    }

    /**
     * Stop the live preview if the camera source is not null.
     */
    private void stopCamera() {
        if (mCameraSource != null) {
            mCameraSource.stop();
        }
    }

    /**
     * Check if the fluidic element is at the bottom position, else bring it down.
     * Resume the preview if paused.
     * Else calls the activity's onBackPressed().
     */
    @Override
    public void onBackPressed() {

        if (mFluidicElement.getY() != mFluidicElement.getMaxY()) {
            // If the UI card is not down, bring it down.
            mFluidicElement.flowDown();
        } else if (previewPaused) {
            // If preview is paused, resume the preview.
            pausePreview(pausePreviewBtn);
        } else {
            // Else close the app.
            super.onBackPressed();
        }
    }

    /**
     * Returns a {@link WordDataUpdateCallback} callback to update the UI when a word's data is
     * updated (throught the {@link adk.string.Wordnik.WordnikGrabber}).
     *
     * @return WordDataUpdateCallback.
     */
    private WordDataUpdateCallback getWordDataUpdateCallback() {

        return new WordDataUpdateCallback() {

            @Override
            synchronized public void onUpdate(final Word word) {

                // Update the UI only if the word being shown is the same as the word that has
                // invoked this update.
                if (mWordTextView.getText().toString().equals(word.getWord())) {

                    final float oldHeight = mUiLinearLayout.getHeight();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            // Show current status of the word's data. ( Found/ Not found/ Searching)
                            switch (word.getOverallStatus()) {
                                case Word.STATUS_AVAILABLE: {
                                    mWordStatus.setVisibility(View.GONE);
                                    mGoogleSearchBtn.setPressed(false);
                                    break;
                                }
                                case Word.STATUS_NOT_AVAILABLE: {
                                    mWordStatus.setTextColor(ContextCompat.getColor(context, R.color.red));
                                    mWordStatus.setText(R.string.status_400);
                                    mGoogleSearchBtn.setPressed(true);
                                    break;
                                }
                                case Word.STATUS_SEARCHING: {
                                    mWordStatus.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
                                    mWordStatus.setVisibility(View.VISIBLE);
                                    mWordStatus.setText(R.string.status_600);
                                    mGoogleSearchBtn.setPressed(false);
                                    break;
                                }
                            }

                            // Show any available data; else hide the respective components.

                            // Pronunciation
                            if (word.hasData(Word.MODE_PRONUNCIATION)) {
                                mWordPronunciationTextView.setVisibility(View.VISIBLE);
                                mWordPronunciationTextView.setTextColor(ContextCompat
                                        .getColor(context, R.color.defined_color));
                                mWordPronunciationTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
                                mWordPronunciationTextView.setText(word.getData(Word.MODE_PRONUNCIATION));
                            } else {
                                mWordPronunciationTextView.setVisibility(GONE);
                            }

                            // Noun
                            if (word.hasData(Word.MODE_NOUN)) {
                                mNounHintTextView.setText(R.string.noun);
                                mNounHintTextView.setVisibility(View.VISIBLE);
                                mWordDefNoun.setVisibility(View.VISIBLE);
                                mWordDefNoun.setText(word.getData(Word.MODE_NOUN));
                            } else {
                                mNounHintTextView.setVisibility(GONE);
                                mWordDefNoun.setVisibility(GONE);
                            }

                            // Verb
                            if (word.hasData(Word.MODE_VERB)) {
                                mVerbHintTextView.setText(R.string.verb);
                                mVerbHintTextView.setVisibility(View.VISIBLE);
                                mWordDefVerb.setVisibility(View.VISIBLE);
                                mWordDefVerb.setText(word.getData(Word.MODE_VERB));
                            } else {
                                mVerbHintTextView.setVisibility(GONE);
                                mWordDefVerb.setVisibility(GONE);
                            }

                            // Adjective
                            if (word.hasData(Word.MODE_ADJ)) {
                                mAdjectiveHintTextView.setText(R.string.adj);
                                mAdjectiveHintTextView.setVisibility(View.VISIBLE);
                                mWordDefAdj.setVisibility(View.VISIBLE);
                                mWordDefAdj.setText(word.getData(Word.MODE_ADJ));
                            } else {
                                mAdjectiveHintTextView.setVisibility(GONE);
                                mWordDefAdj.setVisibility(GONE);
                            }

                            // Example
                            if (word.hasData(Word.MODE_EXAMPLE)) {
                                mExampleHintTextView.setText(R.string.example);
                                mExampleHintTextView.setVisibility(View.VISIBLE);
                                mWordExample.setVisibility(View.VISIBLE);
                                mWordExample.setText(word.getData(Word.MODE_EXAMPLE));
                            } else {
                                mExampleHintTextView.setVisibility(GONE);
                                mWordExample.setVisibility(GONE);
                            }

                            synchronized (mUiLinearLayout) {
                                mUiLinearLayout.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        float newHeight = mUiLinearLayout.getHeight();
                                        final float offset = newHeight - oldHeight;
                                        Log.d(TAG, "Offset is " + offset);

                                        mFluidicElement.requestLayout();
                                        mFluidicElement.post(new Runnable() {
                                            @Override
                                            public void run() {

                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {

                                                        synchronized (mFluidicElement) {
                                                            mFluidicElement
                                                                    .setBounds(mFluidicElement.getMinX(),
                                                                            mFluidicElement.getMinY() - offset,
                                                                            mFluidicElement.getMaxX(),
                                                                            mFluidicElement.getMaxY(),
                                                                            true,
                                                                            mFluidicElement.getX(),
                                                                            (60 * context.getResources().getDisplayMetrics().density) + 0.5f);
                                                        }

                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }

                            // If history is enabled, save the word to the history.
                            if (historyController.isHistoryEnabled()) {
                                historyController.addToHistory(word);
                            }
                        }
                    });
                }
            }

        };
    }

    /**
     * Open the history activity.
     *
     * @param view Button that invokes this method.
     */
    public void showHistory(View view) {

        // Save all the words to the history.
        historyController.saveHistory(context);

        // Show the history.
        startActivity(new Intent(this,
                HistoryActivity.class));
    }

    /**
     * Open the settings activity.
     *
     * @param view Button that invokes this method.
     */
    public void openSettings(View view) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    /**
     * Google search the word being shown on the {@link #mWordTextView}.
     *
     * @param view Button that invokes this method.
     */
    public void openInChrome(View view) {
        // Google the word being shown.
        startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse(Utility.CHROME_DICT_BASE_LINK + mWordTextView.getText().toString())));
    }

    /**
     * Pauses the live preview by ignoring the frames from the camera.
     *
     * @param view Button that invokes this method.
     */
    public void pausePreview(View view) {

        // Change the icon and the background of the button.
        view.setBackground(ContextCompat.getDrawable(context, previewPaused ? R.color.white : R.color.colorAccent));
        ((ImageButton) view).setImageResource(previewPaused ? R.drawable.ic_freeze : R.drawable.ic_freeze_white);

        if (previewPaused) {
            startCamera();
            mGestureDrivenStringDetector.pausePreview(false);
            view.setBackground(ContextCompat.getDrawable(context, R.drawable.button_background_color));
            ((ImageButton) view).setImageResource(R.drawable.freeze_btn_dynamic);
        } else {
            mGestureDrivenStringDetector.pausePreview(true);
            stopCamera();
        }

        previewPaused = !previewPaused;
    }

    /**
     * Searches the word entered in the {@link #mManualWordEditText} and adds it to the temporary
     * history if saving is enabled.
     *
     * @param view Button that invokes this method.
     */
    public void manualSearch(View view) {

        // Hide the keyboard.
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(mManualWordEditText.getWindowToken(), 0);
        }

        // Check if the edit text has some text in it.
        String enteredWord = mManualWordEditText.getText().toString();
        if (!enteredWord.equals("")) {

            // Set the word in the card.
            mWordTextView.setText(enteredWord);

            // Before building a new word, check if the word is already in the history.
            Word word = historyController.hasWord(enteredWord);

            if (word == null) {
                // Not found.
                word = new Word(enteredWord, mWordDataUpdateCallback, true);
            } else {
                // Found.
                word.getDataSequentially();
            }

            // Update the UI to show the searching status.
            mWordDataUpdateCallback.onUpdate(word);

            // If history is enabled, save the word to history.
            if (historyController.isHistoryEnabled()) historyController.addToHistory(word);

        }
    }
}
