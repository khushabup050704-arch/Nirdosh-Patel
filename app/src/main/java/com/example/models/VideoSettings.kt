package com.example.models

enum class AspectRatio(val label: String, val ratioValue: Float, val iconDescription: String) {
    RATIO_16_9("16:9", 16f / 9f, "Landscape / YouTube"),
    RATIO_9_16("9:16", 9f / 16f, "Portrait / Shorts / TikTok"),
    RATIO_1_1("1:1", 1f, "Square / Feed"),
    RATIO_4_5("4:5", 4f / 5f, "Social / Portrait")
}

enum class DurationOption(val label: String, val seconds: Int, val creditsRequired: Int) {
    SEC_30("30 seconds", 30, 2),
    MIN_1("1 minute", 60, 4),
    MIN_3("3 minutes", 180, 8),
    MIN_5("5 minutes", 300, 12)
}

enum class QualityOption(val label: String, val resolutionString: String) {
    Q_480P("480p", "854x480"),
    Q_720P("720p", "1280x720"),
    Q_1080P("1080p", "1920x1080")
}

enum class StyleOption(val label: String, val promptDescriptor: String) {
    CINEMATIC("Cinematic", "hyper-cinematic 8k footage, arri alexa, film grain, dramatic lighting"),
    REALISTIC("Realistic", "photorealistic 4k video, lifelike textures, natural lighting, documentary style"),
    ANIME("Anime", "masterpiece anime aesthetic, Studio Ghibli inspired, vibrant cel shading"),
    ANIMATION_3D("3D Animation", "Pixar 3D animated style, raytracing, soft lighting, vibrant depth"),
    CARTOON("Cartoon", "stylized 2D animation, hand-drawn aesthetic, expressive outlines"),
    FANTASY("Fantasy", "ethereal high fantasy, magical particles, mystical glow, epic atmosphere"),
    SCI_FI("Sci-Fi", "futuristic cyberpunk sci-fi, neon glows, holographic elements, tech details"),
    DOCUMENTARY("Documentary", "National Geographic documentary quality, authentic atmosphere, telephoto lens"),
    HORROR("Horror", "dark eerie horror cinematography, haunting volumetric shadows, suspenseful tension"),
    HISTORICAL("Historical", "vintage historical film look, period-accurate textures, sepia undertones")
}

enum class CameraMotion(val label: String, val promptDescriptor: String) {
    CINEMATIC("Cinematic", "smooth cinematic camera movement with shallow depth of field"),
    STATIC("Static", "stable tripod locked shot, crisp focus"),
    PAN("Pan", "smooth horizontal panning camera movement"),
    TILT("Tilt", "vertical tilt camera shot moving seamlessly"),
    ZOOM("Zoom", "subtle slow dolly zoom effect"),
    TRACKING("Tracking", "dynamic tracking camera following the subject"),
    DRONE("Drone", "sweeping aerial drone camera shot with grand perspective"),
    HANDHELD("Handheld", "immersive organic handheld camera motion")
}

enum class FpsOption(val fpsValue: Int, val label: String) {
    FPS_24(24, "24 FPS (Cinematic)"),
    FPS_30(30, "30 FPS (Standard)"),
    FPS_60(60, "60 FPS (Ultra Smooth)")
}

enum class GenerationMode {
    TEXT_TO_VIDEO,
    IMAGE_TO_VIDEO
}
