import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "vn.ltdidong.apphoctienganh"
    compileSdk {
        version = release(36)
    }

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "vn.ltdidong.apphoctienganh"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val groqApiKey = localProperties.getProperty("GROQ_API_KEY") ?: ""
        val groqChatModel = localProperties.getProperty("GROQ_CHAT_MODEL") ?: ""
        val groqVisionModel = localProperties.getProperty("GROQ_VISION_MODEL") ?: ""

        buildConfigField("String", "GROQ_API_KEY", "\"$groqApiKey\"")
        buildConfigField("String", "GROQ_CHAT_MODEL", "\"$groqChatModel\"")
        buildConfigField("String", "GROQ_VISION_MODEL", "\"$groqVisionModel\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson:gson:2.10.1")

    implementation(libs.room.common.jvm)
    implementation(libs.room.runtime)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

//    retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

//  Markwon
    implementation("io.noties.markwon:core:4.6.2")
    
//  Jsoup for web scraping
    implementation("org.jsoup:jsoup:1.17.2")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")

    implementation ("com.google.firebase:firebase-auth")
    implementation ("com.google.firebase:firebase-database")

    annotationProcessor(libs.room.compiler)

    implementation("androidx.gridlayout:gridlayout:1.0.0")

    // CameraX dependencies
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)

    // các gói phụ thuộc cho tính năng gợi ý từ vựng mới
    implementation("com.google.code.gson:gson:2.10.1")
    
    // WorkManager for background tasks
    implementation("androidx.work:work-runtime:2.9.0")
    
    // RecyclerView (nếu chưa có)
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    
    // CardView (nếu chưa có)
    implementation("androidx.cardview:cardview:1.0.0")
}
