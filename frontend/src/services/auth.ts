import axios from 'axios';
import { User } from '../types/user';

interface LoginResponse {
  id: number;
  username: string;
  role: string;
  token: string;
}

class AuthService {
  private API_URL = '/api';
  
  async login(username: string, password: string): Promise<any> {
    try {
      console.log('Attempting login for:', username);
      const response = await axios.post<LoginResponse>(`${this.API_URL}/auth/login`, { username, password });
      console.log('Login response:', response.data);
      if (response.data.token) {
        localStorage.setItem('user', JSON.stringify(response.data));
        console.log('Login successful, role:', response.data.role, 'user stored in localStorage');
      }
      return response;
    } catch (error) {
      console.error('Login error:', error);
      throw error;
    }
  }
  
  logout(): void {
    localStorage.removeItem('user');
  }
  
  getCurrentUser(): User | null {
    const userStr = localStorage.getItem('user');
    console.log('getCurrentUser - raw localStorage:', userStr);
    if (userStr) {
      try {
        const user = JSON.parse(userStr) as User;
        console.log('getCurrentUser - parsed user:', user);
        return user;
      } catch (e) {
        console.error('Error parsing user from localStorage', e);
        localStorage.removeItem('user');
      }
    }
    return null;
  }
  
  isAuthenticated(): boolean {
    return !!this.getCurrentUser();
  }
  
  isAdmin(): boolean {
    const user = this.getCurrentUser();
    const isAdmin = user !== null && user.role === 'ADMIN';
    console.log('Auth service - isAdmin check:', isAdmin, 'user:', user);
    return isAdmin;
  }
  
  getToken(): string | null {
    const user = this.getCurrentUser();
    return user ? (user as unknown as LoginResponse).token : null;
  }
  
  getAuthHeader(): { Authorization: string } | {} {
    const token = this.getToken();
    if (token) {
      return { Authorization: `Bearer ${token}` };
    }
    return {};
  }
}

const authService = new AuthService();
export { authService };
export default authService; 