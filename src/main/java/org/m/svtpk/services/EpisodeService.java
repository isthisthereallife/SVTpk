package org.m.svtpk.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.m.svtpk.entity.*;
import org.m.svtpk.utils.Arrow;
import org.m.svtpk.utils.Settings;
import org.m.svtpk.utils.StringHelpers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.m.svtpk.utils.HttpBodyGetter.connectToURLReturnBodyAsString;

public class EpisodeService {
    Settings settings = Settings.load();

    public ArrayList<SeasonEntity> getSeasonsFromEpisode(EpisodeEntity episode) {

        ArrayList<SeasonEntity> seasons = new ArrayList<>();

        //det här anropet har redan gjorts
        System.out.println("getSeasonsFromEpisode, hämtar från episode.getSplashURL:");
        String res = connectToURLReturnBodyAsString(episode.getSplashURL());

        if (episode.getSvtId() != null) {
            res = res.split("<script id=\"__NEXT_DATA__\" type=\"application/json\">")[1];
            String[] selectionTypes = res.split("selectionType");

            // Season loop
            for (String selection : selectionTypes) {
                if (selection.contains("listId")) {
                    SeasonEntity season = new SeasonEntity();

                    //https://www.svtstatic.se/image/custom/1024/35680761/1655299163?format=auto&chromaSubSampling=false&enableAvif=true

                    String type = "";
                    try {
                        type = selection.split(":")[1].split(",")[0];
                        type = StringHelpers.lazyfix(type);
                        season.setTypeFromString(type);
                        String listId = "";
                        if (selection.contains("listId")) {
                            listId = selection.split("listId")[1].split(",")[0];
                            listId = StringHelpers.lazyfix(listId);
                            season.setName(listId);
                        }

                        if (selection.contains("listType")) {
                            // split without removing
                            String[] episodes = selection.split("(?=\"heading)");

                            // check if it is an episode, add to season if so
                            for (String episodeString : episodes) {
                                EpisodeEntity episodeFromString = new EpisodeEntity();
                                episodeFromString.setProgramTitle(episode.getProgramTitle());
                                String imagesString = "";
                                String urlString = "";
                                String episodeTitle = "";
                                String svtId = "";
                                String imageURL = "";
                                try {
                                    if (episodeString.contains("\"heading")) {
                                        episodeTitle = episodeString
                                                .split("\"heading")[1]
                                                .split(",")[0]
                                                .replace("\\", "")
                                                .replace("\"", "")
                                                .replace(":", "");
                                    }
                                    if (episodeString.contains("\"videoSvtId")) {
                                        svtId = episodeString.split("\"videoSvtId")[1]
                                                .split(",")[0]
                                                .replace("\\", "")
                                                .replace("\"", "")
                                                .replace(":", "");
                                    }

                                    try {
                                        if (episodeString.contains("\"images\\")) {
                                            imagesString = episodeString.split("\"images")[1];
                                            imagesString = imagesString.split("\"wide")[1];
                                            imageURL = imagesString.split("\"changed")[1];
                                            imageURL = imageURL
                                                    .split(",")[0]
                                                    .replace("\\", "")
                                                    .replace("\"", "")
                                                    .replace(":", "")
                                                    .trim();
                                            //jag får en trailer-id för att jag inte klippt den

                                            String imageId =
                                                    imagesString
                                                            .split("\"id")[1]
                                                            .split(",")[0]
                                                            .replace("\\", "")
                                                            .replace("\"", "")
                                                            .replace(":", "")
                                                            .trim();
                                            imageURL = imageId.concat("/" + imageURL);
                                        }
                                        if (imageURL.isBlank()) {
                                            imageURL = episode.getImageURL().toString();
                                        }


                                        if (!imageURL.contains("https://www.svtstatic.se/image/custom/1024/")) {
                                            imageURL = "https://www.svtstatic.se/image/custom/1024/".concat(imageURL);
                                        }
                                        episodeFromString.setImageURL(new URL(imageURL));
                                        episodeFromString.setThumbnail(Arrow.getImgArrowDown("grey"));
                                        //episodeFromString.setThumbnail(
                                        //        new Image(imageURL, 25, 25, false, false));
                                    } catch (Exception e) {
                                        if (settings.isAdvancedUser()) e.printStackTrace();
                                    }
                                    URL url = null;
                                    if (episodeString.contains("\"urls")) {
                                        urlString = episodeString
                                                .split("\"urls")[1]
                                                .split("\"svtplay")[1]
                                                .split(",")[0]
                                                .replace("\\", "")
                                                .replace("\"", "")
                                                .replace(":", "");
                                        url = new URL("https://www.svtplay.se" + urlString);
                                    }

                                    if (url != null) {
                                        episodeFromString.setSplashURL(url);
                                        episodeFromString.setEpisodeTitle(episodeTitle);
                                        episodeFromString.setSvtId(svtId);
                                        if (episodeString.contains("\"description")) {
                                            try {
                                                episodeFromString.setDescription(episodeString
                                                        .split("\"description")[1]
                                                        .split("\",")[0]
                                                        .replace("\\", "")
                                                        .replace("\"", "")
                                                        .replaceFirst(":", ""));
                                                System.out.println("EpisodeService.getSeasonsFromEpisode letar description för " + episodeTitle);
                                            } catch (Exception e) {
                                                episode.setDescription("Ingen information tillgänglig.");
                                                System.out.println("No description available for episode " + episodeTitle);
                                            }
                                        }
                                        episodeFromString.setProgressState(ProgressStates.IGNORED);
                                        season.addItem(episodeFromString);
                                    }
                                } catch (Exception e) {
                                    System.out.println("Tråkigt!");
                                    System.out.println(e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                        }

                    } catch (ArrayIndexOutOfBoundsException e) {
                        if (settings.isAdvancedUser()) e.printStackTrace();
                    }

                    seasons.add(season);
                }
            }
        }

        return seasons;
    }

    public EpisodeEntity findEpisode(String address) {
        String res = "";
        address = address.replace(" ", "").trim();
        if (address.endsWith("/")) address = address.substring(0, address.length() - 1);
        EpisodeEntity episode = new EpisodeEntity();
        if (address.length() > 9) {
            try {
                URI uri = URI.create(address);
                URL url = new URL(address);
                //address = URLEncoder.encode(address, StandardCharsets.UTF_8);
                if (uri.isAbsolute() && address.contains("\\id=")) {

                    episode = getEpisodeInfo(address);
                } else if (uri.isAbsolute()) {
                    if (url.toString().contains("svtplay.se")) {
                        res = connectToURLReturnBodyAsString(url);
                        if (!res.equals("")) {
                            String id = res.split("data-rt=\"top-area-play-button")[1].split("\\?")[1].split("\"")[0];
                            episode = getEpisodeInfo(address + "?" + id);
                            episode.setSplashURL(uri.toURL());
                        } else {
                            if (settings.isAdvancedUser()) System.out.println("URI was not absolute, didn't search.");
                        }
                    } else {
                        if (settings.isAdvancedUser()) System.out.println("Vänligen ange en adress till SVT Play");
                    }
                }

            } catch (Exception e) {
                if (settings.isAdvancedUser()) e.printStackTrace();
            }
        }
        return episode;
    }

    public EpisodeEntity getEpisodeInfo(String address) {
        EpisodeEntity episode = new EpisodeEntity();
        String[] id = address.split("id=");
        if (id.length > 1 && id[1].trim().length() > 5) {


            //clean up the id, remove excess
            id[1] = id[1].split("\\W")[0];

            String episodeId = id[1];
            String URI = "https://api.svt.se/video/" + episodeId;
            String body = "";
            try {
                body = connectToURLReturnBodyAsString(new URL("https://api.svt.se/video/" + episodeId));
                updateEpisodeLinks(episode, body);
                //kan jag här köra updateEpisodeLinks???

            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (!body.equals("")) {
                    //check if it is a live stream
                    if (body.contains("\"live\":true")) return new EpisodeEntity(true);
                    episode.setSvtId(body.split("svtId\":\"")[1].split("\"")[0]);
                    try {
                        String progTitle = body.split("programTitle\":\"")[1].split("\",")[0];
                        episode.setProgramTitle(progTitle);
                    } catch (Exception e) {
                        episode.setProgramTitle(LocalDateTime.now().toLocalDate().toString());
                        System.out.println("Couldn't find program title. Setting program title to " + (LocalDateTime.now().toLocalDate().toString()));
                    }
                    try {
                        episode.setEpisodeTitle(body.split("episodeTitle\":\"")[1].split("\",")[0]);
                    } catch (Exception e) {
                        episode.setEpisodeTitle(LocalDateTime.now().toLocalTime().toString());
                        System.out.println("Couldn't find episode title. Setting episode title to " + (LocalDateTime.now().toLocalDate().toString()));
                    }


                    episode.setContentDuration(Integer.parseInt(body.split("contentDuration\":")[1].split(",")[0]));

                    episode.setFilename(StringHelpers.fileNameFixerUpper(episode.getProgramTitle() + "-" + episode.getEpisodeTitle()).concat(".mkv"));
                    //episode = updateEpisodeLinks(episode);
                    try {
                        //inte helt såld på den här iden
                        episode.setImageURL(new URL(getImgURL(address)));
                        System.out.println("sätter ImageURL här nere nu, till: " + episode.getImageURL());
//                        episode.setThumbnail(new Image(episode.getImageURL().toString()));
                        episode.setThumbnail(Arrow.getImgArrowDown("grey"));

                    } catch (Exception e) {
                        if (settings.isAdvancedUser()) System.out.println("Could not get episode image");
                    }
                }
            } catch (Exception e) {
                if (settings.isAdvancedUser()) {
                    System.out.println("Could not get episode info");
                    e.printStackTrace();
                }
                return new EpisodeEntity();
            }
        }
        return episode;
    }

    private String getImgURL(String addressWithId) throws NullPointerException {
        String body = "";

        try {
            System.out.println("getImgURL, hämtar med en provided address");
            body = connectToURLReturnBodyAsString(new URL(addressWithId.split("\\?id=")[0]));
            if (!body.equals("")) {
                return body.split("data-src=\"")[1].split("\"")[0];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public EpisodeEntity updateEpisodeLinks(EpisodeEntity episode, String body) {
        VideoJsonObjectEntity videoObjectFromJson = null;
        ObjectMapper objectMapper = new ObjectMapper();
        String episodeInfoString = "";
        String mpdUrl = "";
        String URI = "https://api.svt.se/video/" + episode.getSvtId();
        String body = "";
        try {
            //anrop 1
            //det här anropet har redan gjorts, kan jag köra detta när det första anropet görs??
            //body = connectToURLReturnBodyAsString(new URL("https://api.svt.se/video/" + episode.getSvtId()));
            if (!body.equals("")) {
                //anrop 2
                //System.out.println(gson.);
                VideoJsonObjectEntity vJoE = objectMapper.readValue(body, VideoJsonObjectEntity.class);

                System.out.println(vJoE.getSvtId());
                System.out.println(vJoE.getVideoReferences().length);
                for (VideoReferencesEntity vre : vJoE.getVideoReferences()) {
                    if (vre.getFormat().equalsIgnoreCase("dash-full")) {
                        System.out.println("den här är det:");
                        System.out.println(vre.getUrl());
                        episode.setMpdURL(new URL(vre.getUrl()));

                        //anrop 3, till mpdURLen
                        body = connectToURLReturnBodyAsString(episode.getMpdURL());
                        if (!body.equals("")) {
                            System.out.println("episodeInfoString: \n" + episodeInfoString);
                            episodeInfoString = body;
                        }
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (episodeInfoString.equals("")) return new EpisodeEntity();
        if (episodeInfoString.contains("\"live\":true")) return new EpisodeEntity(true);

        // this is sketchy AF
        String BASE_URL = episode.getMpdURL().toString().split("dash-full.mpd")[0];

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
                    System.out.println("Nån på svt har glömt ge undertextspåret en Label :/ Sätter den till \"Svenska\"");
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
