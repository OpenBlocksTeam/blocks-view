package com.openblocks.blocks.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

public class SketchwareField {

    // This boolean indicates if this is a block or not
    public boolean is_block;
    public String value = "";  // This value is going to be used if is_block is false

    public SketchwareBlock block;

    Paint text_paint = new Paint();
    Paint rect_paint = new Paint();

    int padding = 10;

    /**
     * This will initialize this class as a SketchwareBlock (return value block)
     * @param block The block
     */
    public SketchwareField(SketchwareBlock block) {
        this.block = block;
        this.block.is_parameter = true;
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
        text_paint.setTextSize(20f);

        rect_paint.setStyle(Paint.Style.FILL);
        rect_paint.setColor(0xFFFFFFFF);
        rect_paint.setAntiAlias(true);
    }

    public int getWidth(Paint block_text_paint) {
        // Padding for the text should only be about 5
        if (!is_block) {
            return (int) text_paint.measureText(value) + padding * 2;
        } else {
            return block.getWidth(block_text_paint);
        }
    }

    public int getHeight(Paint block_text_paint) {
        if (!is_block) {
            Paint.FontMetrics fm = text_paint.getFontMetrics();
            float height = fm.descent - fm.ascent;

            return (int) height + padding * 2;
        } else {
            return block.getHeight(block_text_paint);
        }
    }

    public void draw(Context context, Canvas canvas, int left, int top, Paint block_text_paint, int parent_block_height) {
        if (!is_block) {
            int bottom_background = top + parent_block_height;

            // Draw the white background
            canvas.drawRect(left, top, left + getWidth(block_text_paint), bottom_background, rect_paint);

            // Draw the text / value
            canvas.drawText(value, left + padding, top - ((top - bottom_background) / 2) + padding, text_paint);
        } else {
            // Well, draw the block as the parameter, I guess
            block.draw(context, canvas, rect_paint, block_text_paint, top, left, 0, 0, 0, padding, false, 0x00000000);
            //                                                                                                                        ^
                                                                                       /* we're setting the outset_height to add a padding to the text, this shouldn't be a thing TODO */
        }
    }
}
