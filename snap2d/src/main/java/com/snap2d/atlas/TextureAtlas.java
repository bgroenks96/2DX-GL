package com.snap2d.atlas;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import com.snap2d.ImageRef;

public class TextureAtlas implements ImageRef {

    private final int width, height;
    private final Set<Rectangle> innerTexBounds;
    private final Set<InnerTextureData> innerTexData;

    private BufferedImage atlasImage;
    private boolean invalidated = false;

    public TextureAtlas(int width, int height) {
        this.width = width;
        this.height = height;
        this.innerTexBounds = new HashSet<>();
        innerTexData = new HashSet<>();
        atlasImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        atlasImage.setAccelerationPriority(1.0f);
    }

    /**
     * Adds an ImageRef to this atlas. The position of the image in the atlas
     * will be algorithmically determined when TextureAtlas is updated.
     * 
     * @param name
     * @param img
     */
    public void addImageToAtlas(String name, ImageRef img) {
        InnerTextureData data = new InnerTextureData();
        data.imgRef = img;
        data.name = name;
    }

    /**
     * Removes the sub-image added to this atlas with the given 'name'
     * 
     * @param name
     */
    public void removeImageFromAtlas(String name) {
        innerTexData.remove(name);
    }

    @Override
    public BufferedImage asBufferedImage() {
        return atlasImage;
    }

    @Override
    public int imageWidth() {
        return width;
    }

    @Override
    public int imageHeight() {
        return height;
    }

    @Override
    public void update() {
        layoutAndRepaintAtlas();
    }

    private void layoutAndRepaintAtlas() {

    }

    private class InnerTextureData {
        ImageRef imgRef;
        String name;
        Rectangle bounds;

        // Override hashCode and equals so that InnerTextureData can be
        // identified solely by its name.

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || ! (obj instanceof InnerTextureData)) return false;
            return ((InnerTextureData) obj).name.equals(this.name);
        }
    }
}
