import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import Login from '../../components/Login';
import { authService } from '../../services/auth';

// Mock the auth service
jest.mock('../../services/auth', () => {
  return {
    authService: {
      login: jest.fn(),
      isAuthenticated: jest.fn().mockReturnValue(false)
    }
  };
});

// Mock router navigation
const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate
}));

describe('Login Component', () => {
  beforeEach(() => {
    // Reset all mocks
    jest.clearAllMocks();
  });
  
  test('renders the login form with all required elements', () => {
    render(<Login onLoginSuccess={jest.fn()} />);
    
    // Check for component title
    expect(screen.getByText(/sign in/i)).toBeInTheDocument();
    
    // Check for form fields
    expect(screen.getByLabelText(/username/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    
    // Check for submit button
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument();
    
    // Check for default credential info
    expect(screen.getByText(/default credentials/i)).toBeInTheDocument();
    expect(screen.getByText(/admin\/admin/i)).toBeInTheDocument();
    expect(screen.getByText(/user\/user/i)).toBeInTheDocument();
  });
  
  test('validates required fields on form submission', async () => {
    render(<Login onLoginSuccess={jest.fn()} />);
    
    // Submit the form with empty fields
    const submitButton = screen.getByRole('button', { name: /sign in/i });
    fireEvent.click(submitButton);
    
    // Check for validation errors
    await waitFor(() => {
      expect(screen.getByText(/username is required/i)).toBeInTheDocument();
      expect(screen.getByText(/password is required/i)).toBeInTheDocument();
    });
    
    // Verify login was not called
    expect(authService.login).not.toHaveBeenCalled();
  });
  
  test('shows loading state during login process', async () => {
    // Setup auth service to return a promise that doesn't resolve immediately
    (authService.login as jest.Mock).mockImplementation(() => new Promise(resolve => {
      setTimeout(() => resolve({ success: true }), 100);
    }));
    
    render(<Login onLoginSuccess={jest.fn()} />);
    
    // Fill in form fields
    fireEvent.change(screen.getByLabelText(/username/i), { target: { value: 'testuser' } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'password123' } });
    
    // Submit the form
    const submitButton = screen.getByRole('button', { name: /sign in/i });
    fireEvent.click(submitButton);
    
    // Check for loading state
    expect(submitButton).toBeDisabled();
    expect(screen.getByText(/signing in/i)).toBeInTheDocument();
  });
  
  test('handles successful login', async () => {
    // Setup auth service to return success
    (authService.login as jest.Mock).mockResolvedValue({ success: true });
    const onLoginSuccess = jest.fn();
    
    render(<Login onLoginSuccess={onLoginSuccess} />);
    
    // Fill in form fields
    fireEvent.change(screen.getByLabelText(/username/i), { target: { value: 'admin' } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'admin' } });
    
    // Submit the form
    const submitButton = screen.getByRole('button', { name: /sign in/i });
    fireEvent.click(submitButton);
    
    // Wait for the login process to complete
    await waitFor(() => {
      expect(authService.login).toHaveBeenCalledWith('admin', 'admin');
      expect(onLoginSuccess).toHaveBeenCalled();
    });
  });
  
  test('handles login failure', async () => {
    // Setup auth service to return failure
    const errorMessage = 'Invalid credentials';
    (authService.login as jest.Mock).mockRejectedValue(new Error(errorMessage));
    
    render(<Login onLoginSuccess={jest.fn()} />);
    
    // Fill in form fields
    fireEvent.change(screen.getByLabelText(/username/i), { target: { value: 'wronguser' } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'wrongpass' } });
    
    // Submit the form
    const submitButton = screen.getByRole('button', { name: /sign in/i });
    fireEvent.click(submitButton);
    
    // Wait for error message to appear
    await waitFor(() => {
      expect(screen.getByText(errorMessage)).toBeInTheDocument();
      expect(submitButton).not.toBeDisabled();
    });
  });
  
  test('handles default credentials click', () => {
    render(<Login onLoginSuccess={jest.fn()} />);
    
    // Click on admin credentials
    fireEvent.click(screen.getByText(/admin\/admin/i));
    
    // Check if form fields are populated
    expect(screen.getByLabelText(/username/i)).toHaveValue('admin');
    expect(screen.getByLabelText(/password/i)).toHaveValue('admin');
    
    // Click on user credentials
    fireEvent.click(screen.getByText(/user\/user/i));
    
    // Check if form fields are updated
    expect(screen.getByLabelText(/username/i)).toHaveValue('user');
    expect(screen.getByLabelText(/password/i)).toHaveValue('user');
  });
  
  test('redirects already authenticated users', () => {
    // Set up auth service to return true for isAuthenticated
    (authService.isAuthenticated as jest.Mock).mockReturnValue(true);
    
    render(<Login onLoginSuccess={jest.fn()} />);
    
    // Check if navigation was called
    expect(mockNavigate).toHaveBeenCalledWith('/');
  });
}); 