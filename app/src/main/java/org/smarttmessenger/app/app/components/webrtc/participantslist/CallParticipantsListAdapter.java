package com.smarttmessenger.app.components.webrtc.participantslist;

import com.smarttmessenger.app.R;
import com.smarttmessenger.app.util.adapter.mapping.LayoutFactory;
import com.smarttmessenger.app.util.adapter.mapping.MappingAdapter;

public class CallParticipantsListAdapter extends MappingAdapter {

  CallParticipantsListAdapter() {
    registerFactory(CallParticipantsListHeader.class, new LayoutFactory<>(CallParticipantsListHeaderViewHolder::new, R.layout.call_participants_list_header));
    registerFactory(CallParticipantViewState.class, new LayoutFactory<>(CallParticipantViewHolder::new, R.layout.call_participants_list_item));
  }

}
