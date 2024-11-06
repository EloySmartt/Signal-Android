package com.smarttmessenger.app.mediasend.v2.gallery

import com.smarttmessenger.app.util.adapter.mapping.MappingModel

data class MediaGalleryState(
  val bucketId: String?,
  val bucketTitle: String?,
  val items: List<MappingModel<*>> = listOf()
)
