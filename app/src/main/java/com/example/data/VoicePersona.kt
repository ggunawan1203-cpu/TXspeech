package com.example.data

data class VoicePersona(
    val id: String,
    val name: String,
    val geminiVoiceName: String,
    val gender: String, // "Wanita" / "Pria"
    val timbre: String, // e.g. "Hangat & Lembut"
    val description: String,
    val recommendedStyle: String,
    val avatarEmoji: String,
    val accentColor: Long
)

object VoicePersonas {
    val list = listOf(
        VoicePersona(
            id = "lestari",
            name = "Lestari",
            geminiVoiceName = "Aoede",
            gender = "Wanita",
            timbre = "Hangat, Lembut & Keibuan",
            description = "Suara teduh dan penuh kasih, sangat cocok untuk narasi buku, cerita dongeng, meditasi, dan puisi.",
            recommendedStyle = "Narasi Cerita & Dongeng",
            avatarEmoji = "🌸",
            accentColor = 0xFFE91E63
        ),
        VoicePersona(
            id = "budi",
            name = "Budi",
            geminiVoiceName = "Charon",
            gender = "Pria",
            timbre = "Wibawa, Berat & Tenang",
            description = "Suara dalam dengan artikulasi tegas, ideal untuk pembaca berita resmi, pengumuman formal, dan dokumenter.",
            recommendedStyle = "Pembaca Berita Formal",
            avatarEmoji = "🎙️",
            accentColor = 0xFF3F51B5
        ),
        VoicePersona(
            id = "dewi",
            name = "Dewi",
            geminiVoiceName = "Kore",
            gender = "Wanita",
            timbre = "Ceria, Ramah & Jernih",
            description = "Suara cerah dan bersahabat, sangat pas untuk asisten virtual, percakapan harian, customer service, dan edukasi.",
            recommendedStyle = "Alami & Ramah",
            avatarEmoji = "✨",
            accentColor = 0xFF00BFA5
        ),
        VoicePersona(
            id = "bayu",
            name = "Bayu",
            geminiVoiceName = "Fenrir",
            gender = "Pria",
            timbre = "Energik, Percaya Diri & Kuat",
            description = "Suara bersemangat dan meyakinkan, cocok untuk iklan produk, podcast motivasi, olahraga, dan video promosi.",
            recommendedStyle = "Motivasi & Semangat",
            avatarEmoji = "⚡",
            accentColor = 0xFFFF6E40
        ),
        VoicePersona(
            id = "rian",
            name = "Rian",
            geminiVoiceName = "Puck",
            gender = "Pria",
            timbre = "Santai, Kasual & Gaul",
            description = "Gaya bicara rileks seperti teman mengobrol, pas untuk dialog media sosial, video TikTok/Reels, dan review santai.",
            recommendedStyle = "Kasual & Santai",
            avatarEmoji = "🎧",
            accentColor = 0xFF7C4DFF
        )
    )

    val defaultPersona = list[2] // Dewi
}
