package org.m.svtpk.entity;

public class VideoJsonObjectEntity {

    private String svtId;
    private VideoReferencesEntity[] videoReferences;

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



}
