
package com.ultramegatech.widget;

import java.util.Observable;

/**
 * Maintains the state of a view's zoom level and pan position.
 * 
 * @author Steve Guidetti
 */
public class ZoomState extends Observable {
    /* Zoom level, 1.0 being the level at which the content fits the view */
    private double mZoom;
    
    /* Coordinates of the zoom window center relative to the content */
    private double mPanX;
    private double mPanY;
    
    /**
     * Get the pan value in the X dimension.
     * 
     * @return 
     */
    public double getPanX() {
        return mPanX;
    }
    
    /**
     * Get the pan value in the Y dimension.
     * 
     * @return 
     */
    public double getPanY() {
        return mPanY;
    }
    
    /**
     * Get the zoom level.
     * 
     * @return 
     */
    public double getZoom() {
        return mZoom;
    }
    
    /**
     * Calculate the zoom value in the X dimension.
     * 
     * @param aspectQuotient Quotient of content and view aspect ratios
     * @return 
     */
    public double getZoomX(double aspectQuotient) {
        return Math.min(mZoom, mZoom * aspectQuotient);
    }
    
    /**
     * Calculate the zoom value in the Y dimension.
     * 
     * @param aspectQuotient Quotient of content and view aspect ratios
     * @return 
     */
    public double getZoomY(double aspectQuotient) {
        return Math.min(mZoom, mZoom / aspectQuotient);
    }
    
    /**
     * Set the pan value in the X dimension.
     * 
     * @param panX 
     */
    public void setPanX(double panX) {
        if(panX != mPanX) {
            mPanX = panX;
            setChanged();
        }
    }
    
    /**
     * Set the pan value in the Y dimension.
     * 
     * @param panY 
     */
    public void setPanY(double panY) {
        if(panY != mPanY) {
            mPanY = panY;
            setChanged();
        }
    }
    
    /**
     * Set the zoom level.
     * 
     * @param zoom 
     */
    public void setZoom(double zoom) {
        if(zoom != mZoom) {
            mZoom = zoom;
            setChanged();
        }
    }
}