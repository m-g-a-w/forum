<template>
  <div class="settings-page">
    <el-header height="60px" class="glass-header">
       <div class="header-inner">
         <div class="logo" @click="$router.push('/')" style="cursor: pointer;"><i class="el-icon-back"></i> 返回首页</div>
         <div class="user-info">
             <el-avatar size="small" :src="user?.avatar"></el-avatar>
             <span style="margin-left: 10px; font-weight: bold">{{ user?.username }}</span>
         </div>
       </div>
    </el-header>
    
    <div class="settings-container">
      <el-row :gutter="30">
        <!-- 侧边导航 -->
        <el-col :span="6">
          <el-card shadow="never" class="menu-card">
            <el-menu default-active="1" class="settings-menu" @select="handleSelect">
              <el-menu-item index="1">
                <i class="el-icon-user"></i>
                <span slot="title">个人资料</span>
              </el-menu-item>
              <el-menu-item index="2">
                <i class="el-icon-wallet"></i>
                <span slot="title">钱包与资产</span>
              </el-menu-item>
              <el-menu-item index="3">
                <i class="el-icon-collection-tag"></i>
                <span slot="title">我的订阅</span>
              </el-menu-item>
              <el-menu-item index="4">
                <i class="el-icon-lock"></i>
                <span slot="title">账号安全</span>
              </el-menu-item>
            </el-menu>
          </el-card>
        </el-col>
        
        <!-- 主要设置区 -->
        <el-col :span="18">
          <el-card shadow="never" class="content-card">
            <!-- 个人资料 -->
            <div v-show="activeIndex === '1'" class="section fade-in">
               <h2 class="section-title">基本信息设置</h2>
               <el-divider></el-divider>
               <el-form label-width="100px" style="max-width: 500px; margin-top: 30px;">
                 <el-form-item label="头像URL">
                   <el-input v-model="form.avatar" placeholder="填入网络图片链接"></el-input>
                 </el-form-item>
                 <el-form-item label="用户名">
                   <el-input v-model="form.username" disabled></el-input>
                   <p class="help-text" style="margin-top: 8px;">用户名注册后暂时不可修改</p>
                 </el-form-item>
                 <el-form-item label="个人简介">
                   <el-input type="textarea" :rows="4" v-model="form.bio" placeholder="一句话介绍自己，将在您的个人主页展示"></el-input>
                 </el-form-item>
                 <el-form-item label="账号邮箱">
                   <el-input v-model="form.email" placeholder="example@gmail.com"></el-input>
                 </el-form-item>
                 <el-form-item>
                   <el-button type="primary" @click="saveProfile">保存修改</el-button>
                 </el-form-item>
               </el-form>
            </div>
            
            <!-- 钱包资产 -->
            <div v-show="activeIndex === '2'" class="section fade-in">
               <h2 class="section-title">钱包资产</h2>
               <el-divider></el-divider>
               <div class="wallet-box">
                  <i class="el-icon-coin wallet-icon"></i>
                  <div class="wallet-amount">¥ {{ user?.balance || '0.00' }}</div>
                  <p class="wallet-desc">您的当前系统虚拟可用余额</p>
                  <el-button type="success" icon="el-icon-shopping-bag-1" @click="handleRecharge">模拟充值</el-button>
               </div>
            </div>
            
            <!-- 我的订阅 -->
            <div v-show="activeIndex === '3'" class="section fade-in">
               <h2 class="section-title">我的已购专栏</h2>
               <el-divider></el-divider>
               <div v-if="subscriptions.length > 0" class="sub-grid">
                 <div v-for="col in subscriptions" :key="col.id" class="sub-item" @click="$router.push('/column/' + col.id)">
                   <img :src="col.cover || 'https://picsum.photos/seed/' + col.id + '/400/240'" class="sub-cover" />
                   <div class="sub-info">
                     <p class="sub-title">{{ col.title }}</p>
                     <el-tag size="mini" :type="col.price > 0 ? 'warning' : 'success'">
                       {{ col.price > 0 ? '¥' + col.price : '免费' }}
                     </el-tag>
                   </div>
                 </div>
               </div>
               <el-empty v-else-if="subscriptionLoaded" description="暂无已订阅专栏，快去探索吧～">
                 <el-button type="primary" @click="$router.push('/')">前往探索</el-button>
               </el-empty>
               <div v-else style="text-align:center;padding:60px 0;"><el-spinner /></div>
            </div>

            
            <!-- 账号安全 -->
            <div v-show="activeIndex === '4'" class="section fade-in">
               <h2 class="section-title">账号与安全</h2>
               <el-divider></el-divider>
               <el-alert title="请妥善保管您的密码" type="warning" show-icon style="margin-bottom: 20px;"></el-alert>
               <el-form label-width="120px" style="max-width: 400px;">
                 <el-form-item label="旧密码">
                   <el-input type="password" placeholder="请输入原密码"></el-input>
                 </el-form-item>
                 <el-form-item label="新密码">
                   <el-input type="password" placeholder="请输入新密码"></el-input>
                 </el-form-item>
                 <el-form-item label="确认新密码">
                   <el-input type="password" placeholder="再次输入确认"></el-input>
                 </el-form-item>
                 <el-form-item>
                   <el-button type="danger">确认修改密码</el-button>
                 </el-form-item>
               </el-form>
               
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import request from '@/api/request'

export default {
  data() {
    return {
      activeIndex: '1',
      form: {
        avatar: '',
        username: '',
        bio: '',
        email: ''
      },
      subscriptions: [],
      subscriptionLoaded: false
    }
  },
  computed: {
    ...mapState(['user'])
  },
  mounted() {
    if (this.user) {
      this.form.avatar = this.user.avatar || ''
      this.form.username = this.user.username
      this.form.bio = this.user.bio || ''
      this.form.email = this.user.email || ''
    } else {
      this.$message.warning('请先登录使用设置中心')
      this.$router.push('/login')
    }
  },
  methods: {
    handleSelect(index) {
      this.activeIndex = index
      if (index === '3' && !this.subscriptionLoaded) {
        this.fetchSubscriptions()
      }
    },
    saveProfile() {
      const payload = {
        avatar: this.form.avatar,
        bio: this.form.bio,
        email: this.form.email
      }
      request.put('/user/profile', payload).then(updatedUser => {
        this.$message.success('个人资料更新成功！')
        const merged = { ...this.user, ...updatedUser }
        this.$store.commit('SET_USER', merged)
      }).catch(() => {})
    },
    handleRecharge() {
      this.$message.success('已模拟向账户打入 100 元 (只更新内存数据供测试)')
      const updatedUser = { ...this.user, balance: (parseFloat(this.user.balance || 0) + 100).toFixed(2) }
      this.$store.commit('SET_USER', updatedUser)
    },
    fetchSubscriptions() {
      request.get('/subscription/my').then(res => {
        this.subscriptions = Array.isArray(res) ? res : []
        this.subscriptionLoaded = true
      }).catch(() => {
        this.subscriptions = []
        this.subscriptionLoaded = true
      })
    }
  }
}
</script>

<style scoped>
.settings-page {
  background-color: #f4f6fa;
  min-height: 100vh;
}
.glass-header {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: saturate(180%) blur(20px);
  -webkit-backdrop-filter: saturate(180%) blur(20px);
  line-height: 60px;
  border-bottom: 1px solid rgba(0,0,0,0.05);
  box-shadow: 0 2px 10px rgba(0,0,0,0.02);
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
}
.logo {
  font-size: 16px;
  color: #606266;
  font-weight: 500;
  transition: color 0.3s;
}
.logo:hover {
  color: #409EFF;
}
.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
}
::v-deep .el-avatar > img {
  object-fit: cover !important;
  width: 100%;
  height: 100%;
}
.settings-container {
  max-width: 1200px;
  margin: 40px auto;
  padding: 0 20px;
}
.menu-card {
  border-radius: 12px;
  padding: 10px 0;
}
.settings-menu {
  border-right: none;
}
::v-deep .el-menu-item {
  font-weight: 500;
  border-radius: 8px;
  margin: 0 10px;
  height: 50px;
  line-height: 50px;
}
::v-deep .el-menu-item.is-active {
  background-color: #ecf5ff;
}
.content-card {
  border-radius: 12px;
  min-height: 600px;
  padding: 20px 40px;
}
.section-title {
  font-size: 24px;
  color: #303133;
  margin: 0;
}
.help-text {
  font-size: 12px;
  color: #909399;
  margin: 0;
  line-height: 1;
}
.wallet-box {
  text-align: center;
  padding: 60px 0;
}
.wallet-icon {
  font-size: 64px;
  color: #F56C6C;
  margin-bottom: 20px;
}
.wallet-amount {
  font-size: 48px;
  font-weight: bold;
  color: #303133;
  font-family: Menlo, Monaco, Consolas, monospace;
}
.wallet-desc {
  color: #909399;
  margin-bottom: 40px;
}
.fade-in {
  animation: fadeIn 0.4s ease;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.sub-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 24px;
  padding: 10px 0;
}
.sub-item {
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  border: 1px solid #ebeef5;
}
.sub-item:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.08);
}
.sub-cover {
  width: 100%;
  height: 110px; /* 固定高度确保对齐 */
  object-fit: cover;
  background-color: #f5f7fa;
}
.sub-info {
  padding: 12px;
}
.sub-title {
  font-size: 15px;
  color: #303133;
  margin: 0 0 8px 0;
  line-height: 1.4;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis; /* 限制标题为 1 行 */
}
</style>
