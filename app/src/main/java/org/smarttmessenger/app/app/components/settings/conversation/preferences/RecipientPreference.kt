package com.smarttmessenger.app.components.settings.conversation.preferences

import android.text.SpannableStringBuilder
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.smarttmessenger.app.R
import com.smarttmessenger.app.badges.BadgeImageView
import com.smarttmessenger.app.components.AvatarImageView
import com.smarttmessenger.app.components.settings.PreferenceModel
import com.smarttmessenger.app.recipients.Recipient
import com.smarttmessenger.app.util.ContextUtil
import com.smarttmessenger.app.util.SpanUtil
import com.smarttmessenger.app.util.adapter.mapping.LayoutFactory
import com.smarttmessenger.app.util.adapter.mapping.MappingAdapter
import com.smarttmessenger.app.util.adapter.mapping.MappingViewHolder
import com.smarttmessenger.app.util.visible

/**
 * Renders a Recipient as a row item with an icon, avatar, status, and admin state
 */
object RecipientPreference {

  fun register(adapter: MappingAdapter) {
    adapter.registerFactory(Model::class.java, LayoutFactory(::ViewHolder, R.layout.group_recipient_list_item))
  }

  class Model(
    val recipient: Recipient,
    val isAdmin: Boolean = false,
    val onClick: (() -> Unit)? = null
  ) : PreferenceModel<Model>() {
    override fun areItemsTheSame(newItem: Model): Boolean {
      return recipient.id == newItem.recipient.id
    }

    override fun areContentsTheSame(newItem: Model): Boolean {
      return super.areContentsTheSame(newItem) &&
        recipient.hasSameContent(newItem.recipient) &&
        isAdmin == newItem.isAdmin
    }
  }

  class ViewHolder(itemView: View) : MappingViewHolder<Model>(itemView) {
    private val avatar: AvatarImageView = itemView.findViewById(R.id.recipient_avatar)
    private val name: TextView = itemView.findViewById(R.id.recipient_name)
    private val about: TextView? = itemView.findViewById(R.id.recipient_about)
    private val admin: View? = itemView.findViewById(R.id.admin)
    private val badge: BadgeImageView = itemView.findViewById(R.id.recipient_badge)

    override fun bind(model: Model) {
      if (model.onClick != null) {
        itemView.setOnClickListener { model.onClick.invoke() }
      } else {
        itemView.setOnClickListener(null)
      }

      avatar.setRecipient(model.recipient)
      badge.setBadgeFromRecipient(model.recipient)
      name.text = if (model.recipient.isSelf) {
        context.getString(R.string.Recipient_you)
      } else {
        if (model.recipient.isSystemContact) {
          SpannableStringBuilder(model.recipient.getDisplayName(context)).apply {
            val drawable = ContextUtil.requireDrawable(context, R.drawable.symbol_person_circle_24).apply {
              setTint(ContextCompat.getColor(context, R.color.signal_colorOnSurface))
            }
            SpanUtil.appendCenteredImageSpan(this, drawable, 16, 16)
          }
        } else {
          model.recipient.getDisplayName(context)
        }
      }

      val aboutText = model.recipient.combinedAboutAndEmoji
      if (aboutText.isNullOrEmpty()) {
        about?.visibility = View.GONE
      } else {
        about?.text = model.recipient.combinedAboutAndEmoji
        about?.visibility = View.VISIBLE
      }

      admin?.visible = model.isAdmin
    }
  }
}
