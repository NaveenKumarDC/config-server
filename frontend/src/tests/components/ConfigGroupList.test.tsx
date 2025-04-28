import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import ConfigGroupList from '../../components/ConfigGroupList';
import { groupsApi } from '../../services/api';

// Mock the API
jest.mock('../../services/api', () => ({
  groupsApi: {
    getAllGroups: jest.fn(),
    deleteGroup: jest.fn()
  }
}));

// Mock confirm dialog 
global.confirm = jest.fn();

describe('ConfigGroupList Component', () => {
  const mockGroups = [
    { id: '1', name: 'auth-service', description: 'Authentication service configs' },
    { id: '2', name: 'user-service', description: 'User management service configs' },
    { id: '3', name: 'payment-service', description: 'Payment processing configs' }
  ];
  
  const mockEmptyGroups: any[] = [];
  
  beforeEach(() => {
    jest.clearAllMocks();
  });
  
  test('renders loading state initially', () => {
    // Mock API to delay response
    (groupsApi.getAllGroups as jest.Mock).mockImplementation(() => 
      new Promise(resolve => setTimeout(() => resolve(mockGroups), 100))
    );
    
    render(<ConfigGroupList onGroupSelect={jest.fn()} />);
    
    // Check loading indicator is shown
    expect(screen.getByText(/loading/i)).toBeInTheDocument();
  });
  
  test('renders groups when data is loaded', async () => {
    // Mock API to return groups
    (groupsApi.getAllGroups as jest.Mock).mockResolvedValue(mockGroups);
    
    render(<ConfigGroupList onGroupSelect={jest.fn()} />);
    
    // Wait for groups to be displayed
    await waitFor(() => {
      mockGroups.forEach(group => {
        expect(screen.getByText(group.name)).toBeInTheDocument();
        expect(screen.getByText(group.description)).toBeInTheDocument();
      });
    });
  });
  
  test('renders empty state when no groups exist', async () => {
    // Mock API to return empty array
    (groupsApi.getAllGroups as jest.Mock).mockResolvedValue(mockEmptyGroups);
    
    render(<ConfigGroupList onGroupSelect={jest.fn()} />);
    
    // Wait for empty state message
    await waitFor(() => {
      expect(screen.getByText(/no configuration groups/i)).toBeInTheDocument();
    });
  });
  
  test('calls onGroupSelect when a group is clicked', async () => {
    // Mock API to return groups
    (groupsApi.getAllGroups as jest.Mock).mockResolvedValue(mockGroups);
    
    const mockOnGroupSelect = jest.fn();
    render(<ConfigGroupList onGroupSelect={mockOnGroupSelect} />);
    
    // Wait for groups to be displayed
    await waitFor(() => {
      expect(screen.getByText(mockGroups[0].name)).toBeInTheDocument();
    });
    
    // Click on the first group
    fireEvent.click(screen.getByText(mockGroups[0].name));
    
    // Check if onGroupSelect was called with the correct group
    expect(mockOnGroupSelect).toHaveBeenCalledWith(mockGroups[0]);
  });
  
  test('handles error state when API fails', async () => {
    // Mock API to throw error
    const errorMessage = 'Failed to fetch groups';
    (groupsApi.getAllGroups as jest.Mock).mockRejectedValue(new Error(errorMessage));
    
    render(<ConfigGroupList onGroupSelect={jest.fn()} />);
    
    // Wait for error message
    await waitFor(() => {
      expect(screen.getByText(/error loading groups/i)).toBeInTheDocument();
    });
  });
  
  test('confirms and deletes a group when delete button is clicked', async () => {
    // Mock API to return groups
    (groupsApi.getAllGroups as jest.Mock).mockResolvedValue(mockGroups);
    (groupsApi.deleteGroup as jest.Mock).mockResolvedValue({ success: true });
    
    // Mock confirm to return true
    (global.confirm as jest.Mock).mockReturnValue(true);
    
    render(<ConfigGroupList onGroupSelect={jest.fn()} />);
    
    // Wait for groups to be displayed
    await waitFor(() => {
      expect(screen.getAllByRole('button', { name: /delete/i })).toHaveLength(mockGroups.length);
    });
    
    // Click delete button for first group
    fireEvent.click(screen.getAllByRole('button', { name: /delete/i })[0]);
    
    // Check if confirm was called
    expect(global.confirm).toHaveBeenCalledWith(expect.stringContaining(mockGroups[0].name));
    
    // Check if deleteGroup was called with correct ID
    expect(groupsApi.deleteGroup).toHaveBeenCalledWith(mockGroups[0].id);
    
    // Verify getAllGroups is called again to refresh list
    await waitFor(() => {
      expect(groupsApi.getAllGroups).toHaveBeenCalledTimes(2);
    });
  });
  
  test('does not delete group when confirmation is cancelled', async () => {
    // Mock API to return groups
    (groupsApi.getAllGroups as jest.Mock).mockResolvedValue(mockGroups);
    
    // Mock confirm to return false
    (global.confirm as jest.Mock).mockReturnValue(false);
    
    render(<ConfigGroupList onGroupSelect={jest.fn()} />);
    
    // Wait for groups to be displayed
    await waitFor(() => {
      expect(screen.getAllByRole('button', { name: /delete/i })).toHaveLength(mockGroups.length);
    });
    
    // Click delete button for first group
    fireEvent.click(screen.getAllByRole('button', { name: /delete/i })[0]);
    
    // Check if confirm was called
    expect(global.confirm).toHaveBeenCalled();
    
    // Check that deleteGroup was not called
    expect(groupsApi.deleteGroup).not.toHaveBeenCalled();
  });
}); 