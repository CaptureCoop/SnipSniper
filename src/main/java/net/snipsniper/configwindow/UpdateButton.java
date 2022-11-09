package net.snipsniper.configwindow;

import net.snipsniper.ImageManager;
import org.capturecoop.cclogger.CCLogger;
import net.snipsniper.SnipSniper;
import net.snipsniper.config.ConfigHelper;
import net.snipsniper.utils.*;
import org.capturecoop.cclogger.CCLogLevel;
import org.capturecoop.ccutils.utils.CCStringUtils;

import javax.swing.*;
import java.awt.*;

public class UpdateButton extends IDJButton {
    final String STATE_WAITING = "waiting";
    final String STATE_DOUPDATE = "update";
    final String STATE_IDLE = "idle";
    private final Image roundArrows = ImageManager.Companion.getImage("icons/roundarrows.png").getScaledInstance(16, 16, 0);
    private final Image checkmark = ImageManager.Companion.getImage("icons/checkmark.png").getScaledInstance(16, 16, 0);
    private final Image download = ImageManager.Companion.getImage("icons/download.png").getScaledInstance(16, 16, 0);

    public UpdateButton() {
        super("");
        setId(STATE_WAITING);
        setText("Check for update");
        setIcon(new ImageIcon(roundArrows));
        addActionListener(e -> {
            if(getId().equals(STATE_DOUPDATE)) {
                UpdateUtils.Companion.update();
                return;
            }

            BuildInfo bi = SnipSniper.Companion.getBuildInfo();
            Version version = bi.getVersion();
            ReleaseType updateChannel = Utils.Companion.getReleaseType(SnipSniper.Companion.getConfig().getString(ConfigHelper.MAIN.updateChannel));
            boolean isJar = SnipSniper.Companion.getPlatformType() == PlatformType.JAR;
            boolean isDev = bi.getReleaseType() == ReleaseType.DEV;
            boolean isStable = bi.getReleaseType() == ReleaseType.STABLE;

            boolean isJarAndDev = isDev && isJar;
            boolean isJarAndDevButStableBranch = updateChannel == ReleaseType.STABLE && !isStable && isJar;

            if(isJarAndDev) {
                if(getId().equals(STATE_WAITING)) {
                    setText("Checking for update...");
                    String newestHash = Utils.Companion.getShortGitHash(Utils.Companion.getHashFromAPI(Links.API_LATEST_COMMIT));
                    if(newestHash == null || newestHash.isEmpty()) {
                        setText("Error - No connection");
                        setId(STATE_WAITING);
                        setIcon(new ImageIcon(roundArrows));
                    } else if(newestHash.equals(SnipSniper.Companion.getBuildInfo().getGitHash())) {
                        setText("Up to date!");
                        setId(STATE_IDLE);
                        setIcon(new ImageIcon(checkmark));
                    } else {
                        setText(CCStringUtils.format("<html><p align='center'>Update available! (%c)</p></html>", newestHash));
                        setId(STATE_DOUPDATE);
                        setIcon(new ImageIcon(download));
                    }
                }
            } else if(isJarAndDevButStableBranch) {
                if(getId().equals(STATE_WAITING)) {
                    setText("<html><p align='center'>Switch to stable</p></html>");
                    setId(STATE_DOUPDATE);
                }
            } else {
                if(getId().equals(STATE_WAITING)) {
                    setText("Checking for update...");
                    String versionString = Utils.Companion.getTextFromWebsite(Links.STABLE_VERSION_TXT);
                    Version onlineVersion = new Version(versionString);
                    Version currentVersion = SnipSniper.Companion.getBuildInfo().getVersion();
                    if(versionString == null || versionString.isEmpty()) {
                        setText("Error - No connection");
                        setId(STATE_WAITING);
                        setIcon(new ImageIcon(roundArrows));
                    } else if (onlineVersion.equals(currentVersion) || currentVersion.isNewerThan(onlineVersion)) {
                        setText("Up to date!");
                        setId(STATE_IDLE);
                        setIcon(new ImageIcon(checkmark));
                    } else if (onlineVersion.isNewerThan(currentVersion)) {
                        if(SnipSniper.Companion.getPlatformType() == PlatformType.STEAM) {
                            setText(CCStringUtils.format("<html><p align='center'>Update available! (%c)</p><p align='center'>Check Steam to update!</p></html>", onlineVersion.digitsToString()));
                            setId(STATE_IDLE);
                        } else {
                            setText(CCStringUtils.format("<html><p align='center'>Update available! (%c)</p></html>", onlineVersion.digitsToString()));
                            setId(STATE_DOUPDATE);
                        }
                        setIcon(new ImageIcon(download));
                    } else {
                        setText("Error. Check console.");
                        CCLogger.Companion.error(String.format("Issue checking for updates. Our Version: %s, Online version: %s", currentVersion.digitsToString(), onlineVersion.digitsToString()));
                        setId(STATE_IDLE);
                        setIcon(null);
                    }
                }
            }
        });
    }
}
