package com.thespeakers_studio.thespeakersstudioapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.model.Outline;
import com.thespeakers_studio.thespeakersstudioapp.model.OutlineItem;
import com.thespeakers_studio.thespeakersstudioapp.model.PresentationData;
import com.thespeakers_studio.thespeakersstudioapp.utils.AnalyticsHelper;
import com.thespeakers_studio.thespeakersstudioapp.utils.OutlineHelper;
import com.thespeakers_studio.thespeakersstudioapp.utils.Utils;

import java.util.ArrayList;

import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.makeLogTag;

/**
 * Created by smcgi_000 on 8/4/2016.
 */
public class OutlineActivity extends BaseActivity implements
    View.OnClickListener {

    private static final String TAG = makeLogTag(OutlineActivity.class);
    private static final String SCREEN_LABEL = "Presentation Outline";

    public static final int REQUEST_CODE = 3;

    private PresentationData mPresentation;

    private LinearLayout mContentWrapper;
    private LinearLayout mOutlineList;

    private OutlineHelper mHelper;
    private Outline mOutline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_outline);

        AnalyticsHelper.sendScreenView(SCREEN_LABEL);

        setPresentationId(getIntent().getStringExtra(Utils.INTENT_PRESENTATION_ID));

        mContentWrapper = (LinearLayout) findViewById(R.id.content_wrapper);
        mOutlineList = (LinearLayout) findViewById(R.id.outline_list);
    }

    private void setPresentationId(String id) {
        if (id != null) {
            mPresentation = mDbHelper.loadPresentationById(id);
            mOutline = Outline.fromPresentation(this, mPresentation);
            render();
        }
    }

    private void render() {
        mHelper = new OutlineHelper();

        ((TextView) findViewById(R.id.outline_title)).setText(mOutline.getTitle());
        ((TextView) findViewById(R.id.outline_duration)).setText(mOutline.getDuration());
        ((TextView) findViewById(R.id.outline_date)).setText(mOutline.getDate());

        findViewById(R.id.fab_practice).setOnClickListener(this);

        LinearLayout listWrapper = (LinearLayout) findViewById(R.id.outline_list);
        if (listWrapper != null) {
            listWrapper.removeAllViews();
            renderList(mOutline.getItems(), listWrapper, 1);
        }
    }

    private void renderList (ArrayList<OutlineItem> items, LinearLayout wrapper, int level) {
        if (items.size() == 0) {
            return;
        }

        LayoutInflater inflater = getLayoutInflater();
        int index = 0;

        for (OutlineItem item : items) {
            RelativeLayout itemLayout = (RelativeLayout) inflater.inflate(R.layout.outline_item, wrapper, false);

            // set the icon
            ((TextView) itemLayout.findViewById(R.id.list_main_bullet)).setText(mHelper.getBullet(level, index + 1));
            // set the text for this thing
            ((TextView) itemLayout.findViewById(R.id.list_topic)).setText(item.getText());
            // set the duration for this thing
            long duration = item.getDuration();
            TextView timeView = (TextView) itemLayout.findViewById(R.id.list_duration);
            if (duration > 0) {
                timeView.setText(Utils.getTimeStringFromMillis(duration, getResources()));
                //timeView.setText("" + duration);
            } else {
                timeView.setText("");
            }

            renderList(item.getSubItems(), (LinearLayout) itemLayout.findViewById(R.id.outline_sub_item_wrapper), level == 3 ? 1 : level + 1);

            wrapper.addView(itemLayout);

            index++;
        }
    }

    @Override
    protected void setLayoutPadding(int actionBarSize) {
        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams)
                mContentWrapper.getLayoutParams();
        if (mlp.topMargin != actionBarSize) {
            mlp.topMargin = actionBarSize;
            mContentWrapper.setLayoutParams(mlp);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_practice:
                Intent intent = new Intent(getApplicationContext(), PracticeSetupActivity.class);
                intent.putExtra(Utils.INTENT_PRESENTATION_ID, mPresentation.getId());
                //startActivityForResult(intent, PracticeSetupActivity.REQUEST_CODE);
                createBackStack(intent);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_outline, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.menu_action_edit_outline:
                Toast.makeText(this, "You can't edit outlines just yet", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_action_outline_view:
                Toast.makeText(this, "The timeline com.thespeakers_studio.thespeakersstudioapp.view isn't ready yet", Toast.LENGTH_SHORT).show();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (getCallingActivity() != null) {
            returnActivityResult();
        } else {
            navigateUpOrBack(this, getIntent().getExtras(), null);
        }
    }

    private void returnActivityResult() {
        Intent intent = new Intent();
        intent.putExtra(Utils.INTENT_PRESENTATION_ID, mPresentation.getId());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

}
