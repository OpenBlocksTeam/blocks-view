package com.iyxan23.blocks.view;

import java.util.ArrayList;

public class SketchwareNestedBlock extends SketchwareBlock {

    public ArrayList<SketchwareBlock> blocks = new ArrayList<>();

    public SketchwareNestedBlock(String format, String id, int next_block, ArrayList<SketchwareField> parameters, int color) {
        super(format, id, next_block, parameters, color);
    }
}
