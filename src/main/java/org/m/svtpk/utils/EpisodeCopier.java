package org.m.svtpk.utils;

import javafx.application.Platform;
import org.m.svtpk.SvtpkApplication;
import org.m.svtpk.entity.EpisodeEntity;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class EpisodeCopier implements Runnable {
    private final EpisodeEntity episode;

    public EpisodeCopier(EpisodeEntity episode) {
        this.episode = episode;
    }

    @Override
    public void run() {
        Settings settings = Settings.load();

        String vidArgs = "0:" + episode.getAvailableResolutions().get(settings.getResolution()).getId();
        String audArgs = "0:" + episode.getAvailableAudio().get(settings.getAudio()).getId();
        String subArgs = settings.getSubs().equalsIgnoreCase("Inga undertexter") ?
                ""
                :
                "0:" + episode.getAvailableSubs().get(settings.getSubs()).getId();

        String map = "-map";
        String filename = StringHelpers.fileNameFixerUpper(episode.getProgramTitle() + "-" + episode.getEpisodeTitle()).concat(".mkv");
        ArrayList<String> temp = new ArrayList<>(List.of(new String[]{"tools\\ffmpeg",
                "-i",
                "\"" + episode.getMpdURL() + "\"",
                map,
                vidArgs,
                map,
                audArgs}));
        if (!subArgs.equals("")) {
            temp.add(map);
            temp.add(subArgs);
        }
        temp.add(filename);
        String[] cmd = temp.toArray(new String[0]);

        System.out.println("command:" + Arrays.toString(cmd).replace(",", ""));

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(new File(System.getProperty("user.dir")));
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectErrorStream(true);
        try {
            System.out.println("innan");
            Process process = pb.start();
            //process.getInputStream().close();
            Platform.runLater(() -> {
                SvtpkApplication.updateLoadingBar(50);
            });

            //läs från strömmen hur mycket av avsnittet som bearbetats
            //jämför med episode.getContentDuration()
           /* boolean debug = false;
            int timestamp = 1;
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = reader.readLine();
            while (line!= null && ! line.trim().equals("---EOF---")){
                System.out.println("LINJEN: "+line);
                line = reader.readLine();
            }
            //System.out.println("what is "+ br.readLine());

            //read line from stream
            while (debug) {
            //line has "time="
                if (false) {
                    //timestamp = line.split
                    assert episode.getContentDuration() != 0;
                    Platform.runLater(() -> {
                        SvtpkApplication.updateLoadingBar((int) (timestamp / episode.getContentDuration() * 100));
                    });
                }
            }
            */
            process.waitFor();
            System.out.println("efter");
            Platform.runLater(() -> {
                SvtpkApplication.updateLoadingBar(100);
            });
            System.out.println("Exited with error code " + process.waitFor());

            //flytta skiten
            Path source = Paths.get(System.getProperty("user.dir")).resolve(filename);
            Path target = Paths.get(settings.getPath()).resolve(filename);

            Files.copy(source, target, REPLACE_EXISTING);
            System.out.println("Flyttat!");
            Files.delete(source);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
