package com.openblocks.blocks.view.styles;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.NinePatchDrawable;

import androidx.core.content.res.ResourcesCompat;

import com.openblocks.blocks.view.R;
import com.openblocks.blocks.view.SketchwareBlock;

/**
 * This class is used to draw a good old sketchware block instead of the current default block design
 */
public class SketchwareBlockOriginalStyle extends SketchwareBlock {
    public SketchwareBlockOriginalStyle(String text, int color) {
        super(text, color);
    }

    @Override
    public void draw(Context context, Canvas canvas, Paint rect_paint, Paint text_paint, int top, int left, int height, int shadow_height, int block_outset_left_margin, int top_block_outset_left_margin, int block_outset_width, int block_outset_height, boolean is_overlapping, int previous_block_color, boolean is_round, int round_radius) {
        // super.draw(context, canvas, rect_paint, text_paint, top, left, height, shadow_height, block_outset_left_margin, top_block_outset_left_margin, block_outset_width, block_outset_height, is_overlapping, previous_block_color, is_round, round_radius);

        NinePatchDrawable sketchwareBlock = (NinePatchDrawable) ResourcesCompat.getDrawable(context.getResources(), R.drawable.sketchware_block, context.getTheme());
        if (sketchwareBlock != null) {
            sketchwareBlock.setBounds(left, top, getWidth(text_paint), getHeight(text_paint));
            sketchwareBlock.draw(canvas);
        }

        drawParameters(context, canvas, left, top, top + ((getHeight(text_paint) + shadow_height + block_outset_height + 10) / 2), height, shadow_height, text_paint);
    }
}
