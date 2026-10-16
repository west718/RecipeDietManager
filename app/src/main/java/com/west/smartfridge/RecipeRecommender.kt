package com.west.smartfridge

// 菜谱数据类
data class Recipe(
    val name: String,
    val requiredIngredients: List<String> // 只需要食材的名字
)

// 菜谱推荐器（本地贪心算法）
object RecipeRecommender {

    // 内置一个简单的本地菜谱库
    val allRecipes = listOf(
        Recipe("西红柿炒鸡蛋", listOf("鸡蛋", "西红柿", "葱")),
        Recipe("青椒肉丝", listOf("猪肉", "青椒", "姜")),
        Recipe("紫菜蛋花汤", listOf("鸡蛋", "紫菜", "葱")),
        Recipe("牛奶炖蛋", listOf("鸡蛋", "牛奶", "糖")),
        Recipe("蒜蓉炒青菜", listOf("青菜", "大蒜")),
        Recipe("土豆炖牛肉", listOf("牛肉", "土豆", "胡萝卜")),
        Recipe("鸡蛋羹", listOf("鸡蛋", "温水", "盐"))
    )

    /**
     * 核心算法：根据冰箱里的食材，推荐匹配度最高的菜谱。
     * 匹配度 = 已有的食材数量 / 菜谱需要的总食材数量
     */
    fun recommendRecipes(fridgeIngredients: List<Ingredient>): List<Pair<Recipe, Int>> {
        // 把冰箱里的食材名字提取出来，方便匹配
        val fridgeNames = fridgeIngredients.map { it.name }

        val result = mutableListOf<Pair<Recipe, Int>>()

        for (recipe in allRecipes) {
            // 计算这道菜里，有多少种食材我们已经有了
            val matchedCount = recipe.requiredIngredients.count { requiredName ->
                fridgeNames.any { it.contains(requiredName) || requiredName.contains(it) }
            }

            // 如果至少匹配了 1 种食材，就加入推荐列表
            if (matchedCount > 0) {
                // 计算匹配百分比（匹配数 / 总需求数 * 100）
                val matchScore = (matchedCount * 100) / recipe.requiredIngredients.size
                result.add(Pair(recipe, matchScore))
            }
        }

        // 按匹配度从高到低排序
        return result.sortedByDescending { it.second }
    }
}