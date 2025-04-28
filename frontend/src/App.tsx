import React, { useState, useEffect, useRef, useLayoutEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useSearchParams, Link, useNavigate } from 'react-router-dom';
import { groupsApi, itemsApi, environmentsApi, ConfigurationGroup, ConfigurationItem } from './services/api';
import Login from './components/Login';
import ForgotPassword from './components/ForgotPassword';
import ResetPassword from './components/ResetPassword';
import UserManagement from './components/UserManagement';
import authService from './services/auth';
import ConfigItemForm from './components/ConfigItemForm';
import SimpleInput from './components/SimpleInput';
import './index.css';

// Define User interface directly here to fix the import issue
interface User {
  username: string;
  role: string;
  id?: number;
  email?: string;
}

// This component handles URL params for ResetPassword
const ResetPasswordWrapper = ({ isSetPassword = false }) => {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token') || '';
  return <ResetPassword token={token} isSetPassword={isSetPassword} />;
};

// Header component to be shared across pages
interface HeaderProps {
  isAdmin: boolean;
  currentUser: User | null;
  onLogout: () => void;
}

const Header: React.FC<HeaderProps> = ({ isAdmin, currentUser, onLogout }) => {
  const navigate = useNavigate();
  
  return (
    <header className="bg-blue-600 shadow-md text-white">
      <div className="container mx-auto p-4 flex justify-between items-center">
        <Link to="/" className="text-2xl font-bold text-white hover:text-blue-100">
          Configuration Server
        </Link>
        <div className="flex items-center space-x-4">
          <span className="mr-2">
            Logged in as <span className="font-semibold">{currentUser?.username}</span> ({isAdmin ? 'Admin' : 'Read-only'})
          </span>
          {isAdmin && (
            <Link 
              to="/users"
              className="px-3 py-1 bg-blue-500 hover:bg-blue-400 rounded text-white"
            >
              Users
            </Link>
          )}
          <button 
            onClick={onLogout}
            className="px-3 py-1 bg-blue-500 hover:bg-blue-400 rounded text-white"
          >
            Logout
          </button>
        </div>
      </div>
    </header>
  );
};

// Add this function at the top level, before React components
function createIsolatedInputHtml(id: string, initialValue: string, placeholder: string, className: string) {
  return `
    <!DOCTYPE html>
    <html>
    <head>
      <style>
        body, html {
          margin: 0;
          padding: 0;
          overflow: hidden;
          height: 100%;
          width: 100%;
        }
        input {
          width: 100%;
          height: 100%;
          box-sizing: border-box;
          font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', sans-serif;
          font-size: 14px;
          ${className.includes('p-1') ? 'padding: 0.25rem;' : 'padding: 0.5rem;'}
          ${className.includes('border') ? 'border: 1px solid #d1d5db;' : ''}
          ${className.includes('rounded') ? 'border-radius: 0.25rem;' : ''}
          outline: none;
        }
        input:focus {
          border-color: #3b82f6;
          box-shadow: 0 0 0 1px #3b82f6;
        }
      </style>
    </head>
    <body>
      <input 
        id="${id}" 
        type="text" 
        value="${initialValue.replace(/"/g, '&quot;')}" 
        placeholder="${placeholder?.replace(/"/g, '&quot;') || ''}"
        autofocus
      />
      <script>
        const input = document.getElementById("${id}");
        
        // Forward input changes to parent
        input.addEventListener('input', function(e) {
          window.parent.postMessage({
            type: 'input-change',
            id: "${id}",
            value: e.target.value
          }, '*');
        });
        
        // Receive value updates from parent
        window.addEventListener('message', function(e) {
          if (e.data && e.data.type === 'update-value' && e.data.id === "${id}") {
            input.value = e.data.value;
          }
        });
        
        // Focus handling
        input.addEventListener('focus', function() {
          window.parent.postMessage({
            type: 'input-focus',
            id: "${id}"
          }, '*');
        });
        
        // Ensure input is focused on load
        setTimeout(() => {
          input.focus();
        }, 10);
      </script>
    </body>
    </html>
  `;
}

// Create an iframe-based isolated input
const IframeInput = ({
  id,
  initialValue = '',
  onValueChange,
  placeholder = '',
  className = ''
}: {
  id: string,
  initialValue: string,
  onValueChange: (value: string) => void,
  placeholder?: string,
  className?: string
}) => {
  const iframeRef = useRef<HTMLIFrameElement>(null);
  const valueRef = useRef(initialValue);
  const timerRef = useRef<NodeJS.Timeout | null>(null);
  
  // Setup iframe content
  useEffect(() => {
    const handleMessage = (event: MessageEvent) => {
      if (event.data && event.data.type === 'input-change' && event.data.id === id) {
        const newValue = event.data.value;
        valueRef.current = newValue;
        
        // Debounce notifying parent
        if (timerRef.current) {
          clearTimeout(timerRef.current);
        }
        
        timerRef.current = setTimeout(() => {
          onValueChange(newValue);
          timerRef.current = null;
        }, 300);
      }
    };
    
    // Add message listener
    window.addEventListener('message', handleMessage);
    
    // Set iframe content
    if (iframeRef.current) {
      try {
        const iframe = iframeRef.current;
        const iframeDoc = iframe.contentDocument || iframe.contentWindow?.document;
        
        if (iframeDoc) {
          iframeDoc.open();
          iframeDoc.write(createIsolatedInputHtml(id, valueRef.current, placeholder, className));
          iframeDoc.close();
          
          // Make sure to focus the input
          setTimeout(() => {
            iframe.contentWindow?.postMessage({
              type: 'focus-input',
              id
            }, '*');
          }, 50);
        }
      } catch (err) {
        console.error('Error setting iframe content:', err);
      }
    }
    
    return () => {
      window.removeEventListener('message', handleMessage);
      if (timerRef.current) {
        clearTimeout(timerRef.current);
      }
    };
  }, [id]);
  
  // Update iframe value if initialValue changes
  useEffect(() => {
    if (iframeRef.current && initialValue !== valueRef.current) {
      valueRef.current = initialValue;
      iframeRef.current.contentWindow?.postMessage({
        type: 'update-value',
        id,
        value: initialValue
      }, '*');
    }
  }, [initialValue, id]);
  
  return (
    <iframe
      ref={iframeRef}
      title={`input-${id}`}
      style={{
        border: 'none',
        width: '100%',
        height: '38px',
        display: 'block',
        overflow: 'hidden'
      }}
    />
  );
};

// Simple editable div component that fully bypasses React's input handling
const EditableDiv = ({
  value,
  onChange,
  placeholder = '',
  className = ''
}: {
  value: string,
  onChange: (value: string) => void,
  placeholder?: string,
  className?: string
}) => {
  const divRef = useRef<HTMLDivElement>(null);
  const timeout = useRef<NodeJS.Timeout | null>(null);
  
  // Add CSS for editable div placeholders
  useEffect(() => {
    // Add CSS for editable div placeholders
    const style = document.createElement('style');
    style.innerHTML = `
      [contenteditable=true]:empty:before {
        content: attr(data-placeholder);
        color: #aaa;
        font-style: italic;
      }
      [contenteditable=true]:focus {
        outline: none;
        border-color: #3b82f6 !important;
        box-shadow: 0 0 0 1px #3b82f6;
      }
    `;
    document.head.appendChild(style);
    
    return () => {
      document.head.removeChild(style);
    };
  }, []);
  
  useEffect(() => {
    if (divRef.current && divRef.current.textContent !== value) {
      divRef.current.textContent = value;
    }
  }, [value]);
  
  const handleInput = () => {
    if (!divRef.current) return;
    
    const newValue = divRef.current.innerText;
    
    // Debounce the update
    if (timeout.current) {
      clearTimeout(timeout.current);
    }
    
    timeout.current = setTimeout(() => {
      onChange(newValue);
      timeout.current = null;
    }, 300);
  };
  
  return (
    <div
      ref={divRef}
      contentEditable
      className={className}
      style={{ 
        minHeight: '1.5rem',
        whiteSpace: 'nowrap',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        display: 'inline-block',
        backgroundColor: 'white',
        width: '100%'
      }}
      onInput={handleInput}
      data-placeholder={placeholder}
      suppressContentEditableWarning={true}
      dangerouslySetInnerHTML={{__html: value || ''}}
    />
  );
};

// Simple notification component
const Notification = ({ message, type, onClose }: { message: string, type: 'success' | 'error', onClose: () => void }) => {
  useEffect(() => {
    const timer = setTimeout(() => {
      onClose();
    }, 5000);
    
    return () => clearTimeout(timer);
  }, [onClose]);
  
  return (
    <div className={`fixed top-4 right-4 px-4 py-3 rounded shadow-lg ${type === 'success' ? 'bg-green-500' : 'bg-red-500'} text-white flex items-center`}>
      <span className="mr-2">
        {type === 'success' ? <i className="fas fa-check-circle" /> : <i className="fas fa-exclamation-circle" />}
      </span>
      <p>{message}</p>
      <button 
        onClick={onClose}
        className="ml-3 text-white hover:text-gray-200"
      >
        <i className="fas fa-times" />
      </button>
    </div>
  );
};

const App: React.FC = () => {
  // Add a ref for debouncing the updateItem API calls
  const updateItemTimeoutRef = React.useRef<NodeJS.Timeout | null>(null);
  // Add a ref for debouncing input field updates
  const debouncedUpdateRef = React.useRef<NodeJS.Timeout | null>(null);
  
  // Authentication state
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [isAdmin, setIsAdmin] = useState<boolean>(false);
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  
  // Application state
  const [groups, setGroups] = useState<ConfigurationGroup[]>([]);
  const [items, setItems] = useState<ConfigurationItem[]>([]);
  const [environments, setEnvironments] = useState<string[]>([]);
  const [selectedGroup, setSelectedGroup] = useState<ConfigurationGroup | null>(null);
  const [selectedEnvironment, setSelectedEnvironment] = useState('');
  const [createGroupName, setCreateGroupName] = useState('');
  const [createGroupDescription, setCreateGroupDescription] = useState('');
  
  // Notification state
  const [notification, setNotification] = useState<{ message: string, type: 'success' | 'error' } | null>(null);

  // Add this new state to track modified items and pending changes
  const [modifiedItems, setModifiedItems] = useState<{[key: number]: ConfigurationItem}>({});
  const [hasPendingChanges, setHasPendingChanges] = useState<boolean>(false);

  // Function to fetch all data
  const fetchData = () => {
    console.log('Fetching all data...');
    fetchGroups();
    fetchEnvironments();
    fetchItems();
  };

  // Load initial data and check auth on mount
  useEffect(() => {
    console.log('App mount useEffect running');
    checkAuth();
    fetchData();
  }, []);

  // Check authentication status
  const checkAuth = () => {
    console.log('Checking authentication state...');
    const user = authService.getCurrentUser();
    console.log('Current user from auth service:', user);
    const isLoggedIn = !!user;
    const isAdmin = authService.isAdmin();
    console.log('Authentication state:', { isLoggedIn, isAdmin });
    
    setCurrentUser(user);
    setIsAuthenticated(isLoggedIn);
    setIsAdmin(isAdmin);
  };
  
  // Clear any invalid auth data on load
  useEffect(() => {
    // Check if user data in localStorage is valid, clear if not
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      try {
        JSON.parse(storedUser);
      } catch (e) {
        console.error('Invalid user data in localStorage');
        localStorage.removeItem('user');
      }
    }
    
    checkAuth();
    
    // Add listener for storage events (for multi-tab logout)
    window.addEventListener('storage', (e) => {
      if (e.key === 'user' && !e.newValue) {
        checkAuth();
      }
    });
  }, []);
  
  // Load data when authentication status changes
  useEffect(() => {
    if (isAuthenticated) {
      fetchGroups();
      fetchEnvironments();
    }
  }, [isAuthenticated]);
  
  // Update items when selected group or environment changes
  useEffect(() => {
    if (selectedGroup && selectedEnvironment) {
      fetchItems();
    }
  }, [selectedGroup, selectedEnvironment]);

  const handleLoginSuccess = () => {
    console.log('Login success handler called');
    checkAuth();
    fetchData();
  };

  const handleLogout = () => {
    authService.logout();
    setIsAuthenticated(false);
    setIsAdmin(false);
    setCurrentUser(null);
  };

  // Fetch data from API
  const fetchGroups = async () => {
    try {
      console.log('Fetching groups...');
      const groups = await groupsApi.getAllGroups();
      console.log('Groups fetched successfully:', groups);
      setGroups(groups);
    } catch (error) {
      console.error('Error fetching groups:', error);
    }
  };

  const fetchEnvironments = async () => {
    try {
      console.log('Fetching environments...');
      const envs = await environmentsApi.getAllEnvironments();
      console.log('Environments fetched successfully:', envs);
      if (envs.length > 0) {
        setEnvironments(envs);
        setSelectedEnvironment(envs[0]);
      }
    } catch (error) {
      console.error('Error fetching environments:', error);
    }
  };

  const fetchItems = async () => {
    if (!selectedGroup || !selectedEnvironment) {
      setItems([]);
      return;
    }
    
    try {
      console.log('Fetching items for group:', selectedGroup.id, 'and environment:', selectedEnvironment);
      const items = await itemsApi.getItemsByGroupAndEnvironment(selectedGroup.id!, selectedEnvironment);
      console.log('Items fetched successfully:', items);
      setItems(items);
    } catch (error) {
      console.error('Error fetching items:', error);
      setItems([]);
    }
  };

  // Group actions
  const handleCreateGroup = async () => {
    if (!createGroupName) return;
    
    try {
      await groupsApi.createGroup({
        name: createGroupName,
        description: createGroupDescription
      });
      
      setCreateGroupName('');
      setCreateGroupDescription('');
      // Delay the fetchGroups call to prevent UI flicker
      setTimeout(() => {
        fetchGroups();
      }, 100);
      setNotification({ message: 'Group created successfully', type: 'success' });
    } catch (error) {
      console.error('Error creating group:', error);
      setNotification({ message: 'Failed to create group', type: 'error' });
    }
  };

  const handleDeleteGroup = async (id: number) => {
    // Show confirmation dialog
    if (!window.confirm('Are you sure you want to delete this group? This will also delete all configuration items in this group.')) {
      return;
    }
    
    try {
      await groupsApi.deleteGroup(id);
      // Delay the fetchGroups call to prevent UI flicker
      setTimeout(() => {
        fetchGroups();
        if (selectedGroup && selectedGroup.id === id) {
          setSelectedGroup(null);
          setItems([]);
        }
      }, 100);
      setNotification({ message: 'Group deleted successfully', type: 'success' });
    } catch (error) {
      console.error('Error deleting group:', error);
      setNotification({ message: 'Failed to delete group', type: 'error' });
    }
  };

  // Item actions
  const handleCreateItem = async (newItem: ConfigurationItem) => {
    try {
      await itemsApi.createItem(newItem);
      // Delay the fetchItems call to prevent UI flicker
      setTimeout(() => {
        fetchItems();
      }, 100);
      setNotification({ message: 'Configuration item created successfully', type: 'success' });
    } catch (error) {
      console.error('Error creating item:', error);
      setNotification({ message: 'Failed to create configuration item', type: 'error' });
      throw error;
    }
  };

  const handleDeleteItem = async (id: number) => {
    // Show confirmation dialog
    if (!window.confirm('Are you sure you want to delete this configuration item?')) {
      return;
    }
    
    try {
      await itemsApi.deleteItem(id);
      // Delay the fetchItems call to prevent UI flicker
      setTimeout(() => {
        fetchItems();
      }, 100);
      setNotification({ message: 'Configuration item deleted successfully', type: 'success' });
    } catch (error) {
      console.error('Error deleting item:', error);
      setNotification({ message: 'Failed to delete configuration item', type: 'error' });
    }
  };

  const handleUpdateItem = async (id: number, value: string) => {
    const item = items.find(i => i.id === id);
    if (!item) return;
    
    // Cancel any previous update for this item
    if (updateItemTimeoutRef.current) {
      clearTimeout(updateItemTimeoutRef.current);
    }
    
    // Immediately update local state for smooth UI
    setItems(prevItems => 
      prevItems.map(i => i.id === id ? { ...i, value } : i)
    );
    
    // Track modified items for batch save
    setModifiedItems(prev => ({
      ...prev,
      [id]: { ...item, value }
    }));
    setHasPendingChanges(true);
    
    // Debounce the API call to prevent too many requests
    updateItemTimeoutRef.current = setTimeout(async () => {
      try {
        const updatedItem = {
          ...item,
          value
        };
        
        await itemsApi.updateItem(id, updatedItem);
        console.log('Item updated successfully:', id);
        setNotification({ message: 'Configuration item updated successfully', type: 'success' });
        
        // Remove from modified items after successful update
        setModifiedItems(prev => {
          const newModified = { ...prev };
          delete newModified[id];
          return newModified;
        });
        
        // Check if there are still pending changes
        setHasPendingChanges(Object.keys(modifiedItems).length > 1); // > 1 because this item is still in the object
      } catch (error) {
        console.error('Error updating item:', error);
        // On error, revert to the original value
        setItems(prevItems => 
          prevItems.map(i => i.id === id ? item : i)
        );
        setNotification({ message: 'Failed to update configuration item', type: 'error' });
      }
    }, 500); // 500ms debounce
  };
  
  // Add a new function to save all modified items at once
  const handleSaveAllChanges = async () => {
    if (Object.keys(modifiedItems).length === 0) {
      return;
    }
    
    // Cancel any pending updateItem timeout
    if (updateItemTimeoutRef.current) {
      clearTimeout(updateItemTimeoutRef.current);
    }
    
    try {
      // Create an array of promises for all item updates
      const updatePromises = Object.values(modifiedItems).map(item => 
        itemsApi.updateItem(item.id!, item)
      );
      
      // Execute all update requests in parallel
      await Promise.all(updatePromises);
      
      // Clear modified items and refresh the list
      setModifiedItems({});
      setHasPendingChanges(false);
      fetchItems();
      
      setNotification({ 
        message: `Successfully saved ${updatePromises.length} configuration item(s)`, 
        type: 'success' 
      });
    } catch (error) {
      console.error('Error saving all changes:', error);
      setNotification({ 
        message: 'Failed to save all changes. Please try again.', 
        type: 'error' 
      });
    }
  };

  // Main dashboard component
  const ConfigDashboard = () => {
    return (
      <div className="min-h-screen bg-gray-100">
        <Header isAdmin={isAdmin} currentUser={currentUser} onLogout={handleLogout} />

        <div className="container mx-auto p-6">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
            {/* Left sidebar - Groups */}
            <div className="col-span-1 border bg-white p-4 rounded-lg shadow-sm">
              <h2 className="text-lg font-semibold mb-4 text-blue-700">Configuration Groups</h2>
              
              <ul className="mb-6 divide-y divide-gray-200">
                {groups.map(group => (
                  <li 
                    key={group.id} 
                    className={`py-2 px-3 cursor-pointer hover:bg-gray-100 rounded flex justify-between items-center ${selectedGroup?.id === group.id ? 'bg-blue-50 border-l-4 border-blue-500' : ''}`}
                    onClick={() => setSelectedGroup(group)}
                  >
                    <div>
                      <div className="font-medium">{group.name}</div>
                      <div className="text-sm text-gray-600">{group.description}</div>
                    </div>
                    {isAdmin && (
                      <button 
                        onClick={(e) => {
                          e.stopPropagation();
                          handleDeleteGroup(group.id!);
                        }}
                        className="text-red-500 hover:text-red-700 p-1 rounded-full hover:bg-red-100 transition-colors"
                        title="Delete group"
                      >
                        <i className="fas fa-trash-alt"></i>
                      </button>
                    )}
                  </li>
                ))}
              </ul>
              
              {isAdmin && (
                <div className="mt-4 p-3 border rounded-lg bg-gray-50">
                  <h3 className="text-md font-semibold mb-2 text-blue-700">Add New Group</h3>
                  <div className="mb-2">
                    <EditableDiv
                      value={createGroupName}
                      placeholder="Group Name"
                      className="w-full p-2 border rounded focus:ring-blue-500 focus:border-blue-500"
                      onChange={(newValue) => setCreateGroupName(newValue)}
                    />
                  </div>
                  <div className="mb-2">
                    <EditableDiv
                      value={createGroupDescription}
                      placeholder="Description"
                      className="w-full p-2 border rounded focus:ring-blue-500 focus:border-blue-500"
                      onChange={(newValue) => setCreateGroupDescription(newValue)}
                    />
                  </div>
                  <button
                    onClick={handleCreateGroup}
                    disabled={!createGroupName}
                    className={`w-full p-2 rounded text-white ${createGroupName ? 'bg-blue-600 hover:bg-blue-700' : 'bg-gray-300 cursor-not-allowed'}`}
                  >
                    Create Group
                  </button>
                </div>
              )}
            </div>

            {/* Main content - Items & Environment selector */}
            <div className="col-span-3 border bg-white p-4 rounded-lg shadow-sm">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-lg font-semibold text-blue-700">
                  {selectedGroup ? `${selectedGroup.name} Configuration` : 'Select a group'}
                </h2>
                
                <div className="flex items-center space-x-4">
                  {isAdmin && hasPendingChanges && (
                    <button
                      onClick={handleSaveAllChanges}
                      className="px-3 py-1 bg-green-600 text-white rounded-md hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-opacity-50 flex items-center"
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                      </svg>
                      Save All ({Object.keys(modifiedItems).length})
                    </button>
                  )}
                  <div className="flex items-center">
                    <span className="mr-2">Environment:</span>
                    <select 
                      className="border rounded p-1 text-gray-700 focus:ring-blue-500 focus:border-blue-500"
                      value={selectedEnvironment}
                      onChange={(e) => setSelectedEnvironment(e.target.value)}
                    >
                      {environments.map(env => (
                        <option key={env} value={env}>{env}</option>
                      ))}
                    </select>
                  </div>
                </div>
              </div>

              {selectedGroup ? (
                <>
                  <div className="overflow-x-auto">
                    <table className="min-w-full bg-white border-collapse">
                      <thead>
                        <tr className="bg-gray-100 border-b">
                          <th className="text-left py-2 px-4 w-1/3 text-gray-700">Key</th>
                          <th className="text-left py-2 px-4 w-2/3 text-gray-700">Value</th>
                          {isAdmin && <th className="py-2 px-4 w-24 text-gray-700">Actions</th>}
                        </tr>
                      </thead>
                      <tbody>
                        {items.map(item => (
                          <tr key={item.id} className="border-b hover:bg-gray-50">
                            <td className="py-2 px-4">{item.key}</td>
                            <td className="py-2 px-4">
                              {isAdmin ? (
                                <SimpleInput
                                  value={item.value}
                                  className="w-full p-1 border rounded focus:ring-blue-500 focus:border-blue-500"
                                  onChange={(newValue: string) => handleUpdateItem(item.id!, newValue)}
                                />
                              ) : (
                                item.value
                              )}
                            </td>
                            {isAdmin && (
                              <td className="py-2 px-4 text-center">
                                <button
                                  onClick={() => handleDeleteItem(item.id!)}
                                  className="text-red-500 hover:text-red-700 p-1 rounded-full hover:bg-red-100 transition-colors"
                                  title="Delete configuration item"
                                >
                                  <i className="fas fa-trash-alt"></i>
                                </button>
                              </td>
                            )}
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>

                  {isAdmin && (
                    <div className="mt-6 p-3 border rounded-lg bg-gray-50">
                      <ConfigItemForm 
                        groupId={String(selectedGroup.id!)}
                        environments={environments}
                        onSave={handleCreateItem}
                        onCancel={() => {}} // Empty function as there's no cancel needed here
                      />
                    </div>
                  )}
                </>
              ) : (
                <div className="text-center py-8 text-gray-500">
                  Please select a configuration group from the sidebar
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    );
  };

  // User Management Page
  const UserManagementPage = () => {
    return (
      <div className="min-h-screen bg-gray-100">
        <Header isAdmin={isAdmin} currentUser={currentUser} onLogout={handleLogout} />
        
        <div className="container mx-auto p-6">
          <div className="bg-white rounded-lg shadow-sm p-6">
            <h1 className="text-2xl font-semibold text-blue-700 mb-6">User Management</h1>
            <UserManagement isAdmin={isAdmin} />
          </div>
        </div>
      </div>
    );
  };

  return (
    <Router>
      {notification && (
        <Notification 
          message={notification.message} 
          type={notification.type} 
          onClose={() => setNotification(null)} 
        />
      )}
      <Routes>
        <Route path="/login" element={
          isAuthenticated ? <Navigate to="/" /> : <Login onLoginSuccess={handleLoginSuccess} />
        } />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/reset-password" element={<ResetPasswordWrapper />} />
        <Route path="/set-password" element={<ResetPasswordWrapper isSetPassword={true} />} />
        <Route path="/users" element={
          isAuthenticated && isAdmin ? <UserManagementPage /> : <Navigate to="/" />
        } />
        <Route path="/" element={
          isAuthenticated ? <ConfigDashboard /> : <Navigate to="/login" />
        } />
      </Routes>
    </Router>
  );
};

export default App;