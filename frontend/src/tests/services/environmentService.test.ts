import { environmentsApi } from '../../services/api';
import axios from 'axios';
import MockAdapter from 'axios-mock-adapter';

// Setup axios mock
const mockAxios = new MockAdapter(axios);

describe('Environment API Service', () => {
  const mockEnvironments = ['DEV', 'STAGE', 'PROD'];
  
  beforeEach(() => {
    // Reset mock before each test
    mockAxios.reset();
  });
  
  afterAll(() => {
    // Clean up
    mockAxios.restore();
  });

  test('getAllEnvironments returns the list of environments', async () => {
    // Setup mock response
    mockAxios.onGet('http://localhost:8080/api/environments').reply(200, mockEnvironments);
    
    // Call the function
    const result = await environmentsApi.getAllEnvironments();
    
    // Verify the result
    expect(result).toEqual(mockEnvironments);
  });

  test('getAllEnvironments handles API error', async () => {
    // Setup mock to return an error
    mockAxios.onGet('http://localhost:8080/api/environments').reply(500);
    
    // Call the function and expect it to throw
    await expect(environmentsApi.getAllEnvironments()).rejects.toThrow();
  });

  test('getAllEnvironments handles network error', async () => {
    // Setup mock to simulate network error
    mockAxios.onGet('http://localhost:8080/api/environments').networkError();
    
    // Call the function and expect it to throw
    await expect(environmentsApi.getAllEnvironments()).rejects.toThrow();
  });

  test('getAllEnvironments handles timeout', async () => {
    // Setup mock to simulate timeout
    mockAxios.onGet('http://localhost:8080/api/environments').timeout();
    
    // Call the function and expect it to throw
    await expect(environmentsApi.getAllEnvironments()).rejects.toThrow();
  });

  test('getAllEnvironments retry logic works on temporary failure', async () => {
    // Setup mock to fail once, then succeed
    mockAxios.onGet('http://localhost:8080/api/environments')
      .replyOnce(503) // Service unavailable
      .onGet('http://localhost:8080/api/environments')
      .reply(200, mockEnvironments);
    
    // Call the function
    const result = await environmentsApi.getAllEnvironments();
    
    // Verify the result
    expect(result).toEqual(mockEnvironments);
  });

  test('getAllEnvironments handles unauthorized request', async () => {
    // Setup mock to return 401 unauthorized
    mockAxios.onGet('http://localhost:8080/api/environments').reply(401);
    
    // Call the function and expect it to throw with specific message
    await expect(environmentsApi.getAllEnvironments()).rejects.toThrow();
  });
}); 