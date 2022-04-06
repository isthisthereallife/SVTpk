package org.m.svtpk.services;

import org.m.svtpk.entity.EpisodeEntity;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.util.Calendar;
import java.util.Objects;

@Service
public class EpisodeService {

    public EpisodeEntity findEpisode(String address) {
        System.out.println("address supplied to findEpisode: " + address);
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

                    ResponseEntity<String> response = new RestTemplate().exchange(address, HttpMethod.GET, new HttpEntity<String>(setHeaders()), String.class);
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
        System.out.println("Bild finns på: " + episode.getImageURL());
        return episode;
    }

    private String getImgURL(String addressWithId) throws NullPointerException, HttpClientErrorException {
        String HTML_URI = addressWithId.split("\\?id=")[0];
        ResponseEntity<String> response = new RestTemplate().exchange(HTML_URI, HttpMethod.GET, new HttpEntity<String>(setHeaders()), String.class);
        return response.getBody().split("data-src=\"")[1].split("\"")[0];
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
            Calendar c = Calendar.getInstance();
            String filename = episode.getProgramTitle() + "-" + episode.getEpisodeTitle() + "-" + c.get(Calendar.YEAR) + c.get(Calendar.MONTH) + c.get(Calendar.DAY_OF_MONTH) + 1 + "-" + c.get(Calendar.HOUR) + c.get(Calendar.MINUTE);
            String filetype = ".txt";
            File file = new File(filename + filetype);
            FileWriter fw = new FileWriter(filename + filetype);
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
