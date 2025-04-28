import React, { useState } from 'react';
import { groupsApi } from '../services/api';

interface ConfigGroupProps {
  group: {
    id?: string | number;
    name: string;
    description: string;
  };
  isSelected: boolean;
  onSelect: (group: any) => void;
  onEdit: (group: any) => void;
  onDelete: (group: any) => void;
}

const ConfigGroup: React.FC<ConfigGroupProps> = ({
  group,
  isSelected,
  onSelect,
  onEdit,
  onDelete
}) => {
  const [isHovered, setIsHovered] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleClick = () => {
    onSelect(group);
  };

  const handleEditClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    onEdit(group);
  };

  const handleDeleteClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    setShowDeleteConfirm(true);
  };

  const handleDeleteConfirm = async () => {
    try {
      setIsDeleting(true);
      setError(null);
      
      if (group.id) {
        await groupsApi.deleteGroup(Number(group.id));
        onDelete(group);
      }
    } catch (err) {
      setError("Failed to delete group. Please try again.");
      console.error("Delete group error:", err);
    } finally {
      setIsDeleting(false);
      setShowDeleteConfirm(false);
    }
  };

  const handleDeleteCancel = () => {
    setShowDeleteConfirm(false);
  };

  return (
    <div
      data-testid={`group-${group.id}`}
      className={`p-3 border-b hover:bg-gray-50 cursor-pointer relative ${
        isSelected ? 'bg-blue-100' : ''
      }`}
      onClick={handleClick}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
    >
      <div className="flex justify-between items-start">
        <div>
          <h3 className="font-medium text-gray-800">{group.name}</h3>
          <p className="text-sm text-gray-600">{group.description}</p>
        </div>
        
        <div className="flex space-x-2">
          <button
            aria-label="Edit"
            className={`text-blue-500 hover:text-blue-700 transition-opacity ${
              isHovered ? 'opacity-100' : 'opacity-0'
            }`}
            onClick={handleEditClick}
          >
            Edit
          </button>
          
          <button
            aria-label="Delete"
            className={`text-red-500 hover:text-red-700 transition-opacity ${
              isHovered ? 'opacity-100' : 'opacity-0'
            }`}
            onClick={handleDeleteClick}
          >
            Delete
          </button>
        </div>
      </div>

      {/* Delete confirmation dialog */}
      {showDeleteConfirm && (
        <div className="absolute top-0 left-0 w-full h-full bg-white p-3 z-10">
          <p className="font-medium text-gray-800">Are you sure you want to delete <span className="font-bold">{group.name}</span>?</p>
          <p className="text-sm text-gray-600 mb-3">This action cannot be undone.</p>
          
          {error && <p className="text-red-500 text-sm mb-2">{error}</p>}
          
          <div className="flex space-x-2">
            <button
              className="px-3 py-1 bg-red-500 text-white rounded hover:bg-red-600 disabled:opacity-50"
              onClick={handleDeleteConfirm}
              disabled={isDeleting}
            >
              {isDeleting ? 'Deleting...' : 'Confirm'}
            </button>
            
            <button
              className="px-3 py-1 bg-gray-200 text-gray-800 rounded hover:bg-gray-300"
              onClick={handleDeleteCancel}
              disabled={isDeleting}
            >
              Cancel
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default ConfigGroup; 