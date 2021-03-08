package com.openblocks.blocks.view;

import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.annotation.NonNull;

import java.util.ArrayList;

/**
 * This class is used to represent a collection of blocks and an event / orange-yellow thingy at the top
 */
public class SketchwareEvent {

    public String activity_name;
    public String name;

    public int color = 0xFFF39B0E;

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
    public void draw(Canvas canvas, int height, int outset_height, int left_position, int top_position, int top_bump_height, int outset_left_margin, int outset_width, int shadow_height, Paint rect_paint, Paint text_paint) {

//        int height = 50;
//        int outset_height = 10;

        String text = activity_name + ": " + name;
        int text_width = (int) text_paint.measureText(text) + text_padding * 2;

        // Draw the block's body
        DrawHelper.drawRectSimpleOutsideShadow(canvas, left_position, top_position, text_width + text_padding, height + text_padding, shadow_height, color);

        // top bump, don't draw shadow
        DrawHelper.drawRect(canvas, left_position, top_position - top_bump_height, 250, height, color);

        // outset
        DrawHelper.drawRectSimpleOutsideShadow(canvas, left_position + outset_left_margin, top_position, outset_width, height + outset_height + outset_height, shadow_height, color);


        // Draw the text
        canvas.drawText(text, left_position + text_padding, top_bump_height + top_position + (height >> 1), text_paint);
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
