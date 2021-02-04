package com.iyxan23.blocks.view;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;

public class SketchwareField {

    // This boolean indicates if this is a block or not
    public boolean is_block = true;
    public String value = "";  // This value is going to be used if is_block is false

    public SketchwareBlock block;

    Paint text_paint = new Paint();
    Paint rect_paint = new Paint();

    /**
     * This will initialize this class as a SketchwareBlock (return value block)
     * @param block The block
     */
    public SketchwareField(SketchwareBlock block) {
        this.block = block;
        is_block = true;
        init();
    }

    /**
     * This constructor initializes this class as a fixed value
     * @param value The value
     */
    public SketchwareField(String value) {
        this.value = value;
        is_block = false;
        init();
    }

    private void init() {
        text_paint.setColor(0xFF000000);
        text_paint.setAntiAlias(true);
        text_paint.setStyle(Paint.Style.FILL);
        text_paint.setTextSize(18f);

        rect_paint.setStyle(Paint.Style.FILL);
        rect_paint.setColor(0xFFFFFFFF);
        rect_paint.setAntiAlias(true);
    }

    public int getWidth() {
        // Padding for the text should only be about 5
        if (!is_block) {
            return (int) text_paint.measureText(value) + 5 * 2;
        } else {
            // TODO
        }
    }

    public void draw(Canvas canvas, int left, int top, int bottom) {
        if (!is_block) {
            // Draw the white background
            canvas.drawRect(left, top, left + getWidth(), bottom, rect_paint);

            // Draw the text / value
            canvas.drawText(value, left + 5, top, text_paint);
        } else {
            // TODO
        }
    }
}
