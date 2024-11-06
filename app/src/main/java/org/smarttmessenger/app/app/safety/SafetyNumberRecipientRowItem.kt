package com.smarttmessenger.app.safety

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.signal.core.util.DimensionUnit
import org.signal.core.util.or
import com.smarttmessenger.app.R
import com.smarttmessenger.app.components.AvatarImageView
import com.smarttmessenger.app.components.menu.ActionItem
import com.smarttmessenger.app.components.menu.SignalContextMenu
import com.smarttmessenger.app.recipients.Recipient
import com.smarttmessenger.app.util.adapter.mapping.LayoutFactory
import com.smarttmessenger.app.util.adapter.mapping.MappingAdapter
import com.smarttmessenger.app.util.adapter.mapping.MappingModel
import com.smarttmessenger.app.util.adapter.mapping.MappingViewHolder
import com.smarttmessenger.app.util.visible

/**
 * An untrusted recipient who can be verified or removed.
 */
object SafetyNumberRecipientRowItem {
  fun register(mappingAdapter: MappingAdapter) {
    mappingAdapter.registerFactory(Model::class.java, LayoutFactory(::ViewHolder, R.layout.safety_number_recipient_row_item))
  }

  class Model(
    val recipient: Recipient,
    val isVerified: Boolean,
    val distributionListMembershipCount: Int,
    val groupMembershipCount: Int,
    val getContextMenuActions: (Model) -> List<ActionItem>
  ) : MappingModel<Model> {
    override fun areItemsTheSame(newItem: Model): Boolean {
      return recipient.id == newItem.recipient.id
    }

    override fun areContentsTheSame(newItem: Model): Boolean {
      return recipient.hasSameContent(newItem.recipient) &&
        isVerified == newItem.isVerified &&
        distributionListMembershipCount == newItem.distributionListMembershipCount &&
        groupMembershipCount == newItem.groupMembershipCount
    }
  }

  class ViewHolder(itemView: View) : MappingViewHolder<Model>(itemView) {

    private val avatar: AvatarImageView = itemView.findViewById(R.id.safety_number_recipient_avatar)
    private val name: TextView = itemView.findViewById(R.id.safety_number_recipient_name)
    private val identifier: TextView = itemView.findViewById(R.id.safety_number_recipient_identifier)

    override fun bind(model: Model) {
      avatar.setRecipient(model.recipient)
      name.text = model.recipient.getDisplayName(context)

      val identifierText = model.recipient.e164.or(model.recipient.username).orElse(null)
      val subLineText = when {
        model.isVerified && identifierText.isNullOrBlank() -> context.getString(R.string.SafetyNumberRecipientRowItem__verified)
        model.isVerified -> context.getString(R.string.SafetyNumberRecipientRowItem__s_dot_verified, identifierText)
        else -> identifierText
      }

      identifier.text = subLineText
      identifier.visible = !subLineText.isNullOrBlank()

      itemView.setOnClickListener {
        itemView.isSelected = true
        SignalContextMenu.Builder(itemView, itemView.rootView as ViewGroup)
          .offsetY(DimensionUnit.DP.toPixels(12f).toInt())
          .onDismiss { itemView.isSelected = false }
          .show(model.getContextMenuActions(model))
      }
    }
  }
}
