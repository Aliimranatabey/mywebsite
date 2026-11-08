import axios from 'axios';

const API_URL = '/api';

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  // Oturum çerezinin her istekte gitmesi için şart
  withCredentials: true,
  // Spring Security'nin bıraktığı CSRF çerezini otomatik okuyup header'a koyar
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
});

// Backend tüm hataları {"message": "..."} olarak dönüyor; onu tek yerde okunur hale getiriyoruz
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const message =
      error.response?.data?.message || error.message || 'İstek başarısız oldu';
    const wrapped = new Error(message);
    wrapped.status = error.response?.status;
    wrapped.fieldErrors = error.response?.data?.errors;
    return Promise.reject(wrapped);
  },
);

export default {
  // ---- Blog ----
  getBlogs(page = 0, size = 12) {
    return api.get('/blogs', { params: { page, size } });
  },
  getBlog(id) {
    return api.get(`/blogs/${id}`);
  },
  createBlog(blogData) {
    return api.post('/blogs', blogData);
  },
  updateBlog(id, blogData) {
    return api.put(`/blogs/${id}`, blogData);
  },
  deleteBlog(id) {
    return api.delete(`/blogs/${id}`);
  },

  // ---- Kimlik doğrulama ----
  register(payload) {
    return api.post('/auth/register', payload);
  },
  login(payload) {
    return api.post('/auth/login', payload);
  },
  logout() {
    return api.post('/auth/logout');
  },
  me() {
    return api.get('/auth/me');
  },
  // Yeni üye kaydı açık mı ({ enabled })
  registrationStatus() {
    return api.get('/auth/registration');
  },
  // CSRF çerezini almak için; giriş/kayıt öncesi çağrılır
  primeCsrf() {
    return api.get('/auth/csrf');
  },

  // ---- Görsel yükleme ----
  // Dönen { url } yazının coverImageUrl alanına yazılır
  uploadImage(file) {
    const form = new FormData();
    form.append('file', file);
    // Content-Type'ı tarayıcı boundary ile birlikte kendisi koysun
    return api.post('/uploads', form, { headers: { 'Content-Type': undefined } });
  },

  // ---- İletişim ----
  sendMessage(message) {
    return api.post('/contact', message);
  },
};
