package com.openblocks.blocks.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is a model used to represent a block
 */
public class SketchwareBlock {

    private static final String TAG = "SketchwareBlock";

    // TODO: 3/8/21 REMOVE next_block AND id

    // Variables ===================================================================================
    /**
     * This variable format is formatted in sketchware's way, example:
     * <code>
     *   Toast %s
     * </code>
     * the % indicates that this is a parameter, s means that it's a string parameter type
     */
    private String format;
    private ArrayList<Object[]> parsed_format;
    public String id;
    public ArrayList<SketchwareField> parameters;

    // Indicates if this block can't have a next_block, e.g. Finish Activity block
    public boolean is_bottom;

    int next_block;

    public int color;

    int text_padding = 10;

    // Indicates if this block returns a value or not
    boolean is_return_block;

    // Indicates this block's return type (if is_return_block is true)
    SketchwareField.Type return_type;

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
        this(text, id, next_block, new ArrayList<>(), color, false, null);
    }

    public SketchwareBlock(String format, ArrayList<SketchwareField> parameters, int color) {
        this(format, "1", 2, parameters, color);
    }

    public SketchwareBlock(String format, String id, int next_block, ArrayList<SketchwareField> parameters, int color) {
        this(format, id, next_block, parameters, color, false, null);
    }

    public SketchwareBlock(String format, String id, int next_block, ArrayList<SketchwareField> parameters, int color, boolean is_parameter, SketchwareField.Type parameter_type) {
        this.id = id;
        this.next_block = next_block;
        this.parameters = parameters;
        this.color = color;
        this.is_return_block = is_parameter;
        this.return_type = parameter_type;
        this.setFormat(format);

        // next_block is -1 if there is nothing after it
        this.is_bottom = next_block == -1;
    }
    // Constructors ================================================================================



    // Factory constructors ========================================================================

    /**
     * Create a simple block
     * @param text The text of the block
     * @param color The color of the block
     * @return The block according to the parameters given
     */
    public static SketchwareBlock newSimpleBlock(String text, int color) {
        return new SketchwareBlock(text, color);
    }

    /**
     * Create a simple block with parameters in it
     * @param text The text of the block
     * @param color The color of the block
     * @param parameters The parameters of the block
     * @return The block according to the parameters given
     */
    public static SketchwareBlock newBlockWithParameters(String text, int color, SketchwareField... parameters) {
        return new SketchwareBlock(text, new ArrayList<>(Arrays.asList(parameters)), color);
    }

    /**
     * Create a parameter block
     * @param text The text of the block
     * @param color The color of the block
     * @param return_type The return type of the block
     * @return The block according to the parameters given
     */
    public static SketchwareBlock newReturnBlock(String text, int color, SketchwareField.Type return_type) {
        return new SketchwareBlock(text, "1", 2, new ArrayList<>(), color, true, return_type);
    }

    /**
     * Create a parameter block that has parameters in it
     * @param text The text of the block
     * @param color The color of the block
     * @param return_type The return type of the block
     * @param parameters The parameters for this block
     * @return The block according to the parameters given
     */
    public static SketchwareBlock newReturnBlockWithParams(String text, int color, SketchwareField.Type return_type, SketchwareField... parameters) {
        return new SketchwareBlock(text, "1", 2, new ArrayList<>(Arrays.asList(parameters)), color, true, return_type);
    }
    // Factory constructors ========================================================================



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
        if (is_return_block && parameters.size() == 0)
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
     * @return Pair of "Should we remove this block from the block list" and the block that is picked up
     */
    public Pair<Boolean, SketchwareBlock> onPickup(int x, int y, Paint text_paint) {
        // int x = left + text_padding;  // The initial x's text position

        Log.d(TAG, "onPickup: x: " + x + " y: " + y);

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

            Log.d(TAG, "onPickup: [Init] before: " + x_before + " total: " + x_total);

            // Get the text between a field (should be 0 for the first time) and another field
            String text = getFormat().substring(last_substring_index, (int) param[0]);

            x_total += text_paint.measureText(text) + 5;

            Log.d(TAG, "onPickup: [Text Check] before: " + x_before + " total: " + x_total);

            // Check if the X is somewhere in this text
            if (x > x_before && x < x_total) {
                Log.d(TAG, "onPickup: Somewhere in text, self");

                // Oop, then just pick ourself, i guess
                return new Pair<>(true, this);
            }

            // Update the x_before to the text
            x_before = Integer.parseInt(String.valueOf(x_total));

            last_substring_index = (int) param[1];

            SketchwareField field = (SketchwareField) param[3];

            x_total += field.getWidth(text_paint) + 5;

            Log.d(TAG, "onPickup: [Field Check] before: " + x_before + " total: " + x_total);

            // Check if X is somewhere in this field
            if (x > x_before && x < x_total) {
                Log.d(TAG, "onPickup: Dragging a field");

                // Yup, this the user is dragging a field, check if this is a block
                if (field.is_block) {
                    Log.d(TAG, "onPickup: Is a block");

                    // Ohk this is a block, check if the parameter block has a parameter too
                    if (field.block.parameters.size() == 0) {
                        Log.d(TAG, "onPickup: No parameters, pick up ourself");

                        // Nop it doesn't have any, means we can pick this parameter!
                        // Remove the field from parameters
                        parameters.set(index, new SketchwareField("", field.block.return_type, field.other_type));

                        parsed_format.get(index)[3] = new SketchwareField("", field.block.return_type, field.other_type);

                        // Then set the block
                        return new Pair<>(false, field.block);
                    } else {
                        Log.d(TAG, "onPickup: Has parameter, recursive call");

                        // This block has a parameter, recursively call onPickup!
                        // oh yeah don't forget to offset the x
                        Pair<Boolean, SketchwareBlock> pickup_block = field.block.onPickup(x - x_before, y, text_paint);

                        // Should we remove this block?
                        if (pickup_block.first) {
                            // Yep, we should, for now, it will just be a text, nothing fancy
                            parameters.set(index, new SketchwareField("", field.block.return_type, field.other_type));

                            parsed_format.get(index)[3] = new SketchwareField("", field.block.return_type, field.other_type);
                        }

                        return new Pair<>(false, pickup_block.second);
                    }
                } else {
                    Log.d(TAG, "onPickup: Value parameter, self");

                    // Oop, this is just a value parameter, just pick ourself, i guess
                    return new Pair<>(true, this);
                }
            }

            index++;
        }

        // Wat, nothing?
        Log.d(TAG, "onPickup: Weird, nothing");

        // This shouldn't happen but meh, let's just pick ourself
        return new Pair<>(true, this);
    }

    /**
     * This function returns the bounds of this block in RectF
     * @param x The x position of this block
     * @param y The y position of this block
     * @param text_paint The paint used to draw the block text
     * @return This block's bounds
     */
    public RectF getBounds(int x, int y, Paint text_paint) {
        return new RectF(
                x,
                y,
                x + getWidth(text_paint),
                y + getHeight(text_paint)
        );
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
    public void draw(Context context, Canvas canvas, Paint rect_paint, Paint text_paint, int top, int left, int shadow_height, int block_outset_left_margin, int block_outset_width, int block_outset_height, boolean is_overlapping, int previous_block_color, boolean is_round, int round_radius) {
        draw(context, canvas, rect_paint, text_paint, top, left, getHeight(text_paint), shadow_height, block_outset_left_margin, block_outset_left_margin, block_outset_width, block_outset_height, is_overlapping, previous_block_color, is_round, round_radius);
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
     * @param top_block_outset_left_margin The block's top outset's left margin
     * @param block_outset_width The outset block's width
     * @param block_outset_height The outset block's height
     * @param is_overlapping Do you want to overlap the block above's shadow?
     * @param previous_block_color The previous block's color, used to draw the outset of the block above
     */
    public void draw(Context context,
                     Canvas canvas,
                     Paint rect_paint,
                     Paint text_paint,
                     int top,
                     int left,
                     int height,
                     int shadow_height,
                     int block_outset_left_margin,
                     int top_block_outset_left_margin,
                     int block_outset_width,
                     int block_outset_height,
                     boolean is_overlapping,
                     int previous_block_color,
                     boolean is_round,
                     int round_radius
    ) {
        // int block_width = (int) text_paint.measureText(format) + 20;
        int block_width = getWidth(text_paint);

        int left_parameter = left;

        // FIXME: 3/8/21 Height shouldn't be added with the shadow height
        // Draw the block body
        if (is_return_block) {
            switch (return_type) {
                case STRING:
                    DrawHelper.drawRect(canvas, left, top, block_width, height + shadow_height, color);
                    break;

                case INTEGER:
                    DrawHelper.drawIntegerField(canvas, left, top, block_width + height / 5, height + shadow_height, color);

                    left_parameter += 5;
                    break;

                case BOOLEAN:
                    DrawHelper.drawBooleanField(canvas, left, top, block_width + height / 5, height + shadow_height, color);

                    left_parameter += height / 5;
                    break;

                case OTHER:
                    DrawHelper.drawRectSimpleOutsideShadow(canvas, left, top, block_width, height + shadow_height, shadow_height, color);
                    break;
            }
        } else {
            if (is_round) {
                DrawHelper.drawRoundRectSimpleOutsideShadow(canvas, left, top, block_width, height + shadow_height, shadow_height, round_radius, color);
            } else {
                DrawHelper.drawRectSimpleOutsideShadow(canvas, left, top, block_width, height + shadow_height, shadow_height, color);
            }
        }

        // If this is a return block, don't draw the outset
        // return blocks doesn't have an outset cheems
        if (!is_return_block) {
            // Should the blocks overlap each other?
            if (!is_overlapping) {
                // Ohk no, draw the outset with shadow and the top block's outset

                // Draw the outset
                DrawHelper.drawRectSimpleOutsideShadow(canvas, left + block_outset_left_margin, top, block_outset_width, height + block_outset_height + block_outset_height, shadow_height, color);

                // Draw the top block's outset
                DrawHelper.drawRect(canvas, left + top_block_outset_left_margin, top, block_outset_width, block_outset_height, DrawHelper.manipulateColor(previous_block_color, 0.8f));
            } else {
                // Yes, just draw the top block's outset

                // Draw the top block's outset
                DrawHelper.drawRect(canvas, left + top_block_outset_left_margin, top, block_outset_width, block_outset_height, previous_block_color);
            }
        }

        // Draw the block's text and parameters
        drawParameters(context, canvas, left_parameter, top, top + ((getHeight(text_paint) + shadow_height + block_outset_height + text_padding) / 2), height, shadow_height, text_paint);
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

            field.draw(context, canvas, x, top + text_padding, text_paint, height - text_padding - shadow_height, DrawHelper.manipulateColor(color, .8f));

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
                ", text_padding=" + text_padding +
                ", is_parameter=" + is_return_block +
                ", default_height=" + default_height +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SketchwareBlock that = (SketchwareBlock) o;
        return is_bottom == that.is_bottom &&
                next_block == that.next_block &&
                color == that.color &&
                text_padding == that.text_padding &&
                is_return_block == that.is_return_block &&
                default_height == that.default_height &&
                format.equals(that.format) &&
                parsed_format.equals(that.parsed_format) &&
                id.equals(that.id) &&
                parameters.equals(that.parameters) &&
                return_type == that.return_type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(format, parsed_format, id, parameters, is_bottom, next_block, color, text_padding, is_return_block, return_type, default_height);
    }
}
