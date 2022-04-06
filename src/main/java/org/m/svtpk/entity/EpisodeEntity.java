package org.m.svtpk.entity;


import javafx.scene.image.Image;

public class EpisodeEntity {
    private String svtId;
    private String programVersionId;
    private int contentDuration;
    private boolean blockedForChildren;
    private String programTitle;
    private String episodeTitle;
    private VideoReferencesEntity[] videoReferences;
    private SubtitleReferencesEntity[] subtitleReferences;
    private String imageURL;

    public EpisodeEntity() {
        svtId = "";
        imageURL = "";
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

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    @Override
    public String toString() {
        return "Program: '" + programTitle + "'\n" +
                "Avsnitt: '" + episodeTitle + "'\n" +
                "Längd: " + contentDuration / 60 + " min";
    }
}
