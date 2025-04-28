import React, { useState, useEffect } from 'react';
import ConfigGroup from './ConfigGroup';
import { groupsApi } from '../services/api';

interface ConfigGroupListProps {
  onGroupSelect: (group: any) => void;
  selectedGroup?: any;
}

const ConfigGroupList: React.FC<ConfigGroupListProps> = ({ 
  onGroupSelect, 
  selectedGroup 
}) => {
  const [groups, setGroups] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [editingGroup, setEditingGroup] = useState<any | null>(null);
  const [newGroupName, setNewGroupName] = useState('');
  const [newGroupDescription, setNewGroupDescription] = useState('');
  
  // Load groups on component mount
  useEffect(() => {
    fetchGroups();
  }, []);
  
  const fetchGroups = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await groupsApi.getAllGroups();
      setGroups(data);
    } catch (err) {
      console.error('Error fetching groups:', err);
      setError('Error loading groups. Please try again.');
    } finally {
      setLoading(false);
    }
  };
  
  const handleGroupSelect = (group: any) => {
    onGroupSelect(group);
  };
  
  const handleEditGroup = (group: any) => {
    setEditingGroup(group);
    // Pre-fill form with current values
    setNewGroupName(group.name);
    setNewGroupDescription(group.description);
  };
  
  const handleDeleteGroup = (group: any) => {
    // Just refresh the list after deletion
    fetchGroups();
  };
  
  const handleCreateGroup = async () => {
    if (!newGroupName) return;
    
    try {
      setLoading(true);
      setError(null);
      
      if (editingGroup) {
        // Update existing group
        await groupsApi.updateGroup(editingGroup.id, {
          ...editingGroup,
          name: newGroupName,
          description: newGroupDescription
        });
      } else {
        // Create new group
        await groupsApi.createGroup({
          name: newGroupName,
          description: newGroupDescription
        });
      }
      
      // Reset form and refresh list
      setNewGroupName('');
      setNewGroupDescription('');
      setEditingGroup(null);
      fetchGroups();
    } catch (err) {
      console.error('Error saving group:', err);
      setError('Error saving group. Please try again.');
    } finally {
      setLoading(false);
    }
  };
  
  const handleCancelEdit = () => {
    setEditingGroup(null);
    setNewGroupName('');
    setNewGroupDescription('');
  };
  
  if (loading && groups.length === 0) {
    return <div className="p-4 text-center">Loading groups...</div>;
  }
  
  if (error) {
    return (
      <div className="p-4 text-center text-red-500">
        {error}
        <button 
          className="block mx-auto mt-2 px-3 py-1 bg-blue-500 text-white rounded"
          onClick={fetchGroups}
        >
          Retry
        </button>
      </div>
    );
  }
  
  return (
    <div className="bg-white shadow-sm rounded-lg">
      <h2 className="text-lg font-semibold p-4 border-b">Configuration Groups</h2>
      
      {groups.length === 0 ? (
        <div className="p-4 text-center text-gray-500">
          No configuration groups found
        </div>
      ) : (
        <div className="divide-y">
          {groups.map(group => (
            <ConfigGroup
              key={group.id}
              group={group}
              isSelected={selectedGroup?.id === group.id}
              onSelect={handleGroupSelect}
              onEdit={handleEditGroup}
              onDelete={handleDeleteGroup}
            />
          ))}
        </div>
      )}
      
      {/* Group form */}
      <div className="p-4 border-t">
        <h3 className="font-medium mb-2">
          {editingGroup ? 'Edit Group' : 'Add New Group'}
        </h3>
        
        <div className="mb-2">
          <input
            type="text"
            placeholder="Group Name"
            className="w-full p-2 border rounded"
            value={newGroupName}
            onChange={(e) => setNewGroupName(e.target.value)}
          />
        </div>
        
        <div className="mb-3">
          <input
            type="text"
            placeholder="Description"
            className="w-full p-2 border rounded"
            value={newGroupDescription}
            onChange={(e) => setNewGroupDescription(e.target.value)}
          />
        </div>
        
        <div className="flex space-x-2">
          <button
            className={`px-3 py-2 rounded text-white ${
              newGroupName ? 'bg-blue-500 hover:bg-blue-600' : 'bg-gray-300 cursor-not-allowed'
            }`}
            onClick={handleCreateGroup}
            disabled={!newGroupName || loading}
          >
            {loading ? 'Saving...' : editingGroup ? 'Update' : 'Create'}
          </button>
          
          {editingGroup && (
            <button
              className="px-3 py-2 bg-gray-200 text-gray-800 rounded hover:bg-gray-300"
              onClick={handleCancelEdit}
            >
              Cancel
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default ConfigGroupList; 