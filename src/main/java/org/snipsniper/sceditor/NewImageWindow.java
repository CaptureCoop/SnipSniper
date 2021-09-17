package org.snipsniper.sceditor;

import org.snipsniper.colorchooser.ColorChooser;
import org.snipsniper.utils.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class NewImageWindow extends JFrame implements IClosable{
    private final NewImageWindow instance;
    private BufferedImage image;
    private Function onSubmit;
    private final ArrayList<IClosable> cWindows = new ArrayList<>();

    public NewImageWindow() {
        instance = this;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new GridBagLayout());
        setIconImage(Icons.getImage("icons/editor.png"));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                super.windowClosing(windowEvent);
                close();
                dispose();
            }
        });
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 5, 0, 5);
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        add(new JLabel("New Image", JLabel.CENTER), gbc);
        gbc.gridwidth = 1;
        add(new JLabel("Width", JLabel.RIGHT), gbc);
        gbc.gridx = 1;
        JTextField widthTextField = new JTextField("512");
        add(widthTextField, gbc);
        gbc.gridx = 0;
        add(new JLabel("Height", JLabel.RIGHT), gbc);
        gbc.gridx = 1;
        JTextField heightTextField = new JTextField("512");
        add(heightTextField, gbc);
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JButton pickResolution = new JButton("Use monitor resolution");
        pickResolution.addActionListener(e -> {
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            widthTextField.setText(gd.getDisplayMode().getWidth() + "");
            heightTextField.setText(gd.getDisplayMode().getHeight() + "");
        });
        add(pickResolution, gbc);
        SSColor color = new SSColor(Color.WHITE);
        GradientJButton colorButton= new GradientJButton("Color", color);
        colorButton.addActionListener(e -> cWindows.add(new ColorChooser(null, "Color", color, null, (int) getLocation().getX() + getWidth() / 2, (int) getLocation().getY() + getHeight() / 2, true)));
        add(colorButton, gbc);
        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> {
            String widthString = widthTextField.getText();
            String heightString = heightTextField.getText();
            if(!MathUtils.isInteger(widthString) || !MathUtils.isInteger(heightString)) {
                JOptionPane.showMessageDialog(instance, "Bad input! Not a valid number.", "Error", JOptionPane.ERROR_MESSAGE, new ImageIcon(Icons.getImage("icons/redx.png").getScaledInstance(32, 32, 0)));
            } else {
                int width = Integer.parseInt(widthString);
                int height = Integer.parseInt(heightString);
                image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = image.createGraphics();
                g.setPaint(color.getGradientPaint(width, height));
                g.fillRect(0, 0, width, height);
                g.dispose();
                onSubmit.run();
                dispose();
            }
        });
        add(submitButton, gbc);
        pack();
        setVisible(true);
    }

    public void setOnSubmit(Function function) {
        onSubmit = function;
    }

    public BufferedImage getImage() {
        return image;
    }

    @Override
    public void close() {
        for(IClosable wnd : cWindows)
            wnd.close();
    }
}
