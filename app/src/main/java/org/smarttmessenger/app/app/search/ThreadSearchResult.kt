package com.smarttmessenger.app.search

import com.smarttmessenger.app.database.model.ThreadRecord

data class ThreadSearchResult(val results: List<ThreadRecord>, val query: String)
