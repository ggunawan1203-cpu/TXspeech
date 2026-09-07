package com.example.data

data class ExpressiveStyle(
    val id: String,
    val label: String,
    val subtitle: String,
    val iconEmoji: String,
    val promptInstruction: String,
    val defaultPitch: Float = 1.0f,
    val defaultRate: Float = 1.0f
)

object ExpressiveStyles {
    val list = listOf(
        ExpressiveStyle(
            id = "natural",
            label = "Alami & Ramah",
            subtitle = "Tutur kata hangat bersahabat",
            iconEmoji = "😊",
            promptInstruction = "Ucapkan dengan nada hangat, santai, alami, dan ramah seperti berbicara langsung dengan teman.",
            defaultPitch = 1.0f,
            defaultRate = 1.0f
        ),
        ExpressiveStyle(
            id = "news",
            label = "Berita Resmi",
            subtitle = "Artikulasi tegas & berwibawa",
            iconEmoji = "📰",
            promptInstruction = "Ucapkan dengan nada penyiar berita profesional, artikulasi jelas, tempo stabil, dan intonasi formal berwibawa.",
            defaultPitch = 0.95f,
            defaultRate = 1.05f
        ),
        ExpressiveStyle(
            id = "story",
            label = "Narasi Dongeng",
            subtitle = "Penuh emosi & imajinatif",
            iconEmoji = "📖",
            promptInstruction = "Ucapkan dengan gaya pendongeng cerita yang memikat, penuh penjiwaan, variasi nada emosional, dan jeda dramatis.",
            defaultPitch = 1.05f,
            defaultRate = 0.92f
        ),
        ExpressiveStyle(
            id = "motivational",
            label = "Motivasi & Semangat",
            subtitle = "Penuh energi & menginspirasi",
            iconEmoji = "🔥",
            promptInstruction = "Ucapkan dengan penuh antusiasme, nada bersemangat membara, tegas, dan membakar tekad pendengar.",
            defaultPitch = 1.1f,
            defaultRate = 1.1f
        ),
        ExpressiveStyle(
            id = "calm",
            label = "Tenang & Menenangkan",
            subtitle = "Lembut, pelan & damai",
            iconEmoji = "🌙",
            promptInstruction = "Ucapkan dengan suara sangat lembut, tempo perlahan, penuh ketenangan hati seperti pemandu meditasi atau pengantar tidur.",
            defaultPitch = 0.9f,
            defaultRate = 0.85f
        ),
        ExpressiveStyle(
            id = "casual",
            label = "Kasual & Obrolan",
            subtitle = "Santai khas percakapan harian",
            iconEmoji = "💬",
            promptInstruction = "Ucapkan dengan gaya santai kasual, tawa halus di sela kata, dan nada obrolan akrab sehari-hari khas Indonesia.",
            defaultPitch = 1.02f,
            defaultRate = 1.02f
        )
    )

    val defaultStyle = list[0] // Alami & Ramah
}
