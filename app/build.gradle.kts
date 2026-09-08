plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.iptv.smartplayer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.iptv.smartplayer"
        // الحد الأدنى 24 لضمان توافق واسع مع أجهزة Android TV القديمة نسبياً
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // متغيرات بيئية لمفاتيح API — تُقرأ من local.properties ولا تُرفع لأي مستودع
        buildConfigField("String", "TMDB_API_KEY", "\"${'$'}{project.findProperty("TMDB_API_KEY") ?: ""}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            // تفادي تعارض نسخ .so عند وجود أكثر من مكتبة أصلية بنفس الاسم (ExoPlayer + libmpv)
            pickFirsts += listOf("**/libc++_shared.so")
        }
    }
}

dependencies {
    // ---------- Core / Kotlin ----------
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // ---------- Compose (Mobile) ----------
    val composeBom = platform("androidx.compose:compose-bom:2024.09.02")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.navigation:navigation-compose:2.8.1")

    // ---------- Compose for TV (Leanback الحديث) ----------
    implementation("androidx.tv:tv-foundation:1.0.0-alpha11")
    implementation("androidx.tv:tv-material:1.0.0")

    // ---------- Media3 / ExoPlayer ----------
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.4.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.4.1")
    implementation("androidx.media3:media3-ui:1.4.1")
    implementation("androidx.media3:media3-session:1.4.1")
    implementation("androidx.media3:media3-datasource-okhttp:1.4.1")

    // ---------- libmpv (محرك تشغيل احتياطي أوسع دعماً للحاويات/الترميزات من ExoPlayer) ----------
    // AAR رسمي منشور على Maven Central (مضمّن أعلاه عبر mavenCentral() في settings.gradle.kts).
    // المصدر: https://github.com/jarnedemeulemeester/libmpv-android — رخصة MIT.
    // ملاحظة: تحقق من رقم الإصدار الأحدث على Maven Central قبل البناء، فقد يصدر إصدار أجد من 1.0.0.
    implementation("dev.jdtech.mpv:libmpv:1.0.0")

    // ---------- الشبكة (Networking) ----------
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ---------- قاعدة البيانات (Room) ----------
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // ---------- حقن الاعتماديات (Hilt) ----------
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-android-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // ---------- تحميل الصور ----------
    implementation("io.coil-kt:coil-compose:2.7.0")

    // ---------- التصفح المُقسّم (القوائم الطويلة) ----------
    implementation("androidx.paging:paging-runtime-ktx:3.3.2")
    implementation("androidx.paging:paging-compose:3.3.2")

    // ---------- تخزين آمن للإعدادات وبيانات الدخول ----------
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // ---------- أيقونات ----------
    implementation("androidx.compose.material:material-icons-extended")

    // ---------- الاختبارات ----------
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("app.cash.turbine:turbine:1.1.0")
    testImplementation("io.mockk:mockk:1.13.12")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
