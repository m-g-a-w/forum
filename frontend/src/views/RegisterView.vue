<template>
  <div class="register-container">
    <el-card class="register-card">
      <h2 style="text-align: center;">论坛 - 注册</h2>
      <el-form :model="regForm" :rules="rules" ref="regForm" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="regForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input type="password" v-model="regForm.password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" style="width: 100%;">注册</el-button>
        </el-form-item>
        <div style="text-align: right; font-size: 14px;">
          已有账号？ <el-link type="primary" @click="$router.push('/login')">去登录</el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import request from '@/api/request'

export default {
  data() {
    return {
      regForm: {
        username: '',
        password: ''
      },
      rules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      }
    }
  },
  methods: {
    handleRegister() {
      this.$refs.regForm.validate(valid => {
        if (valid) {
          request.post('/user/register', this.regForm).then(() => {
            this.$message.success('注册成功，请登录')
            this.$router.push('/login')
          }).catch(err => {
            this.$message.error(err.message || '注册失败，账号可能已被注册')
          })
        }
      })
    }
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
}
.register-card {
  width: 400px;
  padding: 20px;
}
</style>
