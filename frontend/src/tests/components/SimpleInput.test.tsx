import React from 'react';
import { render, fireEvent, waitFor, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import SimpleInput from '../../components/SimpleInput';

describe('SimpleInput Component', () => {
  test('renders input field with placeholder', () => {
    render(
      <SimpleInput 
        value="" 
        onChange={jest.fn()} 
        placeholder="Enter text"
      />
    );
    
    expect(screen.getByPlaceholderText('Enter text')).toBeInTheDocument();
  });
  
  test('renders with initial value', () => {
    render(
      <SimpleInput 
        value="Test value" 
        onChange={jest.fn()} 
      />
    );
    
    const inputElement = screen.getByDisplayValue('Test value');
    expect(inputElement).toBeInTheDocument();
  });
  
  test('calls onChange when value changes and blur event occurs', async () => {
    const handleChange = jest.fn();
    
    render(
      <SimpleInput 
        value="Initial" 
        onChange={handleChange} 
      />
    );
    
    const inputElement = screen.getByDisplayValue('Initial');
    
    // Change the value
    fireEvent.change(inputElement, { target: { value: 'Updated value' } });
    
    // Blur the input to trigger the onChange callback
    fireEvent.blur(inputElement);
    
    // Check if onChange was called with the new value
    expect(handleChange).toHaveBeenCalledWith('Updated value');
  });
  
  test('does not call onChange if value has not changed', async () => {
    const handleChange = jest.fn();
    
    render(
      <SimpleInput 
        value="Test" 
        onChange={handleChange} 
      />
    );
    
    const inputElement = screen.getByDisplayValue('Test');
    
    // Blur without changing the value
    fireEvent.blur(inputElement);
    
    // Check that onChange was not called
    expect(handleChange).not.toHaveBeenCalled();
  });
  
  test('renders textarea when isTextarea is true', () => {
    render(
      <SimpleInput 
        value="Test" 
        onChange={jest.fn()} 
        isTextarea={true}
      />
    );
    
    const textareaElement = screen.getByDisplayValue('Test');
    expect(textareaElement.tagName).toBe('TEXTAREA');
  });
  
  test('applies className correctly', () => {
    render(
      <SimpleInput 
        value="Test" 
        onChange={jest.fn()} 
        className="custom-class"
      />
    );
    
    const inputElement = screen.getByDisplayValue('Test');
    expect(inputElement).toHaveClass('custom-class');
  });
  
  test('updates displayed value when prop changes', async () => {
    const { rerender } = render(
      <SimpleInput 
        value="Initial" 
        onChange={jest.fn()} 
      />
    );
    
    // Check initial value
    expect(screen.getByDisplayValue('Initial')).toBeInTheDocument();
    
    // Re-render with new value
    rerender(
      <SimpleInput 
        value="Updated" 
        onChange={jest.fn()} 
      />
    );
    
    // Check updated value
    expect(screen.getByDisplayValue('Updated')).toBeInTheDocument();
  });
}); 