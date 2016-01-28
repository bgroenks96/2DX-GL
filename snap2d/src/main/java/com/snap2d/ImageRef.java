package com.snap2d;

import java.awt.image.BufferedImage;

import javax.annotation.Nonnull;

/**
 * A high level interface for dealing with any kind of image that can be loaded
 * as an AWT BufferedImage. The image may come from any source and be of any
 * type (even another ImageRef).
 * 
 * @author Brian Groenke
 *
 */
public interface ImageRef {

    /**
     * Fetches the data of the image referenced by this ImageRef and returns it
     * as a BufferedImage. Note that all implementations of ImageRef are
     * required to have loaded the image data upon first call to this method.
     * This method should never return null.
     * 
     * @return the image referenced by this ImageRef as a BufferedImage
     */
    @Nonnull
    BufferedImage asBufferedImage();

    /**
     * @return width of the referenced image
     */
    int imageWidth();

    /**
     * @return height of the referenced image
     */
    int imageHeight();

    /**
     * Updates any data currently cached by this ImageRef so it reflects the
     * current state of the referenced image. This method is not required for
     * implementation.
     */
    void update();
}
