/*
 * The MIT License (MIT)
 * Copyright Â© 2012 Steve Guidetti
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the â€œSoftwareâ€�), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED â€œAS ISâ€�, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ultramegatech.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import com.ultramegatech.ey.R;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Zoomable, color coded View of the Periodic Table of the Elements. Renders a list of
 * PeriodicTableBlock objects in the standard periodic table layout. Also implements a custom
 * OnItemClickListener that passes the selected PeriodicTableBlock object.
 */
public class PeriodicTableView extends View implements Observer {
    /**
     * Callback interface for click listeners
     */
    public interface OnItemClickListener {
        /**
         * Called when a block is clicked.
         * 
         * @param item The selected block
         */
        public void onItemClick(PeriodicTableBlock item);
    }
    
    /* The list of blocks to render */
    private List<PeriodicTableBlock> mPeriodicTableBlocks;
    
    /* Callback for item clicks */
    private OnItemClickListener mItemClickListener;
    
    /* Color legend */
    private final PeriodicTableLegend mLegend = new PeriodicTableLegend();
    
    /* Title string */
    private CharSequence mTitle;
    
    /* Block size in pixels in the zoomed out state */
    private int mBaseBlockSize = 20;
    
    /* Actual current block size */
    private int mBlockSize;
    
    /* Amount of space around the table */
    private int mPadding;
    
    /* Number of rows and columns in the table */
    private int mNumRows;
    private int mNumCols;
    
    /* Paint for block backgrounds */
    private Paint mBlockPaint;
    
    /* Paint for row and column headers */
    private Paint mHeaderPaint;
    
    /* Paint for symbols */
    private Paint mSymbolPaint;
    
    /* Paint for atomic numbers */
    private Paint mNumberPaint;
    
    /* Paint for the text below the symbol */
    private Paint mSmallTextPaint;
    
    /* Paint for the selection indicator */
    private Paint mSelectedPaint;
    
    /* This view's aspect quotient */
    private final AspectQuotient mAspectQuotient = new AspectQuotient();
    
    /* This view's zoom state */
    private ZoomState mState;
    
    /* Offsets for drawing on the canvas based on zoom state */
    private int mOffsetX;
    private int mOffsetY;
    
    /* Rectangle for many purposes */
    private final Rect mRect = new Rect();
    
    /* The currently selected block */
    private PeriodicTableBlock mBlockSelected;
    
    /**
     * Constructor
     * 
     * @param context 
     */
    public PeriodicTableView(Context context) {
        this(context, null, 0);
    }
    
    /**
     * Constructor
     * 
     * @param context
     * @param attrs 
     */
    public PeriodicTableView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    /**
     * Constructor
     * 
     * @param context
     * @param attrs
     * @param defStyle 
     */
    public PeriodicTableView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PeriodicTableView,
                defStyle, 0);
        
        mTitle = a.getText(R.styleable.PeriodicTableView_title);
        
        final int colorsArrayId = a.getResourceId(R.styleable.PeriodicTableView_legendColors, 0);
        if(colorsArrayId != 0) {
            final int[] legendColors = a.getResources().getIntArray(colorsArrayId);
            final CharSequence[] legendValues =
                    a.getTextArray(R.styleable.PeriodicTableView_legendValues);
            if(legendColors != null && legendValues != null
                    && legendColors.length >= legendValues.length) {
                final HashMap<Object, Integer> colorMap = new LinkedHashMap<Object, Integer>();

                for(int i = 0; i < legendValues.length; i++) {
                    colorMap.put(legendValues[i], legendColors[i]);
                }

                mLegend.setColorMap(colorMap);
            }
        }
        
        a.recycle();
        
        init();
    }
    
    /**
     * Setup helpers and listeners.
     */
    private void init() {
        final DynamicZoomControl zoomControl = new DynamicZoomControl();
        zoomControl.setAspectQuotient(mAspectQuotient);
        
        final PeriodicTableTouchListener touchListener =
                new PeriodicTableTouchListener(getContext());
        touchListener.setZoomControl(zoomControl);
        setOnTouchListener(touchListener);
        
        mState = zoomControl.getZoomState();
        mState.addObserver(this);
        
        mLegend.addObserver(this);
        
        setupPaints();
    }
    
    /**
     * Initialize and configure all Paint objects.
     */
    private void setupPaints() {
        mBlockPaint = new Paint();
        
        mSelectedPaint = new Paint(mBlockPaint);
        mSelectedPaint.setAntiAlias(true);
        mSelectedPaint.setStyle(Paint.Style.STROKE);
        mSelectedPaint.setStrokeJoin(Paint.Join.ROUND);
        mSelectedPaint.setColor(0x9900D4FF);
        
        mNumberPaint = new Paint();
        mNumberPaint.setAntiAlias(true);
        mNumberPaint.setSubpixelText(true);
        mNumberPaint.setColor(0xFF000000);
        
        mSymbolPaint = new Paint(mNumberPaint);
        mSymbolPaint.setTextAlign(Paint.Align.CENTER);
        
        mHeaderPaint = new Paint(mSymbolPaint);
        mSmallTextPaint = new Paint(mSymbolPaint);
    }
    
    /**
     * Set the list of blocks to be rendered. This method also determines the row and column of each
     * block and sets the colors using the legend.
     * 
     * @param blocks 
     */
    public void setBlocks(List<PeriodicTableBlock> blocks) {
        if(blocks.isEmpty()) {
            return;
        }
        
        int numRows = 0;
        int numCols = 0;
        
        for(PeriodicTableBlock block : blocks) {
            if(block.period > numRows) {
                numRows = block.period;
            }
            if(block.group > numCols) {
                numCols = block.group;
            }
            if(block.group == 0) {
                if(block.period == 6) {
                    block.row = 8;
                    block.col = block.number - 54;
                } else if(block.period == 7) {
                    block.row = 9;
                    block.col = block.number - 86;
                }
            } else {
                block.row = block.period;
                block.col = block.group;
            }
            
            mLegend.colorBlock(block);
        }
        numRows += 2;
        
        mNumRows = numRows;
        mNumCols = numCols;
        mPeriodicTableBlocks = blocks;
        
        requestLayout();
        invalidate();
    }
    
    /**
     * Get the color legend.
     * 
     * @return 
     */
    public PeriodicTableLegend getLegend() {
        return mLegend;
    }
    
    /**
     * Set the item click listener.
     * 
     * @param listener 
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }
    
    /**
     * Get the item click listener.
     * 
     * @return 
     */
    public OnItemClickListener getOnItemClickListener() {
        return mItemClickListener;
    }
    
    /**
     * Set the title from a string resource.
     * 
     * @param resid Resource id
     */
    public void setTitle(int resid) {
        setTitle(getResources().getText(resid));
    }
    
    /**
     * Set the title.
     * 
     * @param title 
     */
    public void setTitle(CharSequence title) {
        mTitle = title;
        invalidate();
    }
    
    /**
     * Get the title.
     * 
     * @return 
     */
    public CharSequence getTitle() {
        return mTitle;
    }
    
    /**
     * Called by the touch listener on a down event. Determines which block, if any, this click
     * occurred in and selects it.
     * 
     * @param x X coordinate
     * @param y Y coordinate
     */
    public void onDown(float x, float y) {
        if(mPeriodicTableBlocks == null) {
            return;
        }
        
        mBlockSelected = null;
        for(PeriodicTableBlock block : mPeriodicTableBlocks) {
            findBlockPosition(block);
            if(x > mRect.left && x < mRect.right && y > mRect.top && y < mRect.bottom) {
                mBlockSelected = block;
                break;
            }
        }
        
        invalidate();
    }
    
    /**
     * Clear the selected block.
     */
    public void clearSelection() {
        mBlockSelected = null;
        invalidate();
    }
    
    /**
     * Called by the touch listener on a click event. If an item has been selected, this method
     * calls the item click listener if one is set.
     */
    public void onClick() {
        if(mItemClickListener != null && mBlockSelected != null) {
            mItemClickListener.onItemClick(mBlockSelected);
        }
        clearSelection();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        
        final int width = right - left;
        final int height = bottom - top;
        
        if(mNumCols > 0 && mNumRows > 0) {
            mAspectQuotient.updateAspectQuotient(width, height, mNumCols, mNumRows);
            mAspectQuotient.notifyObservers();

            mBaseBlockSize = Math.min(width / mNumCols, height / mNumRows);
        }
    }
    
    /**
     * Determine if a block is within the visible region. This is used to avoid useless drawing
     * operations.
     * 
     * @param rect The block boundaries
     * @return True if the block is visible
     */
    private boolean isBlockVisible(Rect rect) {
        return (rect.right > getLeft() && rect.bottom > getTop()
                && rect.left < getRight() && rect.top < getBottom());
    }
    
    /**
     * Calculate the position of the specified block and store it in the shared rectangle.
     * 
     * @param block 
     */
    private void findBlockPosition(PeriodicTableBlock block) {
        mRect.right = (block.col * mBlockSize - mOffsetX + mPadding) - 1;
        mRect.bottom = (block.row * mBlockSize - mOffsetY + mPadding) - 1;
        mRect.left = mRect.right - mBlockSize + 1;
        mRect.top = mRect.bottom - mBlockSize + 1;
        
        final int number = block.number;
        if((number > 56 && number < 72) || (number > 88 && number < 104)) {
            mRect.top += mPadding / 2;
            mRect.bottom += mPadding / 2;
        }
    }
    
    /**
     * Draw the headers and placeholders on the supplied canvas.
     * 
     * @param canvas 
     */
    private void writeHeaders(Canvas canvas) {
        mHeaderPaint.setTextSize(mBlockSize / 4);

        for(int i = 1; i <= mNumCols; i++) {
            canvas.drawText(String.valueOf(i), mBlockSize * i - mOffsetX, mPadding / 2 - mOffsetY,
                    mHeaderPaint);
        }
        for(int i = 1; i <= mNumRows - 2; i++) {
            canvas.drawText(String.valueOf(i), mPadding / 2 - mOffsetX, mBlockSize * i - mOffsetY,
                    mHeaderPaint);
        }
        
        canvas.drawText("57-71", mBlockSize * 3 - mOffsetX,
                mBlockSize * 6 - mOffsetY + mHeaderPaint.getTextSize() / 2, mHeaderPaint);
        
        canvas.drawText("89-103", mBlockSize * 3 - mOffsetX,
                mBlockSize * 7 - mOffsetY + mHeaderPaint.getTextSize() / 2, mHeaderPaint);
    }
    
    /**
     * Draw the title on the supplied canvas.
     * 
     * @param canvas 
     */
    private void writeTitle(Canvas canvas) {
        if(mTitle != null) {
            canvas.drawText(mTitle, 0, mTitle.length(), mBlockSize * mNumCols / 2 - mOffsetX,
                    mBlockSize - mOffsetY, mSymbolPaint);
        }
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        if(mPeriodicTableBlocks != null && mState != null) {
            final double aspectQuotient = mAspectQuotient.get();
            mBlockSize = (int)(mState.getZoom() * mBaseBlockSize);
            mPadding = mBlockSize / 2;
            mBlockSize -= mBlockSize / Math.min(mNumCols, mNumRows);
            
            final int viewWidth = getWidth();
            final int viewHeight = getHeight();
            final int bitmapWidth = mBlockSize * mNumCols + mPadding * 2;
            final int bitmapHeight = mBlockSize * mNumRows + mPadding * 2;
            
            final double zoomX = mState.getZoomX(aspectQuotient) * viewWidth / bitmapWidth;
            final double zoomY = mState.getZoomY(aspectQuotient) * viewHeight / bitmapHeight;
            mOffsetX = (int)(mState.getPanX() * bitmapWidth - viewWidth / (zoomX * 2));
            mOffsetY = (int)(mState.getPanY() * bitmapHeight - viewHeight / (zoomY * 2));

            mSymbolPaint.setTextSize(mBlockSize / 2);
            mNumberPaint.setTextSize(mBlockSize / 4);
            mSmallTextPaint.setTextSize(mBlockSize / 5);
            
            mRect.top = (int)(mBlockSize * 1.3) - mOffsetY;
            mRect.left = mBlockSize * 4 - mOffsetX;
            mRect.bottom = mRect.top + mBlockSize * 2;
            mRect.right = mRect.left + mBlockSize * 8;
            mLegend.drawLegend(canvas, mRect);
            
            writeHeaders(canvas);
            writeTitle(canvas);
            
            for(PeriodicTableBlock block : mPeriodicTableBlocks) {
                findBlockPosition(block);
                
                if(!isBlockVisible(mRect)) {
                    continue;
                }
                
                mBlockPaint.setColor(block.color);

                canvas.drawRect(mRect, mBlockPaint);
                
                canvas.drawText(block.symbol, mRect.left + mBlockSize / 2,
                        mRect.bottom - (int)(mBlockSize / 2.8), mSymbolPaint);
                
                canvas.drawText(String.valueOf(block.number), mRect.left + mBlockSize / 20,
                        mRect.top + mNumberPaint.getTextSize(), mNumberPaint);
                
                canvas.drawText(block.subtext, mRect.left + mBlockSize / 2,
                        mRect.bottom - mBlockSize / 20, mSmallTextPaint);
            }
            
            if(mBlockSelected != null) {
                mSelectedPaint.setStrokeWidth(mBlockSize / 10);
                findBlockPosition(mBlockSelected);
                canvas.drawRect(mRect, mSelectedPaint);
            }
        }
    }

    public void update(Observable observable, Object data) {
        if(observable instanceof PeriodicTableLegend) {
            mLegend.colorBlocks(mPeriodicTableBlocks);
        }
        invalidate();
    }
}