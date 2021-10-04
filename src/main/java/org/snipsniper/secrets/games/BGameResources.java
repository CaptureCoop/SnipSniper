package org.snipsniper.secrets.games;

import org.snipsniper.ImageManager;

import java.awt.*;
import java.util.ArrayList;

public class BGameResources {
    private final ArrayList<int[][]> pieces = new ArrayList<>();
    private final ArrayList<Image> sprites = new ArrayList<>();

    public void init() {
        pieces.add(new int[][]{
                {0,0,1,0},
                {0,0,1,0},
                {0,0,1,0},
                {0,0,1,0}
        });
        sprites.add(ImageManager.getImage("icons/snipsniper.png"));

        pieces.add(new int[][]{
                {1,1},
                {1,1}
        });
        sprites.add(ImageManager.getImage("icons/editor.png"));

        pieces.add(new int[][]{
                {1,0,0},
                {1,0,0},
                {1,1,0}
        });
        sprites.add(ImageManager.getImage("icons/viewer.png"));

        pieces.add(new int[][]{
                {1,0,0},
                {1,1,0},
                {1,0,0}
        });
        sprites.add(ImageManager.getImage("icons/random/kiwi.png"));

        pieces.add(new int[][]{
                {0,1,0},
                {0,1,0},
                {1,1,0}
        });
        sprites.add(ImageManager.getImage("icons/console.png"));

        pieces.add(new int[][]{
                {0,1,0},
                {1,1,0},
                {1,0,0}
        });
        sprites.add(ImageManager.getImage("icons/about.png"));

        pieces.add(new int[][]{
                {1,0,0},
                {1,1,0},
                {0,1,0}
        });
        sprites.add(ImageManager.getImage("icons/folder.png"));
    }

    public Image getImage(int index) {
        return sprites.get(index);
    }

    public int[][] getPiece(int index) {
        return pieces.get(index);
    }

    public int getSize() {
        return pieces.size();
    }
}
