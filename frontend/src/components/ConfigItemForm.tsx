import React, { useState, useEffect, useRef } from 'react';
import { ConfigurationItem, itemsApi } from '../services/api';
import SimpleInput from './SimpleInput';

interface ConfigItemFormProps {
  item?: ConfigurationItem;
  groupId: string;
  onSave: (item: ConfigurationItem) => void;
  onCancel: () => void;
  environments: string[];
}

// Extend ConfigurationItem for our form (description is not in the original interface)
interface FormConfigurationItem extends ConfigurationItem {
  description?: string;
}

const ConfigItemForm: React.FC<ConfigItemFormProps> = ({
  item,
  groupId,
  onSave,
  onCancel,
  environments
}) => {
  // Split state to prevent unnecessary re-renders of all form fields
  const [key, setKey] = useState(item?.key || '');
  const [value, setValue] = useState(item?.value || '');
  const [description, setDescription] = useState('');
  const [environment, setEnvironment] = useState(item?.environment || environments[0] || '');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [validation, setValidation] = useState({ key: true, value: true, environment: true });
  
  // Track submitted status to avoid state updates after unmounting
  const isSubmittedRef = useRef(false);
  
  // Update if item props change externally
  useEffect(() => {
    if (item) {
      setKey(item.key || '');
      setValue(item.value || '');
      setEnvironment(item.environment || environments[0] || '');
    }
  }, [item, environments]);

  const isEdit = !!item;

  const validateForm = () => {
    const newValidation = {
      key: key.trim() !== '',
      value: value.trim() !== '',
      environment: environment !== ''
    };
    setValidation(newValidation);
    return Object.values(newValidation).every(valid => valid);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    setError('');
    setIsLoading(true);
    isSubmittedRef.current = true;

    try {
      let savedItem;
      
      const itemData: ConfigurationItem = {
        key,
        value,
        environment,
        groupId: parseInt(groupId, 10),
        groupName: 'temp' // The backend will replace this
      };
      
      if (isEdit && item?.id) {
        savedItem = await itemsApi.updateItem(item.id, {
          ...itemData,
          id: item.id
        });
      } else {
        savedItem = await itemsApi.createItem(itemData);
      }
      
      onSave(savedItem);
      
      // Only clear form if component is still mounted and it's not an edit
      if (!isEdit && !isSubmittedRef.current) {
        setKey('');
        setValue('');
        setDescription('');
      }
    } catch (err) {
      console.error('Error saving item:', err);
      if (!isSubmittedRef.current) {
        setError('Failed to save configuration item. Please try again.');
      }
    } finally {
      if (!isSubmittedRef.current) {
        setIsLoading(false);
      }
    }
  };

  useEffect(() => {
    return () => {
      // Mark component as unmounted to prevent state updates
      isSubmittedRef.current = true;
    };
  }, []);

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <h2 className="text-xl font-semibold mb-4">
        {isEdit ? 'Edit Configuration Item' : 'Add New Configuration Item'}
      </h2>
      
      <div>
        <label className="block text-sm font-medium text-gray-700">Environment</label>
        <select
          value={environment}
          onChange={(e) => setEnvironment(e.target.value)}
          className={`mt-1 block w-full p-2 border rounded ${
            !validation.environment ? 'border-red-500' : 'border-gray-300'
          }`}
          disabled={isEdit}
        >
          <option value="">Select Environment</option>
          {environments.map(env => (
            <option key={env} value={env}>{env}</option>
          ))}
        </select>
        {!validation.environment && (
          <p className="text-red-500 text-xs mt-1">Environment is required</p>
        )}
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700">Key</label>
        <SimpleInput
          value={key}
          onChange={setKey}
          placeholder="Enter configuration key"
          className={`mt-1 block w-full p-2 border rounded ${
            !validation.key ? 'border-red-500' : 'border-gray-300'
          }`}
        />
        {!validation.key && (
          <p className="text-red-500 text-xs mt-1">Key is required</p>
        )}
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700">Value</label>
        <SimpleInput
          value={value}
          onChange={setValue}
          placeholder="Enter configuration value"
          className={`mt-1 block w-full p-2 border rounded ${
            !validation.value ? 'border-red-500' : 'border-gray-300'
          }`}
          isTextarea={true}
        />
        {!validation.value && (
          <p className="text-red-500 text-xs mt-1">Value is required</p>
        )}
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700">Description (optional)</label>
        <SimpleInput
          value={description}
          onChange={setDescription}
          placeholder="Enter description"
          className="mt-1 block w-full p-2 border rounded border-gray-300"
          isTextarea={true}
        />
      </div>

      {error && (
        <div className="text-red-500 text-sm py-2">{error}</div>
      )}

      <div className="flex justify-end space-x-2 pt-2">
        <button
          type="button"
          onClick={onCancel}
          className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md shadow-sm hover:bg-gray-50"
          disabled={isLoading}
        >
          Cancel
        </button>
        <button
          type="submit"
          className="px-4 py-2 text-sm font-medium text-white bg-blue-600 border border-transparent rounded-md shadow-sm hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
          disabled={isLoading}
        >
          {isLoading ? 'Saving...' : isEdit ? 'Update' : 'Create'}
        </button>
      </div>
    </form>
  );
};

export default ConfigItemForm; 