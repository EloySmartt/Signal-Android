package com.smarttmessenger.app.stories.my

import androidx.fragment.app.Fragment
import com.smarttmessenger.app.components.FragmentWrapperActivity

class MyStoriesActivity : FragmentWrapperActivity() {
  override fun getFragment(): Fragment {
    return MyStoriesFragment()
  }
}
