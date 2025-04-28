import React, { useEffect, useRef } from 'react';

interface SimpleInputProps {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  className?: string;
  isTextarea?: boolean;
}

const SimpleInput: React.FC<SimpleInputProps> = ({
  value,
  onChange,
  placeholder = '',
  className = '',
  isTextarea = false
}) => {
  // Create separate refs for each input type
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);
  
  // Track previous value to detect external changes
  const prevValueRef = useRef(value);
  
  // Get the current input element ref based on type
  const getInputEl = () => isTextarea ? textareaRef.current : inputRef.current;
  
  // Only update the input value if it's changed externally (not from user input)
  useEffect(() => {
    const input = getInputEl();
    if (input && value !== prevValueRef.current && document.activeElement !== input) {
      // Only update if the input is not focused
      input.value = value;
    }
    prevValueRef.current = value;
  }, [value]);

  const handleChange = () => {
    const input = getInputEl();
    if (!input) return;
    
    const newValue = input.value;
    prevValueRef.current = newValue;
    onChange(newValue);
  };

  const commonProps = {
    defaultValue: value,
    onChange: handleChange,
    onBlur: handleChange, // Also update on blur for good measure
    placeholder,
    className: `${className} w-full p-2 border rounded focus:ring-blue-500 focus:border-blue-500`
  };

  return isTextarea ? (
    <textarea 
      ref={textareaRef}
      {...commonProps}
      rows={3}
    />
  ) : (
    <input 
      ref={inputRef}
      type="text"
      {...commonProps}
    />
  );
};

export default SimpleInput; 