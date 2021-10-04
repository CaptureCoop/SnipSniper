package org.snipsniper.secrets.games;

import javax.swing.*;
import java.awt.*;

public class BGamePanel extends JPanel {
    private final BGame game;

    public BGamePanel(BGame game) {
        this.game = game;
    }

    @Override
    public void paint(Graphics g) {
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
        int offY = drawScoreText(g, offsetX, ts, 7, game.getLinesCleared() + "");
        int npX = getWidth() - (offsetX + game.BOARD_WIDTH * ts) + game.BOARD_WIDTH * ts;
        int npOffsetX = getWidth() - npX;

        BGamePiece np = game.getNextPiece();
        int npWidth = game.getResources().getTrueWidth(np.index) * ts;
        
        for(int y = 0; y < np.figure[0].length; y++) {
            for(int x = 0; x < np.figure.length; x++) {
                if(np.figure[y][x] != 0) {
                    g.drawImage(game.getResources().getImage(np.index), npOffsetX / 2 - npWidth / 2 + npX + x * ts, offY + y * ts, ts, ts, null);
                }
            }
        }

        if(game.isPaused()) {
            g.setColor(new Color(0,0,0,100));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.WHITE);
            drawCenteredString(g, "PAUSED", new Rectangle(0,0, getWidth(), getHeight()), new Font("Monospaced", Font.BOLD, getHeight()/20));
        }
    }

    //Returns Y
    public int drawScoreText(Graphics g, int offsetX, int ts, int index, String text) {
        int height = getHeight() / 20;
        Rectangle rect = new Rectangle(offsetX + game.BOARD_WIDTH * ts, height * index, getWidth() - (offsetX + game.BOARD_WIDTH * ts), height);
        drawCenteredString(g, text, rect, new Font("Monospaced", Font.BOLD, height));
        return rect.y + rect.height;
    }

    public void drawCenteredString(Graphics g, String text, Rectangle rect, Font font ) {
        FontMetrics metrics = g.getFontMetrics(font);
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        g.setFont(font);
        g.drawString(text, x, y);
    }
}
