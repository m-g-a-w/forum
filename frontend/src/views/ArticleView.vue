<template>
  <div class="article-view">
    <el-container v-if="article">
      <el-header height="60px" class="glass-header">
        <div class="header-inner">
          <div class="nav-back" @click="goBack">
            <i class="el-icon-back"></i> 返回专栏
          </div>
        </div>
      </el-header>
      
      <el-main class="article-main">
        <div class="article-wrapper">
          <h1 class="title">{{ article.title }}</h1>
          
          <div class="meta-section">
            <div class="author-info">
              <el-avatar size="medium" src="https://api.dicebear.com/7.x/avataaars/png?seed=default"></el-avatar>
              <div class="author-details">
                <span class="author-name">知识专栏作者</span>
                <span class="publish-time">{{ article.createTime }} · 约阅读 5 分钟</span>
              </div>
            </div>
          </div>
          <el-divider></el-divider>
          
          <div class="content render-body" v-html="contentHtml"></div>

          <!-- 评论区 -->
          <div class="comment-section" v-if="!showPaywall">
            <div class="comment-header">
              <i class="el-icon-chat-dot-round"></i> 读者留言
            </div>
            <!-- 发表评论 -->
            <div class="comment-input">
              <el-input
                type="textarea"
                :rows="2"
                placeholder="读完有收获？留下你的想法吧..."
                v-model="newComment"
                maxlength="200"
                show-word-limit
              ></el-input>
              <el-button type="primary" size="small" round @click="submitComment" :loading="submitting" style="margin-top: 10px;">
                发布评论
              </el-button>
            </div>
            <!-- 评论列表 -->
            <div class="comment-list">
              <div v-for="comment in comments" :key="comment.id" class="comment-item">
                <el-avatar :size="36" :src="comment.avatar || 'https://api.dicebear.com/7.x/avataaars/png?seed=' + (comment.username || 'default')"></el-avatar>
                <div class="comment-body">
                  <div class="comment-meta">
                    <span class="comment-author">{{ comment.nickname || comment.username || '匿名用户' }}</span>
                    <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
                  </div>
                  <div class="comment-text">{{ comment.content }}</div>
                </div>
              </div>
              <el-empty v-if="comments.length === 0" description="暂无评论，来抢沙发吧~" :image-size="60"></el-empty>
            </div>
          </div>

          <div class="paywall-overlay" v-if="showPaywall">
            <div class="paywall-gradient"></div>
            <div class="paywall-box glass-card">
              <i class="el-icon-lock lock-icon"></i>
              <h3>解锁专栏，阅读完整文章</h3>
              <p class="paywall-desc">本篇为专栏付费进阶内容。订阅专栏后即可畅享全部干货与源码解读。</p>
              <el-button type="primary" class="unlock-btn" @click="goToColumn" round>
                立即解锁专栏
              </el-button>
            </div>
          </div>
          
          <el-divider v-if="!showPaywall"></el-divider>
          <div class="end-mark" v-if="!showPaywall">
             <i class="el-icon-magic-stick"></i> 完 
          </div>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script>
import request from '@/api/request'
import dayjs from 'dayjs'

export default {
  data() {
    return {
      article: null,
      showPaywall: false,
      comments: [],
      newComment: '',
      submitting: false,
      isPaywall: false
    }
  },
  computed: {
    contentHtml() {
      if (!this.article) return ''
      return this.article.content
    }
  },
  created() {
    this.fetchData()
  },
  methods: {
    fetchData() {
      const id = this.$route.params.id
      request.get('/article/' + id).then(res => {
        this.article = res
        this.isPaywall = res.paywall === true
        if (this.isPaywall) {
          this.showPaywall = true
        }
      }).catch(() => {
        // 文章不存在或获取失败，显示付费墙
        this.showPaywall = true
      })
      this.fetchComments()
    },
    fetchComments() {
      const id = this.$route.params.id
      request.get('/comment/list/' + id).then(res => {
        // 处理 Result 格式的返回
        if (res && res.data) {
          this.comments = res.data || []
        } else if (Array.isArray(res)) {
          this.comments = res
        } else {
          this.comments = []
        }
      }).catch(() => {
        this.comments = []
      })
    },
    submitComment() {
      if (!this.newComment.trim()) {
        this.$message.warning('请输入评论内容')
        return
      }
      if (!this.$store.getters.isLoggedIn) {
        this.$message.warning('请先登录后再发表评论')
        return this.$router.push('/login')
      }
      this.submitting = true
      const id = this.$route.params.id
      request.post('/comment/publish', {
        articleId: id,
        content: this.newComment
      }).then(() => {
        this.newComment = ''
        this.$message.success('评论成功！')
        this.fetchComments()
      }).catch(() => {}).finally(() => {
        this.submitting = false
      })
    },
    formatDate(date) {
      return dayjs(date).format('YYYY-MM-DD HH:mm')
    },
    goBack() {
      if (this.article) {
        this.$router.push('/column/' + this.article.columnId)
      } else {
        this.$router.back()
      }
    },
    goToColumn() {
      if (this.article) {
        this.$router.push('/column/' + this.article.columnId)
      }
    }
  }
}
</script>

<style scoped>
/* 现代沉浸式博客风格 */
.article-view {
  background-color: #f7f9fc;
  min-height: 100vh;
}
.glass-header {
  line-height: 60px;
  background-color: rgba(255, 255, 255, 0.85);
  backdrop-filter: saturate(180%) blur(20px);
  border-bottom: 1px solid rgba(0,0,0,0.05);
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-inner {
  max-width: 800px;
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
.article-main {
  padding: 40px 20px;
}
.article-wrapper {
  max-width: 760px;
  margin: 0 auto;
  position: relative;
  background: #fff;
  padding: 60px 80px;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.04);
  min-height: 600px;
}
.title {
  font-size: 32px;
  color: #1a1a1a;
  line-height: 1.4;
  margin-bottom: 30px;
  font-weight: 600;
}
.meta-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}
.author-details {
  display: flex;
  flex-direction: column;
}
.author-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}
.publish-time {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}
.render-body {
  line-height: 1.8;
  font-size: 16px;
  color: #2c3e50;
  margin-bottom: 60px;
}
::v-deep .render-body p {
  margin-bottom: 1.5em;
}
::v-deep .render-body img {
  max-width: 100%;
  border-radius: 8px;
  margin: 20px 0;
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
}
::v-deep .render-body code {
  background-color: #f3f4f4;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: Menlo, Monaco, Consolas, monospace;
}
/* 渐变付费遮罩层 */
.paywall-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 70%;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  align-items: center;
  border-radius: 0 0 16px 16px;
  overflow: hidden;
}
.paywall-gradient {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(to bottom, rgba(255,255,255,0) 0%, rgba(255,255,255,0.9) 30%, rgba(255,255,255,1) 100%);
  pointer-events: none;
}
.glass-card {
  position: relative;
  z-index: 10;
  text-align: center;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  padding: 40px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 12px;
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.08);
  width: 70%;
  margin-bottom: 60px;
}
.lock-icon {
  font-size: 48px;
  background: linear-gradient(135deg, #FFB75E 0%, #ED8F03 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 15px;
}
.glass-card h3 {
  margin: 0 0 10px 0;
  color: #303133;
  font-size: 22px;
  font-weight: 600;
}
.paywall-desc {
  color: #606266;
  font-size: 14px;
  margin-bottom: 25px;
  line-height: 1.6;
}
.unlock-btn {
  padding: 12px 36px;
  font-size: 16px;
  font-weight: 500;
  border: none;
  background: linear-gradient(135deg, #409EFF 0%, #3a8ee6 100%);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.3);
  transition: transform 0.2s, box-shadow 0.2s;
}
.unlock-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(64, 158, 255, 0.4);
}
.end-mark {
  text-align: center;
  color: #c0c4cc;
  margin-top: 60px;
  font-size: 14px;
}

/* 评论区域 */
.comment-section {
  margin-top: 60px;
  padding-top: 40px;
  border-top: 1px solid #f0f2f5;
}
.comment-header {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.comment-input {
  margin-bottom: 30px;
}
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.comment-item {
  display: flex;
  gap: 15px;
  padding: 15px;
  background: #f9fafb;
  border-radius: 8px;
  transition: background 0.3s;
}
.comment-item:hover {
  background: #f0f2f5;
}
.comment-body {
  flex: 1;
}
.comment-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}
.comment-author {
  font-weight: 600;
  color: #409EFF;
  font-size: 14px;
}
.comment-time {
  font-size: 12px;
  color: #909399;
}
.comment-text {
  color: #606266;
  line-height: 1.6;
  font-size: 14px;
}
</style>
