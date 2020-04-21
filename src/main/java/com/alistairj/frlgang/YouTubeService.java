package com.alistairj.frlgang;

import com.alistairj.frlgang.player.archive.ArchivedVideo;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequestInitializer;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fetch information from YouTube.
 *
 * TODO: change this to do the so that the YouTube objectS are behind a Singleton
 *
 * @author Alistair Jones (alistair@ohalo.co)
 */
public class YouTubeService {

  private static Logger logger = LoggerFactory.getLogger(YouTubeService.class);

  private static final String EFFERALGANG_RADIO_CHANNEL_ID = "UCEhyiFmy5c6MrTY1iLz2bAQ";

  private static String API_KEY = null;

  private static final String APPLICATION_NAME = "EfferalGang Radio Live";
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  private static YouTube service = null;

  public static void configureAPIKey(String apiKey) {
    API_KEY = apiKey;
  }

  /**
   * Build and return an authorized API client service.
   *
   * @return an authorized API client service
   */
  private static YouTube getService() throws GeneralSecurityException, IOException {

    if (service == null) {

      final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

      service = new YouTube.Builder(httpTransport, JSON_FACTORY, null)
          .setApplicationName(APPLICATION_NAME)
          .setYouTubeRequestInitializer(new YouTubeRequestInitializer(API_KEY))
          .build();
    }

    return service;
  }

  /**
   * Fetch current live show.
   * TODO: Cache this
   */
  public static List<String> getCurrentLiveShows() throws GeneralSecurityException, IOException {

    YouTube youtubeService = getService();
    // Define and execute the API request

    YouTube.Search.List request = youtubeService.search()
        .list("id");
    SearchListResponse response = request
        .setMaxResults(10L)
        .setOrder("date")
        .setChannelId(EFFERALGANG_RADIO_CHANNEL_ID)
        .setType("video")
        .setEventType("live")
        .execute();

    List<String> videoIds = new ArrayList<>();

    for (SearchResult result: response.getItems()) {
      videoIds.add(result.getId().getVideoId());
    }

    logger.info("Fetched current live shows, count:{}", videoIds.size());

    return videoIds;
  }

  public static List<String> getUpcomingShows() throws GeneralSecurityException, IOException {

    YouTube youtubeService = getService();
    // Define and execute the API request

    YouTube.Search.List request = youtubeService.search()
        .list("id");
    SearchListResponse response = request
        .setMaxResults(5L)
        .setOrder("date")
        .setChannelId(EFFERALGANG_RADIO_CHANNEL_ID)
        .setType("video")
        .setEventType("upcoming")
        .execute();

    List<String> videoIds = new ArrayList<>();

    for (SearchResult result: response.getItems()) {
      videoIds.add(result.getId().getVideoId());
    }

    logger.info("Fetched current upcoming shows, count:{}", videoIds.size());

    return videoIds;
  }

  public static List<Video> getVideoDetails(List<String> videoIds) throws GeneralSecurityException, IOException {

    YouTube youtubeService = getService();
    // Define and execute the API request

    YouTube.Videos.List request = youtubeService.videos()
        .list("liveStreamingDetails,snippet");

    VideoListResponse response = request
        .setMaxResults(1L)
        .setId(String.join(",", videoIds))
        .execute();

    logger.info("Fetched details about multiple videos, count:{}", videoIds.size());

    return response.getItems();
  }

  /**
   * Fetch random completed show.
   */
  public static List<ArchivedVideo> getCompletedShows() throws GeneralSecurityException, IOException {

    YouTube youtubeService = getService();

    // Define and execute the API request
    YouTube.Search.List request = youtubeService.search()
        .list("id");
    SearchListResponse response = request
        .setMaxResults(50L)
        .setPart("id")
        .setOrder("date")
        .setChannelId(EFFERALGANG_RADIO_CHANNEL_ID)
        .setType("video")
        .setEventType("completed")
        .execute();

    logger.info("response: {}", response);

    List<String> videoIds = new ArrayList<>();

    for (SearchResult item : response.getItems()) {
      videoIds.add(item.getId().getVideoId());
    }
    logger.info("Fetched {} past shows", videoIds.size());
    // TODO: get all shows

    List<Video> videoDetails = YouTubeService.getVideoDetails(videoIds);

    List<ArchivedVideo> archivedVideos = new ArrayList<>();
    for (Video v: videoDetails) {
      long startInstant = v.getLiveStreamingDetails().getActualStartTime().getValue();
      long endInstant = v.getLiveStreamingDetails().getActualEndTime().getValue();
      long duration = endInstant - startInstant;
      archivedVideos.add(new ArchivedVideo(v.getId(), duration));
    }

    return archivedVideos;
  }
}
