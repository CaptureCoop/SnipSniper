package net.snipsniper

import com.formdev.flatlaf.FlatDarculaLaf
import com.formdev.flatlaf.FlatIntelliJLaf
import net.snipsniper.config.Config
import net.snipsniper.config.ConfigHelper
import org.capturecoop.legiblelogger.LegibleLogger
import java.awt.Toolkit
import java.awt.Window
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.UIManager

enum class Theme {
    LIGHT,
    DARK;

    companion object {
        fun fromConfigKey(theme: String) = when(theme) {
            "light" -> LIGHT
            "dark" -> DARK
            else -> throw Exception("Bad Theme $theme")
        }
        fun fromConfig(config: Config) = fromConfigKey(config.getString(ConfigHelper.MAIN.theme))
    }
}

object SnipUIManager {
    fun init() {
        setTheme()
        UIManager.put("ScrollBar.showButtons", true)
        UIManager.put("ScrollBar.width", 16)
        UIManager.put("TabbedPane.showTabSeparators", true)

        JFrame.setDefaultLookAndFeelDecorated(true)
        JDialog.setDefaultLookAndFeelDecorated(true)
        setUIScale()
    }

    fun getSysUIScale(): Float = Toolkit.getDefaultToolkit().screenResolution / 96.toFloat()

    fun getEffectiveUIScale(): Float {
        val scaleValue = SnipSniper.config.getString(ConfigHelper.MAIN.uiScaling)
        return if(scaleValue == "auto") getSysUIScale() else scaleValue.toFloat()
    }

    fun calculateEffectiveUIScale(size: Int): Int = (size * getEffectiveUIScale()).toInt()

    fun setUIScale() {
        val scaleValue = getEffectiveUIScale()
        LegibleLogger.info("Setting FlatLaf Scale to $scaleValue")
        System.setProperty("flatlaf.uiScale", scaleValue.toString())
        UIManager.setLookAndFeel(UIManager.getLookAndFeel())

        for (w in Window.getWindows()) {
            SwingUtilities.updateComponentTreeUI(w)
            w.pack()
            w.invalidate()
            w.validate()
            w.repaint()
        }
    }

    fun setTheme(
        theme: Theme = Theme.fromConfig(SnipSniper.config)
    ) {
        when(theme) {
            Theme.DARK -> UIManager.setLookAndFeel(FlatDarculaLaf())
            Theme.LIGHT -> UIManager.setLookAndFeel(FlatIntelliJLaf())
        }
    }
}