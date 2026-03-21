import axios from 'axios'
import store from '@/store'
import { MessageBox } from 'element-ui'

const service = axios.create({
  baseURL: '/api', // 使用 vue.config.js 的代理
  timeout: 5000
})

// 请求拦截器：添加 token
service.interceptors.request.use(
  config => {
    if (store.state.token) {
      config.headers['Authorization'] = 'Bearer ' + store.state.token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      if (res.code === 401) {
        store.commit('LOGOUT')
        MessageBox.confirm('您还未登录，是否前往登录页面？', '提示', {
          confirmButtonText: '去登录',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          window.location.href = '/login'
        }).catch(() => {})
      }
      // 400 业务错误不弹出提示，让调用者自行处理
      if (res.code !== 400) {
        MessageBox.alert(res.message || '操作失败', '提示', {
          confirmButtonText: '确定',
          type: 'warning'
        }).catch(() => {})
      }
      return Promise.reject(res)
    }
    return res.data
  },
  error => {
    if (error.response && error.response.status === 401) {
      store.commit('LOGOUT')
      MessageBox.confirm('您还未登录，是否前往登录页面？', '提示', {
        confirmButtonText: '去登录',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        window.location.href = '/login'
      }).catch(() => {})
      return Promise.resolve(null)
    }
    let errorMsg = error.message
    if (error.response && error.response.data && error.response.data.message) {
      errorMsg = error.response.data.message
    }
    MessageBox.alert(errorMsg, '系统异常', {
      confirmButtonText: '我知道了',
      type: 'error'
    }).catch(() => {})
    return Promise.resolve(null)
  }
)

export default service
