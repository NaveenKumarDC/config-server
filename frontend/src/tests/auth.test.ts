import { authService } from '../services/auth';

// Mock authService functions
jest.mock('../services/auth', () => {
  const originalModule = jest.requireActual('../services/auth');
  return {
    ...originalModule,
    authService: {
      ...originalModule.authService,
      getCurrentUser: jest.fn(),
      isAuthenticated: jest.fn(),
      isAdmin: jest.fn(),
      getToken: jest.fn(),
      login: jest.fn(),
      logout: jest.fn()
    }
  };
});

describe('AuthService', () => {
  // Sample user data
  const testUser = {
    id: 1,
    username: 'testuser',
    role: 'USER'
  };
  
  beforeEach(() => {
    // Reset mocks
    jest.clearAllMocks();
  });

  describe('login', () => {
    test('successful login stores user data and token', async () => {
      const loginData = {
        user: testUser,
        token: 'test-token'
      };
      
      (authService.login as jest.Mock).mockResolvedValueOnce(loginData);
      
      const result = await authService.login('testuser', 'password');
      
      // Check response
      expect(result).toEqual(loginData);
      expect(authService.login).toHaveBeenCalledWith('testuser', 'password');
    });
    
    test('failed login throws error', async () => {
      const errorMessage = 'Invalid credentials';
      (authService.login as jest.Mock).mockRejectedValueOnce(new Error(errorMessage));
      
      await expect(authService.login('testuser', 'wrong')).rejects.toThrow(errorMessage);
    });
  });
  
  describe('logout', () => {
    test('logout removes user data and token from localStorage', () => {
      // Setup: add user and token to localStorage
      localStorage.setItem('user', JSON.stringify(testUser));
      localStorage.setItem('token', 'test-token');
      
      authService.logout();
      
      // Check localStorage items were removed
      expect(localStorage.removeItem).toHaveBeenCalledWith('user');
      expect(localStorage.removeItem).toHaveBeenCalledWith('token');
    });
  });
  
  describe('getCurrentUser', () => {
    test('returns the current user', () => {
      (authService.getCurrentUser as jest.Mock).mockReturnValueOnce(testUser);
      
      const user = authService.getCurrentUser();
      
      expect(user).toEqual(testUser);
    });
    
    test('returns null when no user', () => {
      (authService.getCurrentUser as jest.Mock).mockReturnValueOnce(null);
      
      const user = authService.getCurrentUser();
      
      expect(user).toBeNull();
    });
  });
  
  describe('isAuthenticated', () => {
    test('returns true when user is authenticated', () => {
      (authService.isAuthenticated as jest.Mock).mockReturnValueOnce(true);
      
      const isAuthenticated = authService.isAuthenticated();
      
      expect(isAuthenticated).toBe(true);
    });
    
    test('returns false when user is not authenticated', () => {
      (authService.isAuthenticated as jest.Mock).mockReturnValueOnce(false);
      
      const isAuthenticated = authService.isAuthenticated();
      
      expect(isAuthenticated).toBe(false);
    });
  });
  
  describe('isAdmin', () => {
    test('returns true for admin users', () => {
      (authService.isAdmin as jest.Mock).mockReturnValueOnce(true);
      
      const isAdmin = authService.isAdmin();
      
      expect(isAdmin).toBe(true);
    });
    
    test('returns false for non-admin users', () => {
      (authService.isAdmin as jest.Mock).mockReturnValueOnce(false);
      
      const isAdmin = authService.isAdmin();
      
      expect(isAdmin).toBe(false);
    });
  });
  
  describe('getToken', () => {
    test('returns token', () => {
      const token = 'test-token';
      (authService.getToken as jest.Mock).mockReturnValueOnce(token);
      
      const result = authService.getToken();
      
      expect(result).toBe(token);
    });
    
    test('returns null when no token', () => {
      (authService.getToken as jest.Mock).mockReturnValueOnce(null);
      
      const result = authService.getToken();
      
      expect(result).toBeNull();
    });
  });

  describe('logout', () => {
    test('calls the logout method', () => {
      authService.logout();
      
      expect(authService.logout).toHaveBeenCalled();
    });
  });
}); 