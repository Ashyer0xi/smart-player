package com.iptv.smartplayer.presentation.theme

import androidx.compose.ui.unit.dp

/**
 * وحدات القياس والمسافات — مبنية على وحدة أساسية 8dp كما في مواصفات نظام التصميم.
 */
object IptvDimens {
    val unit = 8.dp

    // هامش آمن لأجهزة TV (Overscan) لتفادي اقتصاص المحتوى عند حواف الشاشة
    val tvOverscanMargin = 48.dp
    val mobileScreenMargin = 16.dp

    // أبعاد بطاقات المحتوى
    val posterCardWidth = 140.dp
    val posterCardHeight = 210.dp // نسبة 2:3
    val posterCardWidthTv = 180.dp
    val posterCardHeightTv = 270.dp

    val channelLogoSize = 96.dp
    val episodeCardWidth = 320.dp
    val episodeCardHeight = 180.dp

    // زوايا دائرية موحّدة
    val cardCornerRadius = 12.dp
    val chipCornerRadius = 20.dp

    // حالة التركيز (Focus)
    val focusScale = 1.1f
    val focusBorderWidth = 3.dp
    val focusAnimationMillis = 150

    // الهيرو
    val heroHeightFraction = 0.55f
}
