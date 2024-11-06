package com.smarttmessenger.app.keyboard.gif

import com.smarttmessenger.app.util.adapter.mapping.MappingModel

data class GifQuickSearch(val gifQuickSearchOption: GifQuickSearchOption, val selected: Boolean) : MappingModel<GifQuickSearch> {
  override fun areItemsTheSame(newItem: GifQuickSearch): Boolean {
    return gifQuickSearchOption == newItem.gifQuickSearchOption
  }

  override fun areContentsTheSame(newItem: GifQuickSearch): Boolean {
    return selected == newItem.selected
  }
}
