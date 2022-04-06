package org.m.svtpk.services;

import org.m.svtpk.entity.EpisodeEntity;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.Objects;

@Service
public class EpisodeService {
    public EpisodeEntity getEpisodeInfo(String address) {
        EpisodeEntity episode = new EpisodeEntity();
        System.out.println("getEpisodeInfo says address = " + address);
        String episodeInfo = "";
        String[] id = address.split("id=");
        if (address.contains("id=") && id.length > 1 && id[1].trim().length() > 5) {
            System.out.println("Close enough, trying to get: " + address);
            String episodeId = address.split("id=")[1];
            String URI = "https://api.svt.se/video/" + episodeId;
            ResponseEntity<String> response = null;
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = setHeaders();
            HttpEntity<String> entity = new HttpEntity<String>(headers);
            try {
                response = restTemplate.exchange(URI, HttpMethod.GET, entity, String.class);
                if (response.getStatusCode().equals(HttpStatus.OK)) {
                    String res = response.getBody();

                    episode.setSvtId(res.split("svtId\":\"")[1].split("\"")[0]);
                    episode.setProgramTitle(res.split("programTitle\":\"")[1].split("\",")[0]);
                    episode.setEpisodeTitle(res.split("episodeTitle\":\"")[1].split("\",")[0]);
                    episode.setContentDuration(Integer.parseInt(res.split("contentDuration\":")[1].split(",")[0]));
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        return episode;
    }


    public void copyEpisodeToDisk(EpisodeEntity episode) {
        String URI = "https://api.svt.se/video/" + episode.getSvtId();
        ResponseEntity<String> response = null;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        try {
            response = restTemplate.exchange(URI, HttpMethod.GET, entity, String.class);
            String resolveURI = response.getBody().split("dash-full.mpd\",\"resolve\":\"")[1].split("\",")[0];
            System.out.println("RESOLVE URI:" + resolveURI);
            response = restTemplate.exchange(resolveURI, HttpMethod.GET, entity, String.class);
            System.out.println(response.getBody());

            String location = response.getBody().split("location\":\"")[1].split("\"")[0];
            location = "https://api.svt.se/ditto/api/v1/web?manifestUrl=" + location + "&excludeCodecs=hvc&excludeCodecs=ac-3";
            response = restTemplate.exchange(location, HttpMethod.GET, entity, String.class);
            System.out.println(response.getBody());
            String resbody = response.getBody();

            //GET BASE URL
            String BASE_URL = resbody.split("<BaseURL>")[1].split("</BaseURL")[0];
            String SUBS_BASE_URL = resbody.split("</BaseURL>")[1].split("<BaseURL>")[1].split("</BaseURL>")[0];
            //RESOLUTION IS CHOSEN AT THIS POINT
            String initial = resbody.split("<SegmentTemplate initialization=\"")[1].split("\"")[0];


            //response = restTemplate.exchange(BASE_URL+initial, HttpMethod.GET, entity, String.class);
            response = restTemplate.exchange(BASE_URL + SUBS_BASE_URL, HttpMethod.GET, entity, String.class);
            //System.out.println("response.body: " + response.getBody());
            Calendar c = Calendar.getInstance();
            String subsname = episode.getProgramTitle()+"-"+episode.getEpisodeTitle()+"-"+c.get(Calendar.YEAR)+c.get(Calendar.MONTH)+c.get(Calendar.DAY_OF_MONTH)+1+"-"+c.get(Calendar.HOUR)+c.get(Calendar.MINUTE);
            File mp4 = new File(subsname);
            FileWriter fw = new FileWriter(subsname);
            fw.write(Objects.requireNonNull(response.getBody()));
            fw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    static HttpHeaders setHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; rv:91.0) Gecko/20100101 Firefox/91.0");
        return headers;
    }
}
