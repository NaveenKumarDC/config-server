import axios from 'axios';
import authService from './auth';

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
  groupName: string;
}

// API client
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add a request interceptor to add the auth token
apiClient.interceptors.request.use(
  (config) => {
    const token = authService.getToken();
    if (token) {
      // Ensure headers exist before setting Authorization
      config.headers = config.headers || {};
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Groups API
export const groupsApi = {
  getAllGroups: async (): Promise<ConfigurationGroup[]> => {
    const response = await apiClient.get<ConfigurationGroup[]>('/groups');
    return response.data;
  },
  
  getGroupById: async (id: number): Promise<ConfigurationGroup> => {
    const response = await apiClient.get<ConfigurationGroup>(`/groups/${id}`);
    return response.data;
  },
  
  getGroupByName: async (name: string): Promise<ConfigurationGroup> => {
    const response = await apiClient.get<ConfigurationGroup>(`/groups/name/${name}`);
    return response.data;
  },
  
  createGroup: async (group: ConfigurationGroup): Promise<ConfigurationGroup> => {
    const response = await apiClient.post<ConfigurationGroup>('/groups', group);
    return response.data;
  },
  
  updateGroup: async (id: number, group: ConfigurationGroup): Promise<ConfigurationGroup> => {
    const response = await apiClient.put<ConfigurationGroup>(`/groups/${id}`, group);
    return response.data;
  },
  
  deleteGroup: async (id: number): Promise<void> => {
    await apiClient.delete(`/groups/${id}`);
  }
};

// Items API
export const itemsApi = {
  getAllItems: async (): Promise<ConfigurationItem[]> => {
    const response = await apiClient.get<ConfigurationItem[]>('/config-items');
    return response.data;
  },
  
  getItemById: async (id: number): Promise<ConfigurationItem> => {
    const response = await apiClient.get<ConfigurationItem>(`/config-items/${id}`);
    return response.data;
  },
  
  getItemsByGroup: async (groupId: number): Promise<ConfigurationItem[]> => {
    const response = await apiClient.get<ConfigurationItem[]>(`/config-items/group/${groupId}`);
    return response.data;
  },
  
  getItemsByGroupAndEnvironment: async (groupId: number, environment: string): Promise<ConfigurationItem[]> => {
    const response = await apiClient.get<ConfigurationItem[]>(`/config-items/group/${groupId}/environment/${environment}`);
    return response.data;
  },
  
  createItem: async (item: ConfigurationItem): Promise<ConfigurationItem> => {
    const response = await apiClient.post<ConfigurationItem>('/config-items', item);
    return response.data;
  },
  
  updateItem: async (id: number, item: ConfigurationItem): Promise<ConfigurationItem> => {
    const response = await apiClient.put<ConfigurationItem>(`/config-items/${id}`, item);
    return response.data;
  },
  
  deleteItem: async (id: number): Promise<void> => {
    console.log(`Attempting to delete item with ID: ${id}`);
    try {
      await apiClient.delete(`/config-items/${id}`);
      console.log(`Successfully deleted item with ID: ${id}`);
    } catch (error) {
      console.error(`Error deleting item with ID: ${id}`, error);
      throw error;
    }
  }
};

// Environments API
export const environmentsApi = {
  getAllEnvironments: async (): Promise<string[]> => {
    const response = await apiClient.get<string[]>('/environments');
    return response.data;
  }
}; 