package net.snipsniper.secrets.games;

import net.snipsniper.utils.ImageUtils;

import java.awt.*;
import java.awt.image.BufferedImage;

public class BGamePiece {
    private final BGame game;

    public int[][] figure;
    private int posX;
    private int posY = 1;

    private int rotationCooldown = 0;
    private final int rotationCooldownMax = 10;

    private int moveCooldown = 0;
    private final int moveCooldownMax = 10;

    public final int index;

    public BGamePiece(BGame game) {
        index = BGame.randomRange(0,game.getResources().getSize() - 1);
        //index = 0;
        figure = game.getResources().getPiece(index);
        this.game = game;

        posX = (this.game.BOARD_WIDTH/2) - figure[0].length/2;

        for(int y = 0; y < figure.length; y++) {
            for(int x = 0; x < figure[y].length; x++) {
                if(game.getBoard()[posX + x][posY + y] != null) {
                    game.gameOver();
                }
            }
        }
        
        //Important:
        //When looping through int[][] figure you need to do figure[y][x]
    }

    public boolean update() {
        if(rotationCooldown > 0)
            rotationCooldown--;
        if(moveCooldown > 0)
            moveCooldown--;

        return checkCollision();
    }

    public boolean moveDown() {
        if(!checkCollision()) {
            posY++;
            return false;
        }
        return true;
    }

    public BufferedImage getRawImage(int size) {
        BufferedImage image = new BufferedImage(figure[0].length * size, figure[1].length * size, BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.createGraphics();
        for(int y = 0; y < figure[0].length; y++) {
            for(int x = 0; x < figure.length; x++) {
                if(figure[y][x] != 0) {
                    g.drawImage(game.getResources().getImage(index), x * size, y * size, size, size, null);
                }
            }
        }
        g.dispose();
        return ImageUtils.Companion.trimImage(image);
    }

    //returns true if the piece hit something
    public boolean checkCollision() {
        for(int y = 0; y < figure[0].length; y++) {
            for(int x = 0; x < figure.length; x++) {
                if(figure[y][x] != 0) {
                    final int nPosX = posX + x;
                    final int nPosY = posY + y;
                    if(!(nPosX <= -1) && !(nPosX >= game.BOARD_WIDTH) && !(nPosY <= 0) && !(nPosY >= game.BOARD_HEIGHT - 1)) {
                        BGameBlock cBlock = game.getBoard()[posX + x][posY + y + 1];
                        if(cBlock != null)
                            return true;
                    }
                    if(posY + y >= game.BOARD_HEIGHT-1)
                        return true;
                }
            }
        }
        return false;
    }

    public void hit() {
        for(int y = 0; y < figure[0].length; y++) {
            for(int x = 0; x < figure.length; x++) {
                if(figure[y][x] != 0) {
                    final int nPosX = posX + x;
                    final int nPosY = posY + y;
                    if(!(nPosX < 0 || nPosX >= game.BOARD_WIDTH || nPosY < 0 || nPosY >= game.BOARD_HEIGHT))
                        game.getBoard()[posX + x][posY + y] = new BGameBlock(index);
                }
            }
        }
    }

    private boolean checkMoveCollision(int dir) {
        for(int y = 0; y < figure[0].length; y++) {
            for(int x = 0; x < figure.length; x++) {
                final int piecePosX = posX + x + dir;
                final int piecePosY = posY + y;

                if(figure[y][x] != 0) {
                    if(piecePosX <= -1)
                        return false;
                    if(piecePosX >= game.BOARD_WIDTH)
                        return false;
                    if(game.getBoard()[piecePosX][piecePosY] != null)
                        return false;
                }
            }
        }
        return true;
    }

    public void move(int dir) {
        if(dir == 1 && moveCooldown == 0) {
            if(checkMoveCollision(dir)) {
                posX++;
                moveCooldown = moveCooldownMax;
            }
        } else if (dir == -1 && moveCooldown == 0) {
            if(checkMoveCollision(dir)) {
                posX--;
                moveCooldown = moveCooldownMax;
            }
        }
    }

    private boolean checkRotation(int[][] newFigure) {
        for(int y = 0; y < newFigure[0].length; y++) {
            for(int x = 0; x < newFigure.length; x++) {
                final int nPosX = posX + x;
                final int nPosY = posY + y;
                if((nPosX <= -1) || (nPosX >= game.BOARD_WIDTH) || (nPosY <= 0) || (nPosY >= game.BOARD_HEIGHT - 1)) {
                    if(newFigure[y][x] == 1)
                        return false;
                }
                if(!(nPosX < 0 || nPosX >= game.BOARD_WIDTH || nPosY < 0 || nPosY >= game.BOARD_HEIGHT))
                    if(game.getBoard()[nPosX][nPosY] != null)
                        return false;
            }
        }
        return true;
    }

    public void rotate(int dir) {
        if(rotationCooldown == 0) {
            int[][] newFigure = rotateMatrix(figure, dir);
            if(checkRotation(newFigure)) {
                figure = newFigure;
                rotationCooldown = rotationCooldownMax;
            }
        }

    }

    private static int[][] rotateMatrix(int[][] figure2, int dir) {
        int[][] rotated = new int[figure2[0].length][figure2.length];
        for (int i = 0; i < figure2[0].length; ++i) {
            for (int j = 0; j < figure2.length; ++j) {
                if(dir == 1)
                    rotated[i][j] = figure2[figure2.length - j - 1][i];
                else if(dir == -1)
                    rotated[i][j] = figure2[j][figure2[0].length - i - 1];
            }
        }
        return rotated;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }
}
