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

  private static boolean isArchivePlaylistActive = false;

  private static String uploadPlaylistId;

  private static int currentIndex = 0;

  /**
   * Initializes API Manager.
   *
   * <p>
   * MUST BE CALLED FIRST.
   * </p>
   *
   * @param key API key for the YouTube key
   */
  public static void initialize(String key, String archivePlaylistId)
      throws GeneralSecurityException, IOException {

    if (archivePlaylistId == null) {
      throw new IllegalArgumentException();
    }

    logger.debug("Initializing archived playlist source: {}", archivePlaylistId);

    ApiManager.archivePlaylistId = archivePlaylistId;
    ApiManager.isArchivePlaylistActive = true;

    logger.debug("Initializing API service with key '{}'", key.substring(0, 6));

    final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

    youTubeAPI = new YouTube.Builder(httpTransport, JSON_FACTORY, null)
        .setApplicationName(APPLICATION_NAME)
        .setYouTubeRequestInitializer(new YouTubeRequestInitializer(key))
        .build();
  }

  /**
   * Get YouTube API to use.
   *
   * @return an authorized API client service
   */
  public static YouTube getYouTubeApi() {
    return youTubeAPI;
  }

  public static String getChannelId() {
    return channelId;
  }

  public static boolean isArchivePlaylistActive() {
    return isArchivePlaylistActive;
  }

  public static String getArchivePlaylistId() {
    return archivePlaylistId;
  }

  public static String getUploadPlaylistId() {
    return uploadPlaylistId;
  }

}
