# IPTV Smart Player — حالة المشروع

هذا المشروع نتيجة تنفيذ برومبت تطوير تطبيق IPTV المتكامل عبر المراحل 0، 1 (جزئياً)، و3 (الشاشات الرئيسية الخمس).

## ما يعمل فعلياً في هذا الهيكل

- **بنية Clean Architecture كاملة**: `core` (شبكة/قاعدة بيانات/أدوات/مشغل) → `data` (Xtream/TMDB/Room/DataStore) → `domain` (نماذج/Use Cases/واجهات) → `presentation` (شاشات Compose).
- **نظام تصميم فعلي بالكود** (وليس وثيقة فقط): ألوان، طباعة، أبعاد، `FocusableCard` بحالة تركيز موحّدة، `ShimmerPlaceholder`، `EmptyStateView`، `ErrorStateView`.
- **8 شاشات Compose كاملة**: تسجيل الدخول (+ تبديل حسابات محفوظة)، الرئيسية (Hero + صفوف)، الأفلام (فلاتر + شبكة/قائمة + بحث)، **تفاصيل الفيلم** (طاقم عمل + أعمال مشابهة + إعجاب)، المسلسلات (قائمة + تفاصيل بمحدد مواسم وحلقات)، البث المباشر (تصنيفات + قنوات + EPG)، المشغل (محركان: ExoPlayer + libmpv + Overlay شفاف)، **المفضلة** (شبكة موحّدة لكل الأنواع)، الإعدادات (Master-Detail بما فيها إدارة الحسابات ومحرك التشغيل).
- **محركا تشغيل مزدوجان**: ExoPlayer (Media3) كافتراضي، و**libmpv** (`dev.jdtech.mpv:libmpv` من Maven Central) كاحتياطي تلقائي أوسع دعماً للحاويات/الترميزات — عند فشل ExoPlayer (`onPlayerError`) في وضع "تلقائي" (الافتراضي)، يُعاد تشغيل نفس الرابط فوراً عبر libmpv دون أي تدخل من المستخدم، مع شارة صغيرة "mpv" في المشغل ورسالة عابرة تُعلمه بصمت. يمكن للمستخدم فرض أحد المحركين يدوياً من الإعدادات ← التشغيل.
- **حسابات Xtream متعددة مع تخزين مشفَّر**: `AccountsPreferences` يستخدم `EncryptedFile` (androidx.security-crypto) لتخزين اسم المستخدم/كلمة المرور، مع `AccountSwitcherCard` في الإعدادات وشاشة تسجيل دخول كاملة (تتحقق من status الحساب: Active/Expired/Banned قبل الحفظ).
- **حقن الاعتماديات الكامل عبر Hilt**: NetworkModule، DatabaseModule، RepositoryModule، PlayerModule، DataStoreModule.
- **Xtream Codes API + TMDB API**: عملاء Retrofit كاملان بكل الـ endpoints المذكورة في البرومبت.
- **Room Database**: كل الجداول السبعة (favorites، watch_history، custom_catalogs، catalog_items، channels، epg_data، tmdb_cache).
- **NetworkBoundResource**: نمط الكاش-أولاً-ثم-الشبكة مطبّق فعلياً.

## محرك libmpv — تفاصيل مهمة قبل البناء

- الاعتمادية `dev.jdtech.mpv:libmpv:1.0.0` منشورة فعلياً على **Maven Central** (لا حاجة لـJitPack)، من مشروع [jarnedemeulemeester/libmpv-android](https://github.com/jarnedemeulemeester/libmpv-android) (رخصة MIT). تحقّق من رقم الإصدار الأحدث على Maven Central عند فتح المشروع لأول مرة.
- الحجم الإضافي على APK ملموس (libmpv يحزم ffmpeg/libass/libplacebo كاملة لكل ABI) — إن كان الحجم مصدر قلق، فعّل [App Bundle / ABI splits](https://developer.android.com/build/configure-apk-splits) بدل APK عام واحد.
- أسماء دوال `MPVLib` في `MpvController.kt` (`create`/`init`/`attachSurface`/`observeProperty`/`addObserver`...) مطابقة للنمط الموثّق في مشروع mpv-android الأصلي؛ عند فتح المشروع في Android Studio بعد Sync، استخدم "Go to Declaration" على `MPVLib` للتأكد من تطابق التوقيعات بالضبط مع نسخة AAR الفعلية (قد تختلف تفاصيل صغيرة بين إصدارات المكتبة).
- ملاحظة Kotlin: اسم الحزمة `is.xyz.mpv` يحتوي كلمة `is` المحجوزة في Kotlin، لذا الاستيراد مكتوب بصيغة `` `is`.xyz.mpv.MPVLib `` (بعلامات backtick) — هذا مقصود وليس خطأ طباعة.
- التكامل الحالي **لا يربط libmpv بعد بخدمة الخلفية** (`PlaybackService`/MediaSession) — عند التشغيل عبر libmpv، التحكم من الإشعار/شاشة القفل غير مفعّل بعد (ExoPlayer فقط مربوط بـMediaSessionService حالياً). هذا موجود ضمن "المتبقي" أدناه.

## تشغيل المحتوى الفعلي — تم ربطه الآن

`PlayerViewModel` يبني رابط التشغيل الحقيقي تلقائياً عند فتح المشغل (عبر `XtreamUrlBuilder` الجديد) بدل انتظار استدعاء خارجي لم يكن أحد يقوم به فعلياً سابقاً:
- **مباشر**: `{server}/live/{user}/{pass}/{streamId}.ts`
- **أفلام**: يجلب `containerExtension` الصحيح من تفاصيل الفيلم أولاً ثم يبني `{server}/movie/{user}/{pass}/{streamId}.{ext}`
- **حلقات المسلسلات**: `{server}/series/{user}/{pass}/{episodeId}.mp4` (الامتداد الافتراضي فقط — لا يُقرأ من `containerExtension` الحقيقي للحلقة بعد)

**فجوة معروفة**: "أكمل المشاهدة" لعنصر مسلسل يستخدم مفتاحاً مركّباً (`{series_id}_{season}_{episode}`) بينما `resolveAndPlay()` يتوقع `stream_id` رقمياً صرفاً للأفلام — النقر على حلقة مسلسل من صف "أكمل المشاهدة" لن يعمل حالياً حتى تُفصَل هذه الحالة في `PlayerViewModel`.

## بناء APK — لا يمكنني بناءه هنا فعلياً

بيئة التنفيذ التي أعمل بها (chat/sandbox) **لا تحتوي Android SDK ولا Gradle، والوصول للإنترنت لتنزيلهما ممنوع صراحة** (`x-deny-reason: host_not_allowed` عند أي محاولة اتصال بخوادم Google/Gradle). لذلك لا يمكنني تجميع كود Kotlin/Compose/Hilt الفعلي إلى ملف APK قابل للتثبيت — أي ملف كنت سأضعه بهذا الاسم سيكون فارغاً أو تالفاً ولن يُثبَّت على تلفازك.

**الحل المُضاف فعلياً في هذا المستودع**: ملف `.github/workflows/build-apk.yml` — أتمتة GitHub Actions تبني APK حقيقياً تلقائياً على خوادم GitHub (التي تملك SDK/Gradle/إنترنت كاملة):

1. أنشئ مستودع جديد فارغ على GitHub وارفع محتوى هذا الـ zip بالكامل إليه (`git push`).
2. (اختياري) أضف مفتاح TMDB: Settings → Secrets and variables → Actions → New repository secret باسم `TMDB_API_KEY`.
3. من تبويب **Actions** في المستودع، شغّل workflow "بناء APK للتلفاز" (أو انتظر تشغيله تلقائياً بعد الـ push).
4. بعد 5-10 دقائق، حمّل الملف الناتج (`iptv-smart-player-debug-apk`) من قسم **Artifacts** في نفس التشغيل، فك ضغطه لتحصل على `app-debug.apk`.
5. انسخه لجهاز Android TV (عبر USB أو تطبيق مثل "Send Files to TV") وثبّته مباشرة (فعّل "مصادر غير معروفة" إن طُلب).

البديل الأبسط إن كان لديك Android Studio مثبَّتاً على جهازك: افتح المشروع → Build → Build APK(s)، بدون الحاجة لـ GitHub إطلاقاً.

## كيف تفتحه

1. افتح المجلد كمشروع في **Android Studio** (Koala أو أحدث).
2. أضف مفتاح TMDB في `local.properties`:
   ```
   TMDB_API_KEY=ضع_مفتاحك_هنا
   ```
3. اضغط Sync ثم Run على محاكي Android TV أو جهاز حقيقي.
4. عند أول تشغيل ستظهر شاشة تسجيل الدخول — أدخل بيانات اشتراك Xtream Codes حقيقية (عنوان السيرفر + المنفذ، اسم المستخدم، كلمة المرور).

> ملاحظة: لم يتم تنفيذ Gradle Sync الفعلي أو الترجمة (Build) داخل هذه البيئة لعدم توفر Android SDK/شبكة إنترنت هنا — الكود مكتوب ومُنظَّم بالكامل يدوياً حسب توثيق Compose/Media3/Room/Hilt/libmpv الرسمي، لكن ينصح بفتحه في Android Studio لأول Sync لالتقاط أي خطأ طباعة بسيط قبل المتابعة.

## المتبقي (حسب خطة البرومبت الأصلية)

- **ربط libmpv بـ MediaSessionService** للتحكم من الإشعار/شاشة القفل (حالياً ExoPlayer فقط مربوط).
- **إصلاح "أكمل المشاهدة" لحلقات المسلسلات** — المفتاح المركّب لا يتوافق حالياً مع `resolveAndPlay()` في PlayerViewModel (انظر قسم "تشغيل المحتوى الفعلي" أعلاه).
- **منطق مطابقة Xtream ↔ TMDB الكامل**: حالياً `tmdb_id` يُقرأ إن توفر من Xtream مباشرة؛ يحتاج fallback بمطابقة الاسم+السنة عند غيابه، مع `SyncWithTMDBUseCase` وتفعيل جدول `tmdb_cache` فعلياً (معرَّف لكن غير مستهلك في الـ Repositories بعد).
- **الكتالوجات المخصصة**: DAO/Entity جاهزان؛ Repository + ViewModel + شاشة الإدارة لم تُبنَ بعد.
- **دليل EPG الكامل (Timeline Grid) بعرض أفقي زمني** — الحالي يعرض EPG القناة المختارة فقط (الآن/التالي)؛ الشبكة الزمنية الكاملة لكل القنوات معاً تحتاج مكوناً خاصاً إضافياً.
- **الاختبارات (Unit/Integration مع MockWebServer)** غير مكتوبة بعد — "العقل السابع" في البرومبت.
- **تصدير/استيراد إعدادات JSON**، وProguard النهائي قبل التوقيع.
- **دعم Compose SharedTransitionLayout** للانتقال السلس بطاقة↔تفاصيل (مذكور في نظام التصميم، غير مطبّق بعد).
- **جودة/مسار صوت/ترجمة قابلة للاختيار من overlay المشغل** — الأيقونات موجودة بصرياً لكن غير مربوطة بمنطق فعلي بعد لأي من المحركين.
- **AppTopBar المشترك غير مربوط فعلياً بالـ NavHost** في MainActivity — المكوّن موجود وجاهز لكن الشاشات الحالية تُعرض بدونه (لا يوجد شريط تنقل ثابت عبر الشاشات بعد).

## بنية المجلدات

```
app/src/main/java/com/iptv/smartplayer/
├── core/            (di, network, database, util, player [ExoPlayer + libmpv])
├── data/            (remote/xtream, remote/tmdb, local/entity, local/dao, local/datastore, repository, mapper)
├── domain/          (model, usecase, repository)
└── presentation/    (theme, navigation, components, auth, home, movies, series, live, player, settings)
```
