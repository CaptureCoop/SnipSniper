package net.snipsniper.secrets.games

import net.snipsniper.config.ConfigHelper
import net.snipsniper.utils.DrawUtils
import net.snipsniper.utils.ImageUtils
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.Rectangle
import java.awt.image.BufferedImage
import javax.swing.JPanel

class BGamePanel(private val game: BGame) : JPanel() {
    fun screenshot() {
        ImageUtils.newBufferedImage(width, height, BufferedImage.TYPE_INT_RGB) {
            render(it, true)
        }.also {
            ImageUtils.saveImage(it, game.sniper.config.getString(ConfigHelper.PROFILE.saveFormat), "_bgame", game.sniper.config)
        }
    }

    fun render(g: Graphics, isScreenshot: Boolean) {
        val ts = game.tileSize
        g.clearRect(0, 0, width, height)
        g.color = Color.CYAN
        g.fillRect(0, 0, width, height)
        val offsetX = width / 2 - BGame.BOARD_WIDTH * ts / 2
        g.color = Color.BLACK
        if (game.currentPiece != null) {
            val cp = game.currentPiece!!
            for (y in cp.figure[0].indices) {
                for (x in cp.figure.indices) {
                    if (cp.figure[y][x] != 0) {
                        g.drawImage(game.resources?.getImage(cp.index), offsetX + (cp.posX + x) * ts, (cp.posY + y) * ts, ts, ts, null)
                    }
                }
            }
        }
        for (y in 0 until BGame.BOARD_HEIGHT) {
            for (x in 0 until BGame.BOARD_WIDTH) {
                val cBlock = game.board[x][y]
                if (cBlock != null) {
                    g.drawImage(game.resources?.getImage(cBlock.index), offsetX + x * ts, y * ts, ts, ts, null)
                    g.color = Color.BLACK
                }
                g.drawRect(offsetX + x * ts, y * ts, ts, ts)
            }
        }
        drawScoreText(g, offsetX, ts, 0, "Level")
        drawScoreText(g, offsetX, ts, 1, game.level.toString())
        drawScoreText(g, offsetX, ts, 3, "Score")
        drawScoreText(g, offsetX, ts, 4, game.score.toString())
        drawScoreText(g, offsetX, ts, 6, "Lines cleared")
        drawScoreText(g, offsetX, ts, 7, game.linesCleared.toString())
        val offY = drawScoreText(g, offsetX, ts, 9, "Next piece:")
        val npX = width - (offsetX + BGame.BOARD_WIDTH * ts) + BGame.BOARD_WIDTH * ts
        val npOffsetX = width - npX
        val np = game.nextPiece
        if (np != null) {
            val npPreview = np.getRawImage(ts)
            g.drawImage(npPreview, npX + npOffsetX / 2 - npPreview.width / 2, offY + ts / 2, null)
        }
        drawHelpText(g, offsetX, 0, "Block Game", 1.5f)
        drawHelpText(g, offsetX, 2, "Controls:", 1.1f)
        drawHelpText(g, offsetX, 4, "Q / E = Rotate")
        drawHelpText(g, offsetX, 5, "A / D = Move")
        drawHelpText(g, offsetX, 6, "S = Faster")
        drawHelpText(g, offsetX, 7, "Space = Drop")
        drawHelpText(g, offsetX, 8, "Escape = Pause")
        drawHelpText(g, offsetX, 9, "R = Restart")
        drawHelpText(g, offsetX, 10, "Right Click = Screenshot")
        if(isScreenshot) return

        fun drawText(text: String) {
            g.color = Color(0, 0, 0, 100)
            g.fillRect(0, 0, width, height)
            g.color = Color.WHITE
            DrawUtils.drawCenteredString(g, text, Rectangle(0, 0, width, height), getFont(height / 20))
        }

        if (game.isGameOver) drawText("GAME OVER")
        else if (game.isPaused) drawText("PAUSED")
    }

    override fun paint(g: Graphics) = render(g, false)

    //Returns Y
    private fun drawScoreText(g: Graphics?, offsetX: Int, ts: Int, index: Int, text: String?): Int {
        val height = height / 20
        val rect = Rectangle(offsetX + BGame.BOARD_WIDTH * ts, height * index, width - (offsetX + BGame.BOARD_WIDTH * ts), height)
        DrawUtils.drawCenteredString(g!!, text!!, rect, getFont(height))
        return rect.y + rect.height
    }

    private fun drawHelpText(g: Graphics, offsetX: Int, index: Int, text: String, fontMultiplier: Float = 1f) {
        val height = ((height / 20f) * fontMultiplier).toInt()
        Rectangle(0, height * index, offsetX, height).also {
            DrawUtils.drawCenteredString(g, text, it, getFont(height))
        }
    }

    private fun getFont(size: Int) = Font("Monospaced", Font.BOLD, size)
}