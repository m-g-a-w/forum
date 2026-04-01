<template>
  <div class="admin-container">
    <el-container style="height: 100vh;">
      <el-aside width="220px" class="fade-in">
        <div class="logo" @click="$router.push('/')">
          <i class="el-icon-back"></i> 返回首页
        </div>
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
          <el-menu-item index="comments">
            <i class="el-icon-chat-dot-round"></i>
            <span slot="title">评论管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header height="60px" class="admin-header fade-in">
          <div class="header-right">
            <el-dropdown v-if="user" @command="handleCommand">
              <span class="user-dropdown">
                <el-avatar size="small" :src="user?.avatar || (user ? 'https://api.dicebear.com/7.x/avataaars/png?seed=' + user.username : 'https://api.dicebear.com/7.x/avataaars/png?seed=default')"></el-avatar>
                <span class="username">{{ user?.username }}</span>
                <i class="el-icon-arrow-down el-icon--right"></i>
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="home"><i class="el-icon-s-home"></i> 返回前台</el-dropdown-item>
                <el-dropdown-item command="logout" divided><i class="el-icon-switch-button"></i> 退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </el-header>

        <el-main class="fade-in" style="animation-delay: 0.2s">
          <!-- 数据看板 -->
          <div v-show="activeTab === 'dashboard'">
             <h2 class="section-title">全局数据看板</h2>
             
             <!-- 核心指标卡片 -->
             <el-row :gutter="20" style="margin-top: 20px;">
               <el-col :span="6">
                 <el-card shadow="hover" class="stat-card">
                    <div class="stat-icon users-icon"><i class="el-icon-user"></i></div>
                    <div class="stat-info">
                      <div class="stat-value">{{ stat.userCount }}</div>
                      <div class="stat-label">总注册用户</div>
                    </div>
                 </el-card>
               </el-col>
               <el-col :span="6">
                 <el-card shadow="hover" class="stat-card">
                    <div class="stat-icon columns-icon"><i class="el-icon-document"></i></div>
                    <div class="stat-info">
                      <div class="stat-value">{{ stat.columnCount }}</div>
                      <div class="stat-label">专栏总数</div>
                    </div>
                 </el-card>
               </el-col>
               <el-col :span="6">
                 <el-card shadow="hover" class="stat-card">
                    <div class="stat-icon orders-icon"><i class="el-icon-shopping-cart-2"></i></div>
                    <div class="stat-info">
                      <div class="stat-value">{{ stat.orderCount }}</div>
                      <div class="stat-label">支付订单</div>
                    </div>
                 </el-card>
               </el-col>
               <el-col :span="6">
                 <el-card shadow="hover" class="stat-card">
                    <div class="stat-icon subs-icon"><i class="el-icon-s-grid"></i></div>
                    <div class="stat-info">
                      <div class="stat-value">{{ stat.subscriptionCount || 0 }}</div>
                      <div class="stat-label">订阅总数</div>
                    </div>
                 </el-card>
               </el-col>
             </el-row>

             <!-- 近期动态 -->
             <el-row :gutter="20" style="margin-top: 30px;">
               <el-col :span="16">
                 <el-card shadow="hover">
                   <div slot="header" class="card-header">
                     <span><i class="el-icon-time"></i> 近期活跃用户</span>
                   </div>
                   <el-table :data="recentUsers" style="width: 100%" size="small">
                     <el-table-column prop="username" label="用户名"></el-table-column>
                     <el-table-column prop="email" label="邮箱" width="180"></el-table-column>
                     <el-table-column prop="createTime" label="注册时间" width="160"></el-table-column>
                   </el-table>
                 </el-card>
               </el-col>
               <el-col :span="8">
                 <el-card shadow="hover">
                   <div slot="header" class="card-header">
                     <span><i class="el-icon-pie-chart"></i> 数据概览</span>
                   </div>
                   <div class="quick-stats">
                     <div class="quick-item">
                       <span class="quick-label">活跃用户</span>
                       <span class="quick-value">{{ stat.activeUserCount || 0 }}</span>
                     </div>
                     <el-divider></el-divider>
                     <div class="quick-item">
                       <span class="quick-label">今日新注册</span>
                       <span class="quick-value">{{ stat.todayRegister || 0 }}</span>
                     </div>
                     <el-divider></el-divider>
                     <div class="quick-item">
                       <span class="quick-label">今日订单</span>
                       <span class="quick-value">{{ stat.todayOrder || 0 }}</span>
                     </div>
                     <el-divider></el-divider>
                     <div class="quick-item">
                       <span class="quick-label">总收入</span>
                       <span class="quick-value highlight">¥ {{ stat.totalRevenue || 0 }}</span>
                     </div>
                   </div>
                 </el-card>
               </el-col>
             </el-row>

             <!-- 最新订单 -->
             <el-row style="margin-top: 30px;">
               <el-col :span="24">
                 <el-card shadow="hover">
                   <div slot="header" class="card-header">
                     <span><i class="el-icon-document"></i> 最新订单记录</span>
                   </div>
                   <el-table :data="recentOrders" style="width: 100%" size="small">
                     <el-table-column prop="id" label="订单ID" width="80"></el-table-column>
                     <el-table-column prop="username" label="用户" width="120"></el-table-column>
                     <el-table-column prop="columnId" label="专栏ID" width="80"></el-table-column>
                     <el-table-column label="金额" width="100">
                       <template slot-scope="scope">
                         <span class="price-text">¥ {{ scope.row.amount || 0 }}</span>
                       </template>
                     </el-table-column>
                     <el-table-column label="状态" width="80">
                       <template slot-scope="scope">
                         <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small">
                           {{ scope.row.status === 1 ? '已完成' : '待支付' }}
                         </el-tag>
                       </template>
                     </el-table-column>
                     <el-table-column prop="createTime" label="下单时间"></el-table-column>
                   </el-table>
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

          <!-- 评论管理 -->
          <div v-show="activeTab === 'comments'">
            <h2 class="section-title">评论管理</h2>

            <!-- 统计卡片 -->
            <el-row :gutter="20" style="margin-top: 20px;">
              <el-col :span="8">
                <el-card shadow="hover" class="stat-card">
                  <div class="stat-icon comments-icon"><i class="el-icon-chat-dot-round"></i></div>
                  <div class="stat-info">
                    <div class="stat-value">{{ commentStats.total || 0 }}</div>
                    <div class="stat-label">评论总数</div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card shadow="hover" class="stat-card">
                  <div class="stat-icon today-icon"><i class="el-icon-date"></i></div>
                  <div class="stat-info">
                    <div class="stat-value">{{ commentStats.today || 0 }}</div>
                    <div class="stat-label">今日新增</div>
                  </div>
                </el-card>
              </el-col>
            </el-row>

            <!-- 搜索栏 -->
            <div style="margin-top: 20px; display: flex; gap: 10px;">
              <el-input
                v-model="commentSearch"
                placeholder="搜索评论内容、用户名或专栏标题..."
                clearable
                style="width: 300px;"
                @clear="fetchComments"
                @keyup.enter.native="fetchComments"
              >
                <i slot="prefix" class="el-input__icon el-icon-search"></i>
              </el-input>
              <el-button type="primary" icon="el-icon-search" @click="fetchComments">搜索</el-button>
              <el-button type="danger" icon="el-icon-delete" @click="handleBatchDelete" :disabled="selectedComments.length === 0">
                批量删除 ({{ selectedComments.length }})
              </el-button>
            </div>

            <!-- 评论列表 -->
            <el-table
              :data="comments"
              border
              style="width: 100%; margin-top: 20px;"
              @selection-change="handleSelectionChange"
            >
              <el-table-column type="selection" width="55"></el-table-column>
              <el-table-column prop="id" label="ID" width="80"></el-table-column>
              <el-table-column label="评论用户" width="150">
                <template slot-scope="scope">
                  <div style="display: flex; align-items: center; gap: 8px;">
                    <el-avatar size="small" :src="scope.row.userAvatar || 'https://api.dicebear.com/7.x/avataaars/png?seed=default'"></el-avatar>
                    <span>{{ scope.row.username || '用户' + scope.row.userId }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="所属专栏" width="180">
                <template slot-scope="scope">
                  <el-link type="primary" @click="goToColumn(scope.row.articleId)">
                    {{ scope.row.articleTitle || '专栏' + scope.row.articleId }}
                  </el-link>
                </template>
              </el-table-column>
              <el-table-column prop="content" label="评论内容">
                <template slot-scope="scope">
                  <div class="comment-content">{{ scope.row.content }}</div>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="评论时间" width="160"></el-table-column>
              <el-table-column label="操作" width="100" fixed="right">
                <template slot-scope="scope">
                  <el-button
                    type="danger"
                    size="mini"
                    icon="el-icon-delete"
                    @click="handleDeleteComment(scope.row)"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 分页 -->
            <el-pagination
              v-if="commentTotal > 0"
              style="margin-top: 20px; text-align: center;"
              background
              layout="prev, pager, next, total"
              :total="commentTotal"
              :page-size="commentPageSize"
              :current-page="commentPage"
              @current-change="handleCommentPageChange"
            >
            </el-pagination>
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
        orderCount: 0,
        subscriptionCount: 0,
        activeUserCount: 0,
        todayRegister: 0,
        todayOrder: 0,
        totalRevenue: 0
      },
      users: [],
      columns: [],
      recentUsers: [],
      recentOrders: [],
      // 评论管理相关
      comments: [],
      commentStats: { total: 0, today: 0 },
      commentSearch: '',
      commentPage: 1,
      commentPageSize: 10,
      commentTotal: 0,
      selectedComments: []
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
    handleCommand(cmd) {
      if (cmd === 'logout') {
        this.LOGOUT()
        this.$message.success('已安全退出')
        this.$router.push('/login')
      } else if (cmd === 'home') {
        this.$router.push('/')
      }
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
      if (index === 'comments') this.fetchComments()
    },
    fetchDashboard() {
      request.get('/admin/dashboard').then(res => {
        // 提取统计数据
        const { recentUsers, recentOrders, ...statData } = res
        this.stat = { ...this.stat, ...statData }
        this.recentUsers = recentUsers || []
        this.recentOrders = recentOrders || []
      }).catch(() => {})
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
    },
    // 评论管理相关方法
    fetchComments() {
      request.get('/comment/admin/list', {
        params: {
          keyword: this.commentSearch || null,
          page: this.commentPage,
          pageSize: this.commentPageSize
        }
      }).then(res => {
        if (res) {
          this.comments = res.list || []
          this.commentTotal = res.total || 0
        } else {
          this.comments = []
          this.commentTotal = 0
        }
      }).catch(() => {
        this.comments = []
        this.commentTotal = 0
      })
      // 获取统计数据
      request.get('/comment/admin/stats').then(stats => {
        this.commentStats = stats || { total: 0, today: 0 }
      }).catch(() => {})
    },
    handleDeleteComment(row) {
      this.$confirm('确定要删除这条评论吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        request.delete(`/comment/admin/${row.id}`).then(() => {
          this.$message.success('删除成功')
          this.fetchComments()
        }).catch(() => {})
      }).catch(() => {})
    },
    handleSelectionChange(val) {
      this.selectedComments = val
    },
    handleBatchDelete() {
      if (this.selectedComments.length === 0) {
        this.$message.warning('请先选择要删除的评论')
        return
      }
      this.$confirm(`确定要删除选中的 ${this.selectedComments.length} 条评论吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const ids = this.selectedComments.map(c => c.id)
        request.delete('/comment/admin/batch', { data: ids }).then(() => {
          this.$message.success('批量删除成功')
          this.selectedComments = []
          this.fetchComments()
        }).catch(() => {})
      }).catch(() => {})
    },
    handleCommentPageChange(page) {
      this.commentPage = page
      this.fetchComments()
    },
    goToColumn(articleId) {
      // 跳转到专栏详情
      this.$router.push(`/column/${articleId}`)
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
.admin-header {
  background-color: #fff;
  color: #333;
  line-height: 60px;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 20px;
}
.header-right {
  display: flex;
  align-items: center;
}
.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #333;
  cursor: pointer;
  font-weight: 500;
  padding: 6px 12px;
  border-radius: 20px;
  transition: background-color 0.3s;
}
.user-dropdown:hover {
  background-color: #f5f7fa;
}
.username {
  font-size: 14px;
}
::v-deep .el-avatar > img {
  object-fit: cover !important;
  width: 100%;
  height: 100%;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background: linear-gradient(135deg, #2c5282 0%, #4299e1 100%);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  transition: all 0.3s;
}
.logo:hover {
  background: linear-gradient(135deg, #2c5282 0%, #63b3ed 100%);
}
.logo i {
  font-weight: normal;
}

/* 数据看板样式 */
.section-title {
  font-size: 20px;
  color: #2c5282;
  font-weight: 600;
  margin-bottom: 10px;
}
.stat-card {
  display: flex;
  align-items: center;
  padding: 5px 0;
  border-radius: 12px;
  transition: transform 0.3s, box-shadow 0.3s;
}
.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.1) !important;
}
.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  margin-right: 15px;
}
.stat-icon i {
  color: #fff;
}
.users-icon { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.columns-icon { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.orders-icon { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.subs-icon { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }
.comments-icon { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.today-icon { background: linear-gradient(135deg, #fa709a 0%, #fee140 100%); }
.stat-info {
  flex: 1;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #2c5282;
}
.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 5px;
}
.card-header {
  font-size: 16px;
  color: #2c5282;
  font-weight: 600;
}
.card-header i {
  margin-right: 8px;
}
.quick-stats {
  padding: 10px 0;
}
.quick-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}
.quick-label {
  color: #606266;
  font-size: 14px;
}
.quick-value {
  color: #2c5282;
  font-weight: 600;
  font-size: 16px;
}
.quick-value.highlight {
  color: #f56c6c;
  font-size: 18px;
}
.price-text {
  color: #f56c6c;
  font-weight: 600;
}
.comment-content {
  max-width: 400px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
