import { api } from './client'

export async function fetchPosts({ search, page = 0, size = 10 } = {}) {
  const { data } = await api.get('/posts', { params: { search, page, size } })
  return data // {items, page, size, totalElements, totalPages}
}

export async function fetchPostBySlug(slug) {
  const { data } = await api.get(`/posts/${slug}`)
  return data
}
