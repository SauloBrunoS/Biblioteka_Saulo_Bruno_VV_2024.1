import axios from 'axios'
import type { Colecao } from '@/types'
import httpClient from '@/api/HttpClient'

export default {
  
  async findSearch(
    pagina: number,
    itemPerPage: number,
    sortBy: [{ key: keyof Colecao; order: string }],
    search: string
  ) {
    const { data } = await httpClient({
      method: 'get',
      url: `/colecoes`,
      params: {
        search: search,
        page: pagina,
        size: itemPerPage,
        sort: `${sortBy?.[0]?.key ?? 'id'},${sortBy?.[0]?.order ?? 'desc'}`
      }
    })
    if (data?.content) {
      const colecoes = data.content
      const page = data
      return { items: colecoes, pagination: page }
    } else {
      return { items: [], pagination: {} }
    }
  },

  async create(colecao: Colecao): Promise<Colecao> {
    try {
      const { data } = await httpClient.post(`/colecoes`, colecao)
      return data
    } catch (error) {
      if (axios.isAxiosError(error)) {
        throw new Error(JSON.stringify(error.response?.data))
      }
      throw error
    }
  },

  async update(colecao: Colecao, id: number): Promise<Colecao> {
    const { data } = await httpClient({
      method: 'put',
      url: '/colecoes/' + id,
      data: colecao
    })
    return data
  },

  async delete(id: number): Promise<Colecao> {
    const { data } = await httpClient({
      method: 'delete',
      url: `/colecoes/${id}`
    })
    return data
  }
}
