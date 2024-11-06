package com.smarttmessenger.app.util;

import androidx.annotation.StyleRes;

import com.smarttmessenger.app.R;

public class DynamicDarkToolbarTheme extends DynamicTheme {

  protected @StyleRes int getTheme() {
    return R.style.Signal_DayNight_DarkNoActionBar;
  }
}
