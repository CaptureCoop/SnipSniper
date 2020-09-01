package snipsniper.config;

import java.awt.Color;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import snipsniper.Utils;

public class ColorSlider extends JSlider{

	private static final long serialVersionUID = 3928382135874431859L;
	/* 1 = r
	 * 2 = g
	 * 3 = b
	 */
	int color;
	ColorWindow cWnd;
	ColorSlider instance;
	
	public ColorSlider(int _color, ColorWindow _cWnd) {
		color = _color;
		cWnd = _cWnd;
		instance = this;
		
		this.setMaximum(255);
		if(color == 1) this.setValue(cWnd.cfgWnd.sniperInstance.cfg.borderColor.getRed());
		if(color == 2) this.setValue(cWnd.cfgWnd.sniperInstance.cfg.borderColor.getGreen());
		if(color == 3) this.setValue(cWnd.cfgWnd.sniperInstance.cfg.borderColor.getBlue());
		
		this.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				Color currentColor = cWnd.colorPreview.getBackground();
				Color newColor = null;
				if(color == 1) {
					newColor = new Color(instance.getValue(), currentColor.getGreen(), currentColor.getBlue());
					cWnd.rValue.setText(instance.getValue() + "");
				} else if(color == 2) {
					newColor = new Color(currentColor.getRed(), instance.getValue(), currentColor.getBlue());
					cWnd.gValue.setText(instance.getValue() + "");
				} else if(color == 3) {
					newColor = new Color(currentColor.getRed(), currentColor.getGreen(), instance.getValue());
					cWnd.bValue.setText(instance.getValue() + "");
				}
				cWnd.hex.setText(Utils.rgb2hex(newColor));
				cWnd.colorPreview.setBackground(newColor);
			}
		});
	}

}
