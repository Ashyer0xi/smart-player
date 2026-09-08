package com.iptv.smartplayer.core.player

/**
 * محركات التشغيل المدعومة:
 * - EXOPLAYER: الافتراضي، أخف وأسرع بدءاً، ويتكامل مباشرة مع MediaSessionService/الإشعار.
 * - LIBMPV: احتياطي بمحرك mpv (ffmpeg كامل) — يدعم حاويات/ترميزات/ملفات ترجمة أوسع بكثير
 *   من extractors الخاصة بـExoPlayer، وهو الحل الشائع في تطبيقات IPTV لبثوث MPEG-TS "المعطوبة"
 *   جزئياً أو ملفات MKV بمسارات صوت/ترجمة نادرة لا يفهمها ExoPlayer.
 * - AUTO: يُجرَّب ExoPlayer أولاً؛ عند فشله (onPlayerError) يُعاد المحاولة تلقائياً عبر libmpv
 *   دون أي تدخّل من المستخدم — هذا هو الخيار الافتراضي الموصى به.
 */
enum class PlayerEngineType {
    AUTO,
    EXOPLAYER,
    LIBMPV,
}
