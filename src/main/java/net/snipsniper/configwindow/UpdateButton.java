package net.snipsniper.configwindow;

import net.snipsniper.ImageManager;
import org.capturecoop.cclogger.CCLogger;
import net.snipsniper.SnipSniper;
import net.snipsniper.config.ConfigHelper;
import net.snipsniper.utils.*;
import org.capturecoop.cclogger.CCLogLevel;
import net.snipsniper.utils.enums.PlatformType;
import net.snipsniper.utils.enums.ReleaseType;
import org.capturecoop.ccutils.utils.CCStringUtils;

import javax.swing.*;
import java.awt.*;

public class UpdateButton extends IDJButton {
    final String STATE_WAITING = "waiting";
    final String STATE_DOUPDATE = "update";
    final String STATE_IDLE = "idle";
    private final Image roundArrows = ImageManager.getImage("icons/roundarrows.png").getScaledInstance(16, 16, 0);
    private final Image checkmark = ImageManager.getImage("icons/checkmark.png").getScaledInstance(16, 16, 0);
    private final Image download = ImageManager.getImage("icons/download.png").getScaledInstance(16, 16, 0);

    public UpdateButton() {
        super("");
        setID(STATE_WAITING);
        setText("Check for update");
        setIcon(new ImageIcon(roundArrows));
        addActionListener(e -> {
            if(getID().equals(STATE_DOUPDATE)) {
                UpdateUtils.update();
                return;
            }

            Version version = SnipSniper.Companion.getVersion();
            ReleaseType updateChannel = Utils.getReleaseType(SnipSniper.Companion.getConfig().getString(ConfigHelper.MAIN.updateChannel));
            boolean isJar = version.getPlatformType() == PlatformType.JAR;
            boolean isDev = version.getReleaseType() == ReleaseType.DEV;
            boolean isStable = version.getReleaseType() == ReleaseType.STABLE;

            boolean isJarAndDev = isDev && isJar;
            boolean isJarAndDevButStableBranch = updateChannel == ReleaseType.STABLE && !isStable && isJar;

            if(isJarAndDev) {
                if(getID().equals(STATE_WAITING)) {
                    setText("Checking for update...");
                    String newestHash = Utils.getShortGitHash(Utils.getHashFromAPI(Links.API_LATEST_COMMIT));
                    if(newestHash == null || newestHash.isEmpty()) {
                        setText("Error - No connection");
                        setID(STATE_WAITING);
                        setIcon(new ImageIcon(roundArrows));
                    } else if(newestHash.equals(SnipSniper.Companion.getVersion().getGithash())) {
                        setText("Up to date!");
                        setID(STATE_IDLE);
                        setIcon(new ImageIcon(checkmark));
                    } else {
                        setText(CCStringUtils.format("<html><p align='center'>Update available! (%c)</p></html>", newestHash));
                        setID(STATE_DOUPDATE);
                        setIcon(new ImageIcon(download));
                    }
                }
            } else if(isJarAndDevButStableBranch) {
                if(getID().equals(STATE_WAITING)) {
                    setText("<html><p align='center'>Switch to stable</p></html>");
                    setID(STATE_DOUPDATE);
                }
            } else {
                if(getID().equals(STATE_WAITING)) {
                    setText("Checking for update...");
                    String versionString = Utils.getTextFromWebsite(Links.STABLE_VERSION_TXT);
                    Version onlineVersion = new Version(versionString);
                    Version currentVersion = SnipSniper.Companion.getVersion();
                    if(versionString == null || versionString.isEmpty()) {
                        setText("Error - No connection");
                        setID(STATE_WAITING);
                        setIcon(new ImageIcon(roundArrows));
                    } else if (onlineVersion.equals(currentVersion) || currentVersion.isNewerThan(onlineVersion)) {
                        setText("Up to date!");
                        setID(STATE_IDLE);
                        setIcon(new ImageIcon(checkmark));
                    } else if (onlineVersion.isNewerThan(currentVersion)) {
                        if(SnipSniper.Companion.getVersion().getPlatformType() == PlatformType.STEAM) {
                            setText(CCStringUtils.format("<html><p align='center'>Update available! (%c)</p><p align='center'>Check Steam to update!</p></html>", onlineVersion.digitsToString()));
                            setID(STATE_IDLE);
                        } else {
                            setText(CCStringUtils.format("<html><p align='center'>Update available! (%c)</p></html>", onlineVersion.digitsToString()));
                            setID(STATE_DOUPDATE);
                        }
                        setIcon(new ImageIcon(download));
                    } else {
                        setText("Error. Check console.");
                        CCLogger.log("Issue checking for updates. Our Version: %c, Online version: %c", CCLogLevel.ERROR, currentVersion.digitsToString(), onlineVersion.digitsToString());
                        setID(STATE_IDLE);
                        setIcon(null);
                    }
                }
            }
        });
    }
}
