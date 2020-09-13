package com.alistairj.frlgang;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequestInitializer;
import java.io.IOException;
import java.security.GeneralSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rotates API keys.
 *
 * @author Alistair Jones (alistair@ohalo.co)
 */
public class ApiManager {

  private static final Logger logger = LoggerFactory.getLogger(ApiManager.class);

  private static final String APPLICATION_NAME = "EfferalGang Radio Live";

  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  private static YouTube youTubeAPI;

  private static String channelId;

  private static String archivePlaylistId;

  private static int currentIndex = 0;

  /**
   * Initializes a list of API client services, each initialized with an API key from the key
   * string.
   *
   * <p>
   * MUST BE CALLED FIRST.
   * </p>
   *
   * @param key API key for the YouTube key
   */
  public static void initialize(String key, String channelId)
      throws GeneralSecurityException, IOException {

    logger.debug("Initializing API service with key '{}' and channelId '{}'", key, channelId);

    final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

    youTubeAPI = new YouTube.Builder(httpTransport, JSON_FACTORY, null)
        .setApplicationName(APPLICATION_NAME)
        .setYouTubeRequestInitializer(new YouTubeRequestInitializer(key))
        .build();

    ApiManager.channelId = channelId;

  }

  /**
   * Get a YouTube API to use.
   *
   * @return an authorized API client service
   */
  public static YouTube getYouTubeApi() {
    return youTubeAPI;
  }

  public static String getChannelId() {
    return channelId;
  }
}
