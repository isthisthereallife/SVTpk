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
        System.out.println("Processing queue...");
        boolean activity = false;
        for (QueueEntity queueEntity : queue) {
            ProgressStates progress = queueEntity.getEpisode().getProgressState();
            if (progress.equals(ProgressStates.ACTIVE)) {
                System.out.println("#One Was active");
                activity = true;
            } else if (progress.equals(ProgressStates.QUEUED) && !activity) {
                System.out.println("#One was queued.");
                activity = true;
                EpisodeCopier e = new EpisodeCopier(queueEntity);
                Thread h = new Thread(e);
                h.start();
                System.out.println("# Started thread: "+h.toString());
            } else if (progress.equals(ProgressStates.DONE)) {
                System.out.println("# This One Was Done");
            }
        }
    }
}

