package org.m.svtpk.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

public class Settings {
    private boolean isVisible;
    private String resolution;
    private String subs;
    private String path;

    public void save(){
        try {
            Files.write(Paths.get("settings.txt"), Collections.singleton(toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static Settings load(){
        Settings s = new Settings();
        try {
            String settingsString = Files.readString(Paths.get("settings.txt"));
            s = parseSettings(settingsString);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Bad settingsString");
        }
        return s;
    }

    public static Settings parseSettings(String settingsString) {
        Settings s = new Settings();
        try {
            s.setVisible(Boolean.parseBoolean(settingsString.split("isVisible=")[1].split(",")[0]));
            s.setResolution(settingsString.split("resolution=")[1].split(",")[0]);
            s.setSubs(settingsString.split("subs=")[1].split(",")[0]);
            s.setPath(settingsString.split("path=")[1].split(",")[0]);
        } catch (Exception e) {
            System.out.println("error parsing settings");
        }
        return s;
    }

    @Override
    public String toString() {
        return  "isVisible=" + isVisible +
                ",resolution=" + resolution +
                ",subs=" + subs +
                ",path=" + path
                ;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getSubs() {
        return subs;
    }

    public void setSubs(String subs) {
        this.subs = subs;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
