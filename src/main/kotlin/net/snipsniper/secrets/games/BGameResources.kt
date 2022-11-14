package net.snipsniper.secrets.games

import net.snipsniper.utils.getImage
import java.awt.Image

class BGameResources {
    private val pieces = ArrayList<Array<IntArray>>()
    private val sprites = ArrayList<Image>()
    fun init() {
        pieces.add(arrayOf(
                intArrayOf(0, 0, 1, 0),
                intArrayOf(0, 0, 1, 0),
                intArrayOf(0, 0, 1, 0),
                intArrayOf(0, 0, 1, 0)
        ))
        sprites.add("icons/snipsniper.png".getImage())

        pieces.add(arrayOf(
            intArrayOf(1, 1),
            intArrayOf(1, 1)
        ))
        sprites.add("icons/editor.png".getImage())

        pieces.add(arrayOf(
            intArrayOf(1, 0, 0),
            intArrayOf(1, 0, 0),
            intArrayOf(1, 1, 0)
        ))
        sprites.add("icons/viewer.png".getImage())

        pieces.add(arrayOf(
            intArrayOf(1, 0, 0),
            intArrayOf(1, 1, 0),
            intArrayOf(1, 0, 0)
        ))
        sprites.add("icons/random/kiwi.png".getImage())

        pieces.add(arrayOf(
            intArrayOf(0, 1, 0),
            intArrayOf(0, 1, 0),
            intArrayOf(1, 1, 0)
        ))
        sprites.add("icons/console.png".getImage())

        pieces.add(arrayOf(
            intArrayOf(0, 1, 0),
            intArrayOf(1, 1, 0),
            intArrayOf(1, 0, 0)
        ))
        sprites.add("icons/about.png".getImage())

        pieces.add(arrayOf(
            intArrayOf(1, 0, 0),
            intArrayOf(1, 1, 0),
            intArrayOf(0, 1, 0)
        ))
        sprites.add("icons/folder.png".getImage())
    }

    fun getImage(index: Int) = sprites[index]

    fun getPiece(index: Int) = pieces[index]

    fun getSize() = pieces.size
}