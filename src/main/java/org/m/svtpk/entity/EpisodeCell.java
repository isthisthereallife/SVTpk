package org.m.svtpk.entity;

import javafx.scene.control.cell.CheckBoxTreeCell;

public class EpisodeCell extends CheckBoxTreeCell<String> {
    EpisodeEntity episode;

    public EpisodeCell(EpisodeEntity episode) {
        super();
        this.episode = episode;

    }
    @Override
    public void updateItem(String item, boolean empty){
        super.updateItem(item,empty);
        setText(item == null ? "" : item);

    }

    public EpisodeEntity getEpisode() {
        return episode;
    }

    public void setEpisode(EpisodeEntity episode) {
        this.episode = episode;
    }
}
