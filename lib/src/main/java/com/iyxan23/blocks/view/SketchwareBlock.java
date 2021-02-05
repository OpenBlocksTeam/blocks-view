package com.iyxan23.blocks.view;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.ArrayList;

public class SketchwareBlock {

    public String format;
    public String id;
    public ArrayList<SketchwareField> parameters;

    // Indicates if this block can't have a next_block, e.g. Finish Activity block
    public boolean is_bottom;

    int next_block;

    public int color;
    public int color_dark;

    int text_padding = 10;

    public SketchwareBlock(String format, String id, int next_block, ArrayList<SketchwareField> parameters, int color) {
        this.format = format;
        this.id = id;
        this.next_block = next_block;
        this.parameters = parameters;
        this.color = color;
        this.color_dark = Utilities.manipulateColor(color, 0.7f);

        // next_block is -1 if there is nothing after it
        this.is_bottom = next_block == -1;
    }

    /**
     * This function returns the approximate width of the block
     *
     * @param text_paint The text paint that is going to be used
     * @return The block's width
     */
    public int getWidth(Paint text_paint) {
        return text_padding + (int) text_paint.measureText(format) + text_padding;
    }

    /**
     * This function draws the block into the canvas specified at a given level to the bottom (blocks_down)
     *
     * @param canvas The canvas where it will be drawn into
     * @param rect_paint The paint for the rectangle
     * @param text_paint The paint for the text
     * @param blocks_down The block's position
     * @param previous_block_color The previous block's color, used to draw the outset of the block above
     */
    public void draw(Canvas canvas, Paint rect_paint, Paint text_paint, int blocks_down, int previous_block_color) {
        boolean is_overlapping = true;  // This variable should be controllable by the developer

        int block_height = 60;
        int event_offset = 50;

        int shadow_height = 10;
        int block_outset_height = 10;

        int block_left = 50;
        int block_width = (int) text_paint.measureText(format) + 20;

        int top_position;

        if (blocks_down == 1) {
            top_position = event_offset + block_height;

        } else {
            top_position = (blocks_down + 1) * block_height - (block_height - event_offset);

            // Set the offset for blocks below the first block
            top_position += (blocks_down - 1) * (block_height - event_offset);

            if (is_overlapping)
                // Overlap the previous block's shadow
                top_position -= (blocks_down - 1) * shadow_height;
        }

        int bottom_position = top_position + block_height;

        // Draw the block's shadow
        rect_paint.setColor(color_dark);
        canvas.drawRect(block_left, top_position, block_left + block_width, bottom_position + shadow_height, rect_paint);

        // This is the little bottom thing
        if (!is_bottom)
            canvas.drawRect(100, top_position, 175, bottom_position + shadow_height + block_outset_height, rect_paint);

        // Draw the actual block
        rect_paint.setColor(color);
        canvas.drawRect(block_left, top_position, block_left + block_width, bottom_position, rect_paint);

        // This is the little bottom thing
        if (!is_bottom)
            canvas.drawRect(100, top_position, 175, bottom_position + block_outset_height, rect_paint);

        // Draw the previous block's outset (only if we're overlapping it)
        if (is_overlapping) {
            rect_paint.setColor(previous_block_color);
            canvas.drawRect(100, top_position, 175, top_position + block_outset_height, rect_paint);
        } else {
            rect_paint.setColor(Utilities.manipulateColor(previous_block_color, 0.7f));
            canvas.drawRect(100, top_position, 175, top_position + block_outset_height, rect_paint);

            rect_paint.setColor(previous_block_color);
            canvas.drawRect(100, top_position, 175, top_position + block_outset_height - shadow_height, rect_paint);
        }

        // Draw the block's text
        // TODO: ADD A FORMATTER
        canvas.drawText(format, 60, top_position + 45, text_paint);
    }
}
