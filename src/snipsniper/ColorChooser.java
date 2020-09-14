package snipsniper;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.colorchooser.AbstractColorChooserPanel;

public class ColorChooser extends JFrame{
	private static final long serialVersionUID = 8590714455238968415L;

	private JColorChooser jcc;
	
	private PBRColor color = null;
	
	ColorChooser instance;
	
	public ColorChooser(String _title, PBRColor _color) {
		color = _color;
		instance = this;
		this.setTitle(_title);
		init();
	}
	
	public void close() {
		color.c = jcc.getColor();
		this.dispose();
	}
	
	void init() {
		jcc = new JColorChooser();
		jcc.setColor(color.c);
        AbstractColorChooserPanel[] panels = jcc.getChooserPanels();
        jcc.setPreviewPanel(new JPanel());
        for (AbstractColorChooserPanel accp : panels) {
            if (!accp.getDisplayName().equals("RGB")) {
                jcc.removeChooserPanel(accp);
            }
        }
        JPanel mainmain = new JPanel();
        JPanel colorPanel = new JPanel();
        JPanel submitButtonPanel = new JPanel();
        JButton submit = new JButton("Okay");
        submit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				instance.close();
			}
        });
       
        mainmain.setLayout(new BoxLayout(mainmain, BoxLayout.Y_AXIS));
        
        colorPanel.add(jcc);
        
        mainmain.add(colorPanel);
        mainmain.add(submitButtonPanel);
        
        this.add(mainmain);
        this.setResizable(false);
        this.setFocusable(true);
        
        this.setAlwaysOnTop(true);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        this.pack();
        submit.setPreferredSize(new Dimension(this.getWidth()/2, 50));
        submitButtonPanel.add(submit);

        this.pack();
        int w = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getWidth();
		int h = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getHeight();
		this.setLocation((w/2) - this.getWidth()/2, (h/2) - this.getHeight()/2);
        this.setVisible(true);
	}
}
