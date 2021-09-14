package org.snipsniper.configwindow;

import org.snipsniper.LogManager;
import org.snipsniper.SnipSniper;
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
            if(getID().equals(STATE_WAITING)) {
                setText("Checking for update...");
                Version onlineVersion = new Version(Utils.getTextFromWebsite(Links.VERSION_TXT));
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
                PlatformType type = SnipSniper.getVersion().getPlatformType();
                if(type == PlatformType.UNKNOWN) return;

                String pathInJar = "org/snipsniper/resources/SnipUpdater.jar";
                String updaterLocation = System.getProperty("java.io.tmpdir") + "//SnipUpdater.jar";

                switch(type) {
                    case JAR:
                        FileUtils.copyFromJar(pathInJar, updaterLocation);
                        Utils.executeProcess(false, "java", "-jar", updaterLocation, "-url", Links.STABLE_JAR, "-gui", "-exec", "SnipSniper.jar", "-dir", SnipSniper.getJarFolder());
                        SnipSniper.exit(false);
                        break;
                    case WIN:
                        FileUtils.copyFromJar(pathInJar, updaterLocation);
                        Utils.executeProcess(false, "java", "-jar", updaterLocation, "-url", Links.STABLE_PORTABLE, "-gui", "-extract", "-exec", "SnipSniper.exe", "-dir", FileUtils.getCanonicalPath("."));
                        SnipSniper.exit(false);
                        break;
                    case WIN_INSTALLED:
                        FileUtils.copyFromJar(pathInJar, updaterLocation);
                        Utils.executeProcess(false, "java", "-jar", updaterLocation, "-url", Links.STABLE_INSTALLER, "-gui", "-exec", "SnipSniper_Installer_Win.exe");
                        SnipSniper.exit(false);
                        break;
                }
            }
        });
    }
}
