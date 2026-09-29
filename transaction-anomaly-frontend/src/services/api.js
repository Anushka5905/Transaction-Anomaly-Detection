import axios from "axios";

const API = "http://localhost:8080/api";

const api = axios.create({
  baseURL: API
});

api.interceptors.request.use(
  (config) => {

    const token = localStorage.getItem("token");

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export default api;

api.interceptors.response.use(
    (response) => {
      return response;
    },
    (error) => {
  
      if (error.response?.status === 401) {
  
        localStorage.removeItem("token");
        localStorage.removeItem("user");
  
        window.location.reload();
      }
  
      return Promise.reject(error);
    }
  );