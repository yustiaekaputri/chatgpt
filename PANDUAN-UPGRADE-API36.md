# Upgrade Solodroid Aliendroid ke Android 16 (API 36)

Proyek ini diperbarui mengikuti pemberitahuan target API 36 yang Anda kirim.
ZIP asli memakai compileSdk/targetSdk 34; bukan 35 seperti yang disebut dalam pemberitahuan Play Console. Karena itu, pastikan ZIP ini memang sumber aplikasi yang ingin Anda perbarui.

## Perubahan

| Pengaturan | ZIP asli | Hasil upgrade |
| --- | --- | --- |
| compileSdk | 34 | 36 |
| targetSdk | 34 | 36 |
| minSdk | 21 | 21 |
| Android Gradle Plugin | 8.4.1 | 8.10.1 |
| Gradle Wrapper | 8.6 | 8.11.1, disertai checksum SHA-256 |
| versionCode | 4 | 5 |
| versionName | 3.1.0 | 3.1.1 |

`applicationId` tetap `com.app.solodroidwebviewjson3`. Pengaturan website/JSON, iklan, OneSignal, ikon, dan nama aplikasi tetap mengikuti berkas asli. AndroidX Activity 1.10.1 dan Core 1.15.0 ditambahkan untuk API navigasi/insets yang dipakai.

Perubahan kompatibilitas:

- Navigasi Kembali melalui `OnBackPressedDispatcher`: menutup drawer, keluar dari video layar penuh, menelusuri riwayat WebView, lalu menjalankan perilaku keluar aplikasi. Di halaman pengaturan, Kembali pindah ke tab pertama sebelum menutup halaman.
- Pada Android 15/16, konten memperhitungkan status bar, navigation bar, cutout, dan keyboard. Insets diterapkan sekali agar tidak bertumpuk dengan `fitsSystemWindows` lama. Video memakai `WindowInsetsControllerCompat` untuk menyembunyikan/menampilkan bilah sistem.
- Kamera unggahan memakai URI `content://` dari `FileProvider` dengan direktori cache terbatas, menggantikan URI file dan direktori gambar publik. Kamera dibuka melalui aplikasi kamera perangkat, sehingga tidak memerlukan izin CAMERA aplikasi ini.
- Hasil pemilih file, termasuk pembatalan, langsung diteruskan ke WebView tanpa meminta izin baca penyimpanan. Unduhan `DownloadManager` pada Android 10+ tidak lagi terhambat permintaan izin penyimpanan lama. Izin tulis tetap dibatasi sampai API 28 untuk perangkat lama.

## Membuka dan melakukan build

1. Ekstrak ZIP hasil upgrade ke folder baru. Buka folder yang berisi `settings.gradle` melalui Android Studio.
2. Gunakan Android Studio yang mendukung AGP 8.10.1 dan Gradle JDK 17 atau 21 **lengkap, termasuk javac**. Validasi cloud memakai JDK 21.0.12.1.
3. Di SDK Manager, pasang **Android SDK Platform 36**, **Android SDK Build-Tools 35.0.0**, dan **Android SDK Platform-Tools**. Build-Tools 35.0.0 adalah versi default yang didukung AGP 8.10; nilainya tidak perlu sama dengan targetSdk.
4. Jalankan Gradle Sync. `local.properties` dibuat oleh Android Studio sesuai lokasi SDK komputer Anda dan tidak disertakan dalam ZIP.
5. Dari terminal folder proyek, jalankan:

```sh
# Linux/macOS; jika izin eksekusi hilang setelah ekstraksi:
chmod +x gradlew
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:lintRelease :app:bundleRelease
```

Pada Windows, gunakan `gradlew.bat` sebagai pengganti `./gradlew`.

Akses jaringan dibutuhkan ke Google Maven (`dl.google.com`), Maven Central, Gradle, JitPack, dan repositori SDK iklan yang tercantum di `settings.gradle`: `maven.ogury.co`, `android-sdk.is.com`, `unity3ddist.jfrog.io`, dan `maven.wortise.com`. Jangan menonaktifkan verifikasi TLS atau checksum untuk mengatasi kegagalan unduhan.

SDK iklan/push bawaan masih menggunakan beberapa dependensi dengan rentang versi. Simpan laporan dependensi untuk rilis Anda (`./gradlew :app:dependencies --configuration releaseRuntimeClasspath`) dan periksa lagi peringatan SDK di Play Console saat mengunggah.

## Sebelum memperbarui aplikasi yang sudah ada

1. Cocokkan package name di Play Console dengan `applicationId` di `app/build.gradle`. Jangan mengganti package name untuk membuat pembaruan aplikasi yang sama. Jika sumber ini ternyata hanya template dan berbeda dari aplikasi produksi, gunakan sumber aplikasi produksi yang benar; konfigurasi JSON Solodroid juga terikat pada applicationId.
2. Periksa `versionCode` terbesar pada semua track/artefak Play Console, termasuk internal/closed/open testing. Nilai baru harus lebih besar. Angka **5** di proyek ini berdasarkan angka **4** pada ZIP; satu kali unggah belum membuktikan versionCode sebelumnya bernilai 1 atau 4.
3. Di Android Studio, pilih **Build > Generate Signed App Bundle / APK > Android App Bundle** dan varian `release`. Gunakan **upload key/keystore yang sama** atau kunci yang saat ini terdaftar di Play App Signing. Simpan kunci dan kata sandinya sendiri, bukan dalam chat atau kode.
4. `bundleRelease` tanpa konfigurasi signing hanya memverifikasi build dan menghasilkan AAB yang belum ditandatangani. AAB itu belum siap dikirim sebagai rilis produksi. APK debug juga bukan pengganti rilis produksi.
5. Upload AAB bertanda tangan ke **Internal testing**, lalu instal dari tautan pengujian Play. Setelah pengujian selesai, rilis/promosikan versi API 36 ke **Production**, mengikuti pemberitahuan yang Anda terima. Periksa kembali status kebijakan setelah rilis diproses; mengedit targetSdk atau mengunggah ke internal saja tidak menyelesaikan langkah publikasi produksi.

## Daftar uji pada perangkat sebelum produksi

Uji minimal di Android 16, Android 15, dan satu perangkat versi minimum yang masih Anda dukung:

- Pembaruan dari aplikasi lama mempertahankan data, serta splash/konfigurasi JSON membuka website yang benar.
- Geser Kembali dan tombol navigasi tiga tombol: riwayat WebView, drawer terbuka, halaman pengaturan, serta dialog keluar.
- Toolbar, iklan/banner, drawer, halaman redirect, dan WebView tidak tertutup status bar/navigation bar/cutout. Keyboard tidak menutupi input. Periksa mode gelap, rotasi, tablet/foldable, dan multi-window; Android 16 dapat mengabaikan penguncian orientasi pada layar besar.
- Masuk dan keluar video layar penuh menggunakan Kembali; bilah sistem dan toolbar pulih dengan benar.
- Pilih file, ambil foto dengan aplikasi kamera eksternal, serta batalkan pemilih file. Unduh file dan pastikan ada di Downloads. Pada Android 9/lebih lama, uji izin penyimpanan ditolak lalu diizinkan.
- Notifikasi OneSignal, link dari notifikasi, iklan/consent, in-app review/update, dan izin lokasi jika diaktifkan pada konfigurasi aplikasi.
- Lihat pre-launch report, peringatan SDK, kebijakan izin, Data safety, dan kompatibilitas page size 16 KB di Play Console. Jika dependensi baru menambahkan `.so`, periksa alignment ELF dan packaging; menaikkan targetSdk saja tidak memperbaiki binary native pihak ketiga.

Sebanyak 15 uji unit Robolectric menjalankan kode insets pada model Android 15/16, URI kamera pada model Android 9/15/16, serta regresi layout, locale, browser, dan mixed content pada model Android 9/16. Uji JVM tersebut tidak menggantikan pengujian UI, kamera perangkat, iklan, dan notifikasi pada perangkat/emulator.

## Rujukan resmi

- Setup SDK Android 16: https://developer.android.com/about/versions/16/setup-sdk
- Perubahan perilaku Android 16, termasuk Back dan edge-to-edge: https://developer.android.com/about/versions/16/behavior-changes-16
- Kompatibilitas AGP 8.10: https://developer.android.com/build/releases/past-releases/agp-8-10-0-release-notes
- Dukungan page size 16 KB: https://developer.android.com/guide/practices/page-sizes

Hasil validasi cloud dicatat terpisah dalam `HASIL-VALIDASI.md`.

Pembersihan 84 warning dijelaskan di `PERBAIKAN-WARNING.md`. Source/target Java memakai versi 17. HTTP global dan mixed content dinonaktifkan sesuai konfirmasi bahwa seluruh situs memakai HTTPS; URL JSON, halaman, gambar, dan unduhan harus mendukung HTTPS.
