import axios from 'axios'
import store from '@/store'
import { MessageBox } from 'element-ui'

const service = axios.create({
  baseURL: '/api', // 使用 vue.config.js 的代理
  timeout: 5000
})

// 请求拦截器
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
    // 假设后端返回格式 { code: 200, message: '成功', data: ... }
    if (res.code !== 200) {
      MessageBox.alert(res.message || 'Error', '操作失败', {
        confirmButtonText: '确定',
        type: 'error'
      }).catch(() => {})
      
      if (res.code === 401) {
        store.commit('LOGOUT')
      }
      // 抛出普通字符串替代 Error 对象，避免在控制台打印红色长堆栈
      return Promise.reject(res.message || 'Error')
    } else {
      return res.data
    }
  },
  error => {
    let errorMsg = error.message
    if (error.response && error.response.data && error.response.data.message) {
      errorMsg = error.response.data.message
    }
    MessageBox.alert(errorMsg, '系统异常', {
      confirmButtonText: '我知道了',
      type: 'error'
    }).catch(() => {})
    return Promise.reject(errorMsg)
  }
)

export default service
