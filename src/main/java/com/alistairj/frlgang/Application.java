package com.alistairj.frlgang;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import com.alistairj.frlgang.player.FoundVideo;
import com.alistairj.frlgang.player.RadioPlayer;
import com.alistairj.frlgang.player.archive.ArchivePlayer;
import com.alistairj.frlgang.utils.RadioPlayerUtils;
import java.io.IOException;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Random;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@SuppressWarnings("checkstyle:LineLength")
@SpringBootApplication
@RestController
public class Application {

  private static Logger logger = LoggerFactory.getLogger(Application.class);

  private static final String ARG_PARAM_0 = "API_KEY=";
  private static final String ARG_PARAM_1 = "FRONTEND_HOST=";
  private static final String ARG_PARAM_2 = "ARCHIVE_PLAYLIST_ID=";

  private static String frontendUrl = "";

  private static List<String> cachedShows = null;
  private static Random random = new Random();

  private static String cachedliveShow = null;

  private static RadioPlayer radioPlayer;

  private static String getRandomCachedShow() {
    if (cachedShows != null && cachedShows.isEmpty() == false) {
      int chosenIndex = random.nextInt(cachedShows.size() - 1);
      return cachedShows.get(chosenIndex);
    } else {
      return "";
    }
  }

  @CrossOrigin
  @RequestMapping("/live")
  public RadioPlayer getLiveShow(HttpServletResponse response) {
    return radioPlayer;
  }

  @CrossOrigin
  @RequestMapping("/queue")
  public ArchivePlayer getArchiveQueue(HttpServletResponse response) {
    return radioPlayer.getArchivePlayer();
  }

  @CrossOrigin
  @RequestMapping(value = "/track", method = POST)
  @ResponseBody
  public FoundVideo trackVideo(@RequestParam("video_id") String videoIdOrUrl) throws IOException {

    logger.info("Encoded video_id_or_url:{}", videoIdOrUrl);

    String videoIdOrUrlDecoded = URLDecoder.decode(videoIdOrUrl, "UTF-8");

    String videoId = RadioPlayerUtils.parseVideoId(videoIdOrUrlDecoded);

    logger.info("Checking video_id:{}", videoId);
    if (videoId.isEmpty()) {
      throw new IOException("I don't even understand this show id");
    }

    FoundVideo fv = radioPlayer.getLivePlayer().checkVideoId(videoId);

    return fv;
  }

  /**
   * Timed to help with upcoming.
   */
  @Scheduled(cron = "0 59 * ? * *")
  public void fetchUpcomingAndLiveShowIds() {
    radioPlayer.getLivePlayer().fetchUpcomingAndLiveShowIds();
  }

//  @Scheduled(cron = "15,45 * * ? * *")
  @Scheduled(fixedDelay = 1000)
  public void fetchBroadcastStatusOfRelevantIds() {
    radioPlayer.getLivePlayer().fetchBroadcastStatusOfRelevantIds();
  }

  /**
   * Run the application.
   *
   * @param args YouTube API key
   */
  public static void main(String[] args) throws GeneralSecurityException, IOException {

    validateCommandLineParameters(args);

    frontendUrl = args[1].substring(ARG_PARAM_1.length());

    String youtubeApiKey = args[0].substring(ARG_PARAM_0.length());
    String playlistId = args[2].substring(ARG_PARAM_2.length());

    ApiManager.initialize(youtubeApiKey, playlistId);

    // create the radio player
    radioPlayer = new RadioPlayer();

    SpringApplication.run(Application.class, args);
  }

  private static void validateCommandLineParameters(String[] args) {
    for (String s : args) {
      logger.info("Command line args are: {}", s);
    }

    boolean isValid = args.length == 3 || args.length == 4;

    if (isValid) {
      isValid = args[0].startsWith(ARG_PARAM_0)
          && args[1].startsWith(ARG_PARAM_1)
          && args[2].startsWith(ARG_PARAM_2);
    }

    if (isValid) {
      logger.info("Configuration is correct");
    } else {
      logger.error("Run with"
          + "API_KEY=<YOUR_YOUTUBE_API_KEY>,"
          + "FRONTEND_HOST=<URL OF FRONTEND>,"
          + "ARCHIVE_PLAYLIST_ID=<YOUR_CHANNEL_ID>'");
      System.exit(1);
    }
  }
}
