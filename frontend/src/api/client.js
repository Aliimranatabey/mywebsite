import axios from 'axios'

export const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 8000,
})

api.interceptors.response.use(
  (r) => r,
  (err) => {
    const msg =
      err.response?.data?.message || err.response?.data?.error || err.message || 'İstek başarısız'
    return Promise.reject(new Error(msg))
  },
)
