package com.pedro.vlc;

import java.util.ArrayList;

/**
 * Created by pedro on 25/06/17.
 */
public class VlcOptions {

  public ArrayList<String> getDefaultOptions() {
    ArrayList<String> options = new ArrayList<>();
    options.add("-vvv");
    options.add("--network-caching=100");
    options.add("--rtsp-caching=100");
    return options;
  }
}
