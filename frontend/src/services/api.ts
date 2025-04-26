import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

// Types
export interface ConfigurationGroup {
  id?: number;
  name: string;
  description: string;
}

export interface ConfigurationItem {
  id?: number;
  key: string;
  value: string;
  environment: string;
  groupId: number;
  groupName?: string;
}

// API client
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Groups API
export const groupsApi = {
  getAll: () => apiClient.get<ConfigurationGroup[]>('/groups'),
  getById: (id: number) => apiClient.get<ConfigurationGroup>(`/groups/${id}`),
  getByName: (name: string) => apiClient.get<ConfigurationGroup>(`/groups/name/${name}`),
  create: (group: ConfigurationGroup) => apiClient.post<ConfigurationGroup>('/groups', group),
  update: (id: number, group: ConfigurationGroup) => apiClient.put<ConfigurationGroup>(`/groups/${id}`, group),
  delete: (id: number) => apiClient.delete(`/groups/${id}`),
};

// Items API
export const itemsApi = {
  getAll: () => apiClient.get<ConfigurationItem[]>('/items'),
  getById: (id: number) => apiClient.get<ConfigurationItem>(`/items/${id}`),
  getByGroup: (groupId: number) => apiClient.get<ConfigurationItem[]>(`/items/group/${groupId}`),
  getByGroupAndEnvironment: (groupId: number, environment: string) => 
    apiClient.get<ConfigurationItem[]>(`/items/group/${groupId}/environment/${environment}`),
  create: (item: ConfigurationItem) => apiClient.post<ConfigurationItem>('/items', item),
  update: (id: number, item: ConfigurationItem) => apiClient.put<ConfigurationItem>(`/items/${id}`, item),
  delete: (id: number) => apiClient.delete(`/items/${id}`),
};

// Environments API
export const environmentsApi = {
  getAll: () => apiClient.get<string[]>('/environments'),
}; 