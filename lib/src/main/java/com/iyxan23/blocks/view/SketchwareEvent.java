package com.iyxan23.blocks.view;

import java.util.ArrayList;

public class SketchwareEvent {

    public String activity_name;
    public String name;

    public int color = 0xFFF39B0E;
    public int color_dark = 0xFFC8800E;

    public ArrayList<SketchwareBlock> blocks = new ArrayList<>();

    public SketchwareEvent(String activity_name, String name) {
        this.activity_name = activity_name;
        this.name = name;
    }
}
