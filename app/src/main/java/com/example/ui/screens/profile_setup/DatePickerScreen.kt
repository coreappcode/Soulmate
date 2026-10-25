package com.example.ui.screens.profile_setup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GradientButton
import com.example.ui.components.SoulTopBar
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AuroraHorizontalGradient
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CardBackground
import com.example.ui.theme.H3Style
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.OrchidPrimary
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.utils.Validators
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun DatePickerScreen(
    currentDate: String,
    onDateConfirmed: (String) -> Unit,
    onBack: () -> Unit
) {
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    val dateFormatter = remember {
        SimpleDateFormat("MMM d, yyyy", Locale.US).apply { isLenient = false }
    }
    val today = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
    val initialDate = remember(currentDate) {
        val parsePosition = ParsePosition(0)
        val parsed = dateFormatter.parse(currentDate, parsePosition)
            ?.takeIf { parsePosition.index == currentDate.length }
        Calendar.getInstance().apply {
            if (parsed != null) {
                time = parsed
            } else {
                add(Calendar.YEAR, -18)
            }
        }
    }

    var selectedYear by remember(currentDate) {
        mutableIntStateOf(initialDate.get(Calendar.YEAR))
    }
    var selectedMonthIndex by remember(currentDate) {
        mutableIntStateOf(initialDate.get(Calendar.MONTH))
    }
    var selectedDay by remember(currentDate) {
        mutableIntStateOf(initialDate.get(Calendar.DAY_OF_MONTH))
    }

    fun isAllowedDate(year: Int, monthIndex: Int, day: Int): Boolean {
        val candidate = Calendar.getInstance().apply {
            clear()
            set(year, monthIndex, day)
        }
        if (candidate.after(today)) return false
        return Validators.isValidAdultDateOfBirth(dateFormatter.format(candidate.time))
    }

    fun updateMonth(year: Int, monthIndex: Int) {
        val firstOfMonth = Calendar.getInstance().apply {
            clear()
            set(year, monthIndex, 1)
        }
        selectedYear = firstOfMonth.get(Calendar.YEAR)
        selectedMonthIndex = firstOfMonth.get(Calendar.MONTH)
        selectedDay = selectedDay.coerceAtMost(
            firstOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        )
    }

    val daysInMonth = Calendar.getInstance().apply {
        clear()
        set(selectedYear, selectedMonthIndex, 1)
    }.getActualMaximum(Calendar.DAY_OF_MONTH)

    val daysList = (1..daysInMonth).toList()
    val dayNames = listOf("M", "T", "W", "T", "F", "S", "S")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .navigationBarsPadding()
            .testTag("date_picker_screen")
    ) {
        SoulTopBar(
            title = "Select Date of Birth",
            showBack = true,
            onBackClick = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Month and Year selector controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val previousMonth = Calendar.getInstance().apply {
                            clear()
                            set(selectedYear, selectedMonthIndex, 1)
                            add(Calendar.MONTH, -1)
                        }
                        updateMonth(
                            previousMonth.get(Calendar.YEAR),
                            previousMonth.get(Calendar.MONTH)
                        )
                    },
                    enabled = selectedYear > 1 || selectedMonthIndex > Calendar.JANUARY
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Month",
                        tint = TextPrimary
                    )
                }

                Text(
                    text = "${months[selectedMonthIndex]} $selectedYear",
                    style = H3Style,
                    color = TextPrimary
                )

                IconButton(
                    onClick = {
                        val nextMonth = Calendar.getInstance().apply {
                            clear()
                            set(selectedYear, selectedMonthIndex, 1)
                            add(Calendar.MONTH, 1)
                        }
                        val currentMonth = Calendar.getInstance().apply {
                            clear()
                            set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), 1)
                        }
                        if (!nextMonth.after(currentMonth)) {
                            updateMonth(
                                nextMonth.get(Calendar.YEAR),
                                nextMonth.get(Calendar.MONTH)
                            )
                        }
                    },
                    enabled = selectedYear < today.get(Calendar.YEAR) ||
                        selectedMonthIndex < today.get(Calendar.MONTH)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Month",
                        tint = TextPrimary
                    )
                }
            }

            // Quick year picker row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(1995, 1998, 2000, 2002).forEach { year ->
                    val isSelected = selectedYear == year
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .then(
                                if (isSelected) Modifier.background(AuroraHorizontalGradient)
                                else Modifier
                                    .background(CardBackground)
                                    .border(1.dp, BorderSubtle, CircleShape)
                            )
                            .clickable { selectedYear = year }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = year.toString(),
                            fontFamily = PoppinsFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (isSelected) Color.White else TextSecondary
                        )
                    }
                }
            }

            // Day of week headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                dayNames.forEach { dayName ->
                    Text(
                        text = dayName,
                        fontFamily = PoppinsFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                    items(daysList) { day ->
                    val isSelected = selectedDay == day
                    val isAllowed = isAllowedDate(selectedYear, selectedMonthIndex, day)

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .then(
                                if (isSelected) {
                                    Modifier.background(AuroraHorizontalGradient)
                                } else {
                                    Modifier
                                }
                            )
                            .clickable(enabled = isAllowed) { selectedDay = day }
                            .testTag("date_day_$day"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.toString(),
                            fontFamily = InterFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp,
                            color = when {
                                isSelected -> Color.White
                                !isAllowed -> TextMuted
                                else -> TextPrimary
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val selectedDate = Calendar.getInstance().apply {
                clear()
                set(selectedYear, selectedMonthIndex, selectedDay)
            }
            val formattedDate = dateFormatter.format(selectedDate.time)
            val isAllowedSelection =
                isAllowedDate(selectedYear, selectedMonthIndex, selectedDay)

            GradientButton(
                text = "Confirm Date ($formattedDate)",
                onClick = {
                    if (isAllowedSelection) {
                        onDateConfirmed(formattedDate)
                    }
                },
                enabled = isAllowedSelection,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("confirm_date_button")
            )
        }
    }
}
