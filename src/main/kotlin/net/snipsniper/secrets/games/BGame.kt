package net.snipsniper.secrets.games

import net.snipsniper.SnipSniper.Companion.getNewThread
import net.snipsniper.StatsManager
import net.snipsniper.StatsManager.Companion.incrementCount
import net.snipsniper.systray.Sniper
import net.snipsniper.utils.getImage
import org.capturecoop.cclogger.CCLogLevel
import org.capturecoop.cclogger.CCLogger
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.event.*
import java.util.*
import javax.swing.JFrame

class BGame(val sniper: Sniper) : JFrame() {
    lateinit var board: Array<Array<BGameBlock?>>
        private set
    private val gamePanel = BGamePanel(this)
    var currentPiece: BGamePiece? = null
        private set
    private var running = true
    var isPaused = false
        private set
    private var keys = Array(25565) { false }
    var resources: BGameResources? = null
        private set
    var score = 0
        private set
    var level = 0
        private set
    var linesCleared = 0
        private set
    private var rowsBeforeLevelUp = 10
    var nextPiece: BGamePiece? = null
        private set
    var isGameOver = false
        private set
    private var hitDuringDownPress = false
    private fun launch() {
        resources = BGameResources()
        resources!!.init()
        title = "Block Game"
        add(gamePanel)
        iconImage = "icons/random/block.png".getImage()
        addKeyListener(object : KeyAdapter() {
            override fun keyPressed(keyEvent: KeyEvent) {
                keys[keyEvent.keyCode] = true
                if (keyEvent.keyCode == KeyEvent.VK_ESCAPE && !isGameOver) isPaused = !isPaused
            }
            override fun keyReleased(keyEvent: KeyEvent) {
                keys[keyEvent.keyCode] = false
            }
        })
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(event: MouseEvent) = kotlin.run { if (event.button == 3) gamePanel.screenshot() }
        })
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(event: WindowEvent) {
                running = false
                dispose()
            }
        })
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        isVisible = true
        gamePanel.preferredSize = Dimension(INITIAL_SCREEN_WIDTH, INITIAL_SCREEN_HEIGHT)
        pack()
        Toolkit.getDefaultToolkit().screenSize.also {
            setLocation(it.width / 2 - width / 2, it.height / 2 - height / 2)
        }
        start()
        loop()
    }

    private var fallSpeed = 0
    private var fallSpeedMax = FALLSPEED_MAX_START
    private fun loop() {
        while (running) {
            val ts = tileSize
            minimumSize = Dimension(BOARD_WIDTH * ts, BOARD_HEIGHT * ts)
            if (!isPaused) {
                input()
                if (!isGameOver) {
                    var isHit = false
                    if (currentPiece != null) isHit = currentPiece!!.update()
                    if (fallSpeed >= fallSpeedMax) {
                        if (isHit || hitDuringDownPress) {
                            currentPiece!!.hit()
                            hitDuringDownPress = false
                            spawnPiece()
                        }
                        if (currentPiece != null) currentPiece!!.moveDown()
                        fallSpeed = 0
                    } else {
                        fallSpeed += 10
                    }
                    val rows = checkRows()
                    if (rows != 0) {
                        linesCleared += rows
                        if (linesCleared >= rowsBeforeLevelUp) {
                            rowsBeforeLevelUp += LINES_BEFORE_LVLUP_ADD
                            level += 1
                            fallSpeedMax -= 25
                        }
                        val scoreMultiplier = level + 1
                        score += SCORES[rows - 1] * scoreMultiplier
                    }
                }
            }
            gamePanel.repaint()
            try {
                Thread.sleep(10)
            } catch (e: InterruptedException) {
                CCLogger.log("Error sleeping thread for BGame!", CCLogLevel.ERROR)
                CCLogger.logStacktrace(e, CCLogLevel.ERROR)
            }
        }
    }

    fun gameOver() = kotlin.run { isGameOver = true }

    private fun checkRows(): Int {
        var rowsCleared = 0
        for (y in 0 until BOARD_HEIGHT) {
            var hasFull = true
            for (x in 0 until BOARD_WIDTH) {
                if (board[x][y] == null) {
                    hasFull = false
                    break
                }
            }
            if (hasFull) {
                rowsCleared++
                for (x in 0 until BOARD_WIDTH) board[x][y] = null
                for (z in y downTo 1) for (x in 0 until BOARD_WIDTH) board[x][z] = board[x][z - 1]
            }
        }
        return rowsCleared
    }

    private fun spawnPiece() {
        incrementCount(StatsManager.BGAME_STARTED_SPAWNED_PIECES_AMOUNT)
        if (isGameOver) {
            currentPiece = null
            return
        }
        currentPiece = nextPiece ?: BGamePiece(this)
        nextPiece = BGamePiece(this)
    }

    val tileSize: Int
        get() = gamePanel.height / BOARD_HEIGHT

    fun start() {
        board = Array(BOARD_WIDTH) { Array(BOARD_HEIGHT) { null } }
        isGameOver = false
        fallSpeedMax = FALLSPEED_MAX_START
        nextPiece = null
        score = 0
        linesCleared = 0
        level = 0
        spawnPiece()
    }

    private var dropCooldown = 0
    private val dropCooldownMax = 25

    init {
        incrementCount(StatsManager.BGAME_STARTED_AMOUNT)
        getNewThread { launch() }.start()
    }

    fun input() {
        if (dropCooldown > 0) dropCooldown--
        if (isPressed(KeyEvent.VK_R)) {
            start()
            return
        }
        if (currentPiece != null) {
            if (isPressedAny(KeyEvent.VK_SPACE, KeyEvent.VK_SHIFT) && dropCooldown == 0 && !isGameOver) {
                for (i in 0 until BOARD_HEIGHT) {
                    if (currentPiece!!.moveDown()) break
                }
                dropCooldown = dropCooldownMax
            }
            if (isPressedAny(KeyEvent.VK_E, KeyEvent.VK_UP) && !isGameOver) currentPiece!!.rotate(1)
            if (isPressed(KeyEvent.VK_Q) && !isGameOver) currentPiece!!.rotate(-1)
            if (isPressedAny(KeyEvent.VK_A, KeyEvent.VK_LEFT) && !isGameOver) currentPiece!!.move(-1)
            if (isPressedAny(KeyEvent.VK_D, KeyEvent.VK_RIGHT) && !isGameOver) currentPiece!!.move(1)
            if (isPressedAny(KeyEvent.VK_S, KeyEvent.VK_DOWN) && !isGameOver) {
                currentPiece!!.moveDown()
                if (currentPiece!!.checkCollision()) hitDuringDownPress = true
            }
        }
    }

    private fun isPressed(keyCode: Int) = keys[keyCode]

    private fun isPressedAny(vararg keyCodes: Int) = keyCodes.any { keys[it] }

    companion object {
        private const val INITIAL_SCREEN_WIDTH = 1024
        private const val INITIAL_SCREEN_HEIGHT = 512
        const val BOARD_WIDTH = 10
        const val BOARD_HEIGHT = 20
        private const val FALLSPEED_MAX_START = 500
        private val SCORES = intArrayOf(40, 100, 300, 1200)
        private const val LINES_BEFORE_LVLUP_ADD = 10

        fun randomRange(min: Int, max: Int) = Random().nextInt(max - min + 1) + min
    }
}