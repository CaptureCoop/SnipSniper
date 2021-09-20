package org.snipsniper.utils;

public class SSFile {
    private final String path;
    private LOCATION location;

    public enum LOCATION {JAR, LOCAL}

    private final String JAR_IDENTIFIER = "%%JAR%%";
    private final String LOCAL_IDENTIFIER = "%%LOCAL%%";

    public SSFile(String path) {
        if(path.contains(JAR_IDENTIFIER)) {
            location = LOCATION.JAR;
            this.path = path.replace(JAR_IDENTIFIER, "");
        } else if(path.contains(LOCAL_IDENTIFIER)) {
            location = LOCATION.LOCAL;
            this.path = path.replace(LOCAL_IDENTIFIER, "");
        } else {
            this.path = path;
            location = LOCATION.JAR;
        }
    }

    public SSFile(String path, LOCATION location) {
        this.path = path;
        this.location = location;
    }

    public String getPath() {
        return path;
    }

    public String getPathWithLocation() {
        if(location == LOCATION.JAR)
            return JAR_IDENTIFIER + path;
        else return LOCAL_IDENTIFIER + path;
    }

    public LOCATION getLocation() {
        return location;
    }

    public String toString() {
        return StringUtils.format("SSFile path: %c, location: %c", path, location.toString().toLowerCase());
    }
}
