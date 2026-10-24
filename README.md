# Solodroid Aliendroid — Android 16 / API 36

Proyek Android hasil upgrade dari ZIP Solodroid Aliendroid, dengan 84 warning Android Lint yang sudah ditangani.

## Unduh proyek

**[Unduh ZIP proyek siap dibuka di Android Studio](https://github.com/yustiaekaputri/chatgpt/raw/refs/heads/main/downloads/solodroid-api36.zip)**

Jika tautan unduhan langsung tidak terbuka, buka folder [`downloads`](downloads), pilih `solodroid-api36.zip`, lalu tekan tombol **Download raw file** di GitHub. Anda juga dapat memilih **Code → Download ZIP** dari halaman utama repositori untuk mengunduh seluruh isi repositori.

Ekstrak ZIP dan buka folder yang berisi `settings.gradle` melalui Android Studio. ZIP proyek tidak berisi cache build, `local.properties`, atau keystore.

## Status validasi cloud

- `compileSdk` / `targetSdk`: **36**; `minSdk`: **21**.
- Android Gradle Plugin **8.10.1**, Gradle **8.11.1**, Java source/target **17**.
- Android Lint debug dan release: **0 warning, 0 error** pada konfigurasi lint proyek.
- **15 pengujian Robolectric lulus**, 0 gagal, 0 dilewati.
- Build APK debug dan AAB release berhasil. AAB belum ditandatangani untuk produksi.
- HTTP global dan mixed content WebView dinonaktifkan sesuai konfirmasi pemilik bahwa situs aplikasi menggunakan HTTPS.

Lihat [panduan upgrade dan rilis](PANDUAN-UPGRADE-API36.md), [rincian perbaikan warning](PERBAIKAN-WARNING.md), dan [hasil validasi](HASIL-VALIDASI.md).

Sebelum rilis, cocokkan applicationId dan versionCode dengan Play Console, uji pada perangkat, lalu buat signed AAB menggunakan upload key yang terdaftar. Setelah pengujian internal, rilis ke Production. Belum ada publikasi Google Play yang dilakukan.
