package com.iyxan23.blocks.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class SketchwareBlocksView extends View {

    Paint rect_paint;
    Paint text_paint;

    SketchwareEvent event_test;

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

    private void initialize() {
        event_test = new SketchwareEvent("MainActivity", "onCreate");

        event_test.blocks.add(new SketchwareBlock("Test", "1", 2, new ArrayList<>(), 0xFFE10C0C));
        event_test.blocks.add(new SketchwareBlock("Hello", "2", 3, new ArrayList<>(), 0xFFD1159C));
        event_test.blocks.add(new SketchwareBlock("World", "3", 4, new ArrayList<>(), 0xFF14D231));
        event_test.blocks.add(new SketchwareBlock("Dark Blue", "4", 5, new ArrayList<>(), 0xFF2115D1));
        event_test.blocks.add(new SketchwareBlock("Finish Activity", "5", -1, new ArrayList<>(), 0xFF1173E4));

        text_paint = new Paint();
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
        /*
        rect_paint.setColor(0xFFB1720C);
        canvas.drawRect(50, 50, 400, 110, rect_paint);
        canvas.drawRect(100, 50, 175, 125, rect_paint);
        rect_paint.setColor(0xFFE08E0A);
        canvas.drawRect(50, 50, 400, 100, rect_paint);
        canvas.drawRect(50, 35, 300, 100, rect_paint);
        canvas.drawRect(100, 50, 175, 115, rect_paint);

        canvas.drawText("On activity create", 60, 85, text_paint);
         */
        // Draw the blocks in backwards (to preserve the outset thing)
        for (int i = event_test.blocks.size() - 1; i > -1; i--) {
            event_test.blocks
                    .get(i)
                    .draw(canvas, rect_paint, text_paint, i + 1);
        }
        /*
        // Draw the blocks (only used to debug)
        for (int i = 0; i < event_test.blocks.size(); i++) {
            event_test.blocks
                    .get(i)
                    .draw(canvas, rect_paint, text_paint, i + 1);
        }
         */

        event_test.draw(canvas, rect_paint, text_paint);
    }
}
