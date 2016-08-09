package com.thespeakers_studio.thespeakersstudioapp.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.settings.SettingsUtils;
import com.thespeakers_studio.thespeakersstudioapp.utils.Utils;

import com.thespeakers_studio.thespeakersstudioapp.model.Outline;
import com.thespeakers_studio.thespeakersstudioapp.model.OutlineItem;

import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.LOGD;

/**
 * Created by smcgi_000 on 7/20/2016.
 */
public class PresentationPracticeDialog extends DialogFragment implements View.OnClickListener {


    public interface PresentationPracticeDialogInterface {
        public void onDialogDismissed();
    }

    public static final String TAG = "timer";

    private PresentationPracticeDialogInterface mInterface;
    private Outline mOutline;
    private Dialog mDialog;

    private int mDuration;

    private boolean mIsPractice;

    private boolean mDelay;
    private boolean mDisplayTimer;
    private boolean mShowWarning;
    private boolean mTrack;
    private boolean mVibrate;

    private TextView mOutputMainView;
    private TextView mTimerView;
    private TextView mTimerTotalView;
    private TextView mOutputSubView;
    private TextView mWarningView;
    private ImageButton mButtonLeft;
    private ImageButton mButtonRight;
    private ImageButton mButtonDone;

    private Handler mTimeHandler = new Handler();

    private long mOutlineDuration; // the duration of the whole presentation
    private long mCurrentExpiration; // the timer time, in milliseconds
    private long mStartTime = 0L;
    private long mElapsed = 0l;
    //private long mCurrentStartTime = 0L;

    private int mTopicIndex;
    private int mSubTopicIndex;

    private boolean mStarted;
    private boolean mPaused;
    private boolean mFinished;

    private final int PULSE = 500;
    private final int PULSE_GAP = 200;
    private final int BUMP = 200;

    // TODO: make animation times and durations constants
    private final int DELAY_TIME = 5000;
    private final int INTERSTITIAL_DURATION = 1000;
    private final int ANIMATION_DURATION = 300;

    private final int WARNING_TIME = 300000;
    private final int WARNING_DURATION = 5000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mDelay = SettingsUtils.getTimerWait(getContext());
        mDisplayTimer = SettingsUtils.getTimerShow(getContext());
        mShowWarning = SettingsUtils.getTimerWarning(getContext());
        mTrack = SettingsUtils.getTimerTrack(getContext());
        mVibrate = SettingsUtils.getTimerVibrate(getContext());
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mInterface != null) {
            mInterface.onDialogDismissed();
        }

        mTimeHandler.removeCallbacks(updateTimerThread);
    }

    public void setInterface(PresentationPracticeDialogInterface inter) {
        mInterface = inter;
    }

    public void setup (Outline outline) {
        if (outline != null) {
            mOutline = outline;
            mIsPractice = true;
        } else {
            mIsPractice = false;
        }
    }
    public void setup (int duration) {
        mIsPractice = false;
        mDuration = duration * 60 * 1000; // we'll be getting the duration in minutes
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mDialog = new Dialog(getActivity(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        mDialog.setContentView(R.layout.dialog_practice);
        mDialog.getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        mTimerTotalView = (TextView) mDialog.findViewById(R.id.practice_timer_total);
        mOutputSubView = (TextView) mDialog.findViewById(R.id.practice_sub);
        mButtonLeft = (ImageButton) mDialog.findViewById(R.id.button_left);
        mButtonRight = (ImageButton) mDialog.findViewById(R.id.button_right);
        mButtonDone = (ImageButton) mDialog.findViewById(R.id.button_done);
        mOutputMainView = (TextView) mDialog.findViewById(R.id.practice_main);
        mTimerView = (TextView) mDialog.findViewById(R.id.practice_timer_current);
        mWarningView = (TextView) mDialog.findViewById(R.id.practice_interval_warning);

        mTimerView.setOnClickListener(this);
        mDialog.findViewById(R.id.button_next).setOnClickListener(this);

        return mDialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mDelay) {
            startDelay();
        } else {
            start();
        }
        mTimeHandler.postDelayed(updateTimerThread, ANIMATION_DURATION);
    }

    @Override
    public void onStop() {
        super.onStop();
        mTimeHandler.removeCallbacks(updateTimerThread);
    }

    public void startDelay() {
        mTimerTotalView.setVisibility(View.GONE);
        mOutputSubView.setVisibility(View.GONE);

        //mOutputMainView.setText(R.string.get_ready);
        showText(mOutputMainView, R.string.get_ready);
        mTimerView.setText("");

        mCurrentExpiration = DELAY_TIME;

        mStarted = false;
    }

    private void start() {
        mStarted = true;
        mStartTime = 0;
        mElapsed = 0;
        mTopicIndex = -1;
        mOutlineDuration = mIsPractice ? mOutline.getDurationMillis() : mDuration;
        mCurrentExpiration = 0;
    }

    public boolean showText(final TextView view, final String text) {
        String existing = view.getText().toString();
        if (existing.equals(text)) {
            return false;
        }
        boolean empty = existing.isEmpty();

        Animation outfade = new AlphaAnimation(1f, 0f);
        outfade.setDuration(empty ? 0 : ANIMATION_DURATION);
        Animation outslide = new TranslateAnimation(0f, -50f, 0f, 0f);
        outslide.setDuration(empty ? 0 : ANIMATION_DURATION);
        AnimationSet out = new AnimationSet(true);
        out.addAnimation(outfade);
        out.addAnimation(outslide);

        Animation infade = new AlphaAnimation(0f, 1f);
        infade.setDuration(ANIMATION_DURATION);
        Animation inslide = new TranslateAnimation(50f, 0f, 0, 0);
        inslide.setDuration((ANIMATION_DURATION));
        final AnimationSet in = new AnimationSet(true);
        in.addAnimation(infade);
        in.addAnimation(inslide);

        view.setVisibility(View.VISIBLE);
        view.clearAnimation();

        out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setText(text);
                view.startAnimation(in);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(out);
        return true;
    }
    public boolean showText(TextView view, int id) {
        return showText(view, getResources().getString(id));
    }

    public void nextItem() {
        if (mIsPractice) {
            if (!mStarted) {
                start();
            }
            mSubTopicIndex++;
            if (mTopicIndex < 0 || mOutline.getItem(mTopicIndex) == null ||
                    mSubTopicIndex >= mOutline.getItemsByParentId("").size()) { // TODO: This is totally broken
                // move to the next topic
                mTopicIndex++;
                mSubTopicIndex = -1;
                if (mTopicIndex >= mOutline.getItemCount()) {
                    // we're done!
                    finish();
                } else {
                    showText(mOutputMainView, mOutline.getItem(mTopicIndex).getText());
                    // when showing a new topic, we'll highlight the topic name for a second
                    mCurrentExpiration = mElapsed + INTERSTITIAL_DURATION;
                    vibrate(PULSE);
                }

                hideButton(mButtonRight);
            } else {
                // show the next sub item
                showText(mOutputSubView, mOutline.getItem(mTopicIndex).getText());
                OutlineItem subitem = mOutline.getItem(mTopicIndex); // .getSubItem(mSubTopicIndex); TODO: this is totally broken too
                showText(mOutputMainView, subitem.getText());

                if (mPaused) {
                    // mCurrentExpiration is already set, because we will continue from where we left off
                    mPaused = false;
                } else {
                    // add any remaining time from the last item, in case the user skipped ahead
                    // round the duration of this item to the nearest 1000 to make sure it doesn't
                    // throw off the timer
                    long thisDuration = Utils.roundToThousand(subitem.getDuration());
                    long remainingTime = mCurrentExpiration - mElapsed;
                    mCurrentExpiration = mElapsed + (remainingTime + thisDuration);

                    if (mSubTopicIndex == 0) {
                        // since we wasted time showing the topic name, we need to account for that here
                        mCurrentExpiration -= INTERSTITIAL_DURATION;
                    }
                }

                showButton(mButtonRight);
            }

            if (mTopicIndex > 0 || mSubTopicIndex > 0) {
                //showButton(mButtonLeft);
            }
        } else { // this is a timer, so just show a timer
            showText(mOutputSubView, R.string.timer);
            showText(mOutputMainView, "");
            if (!mStarted) {
                mCurrentExpiration = mDuration;
                start();
            } else if (mPaused) {
                mPaused = false;
            } else {
                finish();
            }
        }

        //mTimeHandler.postDelayed(updateTimerThread, 0);
    }

    private void finish() {
        mFinished = true;

        vibrate(new long[]{PULSE, PULSE_GAP, PULSE, PULSE_GAP, PULSE});
        mOutlineDuration = 0;
        mCurrentExpiration = 0;
        mStartTime = 0;

        showText(mOutputSubView, "");
        showText(mOutputMainView, R.string.done);

        hideButton(mButtonRight);
        showButton(mButtonDone);
    }

    private void animateButton(final View button, boolean in) {
        if (button.getVisibility() == View.VISIBLE && in) {
            return;
        } else if (button.getVisibility() == View.GONE && !in) {
            return;
        }
        button.setVisibility(View.VISIBLE);
        //button.setAlpha(0f);

        Animation infade = new AlphaAnimation(in ? 0f : 1f, in ? 1f : 0f);
        infade.setDuration(ANIMATION_DURATION);
        Animation inslide = new TranslateAnimation(in ? 50f : 0f, in ? 0f : 50f, 0, 0);
        inslide.setDuration(ANIMATION_DURATION);

        AnimationSet set = new AnimationSet(true);
        set.addAnimation(infade);
        set.addAnimation(inslide);

        if (!in) {
            set.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    button.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
        }

        button.startAnimation(set);
    }
    private void showButton(View button) {
        animateButton(button, true);
    }
    private void hideButton(View button) {
        animateButton(button, false);
    }

    private long now() {
        return SystemClock.uptimeMillis();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.practice_timer_current:
                // tap the time to pause or resume the timer
                if (!mPaused) {
                    mPaused = true;
                    showText(mOutputMainView, R.string.paused);
                    mTimeHandler.removeCallbacks(updateTimerThread);

                    // save the time remaining so we can pick up where we left off
                    mOutlineDuration = mOutlineDuration - mElapsed; //getRemaining(mStartTime, mOutlineDuration);
                    if (mStarted) {
                        // if the presentation has started, save the current position there, too
                        mCurrentExpiration = mCurrentExpiration - mElapsed; //getRemaining(mCurrentStartTime, mCurrentExpiration);
                    }

                    Animation blink = new AlphaAnimation(0f, 1f);
                    blink.setDuration(50);
                    blink.setStartOffset(500);
                    blink.setRepeatMode(Animation.REVERSE);
                    blink.setRepeatCount(Animation.INFINITE);
                    mTimerView.setAnimation(blink);
                } else {
                    mStartTime = 0;

                    mTimerView.clearAnimation();
                    mSubTopicIndex--;
                    nextItem();
                }
                break;
            case R.id.button_next:
                // skip to next, but only if the presentation is running
                if (mStartTime > 0) {
                    vibrate(BUMP);
                    if (!mFinished) {
                        nextItem();
                    } else {
                        // we are done, so close the thing
                        dismiss();
                    }
                }
                break;
        }
    }

    private void vibrate(long[] pattern) {
        if (pattern.length == 0) {
            return;
        }
        if (mVibrate) {
            Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(pattern, -1);
        }
    }
    private void vibrate(int pattern) {
        vibrate(new long[] {pattern});
    }

    private void setTimer(TextView timer, long time) {
        timer.setVisibility(View.VISIBLE);
        timer.setText(Utils.getTimeStringFromMillis(time, getResources()));
    }

    private Runnable updateTimerThread = new Runnable() {
        @Override
        public void run() {
            if (mStartTime == 0) {
                mStartTime = now();
            }

            mElapsed = now() - mStartTime;
            long totalRemaining = mOutlineDuration - mElapsed;
            long currentRemaining = mCurrentExpiration - mElapsed;

            if (currentRemaining <= 0 && !mFinished) {
                nextItem();
            }

            if (!mFinished) {
                if (mStarted && mDisplayTimer && mIsPractice) {
                    setTimer(mTimerTotalView, totalRemaining);
                }
                if (mSubTopicIndex > -1 && mDisplayTimer) {
                    if (mStarted) {
                        setTimer(mTimerView, currentRemaining);
                    } else {
                        showText(mTimerView, Utils.secondsFromMillis(currentRemaining));
                    }
                } else {
                    mTimerView.setText("");
                }
                //Log.d("SS", "total remaining: " + totalRemaining + " - current remaining: " + currentRemaining);
            } else {
                // we are totally done, so we can begin counting up!
                setTimer(mTimerView, mElapsed);
                setTimer(mTimerTotalView, 0);
            }

            mTimeHandler.postDelayed(this, 0);

            if (mStarted && mShowWarning && !mFinished) {
                if (totalRemaining <= WARNING_TIME && totalRemaining >= WARNING_TIME - 100) {
                    // five minute warning
                    if (showText(mWarningView, R.string.five_minutes_left)) {
                        vibrate(new long[] {PULSE, PULSE_GAP, PULSE});
                    }
                } else if (totalRemaining <= (WARNING_TIME - WARNING_DURATION)
                        && totalRemaining >= (WARNING_TIME - WARNING_DURATION - 100)) {
                    // hide it after five seconds
                    showText(mWarningView, "");
                }
            }
        }
    };

}
