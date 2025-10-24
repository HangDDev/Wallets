package com.example.financemanager.model

enum class Category {
    MEAL, TRANSPORT, DRINKS, SNACKS, SHOPPING, ENTERTAINMENT, SALARY, REWARDS, OTHERS
}

fun getCategoryDisplayName(category: Category): String {
    return when (category) {
        Category.MEAL -> "Meal"
        Category.TRANSPORT -> "Transport"
        Category.DRINKS -> "Drinks"
        Category.SNACKS -> "Snacks"
        Category.SHOPPING -> "Shopping"
        Category.ENTERTAINMENT -> "Entertainment"
        Category.SALARY -> "Salary"
        Category.REWARDS -> "Rewards"
        Category.OTHERS -> "Others"
    }
}

