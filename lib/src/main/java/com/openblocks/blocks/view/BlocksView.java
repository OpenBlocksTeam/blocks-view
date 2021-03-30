package com.openblocks.blocks.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

/**
 * This class is the View that should be used inside your layout, Used to display blocks in an event
 */
public class BlocksView extends View {

    /**
     * TAG is used to call logging functions, (e.g. <code>Log.d(TAG, "Hello World!");</code>)
     */
    private static final String TAG = "BlocksView";

    /**
     * rect_paint is a {@link Paint} that will be used to draw blocks
     */
    Paint rect_paint;

    /**
     * text_paint is a {@link Paint} that will be used to draw texts in the blocks
     */
    Paint text_paint;

    /**
     * shadow_paint is a {@link Paint} that will be used to draw shadow when we picked up a block
     */
    Paint shadow_paint;


    // Customizations variables v===================================================================

    int left_position = 50;
    int top_position = 50; // TODO: DELETE THIS

    int shadow_height = 10;
    int block_outset_height = 10;

    int block_height = 60;
    int event_top = 50;

    int nested_bottom_margin = 50;

    int event_height = 50;

    int block_outset_left_margin = 50;
    int block_outset_width = 75;

    float block_text_size = 30f;
    int block_text_color = 0xFFFFFFFF;

    boolean is_overlapping = false;

    boolean is_round = true;
    int round_radius = 10;

    // Customizations variables ^ ==================================================================


    // Other variables =============================================================================

    /** This variable is used to store blocks of collections */
    BlocksViewEvent event;

    /** This variable is used to detect long presses */
    GestureDetector gestureDetector;

    /** This variable is used to vibrate when the user picked up a block */
    Vibrator vibrator;

    /** Well, Context */
    Context context;

    /** Indicates if we're holding a block */
    boolean isHolding = false;

    /** When the block is picked up, we need to know the offset between the xy point of the (picked up) block (top left) and the cursor */
    int picked_up_x_offset = 0;
    int picked_up_y_offset = 0;

    /** When we move the blocks, the {@link #unconnected_blocks} must also move */
    int unconnected_top_offset = 0;
    int unconnected_left_offset = 0;

    /** The index of the block we picked inside {@link #unconnected_blocks} */
    int picked_up_block_index = -1;

    /** This array list is used to store unconnected blocks with its real (not modified by movement) coordinates
     *  then those real coordinates will be added with {@link #unconnected_left_offset} and {@link #unconnected_top_offset}
     *
     *  A Very Important Note: the Vector2D is in it's raw form (doesn't store offset-ed numbers), because most of the blocks are calculated on their raw form, and will be added with event_top and left_position at draw
     *  */
    ArrayList<Pair<Vector2D, Block>> unconnected_blocks = new ArrayList<>();

    /** This array list is used to indicate where the block should land on when dropped */
    // Important note: top_positions are also modified when the view is moved / free move
    ArrayList<Integer> top_positions = new ArrayList<>();

    /**
     * This array list contains "optimized" blocks (blocks that are visible in the canvas)
     * Use this list if you're in need of looping each blocks to respond to user interaction
     */
    ArrayList<Block> optimized_blocks = new ArrayList<>();

    /**
     * This variable is used to determine how many blocks from the top that got
     * optimized / removed when drawing (blocks that are visible in the canvas)
     */
    int top_optimize_cut = 0;

    /**
     * This variable is used to determine how many blocks from the bottom that got
     * optimized / removed when drawing (blocks that are visible in the canvas)
     */
    int bottom_optimize_cut = 0;

    // Other variables =============================================================================



    // Constructors ================================================================================
    public BlocksView(Context context) {
        super(context);
        initialize(context, null);
    }

    public BlocksView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public BlocksView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs);
    }

    public BlocksView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize(context, attrs);
    }
    // Constructors ================================================================================



    // Useful functions ============================================================================
    /**
     * Set the event (collection of blocks) that is to be displayed
     * @param event The event / collection of blocks
     */
    public void setEvent(BlocksViewEvent event) {
        this.event = (BlocksViewEvent) event.clone();
        unconnected_blocks.clear();
        picked_up_block_index = -1;

        initialize(this.context, null);
    }

    /**
     * This method adds a floating block into the editor
     * @param block The block that is to be added
     * @param x The x position
     * @param y The y position
     */
    public void addBlock(Block block, int x, int y) {
        unconnected_blocks.add(new Pair<>(
                new Vector2D(x, y),
                block
        ));
    }

    /**
     * This function checks if the user is dragging a block
     * @return Is the user dragging a block?
     */
    public boolean isDraggingBlock() {
        return picked_up_block_index != -1;
    }
    // Useful functions ============================================================================



    // Initializers ================================================================================
    /**
     * Initializes variables
     *
     * @param context The context
     * @param attrs The attribute set
     */
    private void initialize(Context context, AttributeSet attrs) {
        // Initialize DrawHelper
        DrawHelper.initialize();

        // Set the context
        this.context = context;

        // Get the vibrator system service
        try {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        } catch (AssertionError ignored) {
            // Vibrator service isn't supported, it might be because we are in an emulation or smth, so skip instead
        }

        // Check if attribute is not null
        if (attrs != null) {
            // Initialize our attributes
            initializeAttributes(attrs);
        }

        // Is the event set?
        if (event == null) {
            // If not, show demo blocks instead
            demoBlocks();
        }

        // Initialize Paints

        // text_paint is used to draw our block's texts
        text_paint = new Paint();
        text_paint.setTypeface(Typeface.DEFAULT);
        text_paint.setStyle(Paint.Style.FILL);
        text_paint.setFakeBoldText(true);
        text_paint.setAntiAlias(true);
        text_paint.setColor(block_text_color);
        text_paint.setTextSize(block_text_size);

        // line_paint is used to draw the line that indicates where will the block dropped to
        line_paint = new Paint();
        line_paint.setColor(Color.parseColor("#000000"));
        line_paint.setAntiAlias(true);
        line_paint.setStyle(Paint.Style.FILL_AND_STROKE);

        // rect_paint is used to draw the block
        rect_paint = new Paint();
        rect_paint.setAntiAlias(true);
        rect_paint.setStyle(Paint.Style.FILL);

        // shadow_paint is used to draw the shadow when we picked up a block
        shadow_paint = new Paint();
        shadow_paint.setShadowLayer(16, 0, 0, Color.BLACK);

        // black_rect is used to draw a "block drop to field" indicator
        black_rect = new Paint();
        black_rect.setColor(0x88000000);
        black_rect.setStyle(Paint.Style.FILL);

        // Initialize our gesture detector
        initGestureDetector();
    }

    /**
     * This method sets an attribute to our variables
     *
     * @param attrs The attribute set
     */
    private void initializeAttributes(@NonNull AttributeSet attrs) {
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.BlocksView);

        left_position = attributes.getDimensionPixelSize(R.styleable.BlocksView_left_position, left_position);
        top_position = attributes.getDimensionPixelSize(R.styleable.BlocksView_top_position, top_position);

        shadow_height = attributes.getDimensionPixelSize(R.styleable.BlocksView_shadow_height, shadow_height);

        block_outset_height = attributes.getDimensionPixelSize(R.styleable.BlocksView_block_outset_height, block_outset_height);
        block_outset_width = attributes.getDimensionPixelSize(R.styleable.BlocksView_block_outset_width, block_outset_width);
        block_outset_left_margin = attributes.getDimensionPixelSize(R.styleable.BlocksView_block_outset_left_margin, block_outset_left_margin);
        block_text_size = attributes.getDimensionPixelSize(R.styleable.BlocksView_block_text_size, (int) block_text_size);
        block_height = attributes.getDimensionPixelSize(R.styleable.BlocksView_block_height, block_height);

        block_text_color = attributes.getColor(R.styleable.BlocksView_block_text_color, block_text_color);

        event_top = attributes.getDimensionPixelSize(R.styleable.BlocksView_event_top, event_top);
        event_height = attributes.getDimensionPixelSize(R.styleable.BlocksView_event_height, event_height);

        is_overlapping = attributes.getBoolean(R.styleable.BlocksView_is_overlapping, is_overlapping);

        nested_bottom_margin = attributes.getDimensionPixelSize(R.styleable.BlocksView_nested_bottom_margin, nested_bottom_margin);

        attributes.recycle();
    }

    /**
     * This method adds blocks used to demonstration into our block collection ({@link #event})
     */
    private void demoBlocks() {
        event = new BlocksViewEvent("MainActivity", "onCreate");

        event.blocks.add(new Block("Hello World", "1", 2, new ArrayList<>(), 0xFFE10C0C));
        event.blocks.add(new Block("This is BlocksView", "2", 3, new ArrayList<>(), 0xFFD1159C));
        event.blocks.add(new Block("This block resizes", "3", 4, new ArrayList<>(), 0xFF14D231));
        event.blocks.add(new Block("According to the text's width", "4", 5, new ArrayList<>(), 0xFF2115D1));

        ArrayList<BlockField> fields = new ArrayList<>();
        fields.add(new BlockField("parameters"));
        fields.add(new BlockField("yeah"));

        event.blocks.add(new Block("This block has %s cool right? %s.kek", "5", 6, fields, 0xFFE10C0C));

        ArrayList<BlockField> types_fields = new ArrayList<>();
        types_fields.add(new BlockField("1945", BlockField.Type.INTEGER, null));

        event.blocks.add(new Block("Oh yeah, integers %i", types_fields, 0xFFE65319));

        ArrayList<BlockField> booleans = new ArrayList<>();
        booleans.add(new BlockField("false", BlockField.Type.BOOLEAN, null));

        event.blocks.add(new Block("And booleans %b", booleans, 0xFF2115D1));

        ArrayList<BlockField> value_of_recursive = new ArrayList<>();

        ArrayList<BlockField> get_id_recursive = new ArrayList<>();
        get_id_recursive.add(new BlockField("Hello World"));

        ArrayList<BlockField> recursive_fields_root = new ArrayList<>();
        recursive_fields_root.add(new BlockField(new Block("get ID %s", "10", -1, get_id_recursive, 0xFF0000FF), BlockField.Type.INTEGER));

        value_of_recursive.add(new BlockField(new Block("Is empty %s", "10", -1, recursive_fields_root, 0xFF15D807), BlockField.Type.BOOLEAN));

        event.blocks.add(new Block("Also, recursive fields! %m.view", "6", 7, value_of_recursive, 0xFFE65319));

        ArrayList<Block> bloks = new ArrayList<>();
        bloks.add(new Block("Yeah, nested blocks!", "1", 2, new ArrayList<>(), 0xFFE10C0C));
        bloks.add(new Block("Very cool, right?", "2", 3, new ArrayList<>(), 0xFFE65319));

        ArrayList<BlockField> imageview_set_image = new ArrayList<>();

        imageview_set_image.add(new BlockField("imageView1", BlockField.Type.OTHER, "ImageView"));
        imageview_set_image.add(new BlockField("image_file", BlockField.Type.OTHER, "File"));

        bloks.add(new Block("%m.img Set image to PNG %o.file", "3", -1, imageview_set_image, 0xFFE65319));

        ArrayList<BlockField> a = new ArrayList<>();
        a.add(new BlockField("oh god"));

        event.blocks.add(new NestedBlock("Did i say nested? %a", "7", 8, a, 0xFF21167B, bloks)); //0xFFE10C0C

        event.blocks.add(new Block("Originally Made by Iyxan23 (github.com/Iyxan23)", "8", 9, new ArrayList<>(), 0xFF2115D1));
        event.blocks.add(new Block("Repository transferred to OpenBlocksTeam (github.com/OpenBlocksTeam)", "9", 10, new ArrayList<>(), 0xFFE10C0C));

        event.blocks.add(new Block("Finish Activity", "10", -1, new ArrayList<>(), 0xFF1173E4));
    }
    // Initializers ================================================================================



    // Touch detectors =============================================================================
    /**
     * This function initializes our {@link GestureDetector} used for detecting long clicks
     */
    private void initGestureDetector() {
        gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
            public void onLongPress(MotionEvent e) {
                Log.d(TAG, "onLongPress: long press!");

                // Get the x y position of the long press
                int x = (int) e.getX();
                int y = (int) e.getY();

                // Pick up the block
                picked_up_block_index = pickup_block(x, y);

                // Check if there isn't any blocks below us
                if (picked_up_block_index == -1)
                    // Meh, nothing, just return
                    return;

                // If no, get the block, and pick it up!
                Pair<Vector2D, Block> block = unconnected_blocks.get(picked_up_block_index);
                picked_up_x_offset = block.first.x - x;
                picked_up_y_offset = block.first.y - y;

                // Oh yeah vibrate a little, just to give some sense
                vibrator.vibrate(100);

                // And set that we're holding something
                isHolding = true;
            }
        });
    }

    // Variables used to calculate the offset between when we move the canvas / blocks
    int move_x_delta = 0;
    int move_y_delta = 0;

    /** The position of an element inside {@link #top_positions} where the dragged block wants to be dropped to */
    int drop_location = -1;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent mot_event) {
        // Check if this is a long press
        gestureDetector.onTouchEvent(mot_event);

        switch (mot_event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "onTouchEvent: DOWN");

                // If we didn't picked up anything, and we're just touching the canvas
                if (picked_up_block_index == -1) {
                    // The user is moving the canvas!
                    // Set the delta / differences
                    move_x_delta = (int) mot_event.getX() - left_position;
                    move_y_delta = (int) mot_event.getY() - event_top;
                }

                x_down = (int) mot_event.getX();
                y_down = (int) mot_event.getX();

                // We handled the ACTION_DOWN, return true!
                return true;

            case MotionEvent.ACTION_MOVE:

                // Get the x and y position of our cursor / touch position
                int x = (int) mot_event.getX();
                int y = (int) mot_event.getY();

                // Are we holding a block?
                if (isHolding) {
                    // Move the block to the designated location

                    // Check if we picked up a block
                    if (picked_up_block_index == -1) {
                        // We didn't picked anything, just quit
                        break;
                    }

                    // K, let's move the block
                    unconnected_blocks.get(picked_up_block_index).first.x = x + picked_up_x_offset;
                    unconnected_blocks.get(picked_up_block_index).first.y = y + picked_up_y_offset;

                    // Predict the drop location of where the block should be dropped to
                    drop_location = predictDropLocation();
                } else {
                    // so the user is casually moving the view

                    // Set the offset for unconnected blocks

                    unconnected_top_offset = y - move_y_delta + event_top;
                    unconnected_left_offset = x - move_x_delta + left_position;

                    // and also for every blocks
                    event_top = y - move_y_delta;
                    left_position = x - move_x_delta;

                }

                // Redraw
                invalidate();

                return true;

            case MotionEvent.ACTION_UP:
                Log.d(TAG, "onTouchEvent: UP");

                // Check if that was a click
                if (isAClick(x_down, mot_event.getX(), y_down, mot_event.getY())) {
                    // This is just a click, check if there is any field below us
                    checkFieldClick((int) mot_event.getX(), (int) mot_event.getY());

                } else {
                    // Check if we have a relevant drop location below us
                    if (drop_location != -1) {
                        // Ohh ok, let's add the block into the block collection, at the specified index
                        event.blocks.add(top_positions.indexOf(drop_location), unconnected_blocks.get(picked_up_block_index).second);

                        // Then remove it from the unconnected blocks
                        unconnected_blocks.remove(picked_up_block_index);
                    }
                }

                // Reset values
                isHolding = false;
                picked_up_block_index = -1;

                draw_line_at_pos = -1;
                drop_location = -1;

                // predicted_drop_field = null;

                move_y_delta = 0;
                move_x_delta = 0;

                return true;
        }

        return false;
    }

    int x_down = 0;
    int y_down = 0;

    // https://stackoverflow.com/questions/17831395/how-can-i-detect-a-click-in-an-ontouch-listener/17836095
    private final int CLICK_ACTION_THRESHOLD = 200;

    private boolean isAClick(float startX, float endX, float startY, float endY) {
        float differenceX = Math.abs(startX - endX);
        float differenceY = Math.abs(startY - endY);
        return !(differenceX > CLICK_ACTION_THRESHOLD /* =5 */ || differenceY > CLICK_ACTION_THRESHOLD);
    }
    // Touch detectors =============================================================================



    // Pickup, drop blocks utilities ===============================================================

    // The bounds where of how big we should detect if the user wants to drop a block
    int detection_distance_vertical = 20;
    int detection_distance_right = 400;

    // debug
    int draw_line_at_pos = -1;

    // The paint used to draw the line that indicates where the block should be placed / dropped
    Paint line_paint;

    // This rect is used to draw a basic black rectangle in the canvas if the picked up block is hovering over a field
    Rect predicted_drop_field = null;
    Paint black_rect; // This paint is used to draw the rect above

    /**
     * This function predicts the location of where the picked up block will be dropped
     * @return The index element of where the block will be dropped in {@link #top_positions}, returns -1 if the block is dropped on nothing
     */
    private int predictDropLocation() {
        Pair<Vector2D, Block> picked_up_block = unconnected_blocks.get(picked_up_block_index);
        // The picked up block's position
        Vector2D picked_up_block_position = picked_up_block.first;
        Block picked_up_block_block = picked_up_block.second;

        if (!picked_up_block_block.is_return_block) {
            int index = 0;

            for (Integer point : top_positions) {
                // Check if the picked up block position is inside the bounds of
                // We must add these offsets because the top_positions are modified / offset-ed version of it
                if (
                        picked_up_block_position.y + event_top > point - detection_distance_vertical &&
                        picked_up_block_position.y + event_top < point + detection_distance_vertical &&
                        picked_up_block_position.x + left_position > left_position &&
                        picked_up_block_position.x + left_position < event.blocks.get(index).getWidth(text_paint)
                ) {
                    draw_line_at_pos = point;
                    // Log.d(TAG, "predictDropLocation: top: " + point);
                    return point;
                }

                index++;
            }
        } else {

            int index = 0;
            for (Integer block_top : top_positions) {
                // Check if this block is in the bounds of the drag position
                if (!(picked_up_block_position.y + event_top > block_top && picked_up_block_position.y + event_top < top_positions.get(index - 1)))
                    continue;

                // do onHover

                Rect field_bounds = event.blocks.get(index).onHover(picked_up_block_position.x, picked_up_block_position.y, event.blocks.get(index), false, text_paint);

                if (field_bounds != null) {
                    predicted_drop_field = field_bounds;
                }

                index++;
            }
        }

        draw_line_at_pos = -1;
        return -1;
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
        for (Pair<Vector2D, Block> block : unconnected_blocks) {
            Vector2D block_position = block.first.clone();

            Block mBlock = block.second;

            RectF block_bounds = new RectF(
                    block_position.x + left_position,
                    block_position.y + event_top,

                    block_position.x + left_position
                            + mBlock.getWidth(text_paint),

                    block_position.y + event_top
                            + mBlock.getHeight(text_paint)
            );

            if (block_bounds.contains(x, y)) {
                // Ohh, we're here bois, let's just return the location
                return index;
            }

            index++;
        }

        Log.d(TAG, "pickup_block: Not picked, picking blocks");

        // Looks like it hasn't been dragged yet, let's just check each blocks
        // This code is almost the same as in the onDraw() method
        int previous_top_position = event_height;  // Start with event_offset
        int previous_block_height = event_top;  // Because if not, the first block would get overlapped by the event

        for (int i = 0; i < event.blocks.size(); i++) {
            Block current_block = event.blocks.get(i);

            int top_position;

            top_position = previous_top_position + previous_block_height + shadow_height;

            if (is_overlapping) {
                // Overlap the previous block's shadow
                top_position -= shadow_height;
            }

            previous_top_position = top_position;

            previous_block_height = current_block.getHeight(text_paint);

            // Apply the bottom margin if this is a nested block
            if (current_block instanceof NestedBlock) {
                ((NestedBlock) current_block).bottom_margin = nested_bottom_margin;
            }

            Rect bounds = new Rect(
                    left_position,
                    top_position, // TODO: IMPLEMENT DRAGGING BLOCKS INSIDE A NESTED BLOCK, AND ALSO PARAMETER BLOCKS
                    left_position + current_block.getWidth(text_paint),
                    top_position + current_block.getHeight(text_paint)
            );

            if (bounds.contains(x, y)) {
                Log.d(TAG, "pickup_block: x: " + x + " y: " + y);
                Log.d(TAG, "pickup_block: block bounds: " + bounds);
                Log.d(TAG, "pickup_block: Inside the block " + top_position);
                
                // Ohk, call onPickup of the block
                Pair<Boolean, Block> pickup = event.blocks.get(i).onPickup(x - left_position, y - top_position, text_paint);

                // The first pair is to determine if we should remove the block or not?
                if (pickup.first) {
                    // Yup, remove it

                    try {
                        event.blocks.remove(pickup.second);
                    } catch (Exception ignored) { }
                }

                // Add it to the unconnected blocks (picking it up)
                unconnected_blocks.add(0, new Pair<>(new Vector2D(x - left_position, y - event_top), pickup.second));

                // And returning it's position
                return 0;

                /*
                switch (pickup.first) {
                    case PICKUP_SELF:
                        Log.d(TAG, "pickup_block: self");

                        // Ok, first, we're gonna pop out this block from the event.blocks
                        // then we're gonna add this block to the unconnected blocks
                        event.blocks.remove(i);

                        unconnected_blocks.add(0, new Pair<>(new Vector2D(x - left_position, y - event_top), current_block));

                        // Return the position of the unconnected block (it should be at the first item)
                        return 0;

                    case PICKUP_OTHER_BLOCK:
                        Log.d(TAG, "pickup_block: parameter");

                        // Ah, this guy is picking up a parameter, anyway, because the parameter is
                        // already removed on onPickup, we're just gonna add this to the unconnected blocks

                        // Add it to the unconnected blocks
                        unconnected_blocks.add(0, new Pair<>(new Vector2D(x - left_position, y - event_top), pickup.second));

                        // Return the position of this parameter of unconnected_blocks
                        return 0;
                    case PICKUP_NONE:
                        Log.d(TAG, "pickup_block: Pickup none");
                        // Nothing
                        break;
                }
                 */
            }
        }

        // Ok, this guy is just clicking nothing
        Log.d(TAG, "pickup_block: meh, nothing");
        return -1;
    }

    FieldClick fieldClick = null;

    /**
     * This function sets the field click
     * @param fieldClick The field click listener
     */
    public void setFieldClick(FieldClick fieldClick) {
        this.fieldClick = fieldClick;
    }

    /**
     * This function is used to check if a click clicks a field
     */
    private void checkFieldClick(int x, int y) {
        if (fieldClick == null)
            return;

        int top = event_top + event_height;
        int previous_height = event_height;

        for (Block block : optimized_blocks) {
            int current_block_height = block.getHeight(text_paint);

            // Only check if the top is lower than the y - previous block height
            if (top < y - previous_height) {
                BlockField field = block.onClick(x, y, text_paint);

                if (field != null) {
                    fieldClick.onFieldClick(field);
                }
            }

            previous_height = current_block_height;
            top += current_block_height;
        }
    }
    // Pickup, drop blocks utilities ===============================================================



    // Measurer ====================================================================================
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.v("Chart onMeasure w", MeasureSpec.toString(widthMeasureSpec));
        Log.v("Chart onMeasure h", MeasureSpec.toString(heightMeasureSpec));

        int largest_width = 0;
        int blocks_height_sum = 0;
        for (Block block : event.blocks) {
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
            Log.w("BlocksView", "The view is too small, the content might get cut");
        }

        return result;
    }
    // Measurer ====================================================================================



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        top_positions.clear();

        // Empty our canvas
        canvas.drawColor(0xFFFFFFFF);

        // Draw the blocks from top to bottom
        int previous_block_color = event.color;
        int previous_top_position = event_height;  // Start with event_offset
        int previous_block_height = event_top;  // Because if not, the first block would get overlapped by the event

        // Are we overlapping?
        if (is_overlapping) {
            // If yes, then increase the top position, as the event can overlap our first block
            previous_top_position += block_outset_height;
        }

        // Loop per each block
        for (int i = 0; i < event.blocks.size(); i++) {

            // get the current block
            Block current_block = event.blocks.get(i);

            // Set the height to the defined height
            current_block.default_height = block_height;

            // Get the top position of that block
            int top_position;
            top_position = previous_top_position + previous_block_height + shadow_height;

            if (is_overlapping) {
                // Overlap the previous block's shadow
                top_position -= shadow_height;
            }

            int current_block_height = current_block.getHeight(text_paint);

            // Set the previous stuff to this stuff, will be used later
            previous_top_position = top_position;
            previous_block_height = current_block_height;

            // Apply the bottom margin if this is a nested block
            if (current_block instanceof NestedBlock) {
                ((NestedBlock) current_block).bottom_margin = nested_bottom_margin;
            }

            // To optimize the drawing, check if this block is actually visible to the user
            if (top_position + current_block_height < 0) {
                // no, this block isn't visible, skip this
                top_optimize_cut++;
                continue;
            } else if (top_position > getHeight()) {
                // this block is too far down, skip this
                bottom_optimize_cut++;
                continue;
            } else {
                // This block is visible / being drawn, add this to the optimized blocks
                optimized_blocks.add(current_block);
            }

            // Oh yeah add the top_position to our top_positions array list
            top_positions.add(top_position);

            // Finally, draw our block
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
                        previous_block_color,
                        is_round,
                        round_radius
                );

            previous_block_color = current_block.color;
        }

        // After drawing all the blocks, let's draw the event / the yellow thing on the top
        event.draw(
                canvas,
                event_height,
                10,
                left_position,
                event_top,
                15,
                block_outset_left_margin,
                block_outset_width,
                shadow_height,
                rect_paint,
                text_paint,
                is_round,
                round_radius
        );


        // Null check
        if (unconnected_blocks == null)
            return;

        // Draw the unconnected blocks
        int index = 0;
        for (Pair<Vector2D, Block> block : unconnected_blocks) {
            Vector2D position = block.first.clone();
            position.x += left_position;
            position.y += event_top;

            // Important for certain APIs
            setLayerType(LAYER_TYPE_SOFTWARE, shadow_paint);

            if (index == picked_up_block_index) {
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
                    0x00000000,
                    is_round,
                    round_radius
            );

            index++;
        }

        // Draw the line where it indicates if we're dropping a block into the collection
        if (draw_line_at_pos != -1) {
            canvas.drawRect(left_position, draw_line_at_pos - 5, left_position + detection_distance_right, draw_line_at_pos + 5, line_paint);
        }

        // Draw the rect where it indicates if we're dropping a block into a field
        if (predicted_drop_field != null) {
            canvas.drawRect(predicted_drop_field, black_rect);
        }
    }



    // Utility classes =============================================================================

    /**
     * This class is used to store 2 dimensional coordinates
     */
    public static class Vector2D {
        public int x;
        public int y;

        public Vector2D(int x, int y) { this.x = x; this.y = y; }

        @NonNull
        @Override
        protected Vector2D clone() {
            return new Vector2D(this.x, this.y);
        }
    }
    // Utility classes =============================================================================



    // Interfaces ==================================================================================
    public interface FieldClick {
        void onFieldClick(BlockField field);
    }
    // Interfaces ==================================================================================
}
