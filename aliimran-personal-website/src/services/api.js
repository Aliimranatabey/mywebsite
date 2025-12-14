import axios from 'axios';

const API_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export default {
  // Blog İşlemleri
  getBlogs() {
    return api.get('/blogs');
  },
  getBlog(id) {
    return api.get(`/blogs/${id}`);
  },
  updateBlog(id, blogData) {
    return api.put(`/blogs/${id}`, blogData);
  },
  createBlog(blogData) {
    return api.post('/blogs', blogData);
  },
  // İletişim İşlemleri
  sendMessage(message) {
    return api.post('/contact', message);
  },
};
