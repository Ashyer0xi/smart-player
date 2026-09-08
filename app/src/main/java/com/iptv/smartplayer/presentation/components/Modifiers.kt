package com.iptv.smartplayer.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

/**
 * مودفير خفيف لعناصر نصية بسيطة قابلة للتركيز (روابط شريط التنقل، عناصر القوائم النصية)
 * دون التأثيرات البصرية الكاملة لـ FocusableCard (تُستخدم عندما يكون العنصر نصاً وليس بطاقة).
 */
fun Modifier.clickableFocusable(onClick: () -> Unit): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    this
        .focusable(interactionSource = interactionSource)
        .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
}
