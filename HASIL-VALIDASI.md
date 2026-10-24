# Hasil validasi API 36

Validasi pada 24 Oktober 2026 di cloud Linux dengan JDK 21.0.12.1 (javac), Android SDK Platform 36, Build-Tools 35.0.0, AGP 8.10.1, dan Gradle 8.11.1.

## Hasil akhir

Perintah Gradle berikut selesai dengan `BUILD SUCCESSFUL`:

```sh
./gradlew --continue :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:lintRelease :app:bundleRelease
```

Pada cloud ini, perintah dipanggil melalui `/workspace/android-setup/run-gradle.py`, yang memilih JDK lengkap, SDK, cache yang dapat ditulis, serta konfigurasi proxy bawaan. Konfigurasi khusus cloud itu tidak dibutuhkan pada komputer lokal dengan Android Studio/SDK yang sudah terpasang.

| Pemeriksaan | Hasil |
| --- | --- |
| Kompilasi Java dan resource debug/release | Lulus |
| APK debug | Berhasil dibuat |
| App Bundle release | Berhasil dibuat, **belum ditandatangani** |
| Uji unit Robolectric | **15 lulus, 0 gagal, 0 dilewati** |
| Android Lint debug | **0 error; 0 warning** |
| Android Lint release | **0 error; 0 warning** |
| Lint vital release | Analisis selesai; task pelaporan vital dilewati setelah full lint release |
| Pemeriksaan manifest APK memakai aapt | compileSdk 36, targetSdk 36, minSdk 21 |
| Pemeriksaan library native pada APK dan AAB | Tidak ada berkas `.so` |

Uji unit yang benar-benar berjalan:

- URI kamera melalui FileProvider dapat ditulis/dibaca dan memiliki grant URI yang diperlukan: API 28, 35, 36 (3 kasus).
- Insets status/navigation bar dan keyboard tidak menumpuk saat dikirim ulang: API 35, 36 (2 kasus).
- Regresi pembersihan warning (locale URL, view holder/ripple, dialog/ikon notifikasi, mixed content, pencarian browser): API 28, 36 (10 kasus).

Identitas hasil build:

```text
applicationId = com.app.solodroidwebviewjson3
versionCode   = 5
versionName   = 3.1.1
minSdk        = 21
targetSdk     = 36
compileSdk    = 36
```

SDK dan distribusi Gradle diunduh dengan verifikasi checksum; paket JDK diperoleh melalui metadata repositori Debian yang diverifikasi APT. Verifikasi TLS/checksum tidak dinonaktifkan. Kendala awal jaringan, JDK yang hanya menyediakan runtime, serta cache runner yang tidak dapat ditulis telah diatasi sebelum hasil akhir di atas.

## Hal yang belum diverifikasi

- Aplikasi belum dijalankan pada perangkat/emulator. Mesin ini tidak menyediakan perangkat Android atau `/dev/kvm`. Uji Robolectric adalah uji JVM, bukan uji instalasi/UI perangkat.
- Splash/JSON server, iklan/consent, notifikasi, pemilih file/kamera nyata, pembaruan dari versi produksi, serta perilaku video/keyboard/rotasi perlu diuji menggunakan daftar pada `PANDUAN-UPGRADE-API36.md`.
- Tidak ada signing release atau publikasi Google Play yang dilakukan. Keystore/upload key tetap milik pemilik aplikasi. Nilai package name dan versionCode terbesar di Play Console belum dikonfirmasi.
- Tidak ada binary native dalam APK/AAB yang dihasilkan, sehingga tidak ada alignment ELF native yang perlu diperbaiki pada build ini. Pengujian pada perangkat page size 16 KB belum dilakukan; periksa ulang jika versi dependensi berubah.

Lint debug dan release kini sama-sama melaporkan **0 warning dan 0 error** setelah 84 warning sebelumnya ditangani. Lihat `PERBAIKAN-WARNING.md` untuk rinciannya. Tidak ada baseline atau pengecualian lint baru untuk meluluskan pemeriksaan. Pengecualian GradleDependency/GradleDynamicVersion tetap mengikuti konfigurasi proyek sebelumnya.

Source/target Java sekarang 17. Catatan compiler tentang penggunaan API deprecated dan warning namespace consent library/UMP pihak ketiga masih merupakan keluaran build yang terpisah; tidak diklaim hilang. HTTP global dan mixed content WebView dinonaktifkan sesuai konfirmasi pemilik bahwa semua situs memakai HTTPS.

Hasil ini membuktikan build dan 15 uji regresi di instance cloud saat ini. Hasil tersebut bukan persetujuan kebijakan Google Play atau bukti validasi pada lingkungan cloud baru setelah publikasi.
