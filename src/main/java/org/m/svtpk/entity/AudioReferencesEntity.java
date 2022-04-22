package org.m.svtpk.entity;

public class AudioReferencesEntity {
    private int id;
    private String label;
    private String url;
    private int range;
    private String suffix;

    public AudioReferencesEntity() {
    }

    public AudioReferencesEntity(String label) {
        this.label =label;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
