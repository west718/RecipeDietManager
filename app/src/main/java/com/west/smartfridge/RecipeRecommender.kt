package com.west.smartfridge

// 菜谱数据类
data class Recipe(
    val name: String,
    val requiredIngredients: List<String>,
    val steps: List<String>
)

// 菜谱推荐器（本地贪心算法）
object RecipeRecommender {

    // 内置一个超级丰富的本地菜谱库（18道菜）
    val allRecipes = listOf(
        // --- 经典家常菜 ---
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
            name = "土豆炖牛肉",
            requiredIngredients = listOf("牛肉", "土豆", "胡萝卜"),
            steps = listOf("牛肉切块焯水，土豆、胡萝卜切块。", "热油炒香牛肉，加生抽、老抽。", "加水没过牛肉，大火烧开转小火炖1小时。", "加入土豆和胡萝卜，继续炖20分钟，大火收汁。")
        ),
        Recipe(
            name = "洋葱炒肉",
            requiredIngredients = listOf("猪肉", "洋葱"),
            steps = listOf("猪肉切片，洋葱切丝。", "热锅冷油，下肉片炒至变色。", "加入洋葱翻炒至变软。", "加盐、生抽调味出锅。")
        ),
        Recipe(
            name = "黄瓜炒鸡蛋",
            requiredIngredients = listOf("黄瓜", "鸡蛋"),
            steps = listOf("黄瓜切片，鸡蛋打散。", "热锅烧油，先将鸡蛋炒熟盛出。", "锅内留底油，下黄瓜片翻炒。", "加入炒好的鸡蛋，加盐出锅。")
        ),
        Recipe(
            name = "酸辣土豆丝",
            requiredIngredients = listOf("土豆", "干辣椒", "醋"),
            steps = listOf("土豆切细丝，泡水去淀粉。", "热油爆香干辣椒。", "下土豆丝大火快炒，加醋。", "加盐、鸡精翻炒均匀出锅。")
        ),
        Recipe(
            name = "红烧茄子",
            requiredIngredients = listOf("茄子", "大蒜", "生抽"),
            steps = listOf("茄子切滚刀块，撒盐腌制挤干水分。", "热油炸软茄子捞出。", "爆香蒜末，下茄子翻炒。", "倒入生抽、糖调成的料汁，收汁出锅。")
        ),
        Recipe(
            name = "蒜蓉炒青菜",
            requiredIngredients = listOf("青菜", "大蒜"),
            steps = listOf("青菜洗净，大蒜切末。", "热锅烧油，爆香蒜末。", "下青菜大火快炒，加盐出锅。")
        ),
        Recipe(
            name = "上汤菠菜",
            requiredIngredients = listOf("菠菜", "皮蛋", "大蒜"),
            steps = listOf("菠菜洗净焯水捞出。", "热油爆香蒜末，加入皮蛋丁翻炒。", "加入高汤或清水烧开。", "放入菠菜煮软，加盐调味。")
        ),

        // --- 鲜美海鲜与肉类 ---
        Recipe(
            name = "白灼虾",
            requiredIngredients = listOf("虾", "姜", "葱"),
            steps = listOf("鲜虾去虾线洗净。", "锅中加水，放姜片、葱段烧开。", "下入鲜虾煮至变红弯曲。", "捞出过凉水，搭配蘸料食用。")
        ),
        Recipe(
            name = "红烧排骨",
            requiredIngredients = listOf("排骨", "姜", "生抽"),
            steps = listOf("排骨焯水去血沫。", "热锅冷油，下冰糖炒出糖色。", "倒入排骨翻炒上色，加姜片、生抽。", "加水没过排骨，小火炖40分钟收汁。")
        ),
        Recipe(
            name = "清蒸鱼",
            requiredIngredients = listOf("鱼", "葱", "姜"),
            steps = listOf("鱼处理干净，两面划刀，铺上姜片。", "水开后上锅蒸8-10分钟。", "倒掉盘中汁水，铺上葱丝。", "淋上蒸鱼豉油，浇上热油激发出香味。")
        ),
        Recipe(
            name = "香菇炖鸡",
            requiredIngredients = listOf("蘑菇", "鸡肉", "姜"),
            steps = listOf("鸡肉切块焯水，香菇泡发。", "热油炒香姜片和鸡块。", "加入香菇和泡香菇的水，大火烧开。", "转小火炖40分钟，加盐调味。")
        ),
        Recipe(
            name = "西兰花炒虾仁",
            requiredIngredients = listOf("西兰花", "虾", "大蒜"),
            steps = listOf("西兰花切小朵焯水。", "虾去壳去虾线。", "热油爆香蒜末，下虾仁炒至变色。", "加入西兰花翻炒，加盐、蚝油出锅。")
        ),
        Recipe(
            name = "萝卜排骨汤",
            requiredIngredients = listOf("白萝卜", "排骨", "姜"),
            steps = listOf("排骨焯水，白萝卜切块。", "将排骨和姜片放入锅中加水烧开。", "转小火炖1小时。", "加入白萝卜继续炖20分钟，加盐、葱花出锅。")
        ),

        // --- 汤羹与甜品 ---
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
            name = "鸡蛋羹",
            requiredIngredients = listOf("鸡蛋", "温水", "盐"),
            steps = listOf("鸡蛋打散，加入1.5倍温水和少许盐搅匀。", "用漏网过滤掉气泡，倒入碗中。", "盖上盘子或保鲜膜，水开后蒸10分钟。", "出锅淋上生抽和香油即可。")
        ),
        Recipe(
            name = "牛奶燕麦粥",
            requiredIngredients = listOf("牛奶", "燕麦"),
            steps = listOf("将燕麦片放入锅中加水煮开。", "倒入牛奶，转小火慢煮。", "搅拌均匀，煮至浓稠即可。")
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