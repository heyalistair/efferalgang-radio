package com.alistairj.eflgangradio;

import java.io.IOException;
import java.security.GeneralSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class Application {

  private static Logger logger = LoggerFactory.getLogger(Application.class);

  private static final String ARG_PARAM = "API_KEY=";

  @RequestMapping("/live")
  public String getLiveShow() throws GeneralSecurityException, IOException {

    String videoId = YouTubeService.getCurrentLiveShow();

    String jsonResponse = String.format("{\"video_id\":\"%s\"}", videoId);

    return jsonResponse;
  }

  /**
   * Run the application.
   *
   * @param args YouTube API key
   */
  public static void main(String[] args) {

    if (args.length != 1 || args[0].startsWith(ARG_PARAM) == false) {
      logger.error("Run with 'API_KEY=<YOUR_YOUTUBE_API_KEY>' as a command line argument");
      System.exit(1);
    } else {
      logger.info("API KEY IS SET");
    }

    String youTubeApiKey = args[0].substring(ARG_PARAM.length());

    YouTubeService.configureAPIKey(youTubeApiKey);

    SpringApplication.run(Application.class, args);
  }

}