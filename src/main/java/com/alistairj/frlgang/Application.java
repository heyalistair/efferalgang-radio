package com.alistairj.frlgang;

import com.alistairj.frlgang.player.RadioPlayer;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Random;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class Application {

  private static Logger logger = LoggerFactory.getLogger(Application.class);

  private static final String ARG_PARAM = "API_KEY=";
  private static final String ARG_PARAM_2 = "FRONTEND_HOST=";

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

  @RequestMapping("/live")
  public RadioPlayer getLiveShow(HttpServletResponse response) {

    response.addHeader("Access-Control-Allow-Origin", frontendUrl);
    return radioPlayer;
  }

  /**
   * Timed to help with upcoming.
   */
  @Scheduled(cron = "0 09,19,29,39,49,59 * ? * *")
  public void fetchLiveShowStatusOncePerMinute() {
    radioPlayer.getLivePlayer().fetchUpcomingAndLiveShowIds();
  }


  @Scheduled(cron = "20,50 * * ? * *")
  public void fetchUpcomingShowStatus() {
    radioPlayer.getLivePlayer().fetchBroadcastStatusOfRelevantIds();
  }

  /**
   * Run the application.
   *
   * @param args YouTube API key
   */
  public static void main(String[] args) throws GeneralSecurityException, IOException {
    for (String s : args) {
      logger.info(s);
    }

    if (args.length != 2
        || args[0].startsWith(ARG_PARAM) == false
        || args[1].startsWith(ARG_PARAM_2) == false) {
      logger.error("Run with 'API_KEY=<YOUR_YOUTUBE_API_KEY>' and 'FRONTEND_HOST=<URL OF "
          + "FRONTEND>' as command line arguments");
      System.exit(1);
    } else {
      logger.info("Configuration is correct");
    }

    String youTubeApiKeyCSV = args[0].substring(ARG_PARAM.length());
    frontendUrl = args[1].substring(ARG_PARAM_2.length());

    ApiManager.initialize(youTubeApiKeyCSV);

    // create the radio player
    radioPlayer = new RadioPlayer();

    SpringApplication.run(Application.class, args);
  }

}