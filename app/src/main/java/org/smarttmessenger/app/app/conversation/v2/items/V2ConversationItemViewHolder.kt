/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.app.conversation.v2.items

import com.smarttmessenger.app.util.adapter.mapping.MappingModel
import com.smarttmessenger.app.util.adapter.mapping.MappingViewHolder

/**
 * Base ViewHolder to share some common properties shared among conversation items.
 */
abstract class V2ConversationItemViewHolder<Model : MappingModel<Model>>(
  root: V2ConversationItemLayout,
  appearanceInfoProvider: V2ConversationContext
) : MappingViewHolder<Model>(root) {
  protected val shapeDelegate = V2ConversationItemShape(appearanceInfoProvider)
  protected val themeDelegate = V2ConversationItemTheme(context, appearanceInfoProvider)
}
