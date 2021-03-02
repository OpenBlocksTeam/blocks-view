package com.openblocks.blocks.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.annotation.NonNull;

public class SketchwareField {

    /**
     * This type enum is used to identify what this field really is
     */
    public enum Type {
        STRING,     // Will look like block / rectangle, something like this [ ]
        INTEGER,    // Will look like an ellipse / rounded rectangle, something like this ( )
        BOOLEAN,    // Will look something like this < >
        OTHER       // Some kind of subset of other objects, ex: ArrayList, where you can put the same subset, but not with other subset, will look the same as string
    }

    public boolean is_block; // This boolean indicates if this is a block or not
    public String value = "";  // This value is going to be used if is_block is false

    public SketchwareBlock block;
    public Type type;

    Paint text_paint = new Paint();
    Paint rect_paint = new Paint();

    // This is the padding around the field
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
        this.type = Type.STRING;
        is_block = false;
        init();
    }

    /**
     * This constructor initializes this class as a fixed value
     * @param value The value
     * @param type The type of this field
     */
    public SketchwareField(String value, Type type) {
        this.value = value;
        this.type = type;
        is_block = false;
        init();
    }


    /**
     * This function initializes paints, just like initialize() on SketchwareBlocksView
     */
    private void init() {
        text_paint.setColor(0xFF000000);
        text_paint.setAntiAlias(true);
        text_paint.setStyle(Paint.Style.FILL);
        text_paint.setTextSize(20f);

        rect_paint.setStyle(Paint.Style.FILL);
        rect_paint.setColor(0xFFFFFFFF);
        rect_paint.setAntiAlias(true);
    }

    /**
     * This function returns the width of this field.
     * @param block_text_paint The paint used for the block, our field paint is different, this is just used for "block fields", if you 100% thinks that this isn't a block field, set this to null
     * @return The width of this field
     */
    public int getWidth(Paint block_text_paint) {
        // Padding for the text should only be about 5
        if (!is_block) {
            return (int) text_paint.measureText(value) + padding * 2;
        } else {
            return block.getWidth(block_text_paint);
        }
    }

    /**
     * This function returns the height of this field.
     * @param block_text_paint The paint used for the block, our field paint is different, this is just used for "block fields", if you 100% thinks that this isn't a block field, set this to null
     * @return The height of this field
     */
    public int getHeight(Paint block_text_paint) {
        if (!is_block) {
            Paint.FontMetrics fm = text_paint.getFontMetrics();
            float height = fm.descent - fm.ascent;

            return (int) height + padding * 2;
        } else {
            return block.getHeight(block_text_paint);
        }
    }


    /**
     * This function draws the field at the specified location (We won't add any paddings to it)
     * @param context The context
     * @param canvas The canvas
     * @param left The left position
     * @param top The top position
     * @param block_text_paint The paint used for blocks, if you're sure that this isn't a block field, set this to null
     * @param parent_block_height The parent's block height, NOT THE BLOCK ON TOP OF THE FIELD
     */
    public void draw(Context context, Canvas canvas, int left, int top, Paint block_text_paint, int parent_block_height) {
        if (!is_block) {
            int bottom_background = top + parent_block_height;

            switch (type) {
                case STRING:
                    // Draw the white background
                    canvas.drawRect(left, top, left + getWidth(block_text_paint), bottom_background, rect_paint);

                    // Draw the text / value
                    canvas.drawText(value, left + padding, top - ((top - bottom_background) / 2) + padding, text_paint);
                    break;

                case INTEGER:
                    // FIXME: UNTESTED
                    int radius = parent_block_height / 2;
                    int middle = top + radius;

                    // Draw the oval-ly background
                    // Draw the left circle
                    canvas.drawCircle(padding + left, middle, top - bottom_background, rect_paint);

                    // Draw the right circle
                    canvas.drawCircle(padding + left + getWidth(block_text_paint), middle, radius, rect_paint);

                    // Draw a rectangle
                    canvas.drawRect(left, top, left + getWidth(block_text_paint), bottom_background, rect_paint);

                    // Draw the text / value
                    canvas.drawText(value, left + padding, top - ((top - bottom_background) / 2) + padding, text_paint);

                    break;

                case BOOLEAN:
                    break;

                case OTHER:
                    break;
            }
        } else {
            // Well, draw the block as the parameter, I guess
            block.draw(context, canvas, rect_paint, block_text_paint, top, left, 0, 0, 0, padding, false, 0x00000000);
            //                                                                                                                        ^
                                                                                       /* we're setting the outset_height to add a padding to the text, this shouldn't be a thing TODO */
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "SketchwareField{" +
                "is_block=" + is_block +
                ", value='" + value + '\'' +
                ", block=" + block +
                ", text_paint=" + text_paint +
                ", rect_paint=" + rect_paint +
                ", padding=" + padding +
                '}';
    }
}
