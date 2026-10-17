package com.west.smartfridge

// 菜谱数据类（增加了制作步骤字段）
data class Recipe(
    val name: String,
    val requiredIngredients: List<String>,
    val steps: List<String>
)

// 菜谱推荐器（本地贪心算法）
object RecipeRecommender {

    // 内置一个简单的本地菜谱库
    val allRecipes = listOf(
        Recipe(
            name = "西红柿炒鸡蛋",
            requiredIngredients = listOf("鸡蛋", "西红柿", "葱"),
            steps = listOf("鸡蛋打散，西红柿切块。", "热锅烧油，倒入鸡蛋炒熟盛出。", "底油炒西红柿出汁，加入鸡蛋翻炒。", "加盐、葱花出锅。")
        ),
        Recipe(
            name = "青椒肉丝",
            requiredIngredients = listOf("猪肉", "青椒", "姜"),
            steps = listOf("猪肉切丝腌制，青椒切丝。", "热油滑熟肉丝盛出。", "爆香姜丝，下青椒翻炒。", "倒入肉丝，加生抽、盐翻炒均匀。")
        ),
        Recipe(
            name = "紫菜蛋花汤",
            requiredIngredients = listOf("鸡蛋", "紫菜", "葱"),
            steps = listOf("水烧开，放入紫菜。", "鸡蛋打散，慢慢淋入锅中成蛋花。", "加盐、香油调味，撒上葱花出锅。")
        ),
        Recipe(
            name = "牛奶炖蛋",
            requiredIngredients = listOf("鸡蛋", "牛奶", "糖"),
            steps = listOf("鸡蛋打散，加入牛奶和糖搅匀。", "过筛去掉浮沫，盖上保鲜膜。", "水开后上锅蒸10-15分钟。")
        ),
        Recipe(
            name = "蒜蓉炒青菜",
            requiredIngredients = listOf("青菜", "大蒜"),
            steps = listOf("青菜洗净，大蒜切末。", "热锅烧油，爆香蒜末。", "下青菜大火快炒，加盐出锅。")
        ),
        Recipe(
            name = "土豆炖牛肉",
            requiredIngredients = listOf("牛肉", "土豆", "胡萝卜"),
            steps = listOf("牛肉切块焯水，土豆、胡萝卜切块。", "热油炒香牛肉，加生抽、老抽、八角。", "加水没过牛肉，大火烧开转小火炖1小时。", "加入土豆和胡萝卜，继续炖20分钟，大火收汁。")
        ),
        Recipe(
            name = "鸡蛋羹",
            requiredIngredients = listOf("鸡蛋", "温水", "盐"),
            steps = listOf("鸡蛋打散，加入1.5倍温水和少许盐搅匀。", "用漏网过滤掉气泡，倒入碗中。", "盖上盘子或保鲜膜，水开后蒸10分钟。", "出锅淋上生抽和香油即可。")
        )
    )

    /**
     * 核心算法：根据冰箱里的食材，推荐匹配度最高的菜谱。
     */
    fun recommendRecipes(fridgeIngredients: List<Ingredient>): List<Pair<Recipe, Int>> {
        val fridgeNames = fridgeIngredients.map { it.name }
        val result = mutableListOf<Pair<Recipe, Int>>()

        for (recipe in allRecipes) {
            val matchedCount = recipe.requiredIngredients.count { requiredName ->
                fridgeNames.any { it.contains(requiredName) || requiredName.contains(it) }
            }

            if (matchedCount > 0) {
                val matchScore = (matchedCount * 100) / recipe.requiredIngredients.size
                result.add(Pair(recipe, matchScore))
            }
        }

        return result.sortedByDescending { it.second }
    }
}