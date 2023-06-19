package org.m.svtpk.utils;

import javafx.collections.ListChangeListener;
import org.m.svtpk.entity.ProgressStates;
import org.m.svtpk.entity.QueueEntity;

import static org.m.svtpk.SvtpkApplication.queue;

public class QueueHandler {

    public QueueHandler() {
        queue.addListener(new ListChangeListener() {
            @Override
            public void onChanged(ListChangeListener.Change change) {
                processQueue();

            }
        });
    }

    public static void processQueue() {
        boolean activity = false;
        for (QueueEntity queueEntity : queue) {
            ProgressStates progress = queueEntity.getEpisode().getProgressState();
            if (progress.equals(ProgressStates.ACTIVE)) {
                activity = true;
            } else if (progress.equals(ProgressStates.QUEUED) && !activity) {
                activity = true;
                EpisodeCopier e = new EpisodeCopier(queueEntity);
                Thread h = new Thread(e);
                h.start();
                } else if (progress.equals(ProgressStates.DONE)) {
                //System.out.println("# This One Was Done");
            }
        }
    }
}

