package com.alistairj.eflgangradio;

import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.servlet.http.HttpServletResponse;
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
  private static final String ARG_PARAM_2 = "FRONTEND_HOST=";

  private static String frontendUrl = "";

  @RequestMapping("/live")
  public String getLiveShow(HttpServletResponse response) throws GeneralSecurityException, IOException {

    String videoId = YouTubeService.getCurrentLiveShow();

    String jsonResponse = String.format("{\"video_id\":\"%s\"}", videoId);
    response.addHeader("Access-Control-Allow-Origin", frontendUrl);
    return jsonResponse;
  }

  /**
   * Run the application.
   *
   * @param args YouTube API key
   */
  public static void main(String[] args) {
    for (String s: args) {
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

    String youTubeApiKey = args[0].substring(ARG_PARAM.length());

    YouTubeService.configureAPIKey(youTubeApiKey);

    frontendUrl = args[1].substring(ARG_PARAM_2.length());

    SpringApplication.run(Application.class, args);
  }

}