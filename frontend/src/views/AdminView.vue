<template>
  <div class="admin-container">
    <el-container style="height: 100vh;">
      <el-aside width="220px" class="fade-in">
        <div class="logo">系统管理后台</div>
        <el-menu
          default-active="dashboard"
          class="el-menu-vertical"
          @select="handleSelect"
        >
          <el-menu-item index="dashboard">
            <i class="el-icon-data-line"></i>
            <span slot="title">数据看板</span>
          </el-menu-item>
          <el-menu-item index="users">
            <i class="el-icon-user"></i>
            <span slot="title">用户管理</span>
          </el-menu-item>
          <el-menu-item index="columns">
            <i class="el-icon-document"></i>
            <span slot="title">专栏审核</span>
          </el-menu-item>
          <el-menu-item index="back">
            <i class="el-icon-back"></i>
            <span slot="title">返回前台首页</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header style="text-align: right; font-size: 14px" class="fade-in">
          <el-dropdown>
            <i class="el-icon-setting" style="margin-right: 15px"></i>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item @click.native="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
          <span>{{ user?.username || '管理员' }}</span>
        </el-header>

        <el-main class="fade-in" style="animation-delay: 0.2s">
          <!-- 数据看板 -->
          <div v-show="activeTab === 'dashboard'">
             <h2>全局数据看板</h2>
             <el-row :gutter="20" style="margin-top: 20px;">
               <el-col :span="8">
                 <el-card shadow="always">
                    <div class="card-title">总注册用户</div>
                    <div class="card-num">{{ stat.userCount }}</div>
                 </el-card>
               </el-col>
               <el-col :span="8">
                 <el-card shadow="always">
                    <div class="card-title">专栏总数</div>
                    <div class="card-num">{{ stat.columnCount }}</div>
                 </el-card>
               </el-col>
               <el-col :span="8">
                 <el-card shadow="always">
                    <div class="card-title">支付订单总数</div>
                    <div class="card-num">{{ stat.orderCount }}</div>
                 </el-card>
               </el-col>
             </el-row>
          </div>

          <!-- 用户管理 -->
          <div v-show="activeTab === 'users'">
             <h2>用户管理</h2>
             <el-table :data="users" border style="width: 100%">
               <el-table-column prop="id" label="ID" width="80"></el-table-column>
               <el-table-column prop="username" label="用户名"></el-table-column>
               <el-table-column prop="email" label="邮箱"></el-table-column>
              <el-table-column label="角色">
                <template slot-scope="scope">
                  <el-tag v-if="scope.row.role === 2" type="danger">管理员</el-tag>
                  <el-tag v-else-if="scope.row.columnCount > 0" type="success">创作者</el-tag>
                  <el-tag v-else type="info">普通用户</el-tag>
                </template>
              </el-table-column>
               <el-table-column label="状态" width="100">
                 <template slot-scope="scope">
                   <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
                     {{ scope.row.status === 1 ? '正常' : '封禁' }}
                   </el-tag>
                 </template>
               </el-table-column>
               <el-table-column label="操作" width="150" v-if="user?.role === 2">
                 <template slot-scope="scope">
                   <el-button 
                     size="mini" 
                     :type="scope.row.status === 1 ? 'danger' : 'success'"
                     @click="changeUserStatus(scope.row)"
                     v-if="scope.row.role !== 2"
                   >
                     {{ scope.row.status === 1 ? '封禁' : '解封' }}
                   </el-button>
                 </template>
               </el-table-column>
             </el-table>
          </div>

          <!-- 专栏审核 -->
          <div v-show="activeTab === 'columns'">
             <h2>专栏内容管理</h2>
             <el-table :data="columns" border style="width: 100%">
               <el-table-column prop="id" label="ID" width="80"></el-table-column>
               <el-table-column prop="title" label="专栏标题"></el-table-column>
               <el-table-column prop="creatorId" label="创作者ID" width="100"></el-table-column>
               <el-table-column label="状态" width="100">
                 <template slot-scope="scope">
                   <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
                     {{ scope.row.status === 1 ? '上架中' : '已下架' }}
                   </el-tag>
                 </template>
               </el-table-column>
               <el-table-column label="操作" width="150">
                 <template slot-scope="scope">
                   <el-button 
                     size="mini" 
                     :type="scope.row.status === 1 ? 'danger' : 'success'"
                     @click="changeColumnStatus(scope.row)"
                   >
                     {{ scope.row.status === 1 ? '强制下架' : '恢复上架' }}
                   </el-button>
                 </template>
               </el-table-column>
             </el-table>
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import request from '@/api/request'
import { mapState, mapMutations } from 'vuex'

export default {
  data() {
    return {
      activeTab: 'dashboard',
      stat: {
        userCount: 0,
        columnCount: 0,
        orderCount: 0
      },
      users: [],
      columns: []
    }
  },
  computed: {
    ...mapState(['user'])
  },
  created() {
    this.fetchDashboard()
  },
  methods: {
    ...mapMutations(['LOGOUT']),
    logout() {
      this.LOGOUT()
      this.$router.push('/login')
    },
    handleSelect(index) {
      if (index === 'back') {
        this.$router.push('/')
        return
      }
      this.activeTab = index
      if (index === 'dashboard') this.fetchDashboard()
      if (index === 'users') this.fetchUsers()
      if (index === 'columns') this.fetchColumns()
    },
    fetchDashboard() {
      request.get('/admin/dashboard').then(res => {
        this.stat = res
      })
    },
    fetchUsers() {
      request.get('/admin/users').then(res => {
        this.users = res
      })
    },
    fetchColumns() {
      request.get('/admin/columns').then(res => {
        this.columns = res
      })
    },
    changeUserStatus(row) {
      const newStatus = row.status === 1 ? 0 : 1
      request.post(`/admin/user/status?id=${row.id}&status=${newStatus}`).then(() => {
        this.$message.success('更新状态成功')
        row.status = newStatus
      })
    },
    changeColumnStatus(row) {
      const newStatus = row.status === 1 ? 0 : 1
      request.post(`/admin/column/status?id=${row.id}&status=${newStatus}`).then(() => {
        this.$message.success('更新状态成功')
        row.status = newStatus
      })
    }
  }
}
</script>

<style scoped>
.admin-container {
  --theme-color: #1a365d;
  --theme-light: #4299e1;
  --theme-bg: #f7fafc;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background: linear-gradient(135deg, #2c5282 0%, #4299e1 100%);
}
.el-header {
  background-color: #fff;
  color: #333;
  line-height: 60px;
  border-bottom: 1px solid #e2e8f0;
}
.card-title {
  color: #4299e1;
  font-size: 14px;
}
.card-num {
  font-size: 32px;
  color: #2c5282;
  margin-top: 10px;
  font-weight: bold;
}

/* 科技蓝侧边栏 */
.el-aside {
  background: linear-gradient(180deg, #2c5282 0%, #4299e1 50%, #2c5282 100%) !important;
}
.el-menu {
  border-right: none !important;
  background: transparent !important;
}
.el-menu-item {
  margin: 4px 8px;
  border-radius: 8px;
  color: #e2e8f0 !important;
}
.el-menu-item i {
  color: #e2e8f0 !important;
}
.el-menu-item:hover {
  background-color: rgba(66, 153, 225, 0.3) !important;
}
.el-menu-item.is-active {
  background-color: #4299e1 !important;
  color: #fff !important;
}
.el-menu-item.is-active i {
  color: #fff !important;
}
</style>
