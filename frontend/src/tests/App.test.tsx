import React from 'react';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import App from '../App';
import { authService } from '../services/auth';
import { groupsApi, itemsApi, environmentsApi } from '../services/api';

// Mock the auth service
jest.mock('../services/auth', () => ({
  authService: {
    isAuthenticated: jest.fn(),
    isAdmin: jest.fn(),
    logout: jest.fn(),
    getCurrentUser: jest.fn(),
  }
}));

// Mock the API service
jest.mock('../services/api', () => ({
  groupsApi: {
    getAllGroups: jest.fn(),
    createGroup: jest.fn(),
    updateGroup: jest.fn(),
    deleteGroup: jest.fn(),
  },
  itemsApi: {
    getItemsByGroup: jest.fn(),
    getItemsByGroupAndEnvironment: jest.fn(),
    createItem: jest.fn(),
    updateItem: jest.fn(),
    deleteItem: jest.fn(),
  },
  environmentsApi: {
    getAllEnvironments: jest.fn(),
  }
}));

describe('App Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    
    // Default mock implementations
    (authService.isAuthenticated as jest.Mock).mockReturnValue(true);
    (authService.isAdmin as jest.Mock).mockReturnValue(false);
    (authService.getCurrentUser as jest.Mock).mockReturnValue({
      id: 1,
      username: 'testuser',
      role: 'USER'
    });
    
    (groupsApi.getAllGroups as jest.Mock).mockResolvedValue([
      { id: 1, name: 'Test Group 1' },
      { id: 2, name: 'Test Group 2' }
    ]);
    
    (environmentsApi.getAllEnvironments as jest.Mock).mockResolvedValue([
      'DEV', 'STAGE', 'PROD'
    ]);
    
    (itemsApi.getItemsByGroup as jest.Mock).mockResolvedValue([
      { id: 1, key: 'key1', value: 'value1', groupId: 1, environment: 'DEV' },
      { id: 2, key: 'key2', value: 'value2', groupId: 1, environment: 'PROD' }
    ]);
  });

  test('renders login redirect when not authenticated', async () => {
    // Set up mocks for unauthenticated state
    (authService.isAuthenticated as jest.Mock).mockReturnValue(false);
    
    render(<App />);
    
    // App should redirect to login page or show login message
    await waitFor(() => {
      expect(screen.getByText(/please log in/i)).toBeInTheDocument();
    });
  });

  test('renders dashboard when authenticated', async () => {
    render(<App />);
    
    await waitFor(() => {
      // Should show the user's name
      expect(screen.getByText(/testuser/i)).toBeInTheDocument();
      // Should show the configuration groups
      expect(screen.getByText(/Test Group 1/i)).toBeInTheDocument();
    });
  });

  test('displays admin panel for admin users', async () => {
    // Set up mock for admin user
    (authService.isAdmin as jest.Mock).mockReturnValue(true);
    (authService.getCurrentUser as jest.Mock).mockReturnValue({
      id: 1,
      username: 'admin',
      role: 'ADMIN'
    });
    
    render(<App />);
    
    await waitFor(() => {
      // Should show admin indicator
      expect(screen.getByText(/admin/i)).toBeInTheDocument();
      // Should show user management section
      expect(screen.getByText(/user management/i)).toBeInTheDocument();
    });
  });

  test('handles logout correctly', async () => {
    render(<App />);
    
    // Find and click logout button
    const logoutButton = await screen.findByText(/logout/i);
    fireEvent.click(logoutButton);
    
    // Should call logout method
    expect(authService.logout).toHaveBeenCalled();
  });

  test('can select a configuration group', async () => {
    (itemsApi.getItemsByGroupAndEnvironment as jest.Mock).mockResolvedValue([
      { id: 1, key: 'key1', value: 'value1', groupId: 1, environment: 'DEV' }
    ]);
    
    render(<App />);
    
    // Wait for groups to load
    await waitFor(() => {
      expect(screen.getByText(/Test Group 1/i)).toBeInTheDocument();
    });
    
    // Select a group
    const groupButton = screen.getByText(/Test Group 1/i);
    fireEvent.click(groupButton);
    
    // Should load items for that group
    await waitFor(() => {
      expect(itemsApi.getItemsByGroup).toHaveBeenCalledWith(1);
    });
  });

  test('creates a new configuration item', async () => {
    const newItem = {
      key: 'newKey',
      value: 'newValue',
      groupId: 1,
      environment: 'DEV'
    };
    
    (itemsApi.createItem as jest.Mock).mockResolvedValue({
      id: 3,
      ...newItem
    });
    
    render(<App />);
    
    // Wait for the component to load
    await waitFor(() => {
      expect(screen.getByText(/Test Group 1/i)).toBeInTheDocument();
    });
    
    // Find and fill the form
    const keyInput = screen.getByPlaceholderText(/configuration key/i);
    const valueInput = screen.getByPlaceholderText(/configuration value/i);
    const createButton = screen.getByText(/create configuration/i);
    
    // Select a group first
    const groupButton = screen.getByText(/Test Group 1/i);
    fireEvent.click(groupButton);
    
    // Select environment
    const envSelect = screen.getByLabelText(/environment/i);
    fireEvent.change(envSelect, { target: { value: 'DEV' } });
    
    // Fill in the form
    fireEvent.change(keyInput, { target: { value: newItem.key } });
    fireEvent.change(valueInput, { target: { value: newItem.value } });
    
    // Submit the form
    fireEvent.click(createButton);
    
    // Should call the API to create the item
    await waitFor(() => {
      expect(itemsApi.createItem).toHaveBeenCalledWith(expect.objectContaining({
        key: newItem.key,
        value: newItem.value,
        groupId: 1,
        environment: 'DEV'
      }));
    });
  });

  test('edits an existing configuration item', async () => {
    const updatedItem = {
      id: 1,
      key: 'key1',
      value: 'updated value',
      groupId: 1,
      environment: 'DEV'
    };
    
    (itemsApi.updateItem as jest.Mock).mockResolvedValue(updatedItem);
    
    render(<App />);
    
    // Wait for items to load
    await waitFor(() => {
      expect(screen.getByText(/Test Group 1/i)).toBeInTheDocument();
    });
    
    // Select a group first
    const groupButton = screen.getByText(/Test Group 1/i);
    fireEvent.click(groupButton);
    
    // Find and click edit button for the first item
    await waitFor(() => {
      const editButtons = screen.getAllByText(/edit/i);
      fireEvent.click(editButtons[0]);
    });
    
    // Find the value input in edit mode and change it
    const valueInput = screen.getByDisplayValue('value1');
    fireEvent.change(valueInput, { target: { value: 'updated value' } });
    
    // Save the changes
    const saveButton = screen.getByText(/save/i);
    fireEvent.click(saveButton);
    
    // Should call the API to update the item
    await waitFor(() => {
      expect(itemsApi.updateItem).toHaveBeenCalledWith(1, expect.objectContaining({
        value: 'updated value'
      }));
    });
  });

  test('deletes a configuration item', async () => {
    // Mock window.confirm to return true
    const originalConfirm = window.confirm;
    window.confirm = jest.fn().mockReturnValue(true);
    
    render(<App />);
    
    // Wait for items to load
    await waitFor(() => {
      expect(screen.getByText(/Test Group 1/i)).toBeInTheDocument();
    });
    
    // Select a group first
    const groupButton = screen.getByText(/Test Group 1/i);
    fireEvent.click(groupButton);
    
    // Find and click delete button for the first item
    await waitFor(() => {
      const deleteButtons = screen.getAllByText(/delete/i);
      fireEvent.click(deleteButtons[0]);
    });
    
    // Should call the API to delete the item
    await waitFor(() => {
      expect(itemsApi.deleteItem).toHaveBeenCalledWith(1);
    });
    
    // Restore original confirm
    window.confirm = originalConfirm;
  });

  test('creates a new configuration group', async () => {
    // Set up admin user
    (authService.isAdmin as jest.Mock).mockReturnValue(true);
    
    const newGroup = { name: 'New Group' };
    const createdGroup = { id: 3, name: 'New Group' };
    
    (groupsApi.createGroup as jest.Mock).mockResolvedValue(createdGroup);
    
    render(<App />);
    
    // Wait for the admin section to be visible
    await waitFor(() => {
      expect(screen.getByText(/create new group/i)).toBeInTheDocument();
    });
    
    // Fill in the form
    const nameInput = screen.getByPlaceholderText(/group name/i);
    fireEvent.change(nameInput, { target: { value: newGroup.name } });
    
    // Submit the form
    const createButton = screen.getByText(/create new group/i);
    fireEvent.click(createButton);
    
    // Should call the API to create the group
    await waitFor(() => {
      expect(groupsApi.createGroup).toHaveBeenCalledWith(newGroup);
    });
  });

  test('filters items by environment', async () => {
    render(<App />);
    
    // Wait for groups to load
    await waitFor(() => {
      expect(screen.getByText(/Test Group 1/i)).toBeInTheDocument();
    });
    
    // Select a group first
    const groupButton = screen.getByText(/Test Group 1/i);
    fireEvent.click(groupButton);
    
    // Find environment filter and select PROD
    const envFilter = screen.getByLabelText(/filter by environment/i);
    fireEvent.change(envFilter, { target: { value: 'PROD' } });
    
    // Should call API to get items for the selected environment
    await waitFor(() => {
      expect(itemsApi.getItemsByGroupAndEnvironment).toHaveBeenCalledWith(1, 'PROD');
    });
  });
}); 