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

        Platform.runLater(() -> {
            SvtpkApplication.updateLoadingBar(0);
        });

        String vidArgs = "0:" + episode.getAvailableResolutions().get(settings.getResolution()).getId();
        String audArgs = "0:" + episode.getAvailableAudio().get(settings.getAudio()).getId();
        String subArgs = settings.getSubs().equalsIgnoreCase("Inga undertexter") ?
                ""
                :
                "0:" + episode.getAvailableSubs().get(settings.getSubs()).getId();

        String map = "-map";
        String filename = StringHelpers.fileNameFixerUpper(episode.getProgramTitle() + "-" + episode.getEpisodeTitle()).concat(".mkv");
        ArrayList<String> temp = new ArrayList<>(List.of(new String[]{"ffmpeg",
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
                    String[] time = line.split("time=")[1].split(" ")[0].split(":");
                    int hours = Integer.parseInt(time[0]);
                    int minutes = Integer.parseInt(time[1]);
                    int seconds = Integer.parseInt(time[2].split("\\.")[0]);
                    //int millisec = Integer.parseInt(time[2].split("\\.")[1]);
                    int elapsedTime = (hours * 60 * 60) + (minutes * 60) + seconds;
                    assert episode.getContentDuration() != 0;
                    Platform.runLater(() -> {
                        SvtpkApplication.updateLoadingBar(((double) elapsedTime / episode.getContentDuration()));
                    });
                }
            }
            process.waitFor();
            Platform.runLater(() -> {
                SvtpkApplication.updateLoadingBar(1.0);
            });

            //flytta skiten
            Path source = Paths.get(System.getProperty("user.dir")).resolve(filename);
            Path target = Paths.get(settings.getPath()).resolve(filename);

            Files.copy(source, target, REPLACE_EXISTING);
            Files.delete(source);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
