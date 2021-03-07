package com.openblocks.blocks.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is a model used to represent a block
 */
public class SketchwareBlock {

    // Variables ===================================================================================
    private String format;
    private ArrayList<Object[]> parsed_format;
    public String id;
    public ArrayList<SketchwareField> parameters;

    // Indicates if this block can't have a next_block, e.g. Finish Activity block
    public boolean is_bottom;

    int next_block;

    public int color;
    public int color_dark;

    int text_padding = 10;

    // Indicates if this block is a parameter or not
    boolean is_parameter;

    // Will be used in the overloaded draw function
    public int default_height = 60; // The same as in SketchwareBlocksView
    // Variables ===================================================================================



    // Constructors ================================================================================
    /**
     * Constructs a simple SketchwareBlocks with just a text and a color
     *
     * @param text The text of the block
     * @param color The color of the block
     */
    public SketchwareBlock(String text, int color) {
        this(text, "1", 2, color);
    }

    public SketchwareBlock(String text, String id, int next_block, int color) {
        this(text, id, next_block, new ArrayList<>(), color, false);
    }

    public SketchwareBlock(String format, ArrayList<SketchwareField> parameters, int color) {
        this(format, "1", 2, parameters, color);
    }

    public SketchwareBlock(String format, String id, int next_block, ArrayList<SketchwareField> parameters, int color) {
        this(format, id, next_block, parameters, color, false);
    }

    public SketchwareBlock(String format, String id, int next_block, ArrayList<SketchwareField> parameters, int color, boolean is_parameter) {
        this.id = id;
        this.next_block = next_block;
        this.parameters = parameters;
        this.color = color;
        this.color_dark = DrawHelper.manipulateColor(color, 0.7f);
        this.is_parameter = is_parameter;
        this.setFormat(format);

        // next_block is -1 if there is nothing after it
        this.is_bottom = next_block == -1;
    }
    // Constructors ================================================================================



    // Getters and Setters =========================================================================
    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
        parsed_format = parseFormat();
    }
    // Getters and Setters =========================================================================



    // Essential functions =========================================================================
    /**
     * This function parses the format
     *
     * @return Returns ArrayList of [start_pos, end_pos, name, SketchwareField]
     */
    public ArrayList<Object[]> parseFormat() {
        ArrayList<Object[]> tmp = new ArrayList<>();

        Pattern pattern = Pattern.compile("%[a-z]\\.(\\w+)|%[a-z]");
        Matcher matcher = pattern.matcher(getFormat());

        int index = 0;
        while (matcher.find()) {
            if (parameters.size() <= index)
                //throw new IllegalStateException("Parameters have less elements than the format");
                continue;

            tmp.add(new Object[] {
                    matcher.start(),
                    matcher.end(),
                    matcher.groupCount() == 1 ? matcher.group(0).substring(1) : "",  // Skip the first char because we want to skip the .
                    parameters.get(index)
            });
            index++;
        }

        return tmp;
    }

    /**
     * This function returns the approximate width of the block
     *
     * @param text_paint The text paint that is going to be used
     * @return The block's width
     */
    public int getWidth(Paint text_paint) {
        if (parameters.size() == 0)
            return text_padding + (int) text_paint.measureText(getFormat()) + text_padding;

        // Remove these
        ArrayList<Object[]> params = parseFormat();
        StringBuilder final_string = new StringBuilder();

        // The parameters widths (will be added with the measure text)
        int params_widths = 0;

        int last_num = 0;
        for (Object[] param: params) {
            final_string.append(getFormat().substring(last_num, (int) param[0]));
            last_num = (int) param[1];

            SketchwareField field = (SketchwareField) param[3];

            params_widths +=
                            field.getWidth(text_paint) +
                            text_padding;  // The padding between the text and the field
        }

        // Add the last string at the end
        final_string.append(getFormat().substring(last_num));

        return text_padding + (int) text_paint.measureText(final_string.toString()) + text_padding + params_widths;
    }

    public int getHeight(Paint text_paint) {
        // Return the default height if this is a parameter and it has no other parameters
        if (is_parameter && parameters.size() == 0)
            return default_height;

        // If there aren't any parameters, and this isn't a parameter block, this means that this
        // block is just a freestanding block, nothing special in it, get text height and add 2 text_padding.
        if (parameters.size() == 0)
            return Math.max(default_height, (int) text_paint.getTextSize() + text_padding * 2);

        // Let's calculate the height
        // Quite easy, just loop per every parameters and get the maximum height
        int max_height = 0;
        for (SketchwareField parameter : parameters) {
            if (parameter.is_block) {
                // This is a block!
                // We can just call the getHeight of that block recursively
                max_height = Math.max(parameter.block.getHeight(text_paint), max_height);
            } else {
                max_height = Math.max(parameter.getHeight(text_paint), max_height);
            }
        }

        return Math.max(default_height, max_height + text_padding * 2); // 2 paddings because there will be padding on the top and the bottom
    }

    /**
     * This function is called when a pickup is happened at the location somewhere in this block
     * @param x The x location of the pickup, should be relative to OUR block's 0, 0 point (top left)
     * @param y The y location of the pickup, should be relative to OUR block's 0, 0 point (top left)
     * @param text_paint The {@link Paint} used to draw the block text
     * @return A pair of the pickup action, and a parameter element index. If we returned PICKUP_PARAMETER, the second pair will be used as an index of our {@link #parameters} attribute.
     */
    public Pair<SketchwareBlocksView.PickupAction, Integer> onPickup(int x, int y, Paint text_paint) {
        // int x = left + text_padding;  // The initial x's text position

        // int text_top = top + ((getHeight(text_paint) + shadow_height + block_outset_height + text_padding) / 2);

        int x_total = 0;
        int x_before;

        // This index indicates the position of the parameter (in variable parameters) we're in the loop
        int index = 0;

        // We're using a similar method of drawParameters
        // Loop per each parameters
        int last_substring_index = 0;
        for (Object[] param: parsed_format) {
            // Ik this looks stupid, but if i just pass in x_total, the x_before 's reference will attach to x_total's reference
            // if x_total changed, x_before will change (which is the thing i don't want)
            x_before = Integer.parseInt(String.valueOf(x_total));

            // Get the text between a field (should be 0 for the first time) and another field
            String text = getFormat().substring(last_substring_index, (int) param[0]);
            // canvas.drawText(text, x, block_text_location, text_paint);

            // Check if the X is somewhere in this text
            if (x > x_before && x < x_total) {
                // Oop, then just pick ourself, i guess
                return new Pair<>(SketchwareBlocksView.PickupAction.PICKUP_SELF, -1); // -1 because the user didn't picked up any parameter
            }

            x_total += text_paint.measureText(text) + 5;

            // Update the x_before to the text
            x_before = Integer.parseInt(String.valueOf(x_total));

            last_substring_index = (int) param[1];

            SketchwareField field = (SketchwareField) param[3];

            // Check if X is somewhere in this field
            if (x > x_before && x < x_total) {
                // Yup, this the user is dragging a field, check if this is a block
                if (field.is_block) {
                    // Ohk this is a block, check if the parameter block has a parameter too
                    if (field.block.parameters.size() == 0) {
                        // Nop it doesn't have any, means we can pick this parameter!
                        // FIXME: index can be different for different blocks
                        return new Pair<>(SketchwareBlocksView.PickupAction.PICKUP_PARAMETER, index);
                    } else {
                        // This block has a parameter, recursively call onPickup!
                        // oh yeah don't forget to offset the x
                        field.block.onPickup(x + x_before, y, text_paint);
                    }
                } else {
                    // Oop, this is just a value parameter, just pick ourself, i guess
                    return new Pair<>(SketchwareBlocksView.PickupAction.PICKUP_SELF, -1); // -1 because the user didn't picked up any parameter
                }
            }

            x_total += field.getWidth(text_paint) + 5;
        }

        // Wat, nothing?
        // This shouldn't happen but meh, let's just pick ourself
        return new Pair<>(SketchwareBlocksView.PickupAction.PICKUP_SELF, -1);
    }
    // Essential functions =========================================================================



    // Draw functions ==============================================================================
    /**
     * This function is an overloaded function of draw, but without height
     * the height is get by using the global variable (can be set manually)
     *
     * This function will be used to draw the lowest child inside every parameters
     *
     * @param canvas The canvas where it will be drawn into
     * @param rect_paint The paint for the rectangle
     * @param text_paint The paint for the text
     * @param top The y position of the block
     * @param left The x position of the block
     * @param shadow_height The shadow height of the block
     * @param block_outset_left_margin The outset block's left margin (RTL)
     * @param block_outset_width The outset block's width
     * @param block_outset_height The outset block's height
     * @param is_overlapping Do you want to overlap the block above's shadow?
     * @param previous_block_color The previous block's color, used to draw the outset of the block above
     */
    public void draw(Context context, Canvas canvas, Paint rect_paint, Paint text_paint, int top, int left, int shadow_height, int block_outset_left_margin, int block_outset_width, int block_outset_height, boolean is_overlapping, int previous_block_color) {
        draw(context, canvas, rect_paint, text_paint, top, left, getHeight(text_paint), shadow_height, block_outset_left_margin, block_outset_width, block_outset_height, is_overlapping, previous_block_color);
    }

    // TODO: Maybe at least reduce the parameters
    /**
     * This function draws the block into the canvas specified at a given level to the bottom (blocks_down)
     *
     * @param context The context
     * @param canvas The canvas where it will be drawn into
     * @param rect_paint The paint for the rectangle
     * @param text_paint The paint for the text
     * @param top The y position of the block
     * @param left The x position of the block
     * @param height The height of the block
     * @param shadow_height The shadow height of the block
     * @param block_outset_left_margin The outset block's left margin (RTL)
     * @param block_outset_width The outset block's width
     * @param block_outset_height The outset block's height
     * @param is_overlapping Do you want to overlap the block above's shadow?
     * @param previous_block_color The previous block's color, used to draw the outset of the block above
     */
    public void draw(Context context, Canvas canvas, Paint rect_paint, Paint text_paint, int top, int left, int height, int shadow_height, int block_outset_left_margin, int block_outset_width, int block_outset_height, boolean is_overlapping, int previous_block_color) {
        // int block_width = (int) text_paint.measureText(format) + 20;
        int block_width = getWidth(text_paint);

        int bottom_position = top + height;

        int block_outset_left = left + block_outset_left_margin;

        // Draw the block's shadow
        rect_paint.setColor(color_dark);
        canvas.drawRect(left, top, left + block_width, bottom_position + shadow_height, rect_paint);

        // This is the little bottom thing
        if (!is_bottom)
            canvas.drawRect(block_outset_left, top, block_outset_left + block_outset_width, bottom_position + shadow_height + block_outset_height, rect_paint);

        // Draw the actual block
        rect_paint.setColor(color);
        canvas.drawRect(left, top, left + block_width, bottom_position, rect_paint);

        // This is the little bottom thing
        if (!is_bottom)
            canvas.drawRect(block_outset_left, top, block_outset_left + block_outset_width, bottom_position + block_outset_height, rect_paint);

        // Draw the previous block's outset (only if we're overlapping it)
        if (is_overlapping) {
            rect_paint.setColor(previous_block_color);
            canvas.drawRect(block_outset_left, top, block_outset_left + block_outset_width, top + block_outset_height, rect_paint);
        } else {
            rect_paint.setColor(DrawHelper.manipulateColor(previous_block_color, 0.7f));
            canvas.drawRect(block_outset_left, top, block_outset_left + block_outset_width, top + block_outset_height, rect_paint);

            rect_paint.setColor(previous_block_color);
            canvas.drawRect(block_outset_left, top, block_outset_left + block_outset_width, top + block_outset_height - shadow_height, rect_paint);
        }

        // Draw the block's text and parameters
        drawParameters(context, canvas, left, top, top + ((getHeight(text_paint) + shadow_height + block_outset_height + text_padding) / 2), height, shadow_height, text_paint);
    }

    public final void drawParameters(Context context, Canvas canvas, int left, int top, int block_text_location, int height, int shadow_height, Paint text_paint) {
        // Draw the parameters
        int x = left + text_padding;  // The initial x's text position

        // int text_top = top + ((getHeight(text_paint) + shadow_height + block_outset_height + text_padding) / 2);

        int last_num = 0;
        for (Object[] param: parsed_format) {
            String text = getFormat().substring(last_num, (int) param[0]);
            canvas.drawText(text, x, block_text_location, text_paint);

            x += text_paint.measureText(text) + 5;

            last_num = (int) param[1];

            SketchwareField field = (SketchwareField) param[3];

            if (shadow_height == 0)
                shadow_height = text_padding;

            field.draw(context,canvas, x, top + text_padding, text_paint, height - text_padding - shadow_height);

            x += field.getWidth(text_paint) + 5;
        }

        String text = getFormat().substring(last_num);
        canvas.drawText(text, x, block_text_location, text_paint);
    }
    // Draw functions ==============================================================================



    @NonNull
    @Override
    public String toString() {
        return "SketchwareBlock{" +
                "format='" + getFormat() + '\'' +
                ", id='" + id + '\'' +
                ", parameters=" + parameters +
                ", is_bottom=" + is_bottom +
                ", next_block=" + next_block +
                ", color=" + color +
                ", color_dark=" + color_dark +
                ", text_padding=" + text_padding +
                ", is_parameter=" + is_parameter +
                ", default_height=" + default_height +
                '}';
    }
}
