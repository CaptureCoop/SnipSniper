package net.snipsniper.secrets.games

import net.snipsniper.utils.ImageUtils.Companion.trimImage
import java.awt.Graphics
import java.awt.image.BufferedImage

class BGamePiece(game: BGame) {
    private val game: BGame
    var figure: Array<IntArray>
    var posX: Int
        private set
    var posY = 1
        private set
    private var rotationCooldown = 0
    private val rotationCooldownMax = 10
    private var moveCooldown = 0
    private val moveCooldownMax = 10
    val index: Int

    init {
        index = BGame.randomRange(0, game.resources.getSize() - 1)
        //index = 0;
        figure = game.resources.getPiece(index)
        this.game = game
        posX = this.game.BOARD_WIDTH / 2 - figure[0].size / 2
        for (y in figure.indices) {
            for (x in figure[y].indices) {
                if (game.board[posX + x][posY + y] != null) {
                    game.gameOver()
                }
            }
        }

        //Important:
        //When looping through int[][] figure you need to do figure[y][x]
    }

    fun update(): Boolean {
        if (rotationCooldown > 0) rotationCooldown--
        if (moveCooldown > 0) moveCooldown--
        return checkCollision()
    }

    fun moveDown(): Boolean {
        if (!checkCollision()) {
            posY++
            return false
        }
        return true
    }

    fun getRawImage(size: Int): BufferedImage {
        val image = BufferedImage(figure[0].size * size, figure[1].size * size, BufferedImage.TYPE_INT_ARGB)
        val g: Graphics = image.createGraphics()
        for (y in figure[0].indices) {
            for (x in figure.indices) {
                if (figure[y][x] != 0) {
                    g.drawImage(game.resources.getImage(index), x * size, y * size, size, size, null)
                }
            }
        }
        g.dispose()
        return trimImage(image)
    }

    //returns true if the piece hit something
    fun checkCollision(): Boolean {
        for (y in figure[0].indices) {
            for (x in figure.indices) {
                if (figure[y][x] != 0) {
                    val nPosX = posX + x
                    val nPosY = posY + y
                    if (nPosX > -1 && nPosX < game.BOARD_WIDTH && nPosY > 0 && nPosY < game.BOARD_HEIGHT - 1) {
                        val cBlock = game.board[posX + x][posY + y + 1]
                        if (cBlock != null) return true
                    }
                    if (posY + y >= game.BOARD_HEIGHT - 1) return true
                }
            }
        }
        return false
    }

    fun hit() {
        for (y in figure[0].indices) {
            for (x in figure.indices) {
                if (figure[y][x] != 0) {
                    val nPosX = posX + x
                    val nPosY = posY + y
                    if(!(nPosX < 0 || nPosX >= game.BOARD_WIDTH || nPosY < 0 || nPosY >= game.BOARD_HEIGHT))
                        game.board[posX + x][posY + y] = BGameBlock(index);
                }
            }
        }
    }

    private fun checkMoveCollision(dir: Int): Boolean {
        for (y in figure[0].indices) {
            for (x in figure.indices) {
                val piecePosX = posX + x + dir
                val piecePosY = posY + y
                if (figure[y][x] != 0) {
                    if (piecePosX <= -1) return false
                    if (piecePosX >= game.BOARD_WIDTH) return false
                    if (game.board[piecePosX][piecePosY] != null) return false
                }
            }
        }
        return true
    }

    fun move(dir: Int) {
        if (dir == 1 && moveCooldown == 0) {
            if (checkMoveCollision(dir)) {
                posX++
                moveCooldown = moveCooldownMax
            }
        } else if (dir == -1 && moveCooldown == 0) {
            if (checkMoveCollision(dir)) {
                posX--
                moveCooldown = moveCooldownMax
            }
        }
    }

    private fun checkRotation(newFigure: Array<IntArray>): Boolean {
        for (y in newFigure[0].indices) {
            for (x in newFigure.indices) {
                val nPosX = posX + x
                val nPosY = posY + y
                if (nPosX <= -1 || nPosX >= game.BOARD_WIDTH || nPosY <= 0 || nPosY >= game.BOARD_HEIGHT - 1) {
                    if (newFigure[y][x] == 1) return false
                }
                if(!(nPosX < 0 || nPosX >= game.BOARD_WIDTH || nPosY < 0 || nPosY >= game.BOARD_HEIGHT))
                    if(game.board[nPosX][nPosY] != null)
                        return false;
            }
        }
        return true
    }

    fun rotate(dir: Int) {
        if (rotationCooldown == 0) {
            val newFigure = rotateMatrix(figure, dir)
            if (checkRotation(newFigure)) {
                figure = newFigure
                rotationCooldown = rotationCooldownMax
            }
        }
    }

    companion object {
        private fun rotateMatrix(figure2: Array<IntArray>, dir: Int): Array<IntArray> {
            val rotated = Array(figure2[0].size) { IntArray(figure2.size) }
            for (i in figure2[0].indices) {
                for (j in figure2.indices) {
                    if (dir == 1) rotated[i][j] = figure2[figure2.size - j - 1][i] else if (dir == -1) rotated[i][j] =
                        figure2[j][figure2[0].size - i - 1]
                }
            }
            return rotated
        }
    }
}