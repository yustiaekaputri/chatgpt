# Perbaikan 84 warning Android Lint

Seluruh 84 warning pada laporan sebelumnya telah ditangani. Pemeriksaan ulang dengan konfigurasi lint proyek menghasilkan **0 warning dan 0 error** pada varian **debug dan release**. Tidak ada baseline atau pengecualian lint baru untuk menyembunyikan warning.

| Kelompok warning awal | Jumlah | Perbaikan |
| --- | ---: | --- |
| DefaultLocale | 2 | Pemrosesan URL/ekstensi memakai Locale.ROOT agar unduhan tidak gagal pada locale seperti bahasa Turki. |
| DuplicateIncludedIds | 1 | Panel gagal jaringan dan halaman memakai ID berbeda. |
| UnusedAttribute | 2 | Ripple dipasang pada FrameLayout/CardView yang mendukung foreground sejak API 21. |
| QueryPermissionsNeeded | 1 | Pencarian browser dibatasi ke handler tautan HTTPS, dengan deklarasi queries yang sesuai; tidak meminta QUERY_ALL_PACKAGES. |
| UseRequiresApi | 1 | Callback WebView API 23 diberi @RequiresApi. |
| InsecureBaseConfiguration | 1 | HTTP global dinonaktifkan sesuai konfirmasi bahwa semua situs aplikasi memakai HTTPS; konten HTTP pada halaman HTTPS juga diblokir. |
| ObsoleteSdkInt | 12 | Cabang Android lama yang tidak terjangkau dengan minSdk 21 dihapus, termasuk fallback-nya. |
| UseCompoundDrawables | 1 | Baris menu menggunakan ConstraintLayout; gambar tetap terpisah karena dimuat melalui Glide dan diberi warna secara dinamis. |
| Overdraw | 2 | Background ripple ganda pada baris menu dihapus; separator ditempatkan langsung pada layout header yang memakainya. |
| UnusedResources | 33 | 32 resource yang tidak dipakai dihapus. Ikon OneSignal dipertahankan dengan tools:keep karena dimuat SDK berdasarkan nama saat runtime, sekaligus melindunginya dari resource shrinking. |
| UselessParent | 6 | Wrapper layout diringkas; referensi view holder dan panel animasi dialog disesuaikan. |
| MonochromeLauncherIcon | 2 | Adaptive icon memakai alpha artwork foreground yang sama untuk ikon bertema. |
| IconMissingDensityFolder | 1 | Varian ikon mdpi/xhdpi/xxhdpi/xxxhdpi diturunkan dari artwork hdpi asli. Ilustrasi/background yang ukurannya ditentukan ImageView dipindahkan ke drawable-nodpi. |
| ContentDescription | 9 | Tombol aksi diberi label yang bermakna; gambar dekoratif dikeluarkan dari pengumuman pembaca layar agar tidak dibaca dua kali. |
| SetTextI18n | 4 | Teks versi dan ukuran cache memakai string resource berformat. |
| HardcodedText | 6 | Teks dialog dan label iklan dipindahkan ke string resource; nilai protokol tidak diambil dari teks terjemahan. |

Source/target Java dinaikkan ke 17, sesuai toolchain AGP yang dipakai, untuk menghilangkan warning kompilasi source/target Java 8 yang sudah deprecated pada JDK 21. minSdk tetap 21, targetSdk/compileSdk tetap 36, applicationId dan versionCode tetap seperti hasil upgrade sebelumnya.

## Validasi

Perintah berikut berhasil:

```sh
./gradlew --continue :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:lintRelease :app:bundleRelease
```

**15 test lulus, 0 gagal, 0 dilewati.** Lima test sebelumnya tetap dijalankan. Sepuluh kasus tambahan pada model API 28/36 memeriksa URL pada locale Turki, inflasi dan tipe view holder, ripple, panel/label dialog keluar, keberadaan ikon notifikasi, pemblokiran mixed content, serta penemuan browser yang sesuai. Ini merupakan uji JVM Robolectric, bukan uji UI pada perangkat Android.

Pengaturan lint GradleDependency dan GradleDynamicVersion tetap mengikuti proyek sebelumnya. Catatan compiler tentang API deprecated dan warning namespace milik pustaka consent/UMP pihak ketiga adalah keluaran build yang terpisah dari 84 warning Android Lint ini; tidak diklaim hilang. Tidak ada SDK iklan yang dibuang untuk meluluskan pemeriksaan.

Karena HTTP telah ditutup sesuai permintaan, pastikan URL konfigurasi JSON, halaman, gambar, unduhan, dan konten tersemat menggunakan HTTPS tanpa redirect ke HTTP. Perilaku website/iklan/notifikasi, tampilan animasi dialog, dan ikon bertema tetap perlu diperiksa pada perangkat sebelum produksi. Lihat PANDUAN-UPGRADE-API36.md untuk signing dan rilis Play Console.
