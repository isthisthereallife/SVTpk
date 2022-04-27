package org.m.svtpk.utils;

import javafx.application.Platform;
import org.m.svtpk.SvtpkApplication;
import org.m.svtpk.entity.EpisodeEntity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

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
        String[] cmd = {
                "ffmpeg",
                "-i",
                "\"" + episode.getMpdURL() + "\"",
                map,
                vidArgs,
                map,
                audArgs,
                map,
                subArgs,
                filename
        };

        System.out.println("command:" + Arrays.toString(cmd));

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(new File(System.getProperty("user.dir")));
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectErrorStream(true);
        try {
            System.out.println("innan");
            Process process = pb.start();
            process.getInputStream().close();
            Platform.runLater(() -> {
                SvtpkApplication.updateLoadingBar(50);
            });

            process.waitFor();
            System.out.println("efter");
            Platform.runLater(() -> {
                SvtpkApplication.updateLoadingBar(100);
            });
            System.out.println("Exited with error code " + process.waitFor());

            //kolla om det finns en fil där med samma namn. om så, lägg till en 1a på namnet lol xP
            //flytta skiten
            Path source = Paths.get(System.getProperty("user.dir")).resolve(filename);
            Path target = Paths.get(settings.getPath()).resolve(filename);

            Files.copy(source, target, REPLACE_EXISTING);
            System.out.println("Flyttat!");
            Files.delete(source);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("DET FUNKADE?????");
    }
}
