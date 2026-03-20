<template>
  <div class="column-detail">
    <el-container v-if="column">
      <el-header height="60px">
        <div class="logo" @click="$router.push('/')" style="cursor: pointer;">&lt; 返回首页</div>
      </el-header>
      <el-main class="fade-in">
        <el-card class="top-card">
          <div class="info-layout">
            <img :src="column.cover || 'https://via.placeholder.com/300x150?text=Column'" class="cover" />
            <div class="meta">
              <h2>{{ column.title }}</h2>
              <p class="desc">{{ column.description }}</p>
              <div class="actions">
                <span class="price" v-if="column.price > 0">¥ {{ column.price }}</span>
                <span class="price free" v-else>免费</span>
                <el-button v-if="!isSubscribed" type="success" @click="subscribe" style="margin-left: 20px;">
                  {{ column.price > 0 ? '订阅专栏' : '免费加入' }}
                </el-button>
                <el-tag v-else type="success" style="margin-left: 20px;">✓ 已订阅</el-tag>
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
  </div>
</template>

<script>
import request from '@/api/request'

export default {
  data() {
    return {
      column: null,
      articles: [],
      isSubscribed: false
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
        // 检查是否已订阅
        this.checkSubscription()
      })
      request.get('/article/list/' + id).then(res => {
        this.articles = res
      })
    },
    checkSubscription() {
      if (!this.$store.getters.isLoggedIn || !this.column) return
      request.get('/subscription/check?columnId=' + this.column.id).then(res => {
        this.isSubscribed = res === true || res === 'true'
      }).catch(() => {})
    },
    subscribe() {
      if (!this.$store.getters.isLoggedIn) {
        this.$message.warning('请先登录系统')
        return this.$router.push('/login')
      }
      if (this.isSubscribed) {
        this.$message.info('您已订阅该专栏')
        return
      }
      // 免费专栏：直接订阅，无需确认
      if (this.column.price === 0) {
        request.post('/order/create?columnId=' + this.column.id).then(() => {
          this.$message.success('免费专栏订阅成功！')
          this.isSubscribed = true
        }).catch(() => {})
        return
      }
      // 付费专栏：弹出确认框
      this.$confirm(`确认要订阅 《${this.column.title}》 吗？将从余额中扣除 ¥${this.column.price}`, '确认订阅', {
        type: 'warning',
        confirmButtonText: '确认支付',
        cancelButtonText: '取消'
      }).then(() => {
        request.post('/order/create?columnId=' + this.column.id).then(order => {
          if (!order) {
            this.$message.success('订阅成功')
            this.isSubscribed = true
          } else {
            request.post('/order/pay?orderNo=' + order.orderNo).then(() => {
              this.$message.success('支付成功，已解锁该专栏 🎉')
              this.isSubscribed = true
            }).catch(() => {})
          }
        })
      }).catch(() => {})
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
.el-header {
  line-height: 60px;
  background-color: #ffffff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.logo {
  font-size: 16px;
  color: #606266;
}
.logo:hover {
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
.actions {
  display: flex;
  align-items: center;
  margin-top: 20px;
}
::v-deep .pointer-row {
  cursor: pointer;
}
</style>
