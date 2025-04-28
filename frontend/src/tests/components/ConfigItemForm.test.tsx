import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import ConfigItemForm from '../../components/ConfigItemForm';
import { itemsApi, environmentsApi } from '../../services/api';

// Mock components
jest.mock('../../components/SimpleInput', () => {
  return function MockSimpleInput({ value, onChange, placeholder, isTextarea, className }: any) {
    return (
      <input
        data-testid={isTextarea ? 'textarea' : 'input'}
        className={className}
        placeholder={placeholder}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        aria-label={placeholder}
      />
    );
  };
});

// Mock the API
jest.mock('../../services/api', () => ({
  itemsApi: {
    createItem: jest.fn(),
    updateItem: jest.fn()
  },
  environmentsApi: {
    getAllEnvironments: jest.fn()
  }
}));

describe('ConfigItemForm Component', () => {
  const mockEnvironments = ['DEV', 'STAGE', 'PROD'];
  const mockItem = {
    id: 1,
    key: 'auth.timeout',
    value: '30',
    description: 'Auth timeout in seconds',
    groupId: 1,
    groupName: 'auth-service',
    environment: 'DEV'
  };
  
  beforeEach(() => {
    jest.clearAllMocks();
    itemsApi.createItem = jest.fn().mockResolvedValue({ ...mockItem, id: 999 });
    itemsApi.updateItem = jest.fn().mockResolvedValue(mockItem);
  });
  
  test('renders form with empty fields in create mode', async () => {
    render(
      <ConfigItemForm 
        groupId="1"
        environments={mockEnvironments}
        onSave={jest.fn()}
        onCancel={jest.fn()}
      />
    );
    
    // Form title should indicate create mode
    expect(screen.getByText('Add New Configuration Item')).toBeInTheDocument();
    
    // Environment dropdown should be populated
    const environmentSelect = screen.getByLabelText(/environment/i);
    expect(environmentSelect).toBeInTheDocument();
    
    // Input fields should be empty
    expect(screen.getByPlaceholderText(/enter configuration key/i)).toHaveValue('');
    expect(screen.getByPlaceholderText(/enter configuration value/i)).toHaveValue('');
    expect(screen.getByPlaceholderText(/enter description/i)).toHaveValue('');
    
    // Save button should be present with Create text
    expect(screen.getByRole('button', { name: /create/i })).toBeInTheDocument();
  });
  
  test('renders form with item values in edit mode', () => {
    render(
      <ConfigItemForm 
        item={mockItem}
        groupId="1"
        environments={mockEnvironments}
        onSave={jest.fn()}
        onCancel={jest.fn()}
      />
    );
    
    // Form title should indicate edit mode
    expect(screen.getByText('Edit Configuration Item')).toBeInTheDocument();
    
    // Inputs should contain item values
    expect(screen.getByPlaceholderText(/enter configuration key/i)).toHaveValue(mockItem.key);
    expect(screen.getByPlaceholderText(/enter configuration value/i)).toHaveValue(mockItem.value);
    expect(screen.getByPlaceholderText(/enter description/i)).toHaveValue(mockItem.description);
    
    // Environment dropdown should be disabled in edit mode and have the item's environment
    const environmentSelect = screen.getByLabelText(/environment/i);
    expect(environmentSelect).toBeDisabled();
    expect(environmentSelect).toHaveValue(mockItem.environment);
    
    // Button text should be "Update"
    expect(screen.getByRole('button', { name: /update/i })).toBeInTheDocument();
  });
  
  test('validates required fields', async () => {
    render(
      <ConfigItemForm 
        groupId="1"
        environments={mockEnvironments}
        onSave={jest.fn()}
        onCancel={jest.fn()}
      />
    );
    
    // Try to submit form without values
    fireEvent.click(screen.getByRole('button', { name: /create/i }));
    
    // Check for validation error messages
    expect(screen.getByText(/key is required/i)).toBeInTheDocument();
    expect(screen.getByText(/value is required/i)).toBeInTheDocument();
    
    // API should not be called
    expect(itemsApi.createItem).not.toHaveBeenCalled();
  });
  
  test('calls createItem API in create mode', async () => {
    const onSaveMock = jest.fn();
    
    render(
      <ConfigItemForm 
        groupId="1"
        environments={mockEnvironments}
        onSave={onSaveMock}
        onCancel={jest.fn()}
      />
    );
    
    // Fill in form fields
    fireEvent.change(screen.getByPlaceholderText(/enter configuration key/i), { target: { value: 'new.key' } });
    fireEvent.change(screen.getByPlaceholderText(/enter configuration value/i), { target: { value: '42' } });
    fireEvent.change(screen.getByPlaceholderText(/enter description/i), { target: { value: 'New item description' } });
    fireEvent.change(screen.getByLabelText(/environment/i), { target: { value: 'STAGE' } });
    
    // Submit form
    fireEvent.click(screen.getByRole('button', { name: /create/i }));
    
    // Check if API was called with correct values
    await waitFor(() => {
      expect(itemsApi.createItem).toHaveBeenCalledWith(
        expect.objectContaining({
          key: 'new.key',
          value: '42',
          description: 'New item description',
          groupId: 1,
          environment: 'STAGE',
          groupName: 'temp'
        })
      );
      
      // Check if onSave callback was called with created item
      expect(onSaveMock).toHaveBeenCalled();
    });
  });
  
  test('calls updateItem API in edit mode', async () => {
    const onSaveMock = jest.fn();
    
    render(
      <ConfigItemForm 
        item={mockItem}
        groupId="1"
        environments={mockEnvironments}
        onSave={onSaveMock}
        onCancel={jest.fn()}
      />
    );
    
    // Change value field
    fireEvent.change(screen.getByPlaceholderText(/enter configuration value/i), { target: { value: '999' } });
    
    // Submit form
    fireEvent.click(screen.getByRole('button', { name: /update/i }));
    
    // Check if API was called with updated values
    await waitFor(() => {
      expect(itemsApi.updateItem).toHaveBeenCalledWith(
        expect.objectContaining({
          id: mockItem.id,
          key: mockItem.key,
          value: '999',
          description: mockItem.description,
          environment: mockItem.environment,
          groupId: 1,
          groupName: 'temp'
        })
      );
      
      // Check if onSave callback was called with updated item
      expect(onSaveMock).toHaveBeenCalled();
    });
  });
  
  test('calls onCancel when cancel button is clicked', () => {
    const onCancelMock = jest.fn();
    
    render(
      <ConfigItemForm 
        groupId="1"
        environments={mockEnvironments}
        onSave={jest.fn()}
        onCancel={onCancelMock}
      />
    );
    
    // Click cancel button
    fireEvent.click(screen.getByRole('button', { name: /cancel/i }));
    
    // Check if onCancel callback was called
    expect(onCancelMock).toHaveBeenCalled();
  });
  
  test('shows loading state when saving', async () => {
    // Mock API to delay response
    itemsApi.createItem = jest.fn().mockImplementation(
      () => new Promise(resolve => setTimeout(() => resolve({ ...mockItem, id: 999 }), 100))
    );
    
    render(
      <ConfigItemForm 
        groupId="1"
        environments={mockEnvironments}
        onSave={jest.fn()}
        onCancel={jest.fn()}
      />
    );
    
    // Fill in form fields
    fireEvent.change(screen.getByPlaceholderText(/enter configuration key/i), { target: { value: 'new.key' } });
    fireEvent.change(screen.getByPlaceholderText(/enter configuration value/i), { target: { value: '42' } });
    
    // Submit form
    fireEvent.click(screen.getByRole('button', { name: /create/i }));
    
    // Check if loading indicator is shown
    expect(screen.getByText(/saving/i)).toBeInTheDocument();
    
    // After API resolves, loading indicator should disappear
    await waitFor(() => {
      expect(screen.queryByText(/saving/i)).not.toBeInTheDocument();
    });
  });
  
  test('shows error message when API fails', async () => {
    // Mock API to reject
    itemsApi.createItem = jest.fn().mockRejectedValue(new Error('API error'));
    
    render(
      <ConfigItemForm 
        groupId="1"
        environments={mockEnvironments}
        onSave={jest.fn()}
        onCancel={jest.fn()}
      />
    );
    
    // Fill in form fields
    fireEvent.change(screen.getByPlaceholderText(/enter configuration key/i), { target: { value: 'new.key' } });
    fireEvent.change(screen.getByPlaceholderText(/enter configuration value/i), { target: { value: '42' } });
    
    // Submit form
    fireEvent.click(screen.getByRole('button', { name: /create/i }));
    
    // Check if error message is shown
    await waitFor(() => {
      expect(screen.getByText(/failed to save configuration item/i)).toBeInTheDocument();
    });
  });
}); 