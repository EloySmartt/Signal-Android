package com.smarttmessenger.app.conversation.ui.mentions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.smarttmessenger.app.R;
import com.smarttmessenger.app.util.adapter.mapping.MappingAdapter;
import com.smarttmessenger.app.util.adapter.mapping.MappingModel;
import com.smarttmessenger.app.util.viewholders.RecipientViewHolder;
import com.smarttmessenger.app.util.viewholders.RecipientViewHolder.EventListener;

import java.util.List;

public class MentionsPickerAdapter extends MappingAdapter {
  private final Runnable currentListChangedListener;

  public MentionsPickerAdapter(@Nullable EventListener<MentionViewState> listener, @NonNull Runnable currentListChangedListener) {
    this.currentListChangedListener = currentListChangedListener;
    registerFactory(MentionViewState.class, RecipientViewHolder.createFactory(R.layout.mentions_picker_recipient_list_item, listener));
  }

  @Override
  public void onCurrentListChanged(@NonNull List<MappingModel<?>> previousList, @NonNull List<MappingModel<?>> currentList) {
    super.onCurrentListChanged(previousList, currentList);
    currentListChangedListener.run();
  }
}
