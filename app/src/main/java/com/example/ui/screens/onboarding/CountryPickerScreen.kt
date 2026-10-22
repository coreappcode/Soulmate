package com.example.ui.screens.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Country
import com.example.data.CountryCatalog
import com.example.ui.components.SoulSearchBar
import com.example.ui.components.SoulTopBar
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AuroraHorizontalGradient
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CardBackground
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.OrchidPrimary
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CountryPickerScreen(
    selectedCountry: Country,
    onCountrySelected: (Country) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredCountries = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            CountryCatalog.allCountries
        } else {
            CountryCatalog.allCountries.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                    it.dialCode.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val groupedCountries = remember(filteredCountries) {
        filteredCountries.groupBy { it.name.first().uppercaseChar() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("country_picker_screen")
    ) {
        SoulTopBar(
            title = "Select Country",
            showBack = true,
            onBackClick = onBack
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            SoulSearchBar(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search country name or code..."
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
        ) {
            // Popular section when not searching
            if (searchQuery.isBlank()) {
                stickyHeader {
                    HeaderTitle("Popular")
                }

                items(CountryCatalog.popularCountries) { country ->
                    CountryRow(
                        country = country,
                        isSelected = country.name == selectedCountry.name,
                        onClick = { onCountrySelected(country) }
                    )
                }

                item {
                    HorizontalDivider(
                        color = BorderSubtle,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }

                stickyHeader {
                    HeaderTitle("All Countries")
                }
            }

            groupedCountries.forEach { (initial, countries) ->
                stickyHeader {
                    LetterHeader(initial.toString())
                }

                items(countries) { country ->
                    CountryRow(
                        country = country,
                        isSelected = country.name == selectedCountry.name,
                        onClick = { onCountrySelected(country) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun HeaderTitle(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppBackground)
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun LetterHeader(letter: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppBackground)
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = letter,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = OrchidPrimary
        )
    }
}

@Composable
private fun CountryRow(
    country: Country,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val rowShape = RoundedCornerShape(14.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(rowShape)
            .then(
                if (isSelected) {
                    Modifier
                        .background(CardBackground)
                        .border(2.5.dp, Color(0xFF000000), rowShape)
                } else {
                    Modifier
                        .background(CardBackground)
                        .border(1.5.dp, Color(0xFF000000), rowShape)
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("country_row_${country.name.lowercase()}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = country.flag,
            fontSize = 22.sp
        )

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = country.name,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = country.dialCode,
            fontFamily = InterFontFamily,
            fontSize = 14.sp,
            color = TextMuted,
            modifier = Modifier.padding(end = 8.dp)
        )

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = OrchidPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
