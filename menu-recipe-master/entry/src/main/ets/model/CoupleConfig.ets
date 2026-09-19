/**
 * 情侣配置数据模型
 */
export class CoupleConfig {
  id: number = 0
  space_name: string = ''        // 情侣空间名称
  chef_name: string = ''         // 主厨姓名
  diner_name: string = ''        // 食客姓名
  create_time: number = 0        // 创建时间
  
  constructor() {}
}
