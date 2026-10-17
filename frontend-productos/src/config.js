const local = ['localhost', '127.0.0.1', '[::1]'].includes(window.location.hostname);
export const API_BASE = local ? `http://${window.location.hostname}:8080/api` : '/api';
export const TIENDA_URL = local ? `http://${window.location.hostname}:5500` : '/';
