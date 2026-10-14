package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppBackground
import com.example.ui.theme.AuroraHorizontalGradient
import com.example.ui.theme.BorderFocus
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CardBackground
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.HairlineDivider
import com.example.ui.theme.InputShape
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.OrchidPrimary
import com.example.ui.theme.PillShape
import com.example.ui.theme.PoppinsFontFamily
import com.example.ui.theme.SemanticError
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SoulTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    supportingText: (@Composable () -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else 5,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isError) {
                            Modifier.border(2.dp, SemanticError, InputShape)
                        } else if (isFocused) {
                            Modifier.border(2.dp, AuroraHorizontalGradient, InputShape)
                        } else {
                            Modifier.border(1.5.dp, HairlineDivider, InputShape)
                        }
                    )
                    .onFocusChanged { isFocused = it.isFocused }
                    .testTag("input_${label.lowercase().replace(" ", "_")}"),
                placeholder = {
                    if (placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            fontFamily = InterFontFamily,
                            color = TextMuted,
                            fontSize = 15.sp
                        )
                    }
                },
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                isError = isError,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                singleLine = singleLine,
                maxLines = maxLines,
                readOnly = readOnly || (onClick != null),
                enabled = enabled,
                textStyle = TextStyle(
                    fontFamily = InterFontFamily,
                    fontSize = 15.sp,
                    color = TextPrimary
                ),
                shape = InputShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    errorBorderColor = Color.Transparent,
                    errorContainerColor = ElevatedSurface
                )
            )

            if (onClick != null) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(InputShape)
                        .clickable { onClick() }
                        .testTag("clickable_overlay_${label.lowercase().replace(" ", "_")}")
                )
            }
        }

        if (supportingText != null) {
            Box(modifier = Modifier.padding(top = 4.dp, start = 4.dp)) {
                supportingText()
            }
        }
    }
}

@Composable
fun PhoneInput(
    countryCode: String,
    countryFlag: String,
    phoneNumber: String,
    onCountryTap: () -> Unit,
    onNumberChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    supportingText: String? = null
) {
    var isPhoneFocused by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Country dial code button
            Row(
                modifier = Modifier
                    .height(56.dp)
                    .clip(InputShape)
                    .background(Color.White)
                    .border(1.5.dp, HairlineDivider, InputShape)
                    .clickable { onCountryTap() }
                    .padding(horizontal = 14.dp)
                    .testTag("country_code_selector"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = countryFlag,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = countryCode,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select Country",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Phone number text field
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() }
                    if (filtered.length <= 15) {
                        onNumberChange(filtered)
                    }
                },
                placeholder = {
                    Text(
                        text = "98765 43210",
                        fontFamily = InterFontFamily,
                        color = TextMuted,
                        fontSize = 15.sp
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                isError = isError,
                shape = InputShape,
                textStyle = TextStyle(
                    fontFamily = InterFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    errorBorderColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .then(
                        if (isError) {
                            Modifier.border(2.dp, SemanticError, InputShape)
                        } else if (isPhoneFocused) {
                            Modifier.border(2.dp, AuroraHorizontalGradient, InputShape)
                        } else {
                            Modifier.border(1.5.dp, HairlineDivider, InputShape)
                        }
                    )
                    .onFocusChanged { isPhoneFocused = it.isFocused }
                    .testTag("phone_number_input")
            )
        }

        if (!supportingText.isNullOrEmpty()) {
            Text(
                text = supportingText,
                fontFamily = InterFontFamily,
                fontSize = 12.sp,
                color = if (isError) SemanticError else TextSecondary,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

@Composable
fun OtpInput(
    length: Int = 6,
    otp: String,
    onOtpChange: (String) -> Unit,
    onComplete: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    val coroutineScope = rememberCoroutineScope()
    val shakeOffset = remember { Animatable(0f) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(200)
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    LaunchedEffect(isError) {
        if (isError) {
            coroutineScope.launch {
                shakeOffset.animateTo(15f, animationSpec = tween(50))
                shakeOffset.animateTo(-15f, animationSpec = tween(50))
                shakeOffset.animateTo(10f, animationSpec = tween(50))
                shakeOffset.animateTo(-10f, animationSpec = tween(50))
                shakeOffset.animateTo(0f, animationSpec = tween(50))
            }
        }
    }

    // Blinking cursor indicator for active box
    val cursorTransition = rememberInfiniteTransition(label = "otp_cursor")
    val cursorAlpha by cursorTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_blink"
    )

    Box(
        modifier = modifier
            .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Visual boxes with high-visibility 2dp pure black borders
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until length) {
                val digit = otp.getOrNull(i)?.toString() ?: ""
                val isCurrentBox = otp.length == i
                val isFilled = digit.isNotEmpty()

                val boxShape = RoundedCornerShape(16.dp)
                val bgColor = if (isError) ElevatedSurface else Color.White

                Box(
                    modifier = Modifier
                        .size(width = 48.dp, height = 62.dp)
                        .clip(boxShape)
                        .background(bgColor)
                        .then(
                            if (isError) {
                                Modifier.border(2.dp, SemanticError, boxShape)
                            } else if (isCurrentBox) {
                                Modifier.border(2.dp, AuroraHorizontalGradient, boxShape)
                            } else {
                                Modifier.border(1.5.dp, HairlineDivider, boxShape)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isFilled) {
                        Text(
                            text = digit,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            textAlign = TextAlign.Center,
                            color = TextPrimary
                        )
                    } else if (isCurrentBox) {
                        Box(
                            modifier = Modifier
                                .width(2.5.dp)
                                .height(26.dp)
                                .alpha(cursorAlpha)
                                .background(OrchidPrimary, RoundedCornerShape(1.dp))
                        )
                    }
                }
            }
        }

        // Invisible input field overlay that directly handles soft keyboard input
        BasicTextField(
            value = otp,
            onValueChange = { newOtp ->
                val digits = newOtp.filter { it.isDigit() }.take(length)
                onOtpChange(digits)
                if (digits.length == length) {
                    onComplete(digits)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            cursorBrush = SolidColor(Color.Transparent),
            textStyle = TextStyle(color = Color.Transparent),
            modifier = Modifier
                .matchParentSize()
                .focusRequester(focusRequester)
                .alpha(0.01f)
                .testTag("otp_hidden_input")
        )
    }
}

@Composable
fun SoulSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "Search...",
    modifier: Modifier = Modifier,
    onClear: () -> Unit = { onValueChange("") }
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                fontFamily = InterFontFamily,
                color = TextMuted,
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = TextSecondary
            )
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        singleLine = true,
        shape = PillShape,
        textStyle = TextStyle(
            fontFamily = InterFontFamily,
            fontSize = 15.sp,
            color = TextPrimary
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .border(1.5.dp, HairlineDivider, PillShape)
            .testTag("search_bar")
    )
}
