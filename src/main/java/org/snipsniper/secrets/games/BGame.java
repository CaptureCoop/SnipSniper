package org.snipsniper.secrets.games;

import org.snipsniper.ImageManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Random;

public class BGame extends JFrame {
    public final int INITIAL_SCREEN_WIDTH = 512;
    public final int INITIAL_SCREEN_HEIGHT = 512;

    public final int BOARD_WIDTH = 10;
    public final int BOARD_HEIGHT = 20;

    private final int FALLSPEED_MAX_START = 500;

    private final int SCORE_1ROW = 40;
    private final int SCORE_2ROW = 100;
    private final int SCORE_3ROW = 300;
    private final int SCORE_4ROW = 1200;

    private final int LINES_BEFORE_LVLUP_ADD = 10;


    private BGameBlock[][] board;
    private final BGamePanel gamePanel = new BGamePanel(this);
    private BGamePiece cPiece;

    private boolean running = true;
    private boolean isPaused = false;

    private final boolean[] keys = new boolean[25565];
    private BGameResources resources;

    private int score;
    private int level;
    private int rowsDone;
    private int rowsBeforeLevelUp = 10;


    public BGame() {
        Thread gameThread = new Thread(() -> launch());
        gameThread.start();
    }

    private void launch() {
        resources = new BGameResources();
        resources.init();
        setTitle("Block Game");
        add(gamePanel);
        setIconImage(ImageManager.getImage("icons/random/kiwi.png"));
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                super.keyPressed(keyEvent);
                keys[keyEvent.getKeyCode()] = true;

                if(keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
                    isPaused = !isPaused;
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {
                super.keyReleased(keyEvent);
                keys[keyEvent.getKeyCode()] = false;
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                running = false;
                dispose();
            }
        });
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setVisible(true);
        gamePanel.setPreferredSize(new Dimension(INITIAL_SCREEN_WIDTH, INITIAL_SCREEN_HEIGHT));
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) screenSize.getWidth();
        int height = (int) screenSize.getHeight();
        setLocation(width / 2 - getWidth() / 2, height / 2 - getHeight() / 2);
        start();
        loop();
    }

    int fallSpeed = 0;
    int fallSpeedMax = FALLSPEED_MAX_START;
    public void loop() {
        while(running) {
            final int ts = getTileSize();
            setMinimumSize(new Dimension(BOARD_WIDTH *ts, BOARD_HEIGHT * ts));
            if(!isPaused) {
                input();
                boolean isHit = false;
                if(cPiece != null)
                    isHit = cPiece.update();

                if(fallSpeed >= fallSpeedMax) {
                    if(isHit) cPiece.hit();

                    if(cPiece != null)
                        cPiece.moveDown();
                    fallSpeed = 0;
                } else {
                    fallSpeed += 10;
                }

                int rows = checkRows();
                if(rows != 0) {
                    rowsDone += rows;
                    if(rowsDone >= rowsBeforeLevelUp) {
                        rowsBeforeLevelUp += LINES_BEFORE_LVLUP_ADD;
                        level += 1;
                        fallSpeedMax -= 25;
                    }
                    int scoreMultiplier = level + 1;
                    switch(rows) {
                        case 1: score += SCORE_1ROW * scoreMultiplier; break;
                        case 2: score += SCORE_2ROW * scoreMultiplier; break;
                        case 3: score += SCORE_3ROW * scoreMultiplier; break;
                        case 4: score += SCORE_4ROW * scoreMultiplier; break;
                    }
                }
            }
            gamePanel.repaint();

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public int checkRows() {
        int rowsCleared = 0;
        for(int y = 0; y < BOARD_HEIGHT; y++) {
            boolean hasFull = true;
            for(int x = 0; x < BOARD_WIDTH; x++) {
                if (board[x][y] == null) {
                    hasFull = false;
                    break;
                }
            }
            if(hasFull) {
                rowsCleared++;
                for(int x = 0; x < BOARD_WIDTH; x++) {
                    board[x][y] = null;
                }

                for(int z = y; z > 0; z--) {
                    for(int x = 0; x < BOARD_WIDTH; x++) {
                        board[x][z] = board[x][z-1];
                    }
                }
            }
        }
        return rowsCleared;
    }

    public static int randomRange(int min, int max) {
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    public void spawnPiece() {
        cPiece = new BGamePiece(this);
    }

    public int getTileSize() {
        return gamePanel.getHeight() / BOARD_HEIGHT;
    }

    public void start() {
        board = new BGameBlock[BOARD_WIDTH][BOARD_HEIGHT];
        fallSpeedMax = FALLSPEED_MAX_START;
        spawnPiece();
    }

    int dropCooldown = 0;
    int dropCooldownMax = 25;
    public void input() {
        if(dropCooldown > 0)
            dropCooldown--;

        if(cPiece != null) {
            if(isPressed(KeyEvent.VK_SPACE) && dropCooldown == 0) {
                for(int i = 0; i < BOARD_HEIGHT; i++) {
                    if(cPiece.moveDown())
                        break;
                }
                dropCooldown = dropCooldownMax;
            }

            if(isPressed(KeyEvent.VK_E))
                cPiece.rotate(1);
            if(isPressed(KeyEvent.VK_Q))
                cPiece.rotate(-1);

            if(isPressed(KeyEvent.VK_A))
                cPiece.move(-1);
            if(isPressed(KeyEvent.VK_D))
                cPiece.move(1);

            if(isPressed(KeyEvent.VK_R))
                start();

            if(isPressed(KeyEvent.VK_S)) {
                cPiece.moveDown();
                if(cPiece.checkCollision()) cPiece.hit();
            }
        }
    }

    public boolean isPressed(int keyCode) {
        return keys[keyCode];
    }

    public BGameResources getResources() {
        return resources;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public BGamePiece getCurrentPiece() {
        return cPiece;
    }

    public BGameBlock[][] getBoard() {
        return board;
    }

    public int getScore() {
        return score;
    }

    public int getLevel() {
        return level;
    }

    public int getLinesCleared() {
        return rowsDone;
    }
}
