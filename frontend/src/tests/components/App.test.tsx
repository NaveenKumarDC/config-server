import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import App from '../../App';
import { authService } from '../../services/auth';
import { groupsApi, itemsApi, environmentsApi } from '../../services/api';

// Mock the services
jest.mock('../../services/auth', () => ({
  authService: {
    login: jest.fn(),
    logout: jest.fn(),
    getCurrentUser: jest.fn(),
    isAuthenticated: jest.fn(),
    isAdmin: jest.fn()
  }
}));

jest.mock('../../services/api', () => ({
  groupsApi: {
    getAllGroups: jest.fn(),
    getGroupById: jest.fn(),
    createGroup: jest.fn(),
    updateGroup: jest.fn(),
    deleteGroup: jest.fn()
  },
  itemsApi: {
    getAllItems: jest.fn(),
    getItemById: jest.fn(),
    getItemsByGroup: jest.fn(),
    getItemsByGroupAndEnvironment: jest.fn(),
    createItem: jest.fn(),
    updateItem: jest.fn(),
    deleteItem: jest.fn()
  },
  environmentsApi: {
    getAllEnvironments: jest.fn()
  }
}));

describe('App Component', () => {
  const mockUser = { id: '1', username: 'testuser', role: 'USER' };
  const mockAdminUser = { id: '2', username: 'admin', role: 'ADMIN' };
  const mockGroups = [
    { id: '1', name: 'group1', description: 'First group' },
    { id: '2', name: 'group2', description: 'Second group' }
  ];
  const mockItems = [
    { id: '1', key: 'key1', value: 'value1', description: 'desc1', groupId: '1', environment: 'DEV' },
    { id: '2', key: 'key2', value: 'value2', description: 'desc2', groupId: '1', environment: 'PROD' }
  ];
  const mockEnvironments = ['DEV', 'STAGE', 'PROD'];

  beforeEach(() => {
    // Reset all mocks
    jest.clearAllMocks();

    // Default mock implementations
    (authService.isAuthenticated as jest.Mock).mockReturnValue(false);
    (authService.getCurrentUser as jest.Mock).mockReturnValue(null);
    (authService.isAdmin as jest.Mock).mockReturnValue(false);
    
    (groupsApi.getAllGroups as jest.Mock).mockResolvedValue(mockGroups);
    (itemsApi.getItemsByGroup as jest.Mock).mockResolvedValue(mockItems);
    (environmentsApi.getAllEnvironments as jest.Mock).mockResolvedValue(mockEnvironments);
  });

  test('renders login page when not authenticated', () => {
    render(<App />);
    
    // Check for login form elements
    expect(screen.getByPlaceholderText(/username/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /login/i })).toBeInTheDocument();
  });

  test('attempts login with provided credentials', async () => {
    (authService.login as jest.Mock).mockResolvedValue({ 
      username: 'testuser', 
      role: 'USER' 
    });
    
    render(<App />);
    
    // Fill in login form
    fireEvent.change(screen.getByPlaceholderText(/username/i), { 
      target: { value: 'testuser' } 
    });
    fireEvent.change(screen.getByPlaceholderText(/password/i), { 
      target: { value: 'password' } 
    });
    
    // Submit form
    fireEvent.click(screen.getByRole('button', { name: /login/i }));
    
    // Check if login service was called with correct credentials
    await waitFor(() => {
      expect(authService.login).toHaveBeenCalledWith('testuser', 'password');
    });
  });

  test('renders dashboard when authenticated', () => {
    // Mock authenticated user
    (authService.isAuthenticated as jest.Mock).mockReturnValue(true);
    (authService.getCurrentUser as jest.Mock).mockReturnValue(mockUser);
    
    render(<App />);
    
    // Check for dashboard elements
    expect(screen.getByText(/configuration dashboard/i)).toBeInTheDocument();
    expect(screen.getByText(/configuration groups/i)).toBeInTheDocument();
    expect(screen.getByText(/configuration items/i)).toBeInTheDocument();
  });

  test('loads groups on initial render when authenticated', async () => {
    // Mock authenticated user
    (authService.isAuthenticated as jest.Mock).mockReturnValue(true);
    (authService.getCurrentUser as jest.Mock).mockReturnValue(mockUser);
    
    render(<App />);
    
    // Check if getAllGroups was called
    await waitFor(() => {
      expect(groupsApi.getAllGroups).toHaveBeenCalled();
    });
    
    // Check if groups are rendered
    await waitFor(() => {
      expect(screen.getByText('group1')).toBeInTheDocument();
      expect(screen.getByText('group2')).toBeInTheDocument();
    });
  });

  test('loads items when a group is selected', async () => {
    // Mock authenticated user
    (authService.isAuthenticated as jest.Mock).mockReturnValue(true);
    (authService.getCurrentUser as jest.Mock).mockReturnValue(mockUser);
    
    render(<App />);
    
    // Wait for groups to load
    await waitFor(() => {
      expect(screen.getByText('group1')).toBeInTheDocument();
    });
    
    // Click on a group
    fireEvent.click(screen.getByText('group1'));
    
    // Check if getItemsByGroup was called with the correct group ID
    await waitFor(() => {
      expect(itemsApi.getItemsByGroup).toHaveBeenCalledWith('1');
    });
    
    // Check if items are rendered
    await waitFor(() => {
      expect(screen.getByText('key1')).toBeInTheDocument();
      expect(screen.getByText('value1')).toBeInTheDocument();
    });
  });

  test('loads environments on initial render when authenticated', async () => {
    // Mock authenticated user
    (authService.isAuthenticated as jest.Mock).mockReturnValue(true);
    (authService.getCurrentUser as jest.Mock).mockReturnValue(mockUser);
    
    render(<App />);
    
    // Check if getAllEnvironments was called
    await waitFor(() => {
      expect(environmentsApi.getAllEnvironments).toHaveBeenCalled();
    });
  });

  test('shows create group form when add group button is clicked', async () => {
    // Mock authenticated admin user
    (authService.isAuthenticated as jest.Mock).mockReturnValue(true);
    (authService.getCurrentUser as jest.Mock).mockReturnValue(mockAdminUser);
    (authService.isAdmin as jest.Mock).mockReturnValue(true);
    
    render(<App />);
    
    // Click on add group button
    fireEvent.click(screen.getByText(/add group/i));
    
    // Check if create group form is shown
    await waitFor(() => {
      expect(screen.getByPlaceholderText(/group name/i)).toBeInTheDocument();
      expect(screen.getByPlaceholderText(/group description/i)).toBeInTheDocument();
    });
  });

  test('creates a new group when form is submitted', async () => {
    // Mock authenticated admin user
    (authService.isAuthenticated as jest.Mock).mockReturnValue(true);
    (authService.getCurrentUser as jest.Mock).mockReturnValue(mockAdminUser);
    (authService.isAdmin as jest.Mock).mockReturnValue(true);
    
    // Mock group creation
    const newGroup = { id: '3', name: 'newgroup', description: 'New group' };
    (groupsApi.createGroup as jest.Mock).mockResolvedValue(newGroup);
    
    render(<App />);
    
    // Click on add group button
    fireEvent.click(screen.getByText(/add group/i));
    
    // Fill in the form
    await waitFor(() => {
      fireEvent.change(screen.getByPlaceholderText(/group name/i), { 
        target: { value: 'newgroup' } 
      });
      fireEvent.change(screen.getByPlaceholderText(/group description/i), { 
        target: { value: 'New group' } 
      });
    });
    
    // Submit the form
    fireEvent.click(screen.getByRole('button', { name: /create/i }));
    
    // Check if createGroup was called with correct data
    await waitFor(() => {
      expect(groupsApi.createGroup).toHaveBeenCalledWith({ 
        name: 'newgroup', 
        description: 'New group' 
      });
    });
    
    // Check if groups are reloaded
    await waitFor(() => {
      expect(groupsApi.getAllGroups).toHaveBeenCalledTimes(2); // Initial load + after creation
    });
  });

  test('shows create item form when add item button is clicked', async () => {
    // Mock authenticated admin user
    (authService.isAuthenticated as jest.Mock).mockReturnValue(true);
    (authService.getCurrentUser as jest.Mock).mockReturnValue(mockAdminUser);
    (authService.isAdmin as jest.Mock).mockReturnValue(true);
    
    render(<App />);
    
    // Click on add item button
    fireEvent.click(screen.getByText(/add item/i));
    
    // Check if create item form is shown
    await waitFor(() => {
      expect(screen.getByPlaceholderText(/config key/i)).toBeInTheDocument();
      expect(screen.getByPlaceholderText(/config value/i)).toBeInTheDocument();
      expect(screen.getByPlaceholderText(/description/i)).toBeInTheDocument();
    });
  });

  test('creates a new item when form is submitted', async () => {
    // Mock authenticated admin user
    (authService.isAuthenticated as jest.Mock).mockReturnValue(true);
    (authService.getCurrentUser as jest.Mock).mockReturnValue(mockAdminUser);
    (authService.isAdmin as jest.Mock).mockReturnValue(true);
    
    // Mock group selection
    render(<App />);
    
    // Wait for groups to load
    await waitFor(() => {
      expect(screen.getByText('group1')).toBeInTheDocument();
    });
    
    // Click on a group to select it
    fireEvent.click(screen.getByText('group1'));
    
    // Click on add item button
    fireEvent.click(screen.getByText(/add item/i));
    
    // Fill in the form
    await waitFor(() => {
      fireEvent.change(screen.getByPlaceholderText(/config key/i), { 
        target: { value: 'newkey' } 
      });
      fireEvent.change(screen.getByPlaceholderText(/config value/i), { 
        target: { value: 'newvalue' } 
      });
      fireEvent.change(screen.getByPlaceholderText(/description/i), { 
        target: { value: 'New item description' } 
      });
      
      // Select environment
      const envSelect = screen.getByLabelText(/environment/i);
      fireEvent.change(envSelect, { target: { value: 'DEV' } });
    });
    
    // Mock item creation
    const newItem = { 
      id: '3', 
      key: 'newkey', 
      value: 'newvalue', 
      description: 'New item description',
      groupId: '1',
      environment: 'DEV'
    };
    (itemsApi.createItem as jest.Mock).mockResolvedValue(newItem);
    
    // Submit the form
    fireEvent.click(screen.getByRole('button', { name: /create/i }));
    
    // Check if createItem was called with correct data
    await waitFor(() => {
      expect(itemsApi.createItem).toHaveBeenCalledWith(expect.objectContaining({ 
        key: 'newkey',
        value: 'newvalue',
        description: 'New item description',
        groupId: '1',
        environment: 'DEV'
      }));
    });
    
    // Check if items are reloaded
    await waitFor(() => {
      expect(itemsApi.getItemsByGroup).toHaveBeenCalledTimes(2); // Initial load + after creation
    });
  });

  test('logs out when logout button is clicked', async () => {
    // Mock authenticated user
    (authService.isAuthenticated as jest.Mock).mockReturnValue(true);
    (authService.getCurrentUser as jest.Mock).mockReturnValue(mockUser);
    
    render(<App />);
    
    // Click on logout button
    fireEvent.click(screen.getByText(/logout/i));
    
    // Check if logout was called
    expect(authService.logout).toHaveBeenCalled();
    
    // Check if app returns to login page
    await waitFor(() => {
      expect(screen.getByPlaceholderText(/username/i)).toBeInTheDocument();
    });
  });

  test('shows user management section for admin users', async () => {
    // Mock authenticated admin user
    (authService.isAuthenticated as jest.Mock).mockReturnValue(true);
    (authService.getCurrentUser as jest.Mock).mockReturnValue(mockAdminUser);
    (authService.isAdmin as jest.Mock).mockReturnValue(true);
    
    render(<App />);
    
    // Check if user management section is present
    expect(screen.getByText(/user management/i)).toBeInTheDocument();
  });

  test('does not show user management section for regular users', async () => {
    // Mock authenticated regular user
    (authService.isAuthenticated as jest.Mock).mockReturnValue(true);
    (authService.getCurrentUser as jest.Mock).mockReturnValue(mockUser);
    (authService.isAdmin as jest.Mock).mockReturnValue(false);
    
    render(<App />);
    
    // Check if user management section is not present
    expect(screen.queryByText(/user management/i)).not.toBeInTheDocument();
  });
}); 