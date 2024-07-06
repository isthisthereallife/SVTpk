package org.m.svtpk.utils;

import javafx.application.Platform;
import javafx.scene.control.Control;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import org.m.svtpk.SvtpkApplication;
import org.m.svtpk.entity.ProgressStates;
import org.m.svtpk.entity.QueueEntity;

import java.io.*;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.m.svtpk.utils.HttpBodyGetter.connectToURLReturnBodyAsString;

public class EpisodeCopier implements Runnable {

    QueueEntity queueEntity;

    public EpisodeCopier(QueueEntity queueEntity) {
        this.queueEntity = queueEntity;
    }

    @Override
    public void run() {
        Platform.runLater(() -> {
            queueEntity.setBackground(new Background(new BackgroundFill(Color.rgb(123, 152, 115), null, null)));
            queueEntity.setPrefWidth(170);
        });
        /*log = Logger.getLogger("Debug");
        log.setFilter(new Filter() {
            @Override
            public boolean isLoggable(LogRecord record) {

                record.setLoggerName("Debug");
                record.setMessage("Hello");
                return false;
            }
        });

         */
        Settings settings = Settings.load();
        String videoFiletype = ".mp4";
        String subsFiletype = ".srt";
        String filename = queueEntity.getEpisode().getFilename().concat(videoFiletype);
        String subsname = queueEntity.getEpisode().getFilename().concat(subsFiletype);


        /*
        Platform.runLater(() -> {
            SvtpkApplication.updateLoadingBar(0);
        });
         */


        String vidArgs = "0:";
        vidArgs = vidArgs.concat(queueEntity.getEpisode().getAvailableResolutions() != null ? String.valueOf(queueEntity
                .getEpisode()
                .getAvailableResolutions()
                .get(settings.getResolution()).getId()) : "0");

        if (settings.isAdvancedUser()) System.out.println(queueEntity.getEpisode().toString());

        String audArgs = "0:";
        audArgs = audArgs.concat(queueEntity.getEpisode().getAvailableAudio() != null ? String.valueOf(queueEntity
                .getEpisode()
                .getAvailableAudio()
                .get(settings.getAudio()).getId()) : "0");
        /*String subArgs = settings.getSubs().equalsIgnoreCase("Inga undertexter") ?
                ""
                :
                "0:" + queueEntity.getEpisode().getAvailableSubs().get(settings.getSubs()).getId();
        */
        String map = "-map";
        ArrayList<String> temp = new ArrayList<>(List.of(new String[]{"ffmpeg",
                "-i",
                "\"" + queueEntity.getEpisode().getMpdURL() + "\"",
                map,
                vidArgs,
                map,
                audArgs}));
        /*if (!subArgs.equals("")) {
            temp.add(map);
            temp.add(subArgs);
        }*/
        temp.add(filename);
        String[] cmd = temp.toArray(new String[0]);
        if (settings.isAdvancedUser()) System.out.println("command: \n" + Arrays.toString(cmd).replace(",", ""));


        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(new File(System.getProperty("user.dir")));
        pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
        pb.redirectErrorStream(true);
        try {
            Process process = pb.start();

            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                if (settings.isAdvancedUser()) {
                    System.out.println(line);
                }
                if (line.contains("time=")) {
                    int elapsedTime = 0;
                    try {
                        String[] time = line.split("time=")[1].split(" ")[0].split(":");
                        int hours = Integer.parseInt(time[0]);
                        int minutes = Integer.parseInt(time[1]);
                        int seconds = Integer.parseInt(time[2].split("\\.")[0]);
                        //int millisec = Integer.parseInt(time[2].split("\\.")[1]);
                        elapsedTime = (hours * 60 * 60) + (minutes * 60) + seconds;
                    }catch(NumberFormatException ignored){
                        // let's just pretend that it is finished. it's probably fine.
                        elapsedTime = queueEntity.getEpisode().getContentDuration();
                    }
                    assert queueEntity.getEpisode().getContentDuration() != 0;
                    //Platform.runLater(() -> SvtpkApplication.updateLoadingBar(((double) elapsedTime / queueEntity.getEpisode().getContentDuration())));
                    //istället ska jag göra den här platform.runLater-grejen från ett annat ställe
                    //men ändra progressInt kan jag göra
                    double progress = (double) elapsedTime / queueEntity.getEpisode().getContentDuration();
                    Platform.runLater(() -> {
                        //queueEntity.getEpisode().setProgressDouble(progress);
                        if (progress > 0) {
                            queueEntity.getEpisode().setProgressState(ProgressStates.ACTIVE);
                        }
                        SvtpkApplication.updateLoadingBar(queueEntity, progress);
                    });
                }
            }
            process.waitFor();

            //flytta skiten
            Path source = Paths.get(System.getProperty("user.dir")).resolve(filename);
            Path target = Paths.get(settings.getPath()).resolve(filename);
            if (settings.isAdvancedUser()) System.out.println("Flyttar filen.");
            if (!Files.exists(Paths.get(settings.getPath()))) {
                System.out.println("Skapar mapp " + settings.getPath());
                Files.createDirectories(Paths.get(settings.getPath()));
            }
            try {
                Files.copy(source, target, REPLACE_EXISTING);
            } catch (FileSystemException e) {
                System.out.println("Misslyckades med att flytta filen till önskad mapp.");
            }
            queueEntity.getEpisode().setSaveLocation(target);
            if (settings.isAdvancedUser()) System.out.println("Fil flyttad.");
            Files.delete(source);
            if (settings.isAdvancedUser()) System.out.println("Orginalfil borttagen.");

            //if subs
            if (!settings.getSubs().equalsIgnoreCase("Inga undertexter")) {
                try {
                    String subs = connectToURLReturnBodyAsString(queueEntity.getEpisode().getAvailableSubs().get(settings.getSubs()).getUrl());

                    FileWriter fw = new FileWriter(subsname);
                    fw.write(subs);
                    fw.close();


                    Files.copy(
                            Paths.get(System.getProperty("user.dir")).resolve(subsname),
                            Paths.get(settings.getPath()).resolve(subsname),
                            REPLACE_EXISTING
                    );
                    Files.delete(Paths.get(System.getProperty("user.dir")).resolve(subsname));
                } catch (NullPointerException e) {
                    if (settings.isAdvancedUser()) System.out.println("Kunde inte kopiera undertexter.");
                }
            }

            Platform.runLater(() -> {
                queueEntity.setBackground(new Background(new BackgroundFill(Color.rgb(0, 194, 0), null, null)));
                queueEntity.getEpisode().setProgressDouble(1);
                queueEntity.setText(queueEntity.toString());
                queueEntity.setContextMenu(queueEntity.createContextMenu());

            });
            queueEntity.getEpisode().setProgressState(ProgressStates.DONE);
            QueueHandler.processQueue();

        } catch (IOException | InterruptedException e) {
            //e.printStackTrace();
            queueEntity.getEpisode().setProgressState(ProgressStates.ERROR);
            queueEntity.setBackground(new Background(new BackgroundFill(Color.FIREBRICK, null, null)));

        }
        if (settings.isAdvancedUser()) System.out.println("Finished operation on Thread: " + Thread.currentThread());
    }

}
