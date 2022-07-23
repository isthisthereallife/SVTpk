package org.m.svtpk.entity;


import javafx.scene.image.Image;

import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Objects;

public class EpisodeEntity {
    private String svtId;
    private String programVersionId;
    private int contentDuration;
    private boolean blockedForChildren;
    private String programTitle;
    private String episodeTitle;
    private String description;
    private VideoReferencesEntity[] videoReferences;
    private SubtitleReferencesEntity[] subtitleReferences;
    private URL imageURL;
    private URL mpdURL;
    private URL splashURL;
    private String filename;
    private HashMap<String, SubtitleReferencesEntity> availableSubs;
    private HashMap<String, VideoReferencesEntity> availableResolutions;
    private HashMap<String, AudioReferencesEntity> availableAudio;
    private boolean isLive;
    private ProgressStates progressState;
    private double progressDouble;
    private Path saveLocation;
    private Image thumbnail;

    public EpisodeEntity() {
        programTitle = "fun program no bugs";
        svtId = "";
        availableSubs = new HashMap<>();
        availableResolutions = new HashMap<>();
        availableAudio = new HashMap<>();
        description = "";
    }

    public EpisodeEntity(String episodeTitle, String programTitle, ProgressStates progressState) {
        this.episodeTitle = episodeTitle;
        this.programTitle = programTitle;
        this.progressState = progressState;
        description = "";
        svtId = "";
        availableSubs = new HashMap<>();
        availableResolutions = new HashMap<>();
        availableAudio = new HashMap<>();
    }

    public EpisodeEntity(boolean isLive) {
        super();
        this.isLive = isLive;
    }

    public boolean isLive() {
        return isLive;
    }

    public void setLive(boolean live) {
        isLive = live;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public URL getMpdURL() {
        return mpdURL;
    }

    public void setMpdURL(URL mpdURL) {
        this.mpdURL = mpdURL;
    }

    public void addAvailableAudio(String name, AudioReferencesEntity availableAudio) {
        this.availableAudio.put(name, availableAudio);
    }

    public HashMap<String, AudioReferencesEntity> getAvailableAudio() {
        return availableAudio;
    }

    public void setAvailableAudio(HashMap<String, AudioReferencesEntity> availableAudio) {
        this.availableAudio = availableAudio;
    }

    public void addAvailableResolutions(String name, VideoReferencesEntity newResolutions) {
        this.availableResolutions.put(name, newResolutions);
    }

    public AudioReferencesEntity getSelectedAudio(String audio) {
        if (availableAudio.containsKey(audio)) {
            return availableAudio.get(audio);
        } else if (availableAudio.containsKey("Svenska")) {
            return availableAudio.get("Svenska");
        } else if (availableAudio.containsKey("Engelska"))
            return availableAudio.get("Engelska");
        else if (availableAudio.containsKey("English"))
            return availableAudio.get("English");
        return new AudioReferencesEntity("No Audio");
    }

    public HashMap<String, VideoReferencesEntity> getAvailableResolutions() {
        return availableResolutions;
    }

    public VideoReferencesEntity getBestAvailableResolutions(String resolution) {
        if (availableResolutions.containsKey(resolution)) {
            return availableResolutions.get(resolution);
        } else if (availableResolutions.containsKey("1080")) {
            return availableResolutions.get("1080");
        } else if (availableResolutions.containsKey("720")) {
            return availableResolutions.get("720");
        } else if (availableResolutions.containsKey("540")) {
            return availableResolutions.get("540");
        } else if (availableResolutions.containsKey("360")) {
            return availableResolutions.get("360");
        } else {
            System.out.println("ingen upplösning hittad | Episode Entity getBestAvailableResolutions ");
            return null;
        }
    }

    public void setAvailableResolutions(HashMap<String, VideoReferencesEntity> availableResolutions) {
        this.availableResolutions = availableResolutions;
    }

    public HashMap<String, SubtitleReferencesEntity> getAvailableSubs() {
        return availableSubs;
    }

    public SubtitleReferencesEntity getSelectedSubs(String subs) {
        if (availableSubs.containsKey(subs))
            return availableSubs.get(subs);
        return null;
    }

    public void addAvailableSubs(SubtitleReferencesEntity sub) {
        availableSubs.put(sub.getLabel(), sub);
    }

    public String getSvtId() {
        return svtId;
    }

    public void setSvtId(String svtId) {
        this.svtId = svtId;
    }

    public String getProgramVersionId() {
        return programVersionId;
    }

    public void setProgramVersionId(String programVersionId) {
        this.programVersionId = programVersionId;
    }

    public int getContentDuration() {
        return contentDuration;
    }

    public void setContentDuration(int contentDuration) {
        this.contentDuration = contentDuration;
    }

    public boolean isBlockedForChildren() {
        return blockedForChildren;
    }

    public void setBlockedForChildren(boolean blockedForChildren) {
        this.blockedForChildren = blockedForChildren;
    }

    public String getProgramTitle() {
        return programTitle;
    }

    public void setProgramTitle(String programTitle) {
        System.out.println("EpisodeEntity.setProgramTitle satte programtitle till: " + programTitle);
        this.programTitle = programTitle;
    }

    public String getEpisodeTitle() {
        return episodeTitle;
    }

    public void setEpisodeTitle(String episodeTitle) {
        this.episodeTitle = episodeTitle;
    }

    public VideoReferencesEntity[] getVideoReferences() {
        return videoReferences;
    }

    public void setVideoReferences(VideoReferencesEntity[] videoReferences) {
        this.videoReferences = videoReferences;
    }

    public SubtitleReferencesEntity[] getSubtitleReferences() {
        return subtitleReferences;
    }

    public void setSubtitleReferences(SubtitleReferencesEntity[] subtitleReferences) {
        this.subtitleReferences = subtitleReferences;
    }

    public URL getImageURL() {
        return imageURL;
    }

    public void setImageURL(URL imageURL) {
        this.imageURL = imageURL;
    }

    public URL getSplashURL() {
        return splashURL;
    }

    public void setSplashURL(URL splashURL) {
        this.splashURL = splashURL;
    }

    public Boolean hasID(EpisodeEntity e) {
        return !Objects.equals(e.getSvtId(), "");
    }

    public void setAvailableSubs(HashMap<String, SubtitleReferencesEntity> availableSubs) {
        this.availableSubs = availableSubs;
    }

    public ProgressStates getProgressState() {
        return progressState;
    }

    public void setProgressState(ProgressStates progressState) {
        this.progressState = progressState;
    }

    public double getProgressDouble() {
        return progressDouble;
    }

    public void setProgressDouble(double progressDouble) {
        this.progressDouble = progressDouble;
    }

    public Path getSaveLocation() {
        return saveLocation;
    }

    public void setSaveLocation(Path saveLocation) {
        this.saveLocation = saveLocation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Image getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Image thumbnail) {
        this.thumbnail = thumbnail;
    }

    @Override
    public String toString() {
        return "Program: '" + programTitle + "'\n" +
                "Avsnitt: '" + episodeTitle + "'\n" +
                description + "\n\n" +
                "Längd: " + contentDuration / 60 + " min";
    }
}
