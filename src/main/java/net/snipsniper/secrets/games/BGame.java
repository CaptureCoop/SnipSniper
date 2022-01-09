package net.snipsniper.secrets.games;

import net.snipsniper.ImageManager;
import net.snipsniper.LogManager;
import net.snipsniper.SnipSniper;
import net.snipsniper.StatsManager;
import net.snipsniper.systray.Sniper;
import net.snipsniper.utils.enums.LogLevel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class BGame extends JFrame {
    public final int INITIAL_SCREEN_WIDTH = 1024;
    public final int INITIAL_SCREEN_HEIGHT = 512;

    public final int BOARD_WIDTH = 10;
    public final int BOARD_HEIGHT = 20;

    private final int FALLSPEED_MAX_START = 500;

    private final int[] SCORES = {40, 100, 300, 1200};

    private final int LINES_BEFORE_LVLUP_ADD = 10;

    private BGameBlock[][] board;
    private final BGamePanel gamePanel = new BGamePanel(this);
    private BGamePiece cPiece;

    private boolean running = true;
    private boolean isPaused = false;

    private boolean[] keys;
    private BGameResources resources;

    private int score;
    private int level;
    private int rowsDone;
    private int rowsBeforeLevelUp = 10;

    private BGamePiece nextPiece;
    private boolean gameOver = false;
    private final Sniper sniper;

    private boolean hitDuringDownPress = false;

    public BGame(Sniper sniper) {
        this.sniper = sniper;
        StatsManager.incrementCount(StatsManager.BGAME_STARTED_AMOUNT);
        SnipSniper.getNewThread(args -> launch()).start();
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

                if(keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE && !gameOver)
                    isPaused = !isPaused;
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {
                super.keyReleased(keyEvent);
                keys[keyEvent.getKeyCode()] = false;
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(e.getButton() == 3) {
                    gamePanel.screenshot();
                }
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
                if(!gameOver) {
                    boolean isHit = false;
                    if(cPiece != null)
                        isHit = cPiece.update();

                    if(fallSpeed >= fallSpeedMax) {
                        if(isHit || hitDuringDownPress) {
                            cPiece.hit();
                            hitDuringDownPress = false;
                            spawnPiece();
                        }
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
                        score += SCORES[rows - 1] * scoreMultiplier;
                    }
                }
            }
            gamePanel.repaint();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                LogManager.log("Error sleeping thread for BGame!", LogLevel.ERROR);
                LogManager.logStacktrace(e, LogLevel.ERROR);
            }
        }
    }

    public void gameOver() {
        gameOver = true;
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
                for(int x = 0; x < BOARD_WIDTH; x++)
                    board[x][y] = null;

                for(int z = y; z > 0; z--)
                    for(int x = 0; x < BOARD_WIDTH; x++)
                        board[x][z] = board[x][z-1];
            }
        }
        return rowsCleared;
    }

    public static int randomRange(int min, int max) {
        return new Random().nextInt((max - min) + 1) + min;
    }

    public void spawnPiece() {
        StatsManager.incrementCount(StatsManager.BGAME_STARTED_SPAWNED_PIECES_AMOUNT);
        BGamePiece newPiece = new BGamePiece(this);
        if(gameOver) {
            cPiece = null;
            return;
        }
        if(nextPiece == null)
            cPiece = newPiece;
        else
            cPiece = nextPiece;

        nextPiece = new BGamePiece(this);
    }

    public int getTileSize() {
        return gamePanel.getHeight() / BOARD_HEIGHT;
    }

    public void start() {
        gameOver = false;
        board = new BGameBlock[BOARD_WIDTH][BOARD_HEIGHT];
        fallSpeedMax = FALLSPEED_MAX_START;
        nextPiece = null;
        keys = new boolean[25565];
        score = 0;
        rowsDone = 0;
        level = 0;
        spawnPiece();
    }

    int dropCooldown = 0;
    int dropCooldownMax = 25;
    public void input() {
        if(dropCooldown > 0)
            dropCooldown--;

        if(isPressed(KeyEvent.VK_R)) {
            start();
            return;
        }

        if(cPiece != null) {
            if(isPressedAny(KeyEvent.VK_SPACE, KeyEvent.VK_SHIFT) && dropCooldown == 0 && !gameOver) {
                for(int i = 0; i < BOARD_HEIGHT; i++) {
                    if(cPiece.moveDown())
                        break;
                }
                dropCooldown = dropCooldownMax;
            }

            if(isPressedAny(KeyEvent.VK_E, KeyEvent.VK_UP) && !gameOver)
                cPiece.rotate(1);
            if(isPressed(KeyEvent.VK_Q) && !gameOver)
                cPiece.rotate(-1);

            if(isPressedAny(KeyEvent.VK_A, KeyEvent.VK_LEFT) && !gameOver)
                cPiece.move(-1);
            if(isPressedAny(KeyEvent.VK_D, KeyEvent.VK_RIGHT) && !gameOver)
                cPiece.move(1);

            if(isPressedAny(KeyEvent.VK_S, KeyEvent.VK_DOWN) && !gameOver) {
                cPiece.moveDown();
                if(cPiece.checkCollision())
                    hitDuringDownPress = true;
            }
        }
    }

    public boolean isPressed(int keyCode) {
        return keys[keyCode];
    }

    public boolean isPressedAny(int... keyCodes) {
        for(int keyCode : keyCodes) {
            if(keys[keyCode])
                return true;
        }
        return false;
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

    public boolean isGameOver() {
        return gameOver;
    }

    public BGamePiece getNextPiece() {
        return nextPiece;
    }

    public Sniper getSniper() {
        return sniper;
    }
}
