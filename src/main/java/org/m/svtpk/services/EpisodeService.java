package org.m.svtpk.services;

import org.m.svtpk.entity.AudioReferencesEntity;
import org.m.svtpk.entity.EpisodeEntity;
import org.m.svtpk.entity.SubtitleReferencesEntity;
import org.m.svtpk.entity.VideoReferencesEntity;
import org.m.svtpk.utils.StringHelpers;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static org.m.svtpk.utils.HttpBodyGetter.connectToURLReturnBodyAsString;

public class EpisodeService {

    public EpisodeEntity findEpisode(String address) {
        address = address.replace(" ", "");
        EpisodeEntity episode = new EpisodeEntity();
        if (address.length() > 9) {
            try {
                URI uri = URI.create(address);
                URL url = new URL(address);
                //address = URLEncoder.encode(address, StandardCharsets.UTF_8);
                if (uri.isAbsolute() && address.contains("\\id=")) {
                    episode = getEpisodeInfo(address);
                } else if (uri.isAbsolute()) {
                    if (url.toString().contains("svtplay")) {
                        String res = connectToURLReturnBodyAsString(url);
                        if (!res.equals("")) {
                            String id = res.split("data-rt=\"top-area-play-button")[1].split("\\?")[1].split("\"")[0];
                            episode = getEpisodeInfo(address + "?" + id);
                        } else System.out.println("URI was not absolute, didn't search.");
                    } else {
                        System.out.println("Vänligen ange en adress till SVT Play");
                    }
                }
            } catch (MalformedURLException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return episode;
    }

    public EpisodeEntity getEpisodeInfo(String address) {
        EpisodeEntity episode = new EpisodeEntity();
        String[] id = address.split("id=");
        if (id.length > 1 && id[1].trim().length() > 5) {
            String episodeId = address.split("id=")[1];
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
                        System.out.println("Could not get episode image");
                    }
                }
            } catch (Exception e) {
                System.out.println("Could not get episode info");
                System.out.println(e.getMessage());
                return new EpisodeEntity();
            }
        }
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

        if (episodeInfoString.equals("")) return new EpisodeEntity();
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
                AudioReferencesEntity aud = new AudioReferencesEntity();
                aud.setId(streamId);
                try {
                    aud.setLabel(set.split("<Label>")[1].split("</Label>")[0]);
                } catch (IndexOutOfBoundsException e) {
                    System.out.println("Nån på svt har glömt ge ljudspåret en Label :/ Sätter den till \"Svenska\"");
                    aud.setLabel("Svenska");
                }
                aud.setUrl(BASE_URL + set.split("media=\"")[1].split("\\$Number\\$")[0]);
                aud.setSuffix(set.split("\\$Number\\$")[1].split("\"")[0]);
                aud.setRange(Integer.parseInt(episodeInfoString.split("<S t=\"")[1].split("r=\"")[1].split("\"/>")[0]));
                episode.addAvailableAudio(aud.getLabel(), aud);
                streamId++;
            } else if (set.contains("mimeType=\"text")) {
                //subs
                SubtitleReferencesEntity sub = new SubtitleReferencesEntity();
                sub.setId(streamId);
                try {
                    sub.setLabel(set.split("<Label>")[1].split("</Label")[0]);
                } catch (IndexOutOfBoundsException e) {
                    sub.setLabel("Svenska");
                }
                try {
                    sub.setUrl(new URL(BASE_URL + set.split("<BaseURL>")[1].split("</BaseURL>")[0]));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                episode.addAvailableSubs(sub);
                streamId++;
            }
        }
        return episode;
    }



}
