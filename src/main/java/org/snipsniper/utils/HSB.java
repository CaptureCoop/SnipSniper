package org.snipsniper.utils;

import java.awt.*;

public class HSB {
    private float hue;
    private float saturation;
    private float brightness;
    private int alpha;

    public HSB(float hue, float saturation, float brightness) {
        load(hue, saturation, brightness, 255);
    }

    public HSB(float hue, float saturation, float brightness, int alpha) {
        load(hue, saturation, brightness, alpha);
    }

    public HSB(Color color) {
        float[] values = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getBlue(), color.getGreen(), values);
        load(values[0], values[1], values[2], color.getAlpha());
    }

    private void load(float hue, float saturation, float brightness, int alpha) {
        this.hue = hue;
        this.saturation = saturation;
        this.brightness = brightness;
        this.alpha = alpha;
    }

    public Color toRGB() {
        Color tempColor = new Color(Color.HSBtoRGB(hue, saturation, brightness));
        return new Color(tempColor.getRed(), tempColor.getGreen(), tempColor.getBlue(), alpha);
    }

    public float getHue() {
        return hue;
    }

    public float getSaturation() {
        return saturation;
    }

    public float getBrightness() {
        return brightness;
    }

    public int getAlpha() {
        return alpha;
    }
}
