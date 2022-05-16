package org.m.svtpk.entity;

import java.net.URL;

public class SubtitleReferencesEntity {
    private int id;
    private String label;
    private URL url;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }
}
