package com.openblocks.blocks.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class SketchwareBlocksView extends View {

    private static final String TAG = "SketchwareBlocksView";

    Paint rect_paint;
    Paint text_paint;
    Paint shadow_paint;

    int left_position = 50;
    int top_position = 50;

    int shadow_height = 10;
    int block_outset_height = 10;

    int block_height = 60;
    int event_top = 50;

    int nested_bottom_margin = 30;

    int event_height = 50;

    int block_outset_left_margin = 50;
    int block_outset_width = 75;

    float block_text_size = 30f;
    int block_text_color = 0xFFFFFFFF;

    boolean is_overlapping = false;

    SketchwareEvent event;

    Context context;

    GestureDetector gestureDetector;
    boolean isHolding = false;

    Vibrator vibrator;

    int picked_up_x_offset = 0;
    int picked_up_y_offset = 0;

    ArrayList<Pair<Vector2D, SketchwareBlock>> unconnected_blocks = new ArrayList<>();
    int picked_up_block = -1;

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
        this.event = (SketchwareEvent) event.clone();
        unconnected_blocks.clear();
        picked_up_block = -1;

        initialize(this.context, null);
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

        int desiredHeight = event_top + event_height + blocks_height_sum + getPaddingTop() + getPaddingBottom() + top_position;

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

        unconnected_blocks.add(new Pair<>(new Vector2D(300, 500), new SketchwareBlock("hello world", 0xFFE65319)));

        try {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        } catch (AssertionError ignored) {
            // Vibrator service isn't supported, it might be because we are in an emulation or smth, so skip instead
        }

        if (attrs != null) {
            TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.SketchwareBlocksView);

            left_position = attributes.getDimensionPixelSize(R.styleable.SketchwareBlocksView_left_position, left_position);
            top_position = attributes.getDimensionPixelSize(R.styleable.SketchwareBlocksView_top_position, top_position);

            shadow_height = attributes.getDimensionPixelSize(R.styleable.SketchwareBlocksView_shadow_height, shadow_height);

            block_outset_height = attributes.getDimensionPixelSize(R.styleable.SketchwareBlocksView_block_outset_height, block_outset_height);
            block_outset_width = attributes.getDimensionPixelSize(R.styleable.SketchwareBlocksView_block_outset_width, block_outset_width);
            block_outset_left_margin = attributes.getDimensionPixelSize(R.styleable.SketchwareBlocksView_block_outset_left_margin, block_outset_left_margin);
            block_text_size = attributes.getDimensionPixelSize(R.styleable.SketchwareBlocksView_block_text_size, (int) block_text_size);
            block_height = attributes.getDimensionPixelSize(R.styleable.SketchwareBlocksView_block_height, block_height);

            block_text_color = attributes.getColor(R.styleable.SketchwareBlocksView_block_text_color, block_text_color);

            event_top = attributes.getDimensionPixelSize(R.styleable.SketchwareBlocksView_event_top, event_top);
            event_height = attributes.getDimensionPixelSize(R.styleable.SketchwareBlocksView_event_height, event_height);

            is_overlapping = attributes.getBoolean(R.styleable.SketchwareBlocksView_is_overlapping, is_overlapping);

            nested_bottom_margin = attributes.getDimensionPixelSize(R.styleable.SketchwareBlocksView_nested_bottom_margin, nested_bottom_margin);

            attributes.recycle();
        }

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
        text_paint.setColor(block_text_color);
        text_paint.setTextSize(block_text_size);

        rect_paint = new Paint();
        rect_paint.setAntiAlias(true);
        rect_paint.setStyle(Paint.Style.FILL);

        shadow_paint = new Paint();
        //shadow_paint.setColor(0x00000000);
        shadow_paint.setShadowLayer(16, 0, 0, Color.BLACK);

        gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
            public void onLongPress(MotionEvent e) {
                Log.d(TAG, "onLongPress: long press!");
                vibrator.vibrate(100);
                isHolding = true;

                int x = (int) e.getX();
                int y = (int) e.getY();

                picked_up_block = pickup_block(x, y);

                if (picked_up_block == -1)
                    return;

                Pair<Vector2D, SketchwareBlock> block = unconnected_blocks.get(picked_up_block);
                picked_up_x_offset = block.first.x - x;
                picked_up_y_offset = block.first.y - y;
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent mot_event) {
        gestureDetector.onTouchEvent(mot_event);

        switch (mot_event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "onTouchEvent: DOWN");
                return true;

            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "onTouchEvent: MOVE");
                int x = (int) mot_event.getX();
                int y = (int) mot_event.getY();

                if (isHolding) {
                    // Move the block to the designated location
                    // Pickup the block first

                    if (picked_up_block == -1) {
                        // There is no block here, just quit
                        break;
                    }

                    // Move the block
                    unconnected_blocks.get(picked_up_block).first.x = x + picked_up_x_offset;
                    unconnected_blocks.get(picked_up_block).first.y = y + picked_up_y_offset;

                    // Redraw
                    invalidate();
                }

                return true;

            case MotionEvent.ACTION_UP:
                Log.d(TAG, "onTouchEvent: UP");
                isHolding = false;
                picked_up_block = -1;
                return true;
        }

        return false;
    }

    /**
     * This function is used to pickup a block at the specified location
     *
     * @param x The x coordinate
     * @param y The y coordinate
     * @return The index of this block in unconnected_blocks
     */
    private int pickup_block(int x, int y) {
        // Check if a block already exists in the unconnected_blocks
        int index = 0;
        for (Pair<Vector2D, SketchwareBlock> block : unconnected_blocks) {
            Vector2D block_position = block.first;
            SketchwareBlock mBlock = block.second;

            RectF block_bounds = new RectF(
                    block_position.x,
                    block_position.y,

                    block_position.x
                            + mBlock.getWidth(text_paint),

                    block_position.y
                            + mBlock.getHeight(text_paint)
            );

            if (block_bounds.contains(x, y)) {
                // Ohh, we're here bois, let's just return the location
                return index;
            }

            index++;
        }

        // Looks like it hasn't been dragged yet, let's just check each blocks
        // This code is almost the same as in the onDraw() method
        int previous_top_position = event_height;  // Start with event_offset
        int previous_block_height = event_top;  // Because if not, the first block would get overlapped by the event

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

            RectF bounds = new RectF(
                    left_position,
                    top_position, // TODO: IMPLEMENT DRAGGING BLOCKS INSIDE A NESTED BLOCK, AND ALSO PARAMETER BLOCKS
                    left_position + current_block.getWidth(text_paint),
                    top_position + current_block.getHeight(text_paint)
            );

            if (bounds.contains(x, y)) {
                // Ok, first, we're gonna pop out this block from the event.blocks
                // then we're gonna add this block to the unconnected blocks
                event.blocks.remove(i);

                unconnected_blocks.add(new Pair<>(new Vector2D(left_position, top_position), current_block));

                // Return the position of the unconnected block (it should be at the last item)
                return unconnected_blocks.size() - 1;
            }
        }

        // Ok, this guy is just clicking nothing
        return -1;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(0xFFFFFFFF);

        // Draw the blocks from top to bottom
        int previous_block_color = event.color;
        int previous_top_position = event_height;  // Start with event_offset
        int previous_block_height = event_top;  // Because if not, the first block would get overlapped by the event
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

        event.draw(canvas, event_height, 10, left_position, event_top, 15, shadow_height, rect_paint, text_paint);


        // Nullcheck
        if (unconnected_blocks == null)
            return;

        // Draw the unconnected blocks
        int index = 0;
        for (Pair<Vector2D, SketchwareBlock> block : unconnected_blocks) {
            Vector2D position = block.first;

            // Important for certain APIs
            setLayerType(LAYER_TYPE_SOFTWARE, shadow_paint);

            if (index == picked_up_block) {
                canvas.drawRect(
                        position.x,
                        position.y,
                        position.x + block.second.getWidth(text_paint),
                        position.y + block.second.getHeight(text_paint),
                        shadow_paint
                );
            }

            // Draw the block
            block.second.draw(
                    context,
                    canvas,
                    rect_paint,
                    text_paint,
                    position.y,
                    position.x,
                    shadow_height,
                    block_outset_left_margin,
                    block_outset_width,
                    block_outset_height,
                    is_overlapping,
                    0x00000000
            );

            index++;
        }
    }


    public static class Vector2D {
        public int x;
        public int y;

        public Vector2D(int x, int y) { this.x = x; this.y = y; }
    }
}
