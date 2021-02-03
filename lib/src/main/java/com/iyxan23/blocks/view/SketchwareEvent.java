package com.iyxan23.blocks.view;

import java.util.ArrayList;

public class SketchwareEvent {

    String activity_name;
    String name;

    public int color = 0xFFF39B0E;
    public int color_dark = 0xFFC8800E;

    ArrayList<SketchwareBlock> blocks;

    public SketchwareEvent(String activity_name, String name) {
        this.activity_name = activity_name;
        this.name = name;
    }

    public ArrayList<SketchwareBlock> getBlocks() {
        return blocks;
    }

    public void setBlocks(ArrayList<SketchwareBlock> blocks) {
        this.blocks = blocks;
    }
}
