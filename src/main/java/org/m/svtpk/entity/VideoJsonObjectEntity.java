package org.m.svtpk.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VideoJsonObjectEntity {

    private String svtId;
    private VideoReferencesEntity[] videoReferences;
    private SubtitleReferencesEntity[] subtitleReferences;

    public String getSvtId() {
        return svtId;
    }
    public void setSvtId(String svtId) {
        this.svtId = svtId;
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
}
