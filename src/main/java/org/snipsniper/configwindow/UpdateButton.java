package org.snipsniper.configwindow;

import org.snipsniper.LogManager;
import org.snipsniper.SnipSniper;
import org.snipsniper.config.ConfigHelper;
import org.snipsniper.utils.*;

public class UpdateButton extends IDJButton {
    final String STATE_WAITING = "waiting";
    final String STATE_DOUPDATE = "update";
    final String STATE_IDLE = "idle";

    public UpdateButton() {
        super("");
        setID(STATE_WAITING);
        setText("Check for update");
        addActionListener(e -> {
            ReleaseType updateChannel = Utils.getReleaseType(SnipSniper.getConfig().getString(ConfigHelper.MAIN.updateChannel));
            if(updateChannel == ReleaseType.RELEASE) {
                if(getID().equals(STATE_WAITING)) {
                    setText("Checking for update...");
                    Version onlineVersion = new Version(Utils.getTextFromWebsite(Links.STABLE_VERSION_TXT));
                    Version currentVersion = SnipSniper.getVersion();
                    if (onlineVersion.equals(currentVersion) || currentVersion.isNewerThan(onlineVersion)) {
                        setText("Up to date!");
                        setID(STATE_IDLE);
                    } else if (onlineVersion.isNewerThan(currentVersion)) {
                        if(SnipSniper.getVersion().getPlatformType() == PlatformType.STEAM) {
                            setText(StringUtils.format("<html><p align='center'>Update available! (%c)</p><p align='center'>Check Steam to update!</p></html>", onlineVersion.getDigits()));
                            setID(STATE_IDLE);
                        } else {
                            setText(StringUtils.format("<html><p align='center'>Update available! (%c)</p><p align='center'>Click here to update</p></html>", onlineVersion.getDigits()));
                            setID(STATE_DOUPDATE);
                        }
                    } else {
                        setText("Error. Check console.");
                        LogManager.log("Issue checking for updates. Our Version: %c, Online version: %c", LogLevel.ERROR, currentVersion.getDigits(), onlineVersion.getDigits());
                        setID(STATE_IDLE);
                    }
                } else if(getID().equals(STATE_DOUPDATE)) {
                    UpdateUtils.update();
                }
            } else {
                if(getID().equals(STATE_WAITING)) {
                    setText("Checking for update...");
                    String newestHash = Utils.getShortGitHash(Utils.getHashFromAPI(Links.API_LATEST_COMMIT));
                    if(newestHash.equals(SnipSniper.getVersion().getGithash())) {
                        setText("Up to date!");
                        setID(STATE_IDLE);
                    } else {
                        setText(StringUtils.format("<html><p align='center'>Update available! (%c)</p><p align='center'>Click here to update</p></html>", newestHash));
                        setID(STATE_DOUPDATE);
                    }
                } else if(getID().equals(STATE_DOUPDATE)) {
                    UpdateUtils.update();
                }
            }
        });
    }
}
