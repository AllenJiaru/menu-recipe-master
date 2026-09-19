/**
 * 菜谱数据模型
 */
export class Recipe {
  id: number = 0
  name: string = ''
  type: number = 0
  categoryId: number = 0
  description: string = ''
  coverImage: string = ''
  imagePath: string = ''
  cookingTime: number = 0
  difficulty: number = 1
  isFavorite: number = 0
  orderCount: number = 0
  status: number = 1
  createTime: number = 0
  updateTime: number = 0
  materials: RecipeMaterial[] = []
  steps: RecipeStep[] = []

  /**
   * 获取分类ID（兼容 categoryId 和 type）
   * 后端分类ID为 11-20，App 内部类型为 1-10，此处统一转换
   */
  getCategoryId(): number {
    if (this.categoryId >= 11 && this.categoryId <= 20) return this.categoryId - 10
    if (this.categoryId > 0) return this.categoryId
    return this.type
  }

  /**
   * 获取封面图片路径（兼容 coverImage 和 imagePath）
   */
  getCoverImage(): string {
    if (this.coverImage) return this.coverImage
    return this.imagePath
  }

  /**
   * 时间解析（兼容 ISO 字符串与毫秒时间戳）
   */
  static parseTime(value: number | string | null | undefined): number {
    if (value === null || value === undefined || value === '') return 0
    if (typeof value === 'number') return value
    const parsed = Date.parse(value as string)
    return isNaN(parsed) ? 0 : parsed
  }

  /**
   * 从后端返回的普通对象构造 Recipe 实例
   */
  static fromApi(raw: object): Recipe {
    const recipe = new Recipe()
    const data = raw as Record<string, Object>
    recipe.id = (data['id'] as number) || 0
    recipe.name = (data['name'] as string) || ''
    recipe.type = (data['type'] as number) || 0
    recipe.categoryId = (data['categoryId'] as number) || 0
    recipe.description = (data['description'] as string) || ''
    recipe.coverImage = (data['coverImage'] as string) || ''
    recipe.imagePath = (data['imagePath'] as string) || ''
    recipe.cookingTime = (data['cookingTime'] as number) || 0
    recipe.difficulty = (data['difficulty'] as number) || 1
    recipe.isFavorite = (data['isFavorite'] as number) || 0
    recipe.orderCount = (data['orderCount'] as number) || 0
    recipe.status = data['status'] !== undefined ? (data['status'] as number) : 1
    recipe.createTime = Recipe.parseTime(data['createTime'] as string)
    recipe.updateTime = Recipe.parseTime(data['updateTime'] as string)
    const materials = data['materials'] as object[]
    if (materials && materials.length > 0) {
      recipe.materials = materials.map((m: object) => {
        const material = new RecipeMaterial()
        const md = m as Record<string, Object>
        material.id = (md['id'] as number) || 0
        material.recipeId = (md['recipeId'] as number) || 0
        material.name = (md['name'] as string) || ''
        material.amount = (md['amount'] as string) || ''
        material.unit = (md['unit'] as string) || ''
        return material
      })
    }
    const steps = data['steps'] as object[]
    if (steps && steps.length > 0) {
      recipe.steps = steps.map((s: object) => {
        const step = new RecipeStep()
        const sd = s as Record<string, Object>
        step.id = (sd['id'] as number) || 0
        step.recipeId = (sd['recipeId'] as number) || 0
        step.stepNumber = (sd['stepNumber'] as number) || 0
        step.description = (sd['description'] as string) || ''
        step.imagePath = (sd['imageUrl'] as string) || ''
        return step
      })
    }
    return recipe
  }
}

/**
 * 食材数据模型
 */
export class RecipeMaterial {
  id: number = 0
  recipeId: number = 0
  name: string = ''
  amount: string = ''
  unit: string = ''
}

/**
 * 步骤数据模型
 */
export class RecipeStep {
  id: number = 0
  recipeId: number = 0
  stepNumber: number = 0
  description: string = ''
  imagePath: string = ''
}
