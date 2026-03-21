<template>
  <div class="home-page">
    <!-- 顶部透明导航栏 -->
    <el-header height="60px" :class="['global-header', { 'is-scrolled': isScrolled }]">
      <div class="header-inner">
        <div class="logo">知识岛屿</div>
        <div class="nav-menus">
          <el-menu mode="horizontal" default-active="home" class="transparent-menu" active-text-color="#409eff">
            <el-menu-item index="home" @click="scrollToTop"><i class="el-icon-s-home"></i> 首页</el-menu-item>
            <el-menu-item index="about" @click="handleCreatorClick"><i class="el-icon-edit-outline"></i> 创作</el-menu-item>
          </el-menu>
        </div>
        <div class="user-actions">
          <el-button type="primary" size="small" round v-if="!isLoggedIn" @click="$router.push('/login')" class="login-btn">
            登录
          </el-button>
          <el-dropdown v-else @command="handleCommand">
            <span class="el-dropdown-link user-dropdown">
               <el-avatar size="small" :src="user?.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"></el-avatar>
               <span class="username">{{ user?.username }}</span>
               <i class="el-icon-arrow-down el-icon--right"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="settings"><i class="el-icon-user"></i> 个人中心</el-dropdown-item>
              <el-dropdown-item v-if="user?.role === 2" command="admin"><i class="el-icon-s-platform"></i> 系统运维</el-dropdown-item>
              <el-dropdown-item divided command="logout"><i class="el-icon-switch-button"></i> 退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </div>
    </el-header>

    <!-- 视差大图 Header -->
    <div class="hero-section">
       <div class="hero-bg" :style="{ transform: `translateY(${scrollY * 0.4}px)` }"></div>
       <div class="hero-content" :style="{ opacity: 1 - scrollY / 400, transform: `translateY(${scrollY * 0.2}px)` }">
          <div class="hitokoto-wrapper">
            <p class="hero-subtitle">
              <span class="hitokoto-text" :key="hitokotoKey">{{ displayedText }}</span>
              <span class="typing-cursor" :class="{ blink: !isTyping }">|</span>
            </p>
            <p class="hitokoto-source" v-if="hitokotoSource">—— {{ hitokotoSource }}</p>
          </div>
       </div>
       <!-- 下拉指示按钮 -->
       <div class="scroll-down-btn" @click="scrollToMain">
          <i class="el-icon-arrow-down"></i>
       </div>
    </div>

    <!-- 主体双栏内容 -->
    <div class="main-container">
      <el-row :gutter="24">
        <!-- 左侧边栏 -->
        <el-col :xs="24" :sm="24" :md="7" :lg="6" class="sidebar-col">
          <!-- 个人名片 -->
          <el-card class="box-card profile-card glow-hover" shadow="hover">
            <div class="profile-header">
              <el-avatar :size="90" :src="getAvatarUrl()" class="profile-avatar"></el-avatar>
              <h3 class="profile-name">{{ user ? user.username : '游客访客' }}</h3>
              <p class="profile-bio">{{ user ? (user.bio || '保持好奇心，持续建设属于自己的知识网络。') : '登录系统，解锁沉浸式阅读与创作之旅' }}</p>
            </div>
            <div class="profile-stats" v-if="isLoggedIn">
              <div class="stat-item">
                <div class="stat-value">{{ (user?.balance || 0).toFixed(2) }}</div>
                <div class="stat-label">我的余额</div>
              </div>
              <div class="stat-item" style="border-left: 1px solid #ebeef5; border-right: 1px solid #ebeef5;">
                <div class="stat-value">{{ purchasedCount }}</div>
                <div class="stat-label">已购文章</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">Lv.{{ getLevel() }}</div>
                <div class="stat-label">等级</div>
              </div>
            </div>
            <div class="profile-actions">
               <el-button type="primary" round class="action-btn" @click="goSettings">
               {{ isLoggedIn ? '前往个人中心' : '登录' }}
               </el-button>
            </div>
          </el-card>

          <!-- 搜索框 -->
          <el-card class="box-card search-card glow-hover" shadow="hover" style="margin-top: 20px;">
            <div slot="header" class="card-header">
               <span><i class="el-icon-search"></i> 站内搜索</span>
            </div>
            <el-input placeholder="搜索你感兴趣的专栏..." v-model="searchInput" clearable @keyup.enter.native="handleSearch">
              <i slot="prefix" class="el-input__icon el-icon-search"></i>
              <i slot="suffix" class="el-input__icon el-icon-search" style="cursor:pointer" @click="handleSearch"></i>
            </el-input>
          </el-card>
          
          <!-- 排行榜板块 -->
          <el-card class="box-card recommend-card glow-hover" shadow="hover" style="margin-top: 20px;">
            <div slot="header" class="card-header">
              <span><i class="el-icon-medal" style="color: #f1c40f;"></i> 热度排行榜</span>
            </div>
            <div class="rank-list">
               <div class="rank-item" v-for="(item, index) in recommendList" :key="item.id" @click="goDetail(item.id)">
                  <div class="rank-number" :class="'rank-' + (index + 1)">{{ index + 1 }}</div>
                  <div class="rank-info">
                    <h4 class="rank-title" :title="item.title">{{ item.title }}</h4>
                    <span class="rank-meta">{{ item.heat || 1000 }} 热度</span>
                  </div>
               </div>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧主内容流 -->
        <el-col :xs="24" :sm="24" :md="17" :lg="18" class="main-col">
          <el-card class="box-card feed-card transparent-card" shadow="never">
            <div class="feed-tabs-container">
              <div class="feed-tabs">
                <div :class="['tab-item', { active: feedType === 'latest' }]" @click="changeFeed('latest')">发现岛屿</div>
                <div :class="['tab-item', { active: feedType === 'my_sub' }]" v-if="isLoggedIn" @click="changeFeed('my_sub')">我的订阅</div>
              </div>
            </div>
            <div class="column-feed-grid">
               <div class="column-feed-card fade-in" v-for="(item, index) in displayColumns" :key="item.id" @click="goDetail(item.id)" :style="{ animationDelay: index * 0.1 + 's' }">
                 <div class="card-cover-box">
                    <img :src="item.cover || 'https://picsum.photos/seed/' + item.id + '/600/400'" class="card-cover" />
                    <div class="card-tag" v-if="item.price === 0">FREE</div>
                    <div class="card-price-overlay">
                       {{ item.price > 0 ? '¥' + item.price : '免费畅读' }}
                    </div>
                 </div>
                 <div class="card-info">
                    <h3 class="card-title">{{ item.title }}</h3>
                    <p class="card-desc">{{ item.description }}</p>
                    <div class="card-footer">
                       <span><i class="el-icon-user"></i> {{ item.creatorId }}</span>
                       <span class="card-time">{{ item.createTime || '03-20' }}</span>
                    </div>
                 </div>
               </div>
            </div>

            <div class="empty-state" v-if="displayColumns.length === 0">
              <el-empty description="什么都没找到呢~"></el-empty>
            </div>

            <div style="text-align: center; margin-top: 30px;" v-if="displayColumns.length > 0">
              <el-button round>加载更多 <i class="el-icon-arrow-down"></i></el-button>
            </div>

          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import { mapState, mapGetters, mapMutations } from 'vuex'
import request from '@/api/request'

export default {
  name: 'HomeView',
  data() {
    return {
      isScrolled: false,
      scrollY: 0,
      searchKey: '',
      searchInput: '',
      feedType: 'latest',
      columns: [],
      scrollListener: null,
      hitokoto: '',
      hitokotoSource: '',
      hitokotoKey: 0,
      displayedText: '',
      isTyping: false,
      typingTimer: null,
      purchasedCount: 0,
      // 备用一言（文学、影视、哲学）
      fallbackHitokotos: [
        { text: '满地黄花堆积，憔悴损，如今有谁堪摘', source: '李清照《声声慢》' },
        { text: '所有的大人都曾经是小孩，虽然，只有少数的人记得', source: '圣埃克苏佩里《小王子》' },
        { text: '我唯一知道的就是我一无所知', source: '苏格拉底' },
        { text: '人生若只如初见，何事秋风悲画扇', source: '纳兰性德《木兰花》' },
        { text: '当你凝视深渊时，深渊也在凝视你', source: '尼采《善恶的彼岸》' }
      ]
    }
  },
  computed: {
    ...mapState(['user']),
    ...mapGetters(['isLoggedIn']),
    recommendList() {
      // 挑出前几个作为推荐（打散或随缘展现）
      return [...this.columns].sort((a,b) => b.id - a.id).slice(0, 4)
    },
    displayColumns() {
      let filtered = this.columns
      if (this.searchKey) {
        filtered = filtered.filter(col => col.title.includes(this.searchKey))
      }
      return filtered
    }
  },
  created() {
    this.fetchColumns()
    this.fetchHitokoto()
    if (this.$store.getters.isLoggedIn) {
      this.fetchMySubscriptions()
    }
  },
  mounted() {
    this.fetchHitokoto()
    this.scrollListener = () => {
      this.scrollY = window.scrollY
      this.isScrolled = window.scrollY > 50
    }
    window.addEventListener('scroll', this.scrollListener)
  },
  beforeDestroy() {
    window.removeEventListener('scroll', this.scrollListener)
    if (this.typingTimer) clearInterval(this.typingTimer)
  },
  methods: {
    ...mapMutations(['LOGOUT']),
    handleCreatorClick() {
      if (this.isLoggedIn) {
        this.$router.push('/creator')
      } else {
        this.$confirm('请先登录后再进行创作', '提示', {
          confirmButtonText: '去登录',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.$router.push('/login')
        }).catch(() => {})
      }
    },
    getAvatarUrl() {
      if (this.user?.avatar) return this.user.avatar
      if (!this.user) {
        return 'https://api.dicebear.com/7.x/identicon/svg?seed=guest&backgroundColor=e0e7ff'
      }
      const seed = this.user.username
      const avatars = [
        `https://api.dicebear.com/7.x/avataaars/svg?seed=${seed}`,
        `https://api.multiavatar.com/${seed}.svg`,
        `https://ui-avatars.com/api/?name=${encodeURIComponent(seed)}&background=random&size=90`
      ]
      return avatars[0]
    },
    async fetchHitokoto() {
      try {
        // 只获取诗词
        const response = await fetch('https://v1.hitokoto.cn/?c=g')
        const data = await response.json()
        this.hitokotoKey++
        this.hitokoto = data.hitokoto
        this.hitokotoSource = data.from_book ? `${data.from}《${data.from_book}》` : (data.from ? `${data.from}` : '')
        this.startTypingAnimation(data.hitokoto)
      } catch (e) {
        console.error('获取一言失败:', e)
        // 使用备用一言列表，随机选一条
        const fallback = this.fallbackHitokotos[
          Math.floor(Math.random() * this.fallbackHitokotos.length)
        ]
        this.hitokotoKey++
        this.hitokoto = fallback.text
        this.hitokotoSource = fallback.source
        this.startTypingAnimation(fallback.text)
      }
    },
    startTypingAnimation(text) {
      if (this.typingTimer) clearInterval(this.typingTimer)
      this.displayedText = ''
      this.isTyping = true
      let index = 0
      this.typingTimer = setInterval(() => {
        if (index < text.length) {
          this.displayedText += text[index]
          index++
        } else {
          clearInterval(this.typingTimer)
          this.isTyping = false
        }
      }, 80)
    },
    handleCommand(command) {
      if (command === 'logout') {
        this.LOGOUT()
        this.$message.success('已安全退出')
      } else if (command === 'settings') {
        this.$router.push('/settings')
      } else if (command === 'creator') {
        this.$router.push('/creator')
      } else if (command === 'admin') {
        this.$router.push('/admin')
      }
    },
    goSettings() {
      if (!this.isLoggedIn) {
         this.$router.push('/login')
      } else {
         this.$router.push('/settings')
      }
    },
    scrollToTop() {
      window.scrollTo({ top: 0, behavior: 'smooth' })
    },
    scrollToMain() {
      const mainEl = this.$el.querySelector('.main-container')
      if (mainEl) mainEl.scrollIntoView({ behavior: 'smooth' })
    },
    handleSearch() {
      this.searchKey = this.searchInput
      if (this.displayColumns.length > 0) {
        this.$message.success(`找到 ${this.displayColumns.length} 个相关专栏`)
      } else {
        this.$message.warning('没有找到匹配的专栏')
      }
    },
    fetchColumns() {
      if (this.feedType === 'my_sub') {
        request.get('/subscription/my').then(res => {
          this.columns = res
        })
      } else {
        request.get('/column/list/public').then(res => {
          this.columns = res
        })
      }
    },
    fetchMySubscriptions() {
      request.get('/subscription/my').then(res => {
        this.purchasedCount = res ? res.length : 0
      }).catch(() => {
        this.purchasedCount = 0
      })
    },
    getLevel() {
      if (this.purchasedCount >= 6) return 3
      if (this.purchasedCount >= 3) return 2
      return 1
    },
    changeFeed(type) {
      this.feedType = type
      this.fetchColumns()
    },
    goDetail(id) {
      this.$router.push('/column/' + id)
    }
  }
}
</script>

<style scoped>
/* 全局页面样式重置感 */
.home-page {
  background-color: #f4f6fa;
  min-height: 100vh;
  margin: 0;
  padding: 0;
}

/* Navbar */
.global-header {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  z-index: 1000;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  background-color: transparent;
  padding: 0;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}
.global-header.is-scrolled {
  background-color: rgba(255, 255, 255, 0.85);
  backdrop-filter: saturate(180%) blur(20px);
  -webkit-backdrop-filter: saturate(180%) blur(20px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  border-bottom: none;
}
.global-header.is-scrolled .logo,
.global-header.is-scrolled ::v-deep .el-menu-item {
  color: #333 !important;
}

/* 侧边栏固定 */
.sidebar-col {
  position: sticky;
  top: 80px;
  align-self: flex-start;
}
.header-inner {
  max-width: 1300px;
  margin: 0 auto;
  height: 60px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}
.logo {
  font-size: 24px;
  font-weight: 800;
  color: #fff;
  letter-spacing: 1.5px;
  transition: all 0.3s;
}
.logo:hover {
  text-shadow: 0 0 10px rgba(64, 158, 255, 0.8);
}

/* 解决 Element UI 水平菜单默认的下边框和背景干扰 */
.transparent-menu.el-menu--horizontal {
  background: transparent !important;
  border-bottom: none !important;
}
.transparent-menu ::v-deep .el-menu-item {
  color: rgba(255,255,255,0.9);
  font-size: 15px;
  background-color: transparent !important;
  border-bottom: 2px solid transparent !important;
}
.transparent-menu ::v-deep .el-menu-item:not(.is-disabled):hover,
.transparent-menu ::v-deep .el-menu-item.is-active {
  color: #409eff !important;
  background-color: transparent !important;
  border-bottom: 2px solid #409eff !important;
}
.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fff;
  cursor: pointer;
  font-weight: 500;
  transition: color 0.3s;
}
.global-header.is-scrolled .user-dropdown {
  color: #333;
}
::v-deep .el-avatar > img {
  object-fit: cover !important;
  width: 100%;
  height: 100%;
}
.login-btn {
  background: rgba(255,255,255,0.2) !important;
  border: 1px solid rgba(255,255,255,0.4) !important;
  color: #fff !important;
}
.global-header.is-scrolled .login-btn {
  background: #409eff !important;
  border-color: #409eff !important;
}

/* 视差背景区 */
.hero-section {
  position: relative;
  height: 500px;
  background-color: #000;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}
.hero-bg {
  position: absolute;
  top: -50px; left: 0; right: 0; bottom: -50px;
  background-image: url('https://images.unsplash.com/photo-1451187580459-43490279c0fa?ixlib=rb-1.2.1&auto=format&fit=crop&w=1920&q=80');
  background-size: cover;
  background-position: center;
  opacity: 0.6;
  z-index: 1;
}
.hero-content {
  position: relative;
  z-index: 2;
  text-align: center;
  color: #fff;
  padding: 0 20px;
}
.hero-title {
  font-size: 56px;
  font-weight: 900;
  margin: 0 0 20px 0;
  text-shadow: 0 4px 16px rgba(0,0,0,0.4);
  background: linear-gradient(to right, #ffffff, #dcdcdc);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: fadeUp 1s ease;
}
.hero-subtitle {
  font-size: 28px;
  font-weight: 500;
  letter-spacing: 2px;
  text-shadow: 0 2px 8px rgba(0,0,0,0.5);
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 2px;
}
.hitokoto-text {
  animation: fadeIn 0.3s ease;
}
.typing-cursor {
  font-weight: 400;
  color: rgba(255, 255, 255, 0.9);
  animation: blink 1s step-end infinite;
}
@keyframes fadeIn {
  from { opacity: 0.8; }
  to { opacity: 1; }
}
@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}
.hitokoto-wrapper {
  min-height: 1.5em;
}
.hitokoto-source {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 10px;
  font-style: italic;
}
.scroll-down-btn {
  position: absolute;
  bottom: 40px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 15;
  color: #fff;
  font-size: 54px; /* 放大按钮 */
  cursor: pointer;
  animation: bounce 2s infinite;
  text-shadow: 0 0 20px rgba(0,0,0,0.5);
  transition: all 0.3s;
}
.scroll-down-btn:hover {
  color: #409EFF;
  transform: translateX(-50%) scale(1.1);
}
@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {transform: translateX(-50%) translateY(0);}
  40% {transform: translateX(-50%) translateY(-10px);}
  60% {transform: translateX(-50%) translateY(-5px);}
}

/* 核心内容区 */
.main-container {
  max-width: 1300px;
  margin: 20px auto 40px;
  position: relative;
  z-index: 10;
  padding: 0 20px;
  display: flex;
  flex-wrap: wrap;
}
.main-container > .el-row {
  width: 100%;
  display: flex;
  flex-wrap: wrap;
}

/* 悬浮白底卡片共用 */
.box-card {
  border: none;
  border-radius: 12px;
  background-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}
.transparent-card {
  background-color: transparent;
}
.transparent-card ::v-deep .el-card__header {
  border-bottom: none;
  padding-bottom: 0;
}
.glow-hover {
  transition: transform 0.3s, box-shadow 0.3s;
}
.glow-hover:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 32px rgba(0,0,0,0.06) !important;
}

/* Profile Card */
.profile-header {
  padding-top: 10px;
}
.profile-card {
  text-align: center;
  padding: 20px 10px 10px;
  background: rgba(255, 255, 255, 0.7) !important;
  border: 1px solid rgba(255, 255, 255, 0.3) !important;
  backdrop-filter: blur(20px) saturate(180%);
}
.profile-avatar {
  border: 4px solid #fff;
  box-shadow: 0 0 15px rgba(64, 158, 255, 0.3);
  margin-top: -50px;
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  animation: borderBreath 3s infinite ease-in-out;
  overflow: visible;
}
@keyframes borderBreath {
  0%, 100% { box-shadow: 0 0 15px rgba(64, 158, 255, 0.3); border-color: #fff; }
  50% { box-shadow: 0 0 30px rgba(64, 158, 255, 0.6); border-color: #6CB6FF; }
}
.profile-avatar:hover {
  transform: rotate(360deg) scale(1.1);
}
.profile-name {
  font-size: 20px;
  margin: 15px 0 8px;
  color: #303133;
  font-weight: 800;
}
.profile-bio {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
  padding: 0 20px;
  margin-bottom: 20px;
}
.profile-stats {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
  padding: 15px 0;
  border-top: 1px solid #f0f2f5;
  border-bottom: 1px solid #f0f2f5;
}
.stat-item {
  flex: 1;
}
.stat-value {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}
.stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.action-btn {
  font-weight: 600;
  background: linear-gradient(135deg, #409EFF, #6CB6FF);
  border: none;
  box-shadow: 0 4px 12px rgba(64,158,255,0.4);
}
.action-btn:hover {
  box-shadow: 0 6px 16px rgba(64,158,255,0.6);
}

/* Rank List */
.rank-list {
  padding: 5px 0;
}
.rank-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 12px 10px;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.3s;
}
.rank-item:hover {
  background: rgba(64, 158, 255, 0.05);
}
.rank-number {
  font-size: 18px;
  font-weight: 900;
  color: #95a5a6;
  font-style: italic;
  width: 25px;
  flex-shrink: 0;
}
.rank-1 { color: #f1c40f; font-size: 22px; }
.rank-2 { color: #bdc3c7; font-size: 20px; }
.rank-3 { color: #e67e22; font-size: 19px; }

.rank-info {
  flex: 1;
  overflow: hidden;
}
.rank-title {
  margin: 0 0 4px 0;
  font-size: 14px;
  color: #2c3e50;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.rank-meta {
  font-size: 12px;
  color: #95a5a6;
}

/* Feed Grid Styles */
.column-feed-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
  margin-top: 10px;
}
.column-feed-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  border: 1px solid #f0f2f5;
}
.column-feed-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0,0,0,0.1);
}
.card-cover-box {
  position: relative;
  width: 100%;
  height: 180px;
  overflow: hidden;
  background: #f8f9fa;
}
.card-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.8s cubic-bezier(0.165, 0.84, 0.44, 1);
}
.column-feed-card:hover .card-cover {
  transform: scale(1.1);
}
.card-tag {
  position: absolute;
  top: 12px;
  left: 12px;
  background: rgba(103, 194, 58, 0.9);
  color: #fff;
  font-size: 11px;
  padding: 3px 10px;
  border-radius: 6px;
  backdrop-filter: blur(4px);
  font-weight: bold;
}
.card-price-overlay {
  position: absolute;
  bottom: 12px;
  right: 12px;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(8px);
  color: #fff;
  padding: 4px 12px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: bold;
}
.card-info {
  padding: 16px 20px 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}
.card-title {
  margin: 0 0 10px 0;
  font-size: 18px;
  color: #2c3e50;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
  overflow: hidden;
  transition: color 0.3s;
}
.column-feed-card:hover .card-title {
  color: #409EFF;
}
.card-desc {
  font-size: 13px;
  color: #606266;
  margin-bottom: 16px;
  line-height: 1.6;
  flex: 1;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #909399;
  border-top: 1px solid #f5f7fa;
  padding-top: 12px;
}

/* Feed Tabs Styles */
.feed-tabs-container {
  margin-bottom: 25px;
  border-bottom: 1px solid #f0f2f5;
}
.feed-tabs {
  display: flex;
  gap: 35px;
}
.tab-item {
  padding: 12px 5px;
  font-size: 18px;
  color: #606266;
  cursor: pointer;
  position: relative;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 500;
}
.tab-item:hover {
  color: #409EFF;
}
.tab-item.active {
  color: #303133;
  font-weight: 800;
}
.tab-item.active::after {
  content: "";
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(to right, #409EFF, #6CB6FF);
  border-radius: 4px;
}

@keyframes fadeUp {
  0% { transform: translateY(30px); opacity: 0; }
  100% { transform: translateY(0); opacity: 1; }
}

@media screen and (max-width: 900px) {
  .column-feed-grid {
    grid-template-columns: 1fr;
  }
}
</style>
