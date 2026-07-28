package org.whispersystems.signalservice.internal.push;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SendMessageResponse {

  @JsonProperty
  private boolean needsSync;

  private boolean sentUnidentfied;

  public SendMessageResponse() {}

  public SendMessageResponse(boolean needsSync, boolean sentUnidentified) {
    this.needsSync       = needsSync;
    this.sentUnidentfied = sentUnidentified;
  }

  public boolean getNeedsSync() {
    return needsSync;
  }

  public boolean sentUnidentified() {
    return sentUnidentfied;
  }

  public void setSentUnidentfied(boolean value) {
    this.sentUnidentfied = value;
  }

  // [Smartt] Communication window: the server sets these extra fields when it holds the message
  // instead of delivering it. Kept in one trailing block for merge isolation.
  @JsonProperty
  private boolean communicationWindowHeld;

  @JsonProperty
  private long windowOpensAt;

  public boolean isCommunicationWindowHeld() {
    return communicationWindowHeld;
  }

  public long getWindowOpensAt() {
    return windowOpensAt;
  }
  // [/Smartt]
}
