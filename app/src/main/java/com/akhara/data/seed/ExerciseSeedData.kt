package com.akhara.data.seed

import com.akhara.data.MuscleGroup
import com.akhara.data.db.entity.Exercise

object ExerciseSeedData {

    val exercises: List<Exercise> = buildList {
        // Chest
        chest("Barbell Bench Press")
        chest("Incline Barbell Bench Press")
        chest("Dumbbell Bench Press")
        chest("Incline Dumbbell Press")
        chest("Dumbbell Flyes")
        chest("Cable Crossover")
        chest("Push-Ups")
        chest("Chest Dips")
        chest("Pec Deck Machine")
        chest("Decline Bench Press")

        // Back
        back("Deadlift")
        back("Pull-Ups")
        back("Chin-Ups")
        back("Barbell Row")
        back("Dumbbell Row")
        back("Lat Pulldown")
        back("Seated Cable Row")
        back("T-Bar Row")
        back("Face Pulls")
        back("Straight Arm Pulldown")

        // Shoulders
        shoulders("Overhead Press")
        shoulders("Dumbbell Shoulder Press")
        shoulders("Lateral Raises")
        shoulders("Front Raises")
        shoulders("Reverse Flyes")
        shoulders("Arnold Press")
        shoulders("Barbell Shrugs")
        shoulders("Dumbbell Shrugs")
        shoulders("Cable Lateral Raises")
        shoulders("Upright Row")

        // Biceps
        biceps("Barbell Curl")
        biceps("Dumbbell Curl")
        biceps("Hammer Curl")
        biceps("Preacher Curl")
        biceps("Concentration Curl")
        biceps("Cable Curl")
        biceps("Incline Dumbbell Curl")
        biceps("EZ-Bar Curl")

        // Triceps
        triceps("Tricep Pushdown")
        triceps("Overhead Tricep Extension")
        triceps("Skull Crushers")
        triceps("Close-Grip Bench Press")
        triceps("Tricep Dips")
        triceps("Cable Kickback")
        triceps("Diamond Push-Ups")

        // Quads
        quads("Barbell Squat")
        quads("Front Squat")
        quads("Leg Press")
        quads("Lunges")
        quads("Leg Extension")
        quads("Bulgarian Split Squat")
        quads("Hack Squat")
        quads("Goblet Squat")
        quads("Walking Lunges")

        // Hamstrings
        hamstrings("Romanian Deadlift")
        hamstrings("Lying Leg Curl")
        hamstrings("Seated Leg Curl")
        hamstrings("Good Mornings")
        hamstrings("Nordic Curl")
        hamstrings("Stiff-Leg Deadlift")

        // Glutes
        glutes("Hip Thrust")
        glutes("Glute Bridge")
        glutes("Cable Kickback")
        glutes("Sumo Deadlift")
        glutes("Step-Ups")
        glutes("Glute Ham Raise")

        // Abs
        abs("Crunches")
        abs("Hanging Leg Raises")
        abs("Planks")
        abs("Cable Crunch")
        abs("Ab Wheel Rollout")
        abs("Russian Twists")
        abs("Mountain Climbers")
        abs("Bicycle Crunches")

        // Calves
        calves("Standing Calf Raise")
        calves("Seated Calf Raise")
        calves("Donkey Calf Raise")
        calves("Single-Leg Calf Raise")

        // Forearms
        forearms("Wrist Curl")
        forearms("Reverse Wrist Curl")
        forearms("Farmer's Walk")
        forearms("Plate Pinch Hold")

        // Cardio
        cardio("Running")
        cardio("Cycling")
        cardio("Jump Rope")
        cardio("Rowing Machine")
        cardio("Stair Climber")
        cardio("Elliptical")
        cardio("Swimming")
    }

    private fun MutableList<Exercise>.chest(name: String) =
        add(Exercise(name = name, muscleGroup = MuscleGroup.CHEST.displayName))

    private fun MutableList<Exercise>.back(name: String) =
        add(Exercise(name = name, muscleGroup = MuscleGroup.BACK.displayName))

    private fun MutableList<Exercise>.shoulders(name: String) =
        add(Exercise(name = name, muscleGroup = MuscleGroup.SHOULDERS.displayName))

    private fun MutableList<Exercise>.biceps(name: String) =
        add(Exercise(name = name, muscleGroup = MuscleGroup.BICEPS.displayName))

    private fun MutableList<Exercise>.triceps(name: String) =
        add(Exercise(name = name, muscleGroup = MuscleGroup.TRICEPS.displayName))

    private fun MutableList<Exercise>.quads(name: String) =
        add(Exercise(name = name, muscleGroup = MuscleGroup.QUADS.displayName))

    private fun MutableList<Exercise>.hamstrings(name: String) =
        add(Exercise(name = name, muscleGroup = MuscleGroup.HAMSTRINGS.displayName))

    private fun MutableList<Exercise>.glutes(name: String) =
        add(Exercise(name = name, muscleGroup = MuscleGroup.GLUTES.displayName))

    private fun MutableList<Exercise>.abs(name: String) =
        add(Exercise(name = name, muscleGroup = MuscleGroup.ABS.displayName))

    private fun MutableList<Exercise>.calves(name: String) =
        add(Exercise(name = name, muscleGroup = MuscleGroup.CALVES.displayName))

    private fun MutableList<Exercise>.forearms(name: String) =
        add(Exercise(name = name, muscleGroup = MuscleGroup.FOREARMS.displayName))

    private fun MutableList<Exercise>.cardio(name: String) =
        add(Exercise(name = name, muscleGroup = MuscleGroup.CARDIO.displayName))
}
