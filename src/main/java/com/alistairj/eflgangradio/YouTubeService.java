package com.alistairj.eflgangradio;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequestInitializer;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fetch information from YouTube.
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
  public static String getCurrentLiveShow() throws GeneralSecurityException, IOException {

    YouTube youtubeService = getService();
    // Define and execute the API request

    YouTube.Search.List request = youtubeService.search()
        .list("id");
    SearchListResponse response = request
        .setMaxResults(1L)
        .setPart("id")
        .setOrder("date")
        .setChannelId(EFFERALGANG_RADIO_CHANNEL_ID)
        .setType("video")
        .setEventType("live")
        .execute();

    String videoId = null;

    if (response.getItems().size() > 0) {
      // get last item
      SearchResult item = response.getItems().get(response.getItems().size() - 1);
      videoId = item.getId().getVideoId();
    }

    logger.info("fetched current live show: {}", videoId);

    return videoId;
  }

  /**
   * Fetch random completed show.
   */
  public static List<String> getCompletedShows() throws GeneralSecurityException, IOException {

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
    Random r = new Random();

    for (SearchResult item : response.getItems()) {
      videoIds.add(item.getId().getVideoId());
    }

    logger.info("Fetched {} past shows", videoIds.size());

    return videoIds;
  }
}
