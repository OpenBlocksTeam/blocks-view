package com.iyxan23.blocks.view;

import java.util.ArrayList;

public class SketchwareField {

    // This boolean indicates if this is a block or not
    public boolean is_block = true;
    public String value = "";  // This value is going to be used if is_block is false

    public SketchwareBlock block;

    /**
     * This will initialize this class as a SketchwareBlock (return value block)
     * @param format The block's format
     * @param id The block's ID
     * @param next_block The block's next block
     * @param parameters The block's parameters
     * @param color The block's color
     */
    public SketchwareField(String format, String id, int next_block, ArrayList<SketchwareField> parameters, int color) {
        block = new SketchwareBlock(format, id, next_block, parameters, color);
        is_block = true;
    }

    /**
     * This constructor initializes this class as a fixed value
     * @param value The value
     */
    public SketchwareField(String value) {
        this.value = value;
        is_block = false;
    }
}
