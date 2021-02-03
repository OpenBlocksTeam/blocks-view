package com.iyxan23.blocks.view;

import java.util.ArrayList;

public class SketchwareBlocksParser {

    String logic_data;

    public SketchwareBlocksParser() { }

    /**
     * This constructor will parse the logic_data
     *
     * @param logic_data The decrypted raw format of data/logic
     */
    public SketchwareBlocksParser(String logic_data) {
        this.logic_data = logic_data;
    }

    /**
     * Parses the logic_data string into an array of SketchwareEvents
     * @return Parsed data
     */
    public ArrayList<SketchwareEvent> parse() {
        // TODO
        return null;
    }
}
