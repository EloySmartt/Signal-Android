package com.smarttmessenger.app.sharing.interstitial;

import com.smarttmessenger.app.R;
import com.smarttmessenger.app.util.adapter.mapping.MappingAdapter;
import com.smarttmessenger.app.util.viewholders.RecipientViewHolder;

class ShareInterstitialSelectionAdapter extends MappingAdapter {
  ShareInterstitialSelectionAdapter() {
    registerFactory(ShareInterstitialMappingModel.class, RecipientViewHolder.createFactory(R.layout.share_contact_selection_item, null));
  }
}
