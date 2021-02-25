package com.openblocks.blocks.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.openblocks.blocks.view.styles.SketchwareBlockOriginalStyle;

import java.util.ArrayList;

public class SketchwareBlocksView extends View {

    Paint rect_paint;
    Paint text_paint;

    int left_position = 50;
    int top_position = 50;

    int shadow_height = 10;
    int block_outset_height = 10;

    int block_height = 60;
    int event_offset = 50;

    int nested_bottom_margin = 20;

    int event_height = 50; // add this to styleable

    int block_outset_left_margin = 50; // add this to styleable
    int block_outset_width = 75; // add this to styleable

    boolean is_overlapping = false;

    SketchwareEvent event;

    Context context;

    public SketchwareBlocksView(Context context) {
        super(context);
        initialize(context, null);
    }

    public SketchwareBlocksView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public SketchwareBlocksView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs);
    }

    public SketchwareBlocksView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize(context, attrs);
    }

    public void setEvent(SketchwareEvent event) {
        this.event = event;

        initialize(null, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.v("Chart onMeasure w", MeasureSpec.toString(widthMeasureSpec));
        Log.v("Chart onMeasure h", MeasureSpec.toString(heightMeasureSpec));

        int largest_width = 0;
        int blocks_height_sum = 0;
        for (SketchwareBlock block : event.blocks) {
            largest_width = Math.max(block.getWidth(text_paint), largest_width);

            blocks_height_sum += block.getHeight(text_paint) + shadow_height;
        }

        int desiredWidth = left_position + largest_width + getPaddingLeft() + getPaddingRight() + left_position /* Just to get some padding on the right */;

        int desiredHeight = event_offset + event_height + blocks_height_sum + getPaddingTop() + getPaddingBottom() + top_position;

        setMeasuredDimension(measureDimension(desiredWidth, widthMeasureSpec),
                measureDimension(desiredHeight, heightMeasureSpec));
    }

    private int measureDimension(int desiredSize, int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = desiredSize;
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }

        if (result < desiredSize){
            Log.w("SketchwareBlocksView", "The view is too small, the content might get cut");
        }

        return result;
    }

    private void initialize(Context context, AttributeSet attrs) {
        this.context = context;

        if (attrs != null) {
            TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.SketchwareBlocksView);

            left_position = attributes.getDimensionPixelSize(R.styleable.SketchwareBlocksView_left_position, left_position);
            top_position = attributes.getDimensionPixelSize(R.styleable.SketchwareBlocksView_top_position, top_position);

            shadow_height = attributes.getDimensionPixelSize(R.styleable.SketchwareBlocksView_shadow_height, shadow_height);

            block_outset_height = attributes.getDimensionPixelSize(R.styleable.SketchwareBlocksView_block_outset_height, block_outset_height);
            block_height = attributes.getDimensionPixelSize(R.styleable.SketchwareBlocksView_block_height, block_height);

            event_offset = attributes.getDimensionPixelSize(R.styleable.SketchwareBlocksView_event_offset, event_offset);

            is_overlapping = attributes.getBoolean(R.styleable.SketchwareBlocksView_is_overlapping, is_overlapping);

            nested_bottom_margin = attributes.getDimensionPixelSize(R.styleable.SketchwareBlocksView_nested_bottom_margin, nested_bottom_margin);

            attributes.recycle();
        }

        event = new SketchwareEvent("MainActivity", "onCreate");
        event.blocks.add(new SketchwareBlockOriginalStyle("Hello World!", 0xFFFFFFFF));

        if (event == null) {
            event = new SketchwareEvent("MainActivity", "onCreate");

            event.blocks.add(new SketchwareBlock("Hello World", "1", 2, new ArrayList<>(), 0xFFE10C0C));
            event.blocks.add(new SketchwareBlock("This is SketchwareBlocksView", "2", 3, new ArrayList<>(), 0xFFD1159C));
            event.blocks.add(new SketchwareBlock("This block resizes", "3", 4, new ArrayList<>(), 0xFF14D231));
            event.blocks.add(new SketchwareBlock("According to the text's width", "4", 5, new ArrayList<>(), 0xFF2115D1));

            ArrayList<SketchwareField> fields = new ArrayList<>();
            fields.add(new SketchwareField("parameters"));
            fields.add(new SketchwareField("yeah"));

            event.blocks.add(new SketchwareBlock("This block has %s cool right? %s.kek", "5", 6, fields, 0xFFE65319));

            ArrayList<SketchwareField> value_of_recursive = new ArrayList<>();

            ArrayList<SketchwareField> get_id_recursive = new ArrayList<>();
            get_id_recursive.add(new SketchwareField("Hello World"));

            ArrayList<SketchwareField> recursive_fields_root = new ArrayList<>();
            recursive_fields_root.add(new SketchwareField(new SketchwareBlock("get ID %s", "10", -1, get_id_recursive, 0xFF0000FF)));

            value_of_recursive.add(new SketchwareField(new SketchwareBlock("value of %s", "10", -1, recursive_fields_root, 0xFF15D807)));

            event.blocks.add(new SketchwareBlock("Also, recursive fields! %m.view", "6", 7, value_of_recursive, 0xFFE65319));

            event.blocks.add(new SketchwareBlock("Originally Made by Iyxan23 (github.com/Iyxan23)", "7", 8, new ArrayList<>(), 0xFF2115D1));
            event.blocks.add(new SketchwareBlock("Repository transferred to OpenBlocksTeam (github.com/OpenBlocksTeam)", "8", 8, new ArrayList<>(), 0xFFE10C0C));

            ArrayList<SketchwareBlock> bloks = new ArrayList<>();
            bloks.add(new SketchwareBlock("Yeah, nested blocks!", "1", 2, new ArrayList<>(), 0xFFE10C0C));
            bloks.add(new SketchwareBlock("Very cool, right?", "2", -1, new ArrayList<>(), 0xFFE65319));

            ArrayList<SketchwareField> a = new ArrayList<>();
            a.add(new SketchwareField("oh god"));

            event.blocks.add(new SketchwareNestedBlock("Did i say nested? %a", "9", 10, a, 0xFF21167B, bloks)); //0xFFE10C0C

            event.blocks.add(new SketchwareBlock("Finish Activity", "10", -1, new ArrayList<>(), 0xFF1173E4));
        }

        text_paint = new Paint();
        text_paint.setTypeface(Typeface.DEFAULT);
        text_paint.setStyle(Paint.Style.FILL);
        text_paint.setFakeBoldText(true);
        text_paint.setAntiAlias(true);
        text_paint.setColor(0xFFFFFFFF);
        text_paint.setTextSize(30f);

        rect_paint = new Paint();
        rect_paint.setAntiAlias(true);
        rect_paint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw the blocks from top to bottom
        int previous_block_color = event.color;
        int previous_top_position = event_height;  // Start with event_offset
        int previous_block_height = event_height;  // Because if not, the first block would get overlapped by the event
        for (int i = 0; i < event.blocks.size(); i++) {

            SketchwareBlock current_block = event.blocks.get(i);
            current_block.default_height = block_height;

            int top_position;

            top_position = previous_top_position + previous_block_height + shadow_height;

            if (is_overlapping) {
                // Overlap the previous block's shadow
                top_position -= shadow_height;
            }

            previous_top_position = top_position;

            previous_block_height = current_block.getHeight(text_paint);

            // Apply the bottom margin if this is a nested block
            if (current_block instanceof SketchwareNestedBlock) {
                ((SketchwareNestedBlock) current_block).bottom_margin = nested_bottom_margin;
            }

            current_block
                .draw(
                        context,
                        canvas,
                        rect_paint,
                        text_paint,
                        top_position,
                        left_position,
                        shadow_height,
                        block_outset_left_margin,
                        block_outset_width,
                        block_outset_height,
                        is_overlapping,
                        previous_block_color
                );
            previous_block_color = current_block.color;
        }

        event.draw(canvas, event_height, 10, left_position, event_offset, 15, shadow_height, rect_paint, text_paint);
    }
}
