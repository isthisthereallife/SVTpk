package org.m.svtpk.entity;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.cell.CheckBoxListCell;
import org.m.svtpk.utils.QueueHandler;

import java.io.IOException;
import java.util.ArrayList;

import static org.m.svtpk.SvtpkApplication.queue;

public class QueueEntity extends CheckBoxListCell {
    ContextMenu contextMenu;
    EpisodeEntity episode;
    int priority;


    public QueueEntity() {
        contextMenu = createContextMenu();
    }

    public QueueEntity(EpisodeEntity episodeEntity) {
        this.episode = episodeEntity;
        contextMenu = createContextMenu();
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public EpisodeEntity getEpisode() {
        return episode;
    }

    public void setEpisode(EpisodeEntity episode) {
        this.episode = episode;
    }


    public ContextMenu createContextMenu() {
        final ContextMenu contextMenu = new ContextMenu();
        //MenuItem miPlay = new MenuItem("Start");
        //MenuItem miPause = new MenuItem("Pause");
        //MenuItem miAbort = new MenuItem("Stop");
        //MenuItem miRemove = new MenuItem("Ta Bort från listan");
        MenuItem miOpenFolder = new MenuItem("Öppna Filens Sökväg");
        MenuItem miRemoveDone = new MenuItem("Ta Bort Färdiga");
        //MenuItem miRemoveAll = new MenuItem("Rensa Listan");
        MenuItem miRetry = new MenuItem("Uppdatera Kö");
        ArrayList<MenuItem> list = new ArrayList<>();
        if (this.episode.getSaveLocation() != null) {
            list.add(miOpenFolder);
        }
        list.add(miRemoveDone);
        contextMenu.getItems().addAll(list);
        //contextMenu.getItems().addAll(/*miPlay,*/ /*miPause,*/ /*miAbort,*/ /*miRemove*/miRemoveDone/*, miRemoveAll*/);
        /*
        miPlay.setOnAction((mi) -> {
            System.out.println("jag är miPlay mi: " + mi.toString());
            System.out.println(mi.getEventType());
            System.out.println("mitt avsnitt är: " + this.episode.getEpisodeTitle());
            System.out.println("id: " + this.episode.getSvtId());
            System.out.println("är jag null?: " + (this.episode == null));


            System.out.println(getEpisode().getEpisodeTitle());


        });
        */

        /*
        miPause.setOnAction((mi) -> {
            // pausa denna
        });
        */
        /*
        miAbort.setOnAction((mi) -> {
            System.out.println("jag är miAbort mi: " + mi.toString());
            System.out.println("mitt avsnitt är: \n" + this.episode.getEpisodeTitle());


        });
        */
        /*
            miRemove.setOnAction((mi) -> {
            System.out.println("jag är miRemove mi: " + mi.toString());
            System.out.println("mitt avsnitt är: \n" + this.episode.getEpisodeTitle());


            //ta bort denna från queue
        });
        */

        miOpenFolder.setOnAction((mi) -> {

            try {
                Runtime.getRuntime().exec("explorer.exe /select," + episode.getSaveLocation());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        miRemoveDone.setOnAction((mi) -> {
            //System.out.println("jag är miRemoveDone mi: " + mi.toString());
            //System.out.println("mitt avsnitt är: \n" + this.episode.getEpisodeTitle());
            ArrayList<QueueEntity> doneList = new ArrayList<>();

            queue.stream()
                    .filter(item -> item.getEpisode().getProgressState().equals(ProgressStates.DONE))
                    .forEach(doneList::add);
            queue.removeAll(doneList);

            //Ta bort dom i queue som är ProgressState.DONE
            /*
            queue.removeIf((u)->{
                return u.getProgressState().equals(ProgressStates.DONE);
            });
             */
        });
        /*
        miRemoveAll.setOnAction((mi) -> {
            System.out.println("jag är miRemoveAll mi: " + mi.toString());
            System.out.println("mitt avsnitt är: \n" + episode.getEpisodeTitle());
            //ta bort alla från queue
        });
        */
        contextMenu.getItems().add(miRetry);
        miRetry.setOnAction((mi) -> {
            QueueHandler.processQueue();
        });
        return contextMenu;
    }

    @Override
    public String toString() {
        return episode.getEpisodeTitle() + "\n" + episode.getProgramTitle() + "\n" + (int) (episode.getProgressDouble() * 100) + "%";
    }
}
