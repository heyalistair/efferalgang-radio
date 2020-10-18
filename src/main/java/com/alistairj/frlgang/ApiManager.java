package com.alistairj.frlgang;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialRefreshListener;
import com.google.api.client.auth.oauth2.DataStoreCredentialRefreshListener;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.auth.oauth2.TokenErrorResponse;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequestInitializer;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
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

  private static YouTube authYouTubeAPI;

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
  public static void initialize(String key, String channelId, String clientId, String clientSecret,
                                String refreshToken, String archivePlaylistId)
      throws GeneralSecurityException, IOException {

    if (archivePlaylistId == null) {
      throw new IllegalArgumentException();
    }

    logger.debug("Initializing archived playlist source: {}", archivePlaylistId);

    ApiManager.archivePlaylistId = archivePlaylistId;
    ApiManager.isArchivePlaylistActive = true;

    ApiManager.initialize(key, channelId, clientId, clientSecret, refreshToken);

  }

  /**
   * Initializes API Manager.
   *
   * @param key API key for the YouTube key
   * @see #initialize(String, String, String, String, String, String)
   */
  public static void initialize(String key, String channelId, String clientId, String clientSecret,
                                String refreshToken)
      throws GeneralSecurityException, IOException {

    if (key == null || channelId == null) {
      throw new IllegalArgumentException();
    }

    logger.debug("Initializing API service with key '{}' and channelId '{}'", key, channelId);

    final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

    youTubeAPI = new YouTube.Builder(httpTransport, JSON_FACTORY, null)
        .setApplicationName(APPLICATION_NAME)
        .setYouTubeRequestInitializer(new YouTubeRequestInitializer(key))
        .build();

    // build authorized API service
    final HttpTransport httpTransport2 = GoogleNetHttpTransport.newTrustedTransport();
    DataStore<StoredCredential> dataStore =
        MemoryDataStoreFactory.getDefaultInstance().getDataStore("credentialDatastore");


    CredentialRefreshListener refreshListener = new CredentialRefreshListener() {
      @Override
      public void onTokenResponse(Credential credential, TokenResponse tokenResponse)
          throws IOException {
        logger.info("Fetching access token has succeeded.");
//
//        StoredCredential storedCredential = new StoredCredential();
//        storedCredential.setAccessToken(credential.getAccessToken());
//        storedCredential.setRefreshToken(credential.getRefreshToken());
//        dataStore.set("username", storedCredential);
//
//        Credential credential = new GoogleCredential.Builder()
//            .setTransport(new NetHttpTransport())
//            .setJsonFactory(new JacksonFactory())
//            .addRefreshListener(
//                new DataStoreCredentialRefreshListener(
//                    "username", dataStore))
//            .build();


      }

      @Override
      public void onTokenErrorResponse(Credential credential, TokenErrorResponse tokenErrorResponse)
          throws IOException {
        logger.error("Fetching access token failed: {}", tokenErrorResponse.getErrorDescription());
      }
    };

    Collection<CredentialRefreshListener> refreshListeners = new ArrayList<>();
    refreshListeners.add(new DataStoreCredentialRefreshListener("username", dataStore));

    final GoogleCredential refreshTokenCredential2 =
        new GoogleCredential.Builder()
            .setJsonFactory(JSON_FACTORY)
            .setTransport(httpTransport2)
            .setClientSecrets(clientId, clientSecret)
            .setRefreshListeners(refreshListeners)
            .build().setRefreshToken(refreshToken);

    boolean success = refreshTokenCredential2.refreshToken(); // do not forget to call this method
    if (success) {
      logger.info("Correctly retrieved access token");
    } else {
      logger.info("Fetching access token failed");
    }

    authYouTubeAPI = new YouTube.Builder(httpTransport2, JSON_FACTORY, refreshTokenCredential2)
        .setApplicationName(APPLICATION_NAME)
        .build();

    ApiManager.channelId = channelId;

    // actually use the channel id to get the upload playlist id
    uploadPlaylistId = YouTubeService.getUploadPlaylistId();
    logger.info("Found upload playlist id: {}", uploadPlaylistId);
  }

  /**
   * Get YouTube API to use.
   *
   * @return an authorized API client service
   */
  public static YouTube getYouTubeApi() {
    return youTubeAPI;
  }

  public static YouTube getAuthYouTubeApi() {
    return authYouTubeAPI;
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
