import React, { useState, useEffect } from 'react';
import authService from '../services/auth';

interface UserManagementProps {
  isAdmin: boolean;
}

interface UserData {
  id: number;
  username: string;
  email: string;
  role: string;
}

interface InlineEditState {
  userId: number | null;
  field: 'username' | 'role' | null;
  value: string;
}

const UserManagement: React.FC<UserManagementProps> = ({ isAdmin }) => {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [role, setRole] = useState('READ_ONLY');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [users, setUsers] = useState<UserData[]>([]);
  const [editingUser, setEditingUser] = useState<UserData | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [sortField, setSortField] = useState<keyof UserData>('username');
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('asc');
  const [inlineEdit, setInlineEdit] = useState<InlineEditState>({
    userId: null,
    field: null,
    value: ''
  });

  // Debug info
  useEffect(() => {
    console.log('UserManagement component - isAdmin:', isAdmin);
    if (isAdmin) {
      fetchUsers();
    }
  }, [isAdmin]);

  // Fetch users
  const fetchUsers = async () => {
    try {
      const response = await fetch('/api/users', {
        headers: {
          'Authorization': `Bearer ${authService.getToken()}`
        }
      });
      if (response.ok) {
        const data = await response.json();
        setUsers(data);
      }
    } catch (err) {
      console.error('Error fetching users:', err);
    }
  };

  // Filter and sort users
  const filteredUsers = users
    .filter(user => 
      user.username.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.role.toLowerCase().includes(searchTerm.toLowerCase())
    )
    .sort((a, b) => {
      if (sortDirection === 'asc') {
        return a[sortField] > b[sortField] ? 1 : -1;
      } else {
        return a[sortField] < b[sortField] ? 1 : -1;
      }
    });

  // Handle sort click
  const handleSort = (field: keyof UserData) => {
    if (field === sortField) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      setSortField(field);
      setSortDirection('asc');
    }
  };

  // Start inline editing
  const startInlineEdit = (user: UserData, field: 'username' | 'role') => {
    setInlineEdit({
      userId: user.id,
      field,
      value: user[field]
    });
  };

  // Cancel inline editing
  const cancelInlineEdit = () => {
    setInlineEdit({
      userId: null,
      field: null,
      value: ''
    });
  };

  // Save inline edit
  const saveInlineEdit = async () => {
    if (!inlineEdit.userId || !inlineEdit.field) return;

    const userToUpdate = users.find(u => u.id === inlineEdit.userId);
    if (!userToUpdate) return;

    setLoading(true);
    try {
      const updatedUser = { ...userToUpdate, [inlineEdit.field]: inlineEdit.value };
      
      const response = await fetch(`/api/users/${inlineEdit.userId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${authService.getToken()}`
        },
        body: JSON.stringify(updatedUser)
      });

      if (response.ok) {
        setMessage(`User ${inlineEdit.field} updated successfully`);
        fetchUsers();
        cancelInlineEdit();
      } else {
        setError(`Failed to update user ${inlineEdit.field}: ${response.status} ${response.statusText}`);
      }
    } catch (err) {
      setError(`An error occurred while updating user ${inlineEdit.field}`);
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  // Handle keypress during inline edit
  const handleInlineEditKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      saveInlineEdit();
    } else if (e.key === 'Escape') {
      cancelInlineEdit();
    }
  };

  // Hide component for non-admin users
  if (!isAdmin) {
    return null;
  }

  const handleCreateUser = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setMessage('');
    setError('');

    try {
      const response = await fetch('/api/users', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${authService.getToken()}`
        },
        body: JSON.stringify({ username, email, role })
      });

      if (response.ok) {
        let msg = 'User created successfully';
        try {
          if (response.headers.get('content-type')?.includes('application/json')) {
            const data = await response.json();
            if (data.message) msg = data.message;
          }
        } catch(e) {
          console.log('No JSON in response, using default message');
        }
        
        setMessage(msg);
        // Reset form
        setUsername('');
        setEmail('');
        setRole('READ_ONLY');
        // Refresh user list
        fetchUsers();
      } else {
        setError(`Failed to create user: ${response.status} ${response.statusText}`);
      }
    } catch (err) {
      setError('An error occurred while creating the user');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleEditUser = (user: UserData) => {
    setEditingUser(user);
    setUsername(user.username);
    setEmail(user.email);
    setRole(user.role);
  };

  const handleUpdateUser = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingUser) return;
    
    setLoading(true);
    setMessage('');
    setError('');

    try {
      const response = await fetch(`/api/users/${editingUser.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${authService.getToken()}`
        },
        body: JSON.stringify({ username, email, role })
      });

      if (response.ok) {
        setMessage('User updated successfully');
        setEditingUser(null);
        setUsername('');
        setEmail('');
        setRole('READ_ONLY');
        fetchUsers();
      } else {
        setError(`Failed to update user: ${response.status} ${response.statusText}`);
      }
    } catch (err) {
      setError('An error occurred while updating the user');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteUser = async (userId: number) => {
    if (!window.confirm('Are you sure you want to delete this user?')) return;
    
    try {
      const token = authService.getToken();
      console.log('Sending delete request for user ID:', userId);
      console.log('Authorization token available:', !!token);
      
      const response = await fetch(`/api/users/${userId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      console.log('Delete response status:', response.status);
      
      if (response.ok) {
        setMessage('User deleted successfully');
        fetchUsers();
      } else {
        setError(`Failed to delete user: ${response.status} ${response.statusText}`);
      }
    } catch (err) {
      setError('An error occurred while deleting the user');
      console.error('Error deleting user:', err);
    }
  };

  const cancelEdit = () => {
    setEditingUser(null);
    setUsername('');
    setEmail('');
    setRole('READ_ONLY');
    setError('');
    setMessage('');
  };

  return (
    <div className="space-y-8">
      {/* Message and Error Notifications */}
      {message && (
        <div className="p-3 bg-green-100 text-green-700 rounded-md">
          {message}
        </div>
      )}
      
      {error && (
        <div className="p-3 bg-red-100 text-red-700 rounded-md">
          {error}
        </div>
      )}

      {/* Search and Filter */}
      <div className="mb-6">
        <div className="flex items-center space-x-4">
          <div className="flex-1">
            <input
              type="text"
              placeholder="Search users..."
              className="w-full px-4 py-2 border rounded-lg focus:ring-blue-500 focus:border-blue-500"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
          <button
            onClick={fetchUsers}
            className="px-4 py-2 bg-blue-100 text-blue-700 rounded-lg hover:bg-blue-200"
          >
            Refresh
          </button>
        </div>
      </div>

      {/* User Table */}
      <div className="bg-white rounded-lg shadow overflow-hidden">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th 
                  className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer"
                  onClick={() => handleSort('username')}
                >
                  Username
                  {sortField === 'username' && (
                    <span className="ml-1">{sortDirection === 'asc' ? '↑' : '↓'}</span>
                  )}
                </th>
                <th 
                  className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer"
                  onClick={() => handleSort('email')}
                >
                  Email
                  {sortField === 'email' && (
                    <span className="ml-1">{sortDirection === 'asc' ? '↑' : '↓'}</span>
                  )}
                </th>
                <th 
                  className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer"
                  onClick={() => handleSort('role')}
                >
                  Role
                  {sortField === 'role' && (
                    <span className="ml-1">{sortDirection === 'asc' ? '↑' : '↓'}</span>
                  )}
                </th>
                <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {filteredUsers.length > 0 ? (
                filteredUsers.map((user) => (
                  <tr key={user.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      {inlineEdit.userId === user.id && inlineEdit.field === 'username' ? (
                        <div className="flex items-center space-x-2">
                          <input
                            type="text"
                            className="w-full px-2 py-1 border rounded"
                            value={inlineEdit.value}
                            onChange={(e) => setInlineEdit({...inlineEdit, value: e.target.value})}
                            onKeyDown={handleInlineEditKeyPress}
                            autoFocus
                          />
                          <button 
                            onClick={saveInlineEdit}
                            className="text-green-600 hover:text-green-900"
                            title="Save"
                          >
                            ✓
                          </button>
                          <button 
                            onClick={cancelInlineEdit}
                            className="text-red-600 hover:text-red-900"
                            title="Cancel"
                          >
                            ✕
                          </button>
                        </div>
                      ) : (
                        <div className="flex items-center group">
                          <span>{user.username}</span>
                          <button 
                            onClick={() => startInlineEdit(user, 'username')}
                            className="ml-2 text-blue-600 hover:text-blue-900 opacity-0 group-hover:opacity-100 transition-opacity"
                            title="Edit username"
                          >
                            ✎
                          </button>
                        </div>
                      )}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">{user.email}</td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {inlineEdit.userId === user.id && inlineEdit.field === 'role' ? (
                        <div className="flex items-center space-x-2">
                          <select
                            className="w-full px-2 py-1 border rounded"
                            value={inlineEdit.value}
                            onChange={(e) => setInlineEdit({...inlineEdit, value: e.target.value})}
                            onKeyDown={handleInlineEditKeyPress}
                            autoFocus
                          >
                            <option value="READ_ONLY">Read Only</option>
                            <option value="ADMIN">Admin</option>
                          </select>
                          <button 
                            onClick={saveInlineEdit}
                            className="text-green-600 hover:text-green-900"
                            title="Save"
                          >
                            ✓
                          </button>
                          <button 
                            onClick={cancelInlineEdit}
                            className="text-red-600 hover:text-red-900"
                            title="Cancel"
                          >
                            ✕
                          </button>
                        </div>
                      ) : (
                        <div className="flex items-center group">
                          <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                            user.role === 'ADMIN' ? 'bg-blue-100 text-blue-800' : 'bg-gray-100 text-gray-800'
                          }`}>
                            {user.role}
                          </span>
                          <button 
                            onClick={() => startInlineEdit(user, 'role')}
                            className="ml-2 text-blue-600 hover:text-blue-900 opacity-0 group-hover:opacity-100 transition-opacity"
                            title="Edit role"
                          >
                            ✎
                          </button>
                        </div>
                      )}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-center">
                      <button
                        onClick={() => handleEditUser(user)}
                        className="text-blue-600 hover:text-blue-900 mx-2 p-1 rounded-full hover:bg-blue-100"
                        title="Edit User"
                      >
                        <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                        </svg>
                      </button>
                      <button
                        onClick={() => handleDeleteUser(user.id)}
                        className="text-red-600 hover:text-red-900 mx-2 p-1 rounded-full hover:bg-red-100"
                        title="Delete User"
                      >
                        <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                        </svg>
                      </button>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={4} className="px-6 py-4 text-center text-gray-500">
                    {users.length === 0 ? 'No users found' : 'No matching users'}
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Create/Edit User Form */}
      <div className="bg-white rounded-lg shadow p-6">
        <h3 className="text-lg font-medium text-blue-700 mb-4">
          {editingUser ? 'Edit User' : 'Create New User'}
        </h3>
        <form onSubmit={editingUser ? handleUpdateUser : handleCreateUser} className="space-y-4">
          <div>
            <label htmlFor="username" className="block text-sm font-medium text-gray-700">
              Username
            </label>
            <input
              type="text"
              id="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            />
          </div>
          
          <div>
            <label htmlFor="email" className="block text-sm font-medium text-gray-700">
              Email
            </label>
            <input
              type="email"
              id="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            />
          </div>
          
          <div>
            <label htmlFor="role" className="block text-sm font-medium text-gray-700">
              Role
            </label>
            <select
              id="role"
              value={role}
              onChange={(e) => setRole(e.target.value)}
              className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            >
              <option value="READ_ONLY">Read Only</option>
              <option value="ADMIN">Admin</option>
            </select>
          </div>
          
          <div className="flex justify-end space-x-3">
            {editingUser && (
              <button
                type="button"
                onClick={cancelEdit}
                className="px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
              >
                Cancel
              </button>
            )}
            <button
              type="submit"
              disabled={loading}
              className="inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              {loading ? (editingUser ? 'Updating...' : 'Creating...') : (editingUser ? 'Update User' : 'Create User')}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default UserManagement; 