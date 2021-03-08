package com.openblocks.blocks.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import androidx.annotation.NonNull;

public class SketchwareField {

    /**
     * This type enum is used to identify what this field's type is
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
    int text_padding = 10;

    // An extra padding for fields with the integer type
    int integer_padding = 10;

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
            // If it's boolean, we don't need the padding, just measure half of the parent's height
            int estimate_width = (int) text_paint.measureText(value) + (type == Type.BOOLEAN ? getHeight(block_text_paint) / 2 : text_padding) * 2;

            // Minimal width for Integer fields are 64
            if (type == Type.INTEGER && estimate_width <= 64) {
                return 64;
            }

            return estimate_width;
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

            return (int) height + text_padding * 2;
        } else {
            return block.getHeight(block_text_paint);
        }
    }


    /**
     * This function draws the field at the specified location (We won't add any paddings to it, jokes on you, the left parameter is added with padding)
     * @param context The context
     * @param canvas The canvas
     * @param left The left position
     * @param top The top position
     * @param block_text_paint The paint used for blocks, if you're sure that this isn't a block field, set this to null
     * @param parent_block_height The parent's block height, NOT THE BLOCK ON TOP OF THE FIELD
     */
    public void draw(Context context, Canvas canvas, int left, int top, Paint block_text_paint, int parent_block_height) {
        // NOTE: top has been applied with the padding, you don't need to add padding yourself

        if (!is_block) {
            int bottom = top + parent_block_height;
            int half_of_parent_height = parent_block_height / 2;
            int middle = top + half_of_parent_height;

            int width = getWidth(block_text_paint);

            switch (type) {
                case STRING:
                    // Draw the white background
                    canvas.drawRect(left, top, left + width, bottom, rect_paint);

                    // Draw the text / value
                    canvas.drawText(value, left + text_padding, top - ((top - bottom) / 2) + text_padding, text_paint);
                    break;

                case INTEGER:
                    DrawHelper.drawIntegerField(canvas, left, top, getWidth(block_text_paint), getHeight(block_text_paint), rect_paint);

                    // Draw the text / value
                    canvas.drawText(value, left + text_padding, middle + text_padding, text_paint);

                    break;

                case BOOLEAN:
                    DrawHelper.drawBooleanField(canvas, left, top, getWidth(block_text_paint), getHeight(block_text_paint), rect_paint);
                    canvas.drawText(value, left + half_of_parent_height, middle + text_padding, text_paint);

                    break;

                case OTHER:
                    break;
            }
        } else {
            // Well, draw the block as the parameter, I guess
            block.draw(context, canvas, rect_paint, block_text_paint, top, left, 0, 0, 0, text_padding, false, 0x00000000);
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
                ", padding=" + text_padding +
                '}';
    }
}
