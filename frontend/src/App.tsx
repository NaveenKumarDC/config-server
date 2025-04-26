import React, { useEffect, useState } from 'react';
import { ConfigurationGroup, ConfigurationItem, groupsApi, itemsApi, environmentsApi } from './services/api';

function App() {
  // State
  const [groups, setGroups] = useState<ConfigurationGroup[]>([]);
  const [items, setItems] = useState<ConfigurationItem[]>([]);
  const [environments, setEnvironments] = useState<string[]>([]);
  const [selectedGroup, setSelectedGroup] = useState<ConfigurationGroup | null>(null);
  const [selectedEnvironment, setSelectedEnvironment] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  
  // Form state
  const [newGroup, setNewGroup] = useState<ConfigurationGroup>({ name: '', description: '' });
  const [newItem, setNewItem] = useState<ConfigurationItem>({ 
    key: '', 
    value: '', 
    environment: '', 
    groupId: -1 
  });
  
  // Load data
  useEffect(() => {
    const loadData = async () => {
      try {
        setLoading(true);
        const [groupsResponse, environmentsResponse] = await Promise.all([
          groupsApi.getAll(),
          environmentsApi.getAll()
        ]);
        
        setGroups(groupsResponse.data);
        setEnvironments(environmentsResponse.data);
        
        if (groupsResponse.data.length > 0) {
          setSelectedGroup(groupsResponse.data[0]);
          
          if (environmentsResponse.data.length > 0) {
            setSelectedEnvironment(environmentsResponse.data[0]);
            const itemsResponse = await itemsApi.getByGroupAndEnvironment(
              groupsResponse.data[0].id!,
              environmentsResponse.data[0]
            );
            setItems(itemsResponse.data);
          }
        }
        
        setError(null);
      } catch (err) {
        setError('Failed to load data. Please try again.');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    
    loadData();
  }, []);
  
  // Load items when group or environment changes
  useEffect(() => {
    const loadItems = async () => {
      if (!selectedGroup || !selectedEnvironment) return;
      
      try {
        setLoading(true);
        const response = await itemsApi.getByGroupAndEnvironment(selectedGroup.id!, selectedEnvironment);
        setItems(response.data);
        setError(null);
      } catch (err) {
        setError('Failed to load items. Please try again.');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    
    loadItems();
  }, [selectedGroup, selectedEnvironment]);
  
  // Event handlers
  const handleGroupChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const groupId = parseInt(e.target.value);
    const group = groups.find(g => g.id === groupId) || null;
    setSelectedGroup(group);
    
    // Reset new item form
    if (group) {
      setNewItem(prev => ({ ...prev, groupId: group.id! }));
    }
  };
  
  const handleEnvironmentChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setSelectedEnvironment(e.target.value);
    
    // Reset new item form
    setNewItem(prev => ({ ...prev, environment: e.target.value }));
  };
  
  const handleCreateGroup = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      setLoading(true);
      const response = await groupsApi.create(newGroup);
      setGroups(prev => [...prev, response.data]);
      setNewGroup({ name: '', description: '' });
      setError(null);
    } catch (err) {
      setError('Failed to create group. Please try again.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };
  
  const handleCreateItem = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      setLoading(true);
      
      // Ensure the item has the correct group and environment
      const itemToCreate = {
        ...newItem,
        groupId: selectedGroup?.id || newItem.groupId,
        environment: newItem.environment || selectedEnvironment
      };
      
      const response = await itemsApi.create(itemToCreate);
      setItems(prev => [...prev, response.data]);
      setNewItem({ key: '', value: '', environment: selectedEnvironment, groupId: selectedGroup?.id || -1 });
      setError(null);
    } catch (err) {
      setError('Failed to create item. Please try again.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };
  
  const handleDeleteItem = async (itemId: number) => {
    try {
      setLoading(true);
      await itemsApi.delete(itemId);
      setItems(prev => prev.filter(item => item.id !== itemId));
      setError(null);
    } catch (err) {
      setError('Failed to delete item. Please try again.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <div className="min-h-screen bg-gray-100">
      <nav className="bg-blue-600 text-white p-4">
        <div className="container mx-auto">
          <h1 className="text-2xl font-bold">Configuration Server</h1>
        </div>
      </nav>
      
      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded container mx-auto mt-4">
          <p>{error}</p>
        </div>
      )}
      
      <main className="container mx-auto p-4">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {/* Left sidebar - Groups */}
          <div className="bg-white shadow rounded-lg p-6">
            <h2 className="text-xl font-semibold mb-4">Configuration Groups</h2>
            
            <div className="mb-6">
              <label className="block text-gray-700 mb-2">Select Group:</label>
              <select 
                className="w-full p-2 border rounded"
                value={selectedGroup?.id || ''}
                onChange={handleGroupChange}
                disabled={loading || groups.length === 0}
              >
                {groups.map(group => (
                  <option key={group.id} value={group.id}>
                    {group.name}
                  </option>
                ))}
              </select>
            </div>
            
            <div className="mb-6">
              <label className="block text-gray-700 mb-2">Select Environment:</label>
              <select 
                className="w-full p-2 border rounded"
                value={selectedEnvironment}
                onChange={handleEnvironmentChange}
                disabled={loading || environments.length === 0}
              >
                {environments.map(env => (
                  <option key={env} value={env}>
                    {env}
                  </option>
                ))}
              </select>
            </div>
            
            <hr className="my-4" />
            
            <h3 className="text-lg font-semibold mb-2">Add New Group</h3>
            <form onSubmit={handleCreateGroup}>
              <div className="mb-4">
                <label className="block text-gray-700 mb-1">Name:</label>
                <input 
                  type="text" 
                  className="w-full p-2 border rounded"
                  value={newGroup.name}
                  onChange={e => setNewGroup(prev => ({ ...prev, name: e.target.value }))}
                  required
                />
              </div>
              
              <div className="mb-4">
                <label className="block text-gray-700 mb-1">Description:</label>
                <textarea 
                  className="w-full p-2 border rounded"
                  value={newGroup.description}
                  onChange={e => setNewGroup(prev => ({ ...prev, description: e.target.value }))}
                  rows={3}
                />
              </div>
              
              <button 
                type="submit" 
                className="bg-blue-500 text-white py-2 px-4 rounded hover:bg-blue-600 disabled:bg-blue-300"
                disabled={loading || !newGroup.name}
              >
                Create Group
              </button>
            </form>
          </div>
          
          {/* Middle - Configuration Items */}
          <div className="bg-white shadow rounded-lg p-6 md:col-span-2">
            <h2 className="text-xl font-semibold mb-4">
              Configuration Items
              {selectedGroup && selectedEnvironment && (
                <span className="font-normal text-gray-600 text-sm ml-2">
                  ({selectedGroup.name} / {selectedEnvironment})
                </span>
              )}
            </h2>
            
            {loading ? (
              <p className="text-gray-500">Loading...</p>
            ) : items.length > 0 ? (
              <div className="overflow-x-auto">
                <table className="min-w-full bg-white">
                  <thead>
                    <tr>
                      <th className="py-2 px-4 border-b border-gray-200 text-left text-sm font-semibold text-gray-700">Key</th>
                      <th className="py-2 px-4 border-b border-gray-200 text-left text-sm font-semibold text-gray-700">Value</th>
                      <th className="py-2 px-4 border-b border-gray-200 text-left text-sm font-semibold text-gray-700">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {items.map(item => (
                      <tr key={item.id}>
                        <td className="py-2 px-4 border-b border-gray-200">{item.key}</td>
                        <td className="py-2 px-4 border-b border-gray-200 overflow-hidden text-ellipsis">{item.value}</td>
                        <td className="py-2 px-4 border-b border-gray-200">
                          <button 
                            onClick={() => handleDeleteItem(item.id!)}
                            className="text-red-500 hover:text-red-700"
                            disabled={loading}
                          >
                            Delete
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <p className="text-gray-500">No configuration items found for this group and environment.</p>
            )}
            
            <hr className="my-6" />
            
            <h3 className="text-lg font-semibold mb-2">Add New Configuration Item</h3>
            <form onSubmit={handleCreateItem}>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                <div>
                  <label className="block text-gray-700 mb-1">Key:</label>
                  <input 
                    type="text" 
                    className="w-full p-2 border rounded"
                    value={newItem.key}
                    onChange={e => setNewItem(prev => ({ ...prev, key: e.target.value }))}
                    required
                  />
                </div>
                
                <div>
                  <label className="block text-gray-700 mb-1">Value:</label>
                  <input 
                    type="text" 
                    className="w-full p-2 border rounded"
                    value={newItem.value}
                    onChange={e => setNewItem(prev => ({ ...prev, value: e.target.value }))}
                    required
                  />
                </div>
              </div>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                <div>
                  <label className="block text-gray-700 mb-1">Group:</label>
                  <select 
                    className="w-full p-2 border rounded"
                    value={newItem.groupId || selectedGroup?.id || ''}
                    onChange={e => setNewItem(prev => ({ ...prev, groupId: parseInt(e.target.value) }))}
                    required
                  >
                    {groups.map(group => (
                      <option key={group.id} value={group.id}>
                        {group.name}
                      </option>
                    ))}
                  </select>
                </div>
                
                <div>
                  <label className="block text-gray-700 mb-1">Environment:</label>
                  <select 
                    className="w-full p-2 border rounded"
                    value={newItem.environment || selectedEnvironment}
                    onChange={e => setNewItem(prev => ({ ...prev, environment: e.target.value }))}
                    required
                  >
                    {environments.map(env => (
                      <option key={env} value={env}>
                        {env}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
              
              <button 
                type="submit" 
                className="bg-green-500 text-white py-2 px-4 rounded hover:bg-green-600 disabled:bg-green-300"
                disabled={loading || !newItem.key || !newItem.value || !newItem.groupId || !newItem.environment}
              >
                Add Configuration
              </button>
            </form>
          </div>
        </div>
      </main>
    </div>
  );
}

export default App;