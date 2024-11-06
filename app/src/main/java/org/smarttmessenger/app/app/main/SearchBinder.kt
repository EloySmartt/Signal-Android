package com.smarttmessenger.app.main

import android.widget.ImageView
import com.smarttmessenger.app.components.Material3SearchToolbar
import com.smarttmessenger.app.util.views.Stub

interface SearchBinder {
  fun getSearchAction(): ImageView

  fun getSearchToolbar(): Stub<Material3SearchToolbar>

  fun onSearchOpened()

  fun onSearchClosed()
}
