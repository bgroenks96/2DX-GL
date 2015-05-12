package com.snap2d;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

public class URLImageRef implements ImageRef {

    private final URL imgURL;

    private BufferedImage image;
    private volatile boolean loaded;

    public URLImageRef(final URL url) {
        this(url, false);
    }

    public URLImageRef(final URL url, final boolean lazyLoad) {
        this.imgURL = url;
        if (lazyLoad) return;
        update();
    }

    @Override
    public BufferedImage asBufferedImage() {
        if ( !loaded) update();
        return image;
    }

    @Override
    public int imageWidth() {
        return loaded ? image.getWidth() : 0;
    }

    @Override
    public int imageHeight() {
        return loaded ? image.getHeight() : 0;
    }

    @Override
    public void update() {
        BufferedImage img = new BufferedImage(0, 0, BufferedImage.TYPE_INT_ARGB);
        try {
            img = ImageIO.read(imgURL);
        } catch (IOException e) {
            e.printStackTrace();
        }
        image = img;
    }
}
