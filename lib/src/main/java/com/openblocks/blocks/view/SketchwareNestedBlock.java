package com.openblocks.blocks.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;

/**
 * This class is used to represent a nested block, where it can contain a collection of blocks inside itself
 */
public class SketchwareNestedBlock extends SketchwareBlock {

    public ArrayList<SketchwareBlock> blocks;

    public int block_bottom_height = 40;
    public int indent_width = 40;

    public int bottom_margin = 20;

    public SketchwareNestedBlock(String format, String id, int next_block, ArrayList<SketchwareField> parameters, int color, ArrayList<SketchwareBlock> blocks_inside) {
        super(format, id, next_block, parameters, color);
        blocks = blocks_inside;
    }

    @Override
    public int getWidth(Paint text_paint) {
        return super.getWidth(text_paint);
    }

    /* Difference between getHeight and getBlockHeight is that:
     *
     * getHeight: Get the entire blocks' height + the block itself height
     * getBlockHeight: Get just the block's height
     */

    @Override
    public int getHeight(Paint text_paint) {
        return calculateBlockHeights(text_paint) + getBlockHeight(text_paint) + block_bottom_height + bottom_margin;
    }

    public int getBlockHeight(Paint text_paint) {
        return super.getHeight(text_paint);
    }

    private int calculateBlockHeights(Paint block_text_paint) {
        int sum = 0;

        for (SketchwareBlock block : blocks) {
            sum += block.getHeight(block_text_paint);
        }

        return sum;
    }

    @Override
    public Pair<SketchwareBlocksView.PickupAction, SketchwareBlock> onPickup(int x, int y, Paint text_paint) {
        int y_start = getBlockHeight(text_paint);

        int index = 0;
        for (SketchwareBlock block : blocks) {

            // Check if this pickup has a block under it
            if (block.getBounds(0, y_start, text_paint).contains(x - indent_width, y)) {
                // Ohk, remove and return the block
                blocks.remove(index);
                return new Pair<>(SketchwareBlocksView.PickupAction.PICKUP_OTHER_BLOCK, block);
            }

            y_start += block.getHeight(text_paint);
            index++;
        }

        // Ight, nothing, return our self instead
        // TODO: Check each blocks so if the user picked up the white part inside our bounds we don't pickup
        return new Pair<>(SketchwareBlocksView.PickupAction.PICKUP_SELF, null);
    }

    @Override
    public void draw(Context context, Canvas canvas, Paint rect_paint, Paint text_paint, int top, int left, int height, int shadow_height, int block_outset_left_margin, int block_outset_width, int block_outset_height, boolean is_overlapping, int previous_block_color) {
        Paint original_block_paint = new Paint();
        original_block_paint.setColor(rect_paint.getColor());
        original_block_paint.setTextSize(rect_paint.getTextSize());

        super.draw(context, canvas, rect_paint, text_paint, top, left, getBlockHeight(text_paint), shadow_height, block_outset_left_margin + indent_width, block_outset_left_margin, block_outset_width, block_outset_height, is_overlapping, previous_block_color);

        int block_outset_left = left + block_outset_left_margin;

        // Draw the childes! (similar to SketchwareBlocksView)
        previous_block_color = color;
        int block_height = getBlockHeight(text_paint);
        int previous_top_position = top + shadow_height;
        int previous_block_height = block_height - shadow_height;  // Because if not, the first block would get overlapped by the event

        for (int i = 0; i < blocks.size(); i++) {

            SketchwareBlock current_block = blocks.get(i);
            current_block.default_height = block_height;

            int top_position;

            top_position = previous_top_position + previous_block_height + shadow_height;

            if (is_overlapping) {
                // Overlap the previous block's shadow
                top_position -= shadow_height;
            }

            previous_top_position = top_position;

            previous_block_height = current_block.getHeight(text_paint);

            current_block
                    .draw(
                            context,
                            canvas,
                            rect_paint,
                            text_paint,
                            top_position,
                            left + indent_width,
                            shadow_height,
                            block_outset_left_margin,
                            block_outset_width,
                            block_outset_height,
                            is_overlapping,
                            previous_block_color
                    );

            previous_block_color = current_block.color;
        }

        Paint extensions_paint = new Paint(rect_paint);

        int bottom_block_top_position = top + getHeight(text_paint) + shadow_height - block_bottom_height;
        int bottom_block_bottom_position = top + getHeight(text_paint);

        // draw the bottom part's shadow
        extensions_paint.setColor(DrawHelper.manipulateColor(this.color, 0.7f));
        canvas.drawRect(left, bottom_block_top_position, left + getWidth(text_paint), bottom_block_bottom_position + shadow_height, extensions_paint);

        extensions_paint.setColor(this.color);
        // Then draw the bottom part
        canvas.drawRect(left, bottom_block_top_position, left + getWidth(text_paint), bottom_block_bottom_position, extensions_paint);

        // Don't forget the outset
        if (is_overlapping) {
            canvas.drawRect(block_outset_left, bottom_block_top_position, block_outset_left + block_outset_width, bottom_block_bottom_position + block_outset_height, extensions_paint);
        } else {
            canvas.drawRect(block_outset_left, bottom_block_top_position, block_outset_left + block_outset_width, bottom_block_bottom_position + block_outset_height, extensions_paint);
            canvas.drawRect(block_outset_left, bottom_block_top_position, block_outset_left + block_outset_width, bottom_block_bottom_position + block_outset_height - shadow_height, extensions_paint);
        }

        // Ok, draw the "indent"
        canvas.drawRect(left, top + block_height, left + indent_width, bottom_block_bottom_position, extensions_paint);

        drawParameters(context,canvas, left, top, top + ((getBlockHeight(text_paint) + shadow_height + block_outset_height + text_padding) / 2), getBlockHeight(text_paint), shadow_height, text_paint);
    }
}
