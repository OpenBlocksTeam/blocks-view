package com.iyxan23.blocks.view;

import java.util.ArrayList;

public class SketchwareBlock {

    public String format;
    public String id;
    public ArrayList<SketchwareField> parameters;

    int next_block;

    public int color;
    public int color_dark;

    public SketchwareBlock(String format, String id, int next_block, ArrayList<SketchwareField> parameters, int color) {
        this.format = format;
        this.id = id;
        this.next_block = next_block;
        this.parameters = parameters;
        this.color = color;
        this.color_dark = Utilities.manipulateColor(color, 0.7f);
    }
}
