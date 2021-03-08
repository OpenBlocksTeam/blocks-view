package com.openblocks.blocks.view;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is a parser that can be used to parse Sketchware's decrypted logic file
 */
public class SketchwareBlocksParser {

    String logic_data;

    private ArrayList<Integer> block_id_blacklist = new ArrayList<>();
    // "id": JSONObject
    private final HashMap<String, JSONObject> tmp_blocks = new HashMap<>();

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
        if (logic_data == null)
            throw new IllegalStateException("logic_data hasn't been set!");

        ArrayList<SketchwareEvent> events = new ArrayList<>();

        // We need this so we can evaluate the blocks before we hit the end of the file
        logic_data += "\n";

        String[] lines = logic_data.split("\n");

        // This boolean is going to skip an entire event until a blank line
        boolean skip_event = false;

        String event_name = "";
        String activity_name = "";

        for (String line: lines) {

            // Check if we're outside of an event
            if (line.trim().equals("")) {
                skip_event = false;

                // Okay, Let's do the sorting here
                SketchwareEvent event = new SketchwareEvent(activity_name, event_name);

                // Loop for every blocks
                for (String id: tmp_blocks.keySet()) {

                    if (block_id_blacklist.contains(Integer.parseInt(id)))
                        continue;

                    JSONObject block = tmp_blocks.get(id);

                    try {
                        event.blocks.add(new SketchwareBlock(
                                /* Format:           */ block.getString("spec"),
                                /* Block ID:         */ id,
                                /* Next Block ID:    */ Integer.parseInt(block.getString("nextBlock")),
                                /* Parameter:        */ parseParameter(block, id),
                                /* Block color:      */ block.getInt("color")
                        ));
                    } catch (JSONException e) {
                        // Weird, probably a project corruption
                    }
                }

                events.add(event);

                block_id_blacklist.clear();
                tmp_blocks.clear();

                // Reset the event name as we are out of any block collection
                event_name = "";
                activity_name = "";
                continue;

            } else {
                if (skip_event) {
                    continue;
                }
            }

            // If we're aren't in an event name
            if (event_name.equals("")) {
                String header_regex = "@(\\w+).java_(.+)";

                Pattern r = Pattern.compile(header_regex);
                Matcher m = r.matcher(line);

                if (m.find()) {
                    if (Objects.equals(m.group(2), "var") || Objects.equals(m.group(2), "func")) {
                        skip_event = true;
                        continue;
                    }

                    activity_name = m.group(1);
                    event_name = m.group(2);
                }

            } else {
                // Fetch every blocks in this event into tmp_blocks

                try {
                    JSONObject json = new JSONObject(line);

                    tmp_blocks.put(json.getString("id"), json);
                } catch (JSONException e) {
                    e.printStackTrace();
                    // Something is wrong
                }
            }
        }

        return events;
    }

    private ArrayList<SketchwareField> parseParameter(JSONObject block, String id) throws JSONException {
        ArrayList<SketchwareField> params = new ArrayList<>();

        JSONArray params_ = block.getJSONArray("parameters");

        for (int index = 0; index < params_.length(); index++) {
            // Check if this parameter references into another block
            String reference_block_regex = "@(\\d+)";
            Pattern r = Pattern.compile(reference_block_regex);
            Matcher m = r.matcher(params_.getString(index));

            if (m.find()) {
                // Ah it references into m.group(1) block id
                String block_reference = m.group(1);

                // blacklist the id so we don't accidentally parse a return value block
                block_id_blacklist.add(Integer.parseInt(block_reference));

                JSONObject parameter_block = tmp_blocks.get(block_reference);

                params.add(
                        new SketchwareField(
                                new SketchwareBlock(
                                        /* Format:           */ block.getString("spec"),
                                        /* Block ID:         */ id,
                                        /* Next Block ID:    */ Integer.parseInt(block.getString("nextBlock")),
                                        /* Parameter:        */ parseParameter(parameter_block, block_reference),
                                        /* Block color:      */ block.getInt("color")
                                )
                        )
                );
            } else {
                params.add(
                        new SketchwareField(
                                (String) params_.get(index)
                        )
                );
            }
        }

        return params;
    }
}
