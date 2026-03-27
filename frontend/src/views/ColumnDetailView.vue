<template>
  <div class="column-detail">
    <el-container v-if="column">
      <el-header height="60px" class="glass-header">
        <div class="header-inner">
          <div class="nav-back" @click="$router.push('/')">
            <i class="el-icon-back"></i> 返回首页
          </div>
        </div>
      </el-header>
      <el-main class="fade-in">
        <el-card class="top-card">
          <div class="info-layout">
            <img :src="column.cover || 'https://picsum.photos/seed/' + column.id + '/600/300'" class="cover" />
            <div class="meta">
              <h2>{{ column.title }}</h2>
              <p class="desc">{{ column.description }}</p>
              <div class="actions">
                <span class="price" v-if="column.price > 0">¥ {{ column.price }}/月</span>
                <span class="price free" v-else>免费</span>
                
                <!-- 未订阅：显示订阅选项 -->
                <template v-if="!isSubscribed">
                  <el-select v-model="subscribeMonths" placeholder="选择月数" style="margin-left: 20px; width: 120px;" v-if="column.price > 0">
                    <el-option :value="1" label="1个月"></el-option>
                    <el-option :value="3" label="3个月"></el-option>
                    <el-option :value="6" label="6个月"></el-option>
                    <el-option :value="12" label="12个月"></el-option>
                  </el-select>
                  <el-button type="success" @click="subscribe" style="margin-left: 10px;">
                    {{ column.price > 0 ? '订阅专栏' : '免费加入' }}
                  </el-button>
                </template>
                
                <!-- 已订阅：显示状态和续费 -->
                <template v-else>
                  <el-tag type="success" style="margin-left: 20px;">✓ 已订阅</el-tag>
                  <span v-if="subscriptionDetail && subscriptionDetail.expireTime" class="expire-info">
                    到期：{{ subscriptionDetail.expireTime.substring(0, 10) }}
                  </span>
                  <el-button type="warning" @click="showRenewDialog" style="margin-left: 10px;" plain>
                    续费
                  </el-button>
                </template>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 文章列表 -->
        <el-card class="article-card" style="margin-top: 20px;">
          <div slot="header" class="clearfix">
            <span>专栏目录</span>
          </div>
          <el-table :data="articles" style="width: 100%" @row-click="readArticle" row-class-name="pointer-row">
            <el-table-column prop="title" label="文章标题"></el-table-column>
            <el-table-column label="阅读权限" width="120">
              <template slot-scope="scope">
                <el-tag :type="scope.row.isFree ? 'success' : 'warning'" size="small">
                  {{ scope.row.isFree ? '免费试读' : '付费内容' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="发布时间" width="200"></el-table-column>
          </el-table>
        </el-card>
      </el-main>
    </el-container>
    
    <!-- 续费对话框 -->
    <el-dialog title="续费专栏" :visible.sync="renewDialogVisible" width="450px" class="renew-dialog">
      <div class="renew-content" v-if="column">
        <div class="renew-header">
          <img :src="column.cover || 'https://picsum.photos/seed/' + column.id + '/100/60'" class="renew-cover" />
          <div class="renew-info">
            <h4>{{ column.title }}</h4>
            <p class="price-tag">¥{{ column.price }}/月</p>
          </div>
        </div>
        
        <el-divider></el-divider>
        
        <div class="renew-detail">
          <div class="detail-row">
            <span class="label">当前到期时间</span>
            <span class="value highlight">{{ formatExpireTime(subscriptionDetail?.expireTime) }}</span>
          </div>
          <div class="detail-row" v-if="subscriptionDetail?.durationMonths">
            <span class="label">已订阅时长</span>
            <span class="value">{{ subscriptionDetail.durationMonths }} 个月</span>
          </div>
        </div>
        
        <div class="renew-form">
          <p class="form-label">选择续费时长</p>
          <el-radio-group v-model="renewMonths" size="medium">
            <el-radio-button :label="1">1个月</el-radio-button>
            <el-radio-button :label="3">3个月</el-radio-button>
            <el-radio-button :label="6">6个月</el-radio-button>
            <el-radio-button :label="12">12个月</el-radio-button>
          </el-radio-group>
        </div>
        
        <div class="renew-summary">
          <span>续费金额</span>
          <span class="amount">¥{{ (column.price * renewMonths).toFixed(2) }}</span>
        </div>
        
        <div class="renew-summary new-expire">
          <span>到期时间将延长至</span>
          <span class="amount">{{ calculateNewExpireTime() }}</span>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="renewDialogVisible = false" plain>取消</el-button>
        <el-button type="warning" @click="renew" :loading="renewLoading">
          确认续费 ¥{{ column ? (column.price * renewMonths).toFixed(2) : 0 }}
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/api/request'

export default {
  data() {
    return {
      column: null,
      articles: [],
      isSubscribed: false,
      subscriptionDetail: null,
      subscribeMonths: 1,
      renewDialogVisible: false,
      renewMonths: 1,
      renewLoading: false
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    fetchData() {
      const id = this.$route.params.id
      request.get('/column/' + id).then(res => {
        this.column = res
        this.checkSubscription()
      })
      request.get('/article/list/' + id).then(res => {
        this.articles = res
      })
    },
    fetchUserBalance() {
      request.get('/user/info').then(user => {
        this.$store.commit('SET_USER', user)
      }).catch(() => {})
    },
    checkSubscription() {
      if (!this.$store.getters.isLoggedIn || !this.column) return
      request.get('/subscription/check?columnId=' + this.column.id).then(res => {
        this.isSubscribed = res === true || res === 'true'
        if (this.isSubscribed) {
          this.fetchSubscriptionDetail()
        }
      }).catch(() => {})
    },
    fetchSubscriptionDetail() {
      request.get('/subscription/my-detail').then(res => {
        if (Array.isArray(res)) {
          const sub = res.find(s => s.columnId == this.column.id)
          if (sub) {
            this.subscriptionDetail = sub
          }
        }
      }).catch(() => {})
    },
    subscribe() {
      if (!this.$store.getters.isLoggedIn) {
        this.$message.warning('请先登录系统')
        return this.$router.push('/login')
      }
      if (this.column.creatorId === this.$store.state.user?.id) {
        this.$message.info('这是您的专栏，无需订阅')
        return
      }
      if (this.isSubscribed) {
        this.$message.info('您已订阅该专栏，可使用续费功能')
        return
      }
      if (this.column.price === 0) {
        request.post('/order/create?columnId=' + this.column.id).then(() => {
          this.$message.success('免费专栏订阅成功！')
          this.isSubscribed = true
          this.fetchUserBalance()
        }).catch(() => {})
        return
      }
      const totalPrice = (this.column.price * this.subscribeMonths).toFixed(2)
      this.$confirm(`确认订阅 《${this.column.title}》 ${this.subscribeMonths}个月？\n共需支付 ¥${totalPrice}`, '确认订阅', {
        type: 'warning',
        confirmButtonText: '确认支付',
        cancelButtonText: '取消'
      }).then(() => {
        request.post('/subscription/subscribe?columnId=' + this.column.id + '&months=' + this.subscribeMonths).then(() => {
          this.$message.success('订阅成功！')
          this.isSubscribed = true
          this.fetchUserBalance()
          this.fetchSubscriptionDetail()
        }).catch(err => {
          this.$message.error(err.message || '订阅失败')
        })
      }).catch(() => {})
    },
    showRenewDialog() {
      this.renewMonths = 1
      this.renewDialogVisible = true
    },
    formatExpireTime(expireTime) {
      if (!expireTime) return '未订阅'
      return expireTime.substring(0, 10)
    },
    calculateNewExpireTime() {
      if (!this.subscriptionDetail?.expireTime) {
        const now = new Date()
        now.setMonth(now.getMonth() + this.renewMonths)
        return now.toISOString().substring(0, 10)
      }
      let expire = new Date(this.subscriptionDetail.expireTime)
      if (expire <= new Date()) {
        expire = new Date()
      }
      expire.setMonth(expire.getMonth() + this.renewMonths)
      return expire.toISOString().substring(0, 10)
    },
    renew() {
      this.renewLoading = true
      request.post('/subscription/renew?columnId=' + this.column.id + '&months=' + this.renewMonths).then(() => {
        this.$message.success('续费成功！')
        this.renewDialogVisible = false
        this.renewLoading = false
        this.fetchUserBalance()
        this.fetchSubscriptionDetail()
      }).catch(err => {
        this.$message.error(err.message || '续费失败')
        this.renewLoading = false
      })
    },
    readArticle(row) {
      if (!this.$store.getters.isLoggedIn) {
         this.$message.warning('请登录后阅读文章')
         return this.$router.push('/login')
      }
      this.$router.push('/article/' + row.id)
    }
  }
}
</script>

<style scoped>
.glass-header {
  line-height: 60px;
  background-color: rgba(255, 255, 255, 0.85);
  backdrop-filter: saturate(180%) blur(20px);
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
  align-items: center;
}
.nav-back {
  font-size: 15px;
  color: #606266;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: color 0.3s;
}
.nav-back:hover {
  color: #409EFF;
}
.top-card {
  margin-top: 20px;
}
.info-layout {
  display: flex;
  gap: 30px;
}
.cover {
  width: 300px;
  height: 200px;
  object-fit: cover;
  border-radius: 8px;
}
.meta {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.meta h2 {
  margin-top: 0;
}
.desc {
  color: #666;
  flex: 1;
}
.price {
  font-size: 24px;
  color: #F56C6C;
  font-weight: bold;
}
.price.free {
  color: #67C23A;
}
.expire-info {
  margin-left: 15px;
  color: #909399;
  font-size: 14px;
}
.actions {
  display: flex;
  align-items: center;
  margin-top: 20px;
  flex-wrap: wrap;
  gap: 10px;
}
::v-deep .pointer-row {
  cursor: pointer;
}

/* 续费对话框样式 */
.renew-content {
  padding: 10px 0;
}

.renew-header {
  display: flex;
  align-items: center;
  gap: 15px;
}

.renew-cover {
  width: 80px;
  height: 50px;
  object-fit: cover;
  border-radius: 6px;
}

.renew-info h4 {
  margin: 0 0 5px 0;
  font-size: 16px;
  color: #303133;
}

.price-tag {
  margin: 0;
  color: #F56C6C;
  font-weight: bold;
  font-size: 14px;
}

.renew-detail {
  margin: 15px 0;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.detail-row .label {
  color: #909399;
  font-size: 14px;
}

.detail-row .value {
  color: #606266;
  font-weight: 500;
}

.detail-row .value.highlight {
  color: #67C23A;
  font-weight: bold;
}

.renew-form {
  margin: 20px 0;
}

.form-label {
  color: #606266;
  font-size: 14px;
  margin-bottom: 12px;
}

::v-deep .el-radio-button__inner {
  border-radius: 4px !important;
  margin-right: 8px;
}

.renew-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-top: 15px;
  color: #606266;
  font-size: 14px;
}

.renew-summary .amount {
  font-size: 20px;
  font-weight: bold;
  color: #F56C6C;
}

.renew-summary.new-expire {
  background: linear-gradient(135deg, #f0f9eb 0%, #e8f5e1 100%);
}

.renew-summary.new-expire .amount {
  color: #67C23A;
  font-size: 16px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
