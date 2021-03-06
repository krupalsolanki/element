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
 * Simulates gliding motion based on a specified amount of friction.
 */
public class GlideDynamics extends Dynamics {
    /* Current friction */
    private double mFriction;
    
    /**
     * Set the friction value.
     * 
     * @param friction 
     */
    public void setFriction(double friction) {
        mFriction = friction;
    }

    @Override
    protected void onUpdate(int dt) {
        final double fdt = dt / 1000d;
        final double a = -mFriction * mVelocity;
        final double newPosition = mPosition + (mVelocity * fdt + 0.5 * a * fdt * fdt);
        
        if(newPosition > mMaxPosition) {
            mPosition = mMaxPosition;
            mVelocity = 0d;
        } else if(newPosition < mMinPosition) {
            mPosition = mMinPosition;
            mVelocity = 0d;
        } else {
            mPosition = newPosition;
            mVelocity += a * fdt;
        }
    }
}