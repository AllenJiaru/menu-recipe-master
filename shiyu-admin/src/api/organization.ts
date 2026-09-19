import request from '@/utils/request'

export interface Organization {
  id: number
  name: string
  description: string
  avatar: string
  type: string
  parentId: number
  ownerId: number
  contactName: string
  contactPhone: string
  contactEmail: string
  address: string
  sortOrder: number
  level: number
  path: string
  settings: string
  status: number
  createTime: string
}

export interface OrganizationTreeNode {
  id: number
  name: string
  type: string
  description: string
  status: number
  level: number
  parentId: number
  contactName: string
  contactPhone: string
  address: string
  createTime: string
  children: OrganizationTreeNode[]
}

export interface OrganizationMember {
  id: number
  userId: number
  username: string
  nickname: string
  avatar: string
  email: string
  phone: string
  role: string
  nicknameInOrg: string
  joinTime: string
  status: number
}

export interface OrganizationInvite {
  id: number
  organizationId: number
  inviterId: number
  inviteCode: string
  inviteeUsername: string
  role: string
  status: string
  expireTime: string
}

export interface OrgStats {
  memberCount: number
  ownerCount: number
  adminCount: number
  organization: Organization
}

export interface UserSearchResult {
  id: number
  username: string
  nickname: string
  avatar: string
  email: string
  phone: string
}

export const organizationApi = {
  // 组织管理
  getUserOrganizations: () => request.get<Organization[]>('/organizations'),
  getOrganization: (id: number) => request.get<Organization>(`/organizations/${id}`),
  createOrganization: (data: Partial<Organization>) => request.post<Organization>('/organizations', data),
  updateOrganization: (id: number, data: Partial<Organization>) => request.put<Organization>(`/organizations/${id}`, data),
  deleteOrganization: (id: number) => request.delete(`/organizations/${id}`),
  
  // 机构管理
  getOrganizationTree: () => request.get<OrganizationTreeNode[]>('/organizations/tree'),
  getOrganizationsByType: (type: string) => request.get<Organization[]>(`/organizations/by-type`, { params: { type } }),
  searchOrganizations: (keyword: string) => request.get<Organization[]>('/organizations/search', { params: { keyword } }),
  getOrganizationChildren: (parentId: number) => request.get<Organization[]>(`/organizations/${parentId}/children`),
  
  // 成员管理
  getMembers: (orgId: number) => request.get<OrganizationMember[]>(`/organizations/${orgId}/members`),
  addMember: (orgId: number, data: { userId: number, role?: string, nickname?: string }) => 
    request.post(`/organizations/${orgId}/members`, data),
  removeMember: (orgId: number, userId: number) => request.delete(`/organizations/${orgId}/members/${userId}`),
  updateMemberRole: (orgId: number, userId: number, role: string) => 
    request.put(`/organizations/${orgId}/members/${userId}/role`, { role }),
  
  // 用户搜索
  searchUsers: (orgId: number, keyword: string) => 
    request.get<UserSearchResult[]>(`/organizations/${orgId}/search-users`, { params: { keyword } }),
  
  // 邀请管理
  getInvites: (orgId: number) => request.get<OrganizationInvite[]>(`/organizations/${orgId}/invites`),
  createInvite: (orgId: number, data: { role?: string, inviteeUsername?: string }) => 
    request.post<OrganizationInvite>(`/organizations/${orgId}/invite`, data),
  joinByInviteCode: (inviteCode: string) => request.post<{ organization: Organization, role: string }>('/organizations/join', { inviteCode }),
  
  // 统计
  getOrgStats: (orgId: number) => request.get<OrgStats>(`/organizations/${orgId}/stats`),
  
  // 工具
  checkMember: (orgId: number) => request.get<boolean>(`/organizations/${orgId}/check-member`),
  getCurrentOrgId: () => request.get<number>('/organizations/current'),
}
