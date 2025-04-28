import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import ConfigItemList from '../../components/ConfigItemList';
import { itemsApi, environmentsApi } from '../../services/api';

// Mock the API
jest.mock('../../services/api', () => ({
  itemsApi: {
    getItemsByGroup: jest.fn(),
    getItemsByGroupAndEnvironment: jest.fn(),
    deleteItem: jest.fn()
  },
  environmentsApi: {
    getAllEnvironments: jest.fn()
  }
}));

// Mock confirm dialog
global.confirm = jest.fn();

describe('ConfigItemList Component', () => {
  const mockGroup = { id: '1', name: 'auth-service', description: 'Auth service configs' };
  const mockEnvironments = ['DEV', 'STAGE', 'PROD'];
  const mockItems = [
    { id: '1', key: 'auth.timeout', value: '30', description: 'Auth timeout in seconds', groupId: '1', environment: 'DEV' },
    { id: '2', key: 'auth.attempts', value: '3', description: 'Max login attempts', groupId: '1', environment: 'DEV' },
    { id: '3', key: 'auth.timeout', value: '60', description: 'Auth timeout in seconds', groupId: '1', environment: 'PROD' }
  ];
  
  beforeEach(() => {
    jest.clearAllMocks();
    
    // Default mock implementations
    (environmentsApi.getAllEnvironments as jest.Mock).mockResolvedValue(mockEnvironments);
    (itemsApi.getItemsByGroup as jest.Mock).mockResolvedValue(mockItems);
    (itemsApi.getItemsByGroupAndEnvironment as jest.Mock).mockImplementation((groupId, env) => {
      return Promise.resolve(mockItems.filter(item => item.environment === env));
    });
  });
  
  test('renders loading state initially', () => {
    // Mock API to delay response
    (itemsApi.getItemsByGroup as jest.Mock).mockImplementation(() =>
      new Promise(resolve => setTimeout(() => resolve(mockItems), 100))
    );
    
    render(<ConfigItemList group={mockGroup} onEditItem={jest.fn()} />);
    
    // Check loading indicator is shown
    expect(screen.getByText(/loading/i)).toBeInTheDocument();
  });
  
  test('renders items for selected group', async () => {
    render(<ConfigItemList group={mockGroup} onEditItem={jest.fn()} />);
    
    // Wait for items to be displayed
    await waitFor(() => {
      expect(screen.getByText('auth.timeout')).toBeInTheDocument();
      expect(screen.getByText('auth.attempts')).toBeInTheDocument();
      expect(screen.getByText('Max login attempts')).toBeInTheDocument();
    });
    
    // Check that the API was called with the correct group ID
    expect(itemsApi.getItemsByGroup).toHaveBeenCalledWith(mockGroup.id);
  });
  
  test('filters items by environment when environment is selected', async () => {
    render(<ConfigItemList group={mockGroup} onEditItem={jest.fn()} />);
    
    // Wait for environments dropdown to be populated
    await waitFor(() => {
      expect(screen.getByRole('combobox')).toBeInTheDocument();
    });
    
    // Select PROD environment
    fireEvent.change(screen.getByRole('combobox'), { target: { value: 'PROD' } });
    
    // Verify the API was called to get items for this group and environment
    expect(itemsApi.getItemsByGroupAndEnvironment).toHaveBeenCalledWith(mockGroup.id, 'PROD');
    
    // Only PROD items should be displayed
    await waitFor(() => {
      expect(screen.getByText('auth.timeout')).toBeInTheDocument();
      expect(screen.queryByText('auth.attempts')).not.toBeInTheDocument();
    });
  });
  
  test('displays empty state when no items exist', async () => {
    // Mock API to return empty array
    (itemsApi.getItemsByGroup as jest.Mock).mockResolvedValue([]);
    
    render(<ConfigItemList group={mockGroup} onEditItem={jest.fn()} />);
    
    // Wait for empty state message
    await waitFor(() => {
      expect(screen.getByText(/no configuration items/i)).toBeInTheDocument();
    });
  });
  
  test('calls onEditItem when edit button is clicked', async () => {
    const mockOnEditItem = jest.fn();
    render(<ConfigItemList group={mockGroup} onEditItem={mockOnEditItem} />);
    
    // Wait for items to be displayed
    await waitFor(() => {
      expect(screen.getAllByRole('button', { name: /edit/i })).toHaveLength(mockItems.length);
    });
    
    // Click edit button for first item
    fireEvent.click(screen.getAllByRole('button', { name: /edit/i })[0]);
    
    // Check if onEditItem was called with the correct item
    expect(mockOnEditItem).toHaveBeenCalledWith(mockItems[0]);
  });
  
  test('confirms and deletes an item when delete button is clicked', async () => {
    // Mock delete API call
    (itemsApi.deleteItem as jest.Mock).mockResolvedValue({ success: true });
    
    // Mock confirm to return true
    (global.confirm as jest.Mock).mockReturnValue(true);
    
    render(<ConfigItemList group={mockGroup} onEditItem={jest.fn()} />);
    
    // Wait for items to be displayed
    await waitFor(() => {
      expect(screen.getAllByRole('button', { name: /delete/i })).toHaveLength(mockItems.length);
    });
    
    // Click delete button for first item
    fireEvent.click(screen.getAllByRole('button', { name: /delete/i })[0]);
    
    // Check if confirm was called
    expect(global.confirm).toHaveBeenCalledWith(expect.stringContaining(mockItems[0].key));
    
    // Check if deleteItem was called with correct ID
    expect(itemsApi.deleteItem).toHaveBeenCalledWith(mockItems[0].id);
    
    // Verify group items are fetched again to refresh list
    await waitFor(() => {
      expect(itemsApi.getItemsByGroup).toHaveBeenCalledTimes(2);
    });
  });
  
  test('does not delete item when confirmation is cancelled', async () => {
    // Mock confirm to return false
    (global.confirm as jest.Mock).mockReturnValue(false);
    
    render(<ConfigItemList group={mockGroup} onEditItem={jest.fn()} />);
    
    // Wait for items to be displayed
    await waitFor(() => {
      expect(screen.getAllByRole('button', { name: /delete/i })).toHaveLength(mockItems.length);
    });
    
    // Click delete button for first item
    fireEvent.click(screen.getAllByRole('button', { name: /delete/i })[0]);
    
    // Check if confirm was called
    expect(global.confirm).toHaveBeenCalled();
    
    // Check that deleteItem was not called
    expect(itemsApi.deleteItem).not.toHaveBeenCalled();
  });
  
  test('handles error state when API fails', async () => {
    // Mock API to throw error
    const errorMessage = 'Failed to fetch items';
    (itemsApi.getItemsByGroup as jest.Mock).mockRejectedValue(new Error(errorMessage));
    
    render(<ConfigItemList group={mockGroup} onEditItem={jest.fn()} />);
    
    // Wait for error message
    await waitFor(() => {
      expect(screen.getByText(/error loading items/i)).toBeInTheDocument();
    });
  });
}); 