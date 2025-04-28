import axios from 'axios';
import { groupsApi, itemsApi, environmentsApi } from '../services/api';
import { authService } from '../services/auth';

// Mock axios
jest.mock('axios');
const mockedAxios = axios as jest.Mocked<typeof axios>;

// Mock auth service
jest.mock('../services/auth', () => {
  return {
    authService: {
      getToken: jest.fn().mockReturnValue('mock-token')
    },
    default: {
      getToken: jest.fn().mockReturnValue('mock-token')
    }
  };
});

// Create a function to generate standard mock response
const createMockResponse = (data: any, status = 200, url = '') => ({
  data,
  status,
  statusText: status === 200 ? 'OK' : status === 201 ? 'Created' : 'No Content',
  headers: {},
  config: { url }
});

describe('API Service', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Groups API', () => {
    const mockGroups = [
      { id: 1, name: 'group1', description: 'First group' },
      { id: 2, name: 'group2', description: 'Second group' }
    ];

    describe('getAllGroups', () => {
      test('fetches groups successfully', async () => {
        mockedAxios.get.mockResolvedValueOnce(createMockResponse(mockGroups, 200, '/api/groups'));
        
        const result = await groupsApi.getAllGroups();
        
        expect(mockedAxios.get).toHaveBeenCalledWith(expect.stringContaining('/groups'));
        expect(result).toEqual(mockGroups);
      });
      
      test('handles errors when fetching groups', async () => {
        const errorMessage = 'Network error';
        mockedAxios.get.mockRejectedValueOnce(new Error(errorMessage));
        
        await expect(groupsApi.getAllGroups()).rejects.toThrow(errorMessage);
      });
    });
    
    describe('getGroupById', () => {
      test('fetches a group by ID successfully', async () => {
        const mockGroup = mockGroups[0];
        mockedAxios.get.mockResolvedValueOnce(createMockResponse(mockGroup, 200, '/api/groups/1'));
        
        const result = await groupsApi.getGroupById(1);
        
        expect(mockedAxios.get).toHaveBeenCalledWith(expect.stringContaining('/groups/1'));
        expect(result).toEqual(mockGroup);
      });
    });
    
    describe('getGroupByName', () => {
      test('fetches a group by name successfully', async () => {
        const mockGroup = mockGroups[0];
        mockedAxios.get.mockResolvedValueOnce(createMockResponse(mockGroup, 200, '/api/groups/name/group1'));
        
        const result = await groupsApi.getGroupByName('group1');
        
        expect(mockedAxios.get).toHaveBeenCalledWith(expect.stringContaining('/groups/name/group1'));
        expect(result).toEqual(mockGroup);
      });
    });
    
    describe('createGroup', () => {
      test('creates a group successfully', async () => {
        const newGroup = { name: 'newGroup', description: 'New group' };
        const createdGroup = { ...newGroup, id: 3 };
        
        mockedAxios.post.mockResolvedValueOnce(createMockResponse(createdGroup, 201, '/api/groups'));
        
        const result = await groupsApi.createGroup(newGroup);
        
        expect(mockedAxios.post).toHaveBeenCalledWith(
          expect.stringContaining('/groups'),
          newGroup
        );
        expect(result).toEqual(createdGroup);
      });
    });
    
    describe('updateGroup', () => {
      test('updates a group successfully', async () => {
        const groupId = 1;
        const updatedGroup = { id: groupId, name: 'updatedGroup', description: 'Updated group' };
        
        mockedAxios.put.mockResolvedValueOnce(createMockResponse(updatedGroup, 200, '/api/groups/1'));
        
        const result = await groupsApi.updateGroup(groupId, updatedGroup);
        
        expect(mockedAxios.put).toHaveBeenCalledWith(
          expect.stringContaining('/groups/1'),
          updatedGroup
        );
        expect(result).toEqual(updatedGroup);
      });
    });
    
    describe('deleteGroup', () => {
      test('deletes a group successfully', async () => {
        mockedAxios.delete.mockResolvedValueOnce(createMockResponse({}, 204, '/api/groups/1'));
        
        await groupsApi.deleteGroup(1);
        
        expect(mockedAxios.delete).toHaveBeenCalledWith(expect.stringContaining('/groups/1'));
      });
    });
  });
  
  describe('Items API', () => {
    const mockItems = [
      { id: 1, key: 'key1', value: 'value1', groupId: 1, groupName: 'group1', environment: 'DEV' },
      { id: 2, key: 'key2', value: 'value2', groupId: 1, groupName: 'group1', environment: 'PROD' }
    ];
    
    describe('getAllItems', () => {
      test('fetches all items successfully', async () => {
        mockedAxios.get.mockResolvedValueOnce(createMockResponse(mockItems, 200, '/api/items'));
        
        const result = await itemsApi.getAllItems();
        
        expect(mockedAxios.get).toHaveBeenCalledWith(expect.stringContaining('/items'));
        expect(result).toEqual(mockItems);
      });
    });
    
    describe('getItemById', () => {
      test('fetches an item by ID successfully', async () => {
        const mockItem = mockItems[0];
        mockedAxios.get.mockResolvedValueOnce(createMockResponse(mockItem, 200, '/api/items/1'));
        
        const result = await itemsApi.getItemById(1);
        
        expect(mockedAxios.get).toHaveBeenCalledWith(expect.stringContaining('/items/1'));
        expect(result).toEqual(mockItem);
      });
    });
    
    describe('getItemsByGroup', () => {
      test('fetches items by group ID successfully', async () => {
        mockedAxios.get.mockResolvedValueOnce(createMockResponse(mockItems, 200, '/api/items/group/1'));
        
        const result = await itemsApi.getItemsByGroup(1);
        
        expect(mockedAxios.get).toHaveBeenCalledWith(expect.stringContaining('/items/group/1'));
        expect(result).toEqual(mockItems);
      });
    });
    
    describe('getItemsByGroupAndEnvironment', () => {
      test('fetches items by group ID and environment successfully', async () => {
        const devItems = [mockItems[0]];
        mockedAxios.get.mockResolvedValueOnce(createMockResponse(devItems, 200, '/api/items/group/1/environment/DEV'));
        
        const result = await itemsApi.getItemsByGroupAndEnvironment(1, 'DEV');
        
        expect(mockedAxios.get).toHaveBeenCalledWith(
          expect.stringContaining('/items/group/1/environment/DEV')
        );
        expect(result).toEqual(devItems);
      });
    });
    
    describe('createItem', () => {
      test('creates an item successfully', async () => {
        const newItem = { 
          key: 'newKey', 
          value: 'newValue', 
          groupId: 1,
          groupName: 'group1',
          environment: 'DEV' 
        };
        const createdItem = { ...newItem, id: 3 };
        
        mockedAxios.post.mockResolvedValueOnce(createMockResponse(createdItem, 201, '/api/items'));
        
        const result = await itemsApi.createItem(newItem);
        
        expect(mockedAxios.post).toHaveBeenCalledWith(
          expect.stringContaining('/items'),
          newItem
        );
        expect(result).toEqual(createdItem);
      });
    });
    
    describe('updateItem', () => {
      test('updates an item successfully', async () => {
        const itemId = 1;
        const updatedItem = { 
          id: itemId, 
          key: 'updatedKey', 
          value: 'updatedValue', 
          groupId: 1,
          groupName: 'group1',
          environment: 'DEV' 
        };
        
        mockedAxios.put.mockResolvedValueOnce(createMockResponse(updatedItem, 200, '/api/items/1'));
        
        const result = await itemsApi.updateItem(itemId, updatedItem);
        
        expect(mockedAxios.put).toHaveBeenCalledWith(
          expect.stringContaining('/items/1'),
          updatedItem
        );
        expect(result).toEqual(updatedItem);
      });
    });
    
    describe('deleteItem', () => {
      test('deletes an item successfully', async () => {
        mockedAxios.delete.mockResolvedValueOnce(createMockResponse({}, 204, '/api/items/1'));
        
        await itemsApi.deleteItem(1);
        
        expect(mockedAxios.delete).toHaveBeenCalledWith(expect.stringContaining('/items/1'));
      });
    });
  });
  
  describe('Environments API', () => {
    const mockEnvironments = ['DEV', 'STAGE', 'PROD'];
    
    describe('getAllEnvironments', () => {
      test('fetches all environments successfully', async () => {
        mockedAxios.get.mockResolvedValueOnce(createMockResponse(mockEnvironments, 200, '/api/environments'));
        
        const result = await environmentsApi.getAllEnvironments();
        
        expect(mockedAxios.get).toHaveBeenCalledWith(expect.stringContaining('/environments'));
        expect(result).toEqual(mockEnvironments);
      });
    });
  });
  
  describe('Axios client configuration', () => {
    test('uses the correct base URL', () => {
      expect(mockedAxios.create).toHaveBeenCalledWith(
        expect.objectContaining({
          baseURL: expect.stringContaining('/api')
        })
      );
    });
    
    test('sets content type headers correctly', () => {
      expect(mockedAxios.create).toHaveBeenCalledWith(
        expect.objectContaining({
          headers: expect.objectContaining({
            'Content-Type': 'application/json'
          })
        })
      );
    });
  });
}); 