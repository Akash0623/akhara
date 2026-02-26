package com.akhara.data

enum class MuscleGroup(val displayName: String) {
    CHEST("Chest"),
    BACK("Back"),
    SHOULDERS("Shoulders"),
    BICEPS("Biceps"),
    TRICEPS("Triceps"),
    QUADS("Quads"),
    HAMSTRINGS("Hamstrings"),
    GLUTES("Glutes"),
    ABS("Abs"),
    CALVES("Calves"),
    FOREARMS("Forearms"),
    CARDIO("Cardio");

    companion object {
        fun fromDisplayName(name: String): MuscleGroup? =
            entries.find { it.displayName.equals(name, ignoreCase = true) }
    }
}
