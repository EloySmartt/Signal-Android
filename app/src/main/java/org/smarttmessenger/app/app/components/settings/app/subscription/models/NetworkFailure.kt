package com.smarttmessenger.app.components.settings.app.subscription.models

import android.view.View
import com.google.android.material.button.MaterialButton
import com.smarttmessenger.app.R
import com.smarttmessenger.app.components.settings.PreferenceModel
import com.smarttmessenger.app.util.adapter.mapping.LayoutFactory
import com.smarttmessenger.app.util.adapter.mapping.MappingAdapter
import com.smarttmessenger.app.util.adapter.mapping.MappingViewHolder

/**
 * NetworkFailure will display a "card" to the user informing them that there
 * was a failure and give them a button which allows them to retry fetching data.
 */
object NetworkFailure {

  class Model(
    val onRetryClick: () -> Unit
  ) : PreferenceModel<Model>() {
    override fun areItemsTheSame(newItem: Model): Boolean = true
  }

  class ViewHolder(itemView: View) : MappingViewHolder<Model>(itemView) {

    private val retryButton = itemView.findViewById<MaterialButton>(R.id.retry_button)

    override fun bind(model: Model) {
      retryButton.setOnClickListener { model.onRetryClick() }
    }
  }

  fun register(mappingAdapter: MappingAdapter) {
    mappingAdapter.registerFactory(Model::class.java, LayoutFactory({ ViewHolder(it) }, R.layout.network_failure_pref))
  }
}
