<template>
  <div class="settings-page">
    <el-header height="60px" class="glass-header">
       <div class="header-inner">
         <div class="logo" @click="$router.push('/')" style="cursor: pointer;"><i class="el-icon-back"></i> 返回首页</div>
         <div class="user-info">
             <el-avatar size="small" :src="user?.avatar || (user ? 'https://api.dicebear.com/7.x/avataaars/png?seed=' + user.username : 'https://api.dicebear.com/7.x/avataaars/png?seed=default')"></el-avatar>
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
                <span slot="title">我的资产</span>
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
            
            <!-- 我的资产 -->
            <div v-show="activeIndex === '2'" class="section fade-in">
               <h2 class="section-title">我的资产</h2>
               <el-divider></el-divider>
               <div class="wallet-box">
                  <i class="el-icon-coin wallet-icon"></i>
                  <div class="wallet-amount">¥ {{ user?.balance || '0.00' }}</div>
                  <p class="wallet-desc">您的当前系统可用余额</p>
                  <el-button type="success" icon="el-icon-shopping-bag-1" @click="showRechargeDialog">充值</el-button>
               </div>
               
               <!-- 充值记录 -->
               <h3 style="margin-top: 40px; color: #606266;">充值记录</h3>
               <el-table :data="rechargeRecords" style="width: 100%; margin-top: 15px;" v-loading="rechargeLoading">
                 <el-table-column prop="rechargeNo" label="充值单号" width="200"></el-table-column>
                 <el-table-column prop="amount" label="充值金额" width="120">
                   <template slot-scope="scope">¥ {{ scope.row.amount }}</template>
                 </el-table-column>
                 <el-table-column prop="payMethodText" label="支付方式" width="120"></el-table-column>
                 <el-table-column label="状态" width="100">
                   <template slot-scope="scope">
                     <el-tag :type="scope.row.status === 1 ? 'success' : (scope.row.status === 0 ? 'warning' : 'info')" size="small">
                       {{ scope.row.statusText }}
                     </el-tag>
                   </template>
                 </el-table-column>
                 <el-table-column prop="createTime" label="充值时间"></el-table-column>
               </el-table>
               <el-empty v-if="!rechargeLoading && rechargeRecords.length === 0" description="暂无充值记录"></el-empty>
            </div>
            
            <!-- 我的订阅 -->
            <div v-show="activeIndex === '3'" class="section fade-in">
               <h2 class="section-title">我的已购专栏</h2>
               <el-divider></el-divider>
               <div v-if="subscriptions.length > 0" class="sub-grid">
                 <div v-for="col in displayedSubscriptions" :key="col.id" class="sub-item" @click="$router.push('/column/' + col.id)">
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

               <div class="load-more-container" v-if="hasMoreSubscriptions">
                 <el-button round :loading="loadingMoreSubscriptions" @click="loadMoreSubscriptions" class="load-more-btn">
                   加载更多 <i class="el-icon-arrow-down"></i>
                 </el-button>
               </div>
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

    <!-- 充值对话框 -->
    <el-dialog title="" :visible.sync="rechargeDialogVisible" width="460px" :close-on-click-modal="false" class="recharge-dialog">
      <div class="recharge-dialog-content">
        <!-- 头部 -->
        <div class="recharge-header">
          <div class="recharge-icon">
            <i class="el-icon-wallet"></i>
          </div>
          <div class="recharge-title">
            <h3>账户充值</h3>
            <p>选择金额，开启知识之旅</p>
          </div>
        </div>

        <!-- 快捷金额 -->
        <div class="recharge-section">
          <div class="section-title">选择金额</div>
          <div class="amount-cards">
            <div
              v-for="amt in quickAmounts"
              :key="amt"
              :class="['amount-card', { active: rechargeForm.amount === amt }]"
              @click="rechargeForm.amount = amt"
            >
              <span class="amount-num">¥{{ amt }}</span>
              <span v-if="amt >= 100" class="amount-badge">荐</span>
            </div>
          </div>
        </div>

        <!-- 自定义金额 -->
        <div class="recharge-section">
          <div class="section-title">自定义金额</div>
          <el-input-number v-model="rechargeForm.amount" :min="1" :max="10000" :precision="2" size="medium"></el-input-number>
        </div>

        <!-- 支付方式 -->
        <div class="recharge-section">
          <div class="section-title">支付方式</div>
          <div class="payment-card active">
            <div class="payment-logo">
              <svg viewBox="0 0 24 24" width="24" height="24">
                <path fill="#1677FF" d="M19.5 6.5c-.3-.9-1.1-1.5-2.1-1.5H6.6c-.9 0-1.8.6-2.1 1.5l-3.2 9.8c-.2.7.3 1.5 1.1 1.5h2.4l1.4 4.2c.2.6.8 1 1.4 1h.4c.8 0 1.4-.6 1.4-1.4v-.2c0-.4-.2-.8-.5-1l-.8-.6 1-3.1h2.4c.9 0 1.8-.6 2.1-1.5l.5-1.4h2.3c.8 0 1.5-.8 1.3-1.6l-2.5-6.5z"/>
              </svg>
            </div>
            <div class="payment-info">
              <span class="payment-name">支付宝</span>
              <span class="payment-sub">推荐</span>
            </div>
            <div class="payment-check"><i class="el-icon-check"></i></div>
          </div>
        </div>

        <!-- 提示 -->
        <div class="recharge-tips">
          <p><i class="el-icon-circle-check"></i> 即时到账</p>
          <p><i class="el-icon-circle-check"></i> 可购买专栏</p>
        </div>
      </div>

      <div slot="footer" class="recharge-footer">
        <div class="footer-info">
          <span class="label">支付金额</span>
          <span class="price">¥{{ rechargeForm.amount }}</span>
        </div>
        <el-button type="primary" size="medium" :loading="recharging" @click="confirmRecharge">立即充值</el-button>
      </div>
    </el-dialog>

    <!-- 扫码支付对话框 -->
    <el-dialog title="" :visible.sync="qrCodeDialogVisible" width="380px" :close-on-click-modal="false" class="qr-dialog">
      <div class="qr-dialog-content">
        <div class="qr-close" @click="cancelQrCodePayment"><i class="el-icon-close"></i></div>

        <div class="qr-amount">
          <span class="qr-label">待支付</span>
          <span class="qr-price">¥{{ rechargeForm.amount }}</span>
        </div>

        <div class="qr-box">
          <div class="qr-loading" v-if="!qrCodeUrl">
            <i class="el-icon-loading"></i>
            <p>生成中...</p>
          </div>
          <img v-else :src="qrCodeUrl" alt="支付二维码" class="qr-image" />
        </div>

        <div class="qr-steps">
          <div class="qr-step"><span class="step-num">1</span><span>打开支付宝</span></div>
          <div class="step-arrow"><i class="el-icon-arrow-right"></i></div>
          <div class="qr-step"><span class="step-num">2</span><span>扫描二维码</span></div>
          <div class="step-arrow"><i class="el-icon-arrow-right"></i></div>
          <div class="qr-step"><span class="step-num">3</span><span>完成支付</span></div>
        </div>

        <div class="qr-status">
          <div class="status-polling" v-if="paymentPolling">
            <i class="el-icon-refresh el-spin"></i> 检测支付结果...
          </div>
        </div>

        <el-button size="small" plain @click="manualRefreshStatus" class="refresh-btn">
          <i class="el-icon-refresh"></i> 刷新状态
        </el-button>
      </div>
    </el-dialog>

    <!-- 支付成功 -->
    <el-dialog :visible.sync="successDialogVisible" width="320px" :show-close="false" class="success-dialog">
      <div class="success-content">
        <div class="success-icon">
          <div class="success-circle"><i class="el-icon-check"></i></div>
        </div>
        <h3>充值成功</h3>
        <p class="success-amount">+ ¥{{ rechargeForm.amount }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import request from '@/api/request'
import QRCode from 'qrcode'

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
      subscriptionLoaded: false,
      // 分页相关
      subscriptionPage: 1,
      subscriptionPageSize: 8,
      loadingMoreSubscriptions: false,
      // 充值相关
      quickAmounts: [10, 30, 50, 100, 200, 500],
      rechargeDialogVisible: false,
      rechargeForm: {
        amount: 100,
        payMethod: 'alipay'
      },
      recharging: false,
      rechargeRecords: [],
      rechargeLoading: false,
      // 二维码相关
      qrCodeUrl: '',
      qrCodeDialogVisible: false,
      currentRechargeNo: '',
      // 支付状态
      paymentPolling: false,
      paymentComplete: false,
      refreshing: false,
      // 成功对话框
      successDialogVisible: false
    }
  },
  computed: {
    ...mapState(['user']),
    displayedSubscriptions() {
      return this.subscriptions.slice(0, this.subscriptionPage * this.subscriptionPageSize)
    },
    hasMoreSubscriptions() {
      return this.subscriptions.length > this.subscriptionPage * this.subscriptionPageSize
    }
  },
  mounted() {
    if (this.user) {
      this.form.avatar = this.user.avatar || 'https://api.dicebear.com/7.x/avataaars/png?seed=' + this.user.username
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
      if (index === '2') {
        this.fetchRechargeRecords()
      }
    },
    showRechargeDialog() {
      this.rechargeForm.amount = 100
      this.rechargeForm.payMethod = 'alipay'
      this.rechargeDialogVisible = true
    },
    confirmRecharge() {
      if (!this.rechargeForm.amount || this.rechargeForm.amount <= 0) {
        this.$message.warning('请输入正确的充值金额')
        return
      }
      this.recharging = true

      request.post(`/user/recharge/create?amount=${this.rechargeForm.amount}&payMethod=${this.rechargeForm.payMethod}`)
        .then(order => {
          if (order.qrCode) {
            this.currentRechargeNo = order.record.rechargeNo
            QRCode.toDataURL(order.qrCode, {
              width: 200,
              margin: 2,
              color: { dark: '#000000', light: '#ffffff' }
            }).then(url => {
              this.qrCodeUrl = url
              this.rechargeDialogVisible = false
              this.qrCodeDialogVisible = true
              this.paymentPolling = true
              this.paymentComplete = false
              this.pollPaymentStatus(order.record.rechargeNo)
            }).catch(err => {
              this.$message.error('二维码生成失败')
            })
          } else if (order.payForm) {
            const newWindow = window.open('', '_blank', 'width=500,height=600')
            if (newWindow) {
              newWindow.document.write(order.payForm)
              newWindow.document.close()
              this.rechargeDialogVisible = false
              this.currentRechargeNo = order.record.rechargeNo
              this.pollPaymentStatus(order.record.rechargeNo)
            }
          } else {
            return request.post(`/user/recharge/pay?rechargeNo=${order.record.rechargeNo}`)
          }
        })
        .then(res => {
          if (res) {
            this.$message.success('充值成功')
            this.$store.commit('SET_USER', res.user)
            this.rechargeDialogVisible = false
            this.fetchRechargeRecords()
          }
        })
        .catch(err => {
          this.$message.error(err.message || '充值失败')
        })
        .finally(() => {
          this.recharging = false
        })
    },
    manualRefreshStatus() {
      if (!this.currentRechargeNo) return
      this.refreshing = true
      request.get(`/user/recharge/status?rechargeNo=${this.currentRechargeNo}`)
        .then(res => {
          if (res.status === 1) {
            this.paymentComplete = true
            this.paymentPolling = false
            this.$message.success('支付成功')
            this.qrCodeDialogVisible = false
            this.successDialogVisible = true
            this.qrCodeUrl = ''
            this.fetchRechargeRecords()
            request.get('/user/info').then(user => {
              this.$store.commit('SET_USER', user)
            })
            setTimeout(() => {
              this.successDialogVisible = false
            }, 2000)
          } else {
            this.$message.info('支付尚未完成，请继续等待')
          }
        })
        .catch(() => {
          this.$message.error('查询失败')
        })
        .finally(() => {
          this.refreshing = false
        })
    },
    cancelQrCodePayment() {
      this.qrCodeDialogVisible = false
      this.qrCodeUrl = ''
      this.currentRechargeNo = ''
      this.paymentPolling = false
    },
    pollPaymentStatus(rechargeNo) {
      let attempts = 0
      const maxAttempts = 60

      const checkStatus = () => {
        if (attempts >= maxAttempts) {
          this.paymentPolling = false
          this.$message.warning('支付超时，请手动刷新状态')
          return
        }

        request.get(`/user/recharge/status?rechargeNo=${rechargeNo}`)
          .then(res => {
            if (res.status === 1) {
              this.paymentComplete = true
              this.paymentPolling = false
              this.$message.success('充值成功')
              this.qrCodeDialogVisible = false
              this.successDialogVisible = true
              this.qrCodeUrl = ''
              this.fetchRechargeRecords()
              request.get('/user/info').then(user => {
                this.$store.commit('SET_USER', user)
              })
              setTimeout(() => {
                this.successDialogVisible = false
              }, 2000)
            } else {
              attempts++
              setTimeout(checkStatus, 2000)
            }
          })
          .catch(() => {
            attempts++
            setTimeout(checkStatus, 2000)
          })
      }

      setTimeout(checkStatus, 3000)
    },
    fetchRechargeRecords() {
      this.rechargeLoading = true
      request.get('/user/recharge/records')
        .then(res => {
          this.rechargeRecords = res || []
        })
        .catch(() => {
          this.rechargeRecords = []
        })
        .finally(() => {
          this.rechargeLoading = false
        })
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
      request.post('/user/recharge?amount=100').then(res => {
        this.$message.success('充值成功！已向账户打入 100 元')
        this.$store.commit('SET_USER', res)
      }).catch(() => {})
    },
    fetchSubscriptions() {
      this.resetSubscriptionsPagination()
      request.get('/subscription/my').then(res => {
        this.subscriptions = Array.isArray(res) ? res : []
        this.subscriptionLoaded = true
      }).catch(() => {
        this.subscriptions = []
        this.subscriptionLoaded = true
      })
    },
    loadMoreSubscriptions() {
      this.loadingMoreSubscriptions = true
      setTimeout(() => {
        this.subscriptionPage++
        this.loadingMoreSubscriptions = false
      }, 500)
    },
    resetSubscriptionsPagination() {
      this.subscriptionPage = 1
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
.pay-tip {
  background: #f4f9ff;
  padding: 10px 15px;
  border-radius: 6px;
  color: #409eff;
  font-size: 13px;
}

.load-more-container {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}
.load-more-btn {
  padding: 10px 35px;
  font-size: 14px;
  transition: all 0.3s;
}
.load-more-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.3);
}

/* ========== 现代充值对话框样式 ========== */
.recharge-dialog-content {
  padding: 0 20px;
}
.recharge-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px dashed #ebeef5;
}
.recharge-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
}
.recharge-title h3 {
  margin: 0 0 4px 0;
  font-size: 18px;
  color: #303133;
}
.recharge-title p {
  margin: 0;
  font-size: 13px;
  color: #909399;
}
.recharge-section {
  margin-bottom: 20px;
}
.section-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 12px;
  font-weight: 500;
}
.amount-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.amount-card {
  position: relative;
  padding: 14px;
  background: #f5f7fa;
  border: 2px solid transparent;
  border-radius: 10px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
}
.amount-card:hover {
  background: #ecf5ff;
  border-color: #d9ecff;
}
.amount-card.active {
  background: #ecf5ff;
  border-color: #409eff;
}
.amount-num {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}
.amount-card.active .amount-num {
  color: #409eff;
}
.amount-badge {
  position: absolute;
  top: -6px;
  right: -6px;
  background: linear-gradient(135deg, #f56c6c, #e64d4d);
  color: #fff;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 10px;
}
.payment-card {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  background: #f5f7fa;
  border: 2px solid transparent;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}
.payment-card.active {
  background: #ecf5ff;
  border-color: #409eff;
}
.payment-logo {
  width: 36px;
  height: 36px;
  background: #fff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.payment-info {
  flex: 1;
  margin-left: 12px;
}
.payment-name {
  display: block;
  font-weight: 500;
  color: #303133;
}
.payment-sub {
  font-size: 12px;
  color: #909399;
}
.payment-check {
  width: 24px;
  height: 24px;
  background: #409eff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 14px;
}
.recharge-tips {
  display: flex;
  gap: 20px;
  padding: 12px 16px;
  background: #f4f9ff;
  border-radius: 8px;
  margin-bottom: 20px;
}
.recharge-tips p {
  margin: 0;
  font-size: 13px;
  color: #606266;
}
.recharge-tips i {
  color: #67c23a;
  margin-right: 4px;
}
.recharge-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: #f5f7fa;
  border-top: 1px solid #ebeef5;
  margin: 0 -20px -20px -20px;
  border-radius: 0 0 8px 8px;
}
.footer-info .label {
  font-size: 13px;
  color: #909399;
}
.footer-info .price {
  font-size: 24px;
  font-weight: 600;
  color: #f56c6c;
  margin-left: 8px;
}
.recharge-footer .el-button {
  padding: 10px 28px;
}

/* ========== 扫码支付对话框 ========== */
.qr-dialog-content {
  position: relative;
  padding: 10px 20px 20px;
  text-align: center;
}
.qr-close {
  position: absolute;
  top: 0;
  right: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #909399;
  font-size: 18px;
  transition: color 0.2s;
}
.qr-close:hover {
  color: #606266;
}
.qr-amount {
  margin-bottom: 20px;
}
.qr-label {
  display: block;
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
}
.qr-price {
  font-size: 32px;
  font-weight: 600;
  color: #303133;
}
.qr-box {
  background: #fff;
  border: 2px dashed #e4e7ed;
  border-radius: 12px;
  padding: 20px;
  display: inline-block;
  margin-bottom: 20px;
}
.qr-image {
  display: block;
  width: 180px;
  height: 180px;
}
.qr-loading {
  width: 180px;
  height: 180px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
}
.qr-loading i {
  font-size: 32px;
  color: #409eff;
}
.qr-loading p {
  margin: 0;
  font-size: 13px;
  color: #909399;
}
.qr-steps {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 16px;
}
.qr-step {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}
.step-num {
  width: 20px;
  height: 20px;
  background: #ecf5ff;
  color: #409eff;
  border-radius: 50%;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
}
.step-arrow {
  color: #c0c4cc;
  font-size: 12px;
}
.qr-status {
  margin-bottom: 16px;
}
.status-polling {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #fdf6ec;
  color: #e6a23c;
  border-radius: 20px;
  font-size: 13px;
}
.refresh-btn {
  color: #409eff;
}
.refresh-btn:hover {
  color: #66b1ff;
}

/* ========== 支付成功对话框 ========== */
.success-dialog >>> .el-dialog__body {
  padding: 40px 20px;
}
.success-content {
  text-align: center;
}
.success-icon {
  margin-bottom: 20px;
}
.success-circle {
  width: 72px;
  height: 72px;
  background: linear-gradient(135deg, #67c23a, #529b2c);
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  animation: successPop 0.5s ease;
}
.success-circle i {
  font-size: 36px;
  color: #fff;
}
@keyframes successPop {
  0% { transform: scale(0); }
  50% { transform: scale(1.2); }
  100% { transform: scale(1); }
}
.success-content h3 {
  margin: 0 0 10px;
  font-size: 20px;
  color: #303133;
}
.success-amount {
  font-size: 28px;
  font-weight: 600;
  color: #67c23a;
  margin: 0 0 8px;
}
.success-tip {
  font-size: 14px;
  color: #909399;
  margin: 0;
}
</style>
