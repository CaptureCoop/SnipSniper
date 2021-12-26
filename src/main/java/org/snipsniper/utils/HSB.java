package org.snipsniper.utils;

import java.awt.*;

public class HSB {
    private float hue;
    private float saturation;
    private float brightness;

    public HSB(float hue, float saturation, float brightness) {
        load(hue, saturation, brightness);
    }

    public HSB(Color color) {
        float[] values = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getBlue(), color.getGreen(), values);
        load(values[0], values[1], values[2]);
    }

    private void load(float hue, float saturation, float brightness) {
        this.hue = hue;
        this.saturation = saturation;
        this.brightness = brightness;
    }

    public Color toRGB() {
        return new Color(Color.HSBtoRGB(hue, saturation, brightness));
    }
}
