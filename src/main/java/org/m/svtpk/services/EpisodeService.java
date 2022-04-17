package org.m.svtpk.services;

import org.m.svtpk.SvtpkApplication;
import org.m.svtpk.entity.EpisodeEntity;
import org.m.svtpk.utils.RunnableCopier;
import org.m.svtpk.utils.StringHelpers;
import org.springframework.http.*;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
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
        ResponseEntity<String> response = new RestTemplate().exchange(HTML_URI, HttpMethod.GET, new HttpEntity<String>(getHeaders()), String.class);
        if (response.getBody() == null) {
            return "";
        }
        return response.getBody().split("data-src=\"")[1].split("\"")[0];
    }

    public HttpStatus copyEpisodeToDisk(EpisodeEntity episode) {
        String URI = "https://api.svt.se/video/" + episode.getSvtId();
        ResponseEntity<String> response;
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(getHeaders());
        try {
            response = restTemplate.exchange(URI, HttpMethod.GET, entity, String.class);
            if (response.getBody() == null) return HttpStatus.NO_CONTENT;
            String resolveURI = response.getBody().split("dash-full.mpd\",\"resolve\":\"")[1].split("\",")[0];

            response = restTemplate.exchange(resolveURI, HttpMethod.GET, entity, String.class);
            response = restTemplate.exchange("https://api.svt.se/ditto/api/v1/web?manifestUrl=" + response.getBody().split("location\":\"")[1].split("\"")[0] + "&excludeCodecs=hvc&excludeCodecs=ac-3", HttpMethod.GET, entity, String.class);
            String resbody = response.getBody();

            //GET BASE URL
            String BASE_URL = resbody.split("<BaseURL>")[1].split("</BaseURL")[0];
            //String SUBS_BASE_URL = resbody.split("</BaseURL>")[1].split("<BaseURL>")[1].split("</BaseURL>")[0];
            //RESOLUTION IS CHOSEN AT THIS POINT
            String initial = resbody.split("<SegmentTemplate initialization=\"")[1].split("\"")[0];
            String partUrl = initial.split("init")[0];
            List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
            int range = Integer.parseInt(resbody.split("<S t=\"")[1].split("r=\"")[1].split("\"/>")[0]);
            messageConverters.add(new ByteArrayHttpMessageConverter());
            RestTemplate restTemplateByte = new RestTemplate(messageConverters);
            System.out.println("Getting part init");
            ResponseEntity<byte[]> res = restTemplateByte.exchange(BASE_URL + initial, HttpMethod.GET, entity, byte[].class);
            String listOfPartsString = "";

            if (res.getStatusCode() == HttpStatus.OK && res.getBody() != null) {
                Calendar c = Calendar.getInstance();

                /*String filename = fileNameFixerUpper(episode.getProgramTitle() + "-" + episode.getEpisodeTitle()
                        + "-" + c.get(Calendar.YEAR) + "_" + ((c.get(Calendar.MONTH) + 1) < 10 ? "0" + (c.get(Calendar.MONTH) + 1)
                        : (c.get(Calendar.MONTH) + 1)) + "_" + c.get(Calendar.DAY_OF_MONTH) + "_" + (c.get(Calendar.HOUR) < 10 ? "0"
                        + c.get(Calendar.HOUR) : c.get(Calendar.HOUR)) + (c.get(Calendar.MINUTE) < 10 ? "0" + c.get(Calendar.MINUTE) : c.get(Calendar.MINUTE)
                        + "-PART_"));

                 */
                String filename = StringHelpers.fileNameFixerUpper(episode.getSvtId() + "-PART_");
                String partInit = filename.concat("init");
                String filetype = ".mp4";
                Files.write(Paths.get(partInit + filetype), res.getBody());
                listOfPartsString = "file '" + partInit + filetype + "'";
                // do the loop,
                for (int i = 1; i <= range + 2; i++) {
                    System.out.println("Getting part " + i);
                    try {
                        RunnableCopier runnable = new RunnableCopier("Thread " + i);
                        runnable.start();
                        runnable.run();
                        String filen = runnable.getAndSave(i, filename, filetype, BASE_URL + partUrl + i + ".mp4", HttpMethod.GET, entity);

                        listOfPartsString = listOfPartsString.concat("\nfile '" + filen + "'");
                        SvtpkApplication.updateLoadingBar(((double) i / (double) (range + 2)) * 100);
                    } catch (HttpClientErrorException e) {
                        e.printStackTrace();
                        return HttpStatus.NOT_FOUND;
                    }
                }
            }
            Files.write(Paths.get("list.txt"), Collections.singleton(listOfPartsString));
        } catch (IOException e) {
            e.printStackTrace();
            return HttpStatus.CONFLICT;
        }
        return HttpStatus.OK;
    }


    static HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; rv:91.0) Gecko/20100101 Firefox/91.0");
        return headers;
    }

}
