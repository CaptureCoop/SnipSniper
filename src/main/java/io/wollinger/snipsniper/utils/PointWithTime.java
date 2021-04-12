package io.wollinger.snipsniper.utils;

import java.awt.*;

public class PointWithTime {
    private Point point;
    private long time;

    public PointWithTime(PointWithTime point) {
        this.point = point.getPoint();
        this.time = point.getTime();
    }

    public PointWithTime(Point point, long time) {
        this.point = point;
        this.time = time;
    }

    public Point getPoint() {
        return point;
    }

    public long getTime() {
        return time;
    }
}
