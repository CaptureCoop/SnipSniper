package org.snipsniper.utils;

import org.snipsniper.SnipSniper;
import org.snipsniper.config.ConfigHelper;

public class UpdateUtils {
    public static void update() {
        PlatformType type = SnipSniper.getVersion().getPlatformType();
        if(type == PlatformType.UNKNOWN) return;

        String pathInJar = "org/snipsniper/resources/SnipUpdater.jar";
        String updaterLocation = System.getProperty("java.io.tmpdir") + "//SnipUpdater.jar";

        switch(type) {
            case JAR:
                FileUtils.copyFromJar(pathInJar, updaterLocation);
                String jarLink = Links.STABLE_JAR;
                ReleaseType relType = Utils.getReleaseType(SnipSniper.getConfig().getString(ConfigHelper.MAIN.updateChannel));
                if(relType == ReleaseType.DEV) jarLink = Links.DEV_JAR;
                Utils.executeProcess(false, "java", "-jar", updaterLocation, "-url", jarLink, "-gui", "-exec", "SnipSniper.jar", "-dir", SnipSniper.getJarFolder());
                SnipSniper.exit(false);
                break;
            case WIN:
                FileUtils.copyFromJar(pathInJar, updaterLocation);
                Utils.executeProcess(false, "java", "-jar", updaterLocation, "-url", Links.STABLE_PORTABLE, "-gui", "-extract", "-exec", "SnipSniper.exe", "-dir", FileUtils.getCanonicalPath("."), "-deleteFile");
                SnipSniper.exit(false);
                break;
            case WIN_INSTALLED:
                FileUtils.copyFromJar(pathInJar, updaterLocation);
                Utils.executeProcess(false, "java", "-jar", updaterLocation, "-url", Links.STABLE_INSTALLER, "-gui", "-exec", "SnipSniper_Installer_Win.exe", "-dir", System.getProperty("java.io.tmpdir"));
                SnipSniper.exit(false);
                break;
        }
    }
}
