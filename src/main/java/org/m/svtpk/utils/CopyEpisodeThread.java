package org.m.svtpk.utils;

import javafx.application.Platform;
import org.m.svtpk.SvtpkApplication;
import org.m.svtpk.entity.EpisodeEntity;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class CopyEpisodeThread implements Runnable {
    private final EpisodeEntity episode;

    public CopyEpisodeThread(EpisodeEntity episode) {
        this.episode = episode;
    }

    @Override
    public void run() {
        Settings settings = Settings.load();

        String vidArgs = "-map 0:" + episode.getAvailableResolutions().get(settings.getResolution()).getId();
        String audArgs = " -map 0:" + episode.getAvailableAudio().get(settings.getAudio()).getId();
        String subArgs = settings.getSubs().equalsIgnoreCase("Inga undertexter") ?
                ""
                :
                " -map 0:" + episode.getAvailableSubs().get(settings.getSubs()).getId();


        String filename = episode.getProgramTitle() + "-" + episode.getEpisodeTitle();
        String[] cmd = {
                "ffmpeg",
                "-i",
                "\"" + episode.getMpdURL() + "\"",
                //vidArgs,
                //audArgs,
                //subArgs,
                StringHelpers.fileNameFixerUpper(filename).concat(".mkv")};

        System.out.println("command:" + Arrays.toString(cmd));

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(new File(System.getProperty("user.dir")));
        pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
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

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("DET FUNKADE?????");
    }
}
