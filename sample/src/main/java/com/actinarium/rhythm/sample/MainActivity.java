/*
 * Copyright (C) 2016 Actinarium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.actinarium.rhythm.sample;

import android.app.ActivityManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.actinarium.rhythm.RhythmControl;
import com.actinarium.rhythm.RhythmDrawable;
import com.actinarium.rhythm.RhythmGroup;
import com.actinarium.rhythm.RhythmOverlay;
import com.actinarium.rhythm.spec.DimensionsLabel;
import com.actinarium.rhythm.spec.Guide;

import static com.actinarium.rhythm.sample.RhythmSampleApplication.ACTIVITY_OVERLAY_GROUP;
import static com.actinarium.rhythm.sample.RhythmSampleApplication.CARD_OVERLAY_GROUP;
import static com.actinarium.rhythm.sample.RhythmSampleApplication.TEXT_OVERLAY_GROUP;

public class MainActivity extends AppCompatActivity {

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Rhythm-unrelated init routines
        setupToolbar();
        setupRecentsIcon();
        // Make links clickable
        ((TextView) findViewById(R.id.copy_1)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) findViewById(R.id.copy_6)).setMovementMethod(LinkMovementMethod.getInstance());

        // Find required layouts
        LinearLayout contentView = (LinearLayout) findViewById(R.id.content);
        CardView cardView = (CardView) findViewById(R.id.card);
        Button toggleCardOverlay = (Button) findViewById(R.id.toggle_card_overlay);
        LinearLayout mallowsView = (LinearLayout) findViewById(R.id.thumbs);

        // Setup Rhythm
        // First let's attach the activity and the card to the groups defined in app's RhythmControl
        final RhythmControl rhythmControl = ((RhythmSampleApplication) getApplication()).getRhythmControl();
        final RhythmGroup cardOverlayGroup = rhythmControl.getGroup(CARD_OVERLAY_GROUP);
        final RhythmGroup activityOverlayGroup = rhythmControl.getGroup(ACTIVITY_OVERLAY_GROUP);
        final RhythmGroup textOverlayGroup = rhythmControl.getGroup(TEXT_OVERLAY_GROUP);

        // Decorate the background of our topmost scrollable layout (LinearLayout) to draw overlays from the 1st group
        // The decorate() method works with all views and draws overlay UNDER content
        activityOverlayGroup.decorate(contentView);

        // Decorate the foreground of our intermission card with the overlay from the 2nd group
        // If you have FrameLayout or its child classes, you can use decorateForeground() to draw overlays OVER content
        cardOverlayGroup.decorateForeground(cardView);

        // Decorate all text views with overlays attached to the 3rd group
        for (int i = 0, count = contentView.getChildCount(); i < count; i++) {
            final View child = contentView.getChildAt(i);
            if (child instanceof com.actinarium.aligned.TextView) {
                textOverlayGroup.decorate(child);
            }
        }

        // Make one of the card buttons toggle the card's overlay
        toggleCardOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardOverlayGroup.selectNextOverlay();
            }
        });

        // Disable card and text overlays by default
        cardOverlayGroup.selectOverlay(RhythmGroup.NO_OVERLAY);
        textOverlayGroup.selectOverlay(RhythmGroup.NO_OVERLAY);

        // Now let's just

        RhythmGroup mallowsGroup = new RhythmGroup();
        final int accentColor = getResources().getColor(R.color.accent);


        RhythmOverlay frameAndDimensions = new RhythmOverlay()
                .addLayer(new Guide(Gravity.LEFT, 0).setAlignOutside(true).setColor(accentColor))
                .addLayer(new Guide(Gravity.TOP, 0).setAlignOutside(true).setColor(accentColor))
                .addLayer(new Guide(Gravity.BOTTOM, 0).setAlignOutside(true).setColor(accentColor))
                .addLayer(new DimensionsLabel(getResources().getDisplayMetrics().density))
                .addToGroup(mallowsGroup);

        // Decorate a few mallows with this group
        mallowsGroup.decorate(mallowsView.getChildAt(0), mallowsView.getChildAt(1), mallowsView.getChildAt(2));

        // Or get a Drawable and use it explicitly
        Drawable drawable = mallowsGroup.makeDrawable();
        mallowsView.getChildAt(3).setBackgroundDrawable(drawable);

        // Furthermore, you may not even need groups - for full manual transmission make RhythmDrawables explicitly
        RhythmOverlay lastFrameOverlay = new RhythmOverlay()
                .addLayersFrom(frameAndDimensions)
                .addLayer(new Guide(Gravity.RIGHT, 0).setAlignOutside(true).setColor(accentColor));
        RhythmDrawable totallyExplicitlyCreatedDrawable = new RhythmDrawable(lastFrameOverlay);
        mallowsView.getChildAt(3).setBackgroundDrawable(totallyExplicitlyCreatedDrawable);

        // Take a look at FeaturesDialogFragment and its layout for RhythmicFrameLayout example
    }

    // Methods for setup, not really relevant to showcasing Rhythm

    private void setupToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setElevation(getResources().getDimension(R.dimen.actionBarElevation));
        actionBar.setTitle(R.string.app_title);
    }

    private void setupRecentsIcon() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = getTheme();
            theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
            int color = typedValue.data;

            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.arl_rhythm);
            ActivityManager.TaskDescription td = new ActivityManager.TaskDescription(null, bm, color);

            setTaskDescription(td);
            bm.recycle();
        }
    }
}
