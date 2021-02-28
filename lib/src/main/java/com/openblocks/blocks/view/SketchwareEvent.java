package com.openblocks.blocks.view;

import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class SketchwareEvent {

    public String activity_name;
    public String name;

    public int color = 0xFFF39B0E;
    public int color_dark = 0xFFC8800E;

    public int text_padding = 10;

    public ArrayList<SketchwareBlock> blocks = new ArrayList<>();

    public SketchwareEvent(String activity_name, String name) {
        this.activity_name = activity_name;
        this.name = name;
    }

    /**
     * Draws the event to a canvas with these specific parameters
     *
     * @param canvas The canvas to be drawn on
     * @param height The event's height (The body, this doesn't include the bump at the top)
     * @param outset_height The outset (or the tiny block coming out from the bottom)'s height
     * @param left_position The left position or Y
     * @param top_position The top position or X
     * @param top_bump_height The top bump's height
     * @param shadow_height The shadow's height
     * @param rect_paint The paint that is used to draw the rect
     * @param text_paint The paint that is used to draw the text
     */
    public void draw(Canvas canvas, int height, int outset_height, int left_position, int top_position, int top_bump_height, int shadow_height, Paint rect_paint, Paint text_paint) {

//        int height = 50;
//        int outset_height = 10;

        String text = activity_name + ": " + name;
        int text_width = (int) text_paint.measureText(text) + text_padding * 2;

        // Draw the "shadow"
        rect_paint.setColor(color_dark);
        canvas.drawRect(left_position, top_position, text_width + left_position, top_position + height + shadow_height, rect_paint);

        // The outset part
        canvas.drawRect(left_position + 50, top_position, 175, top_position + height + shadow_height + outset_height, rect_paint);


        // Draw the actual block
        rect_paint.setColor(color);
        canvas.drawRect(left_position, top_position, text_width + left_position, top_position + height, rect_paint);

        // top bump
        canvas.drawRect(left_position, top_position - top_bump_height, left_position + 250, top_position + height, rect_paint);

        // outset
        canvas.drawRect(left_position + 50, top_position, 175, top_position + height + outset_height, rect_paint);


        // Draw the text
        canvas.drawText(text, 60, top_bump_height + top_position + (height) / 2, text_paint);
    }

    @NonNull
    @Override
    public String toString() {
        return "Sketchware Event:\n\tActivityName:" + activity_name + "\n\tEventName: " + name + "\nBlocks:\n" + blocks.toString();
    }

    @NonNull
    @Override
    protected Object clone() {
        SketchwareEvent clone_event = new SketchwareEvent(this.activity_name, this.name);
        clone_event.blocks = (ArrayList<SketchwareBlock>) blocks.clone();
        return clone_event;
    }
}
