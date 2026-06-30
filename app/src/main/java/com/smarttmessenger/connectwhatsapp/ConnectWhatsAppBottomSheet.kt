/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.smarttmessenger.connectwhatsapp

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.signal.core.ui.compose.BottomSheets
import org.signal.core.ui.compose.Buttons
import org.signal.core.ui.compose.theme.SignalTheme
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.compose.ComposeBottomSheetDialogFragment
import org.thoughtcrime.securesms.registration.ui.countrycode.Country
import org.thoughtcrime.securesms.registration.ui.countrycode.CountryCodeSelectScreen
import org.thoughtcrime.securesms.registration.ui.countrycode.CountryCodeViewModel
import org.thoughtcrime.securesms.util.BottomSheetUtil
import org.thoughtcrime.securesms.util.Util

/**
 * UI/UX-only flow for connecting a WhatsApp account. Presents a single bottom sheet that walks
 * through four steps (intro, phone number, country picker, linking code) with in-sheet navigation.
 *
 * No backend or real WhatsApp integration is wired up yet; functional actions are placeholders.
 */
class ConnectWhatsAppBottomSheet : ComposeBottomSheetDialogFragment() {

  override val peekHeightPercentage: Float = 0.92f

  private val countryViewModel: CountryCodeViewModel by viewModels()

  @Composable
  override fun SheetContent() {
    val context = LocalContext.current

    var step by remember { mutableStateOf(Step.INTRO) }
    var selectedCountry by remember { mutableStateOf(defaultCountry()) }
    var phoneNumber by remember { mutableStateOf("") }

    when (step) {
      Step.INTRO -> IntroStep(
        onLinkClick = { step = Step.PHONE }
      )

      Step.PHONE -> PhoneStep(
        country = selectedCountry,
        phoneNumber = phoneNumber,
        onPhoneNumberChange = { phoneNumber = it },
        onBack = { step = Step.INTRO },
        onPickCountry = { step = Step.COUNTRY },
        onConnectClick = { step = Step.CODE }
      )

      Step.COUNTRY -> CountryStep(
        viewModel = countryViewModel,
        initialCountry = selectedCountry,
        onCountrySelected = {
          selectedCountry = it
          step = Step.PHONE
        },
        onDismissed = { step = Step.PHONE }
      )

      Step.CODE -> CodeStep(
        code = LINK_CODE,
        formattedNumber = formatNumber(selectedCountry, phoneNumber),
        onBack = { step = Step.PHONE },
        onCopyClick = {
          Util.copyToClipboard(context, LINK_CODE)
          Toast.makeText(context, R.string.ConnectWhatsApp_code__copied, Toast.LENGTH_SHORT).show()
        },
        onCodeEnteredClick = {
          // TODO: Wire up real WhatsApp linking once backend integration exists. No behavior yet.
        }
      )
    }
  }

  companion object {
    private const val LINK_CODE = "4F6T-P7Y8"

    @JvmStatic
    fun show(fragmentManager: FragmentManager) {
      ConnectWhatsAppBottomSheet().show(fragmentManager, BottomSheetUtil.STANDARD_BOTTOM_SHEET_FRAGMENT_TAG)
    }

    private fun defaultCountry(): Country = Country(emoji = "🇺🇸", name = "United States", countryCode = 1, regionCode = "US")

    private fun formatNumber(country: Country, phoneNumber: String): String {
      return "+${country.countryCode} $phoneNumber".trim()
    }
  }

  private enum class Step {
    INTRO,
    PHONE,
    COUNTRY,
    CODE
  }
}

private val GUTTER = 24.dp
private const val WHATSAPP_ICON_SCALE = 0.62f

@Composable
private fun IntroStep(onLinkClick: () -> Unit) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .fillMaxWidth()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = GUTTER)
      .padding(bottom = 24.dp)
  ) {
    BottomSheets.Handle(modifier = Modifier.padding(top = 8.dp))

    WhatsAppTile(
      size = 64.dp,
      modifier = Modifier.padding(top = 16.dp)
    )

    Text(
      text = stringResource(R.string.ConnectWhatsApp_intro__title, stringResource(R.string.app_name)),
      style = MaterialTheme.typography.headlineSmall,
      textAlign = TextAlign.Center,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.padding(top = 16.dp)
    )

    Text(
      text = stringResource(R.string.ConnectWhatsApp_intro__subtitle, stringResource(R.string.app_name)),
      style = MaterialTheme.typography.bodyMedium,
      textAlign = TextAlign.Center,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(top = 8.dp)
    )

    Column(modifier = Modifier.padding(top = 24.dp)) {
      InfoRow(
        iconRes = R.drawable.symbol_calendar_24,
        title = stringResource(R.string.ConnectWhatsApp_intro__row_windows_title),
        body = stringResource(R.string.ConnectWhatsApp_intro__row_windows_body)
      )
      InfoRow(
        iconRes = R.drawable.symbol_reply_24,
        title = stringResource(R.string.ConnectWhatsApp_intro__row_reply_title),
        body = stringResource(R.string.ConnectWhatsApp_intro__row_reply_body, stringResource(R.string.app_name)),
        modifier = Modifier.padding(top = 20.dp)
      )
      InfoRow(
        iconRes = R.drawable.symbol_bell_slash_24,
        title = stringResource(R.string.ConnectWhatsApp_intro__row_inbox_title),
        body = stringResource(R.string.ConnectWhatsApp_intro__row_inbox_body, stringResource(R.string.app_name)),
        modifier = Modifier.padding(top = 20.dp)
      )
    }

    Footnote(
      text = stringResource(R.string.ConnectWhatsApp_intro__footnote, stringResource(R.string.app_name)),
      modifier = Modifier.padding(top = 24.dp)
    )

    Buttons.LargePrimary(
      onClick = onLinkClick,
      colors = whatsAppButtonColors(),
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 24.dp)
    ) {
      Text(text = stringResource(R.string.ConnectWhatsApp_intro__action))
    }
  }
}

@Composable
private fun PhoneStep(
  country: Country,
  phoneNumber: String,
  onPhoneNumberChange: (String) -> Unit,
  onBack: () -> Unit,
  onPickCountry: () -> Unit,
  onConnectClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .fillMaxWidth()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = GUTTER)
      .padding(bottom = 24.dp)
  ) {
    BottomSheets.Handle(modifier = Modifier.padding(top = 8.dp))

    SheetBackButton(
      onBack = onBack,
      modifier = Modifier
        .align(Alignment.Start)
        .padding(top = 8.dp)
    )

    WhatsAppTile(
      size = 56.dp,
      modifier = Modifier.padding(top = 4.dp)
    )

    Text(
      text = stringResource(R.string.ConnectWhatsApp_phone__title),
      style = MaterialTheme.typography.headlineSmall,
      textAlign = TextAlign.Center,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.padding(top = 16.dp)
    )

    Text(
      text = stringResource(R.string.ConnectWhatsApp_phone__subtitle),
      style = MaterialTheme.typography.bodyMedium,
      textAlign = TextAlign.Center,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(top = 8.dp)
    )

    Text(
      text = stringResource(R.string.ConnectWhatsApp_phone__label).uppercase(),
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier
        .align(Alignment.Start)
        .padding(top = 28.dp, bottom = 8.dp)
    )

    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()
    ) {
      CountrySelector(
        country = country,
        onClick = onPickCountry
      )

      OutlinedTextField(
        value = phoneNumber,
        onValueChange = onPhoneNumberChange,
        singleLine = true,
        placeholder = { Text(text = stringResource(R.string.ConnectWhatsApp_phone__hint)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .padding(start = 12.dp)
          .weight(1f)
      )
    }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 12.dp)
    ) {
      Icon(
        painter = painterResource(R.drawable.symbol_lock_24),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(16.dp)
      )
      Text(
        text = stringResource(R.string.ConnectWhatsApp_phone__helper),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 8.dp)
      )
    }

    Buttons.LargePrimary(
      onClick = onConnectClick,
      colors = whatsAppButtonColors(),
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 28.dp)
    ) {
      Text(text = stringResource(R.string.ConnectWhatsApp_phone__action))
    }
  }
}

@Composable
private fun CountryStep(
  viewModel: CountryCodeViewModel,
  initialCountry: Country,
  onCountrySelected: (Country) -> Unit,
  onDismissed: () -> Unit
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val screenHeight = LocalConfiguration.current.screenHeightDp.dp

  // Load once for the lifetime of the (fragment-scoped) view model; re-visiting the step reuses it.
  LaunchedEffect(Unit) {
    if (viewModel.state.value.countryList.isEmpty()) {
      viewModel.loadCountries(initialCountry)
    }
  }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(screenHeight * 0.85f)
  ) {
    CountryCodeSelectScreen(
      state = state,
      title = stringResource(R.string.ConnectWhatsApp_phone__choose_country),
      onSearch = viewModel::filterCountries,
      onDismissed = onDismissed,
      onClick = onCountrySelected
    )
  }
}

@Composable
private fun CodeStep(
  code: String,
  formattedNumber: String,
  onBack: () -> Unit,
  onCopyClick: () -> Unit,
  onCodeEnteredClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .fillMaxWidth()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = GUTTER)
      .padding(bottom = 24.dp)
  ) {
    BottomSheets.Handle(modifier = Modifier.padding(top = 8.dp))

    SheetBackButton(
      onBack = onBack,
      modifier = Modifier
        .align(Alignment.Start)
        .padding(top = 8.dp)
    )

    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(top = 4.dp)
    ) {
      AppIconTile(size = 56.dp)
      Icon(
        painter = painterResource(R.drawable.symbol_transfer_24),
        contentDescription = stringResource(R.string.ConnectWhatsApp__link_content_description),
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .size(24.dp)
      )
      WhatsAppTile(size = 56.dp)
    }

    Text(
      text = stringResource(R.string.ConnectWhatsApp_code__title),
      style = MaterialTheme.typography.headlineSmall,
      textAlign = TextAlign.Center,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.padding(top = 16.dp)
    )

    Text(
      text = stringResource(R.string.ConnectWhatsApp_code__subtitle, formattedNumber),
      style = MaterialTheme.typography.bodyMedium,
      textAlign = TextAlign.Center,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(top = 8.dp)
    )

    CodeBox(
      code = code,
      onCopyClick = onCopyClick,
      modifier = Modifier.padding(top = 24.dp)
    )

    Text(
      text = stringResource(R.string.ConnectWhatsApp_code__how_title).uppercase(),
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier
        .align(Alignment.Start)
        .padding(top = 28.dp, bottom = 4.dp)
    )

    NumberedStep(1, stringResource(R.string.ConnectWhatsApp_code__step_1))
    NumberedStep(2, stringResource(R.string.ConnectWhatsApp_code__step_2))
    NumberedStep(3, stringResource(R.string.ConnectWhatsApp_code__step_3))
    NumberedStep(4, stringResource(R.string.ConnectWhatsApp_code__step_4))

    Buttons.LargePrimary(
      onClick = onCodeEnteredClick,
      colors = whatsAppButtonColors(),
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 28.dp)
    ) {
      Text(text = stringResource(R.string.ConnectWhatsApp_code__action))
    }
  }
}

@Composable
private fun InfoRow(
  iconRes: Int,
  title: String,
  body: String,
  modifier: Modifier = Modifier
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier.fillMaxWidth()
  ) {
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .size(40.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(SignalTheme.colors.colorSurface2)
    ) {
      Icon(
        painter = painterResource(iconRes),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.size(22.dp)
      )
    }

    Column(modifier = Modifier.padding(start = 16.dp)) {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = body,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 2.dp)
      )
    }
  }
}

@Composable
private fun Footnote(text: String, modifier: Modifier = Modifier) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(SignalTheme.colors.colorSurface2)
      .padding(horizontal = 16.dp, vertical = 12.dp)
  ) {
    Icon(
      painter = painterResource(R.drawable.symbol_lock_24),
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.size(18.dp)
    )
    Text(
      text = text,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(start = 12.dp)
    )
  }
}

@Composable
private fun CountrySelector(country: Country, onClick: () -> Unit) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .clip(RoundedCornerShape(12.dp))
      .background(SignalTheme.colors.colorSurface2)
      .clickable(onClick = onClick)
      .padding(horizontal = 14.dp, vertical = 16.dp)
  ) {
    Text(text = country.emoji)
    Text(
      text = "+${country.countryCode}",
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.padding(start = 8.dp)
    )
    Icon(
      painter = painterResource(R.drawable.symbol_chevron_down_24),
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier
        .padding(start = 4.dp)
        .size(20.dp)
    )
  }
}

@Composable
private fun CodeBox(code: String, onCopyClick: () -> Unit, modifier: Modifier = Modifier) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(SignalTheme.colors.colorSurface2)
      .padding(horizontal = 20.dp, vertical = 18.dp)
  ) {
    Text(
      text = code,
      style = MaterialTheme.typography.headlineSmall.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = 3.sp
      ),
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.weight(1f)
    )

    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .clip(RoundedCornerShape(8.dp))
        .clickable(onClick = onCopyClick)
        .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
      Icon(
        painter = painterResource(R.drawable.symbol_copy_android_24),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(18.dp)
      )
      Text(
        text = stringResource(R.string.ConnectWhatsApp_code__copy),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 6.dp)
      )
    }
  }
}

@Composable
private fun NumberedStep(number: Int, text: String) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 12.dp)
  ) {
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .size(24.dp)
        .clip(CircleShape)
        .background(SignalTheme.colors.colorSurface2)
    ) {
      Text(
        text = number.toString(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface
      )
    }
    Text(
      text = text,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.padding(start = 16.dp)
    )
  }
}

@Composable
private fun SheetBackButton(onBack: () -> Unit, modifier: Modifier = Modifier) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .clickable(onClick = onBack)
      .padding(vertical = 6.dp, horizontal = 4.dp)
  ) {
    Icon(
      painter = painterResource(R.drawable.symbol_arrow_start_24),
      contentDescription = stringResource(R.string.ConnectWhatsApp__back_content_description),
      tint = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.size(24.dp)
    )
    Text(
      text = stringResource(R.string.ConnectWhatsApp__back_content_description),
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.padding(start = 4.dp)
    )
  }
}

@Composable
private fun WhatsAppTile(size: Dp, modifier: Modifier = Modifier) {
  Box(
    contentAlignment = Alignment.Center,
    modifier = modifier
      .size(size)
      .clip(RoundedCornerShape(size / 4))
      .background(colorResource(R.color.whatsapp_green))
  ) {
    Icon(
      painter = painterResource(R.drawable.ic_whatsapp_glyph),
      contentDescription = stringResource(R.string.ConnectWhatsApp__whatsapp_icon_content_description),
      tint = colorResource(R.color.core_white),
      modifier = Modifier.size(size * WHATSAPP_ICON_SCALE)
    )
  }
}

@Composable
private fun AppIconTile(size: Dp, modifier: Modifier = Modifier) {
  // Renders the app's own owl logo as a rounded-square tile that mirrors the paired WhatsApp tile,
  // independent of the device's adaptive-icon mask shape. Uses a default-config vector
  // (ic_owl_logo) so it resolves on every supported API level.
  val shape = RoundedCornerShape(size / 4)
  Box(
    modifier = modifier
      .size(size)
      .clip(shape)
      .background(colorResource(R.color.ic_launcher_background))
      .border(1.dp, MaterialTheme.colorScheme.outline, shape),
    contentAlignment = Alignment.Center
  ) {
    Image(
      painter = painterResource(R.drawable.ic_owl_logo),
      contentDescription = stringResource(R.string.ConnectWhatsApp__app_icon_content_description, stringResource(R.string.app_name)),
      modifier = Modifier.size(size)
    )
  }
}

@Composable
private fun whatsAppButtonColors() = ButtonDefaults.buttonColors(
  containerColor = colorResource(R.color.whatsapp_green),
  contentColor = colorResource(R.color.core_white)
)
