<template>
  <div class="message-view">
    <div class="message-header">
      <h1 class="animate__animated animate__fadeInDown">留言交流 · 岛屿足迹</h1>
      <p class="animate__animated animate__fadeInUp">在这里留下你的思考，或与远方的岛民低语</p>
    </div>

    <div class="message-container">
      <!-- 发布留言 -->
      <div class="post-card glass-card animate__animated animate__fadeIn">
        <el-input
          type="textarea"
          :rows="3"
          placeholder="既然来了，就留下点什么吧..."
          v-model="newMessage"
          maxlength="200"
          show-word-limit
        ></el-input>
        <div class="post-footer">
          <el-button type="primary" round @click="submitMessage" :loading="submitting">发布留言</el-button>
        </div>
      </div>

      <!-- 留言列表 -->
      <div class="message-list">
        <div 
          v-for="(msg, index) in messages" 
          :key="msg.id" 
          class="message-item glass-card animate__animated animate__fadeInUp"
          :style="{ animationDelay: index * 0.1 + 's' }"
        >
          <div class="message-avatar">
            <el-avatar :size="50" :src="msg.avatar || defaultAvatar"></el-avatar>
          </div>
          <div class="message-content-wrap">
            <div class="message-info">
              <span class="nickname">{{ msg.nickname }}</span>
              <span class="time">{{ formatDate(msg.createTime) }}</span>
            </div>
            <div class="message-text">{{ msg.content }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import request from '@/api/request'
import dayjs from 'dayjs'

export default {
  name: 'MessageView',
  data() {
    return {
      messages: [],
      newMessage: '',
      submitting: false,
      defaultAvatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
    }
  },
  created() {
    this.fetchMessages()
  },
  methods: {
    async fetchMessages() {
      try {
        const res = await request.get('/message/list')
        this.messages = res
      } catch (e) {
        // request.js 会处理错误提示
      }
    },
    async submitMessage() {
      if (!this.newMessage.trim()) {
        this.$message.warning('请输入留言内容')
        return
      }
      this.submitting = true
      try {
        await request.post('/message/post', {
          content: this.newMessage
        })
        this.newMessage = ''
        this.$message.success('留言成功！')
        this.fetchMessages()
      } catch (e) {
        // request.js 会处理错误提示
      } finally {
        this.submitting = false
      }
    },
    formatDate(date) {
      return dayjs(date).format('YYYY-MM-DD HH:mm')
    }
  }
}
</script>

<style scoped>
.message-view {
  min-height: 100vh;
  padding: 100px 20px 50px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.message-header {
  text-align: center;
  margin-bottom: 50px;
}

.message-header h1 {
  font-size: 2.5rem;
  color: #2c3e50;
  margin-bottom: 10px;
  font-weight: 700;
}

.message-header p {
  color: #7f8c8d;
  font-size: 1.1rem;
}

.message-container {
  max-width: 800px;
  margin: 0 auto;
}

.glass-card {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.1);
  margin-bottom: 30px;
  padding: 25px;
}

.post-footer {
  margin-top: 15px;
  display: flex;
  justify-content: flex-end;
}

.message-item {
  display: flex;
  gap: 20px;
  transition: transform 0.3s ease;
}

.message-item:hover {
  transform: translateY(-5px);
}

.message-avatar {
  flex-shrink: 0;
}

.message-content-wrap {
  flex-grow: 1;
}

.message-info {
  margin-bottom: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.nickname {
  font-weight: 600;
  color: #34495e;
}

.time {
  font-size: 0.85rem;
  color: #95a5a6;
}

.message-text {
  color: #2c3e50;
  line-height: 1.6;
  font-size: 1rem;
  word-break: break-all;
}

@media (max-width: 600px) {
  .message-header h1 {
    font-size: 2rem;
  }
}
</style>
