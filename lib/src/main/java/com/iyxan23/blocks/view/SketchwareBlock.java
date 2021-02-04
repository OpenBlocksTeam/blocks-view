package com.iyxan23.blocks.view;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;

public class SketchwareBlock {

    public String format;
    public String id;
    public ArrayList<SketchwareField> parameters;

    int next_block;

    public int color;
    public int color_dark;

    public SketchwareBlock(String format, String id, int next_block, ArrayList<SketchwareField> parameters, int color) {
        this.format = format;
        this.id = id;
        this.next_block = next_block;
        this.parameters = parameters;
        this.color = color;
        this.color_dark = Utilities.manipulateColor(color, 0.7f);

    public void draw(Canvas canvas, Paint rect_paint, Paint text_paint, int blocks_down) {
        int block_height = 60;
        int event_offset = 50;

        int top_position;

        if (blocks_down == 1) {
            top_position = event_offset + block_height;
        } else {
            top_position = (blocks_down + 1) * block_height - (block_height - event_offset);

            // Set the offset for blocks below the first block
            top_position += (blocks_down - 1) * 10;
        }

        int bottom_position = top_position + block_height;

        int shadow_height = 10;
        int block_outset_height = 15;

        // Draw the block's shadow
        rect_paint.setColor(color_dark);
        canvas.drawRect(50, top_position, 400, bottom_position + shadow_height, rect_paint);
        
        canvas.drawRect(100, top_position, 175, bottom_position + shadow_height + block_outset_height, rect_paint);

        // Draw the actual block
        rect_paint.setColor(color);
        canvas.drawRect(50, top_position, 400, bottom_position, rect_paint);


        canvas.drawRect(100, top_position, 175, bottom_position + block_outset_height, rect_paint);

        // Draw the block's text
        // TODO: ADD A FORMATTER
        canvas.drawText(format, 60, top_position + 45, text_paint);
    }
}
