package com.iyxan23.blocks.view;

import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;

public class SketchwareEvent {

    public String activity_name;
    public String name;

    public int color = 0xFFF39B0E;
    public int color_dark = 0xFFC8800E;

    public ArrayList<SketchwareBlock> blocks = new ArrayList<>();

    public SketchwareEvent(String activity_name, String name) {
        this.activity_name = activity_name;
        this.name = name;
    }

    public void draw(Canvas canvas, Paint rect_paint, Paint text_paint) {
        // Draw the "shadow"
        rect_paint.setColor(color_dark);
        canvas.drawRect(50, 50, 400, 110, rect_paint);
        canvas.drawRect(100, 50, 175, 120, rect_paint);

        // Draw the actual block
        rect_paint.setColor(color);
        canvas.drawRect(50, 50, 400, 100, rect_paint);
        canvas.drawRect(50, 35, 300, 100, rect_paint);
        canvas.drawRect(100, 50, 175, 110, rect_paint);

        // Draw the text
        canvas.drawText(activity_name + ": " + name, 60, 85, text_paint);
    }

    @NonNull
    @Override
    public String toString() {
        return "Sketchware Event:\n\tActivityName:" + activity_name + "\n\tEventName: " + name + "\nBlocks:\n" + blocks.toString();
    }
}
