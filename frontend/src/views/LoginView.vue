<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 style="text-align: center;">{{ loginForm.role === 'admin' ? '管理员登录' : '论坛 - 登录' }}</h2>
      <el-form :model="loginForm" :rules="rules" ref="loginForm" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input type="password" v-model="loginForm.password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item label="登录类型" prop="role">
          <el-radio-group v-model="loginForm.role">
            <el-radio label="user">用户</el-radio>
            <el-radio label="admin">管理员</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" style="width: 100%;">登录</el-button>
        </el-form-item>
        <div style="text-align: right; font-size: 14px;" v-if="loginForm.role === 'user'">
          还没有账号？ <el-link type="primary" @click="$router.push('/register')">去注册</el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import request from '@/api/request'
import { mapMutations } from 'vuex'

export default {
  data() {
    return {
      loginForm: {
        username: '',
        password: '',
        role: 'user'
      },
      rules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
        role: [{ required: true, message: '请选择登录类型', trigger: 'change' }]
      }
    }
  },
  methods: {
    ...mapMutations(['SET_TOKEN', 'SET_USER']),
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          const loginData = {
            username: this.loginForm.username,
            password: this.loginForm.password
          }
          request.post('/user/login', loginData).then(res => {
            this.SET_TOKEN(res.token)
            this.SET_USER(res.user)
            this.$message.success('登录成功')
            if (this.loginForm.role === 'admin') {
              this.$router.push('/admin')
            } else {
              this.$router.push('/')
            }
          }).catch((err) => {
            if (err && err.message) {
              this.$message.error(err.message)
            }
          })
        }
      })
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
}
.login-card {
  width: 400px;
  padding: 20px;
}
</style>
