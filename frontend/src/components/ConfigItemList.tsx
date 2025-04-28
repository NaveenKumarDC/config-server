import React, { useState, useEffect } from 'react';
import { itemsApi, environmentsApi } from '../services/api';

interface ConfigItemListProps {
  group: {
    id: string | number;
    name: string;
  };
  onEditItem: (item: any) => void;
}

const ConfigItemList: React.FC<ConfigItemListProps> = ({ group, onEditItem }) => {
  const [items, setItems] = useState<any[]>([]);
  const [environments, setEnvironments] = useState<string[]>([]);
  const [selectedEnvironment, setSelectedEnvironment] = useState<string>('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // Load environments on mount
  useEffect(() => {
    const fetchEnvironments = async () => {
      try {
        const data = await environmentsApi.getAllEnvironments();
        setEnvironments(data);
        if (data.length > 0) {
          setSelectedEnvironment(data[0]);
        }
      } catch (err) {
        console.error('Error fetching environments:', err);
      }
    };
    
    fetchEnvironments();
  }, []);
  
  // Load items when group or environment changes
  useEffect(() => {
    if (!group?.id) return;
    
    const fetchItems = async () => {
      try {
        setLoading(true);
        setError(null);
        
        let data;
        if (selectedEnvironment) {
          data = await itemsApi.getItemsByGroupAndEnvironment(Number(group.id), selectedEnvironment);
        } else {
          data = await itemsApi.getItemsByGroup(Number(group.id));
        }
        
        setItems(data);
      } catch (err) {
        console.error('Error fetching items:', err);
        setError('Error loading items. Please try again.');
      } finally {
        setLoading(false);
      }
    };
    
    fetchItems();
  }, [group?.id, selectedEnvironment]);
  
  const handleEnvironmentChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setSelectedEnvironment(e.target.value);
  };
  
  const handleEditItem = (item: any) => {
    onEditItem(item);
  };
  
  const handleDeleteItem = async (itemId: string | number) => {
    if (!confirm('Are you sure you want to delete this item?')) {
      return;
    }
    
    try {
      await itemsApi.deleteItem(Number(itemId));
      
      // Refresh items
      setItems(items.filter(item => item.id !== itemId));
    } catch (err) {
      console.error('Error deleting item:', err);
      alert('Failed to delete item. Please try again.');
    }
  };
  
  if (loading && items.length === 0) {
    return <div className="p-4 text-center">Loading...</div>;
  }
  
  if (error) {
    return <div className="p-4 text-center text-red-500">{error}</div>;
  }
  
  return (
    <div className="bg-white shadow-sm rounded-lg">
      <div className="p-4 border-b flex justify-between items-center">
        <h2 className="text-lg font-semibold">{group.name} Items</h2>
        
        <div className="flex items-center">
          <label htmlFor="environment" className="mr-2">Environment:</label>
          <select
            id="environment"
            className="border rounded p-1"
            value={selectedEnvironment}
            onChange={handleEnvironmentChange}
            aria-label="Environment"
          >
            {environments.map(env => (
              <option key={env} value={env}>{env}</option>
            ))}
          </select>
        </div>
      </div>
      
      {items.length === 0 ? (
        <div className="p-4 text-center text-gray-500">
          No configuration items found for this group
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead>
              <tr className="bg-gray-50">
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Key</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Value</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Description</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {items.map(item => (
                <tr key={item.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{item.key}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{item.value}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{item.description}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 space-x-2">
                    <button
                      onClick={() => handleEditItem(item)}
                      className="text-blue-500 hover:text-blue-700"
                      aria-label="Edit"
                    >
                      Edit
                    </button>
                    <button
                      onClick={() => handleDeleteItem(item.id)}
                      className="text-red-500 hover:text-red-700"
                      aria-label="Delete"
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default ConfigItemList; 