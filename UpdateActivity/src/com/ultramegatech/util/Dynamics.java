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

package com.ultramegatech.util;

/**
 * Utility to calculate the movement of an object. Implementers determine the rules of motion.

 */
public abstract class Dynamics {
    /* Maximum time difference between updates */
    private static final int MAX_TIMESTEP = 50;
    
    /* Current position */
    protected double mPosition;
    
    /* Current velocity */
    protected double mVelocity;
    
    /* Position limits */
    protected double mMaxPosition = Double.MAX_VALUE;
    protected double mMinPosition = -Double.MAX_VALUE;
    
    /* The time of the last update */
    protected long mLastTime = 0;
    
    /**
     * Set the state of the dynamics object. Should be called before starting to call update.
     * 
     * @param position The current position
     * @param velocity The current velocity in pixels per second
     * @param now The current time
     */
    public void setState(final double position, final double velocity, final long now) {
        mVelocity = velocity;
        mPosition = position;
        mLastTime = now;
    }
    
    /**
     * Get the current position.
     * 
     * @return 
     */
    public double getPosition() {
        return mPosition;
    }
    
    /**
     * Get the current velocity in pixels per second.
     * 
     * @return 
     */
    public double getVelocity() {
        return mVelocity;
    }
    
    /**
     * Test if the dynamic object is considered to be at rest.
     * 
     * @param velocityTolerance Minimum velocity
     * @param positionTolerance Minimum distance from limits
     * @return 
     */
    public boolean isAtRest(final double velocityTolerance, final double positionTolerance) {
        final boolean standingStill = Math.abs(mVelocity) < velocityTolerance;
        final boolean withinLimits = mPosition - positionTolerance < mMaxPosition
                && mPosition + positionTolerance > mMinPosition;
        return standingStill && withinLimits;
    }
    
    /**
     * Set the maximum position.
     * 
     * @param maxPosition 
     */
    public void setMaxPosition(final double maxPosition) {
        mMaxPosition = maxPosition;
    }
    
    /**
     * Set the minimum position.
     * 
     * @param minPosition 
     */
    public void setMinPosition(final double minPosition) {
        mMinPosition = minPosition;
    }
    
    /**
     * Update the position and velocity.
     * 
     * @param now The current time
     */
    public void update(final long now) {
        int dt = (int)(now - mLastTime);
        if(dt > MAX_TIMESTEP) {
            dt = MAX_TIMESTEP;
        }
        
        onUpdate(dt);
        
        mLastTime = now;
    }
    
    /**
     * Update the position and velocity.
     * 
     * @param dt Time elapsed since the previous update
     */
    protected abstract void onUpdate(int dt);
}