package com.example.data

data class SamplePreset(
    val category: String,
    val title: String,
    val text: String,
    val recommendedVoiceId: String,
    val recommendedStyleId: String
)

object SamplePresets {
    val list = listOf(
        SamplePreset(
            category = "Berita",
            title = "Kabar Pagi Nusantara",
            text = "Selamat pagi pemirsa, inilah kabar terkini dari ibu kota dan seluruh penjuru tanah air. Badan Meteorologi memprakirakan cuaca cerah berawan akan menyelimuti sebagian besar wilayah kepulauan Indonesia hari ini.",
            recommendedVoiceId = "budi",
            recommendedStyleId = "news"
        ),
        SamplePreset(
            category = "Dongeng",
            title = "Kisah Sang Kancil & Rimba",
            text = "Alkisah, di tepian sungai yang jernih di bawah naungan pohon beringin tua, hiduplah seekor kancil yang cerdik. Suatu sore saat kabut turun, ia mendengar suara gemerisik lembut dari balik semak belukar...",
            recommendedVoiceId = "lestari",
            recommendedStyleId = "story"
        ),
        SamplePreset(
            category = "Motivasi",
            title = "Percaya Pada Proses",
            text = "Jangan pernah meremehkan setiap langkah kecil yang kamu ambil hari ini. Sukses bukanlah tentang kecepatan semata, melainkan tentang keteguhan hati untuk terus melangkah walau badai menerpa!",
            recommendedVoiceId = "bayu",
            recommendedStyleId = "motivational"
        ),
        SamplePreset(
            category = "Pengumuman",
            title = "Informasi Bandara",
            text = "Perhatian kepada seluruh penumpang penerbangan Garuda Indonesia dengan nomor penerbangan GA empat ratus dua belas tujuan Denpasar, pintu keberangkatan lima kini telah dibuka.",
            recommendedVoiceId = "dewi",
            recommendedStyleId = "natural"
        ),
        SamplePreset(
            category = "Relaksasi",
            title = "Jeda Napas Sejenak",
            text = "Tarik napas perlahan... rasakan udara sejuk mengisi rongga dadamu. Lepaskan seluruh beban pikiran hari ini bersama hembusan napas yang tenang. Istirahatlah sejenak, jiwamu pantas untuk damai.",
            recommendedVoiceId = "lestari",
            recommendedStyleId = "calm"
        ),
        SamplePreset(
            category = "Sosial Media",
            title = "Sapa Teman & Follower",
            text = "Halo semuanya! Gimana kabarnya hari ini? Hari ini aku pengen spill rahasia kecil yang bikin produktivitas naik drastis tanpa ribet. Jangan lupa tonton sampai habis ya!",
            recommendedVoiceId = "rian",
            recommendedStyleId = "casual"
        )
    )
}
