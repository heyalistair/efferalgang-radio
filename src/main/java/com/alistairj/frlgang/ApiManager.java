package com.alistairj.frlgang;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequestInitializer;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

  private static List<YouTube> youTubeAPIs = new ArrayList<>();

  private static int currentIndex = 0;

  /**
   * Initializes a list of API client services, each initialized with an API key from the key
   * string.
   *
   * <p>
   *   MUST BE CALLED FIRST.
   * </p>
   *
   * @param keyString List of keys separated by semi-colons.
   */
  public static void initialize(String keyString) throws GeneralSecurityException, IOException {
    List<String> keys = Arrays.asList(keyString.split(";"));
    Collections.shuffle(keys);

    for (String key : keys) {

      logger.debug("Initializing API service with key '{}'", key);

      final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

      youTubeAPIs.add(new YouTube.Builder(httpTransport, JSON_FACTORY, null)
          .setApplicationName(APPLICATION_NAME)
          .setYouTubeRequestInitializer(new YouTubeRequestInitializer(key))
          .build());
    }

    logger.info("Initialized {} YouTube services", youTubeAPIs.size());
  }

  /**
   * Get a YouTube API to use.
   *
   * @return an authorized API client service
   */
  public static synchronized YouTube getYouTubeApi() {
    if (youTubeAPIs.isEmpty()) {
      throw new IllegalStateException("ApiManager must be initialized with at least one API key.");
    }

    if (currentIndex >= youTubeAPIs.size()) {
      currentIndex = 0;
    }
    return youTubeAPIs.get(currentIndex++);
  }

}
