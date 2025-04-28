import React, { useState, useEffect } from 'react';

interface ResetPasswordProps {
  token: string;
  isSetPassword?: boolean;
}

const ResetPassword: React.FC<ResetPasswordProps> = ({ token, isSetPassword = false }) => {
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [validatingToken, setValidatingToken] = useState(true);
  const [tokenValid, setTokenValid] = useState(false);

  useEffect(() => {
    const validateToken = async () => {
      try {
        const response = await fetch(`/api/users/validate-token?token=${token}`);
        const data = await response.json();
        
        setTokenValid(data.valid);
      } catch (err) {
        setTokenValid(false);
        console.error(err);
      } finally {
        setValidatingToken(false);
      }
    };

    validateToken();
  }, [token]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (password !== confirmPassword) {
      setError('Passwords do not match');
      return;
    }
    
    setLoading(true);
    setMessage('');
    setError('');

    try {
      const response = await fetch(`/api/users/reset-password?token=${token}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ password, confirmPassword })
      });

      const data = await response.json();

      if (response.ok) {
        setMessage(data.message || 'Password has been reset successfully');
        setPassword('');
        setConfirmPassword('');
      } else {
        setError(data.error || 'Failed to reset password');
      }
    } catch (err) {
      setError('An error occurred while resetting the password');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  if (validatingToken) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
        <div className="max-w-md w-full space-y-8 text-center">
          <div>
            <h2 className="text-center text-3xl font-extrabold text-gray-900">
              {isSetPassword ? 'Set Password' : 'Reset Password'}
            </h2>
            <p className="mt-2 text-center text-sm text-gray-600">
              Validating your token...
            </p>
          </div>
        </div>
      </div>
    );
  }

  if (!tokenValid) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
        <div className="max-w-md w-full space-y-8">
          <div>
            <h2 className="text-center text-3xl font-extrabold text-gray-900">
              Invalid or Expired Token
            </h2>
            <p className="mt-2 text-center text-sm text-gray-600">
              The password reset link is invalid or has expired. Please request a new one.
            </p>
          </div>
          <div className="text-center mt-4">
            <a href="/forgot-password" className="font-medium text-blue-600 hover:text-blue-500">
              Request New Link
            </a>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
            {isSetPassword ? 'Set Your Password' : 'Reset Your Password'}
          </h2>
          <p className="mt-2 text-center text-sm text-gray-600">
            {isSetPassword
              ? 'Create a strong password for your account'
              : 'Enter your new password below'}
          </p>
        </div>
        
        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          <div className="rounded-md shadow-sm -space-y-px">
            <div>
              <label htmlFor="password" className="sr-only">
                Password
              </label>
              <input
                id="password"
                name="password"
                type="password"
                autoComplete="new-password"
                required
                className="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-t-md focus:outline-none focus:ring-blue-500 focus:border-blue-500 focus:z-10 sm:text-sm"
                placeholder="Password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                minLength={8}
              />
            </div>
            <div>
              <label htmlFor="confirm-password" className="sr-only">
                Confirm Password
              </label>
              <input
                id="confirm-password"
                name="confirmPassword"
                type="password"
                autoComplete="new-password"
                required
                className="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-b-md focus:outline-none focus:ring-blue-500 focus:border-blue-500 focus:z-10 sm:text-sm"
                placeholder="Confirm Password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                minLength={8}
              />
            </div>
          </div>

          <div>
            <button
              type="submit"
              disabled={loading}
              className="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              {loading
                ? (isSetPassword ? 'Setting...' : 'Resetting...')
                : (isSetPassword ? 'Set Password' : 'Reset Password')}
            </button>
          </div>
          
          {message && (
            <div className="p-3 bg-green-100 text-green-700 rounded-md">
              {message}
              <div className="mt-2">
                <a href="/" className="font-medium text-blue-600 hover:text-blue-500">
                  Go to Login
                </a>
              </div>
            </div>
          )}
          
          {error && (
            <div className="p-3 bg-red-100 text-red-700 rounded-md">
              {error}
            </div>
          )}
          
          {!message && (
            <div className="text-center">
              <a href="/" className="font-medium text-blue-600 hover:text-blue-500">
                Back to Login
              </a>
            </div>
          )}
        </form>
      </div>
    </div>
  );
};

export default ResetPassword; 