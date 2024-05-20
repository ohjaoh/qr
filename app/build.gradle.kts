plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "kr.ac.yuhan.cs.qr"
    compileSdk = 34

    defaultConfig {
        applicationId = "kr.ac.yuhan.cs.qr"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    // Firebase BOM (Bill of Materials) - Firebase 관련 라이브러리 버전을 관리
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))
    // Firebase Analytics - 앱 사용 데이터 분석 관련
    implementation("com.google.firebase:firebase-analytics")
    // QR 스캔 라이브러리 - ZXing ("Zebra Crossing") 기반 QR 코드 스캐너
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    // CircleImageView - 이미지를 원형으로 표시하는 뷰 라이브러리
    implementation("de.hdodenhof:circleimageview:3.1.0")
    // Firebase Authentication - 사용자 인증 관련
    implementation("com.google.firebase:firebase-auth:22.3.1")
    // Firebase Firestore - NoSQL 데이터베이스 서비스
    implementation("com.google.firebase:firebase-firestore:24.11.0")
    // Firebase Storage - 클라우드 파일 저장 서비스
    implementation("com.google.firebase:firebase-storage:20.3.0")

    //기본의존성
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")


    // Glide 라이브러리 추가
    implementation("com.github.bumptech.glide:glide:4.13.2");
    annotationProcessor("com.github.bumptech.glide:compiler:4.13.2");
}