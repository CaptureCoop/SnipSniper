package org.snipsniper.configwindow;

import org.snipsniper.LogManager;
import org.snipsniper.SnipSniper;
import org.snipsniper.config.ConfigHelper;
import org.snipsniper.utils.*;

import javax.swing.*;
import java.awt.*;

public class UpdateButton extends IDJButton {
    final String STATE_WAITING = "waiting";
    final String STATE_DOUPDATE = "update";
    final String STATE_IDLE = "idle";
    private final Image roundArrows = Icons.getImage("icons/roundarrows.png").getScaledInstance(16, 16, 0);
    private final Image checkmark = Icons.getImage("icons/checkmark.png").getScaledInstance(16, 16, 0);
    private final Image download = Icons.getImage("icons/download.png").getScaledInstance(16, 16, 0);


    public UpdateButton() {
        super("");
        setID(STATE_WAITING);
        setText("Check for update");
        setIcon(new ImageIcon(roundArrows));
        addActionListener(e -> {
            ReleaseType updateChannel = Utils.getReleaseType(SnipSniper.getConfig().getString(ConfigHelper.MAIN.updateChannel));
            if(updateChannel == ReleaseType.DEV && SnipSniper.getVersion().getPlatformType() == PlatformType.JAR) {
                if(getID().equals(STATE_WAITING)) {
                    setText("Checking for update...");
                    String newestHash = Utils.getShortGitHash(Utils.getHashFromAPI(Links.API_LATEST_COMMIT));
                    if(newestHash.equals(SnipSniper.getVersion().getGithash())) {
                        setText("Up to date!");
                        setID(STATE_IDLE);
                        setIcon(new ImageIcon(checkmark));
                    } else {
                        setText(StringUtils.format("<html><p align='center'>Update available! (%c)</p><p align='center'>Click here to update</p></html>", newestHash));
                        setID(STATE_DOUPDATE);
                        setIcon(new ImageIcon(download));
                    }
                } else if(getID().equals(STATE_DOUPDATE)) {
                    UpdateUtils.update();
                }
            } else {
                if(getID().equals(STATE_WAITING)) {
                    setText("Checking for update...");
                    Version onlineVersion = new Version(Utils.getTextFromWebsite(Links.STABLE_VERSION_TXT));
                    Version currentVersion = SnipSniper.getVersion();
                    if (onlineVersion.equals(currentVersion) || currentVersion.isNewerThan(onlineVersion)) {
                        setText("Up to date!");
                        setID(STATE_IDLE);
                        setIcon(new ImageIcon(checkmark));
                    } else if (onlineVersion.isNewerThan(currentVersion)) {
                        if(SnipSniper.getVersion().getPlatformType() == PlatformType.STEAM) {
                            setText(StringUtils.format("<html><p align='center'>Update available! (%c)</p><p align='center'>Check Steam to update!</p></html>", onlineVersion.getDigits()));
                            setID(STATE_IDLE);
                            setIcon(new ImageIcon(download));
                        } else {
                            setText(StringUtils.format("<html><p align='center'>Update available! (%c)</p><p align='center'>Click here to update</p></html>", onlineVersion.getDigits()));
                            setID(STATE_DOUPDATE);
                            setIcon(new ImageIcon(download));
                        }
                    } else {
                        setText("Error. Check console.");
                        LogManager.log("Issue checking for updates. Our Version: %c, Online version: %c", LogLevel.ERROR, currentVersion.getDigits(), onlineVersion.getDigits());
                        setID(STATE_IDLE);
                        setIcon(null);
                    }
                } else if(getID().equals(STATE_DOUPDATE)) {
                    UpdateUtils.update();
                }
            }
        });
    }
}
