import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import ConfigGroup from '../../components/ConfigGroup';
import { groupsApi, itemsApi } from '../../services/api';

// Mock the API
jest.mock('../../services/api', () => ({
  groupsApi: {
    updateGroup: jest.fn(),
    deleteGroup: jest.fn()
  },
  itemsApi: {
    getItemsByGroup: jest.fn(),
    deleteItem: jest.fn()
  }
}));

describe('ConfigGroup Component', () => {
  const mockGroup = { 
    id: '1', 
    name: 'auth-service', 
    description: 'Authentication service configurations' 
  };
  
  const mockItems = [
    { id: '1', key: 'auth.timeout', value: '30', description: 'Auth timeout', groupId: '1', environment: 'DEV' },
    { id: '2', key: 'auth.retries', value: '3', description: 'Auth retries', groupId: '1', environment: 'PROD' }
  ];
  
  beforeEach(() => {
    jest.clearAllMocks();
    (itemsApi.getItemsByGroup as jest.Mock).mockResolvedValue(mockItems);
  });
  
  test('renders group name and description', () => {
    render(
      <ConfigGroup 
        group={mockGroup}
        isSelected={false}
        onSelect={jest.fn()}
        onEdit={jest.fn()}
        onDelete={jest.fn()}
      />
    );
    
    expect(screen.getByText(mockGroup.name)).toBeInTheDocument();
    expect(screen.getByText(mockGroup.description)).toBeInTheDocument();
  });
  
  test('highlights when selected', () => {
    const { rerender } = render(
      <ConfigGroup 
        group={mockGroup}
        isSelected={false}
        onSelect={jest.fn()}
        onEdit={jest.fn()}
        onDelete={jest.fn()}
      />
    );
    
    // Check that it's not highlighted when isSelected is false
    const groupElement = screen.getByTestId(`group-${mockGroup.id}`);
    expect(groupElement).not.toHaveClass('bg-blue-100');
    
    // Rerender with isSelected=true
    rerender(
      <ConfigGroup 
        group={mockGroup}
        isSelected={true}
        onSelect={jest.fn()}
        onEdit={jest.fn()}
        onDelete={jest.fn()}
      />
    );
    
    // Check that it's highlighted when isSelected is true
    expect(groupElement).toHaveClass('bg-blue-100');
  });
  
  test('calls onSelect when clicked', () => {
    const onSelectMock = jest.fn();
    
    render(
      <ConfigGroup 
        group={mockGroup}
        isSelected={false}
        onSelect={onSelectMock}
        onEdit={jest.fn()}
        onDelete={jest.fn()}
      />
    );
    
    // Click on the group
    fireEvent.click(screen.getByTestId(`group-${mockGroup.id}`));
    
    // Check if onSelect was called with the group
    expect(onSelectMock).toHaveBeenCalledWith(mockGroup);
  });
  
  test('shows edit button when hovered', async () => {
    render(
      <ConfigGroup 
        group={mockGroup}
        isSelected={false}
        onSelect={jest.fn()}
        onEdit={jest.fn()}
        onDelete={jest.fn()}
      />
    );
    
    // Edit button should initially be hidden or have low opacity
    const editButton = screen.getByLabelText(/edit/i);
    expect(editButton).toHaveClass('opacity-0');
    
    // Hover over the group
    fireEvent.mouseEnter(screen.getByTestId(`group-${mockGroup.id}`));
    
    // Edit button should now be visible
    expect(editButton).toHaveClass('opacity-100');
  });
  
  test('calls onEdit when edit button is clicked', () => {
    const onEditMock = jest.fn();
    
    render(
      <ConfigGroup 
        group={mockGroup}
        isSelected={false}
        onSelect={jest.fn()}
        onEdit={onEditMock}
        onDelete={jest.fn()}
      />
    );
    
    // Click on the edit button
    fireEvent.click(screen.getByLabelText(/edit/i));
    
    // Check if onEdit was called with the group
    expect(onEditMock).toHaveBeenCalledWith(mockGroup);
  });
  
  test('shows delete button when hovered', async () => {
    render(
      <ConfigGroup 
        group={mockGroup}
        isSelected={false}
        onSelect={jest.fn()}
        onEdit={jest.fn()}
        onDelete={jest.fn()}
      />
    );
    
    // Delete button should initially be hidden or have low opacity
    const deleteButton = screen.getByLabelText(/delete/i);
    expect(deleteButton).toHaveClass('opacity-0');
    
    // Hover over the group
    fireEvent.mouseEnter(screen.getByTestId(`group-${mockGroup.id}`));
    
    // Delete button should now be visible
    expect(deleteButton).toHaveClass('opacity-100');
  });
  
  test('shows confirmation dialog when delete button is clicked', () => {
    render(
      <ConfigGroup 
        group={mockGroup}
        isSelected={false}
        onSelect={jest.fn()}
        onEdit={jest.fn()}
        onDelete={jest.fn()}
      />
    );
    
    // Click on the delete button
    fireEvent.click(screen.getByLabelText(/delete/i));
    
    // Check if confirmation dialog is shown
    expect(screen.getByText(/are you sure/i)).toBeInTheDocument();
    expect(screen.getByText(/this action cannot be undone/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /confirm/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /cancel/i })).toBeInTheDocument();
  });
  
  test('calls onDelete when delete is confirmed', async () => {
    const onDeleteMock = jest.fn();
    (groupsApi.deleteGroup as jest.Mock).mockResolvedValue({});
    
    render(
      <ConfigGroup 
        group={mockGroup}
        isSelected={false}
        onSelect={jest.fn()}
        onEdit={jest.fn()}
        onDelete={onDeleteMock}
      />
    );
    
    // Click on the delete button
    fireEvent.click(screen.getByLabelText(/delete/i));
    
    // Click the confirm button in the dialog
    fireEvent.click(screen.getByRole('button', { name: /confirm/i }));
    
    // Check if API was called
    await waitFor(() => {
      expect(groupsApi.deleteGroup).toHaveBeenCalledWith(mockGroup.id);
    });
    
    // Check if onDelete was called with the group
    expect(onDeleteMock).toHaveBeenCalledWith(mockGroup);
  });
  
  test('does not call onDelete when delete is canceled', () => {
    const onDeleteMock = jest.fn();
    
    render(
      <ConfigGroup 
        group={mockGroup}
        isSelected={false}
        onSelect={jest.fn()}
        onEdit={jest.fn()}
        onDelete={onDeleteMock}
      />
    );
    
    // Click on the delete button
    fireEvent.click(screen.getByLabelText(/delete/i));
    
    // Click the cancel button in the dialog
    fireEvent.click(screen.getByRole('button', { name: /cancel/i }));
    
    // Check if API was not called
    expect(groupsApi.deleteGroup).not.toHaveBeenCalled();
    
    // Check if onDelete was not called
    expect(onDeleteMock).not.toHaveBeenCalled();
    
    // Check if confirmation dialog is closed
    expect(screen.queryByText(/are you sure/i)).not.toBeInTheDocument();
  });
  
  test('shows error message when delete fails', async () => {
    const errorMessage = 'Could not delete group';
    (groupsApi.deleteGroup as jest.Mock).mockRejectedValue(new Error(errorMessage));
    
    render(
      <ConfigGroup 
        group={mockGroup}
        isSelected={false}
        onSelect={jest.fn()}
        onEdit={jest.fn()}
        onDelete={jest.fn()}
      />
    );
    
    // Click on the delete button
    fireEvent.click(screen.getByLabelText(/delete/i));
    
    // Click the confirm button in the dialog
    fireEvent.click(screen.getByRole('button', { name: /confirm/i }));
    
    // Check if error message is shown
    await waitFor(() => {
      expect(screen.getByText(/error deleting group/i)).toBeInTheDocument();
    });
  });
  
  test('shows item count for the group', async () => {
    render(
      <ConfigGroup 
        group={mockGroup}
        isSelected={false}
        onSelect={jest.fn()}
        onEdit={jest.fn()}
        onDelete={jest.fn()}
      />
    );
    
    // Wait for items to be loaded
    await waitFor(() => {
      // Check if item count badge shows correct count
      expect(screen.getByText(`${mockItems.length}`)).toBeInTheDocument();
    });
  });
}); 