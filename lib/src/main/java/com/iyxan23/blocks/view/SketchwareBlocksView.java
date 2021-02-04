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
        for (int i = data.blocks.size() - 1; i > -1; i--) {
            data.blocks
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

        data.draw(canvas, rect_paint, text_paint);
    }
}
