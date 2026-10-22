import axios from 'axios';

// Create an Axios instance with the Spring Boot backend URL
export const apiClient = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
    // 'Client-Id': 'frontend-app' // Optional: Used by our Bucket4j Rate Limiter
  },
});