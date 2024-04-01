package org.m.svtpk.entity;


import javafx.scene.image.Image;
import org.m.svtpk.utils.StringHelpers;

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
    private String seasonTitle;
    private String episodeTitle;
    private String episodeTitleUnnumbered;
    private SeasonTypes seasonType;
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
    private boolean isExpired = false;
    private String infotext;
    private int seasonNumber;
    private int episodeNumber;

    public EpisodeEntity() {
        programTitle = "I'm an episode!";
        svtId = "";
        availableSubs = new HashMap<>();
        availableResolutions = new HashMap<>();
        availableAudio = new HashMap<>();
        description = "";
        seasonNumber = -1;
        episodeNumber = -1;
        episodeTitleUnnumbered = "";

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
            System.out.println("Ingen upplösning hittad.");
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
        this.programTitle = programTitle;
    }

    public String getSeasonTitle() {
        return seasonTitle;
    }

    public void setSeasonTitle(String seasonTitle) {
        this.seasonTitle = seasonTitle;
    }

    public SeasonTypes getSeasonType() {
        return seasonType;
    }

    public void setSeasonType(SeasonTypes seasonType) {
        this.seasonType = seasonType;
    }

    public String getEpisodeTitle() {
        return episodeTitle;
    }

    public void setEpisodeTitle(String thisTitle) {
        this.episodeTitle = thisTitle;
    }

    public String getEpisodeTitleUnnumbered() {
        return episodeTitleUnnumbered;
    }

    public void setEpisodeTitleUnnumbered(String episodeTitleUnnumbered) {
        this.episodeTitleUnnumbered = episodeTitleUnnumbered;
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

    public boolean isExpired() {
        return isExpired;
    }

    public void setExpired(boolean isExpired) {
        this.isExpired = isExpired;
    }

    public String getInfotext() {
        return infotext;
    }

    public void setInfotext(String infotext) {
        this.infotext = infotext;
    }

    public int getSeasonNumber() {
        return seasonNumber;
    }

    public void setSeasonNumber(int seasonNumber) {
        this.seasonNumber = seasonNumber;
    }

    public int getEpisodeNumber() {
        return episodeNumber;
    }

    public void setEpisodeNumber(int episodeNumber) {
        this.episodeNumber = episodeNumber;
    }

    public boolean isNumberedEpisodeAndPartOfNumberedSeason() {
        return (this.seasonNumber > 0 && this.episodeNumber > 0);
    }

    @Override
    public String toString() {

        String desc = description.length() < 250 ? description : description.substring(0, 249).concat("...");
        return "Program: '" + programTitle + "'\n" +
                "Avsnitt: '" + episodeTitle + "'\n" +
                desc + "\n\n" +
                "Längd: " + contentDuration / 60 + " min";
    }

    public void extractSeasonAndEpisodeNumbers() {
        // if the type is right, and the season title is right, we have a numbered season
        if(this.seasonType.equals(SeasonTypes.season)) {
            if ((this.seasonTitle.split(" ")[0].equalsIgnoreCase("säsong") || this.seasonTitle.split(" ")[0].equalsIgnoreCase("season"))) {
                try {
                    this.seasonNumber = Integer.parseInt(this.seasonTitle.split(" ")[1]);
                } catch (NumberFormatException ignored) {
                }
            }
            // get and store number if the episode is numbered
            if (this.episodeTitle.split(" ")[0].equalsIgnoreCase("avsnitt") || this.episodeTitle.split(" ")[0].equalsIgnoreCase("episode")) {

                try {
                    this.episodeNumber = Integer.parseInt(this.episodeTitle.split(" ")[1]);
                    this.episodeTitleUnnumbered = this.episodeTitle;

                } catch (NumberFormatException ignored) {
                }

            } else {
                // some episodes may have names like "2. Avsnitt twå" || "14. Fjortooon"
                // but no series has more than 99 episodes to a season. Hopefully.
                // (also hope no episode is called "99 luftballoons" or "47 ronin", because that would mess it all up)

                // check first char
                if (this.episodeTitle.substring(0, 1).matches("\\d")) {
                    // check second char
                    if (this.episodeTitle.substring(1, 2).matches("\\d")) {
                        // if 3'd char is also a number, it's more likely to be a year in a name.
                        // hence the if-inversion
                        if (!this.episodeTitle.substring(2, 3).matches("\\d")) {
                            this.episodeNumber = Integer.parseInt(this.episodeTitle.substring(0, 2));
                            if (this.episodeTitle.charAt(2) == '.') {
                                // if it's like "12. The twelfth episode"
                                this.episodeTitleUnnumbered = this.episodeTitle.substring(3).trim();

                            }
                        }
                    } else if (this.episodeTitle.charAt(1) == '.') {
                        this.episodeNumber = Integer.parseInt(String.valueOf(this.episodeTitle.charAt(0)));
                        this.episodeTitleUnnumbered = this.episodeTitle.substring(2).trim();

                    }
                }
            }
        }
    }
    public void setFilename(){
        // set the filename to ProgramTitle.SxxExx.EpisodeTitle
        // if seasons and episodes are numbered.
        if(this.isNumberedEpisodeAndPartOfNumberedSeason()) {
            System.out.println("PERFECT");
            this.setFilename(
                    StringHelpers.fileNameFixerUpper(
                            this.getProgramTitle() +
                                    ".S" +
                                    (this.getSeasonNumber() < 10 ? "0" + this.getSeasonNumber() : this.getSeasonNumber()) +
                                    "E" +
                                    (this.getEpisodeNumber() < 10 ? "0" + this.getEpisodeNumber() : this.getEpisodeNumber()) +
                                    "." +
                                    this.getEpisodeTitleUnnumbered().trim()));
        } else if(this.getSeasonTitle()!= null && !this.getSeasonTitle().isEmpty() && !this.getSeasonTitle().equals("...")){
            System.out.println("this is not a perfect one. but it has a Season Title.");
            // TODO otherwise if has season-name, set it to ProgramTitle.SeasonTitle.Exx.EpisodeTitle
            this.setFilename(StringHelpers.fileNameFixerUpper(this.getProgramTitle() + "."+this.getSeasonTitle()+"." + this.getEpisodeTitle()));

        } else {//if(this.isFilm){
            // TODO set film name to FilmTitle.year
            System.out.println("i think this is a film. will get treated as one, anyway.");
            this.setFilename(StringHelpers.fileNameFixerUpper(this.getProgramTitle() + "-" + this.getEpisodeTitle()));
        }
    }
}
