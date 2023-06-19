package org.m.svtpk.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

public class Settings {
    private final String STANDARD_PATH_WIN = System.getProperty("user.dir").concat("\\Downloads");

    private boolean isAdvancedUser;
    private String resolution;
    private String subs;
    private String path;
    private String audio;

    public Settings() {
        path = STANDARD_PATH_WIN;
        resolution = "720";
        audio = "Svenska";
        subs = "Svenska";
    }

    public void save() {
        try {
            Files.write(Paths.get("settings.txt"), Collections.singleton(toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Settings load() {
        Settings s = new Settings();
        try {
            String settingsString = Files.readString(Paths.get("settings.txt"));
            s = parseSettings(settingsString);

        } catch (IOException e) {
            /*
            System.out.println("Bad settings file or no settings file");
            System.out.println("Making a new file...");
             */
            try {
                Files.write(Paths.get("settings.txt"), Collections.singleton(new Settings().toString()));
            } catch (IOException ex) {
                System.out.println("Couldn't write new settings file.");
                ex.printStackTrace();
            }
        }
        return s;
    }

    public static Settings parseSettings(String settingsString) {
        Settings s = new Settings();
        try {
            s.setAdvancedUser(Boolean.parseBoolean(settingsString.split("advanced_user=")[1].split(",")[0].trim()));
            s.setResolution(settingsString.split("resolution=")[1].split(",")[0].trim());
            s.setAudio(settingsString.split("audio=")[1].split(",")[0].trim());
            s.setSubs(settingsString.split("subs=")[1].split(",")[0].trim());
            s.setPath(settingsString.split("path=")[1].split(",")[0].trim());

            if(s.resolution.length()==0){
                s.setResolution("720");
            }
            if (s.audio.length()==0){
                s.setAudio("Svenska");
            }
            if(s.path.length()==0){
                s.setPath(load().STANDARD_PATH_WIN);
            }
        } catch (Exception e) {
            System.out.println("error parsing stored settings");
            return new Settings();
        }
        return s;
    }

    @Override
    public String toString() {
        return "advanced_user=" + isAdvancedUser +
                ",audio=" + audio +
                ",resolution=" + resolution +
                ",subs=" + subs +
                ",path=" + path
                ;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public boolean isAdvancedUser() {
        return isAdvancedUser;
    }

    public void setAdvancedUser(boolean advancedUser) {
        isAdvancedUser = advancedUser;
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

    public void setPath() {
        path = STANDARD_PATH_WIN;
    }
}
