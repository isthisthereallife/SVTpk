package org.m.svtpk.services;

import org.m.svtpk.entity.AudioReferencesEntity;
import org.m.svtpk.entity.EpisodeEntity;
import org.m.svtpk.entity.SubtitleReferencesEntity;
import org.m.svtpk.entity.VideoReferencesEntity;
import org.m.svtpk.utils.RunnableCopier;
import org.m.svtpk.utils.Settings;
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
        System.out.println("Bild finns på: " + episode.getImageURL());
        System.out.println("Dessa ljudkanaler finns: ");

        HashMap<String, AudioReferencesEntity> au = episode.getAvailableAudio();
        HashMap<String, HashMap> selects = new HashMap<String, HashMap>();

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
        response = restTemplate.exchange("https://api.svt.se/ditto/api/v1/web?manifestUrl=" + response.getBody().split("location\":\"")[1].split("\"")[0] + "&excludeCodecs=hvc&excludeCodecs=ac-3", HttpMethod.GET, entity, String.class);

        String resbody = response.getBody();
        if (resbody == null) return new EpisodeEntity();
        String BASE_URL = resbody.split("<BaseURL>")[1].split("</BaseURL>")[0];

        //Set Available resolutions
        String[] adaptationSet = resbody.split("<AdaptationSet");
        for (String set : adaptationSet) {
            if (set.contains("contentType=\"video")) {
                //video
                String[] representation = set.split("</AdaptationSet>")[0].split(("<Representation"));
                for (String rep : representation) {
                    if (rep.contains("mimeType")) {
                        VideoReferencesEntity vid = new VideoReferencesEntity();
                        vid.setId(Integer.parseInt(rep.split("id=\"")[1].split("\"")[0]));
                        vid.setUrl(BASE_URL + rep.split("media=\"")[1].split("\\$Number\\$")[0]);
                        vid.setSuffix(rep.split("\\$Number\\$")[1].split("\"")[0]);
                        vid.setHeight(rep.split("height=\"")[1].split("\"")[0]);
                        vid.setWidth(rep.split("width=\"")[1].split("\"")[0]);
                        vid.setCodecs(rep.split("codecs=\"")[0].split("\"")[0]);
                        vid.setRange(Integer.parseInt(resbody.split("<S t=\"")[1].split("r=\"")[1].split("\"/>")[0]));
                        episode.addAvailableResolutions(vid.getHeight(), vid);
                    }
                }
            } else if (set.contains("contentType=\"audio\"")) {
                //audio
                AudioReferencesEntity aud = new AudioReferencesEntity();
                aud.setLabel(set.split("<Label>")[1].split("</Label>")[0]);
                aud.setUrl(BASE_URL + set.split("media=\"")[1].split("\\$Number\\$")[0]);
                aud.setSuffix(set.split("\\$Number\\$")[1].split("\"")[0]);
                aud.setRange(Integer.parseInt(resbody.split("<S t=\"")[1].split("r=\"")[1].split("\"/>")[0]));
                episode.addAvailableAudio(aud.getLabel(), aud);
            } else if (set.contains("mimeType=\"text")) {
                //subs
                SubtitleReferencesEntity sub = new SubtitleReferencesEntity();
                sub.setLabel(set.split("<Label>")[1].split("</Label")[0]);
                sub.setUrl(BASE_URL + set.split("<BaseURL>")[1].split("</BaseURL>")[0]);
                episode.addAvailableSubs(sub);
            }

        }
        return episode;
    }

    public HttpStatus copyEpisodeToDisk(EpisodeEntity episode) {
        Settings settings = Settings.load();
        String URI = "https://api.svt.se/video/" + episode.getSvtId();
        ResponseEntity<String> response;
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(getHeaders());
        response = restTemplate.exchange(URI, HttpMethod.GET, entity, String.class);
        if (response.getBody() == null) return HttpStatus.NO_CONTENT;
        String resolveURI = response.getBody().split("dash-full.mpd\",\"resolve\":\"")[1].split("\",")[0];

        response = restTemplate.exchange(resolveURI, HttpMethod.GET, entity, String.class);
        response = restTemplate.exchange("https://api.svt.se/ditto/api/v1/web?manifestUrl=" + response.getBody().split("location\":\"")[1].split("\"")[0] + "&excludeCodecs=hvc&excludeCodecs=ac-3", HttpMethod.GET, entity, String.class);
        String resbody = response.getBody();
        String BASE_URL = resbody.split("<BaseURL>")[1].split("</BaseURL")[0];

        System.out.println("availabel resoulutions: " + episode.getAvailableResolutions());

        //GET BASE URL
        //String SUBS_BASE_URL = resbody.split("</BaseURL>")[1].split("<BaseURL>")[1].split("</BaseURL>")[0];
        //System.out.println("Subs BASE ="+SUBS_BASE_URL);


        //find best resolution, audio, subs based on settings
        VideoReferencesEntity vid = episode.getBestAvailableResolutions(settings.getResolution());
        AudioReferencesEntity audio = episode.getSelectedAudio(settings.getAudio());
        SubtitleReferencesEntity subs = episode.getSelectedSubs(settings.getSubs());

        String subsString;
        ArrayList<byte[]> videoBytes = getVideo(vid);
        ArrayList<byte[]> audioBytes = getAudio(audio);
        if (subs != null) subsString = getAndSaveSubs(subs);

        //muxxing(vid,audio,subs);

        return HttpStatus.OK;
    }

    private String getAndSaveSubs(SubtitleReferencesEntity subs) {
        HttpEntity<String> entity = new HttpEntity<>(getHeaders());
        String subsString = new RestTemplate().getForEntity(subs.getUrl(), String.class).getBody();
        try {
            Files.write(Paths.get(subs.getLabel().concat(".txt")), Collections.singleton(subsString));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return subsString;
    }

    private ArrayList<byte[]> getVideo(VideoReferencesEntity v) {
        RunnableCopier runnable = new RunnableCopier("New runnable RunnableCopier started");
        runnable.start();
        runnable.run();
        ArrayList<byte[]> videoArray = new ArrayList<>();
        HttpEntity<String> entity = new HttpEntity<>(getHeaders());
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
        messageConverters.add(new ByteArrayHttpMessageConverter());
        RestTemplate restTemplateByte = new RestTemplate(messageConverters);

        videoArray.add(runnable.getAndSave(0, String.valueOf(v.getId()), v.getSuffix(), v.getUrl() + "init", HttpMethod.GET, entity));

        //videoArray.add(restTemplateByte.exchange(v.getUrl() + "init" + v.getSuffix(), HttpMethod.GET, entity, byte[].class).getBody());
        for (int i = 1; i < v.getRange() + 2; i++) {
            videoArray.add(runnable.getAndSave(i, String.valueOf(v.getId()), v.getSuffix(), v.getUrl() + i, HttpMethod.GET, entity));
            //videoArray.add(restTemplateByte.exchange(v.getUrl() + i + v.getSuffix(), HttpMethod.GET, entity, byte[].class).getBody());
        }
        return videoArray;
    }

    private ArrayList<byte[]> getAudio(AudioReferencesEntity a) {
        RunnableCopier runnable = new RunnableCopier("New runnable RunnableCopier started");
        runnable.start();
        runnable.run();
        ArrayList<byte[]> audioArray = new ArrayList<>();
        HttpEntity<String> entity = new HttpEntity<>(getHeaders());
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
        messageConverters.add(new ByteArrayHttpMessageConverter());
        RestTemplate restTemplateByte = new RestTemplate(messageConverters);
        //audioArray.add(restTemplateByte.exchange(a.getUrl() + "init" + a.getSuffix(), HttpMethod.GET, entity, byte[].class).getBody());
        audioArray.add(runnable.getAndSave(0, String.valueOf(a.getLabel()), a.getSuffix(), a.getUrl() + "init", HttpMethod.GET, entity));
        for (int i = 1; i < a.getRange() + 2; i++) {
            //audioArray.add(restTemplateByte.exchange(a.getUrl() + i + a.getSuffix(), HttpMethod.GET, entity, byte[].class).getBody());
            audioArray.add(runnable.getAndSave(i, String.valueOf(a.getLabel()), a.getSuffix(), a.getUrl() + i, HttpMethod.GET, entity));
        }
        return audioArray;
    }

    /*


    String initial = resbody.split("<SegmentTemplate initialization=\"")[1].split("\"")[0];
    String partUrl = initial.split("init")[0];
    List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
    int range = Integer.parseInt(resbody.split("<S t=\"")[1].split("r=\"")[1].split("\"/>")[0]);
            messageConverters.add(new

    ByteArrayHttpMessageConverter());
    RestTemplate restTemplateByte = new RestTemplate(messageConverters);
            System.out.println("Getting part init");
    ResponseEntity<byte[]> res = restTemplateByte.exchange(BASE_URL + initial, HttpMethod.GET, entity, byte[].class);
    String listOfPartsString = "";

            if(res.getStatusCode()==HttpStatus.OK &&res.getBody()!=null)

    {
        Calendar c = Calendar.getInstance();

                /*String filename = fileNameFixerUpper(episode.getProgramTitle() + "-" + episode.getEpisodeTitle()
                        + "-" + c.get(Calendar.YEAR) + "_" + ((c.get(Calendar.MONTH) + 1) < 10 ? "0" + (c.get(Calendar.MONTH) + 1)
                        : (c.get(Calendar.MONTH) + 1)) + "_" + c.get(Calendar.DAY_OF_MONTH) + "_" + (c.get(Calendar.HOUR) < 10 ? "0"
                        + c.get(Calendar.HOUR) : c.get(Calendar.HOUR)) + (c.get(Calendar.MINUTE) < 10 ? "0" + c.get(Calendar.MINUTE) : c.get(Calendar.MINUTE)
                        + "-PART_"));

                 */


    // do the loop,


    private HttpStatus copySubsToDisk(EpisodeEntity episode) {

        //String SUBS_BASE_URL = resbody.split("</BaseURL>")[1].split("<BaseURL>")[1].split("</BaseURL>")[0];
        return null;
    }

    static HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; rv:91.0) Gecko/20100101 Firefox/91.0");
        return headers;
    }

}
