package com.iyxan23.blocks.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class SketchwareBlocksView extends View {

    Paint rect_paint;
    Paint text_paint;

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
        text_paint = new Paint();
        text_paint.setStyle(Paint.Style.FILL);
        text_paint.setAntiAlias(true);
        text_paint.setColor(0xFFFFFFFF);
        text_paint.setTextSize(35f);

        rect_paint = new Paint();
        rect_paint.setStrokeWidth(10f);
        rect_paint.setAntiAlias(true);
        rect_paint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        rect_paint.setColor(Color.parseColor("#A50909"));
        canvas.drawRect(50, 50, 400, 110, rect_paint);
        canvas.drawRect(100, 50, 175, 125, rect_paint);
        rect_paint.setColor(Color.parseColor("#E10C0C"));
        canvas.drawRect(50, 50, 400, 100, rect_paint);
        canvas.drawRect(100, 50, 175, 115, rect_paint);

        canvas.drawText("Test", 60, 85, text_paint);
    }
}
