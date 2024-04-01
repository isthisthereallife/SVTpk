package org.m.svtpk.entity;

import java.net.URL;
import java.util.ArrayList;

public class SeasonEntity {

    private String name;
    private SeasonTypes type;
    private ArrayList<EpisodeEntity> items;
    private URL imageURL;


    public SeasonEntity() {
        this.name = "";
        this.type = SeasonTypes.unknown;
        this.items = new ArrayList<>();
    }

    public SeasonEntity(String name, String type, ArrayList<EpisodeEntity> items) {
        this.name = name;
        setTypeFromString(type);
        this.items = items;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SeasonTypes getType() {
        return type;
    }

    public void setType(SeasonTypes type) {
        this.type = type;
    }
    public void setTypeFromString (String type) {
        switch (type.toLowerCase()) {
            case "accessibility" -> this.type = SeasonTypes.accessibility;
            case "clip" -> this.type = SeasonTypes.clip;
            case "productionperiod" -> this.type = SeasonTypes.productionPeriod;
            case "related" -> this.type = SeasonTypes.related;
            case "season" -> this.type = SeasonTypes.season;
            case "upcoming" -> this.type = SeasonTypes.upcoming;
            default -> this.type = SeasonTypes.unknown;
        }
    }

    public ArrayList<EpisodeEntity> getItems() {
        return items;
    }

    public void setItems(ArrayList<EpisodeEntity> items) {
        this.items = items;
    }

    public void addItem(EpisodeEntity item) {
        this.items.add(item);
    }

    public URL getImageURL() {
        return imageURL;
    }

    public void setImageURL(URL imageURL) {
        this.imageURL = imageURL;
    }
}
