plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id ("kotlin-kapt")
    id ("dagger.hilt.android.plugin")
}

android {
    namespace = "com.msn.dataselectionviewpager"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.msn.dataselectionviewpager"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Hilt dependencies
    implementation ("com.google.dagger:hilt-android:2.48.1")
    implementation(project(":smartswitch"))
    kapt ("com.google.dagger:hilt-android-compiler:2.48.1")

    // Optional: Hilt ViewModel and Navigation support
//    implementation ("androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha03")
    kapt("androidx.hilt:hilt-compiler:1.0.0")
    implementation ("androidx.navigation:navigation-fragment-ktx:2.7.2")
    implementation ("androidx.navigation:navigation-ui-ktx:2.7.2")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // ViewPager2 for swiping between fragments
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    // Data Binding (ViewBinding)
    implementation("androidx.databinding:viewbinding:7.0.4")

    // MVVM Architecture Components
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.4.0")

    // For DocumentsManager and other storage access
    implementation("androidx.documentfile:documentfile:1.0.1")

    // Shapeable
    implementation("com.google.android.material:material:1.2.0")

    // SDP and SSP
    implementation("com.intuit.sdp:sdp-android:1.1.1")
    implementation("com.intuit.ssp:ssp-android:1.1.1")

    // Circular ImageView
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")

    // Retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.10.0")

    implementation ("com.hbb20:ccp:2.5.0")
    implementation ("androidx.room:room-runtime:2.5.0")
    kapt ("androidx.room:room-compiler:2.5.0")
    implementation ("androidx.room:room-ktx:2.5.0")
    implementation ("com.google.code.gson:gson:2.8.9")
    implementation (platform("com.google.firebase:firebase-bom:31.0.1"))
    implementation ("com.google.firebase:firebase-analytics")
//    implementation 'com.google.firebase:firebase-config'
    implementation ("com.google.firebase:firebase-config-ktx")
    implementation ("com.google.firebase:firebase-crashlytics-buildtools:2.9.2")
    implementation ("com.airbnb.android:lottie:5.2.0")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.5.1")


    // camera x
    val cameraxVersion = "1.5.0-alpha05"
    // The following line is optional, as the core library is included indirectly by camera-camera2
    implementation("androidx.camera:camera-core:${cameraxVersion}")
    implementation("androidx.camera:camera-camera2:${cameraxVersion}")
    // If you want to additionally use the CameraX Lifecycle library
    implementation("androidx.camera:camera-lifecycle:${cameraxVersion}")
    // If you want to additionally use the CameraX View class
    implementation("androidx.camera:camera-view:${cameraxVersion}")
    // If you want to additionally use the CameraX Extensions library
    implementation("androidx.camera:camera-extensions:${cameraxVersion}")

    // ML kit barcode reader
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

}
kapt {
    correctErrorTypes =true
}

