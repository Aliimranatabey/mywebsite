import { api } from './client'
export async function sendContact(payload) {
  const { data } = await api.post('/contact', payload) // {name,email,message}
  return data
}
