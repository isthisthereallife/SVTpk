package org.m.svtpk.services;

import org.m.svtpk.entity.AudioReferencesEntity;
import org.m.svtpk.entity.EpisodeEntity;
import org.m.svtpk.entity.SubtitleReferencesEntity;
import org.m.svtpk.entity.VideoReferencesEntity;
import org.m.svtpk.utils.RunnableCopier;
import org.m.svtpk.utils.Settings;
import org.m.svtpk.utils.StringHelpers;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;

@Service
public class EpisodeService {

    public EpisodeEntity findEpisode(String address) {
        System.out.println("address supplied to findEpisode: " + address);
        address = address.replace(" ", "");
        System.out.println("removed spaces");
        EpisodeEntity episode = new EpisodeEntity();
        if (address.length() > 9) {
            try {
                URI uri = URI.create(address);
                //address = URLEncoder.encode(address, StandardCharsets.UTF_8);
                if (uri.isAbsolute() && address.contains("\\id=")) {
                    System.out.println("uri was absolute and contained id");
                    episode = getEpisodeInfo(address);
                } else if (uri.isAbsolute()) {
                    System.out.println("uri was absolute");

                    ResponseEntity<String> response = new RestTemplate().exchange(address, HttpMethod.GET, new HttpEntity<String>(getHeaders()), String.class);
                    if (response.getBody() != null) {
                        String id = response.getBody().split("data-rt=\"top-area-play-button")[1].split("\\?")[1].split("\"")[0];
                        episode = getEpisodeInfo(address + "?" + id);
                    }
                } else System.out.println("URI was not absolute, didn't search.");
            } catch (HttpClientErrorException e) {
                System.out.println("Couldn't find episode");
                System.out.println(e.getMessage());
            } catch (NullPointerException e) {
                System.out.println("Error in body, error in regex?");
                System.out.println(e.getMessage());
            } catch (IllegalArgumentException | ResourceAccessException e) {
                System.out.println(e.getMessage());
            }
        }

        System.out.println("returning episode named: " + episode.getEpisodeTitle());
        return episode;
    }

    public EpisodeEntity getEpisodeInfo(String address) {
        EpisodeEntity episode = new EpisodeEntity();
        System.out.println("getEpisodeInfo says address = " + address);
        String[] id = address.split("id=");
        if (id.length > 1 && id[1].trim().length() > 5) {
            System.out.println("Close enough, trying to get: " + address);
            String episodeId = address.split("id=")[1];
            System.out.println("episodeId= "+episodeId);
            String URI = "https://api.svt.se/video/" + episodeId;

            ResponseEntity<String> response;
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = getHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            try {
                response = restTemplate.exchange(URI, HttpMethod.GET, entity, String.class);
                if (response.getStatusCode().equals(HttpStatus.OK)) {
                    String res = response.getBody();
                    assert res != null;
                    //check if it is a live stream
                    if (res.contains("\"live\":true")) return new EpisodeEntity(true);
                    episode.setSvtId(res.split("svtId\":\"")[1].split("\"")[0]);
                    episode.setProgramTitle(res.split("programTitle\":\"")[1].split("\",")[0]);
                    episode.setEpisodeTitle(res.split("episodeTitle\":\"")[1].split("\",")[0]);
                    episode.setContentDuration(Integer.parseInt(res.split("contentDuration\":")[1].split(",")[0]));

                    episode = updateEpisodeLinks(episode);
                    try {
                        episode.setImageURL(getImgURL(address));
                    } catch (Exception e) {
                        System.out.println("bildhämtningen fuckade upp");
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                System.out.println("hämtningen av avsnittsinfon fuckade upp");
                System.out.println(e.getMessage());
            }
        }

       /* System.out.println("Bild finns på: " + episode.getImageURL());
        System.out.println("Dessa ljudkanaler finns: ");

        HashMap<String, AudioReferencesEntity> au = episode.getAvailableAudio();

        for (Map.Entry<String, AudioReferencesEntity> entry : au.entrySet()) {
            System.out.println("key:" + entry.getKey() + "\tvalue.getLabel:" + entry.getValue().getLabel());
        }
        System.out.println("Dessa upplösningar finns: ");
        for (Map.Entry<String, VideoReferencesEntity> entry : episode.getAvailableResolutions().entrySet()) {
            System.out.println("key:" + entry.getKey() + "\tvalue:" + entry.getValue());
        }
        System.out.println("Dessa subs finns: ");
        for (Map.Entry<String, SubtitleReferencesEntity> entry : episode.getAvailableSubs().entrySet()) {
            System.out.println("key:" + entry.getKey() + "\tvalue.getLabel:" + entry.getValue().getLabel());
        }

        */
        return episode;
    }

    private String getImgURL(String addressWithId) throws NullPointerException, HttpClientErrorException {
        String HTML_URI = addressWithId.split("\\?id=")[0];
        ResponseEntity<String> response = new RestTemplate().exchange(HTML_URI, HttpMethod.GET, new HttpEntity<String>(getHeaders()), String.class);
        if (response.getBody() == null) {
            return "";
        }
        return response.getBody().split("data-src=\"")[1].split("\"")[0];
    }

    public EpisodeEntity updateEpisodeLinks(EpisodeEntity episode) {
        String URI = "https://api.svt.se/video/" + episode.getSvtId();
        ResponseEntity<String> response;
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(getHeaders());
        response = restTemplate.exchange(URI, HttpMethod.GET, entity, String.class);
        if (response.getBody() == null) return new EpisodeEntity();
        String resolveURI = response.getBody().split("dash-full.mpd\",\"resolve\":\"")[1].split("\",")[0];

        response = restTemplate.exchange(resolveURI, HttpMethod.GET, entity, String.class);
        episode.setMpdURL("https://api.svt.se/ditto/api/v1/web?manifestUrl=" + response.getBody().split("location\":\"")[1].split("\"")[0] + "&excludeCodecs=hvc&excludeCodecs=ac-3");

        response = restTemplate.exchange(episode.getMpdURL(), HttpMethod.GET, entity, String.class);

        String resbody = response.getBody();

        if (resbody == null) return new EpisodeEntity();
        if (resbody.contains("\"live\":true")) return new EpisodeEntity(true);

        String BASE_URL = resbody.split("<BaseURL>")[1].split("</BaseURL>")[0];

        //Set Available resolutions
        String[] adaptationSet = resbody.split("<AdaptationSet");
        int streamId = 0;
        for (String set : adaptationSet) {
            if (set.contains("contentType=\"video")) {
                //video
                String[] representation = set.split("</AdaptationSet>")[0].split(("<Representation"));
                for (String rep : representation) {
                    if (rep.contains("mimeType")) {
                        System.out.println("nummer " + streamId + " är " + rep);
                        VideoReferencesEntity vid = new VideoReferencesEntity();
                        vid.setId(streamId);
                        vid.setUrl(BASE_URL + rep.split("media=\"")[1].split("\\$Number\\$")[0]);
                        vid.setSuffix(rep.split("\\$Number\\$")[1].split("\"")[0]);
                        vid.setHeight(rep.split("height=\"")[1].split("\"")[0]);
                        vid.setWidth(rep.split("width=\"")[1].split("\"")[0]);
                        vid.setCodecs(rep.split("codecs=\"")[0].split("\"")[0]);
                        vid.setRange(Integer.parseInt(resbody.split("<S t=\"")[1].split("r=\"")[1].split("\"/>")[0]));
                        episode.addAvailableResolutions(vid.getHeight(), vid);
                        streamId++;
                    }
                }
            } else if (set.contains("contentType=\"audio\"")) {
                //audio
                System.out.println("nummer " + streamId + " är " + set);

                AudioReferencesEntity aud = new AudioReferencesEntity();
                aud.setId(streamId);
                aud.setLabel(set.split("<Label>")[1].split("</Label>")[0]);
                aud.setUrl(BASE_URL + set.split("media=\"")[1].split("\\$Number\\$")[0]);
                aud.setSuffix(set.split("\\$Number\\$")[1].split("\"")[0]);
                aud.setRange(Integer.parseInt(resbody.split("<S t=\"")[1].split("r=\"")[1].split("\"/>")[0]));
                episode.addAvailableAudio(aud.getLabel(), aud);
                streamId++;
            } else if (set.contains("mimeType=\"text")) {
                //subs
                System.out.println("nummer " + streamId + " är " + set);

                SubtitleReferencesEntity sub = new SubtitleReferencesEntity();
                sub.setId(streamId);
                sub.setLabel(set.split("<Label>")[1].split("</Label")[0]);
                sub.setUrl(BASE_URL + set.split("<BaseURL>")[1].split("</BaseURL>")[0]);
                episode.addAvailableSubs(sub);
                streamId++;
            }
        }
        return episode;
    }

    public HttpStatus copyEpisodeToDisk(EpisodeEntity episode) {
        RunnableCopier runnable = new RunnableCopier("New runnable RunnableCopier started");
        runnable.start();
        runnable.run();
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
            process.waitFor();
            System.out.println("efter");
            System.out.println("Exited with error code " + process.waitFor());

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("DET FUNKADE?????");
        return HttpStatus.OK;
    }


    static HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; rv:91.0) Gecko/20100101 Firefox/91.0");
        return headers;
    }

}
