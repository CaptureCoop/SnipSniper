package net.snipsniper.utils;

import net.snipsniper.SnipSniper;
import net.snipsniper.config.ConfigHelper;
import net.snipsniper.utils.enums.PlatformType;
import net.snipsniper.utils.enums.ReleaseType;

public class UpdateUtils {
    public static void update() {
        PlatformType type = SnipSniper.Companion.getVersion().getPlatformType();
        if(type == PlatformType.UNKNOWN) return;

        String pathInJar = "net/snipsniper/resources/SnipUpdater.jar";
        String updaterLocation = System.getProperty("java.io.tmpdir") + "//SnipUpdater.jar";

        switch(type) {
            case JAR:
                FileUtils.copyFromJar(pathInJar, updaterLocation);
                String jarLink = Links.STABLE_JAR;
                ReleaseType relType = Utils.getReleaseType(SnipSniper.Companion.getConfig().getString(ConfigHelper.MAIN.updateChannel));
                if(relType == ReleaseType.DEV) jarLink = Links.DEV_JAR;
                Utils.executeProcess(false, "java", "-jar", updaterLocation, "-url", jarLink, "-gui", "-exec", "SnipSniper.jar", "-dir", SnipSniper.Companion.getJarFolder());
                SnipSniper.Companion.exit(false);
                break;
            case WIN:
                FileUtils.copyFromJar(pathInJar, updaterLocation);
                Utils.executeProcess(false, "java", "-jar", updaterLocation, "-url", Links.STABLE_PORTABLE, "-gui", "-extract", "-exec", "SnipSniper.exe", "-dir", FileUtils.getCanonicalPath("."), "-deleteFile");
                SnipSniper.Companion.exit(false);
                break;
            case WIN_INSTALLED:
                FileUtils.copyFromJar(pathInJar, updaterLocation);
                Utils.executeProcess(false, "java", "-jar", updaterLocation, "-url", Links.STABLE_INSTALLER, "-gui", "-exec", "SnipSniper_Installer_Win.exe", "-dir", System.getProperty("java.io.tmpdir"));
                SnipSniper.Companion.exit(false);
                break;
        }
    }
}
