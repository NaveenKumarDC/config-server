import { authService } from '../../services/auth';
import axios from 'axios';
import MockAdapter from 'axios-mock-adapter';

// Setup axios mock
const mockAxios = new MockAdapter(axios);

// Mock localStorage
const localStorageMock = (() => {
  let store: Record<string, string> = {};
  return {
    getItem: (key: string) => store[key] || null,
    setItem: (key: string, value: string) => {
      store[key] = value;
    },
    removeItem: (key: string) => {
      delete store[key];
    },
    clear: () => {
      store = {};
    }
  };
})();

Object.defineProperty(window, 'localStorage', { value: localStorageMock });

describe('Auth Service', () => {
  const mockUserResponse = {
    username: 'testuser',
    role: 'USER',
    token: 'fake-jwt-token'
  };
  
  const mockAdminResponse = {
    username: 'admin',
    role: 'ADMIN',
    token: 'fake-admin-jwt-token'
  };

  beforeEach(() => {
    // Reset mocks before each test
    mockAxios.reset();
    localStorage.clear();
  });

  test('login with valid credentials returns user data and stores in localStorage', async () => {
    // Setup mock response
    mockAxios.onPost('http://localhost:8080/api/auth/login').reply(200, mockUserResponse);
    
    // Call the login function
    const result = await authService.login('testuser', 'password');
    
    // Verify the result
    expect(result).toEqual(mockUserResponse);
    
    // Verify localStorage was updated
    expect(localStorage.getItem('user')).toBe(JSON.stringify(mockUserResponse));
  });

  test('login with admin credentials sets correct role', async () => {
    // Setup mock response for admin login
    mockAxios.onPost('http://localhost:8080/api/auth/login').reply(200, mockAdminResponse);
    
    // Call the login function
    const result = await authService.login('admin', 'adminpass');
    
    // Verify the result has admin role
    expect(result.role).toBe('ADMIN');
    
    // Verify localStorage was updated with admin data
    expect(localStorage.getItem('user')).toBe(JSON.stringify(mockAdminResponse));
  });

  test('login handles API error', async () => {
    // Setup mock to return an error
    mockAxios.onPost('http://localhost:8080/api/auth/login').reply(401, { message: 'Invalid credentials' });
    
    // Call the function and expect it to throw
    await expect(authService.login('baduser', 'wrongpass')).rejects.toThrow();
    
    // Verify localStorage was not updated
    expect(localStorage.getItem('user')).toBeNull();
  });

  test('logout removes user from localStorage', () => {
    // Setup initial state with user logged in
    localStorage.setItem('user', JSON.stringify(mockUserResponse));
    
    // Call logout
    authService.logout();
    
    // Verify localStorage was cleared
    expect(localStorage.getItem('user')).toBeNull();
  });

  test('getCurrentUser returns user from localStorage', () => {
    // Setup localStorage with user data
    localStorage.setItem('user', JSON.stringify(mockUserResponse));
    
    // Call getCurrentUser
    const user = authService.getCurrentUser();
    
    // Verify result
    expect(user).toEqual(mockUserResponse);
  });

  test('getCurrentUser returns null when no user in localStorage', () => {
    // Ensure localStorage is empty
    localStorage.clear();
    
    // Call getCurrentUser
    const user = authService.getCurrentUser();
    
    // Verify result is null
    expect(user).toBeNull();
  });

  test('isAuthenticated returns true when user exists in localStorage', () => {
    // Setup localStorage with user data
    localStorage.setItem('user', JSON.stringify(mockUserResponse));
    
    // Check authentication status
    const isAuth = authService.isAuthenticated();
    
    // Verify result
    expect(isAuth).toBe(true);
  });

  test('isAuthenticated returns false when no user in localStorage', () => {
    // Ensure localStorage is empty
    localStorage.clear();
    
    // Check authentication status
    const isAuth = authService.isAuthenticated();
    
    // Verify result
    expect(isAuth).toBe(false);
  });

  test('isAdmin returns true for admin users', () => {
    // Setup localStorage with admin user data
    localStorage.setItem('user', JSON.stringify(mockAdminResponse));
    
    // Check admin status
    const isAdmin = authService.isAdmin();
    
    // Verify result
    expect(isAdmin).toBe(true);
  });

  test('isAdmin returns false for regular users', () => {
    // Setup localStorage with regular user data
    localStorage.setItem('user', JSON.stringify(mockUserResponse));
    
    // Check admin status
    const isAdmin = authService.isAdmin();
    
    // Verify result
    expect(isAdmin).toBe(false);
  });

  test('isAdmin returns false when no user in localStorage', () => {
    // Ensure localStorage is empty
    localStorage.clear();
    
    // Check admin status
    const isAdmin = authService.isAdmin();
    
    // Verify result
    expect(isAdmin).toBe(false);
  });

  test('login handles network error', async () => {
    // Setup mock to simulate network error
    mockAxios.onPost('http://localhost:8080/api/auth/login').networkError();
    
    // Call the function and expect it to throw
    await expect(authService.login('testuser', 'password')).rejects.toThrow();
    
    // Verify localStorage was not updated
    expect(localStorage.getItem('user')).toBeNull();
  });

  test('login handles malformed response', async () => {
    // Setup mock to return malformed data
    mockAxios.onPost('http://localhost:8080/api/auth/login').reply(200, { 
      // Missing required fields
      token: 'fake-token' 
    });
    
    // Call the function and store the result
    const result = await authService.login('testuser', 'password');
    
    // Verify the result has default values
    expect(result.username).toBeUndefined();
    expect(result.role).toBeUndefined();
    
    // Verify localStorage was updated with the incomplete data
    expect(localStorage.getItem('user')).toBeTruthy();
  });

  test('getAuthHeader returns correct authorization header when logged in', () => {
    // Setup localStorage with user data
    localStorage.setItem('user', JSON.stringify(mockUserResponse));
    
    // Get auth header
    const header = authService.getAuthHeader();
    
    // Verify result
    expect(header).toEqual({ Authorization: 'Bearer fake-jwt-token' });
  });

  test('getAuthHeader returns empty object when not logged in', () => {
    // Ensure localStorage is empty
    localStorage.clear();
    
    // Get auth header
    const header = authService.getAuthHeader();
    
    // Verify result is empty object
    expect(header).toEqual({});
  });
}); 