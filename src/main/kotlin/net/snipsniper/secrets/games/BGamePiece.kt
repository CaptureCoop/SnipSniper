package net.snipsniper.secrets.games

import net.snipsniper.utils.ImageUtils
import net.snipsniper.utils.trim
import org.capturecoop.ccutils.math.CCVector2Int
import java.awt.image.BufferedImage

class BGamePiece(private val game: BGame) {
    var figure: Array<IntArray>
    var posX: Int
        private set
    var posY = 1
        private set
    private var rotationCooldown = 0
    private val rotationCooldownMax = 10
    private var moveCooldown = 0
    private val moveCooldownMax = 10
    val index: Int = BGame.randomRange(0, game.resources!!.getSize() - 1)

    init {
        figure = game.resources!!.getPiece(index)
        posX = BGame.BOARD_WIDTH / 2 - figure[0].size / 2
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

    fun moveDown() = when(checkCollision()) {
        false -> {
            posY++
            false
        }
        true -> true
    }

    fun getRawImage(size: Int): BufferedImage {
        return ImageUtils.newBufferedImage(figure[0].size * size, figure[1].size * size) {
            for (y in figure[0].indices) {
                for (x in figure.indices) {
                    if(figure[y][x] == 0) continue
                    it.drawImage(game.resources!!.getImage(index), x * size, y * size, size, size, null)
                }
            }
        }.trim()
    }

    //returns true if the piece hit something
    fun checkCollision(): Boolean {
        for (y in figure[0].indices) {
            for (x in figure.indices) {
                if(figure[y][x] == 0) continue
                CCVector2Int(posX + x, posY + y).also { v ->
                    if (
                        v.x > -1 && v.x < BGame.BOARD_WIDTH &&
                        v.y > 0 && v.y < BGame.BOARD_HEIGHT - 1 &&
                        game.board[posX + x][posY + y + 1] != null
                    ) return true
                    if (posY + y >= BGame.BOARD_HEIGHT - 1) return true
                }
            }
        }
        return false
    }

    fun hit() {
        for (y in figure[0].indices) {
            for (x in figure.indices) {
                if(figure[y][x] == 0) continue
                CCVector2Int(posX + x, posY + y).also { v ->
                    if(!(v.x < 0 || v.x >= BGame.BOARD_WIDTH || v.y < 0 || v.y >= BGame.BOARD_HEIGHT))
                        game.board[v.x][v.y] = BGameBlock(index)
                }
            }
        }
    }

    private fun checkMoveCollision(dir: Int): Boolean {
        for (y in figure[0].indices) {
            for (x in figure.indices) {
                if(figure[y][x] == 0) continue
                CCVector2Int(posX + x + dir, posY + y).also { v ->
                    if (v.x <= -1) return false
                    if (v.x >= BGame.BOARD_WIDTH) return false
                    if (game.board[v.x][v.y] != null) return false
                }
            }
        }
        return true
    }

    fun move(dir: Int) {
        if(moveCooldown != 0 || !checkMoveCollision(dir)) return
        when(dir) {
            1 -> {
                posX++
                moveCooldown = moveCooldownMax
            }
            -1 -> {
                posX--
                moveCooldown = moveCooldownMax
            }
        }
    }

    private fun checkRotation(newFigure: Array<IntArray>): Boolean {
        for (y in newFigure[0].indices) {
            for (x in newFigure.indices) {
                CCVector2Int(posX + x, posY + y).also { v ->
                    if (v.x <= -1 || v.x >= BGame.BOARD_WIDTH || v.y <= 0 || v.y >= BGame.BOARD_HEIGHT - 1) {
                        if (newFigure[y][x] == 1) return false
                    }
                    if(!(v.x < 0 || v.x >= BGame.BOARD_WIDTH || v.y < 0 || v.y >= BGame.BOARD_HEIGHT))
                        if(game.board[v.x][v.y] != null)
                            return false
                }
            }
        }
        return true
    }

    fun rotate(dir: Int) {
        if(rotationCooldown != 0) return
        rotateMatrix(figure, dir).also {
            if (checkRotation(it)) {
                figure = it
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