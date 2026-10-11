export const pathToPermissionCode: Record<string, string> = {
  '/dashboard': 'overview:dashboard',
  '/recipes': 'content:recipe',
  '/categories': 'content:category',
  '/reviews': 'content:review',
  '/orders': 'business:order',
  '/inventory': 'business:inventory',
  '/suppliers': 'business:supplier',
  '/meal-plans': 'business:mealplan',
  '/shopping': 'business:shopping',
  '/favorites': 'content:favorite',
  '/gallery': 'content:gallery',
  '/organizations': 'business:organization',
  '/couples': 'business:couple',
  '/statistics': 'system:statistics',
  '/reports': 'system:reports',
  '/export': 'system:export',
  '/users': 'system:user',
  '/roles': 'system:role',
  '/permissions': 'system:permission',
  '/notices': 'system:notice',
  '/settings': 'logs:settings',
  '/logs': 'logs:operation',
  '/sync-logs': 'logs:sync',
  '/health': 'logs:health'
}

export function findPermissionCodeForPath(path: string): string | null {
  let matched: string | null = null
  for (const key of Object.keys(pathToPermissionCode)) {
    if (path === key || path.startsWith(key + '/')) {
      if (!matched || key.length > matched.length) {
        matched = key
      }
    }
  }
  return matched ? pathToPermissionCode[matched] : null
}
