package org.m.svtpk.services;

import org.m.svtpk.entity.AudioReferencesEntity;
import org.m.svtpk.entity.EpisodeEntity;
import org.m.svtpk.entity.SubtitleReferencesEntity;
import org.m.svtpk.entity.VideoReferencesEntity;
import org.m.svtpk.utils.StringHelpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class EpisodeService {

    public EpisodeEntity findEpisode(String address) {
        System.out.println("address supplied to findEpisode: " + address);
        address = address.replace(" ", "");
        System.out.println("removed spaces");
        EpisodeEntity episode = new EpisodeEntity();
        if (address.length() > 9) {
            try {
                URI uri = URI.create(address);
                URL url = new URL(address);
                //address = URLEncoder.encode(address, StandardCharsets.UTF_8);
                if (uri.isAbsolute() && address.contains("\\id=")) {
                    System.out.println("uri was absolute and contained id");
                    episode = getEpisodeInfo(address);
                } else if (uri.isAbsolute()) {
                    System.out.println("uri was absolute");

                    String res = connectToURLReturnBodyAsString(url);


                    //ResponseEntity<String> response = new RestTemplate().exchange(address, HttpMethod.GET, new HttpEntity<String>(getHeaders()), String.class);
                    if (!res.equals("")) {
                        String id = res.split("data-rt=\"top-area-play-button")[1].split("\\?")[1].split("\"")[0];
                        episode = getEpisodeInfo(address + "?" + id);
                    }
                } else System.out.println("URI was not absolute, didn't search.");
            } catch (IOException e) {
                e.printStackTrace();
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
            System.out.println("episodeId= " + episodeId);
            String URI = "https://api.svt.se/video/" + episodeId;
            String body = "";
            try {
                body = connectToURLReturnBodyAsString(new URL("https://api.svt.se/video/" + episodeId));

            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (!body.equals("")) {
                    //check if it is a live stream
                    if (body.contains("\"live\":true")) return new EpisodeEntity(true);
                    episode.setSvtId(body.split("svtId\":\"")[1].split("\"")[0]);
                    episode.setProgramTitle(body.split("programTitle\":\"")[1].split("\",")[0]);
                    episode.setEpisodeTitle(body.split("episodeTitle\":\"")[1].split("\",")[0]);
                    episode.setContentDuration(Integer.parseInt(body.split("contentDuration\":")[1].split(",")[0]));
                    episode.setFilename(StringHelpers.fileNameFixerUpper(episode.getProgramTitle() + "-" + episode.getEpisodeTitle()).concat(".mkv"));
                    episode = updateEpisodeLinks(episode);
                    try {
                        episode.setImageURL(new URL(getImgURL(address)));
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

    private String getImgURL(String addressWithId) throws NullPointerException {
        String body = "";

        try {
            body = connectToURLReturnBodyAsString(new URL(addressWithId.split("\\?id=")[0]));
            if (!body.equals("")) {
                return body.split("data-src=\"")[1].split("\"")[0];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public EpisodeEntity updateEpisodeLinks(EpisodeEntity episode) {
        String episodeInfoString = "";
        String URI = "https://api.svt.se/video/" + episode.getSvtId();
        String body = "";
        try {
            //anrop 1
            body = connectToURLReturnBodyAsString(new URL("https://api.svt.se/video/" + episode.getSvtId()));
            if (!body.equals("")) {
                //anrop 2
                body = connectToURLReturnBodyAsString(new URL(body.split("dash-full.mpd\",\"resolve\":\"")[1].split("\",")[0]));
                if (!body.equals("")) {
                    episode.setMpdURL(new URL("https://api.svt.se/ditto/api/v1/web?manifestUrl=" + body.split("location\":\"")[1].split("\"")[0] + "&excludeCodecs=hvc&excludeCodecs=ac-3"));
                    //anrop 3, till mpdURLen
                    body = connectToURLReturnBodyAsString(episode.getMpdURL());
                    if (!body.equals("")) {
                        episodeInfoString = body;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("episodeInfoString:" + episodeInfoString);
        if (episodeInfoString == "") return new EpisodeEntity();
        if (episodeInfoString.contains("\"live\":true")) return new EpisodeEntity(true);

        String BASE_URL = episodeInfoString.split("<BaseURL>")[1].split("</BaseURL>")[0];

        //Set Available resolutions
        String[] adaptationSet = episodeInfoString.split("<AdaptationSet");
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
                        vid.setRange(Integer.parseInt(episodeInfoString.split("<S t=\"")[1].split("r=\"")[1].split("\"/>")[0]));
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
                aud.setRange(Integer.parseInt(episodeInfoString.split("<S t=\"")[1].split("r=\"")[1].split("\"/>")[0]));
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


    private String connectToURLReturnBodyAsString(URL url) {
        BufferedReader reader = null;
        StringBuilder stringBuilder = null;
        try {
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            huc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; rv:91.0) Gecko/20100101 Firefox/91.0");
            huc.connect();
            // read the output from the server
            reader = new BufferedReader(new InputStreamReader(huc.getInputStream()));
            stringBuilder = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            huc.disconnect();
            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
        System.out.println("returning empty string, something went wronk");
        return "";
    }
}
