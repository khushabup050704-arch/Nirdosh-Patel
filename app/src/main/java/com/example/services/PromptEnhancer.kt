package com.example.services

import com.example.models.CameraMotion
import com.example.models.StyleOption

object PromptEnhancer {

    /**
     * Enhances a raw user prompt into an 11-attribute cinematic masterpiece prompt:
     * - Subject
     * - Environment
     * - Action
     * - Camera movement
     * - Camera angle
     * - Lighting
     * - Time of day
     * - Visual style
     * - Motion
     * - Atmosphere
     * - Quality
     */
    fun enhance(
        rawPrompt: String,
        style: StyleOption = StyleOption.CINEMATIC,
        camera: CameraMotion = CameraMotion.CINEMATIC
    ): String {
        val trimmed = rawPrompt.trim()
        if (trimmed.isEmpty()) {
            return "A majestic panoramic vista with breathtaking volumetric lighting, 8k ultra-detailed cinematic render, smooth camera motion."
        }

        val lower = trimmed.lowercase()

        // 1. Determine Subject & Action enhancement
        val hasCharacter = lower.contains("man") || lower.contains("woman") || lower.contains("boy") ||
                lower.contains("girl") || lower.contains("person") || lower.contains("character") ||
                lower.contains("animal") || lower.contains("cat") || lower.contains("dog") ||
                lower.contains("warrior") || lower.contains("samurai") || lower.contains("robot")

        val hasNature = lower.contains("mountain") || lower.contains("sea") || lower.contains("ocean") ||
                lower.contains("forest") || lower.contains("river") || lower.contains("sky") ||
                lower.contains("sunset") || lower.contains("clouds") || lower.contains("landscape")

        val hasSciFiOrCity = lower.contains("city") || lower.contains("street") || lower.contains("cyber") ||
                lower.contains("future") || lower.contains("space") || lower.contains("neon") ||
                lower.contains("spaceship") || lower.contains("cyberpunk")

        // 2. Select contextual lighting & atmosphere
        val lighting = when {
            hasSciFiOrCity -> "vibrant neon backlighting reflecting on wet surfaces, dramatic chiaroscuro, volumetric haze"
            hasNature -> "golden hour warm sunlight filtering through atmosphere, gentle god rays, soft specular highlights"
            hasCharacter -> "subtle rim lighting outlining silhouette, soft diffused key light, cinematic shallow depth of field"
            else -> "cinematic three-point studio lighting, atmospheric volumetric mist, rich contrast"
        }

        // 3. Time of day / Mood
        val timeAndMood = when {
            lower.contains("night") -> "midnight hour with mysterious luminescence"
            lower.contains("sunrise") || lower.contains("dawn") -> "early dawn breaking over the horizon with pastel gradient sky"
            lower.contains("sunset") -> "dramatic twilight golden hour with fiery amber tones"
            lower.contains("rain") -> "atmospheric rainfall with droplet reflections and moody overcast ambiance"
            else -> "evocative cinematic ambiance, immersive weather dynamics"
        }

        // 4. Camera angle & motion
        val cameraDetails = when (camera) {
            CameraMotion.DRONE -> "sweeping high-altitude aerial drone perspective, majestic wide-angle composition"
            CameraMotion.TRACKING -> "smooth fluid tracking camera keeping steady focus on foreground action"
            CameraMotion.PAN -> "wide panoramic horizontal camera pan revealing scale and depth"
            CameraMotion.ZOOM -> "slow cinematic dolly zoom accentuating emotional gravity"
            CameraMotion.HANDHELD -> "organic immersive handheld camera feel with natural subtle micro-vibrations"
            CameraMotion.STATIC -> "composed tripod frame, crisp foreground-to-background focus, symmetrical framing"
            CameraMotion.TILT -> "slow upward tilt camera angle emphasizing grandeur and verticality"
            CameraMotion.CINEMATIC -> "fluid 35mm anamorphic camera glide, dynamic leading lines, creamy bokeh"
        }

        // 5. Visual style descriptors
        val styleDetails = style.promptDescriptor

        // 6. Quality & motion fidelity
        val qualitySpecs = "photorealistic textures, 8k resolution, ray-traced shadows, hyper-detailed, masterpiece visual fidelity, 60fps fluid motion"

        // Construct cohesive, structured cinematic prompt
        val baseSubject = if (trimmed.endsWith(".")) trimmed.dropLast(1) else trimmed

        return "$baseSubject, $lighting, $timeAndMood, $cameraDetails, $styleDetails, $qualitySpecs"
    }

    /**
     * Curated sample prompts to inspire creators
     */
    val samplePrompts = listOf(
        "A cinematic drone shot flying over the Himalayas at sunrise, realistic clouds, golden sunlight, ultra detailed, smooth camera movement.",
        "A cybernetic samurai walking through a neon-lit Tokyo street in heavy rain, reflections on asphalt, high octane atmosphere.",
        "Cute red panda wearing an astronaut helmet exploring a bioluminescent crystal cave on an alien planet, 3D Pixar style.",
        "Macro timelapse of an enchanted lotus flower unfurling its petals as cosmic stardust drifts around it in deep space.",
        "An ancient mystical library with floating glowing spell books and stained glass windows casting colorful god rays.",
        "A sleek silver retro-futuristic sports car speeding across a desert highway toward a giant retro synthwave sunset.",
        "Photorealistic slow-motion splash of ocean waves colliding against rugged volcanic sea cliffs at golden twilight.",
        "A cozy warm coffee shop on a rainy autumn evening, steam rising from ceramic mug, vintage bokeh background."
    )
}
