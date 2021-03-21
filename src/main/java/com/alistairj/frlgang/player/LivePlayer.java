package com.alistairj.frlgang.player;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * On initialization, getUpcomingShows should always be called be called before getLiveShows.
 *
 * @author Alistair Jones (alistair@ohalo.co)
 */
public class LivePlayer {

  private static final Logger logger = LoggerFactory.getLogger(LivePlayer.class);

  private static final String URL_STATS = "http://localhost/stat";

  OkHttpClient client = new OkHttpClient();

  private DocumentBuilder builder;

  private final RadioPlayer rp;
  /**
   * Build a new LivePlayer.
   */
  public LivePlayer(RadioPlayer rp) {
    this.rp = rp;
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    try {
      this.builder = factory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    }
  }

  public void fetchBroadcastStatus() {
    try {
      String xml = this.getXml();
      Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
      doc.getDocumentElement().normalize();

      // if the XML has a stream element, there is a live stream
      NodeList nl = doc.getElementsByTagName("stream");
      if (nl.getLength() > 0) {
        rp.setStatusLive();
      } else {
        rp.setStatusArchivedPlay();
      }

    } catch (IOException | SAXException e) {
      logger.error("Cannot fetch XML");
    }
  }

  private String getXml() throws IOException {
    Request request = new Request.Builder()
        .url(URL_STATS)
        .build();

    try (Response response = client.newCall(request).execute()) {
      return response.body().string();
    }
  }
}
