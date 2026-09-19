/**
 * RDB数据库工具类
 * 封装鸿蒙关系型数据库操作
 */
import relationalStore from '@ohos.data.relationalStore'
import { CommonConstant } from './CommonConstant'
import { LogUtil } from './LogUtil'
import { Recipe, RecipeMaterial, RecipeStep } from '../model/Recipe'
import { Order } from '../model/Order'
import { GalleryImage } from '../model/GalleryImage'

export class RdbUtil {
  private static rdbStore: relationalStore.RdbStore | null = null
  private static readonly TAG: string = 'RdbUtil'

  static async init(context: Context): Promise<void> {
    try {
      const config: relationalStore.StoreConfig = {
        name: CommonConstant.DB_NAME,
        securityLevel: relationalStore.SecurityLevel.S1
      }
      RdbUtil.rdbStore = await relationalStore.getRdbStore(context, config)
      LogUtil.info(RdbUtil.TAG, '数据库初始化成功')
      await RdbUtil.createTables()
    } catch (err) {
      LogUtil.error(RdbUtil.TAG, `数据库初始化失败: ${err}`)
      throw new Error(`数据库初始化失败: ${err}`)
    }
  }

  private static async createTables(): Promise<void> {
    if (!RdbUtil.rdbStore) return

    await RdbUtil.rdbStore.executeSql(`
      CREATE TABLE IF NOT EXISTS couple_config (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        space_name TEXT NOT NULL,
        chef_name TEXT NOT NULL,
        diner_name TEXT NOT NULL,
        create_time INTEGER NOT NULL
      )`)
    await RdbUtil.rdbStore.executeSql(`
      CREATE TABLE IF NOT EXISTS recipe (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT NOT NULL,
        type INTEGER NOT NULL,
        description TEXT,
        image_path TEXT,
        cooking_time INTEGER,
        difficulty INTEGER DEFAULT 1,
        is_favorite INTEGER DEFAULT 0,
        create_time INTEGER NOT NULL,
        update_time INTEGER NOT NULL
      )`)
    await RdbUtil.rdbStore.executeSql(`
      CREATE TABLE IF NOT EXISTS recipe_material (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        recipe_id INTEGER NOT NULL,
        name TEXT NOT NULL,
        amount TEXT,
        unit TEXT,
        FOREIGN KEY (recipe_id) REFERENCES recipe(id) ON DELETE CASCADE
      )`)
    await RdbUtil.rdbStore.executeSql(`
      CREATE TABLE IF NOT EXISTS recipe_step (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        recipe_id INTEGER NOT NULL,
        step_number INTEGER NOT NULL,
        description TEXT NOT NULL,
        image_path TEXT,
        FOREIGN KEY (recipe_id) REFERENCES recipe(id) ON DELETE CASCADE
      )`)
    await RdbUtil.rdbStore.executeSql(`
      CREATE TABLE IF NOT EXISTS order_record (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        recipe_id INTEGER NOT NULL,
        recipe_name TEXT NOT NULL,
        recipe_image TEXT,
        status INTEGER DEFAULT 0,
        remark TEXT,
        order_time INTEGER NOT NULL,
        accept_time INTEGER,
        complete_time INTEGER,
        FOREIGN KEY (recipe_id) REFERENCES recipe(id) ON DELETE CASCADE
      )`)
    await RdbUtil.rdbStore.executeSql(`
      CREATE TABLE IF NOT EXISTS gallery_img (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        image_path TEXT NOT NULL,
        description TEXT,
        order_id INTEGER,
        create_time INTEGER NOT NULL,
        FOREIGN KEY (order_id) REFERENCES order_record(id) ON DELETE SET NULL
      )`)
    LogUtil.info(RdbUtil.TAG, '数据表创建成功')
  }

  static async insert(tableName: string, values: relationalStore.ValuesBucket): Promise<number> {
    if (!RdbUtil.rdbStore) return -1
    try {
      return await RdbUtil.rdbStore.insert(tableName, values)
    } catch (err) {
      LogUtil.error(RdbUtil.TAG, `插入数据失败: ${err}`)
      return -1
    }
  }

  static async update(tableName: string, values: relationalStore.ValuesBucket,
    predicates: relationalStore.RdbPredicates): Promise<number> {
    if (!RdbUtil.rdbStore) return 0
    try {
      return await RdbUtil.rdbStore.update(values, predicates)
    } catch (err) {
      LogUtil.error(RdbUtil.TAG, `更新数据失败: ${err}`)
      return 0
    }
  }

  static async delete(predicates: relationalStore.RdbPredicates): Promise<number> {
    if (!RdbUtil.rdbStore) return 0
    try {
      return await RdbUtil.rdbStore.delete(predicates)
    } catch (err) {
      LogUtil.error(RdbUtil.TAG, `删除数据失败: ${err}`)
      return 0
    }
  }

  static async query(predicates: relationalStore.RdbPredicates,
    columns?: Array<string>): Promise<relationalStore.ResultSet> {
    if (!RdbUtil.rdbStore) throw new Error('数据库未初始化')
    return await RdbUtil.rdbStore.query(predicates, columns)
  }

  static async executeSql(sql: string): Promise<void> {
    if (!RdbUtil.rdbStore) return
    await RdbUtil.rdbStore.executeSql(sql)
  }

  static getRdbStore(): relationalStore.RdbStore | null {
    return RdbUtil.rdbStore
  }

  // ======================== Recipe CRUD ========================

  private static parseRecipe(resultSet: relationalStore.ResultSet): Recipe {
    const recipe = new Recipe()
    recipe.id = resultSet.getLong(resultSet.getColumnIndex('id'))
    recipe.name = resultSet.getString(resultSet.getColumnIndex('name'))
    recipe.type = resultSet.getLong(resultSet.getColumnIndex('type'))
    recipe.description = resultSet.getString(resultSet.getColumnIndex('description'))
    recipe.imagePath = resultSet.getString(resultSet.getColumnIndex('image_path'))
    recipe.cookingTime = resultSet.getLong(resultSet.getColumnIndex('cooking_time'))
    recipe.difficulty = resultSet.getLong(resultSet.getColumnIndex('difficulty'))
    recipe.isFavorite = resultSet.getLong(resultSet.getColumnIndex('is_favorite'))
    recipe.createTime = resultSet.getLong(resultSet.getColumnIndex('create_time'))
    recipe.updateTime = resultSet.getLong(resultSet.getColumnIndex('update_time'))
    return recipe
  }

  static async insertRecipe(recipe: Recipe): Promise<number> {
    if (!RdbUtil.rdbStore) return -1
    try {
      const recipeId = await RdbUtil.rdbStore.insert('recipe', {
        'name': recipe.name, 'type': recipe.type, 'description': recipe.description,
        'image_path': recipe.imagePath, 'cooking_time': recipe.cookingTime,
        'difficulty': recipe.difficulty, 'is_favorite': recipe.isFavorite,
        'create_time': recipe.createTime, 'update_time': recipe.updateTime
      })
      for (const m of recipe.materials) {
        await RdbUtil.rdbStore.insert('recipe_material', {
          'recipe_id': recipeId, 'name': m.name, 'amount': m.amount, 'unit': m.unit
        })
      }
      for (const s of recipe.steps) {
        await RdbUtil.rdbStore.insert('recipe_step', {
          'recipe_id': recipeId, 'step_number': s.stepNumber,
          'description': s.description, 'image_path': s.imagePath
        })
      }
      return recipeId
    } catch (err) {
      LogUtil.error(RdbUtil.TAG, `插入菜谱失败: ${err}`)
      return -1
    }
  }

  static async updateRecipe(recipe: Recipe): Promise<boolean> {
    if (!RdbUtil.rdbStore) return false
    try {
      const predicates = new relationalStore.RdbPredicates('recipe')
      predicates.equalTo('id', recipe.id)
      const rows = await RdbUtil.rdbStore.update({
        'name': recipe.name, 'type': recipe.type, 'description': recipe.description,
        'image_path': recipe.imagePath, 'cooking_time': recipe.cookingTime,
        'difficulty': recipe.difficulty, 'is_favorite': recipe.isFavorite,
        'update_time': Date.now()
      }, predicates)
      // 更新食材：先删后插
      await RdbUtil.deleteMaterialsByRecipeId(recipe.id)
      for (const m of recipe.materials) {
        await RdbUtil.rdbStore.insert('recipe_material', {
          'recipe_id': recipe.id, 'name': m.name, 'amount': m.amount, 'unit': m.unit
        })
      }
      // 更新步骤：先删后插
      await RdbUtil.deleteStepsByRecipeId(recipe.id)
      for (const s of recipe.steps) {
        await RdbUtil.rdbStore.insert('recipe_step', {
          'recipe_id': recipe.id, 'step_number': s.stepNumber,
          'description': s.description, 'image_path': s.imagePath
        })
      }
      return rows > 0
    } catch (err) {
      LogUtil.error(RdbUtil.TAG, `更新菜谱失败: ${err}`)
      return false
    }
  }

  static async deleteRecipe(recipeId: number): Promise<boolean> {
    if (!RdbUtil.rdbStore) return false
    try {
      // 先删关联数据
      await RdbUtil.deleteMaterialsByRecipeId(recipeId)
      await RdbUtil.deleteStepsByRecipeId(recipeId)
      // 再删菜谱
      const predicates = new relationalStore.RdbPredicates('recipe')
      predicates.equalTo('id', recipeId)
      await RdbUtil.rdbStore.delete(predicates)
      return true
    } catch (err) {
      LogUtil.error(RdbUtil.TAG, `删除菜谱失败: ${err}`)
      return false
    }
  }

  static async queryRecipeById(recipeId: number): Promise<Recipe | null> {
    if (!RdbUtil.rdbStore) return null
    try {
      const predicates = new relationalStore.RdbPredicates('recipe')
      predicates.equalTo('id', recipeId)
      const resultSet = await RdbUtil.rdbStore.query(predicates)
      let recipe: Recipe | null = null
      if (resultSet.goToFirstRow()) {
        recipe = RdbUtil.parseRecipe(resultSet)
        recipe.materials = await RdbUtil.queryMaterialsByRecipeId(recipeId)
        recipe.steps = await RdbUtil.queryStepsByRecipeId(recipeId)
      }
      resultSet.close()
      return recipe
    } catch (err) {
      LogUtil.error(RdbUtil.TAG, `查询菜谱详情失败: ${err}`)
      return null
    }
  }

  static async queryAllRecipes(): Promise<Recipe[]> {
    if (!RdbUtil.rdbStore) return []
    try {
      const predicates = new relationalStore.RdbPredicates('recipe')
      predicates.orderByDesc('create_time')
      const resultSet = await RdbUtil.rdbStore.query(predicates)
      const recipes: Recipe[] = []
      while (resultSet.goToNextRow()) {
        recipes.push(RdbUtil.parseRecipe(resultSet))
      }
      resultSet.close()
      return recipes
    } catch (err) {
      return []
    }
  }

  static async queryRecipesByType(type: number): Promise<Recipe[]> {
    if (!RdbUtil.rdbStore) return []
    try {
      const predicates = new relationalStore.RdbPredicates('recipe')
      predicates.equalTo('type', type)
      predicates.orderByDesc('create_time')
      const resultSet = await RdbUtil.rdbStore.query(predicates)
      const recipes: Recipe[] = []
      while (resultSet.goToNextRow()) {
        recipes.push(RdbUtil.parseRecipe(resultSet))
      }
      resultSet.close()
      return recipes
    } catch (err) {
      return []
    }
  }

  static async queryFavoriteRecipes(): Promise<Recipe[]> {
    if (!RdbUtil.rdbStore) return []
    try {
      const predicates = new relationalStore.RdbPredicates('recipe')
      predicates.equalTo('is_favorite', 1)
      predicates.orderByDesc('create_time')
      const resultSet = await RdbUtil.rdbStore.query(predicates)
      const recipes: Recipe[] = []
      while (resultSet.goToNextRow()) {
        recipes.push(RdbUtil.parseRecipe(resultSet))
      }
      resultSet.close()
      return recipes
    } catch (err) {
      return []
    }
  }

  static async queryRandomRecipe(): Promise<Recipe | null> {
    if (!RdbUtil.rdbStore) return null
    try {
      const predicates = new relationalStore.RdbPredicates('recipe')
      predicates.orderByAsc('RANDOM()')
      predicates.limit(1)
      const resultSet = await RdbUtil.rdbStore.query(predicates)
      let recipe: Recipe | null = null
      if (resultSet.goToFirstRow()) {
        recipe = RdbUtil.parseRecipe(resultSet)
        recipe.materials = await RdbUtil.queryMaterialsByRecipeId(recipe.id)
        recipe.steps = await RdbUtil.queryStepsByRecipeId(recipe.id)
      }
      resultSet.close()
      return recipe
    } catch (err) {
      return null
    }
  }

  static async toggleFavorite(recipeId: number): Promise<boolean> {
    if (!RdbUtil.rdbStore) return false
    try {
      const predicates = new relationalStore.RdbPredicates('recipe')
      predicates.equalTo('id', recipeId)
      const rs = await RdbUtil.rdbStore.query(predicates)
      if (!rs.goToFirstRow()) { rs.close(); return false }
      const current = rs.getLong(rs.getColumnIndex('is_favorite'))
      rs.close()
      const newVal = current === 1 ? 0 : 1
      await RdbUtil.rdbStore.update({ 'is_favorite': newVal, 'update_time': Date.now() }, predicates)
      return newVal === 1
    } catch (err) {
      return false
    }
  }

  // ======================== Materials & Steps ========================

  static async queryMaterialsByRecipeId(recipeId: number): Promise<RecipeMaterial[]> {
    if (!RdbUtil.rdbStore) return []
    try {
      const predicates = new relationalStore.RdbPredicates('recipe_material')
      predicates.equalTo('recipe_id', recipeId)
      const resultSet = await RdbUtil.rdbStore.query(predicates)
      const materials: RecipeMaterial[] = []
      while (resultSet.goToNextRow()) {
        const m = new RecipeMaterial()
        m.id = resultSet.getLong(resultSet.getColumnIndex('id'))
        m.recipeId = resultSet.getLong(resultSet.getColumnIndex('recipe_id'))
        m.name = resultSet.getString(resultSet.getColumnIndex('name'))
        m.amount = resultSet.getString(resultSet.getColumnIndex('amount'))
        m.unit = resultSet.getString(resultSet.getColumnIndex('unit'))
        materials.push(m)
      }
      resultSet.close()
      return materials
    } catch (err) {
      return []
    }
  }

  static async queryStepsByRecipeId(recipeId: number): Promise<RecipeStep[]> {
    if (!RdbUtil.rdbStore) return []
    try {
      const predicates = new relationalStore.RdbPredicates('recipe_step')
      predicates.equalTo('recipe_id', recipeId)
      predicates.orderByAsc('step_number')
      const resultSet = await RdbUtil.rdbStore.query(predicates)
      const steps: RecipeStep[] = []
      while (resultSet.goToNextRow()) {
        const s = new RecipeStep()
        s.id = resultSet.getLong(resultSet.getColumnIndex('id'))
        s.recipeId = resultSet.getLong(resultSet.getColumnIndex('recipe_id'))
        s.stepNumber = resultSet.getLong(resultSet.getColumnIndex('step_number'))
        s.description = resultSet.getString(resultSet.getColumnIndex('description'))
        s.imagePath = resultSet.getString(resultSet.getColumnIndex('image_path'))
        steps.push(s)
      }
      resultSet.close()
      return steps
    } catch (err) {
      return []
    }
  }

  static async deleteMaterialsByRecipeId(recipeId: number): Promise<void> {
    if (!RdbUtil.rdbStore) return
    const p = new relationalStore.RdbPredicates('recipe_material')
    p.equalTo('recipe_id', recipeId)
    await RdbUtil.rdbStore.delete(p)
  }

  static async deleteStepsByRecipeId(recipeId: number): Promise<void> {
    if (!RdbUtil.rdbStore) return
    const p = new relationalStore.RdbPredicates('recipe_step')
    p.equalTo('recipe_id', recipeId)
    await RdbUtil.rdbStore.delete(p)
  }

  // ======================== Order CRUD ========================

  static async insertOrder(order: Order): Promise<number> {
    if (!RdbUtil.rdbStore) return -1
    try {
      return await RdbUtil.rdbStore.insert('order_record', {
        'recipe_id': order.recipeId, 'recipe_name': order.recipeName,
        'recipe_image': order.recipeImage, 'status': order.status,
        'remark': order.remark, 'order_time': order.orderTime
      })
    } catch (err) {
      LogUtil.error(RdbUtil.TAG, `插入订单失败: ${err}`)
      return -1
    }
  }

  static async queryAllOrders(): Promise<Order[]> {
    if (!RdbUtil.rdbStore) return []
    try {
      const predicates = new relationalStore.RdbPredicates('order_record')
      predicates.orderByDesc('order_time')
      const resultSet = await RdbUtil.rdbStore.query(predicates)
      const orders: Order[] = []
      while (resultSet.goToNextRow()) {
        orders.push(RdbUtil.parseOrder(resultSet))
      }
      resultSet.close()
      return orders
    } catch (err) {
      return []
    }
  }

  static async queryOrderById(orderId: number): Promise<Order | null> {
    if (!RdbUtil.rdbStore) return null
    try {
      const predicates = new relationalStore.RdbPredicates('order_record')
      predicates.equalTo('id', orderId)
      const resultSet = await RdbUtil.rdbStore.query(predicates)
      let order: Order | null = null
      if (resultSet.goToFirstRow()) {
        order = RdbUtil.parseOrder(resultSet)
      }
      resultSet.close()
      return order
    } catch (err) {
      return null
    }
  }

  static async updateOrderStatus(orderId: number, status: number): Promise<boolean> {
    if (!RdbUtil.rdbStore) return false
    try {
      const predicates = new relationalStore.RdbPredicates('order_record')
      predicates.equalTo('id', orderId)
      const values: relationalStore.ValuesBucket = { 'status': status }
      if (status === CommonConstant.ORDER_STATUS_ACCEPTED) {
        values['accept_time'] = Date.now()
      } else if (status === CommonConstant.ORDER_STATUS_COMPLETED) {
        values['complete_time'] = Date.now()
      }
      const rows = await RdbUtil.rdbStore.update(values, predicates)
      return rows > 0
    } catch (err) {
      LogUtil.error(RdbUtil.TAG, `更新订单状态失败: ${err}`)
      return false
    }
  }

  static async deleteOrder(orderId: number): Promise<boolean> {
    if (!RdbUtil.rdbStore) return false
    try {
      const predicates = new relationalStore.RdbPredicates('order_record')
      predicates.equalTo('id', orderId)
      await RdbUtil.rdbStore.delete(predicates)
      return true
    } catch (err) {
      return false
    }
  }

  // ======================== Batch Order Operations ========================

  static async batchUpdateOrderStatus(orderIds: number[], status: number): Promise<number> {
    if (!RdbUtil.rdbStore || orderIds.length === 0) return 0
    let successCount = 0
    try {
      for (const orderId of orderIds) {
        const predicates = new relationalStore.RdbPredicates('order_record')
        predicates.equalTo('id', orderId)
        const values: relationalStore.ValuesBucket = { 'status': status }
        if (status === CommonConstant.ORDER_STATUS_ACCEPTED) {
          values['accept_time'] = Date.now()
        } else if (status === CommonConstant.ORDER_STATUS_COMPLETED) {
          values['complete_time'] = Date.now()
        }
        const rows = await RdbUtil.rdbStore.update(values, predicates)
        if (rows > 0) successCount++
      }
      LogUtil.info(RdbUtil.TAG, `批量更新订单状态成功: ${successCount}/${orderIds.length}`)
      return successCount
    } catch (err) {
      LogUtil.error(RdbUtil.TAG, `批量更新订单状态失败: ${err}`)
      return successCount
    }
  }

  static async batchDeleteOrders(orderIds: number[]): Promise<number> {
    if (!RdbUtil.rdbStore || orderIds.length === 0) return 0
    let successCount = 0
    try {
      for (const orderId of orderIds) {
        const predicates = new relationalStore.RdbPredicates('order_record')
        predicates.equalTo('id', orderId)
        await RdbUtil.rdbStore.delete(predicates)
        successCount++
      }
      LogUtil.info(RdbUtil.TAG, `批量删除订单成功: ${successCount}/${orderIds.length}`)
      return successCount
    } catch (err) {
      LogUtil.error(RdbUtil.TAG, `批量删除订单失败: ${err}`)
      return successCount
    }
  }

  private static parseOrder(resultSet: relationalStore.ResultSet): Order {
    const order = new Order()
    order.id = resultSet.getLong(resultSet.getColumnIndex('id'))
    order.recipeId = resultSet.getLong(resultSet.getColumnIndex('recipe_id'))
    order.recipeName = resultSet.getString(resultSet.getColumnIndex('recipe_name'))
    order.recipeImage = resultSet.getString(resultSet.getColumnIndex('recipe_image'))
    order.status = resultSet.getLong(resultSet.getColumnIndex('status'))
    order.remark = resultSet.getString(resultSet.getColumnIndex('remark'))
    order.orderTime = resultSet.getLong(resultSet.getColumnIndex('order_time'))
    order.acceptTime = resultSet.getLong(resultSet.getColumnIndex('accept_time'))
    order.completeTime = resultSet.getLong(resultSet.getColumnIndex('complete_time'))
    return order
  }

  // ======================== Gallery CRUD ========================

  static async insertGalleryImage(image: GalleryImage): Promise<number> {
    if (!RdbUtil.rdbStore) return -1
    try {
      return await RdbUtil.rdbStore.insert('gallery_img', {
        'image_path': image.imagePath, 'description': image.description,
        'order_id': image.orderId, 'create_time': image.createTime
      })
    } catch (err) {
      LogUtil.error(RdbUtil.TAG, `插入相册图片失败: ${err}`)
      return -1
    }
  }

  static async queryAllGalleryImages(): Promise<GalleryImage[]> {
    if (!RdbUtil.rdbStore) return []
    try {
      const predicates = new relationalStore.RdbPredicates('gallery_img')
      predicates.orderByDesc('create_time')
      const resultSet = await RdbUtil.rdbStore.query(predicates)
      const images: GalleryImage[] = []
      while (resultSet.goToNextRow()) {
        images.push(RdbUtil.parseGalleryImage(resultSet))
      }
      resultSet.close()
      return images
    } catch (err) {
      return []
    }
  }

  static async deleteGalleryImage(imageId: number): Promise<boolean> {
    if (!RdbUtil.rdbStore) return false
    try {
      const predicates = new relationalStore.RdbPredicates('gallery_img')
      predicates.equalTo('id', imageId)
      await RdbUtil.rdbStore.delete(predicates)
      return true
    } catch (err) {
      return false
    }
  }

  private static parseGalleryImage(resultSet: relationalStore.ResultSet): GalleryImage {
    const img = new GalleryImage()
    img.id = resultSet.getLong(resultSet.getColumnIndex('id'))
    img.imagePath = resultSet.getString(resultSet.getColumnIndex('image_path'))
    img.description = resultSet.getString(resultSet.getColumnIndex('description'))
    img.orderId = resultSet.getLong(resultSet.getColumnIndex('order_id'))
    img.createTime = resultSet.getLong(resultSet.getColumnIndex('create_time'))
    return img
  }
}
