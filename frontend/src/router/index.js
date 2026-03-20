import Vue from 'vue'
import VueRouter from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import ColumnDetailView from '../views/ColumnDetailView.vue'
import ArticleView from '../views/ArticleView.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView
  },
  {
    path: '/register',
    name: 'register',
    component: RegisterView
  },
  {
    path: '/column/:id',
    name: 'columnDetail',
    component: ColumnDetailView
  },
  {
    path: '/article/:id',
    name: 'article',
    component: ArticleView
  },
  {
    path: '/creator',
    name: 'creator',
    component: () => import('../views/CreatorView.vue')
  },
  {
    path: '/admin',
    name: 'admin',
    component: () => import('../views/AdminView.vue')
  },
  {
    path: '/settings',
    name: 'settings',
    component: () => import('../views/SettingsView.vue')
  },
  {
    path: '/messages',
    name: 'messages',
    component: () => import('../views/MessageView.vue')
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

export default router
