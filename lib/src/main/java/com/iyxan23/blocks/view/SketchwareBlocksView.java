package com.iyxan23.blocks.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class SketchwareBlocksView extends View {

    Paint rect_paint;
    Paint text_paint;

    int left_position = 50;
    int top_position = 50;
    int shadow_height = 10;

    SketchwareEvent data;

    public SketchwareBlocksView(Context context) {
        super(context);
        initialize();
    }

    public SketchwareBlocksView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public SketchwareBlocksView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    public SketchwareBlocksView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    public void setEvent(SketchwareEvent event) {
        this.data = event;

        initialize();
    }

    private void initialize() {
        if (data == null) {
            data = new SketchwareEvent("MainActivity", "onCreate");

            data.blocks.add(new SketchwareBlock("Hello World", "1", 2, new ArrayList<>(), 0xFFE10C0C));
            data.blocks.add(new SketchwareBlock("This is SketchwareBlocksView", "2", 3, new ArrayList<>(), 0xFFD1159C));
            data.blocks.add(new SketchwareBlock("This block resizes", "3", 4, new ArrayList<>(), 0xFF14D231));
            data.blocks.add(new SketchwareBlock("According to the text's width", "4", 5, new ArrayList<>(), 0xFF2115D1));
            data.blocks.add(new SketchwareBlock("Made by Iyxan23 (github.com/Iyxan23)", "4", 5, new ArrayList<>(), 0xFF2115D1));
            data.blocks.add(new SketchwareBlock("Finish Activity", "5", -1, new ArrayList<>(), 0xFF1173E4));
        }

        text_paint = new Paint();
        text_paint.setTypeface(Typeface.DEFAULT);
        text_paint.setStyle(Paint.Style.FILL);
        text_paint.setFakeBoldText(true);
        text_paint.setAntiAlias(true);
        text_paint.setColor(0xFFFFFFFF);
        text_paint.setTextSize(30f);

        rect_paint = new Paint();
        rect_paint.setAntiAlias(true);
        rect_paint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        boolean is_overlapping = true;

        int block_height = 60;
        int event_offset = 50;

        int shadow_height = 10;

        // Draw the blocks from top to bottom
        int previous_block_color = data.color;
        for (int i = 0; i < data.blocks.size(); i++) {

            int top_position;

            if (i == 0) {
                top_position = event_offset + block_height;

            } else {
                top_position = (i + 1) * block_height - (block_height - event_offset);

                // Set the offset for blocks below the first block
                top_position += (i - 1) * (block_height - event_offset);

                // Don't forget the first block
                top_position += block_height;

                if (is_overlapping)
                    // Overlap the previous block's shadow
                    top_position -= (i - 1) * shadow_height;
            }

            data.blocks
                .get(i)
                .draw(
                        canvas,
                        rect_paint,
                        text_paint,
                        top_position,
                        left_position,
                        block_height,
                        shadow_height,
                        is_overlapping,
                        previous_block_color
                );
            previous_block_color = data.blocks.get(i).color;
        }

        data.draw(canvas, rect_paint, text_paint);
    }
}
