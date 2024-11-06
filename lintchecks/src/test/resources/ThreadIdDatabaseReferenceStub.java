package com.smarttmessenger.app.database;

interface ThreadIdDatabaseReference {
  void remapThread(long fromId, long toId);
}
