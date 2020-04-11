package com.alistairj.eflgangradio;

import java.io.IOException;
import java.security.GeneralSecurityException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class Application {

  @RequestMapping("/live")
  public String getLiveShow() throws GeneralSecurityException, IOException {

    String videoId = YouTubeService.getCurrentLiveShow();

    String jsonResponse = String.format("{\"video_id\":\"%s\"}", videoId);

    return jsonResponse;
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

}