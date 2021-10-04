package org.snipsniper.secrets.games;

import org.snipsniper.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class BGamePanel extends JPanel {
    private final BGame game;

    public BGamePanel(BGame game) {
        this.game = game;
    }

    public void screenshot() {
        BufferedImage screenshot = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = screenshot.createGraphics();
        render(g, true);
        g.dispose();
        Utils.saveImage(screenshot, "_bgame", game.getSniper().getConfig());
    }

    public void render(Graphics g, boolean isScreenshot) {
        final int ts = game.getTileSize();
        g.clearRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.CYAN);
        g.fillRect(0, 0, getWidth(), getHeight());
        int offsetX = (getWidth()/2) - (game.BOARD_WIDTH * ts)/2;
        g.setColor(Color.BLACK);
        if(game.getCurrentPiece() != null) {
            BGamePiece cp = game.getCurrentPiece();
            for(int y = 0; y < cp.figure[0].length; y++) {
                for(int x = 0; x < cp.figure.length; x++) {
                    if(cp.figure[y][x] != 0) {
                        g.drawImage(game.getResources().getImage(cp.index), offsetX + (cp.getPosX() + x) * ts, (cp.getPosY() + y) * ts, ts, ts, null);
                    }
                }
            }
        }

        for(int y = 0; y < game.BOARD_HEIGHT; y++) {
            for(int x = 0; x < game.BOARD_WIDTH; x++) {
                if(game.getBoard() != null) {
                    BGameBlock cBlock = game.getBoard()[x][y];
                    if (cBlock != null) {
                        g.drawImage(game.getResources().getImage(cBlock.index), offsetX + x * ts, y * ts, ts, ts, null);
                        g.setColor(Color.BLACK);
                    }
                    g.drawRect(offsetX + x * ts, y * ts, ts, ts);
                }
            }
        }

        drawScoreText(g, offsetX, ts, 0, "Level");
        drawScoreText(g, offsetX, ts, 1, game.getLevel() + "");
        drawScoreText(g, offsetX, ts, 3, "Score");
        drawScoreText(g, offsetX, ts, 4, game.getScore() + "");
        drawScoreText(g, offsetX, ts, 6, "Lines cleared");
        drawScoreText(g, offsetX, ts, 7, game.getLinesCleared() + "");
        int offY = drawScoreText(g, offsetX, ts, 9, "Next piece:");
        int npX = getWidth() - (offsetX + game.BOARD_WIDTH * ts) + game.BOARD_WIDTH * ts;
        int npOffsetX = getWidth() - npX;

        BGamePiece np = game.getNextPiece();
        if(np != null) {
            BufferedImage npPreview = np.getRawImage(ts);
            g.drawImage(npPreview, npX + npOffsetX / 2 - npPreview.getWidth() / 2, offY + ts / 2, null);
        }
        drawHelpText(g, offsetX, 0, "Block Game", 1.5F);
        drawHelpText(g, offsetX, 2, "Controls:", 1.1F);
        drawHelpText(g, offsetX, 4, "Q / E = Rotate", 1);
        drawHelpText(g, offsetX, 5, "A / D = Move", 1);
        drawHelpText(g, offsetX, 6, "S = Faster", 1);
        drawHelpText(g, offsetX, 7, "Space = Drop", 1);
        drawHelpText(g, offsetX, 8, "Escape = Pause", 1);
        drawHelpText(g, offsetX, 9, "R = Restart", 1);
        drawHelpText(g, offsetX, 10, "Right Click = Screenshot", 1);

        if(game.isGameOver() && !isScreenshot) {
            g.setColor(new Color(0,0,0,100));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.WHITE);
            drawCenteredString(g, "GAME OVER", new Rectangle(0,0, getWidth(), getHeight()), new Font("Monospaced", Font.BOLD, getHeight()/20));
        }

        if(game.isPaused() && !isScreenshot) {
            g.setColor(new Color(0,0,0,100));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.WHITE);
            drawCenteredString(g, "PAUSED", new Rectangle(0,0, getWidth(), getHeight()), new Font("Monospaced", Font.BOLD, getHeight()/20));
        }
    }

    @Override
    public void paint(Graphics g) {
        render(g, false);
    }

    //Returns Y
    public int drawScoreText(Graphics g, int offsetX, int ts, int index, String text) {
        int height = getHeight() / 20;
        Rectangle rect = new Rectangle(offsetX + game.BOARD_WIDTH * ts, height * index, getWidth() - (offsetX + game.BOARD_WIDTH * ts), height);
        drawCenteredString(g, text, rect, new Font("Monospaced", Font.BOLD, height));
        return rect.y + rect.height;
    }

    public void drawHelpText(Graphics g, int offsetX, int index, String text, float fontMultiplier) {
        float height = getHeight() / 20F;
        height *= fontMultiplier;
        Rectangle rect = new Rectangle(0, (int)height * index, offsetX, (int)height);
        drawCenteredString(g, text, rect, new Font("Monospaced", Font.BOLD, (int)height));
    }

    public void drawCenteredString(Graphics g, String text, Rectangle rect, Font font ) {
        FontMetrics metrics = g.getFontMetrics(font);
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        g.setFont(font);
        g.drawString(text, x, y);
    }
}
