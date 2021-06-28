package io.wollinger.snipsniper.configwindow;

import io.wollinger.snipsniper.sceditor.stamps.IStamp;
import io.wollinger.snipsniper.utils.Vector2Int;

import javax.swing.*;
import java.awt.*;

public class StampJPanel extends JPanel {
    private IStamp stamp;

    public void setStamp(IStamp stamp) {
        this.stamp = stamp;
    }

    public void paint(Graphics g) {
        super.paint(g);
        g.drawRect(0,0,getWidth()-1,getHeight()-1);

        if(stamp != null)
            stamp.render(g, null, new Vector2Int(getWidth()/2, getHeight()/2), new Double[]{1D, 1D}, false, false, 0);
    }

}
