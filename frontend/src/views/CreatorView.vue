<template>
  <div class="creator-app">
    <div class="glass-bg"></div>
    
    <!-- 顶部导航栏 - 与首页统一风格 -->
    <el-header height="60px" class="global-header is-scrolled">
      <div class="header-inner">
        <div class="logo">知识岛屿</div>
        <div class="nav-menus">
          <el-menu mode="horizontal" default-active="creator" class="transparent-menu" active-text-color="#409eff">
            <el-menu-item index="home" @click="$router.push('/')"><i class="el-icon-s-home"></i> 首页</el-menu-item>
            <el-menu-item index="creator" @click="activeTab = 'columns'"><i class="el-icon-edit-outline"></i> 创作</el-menu-item>
          </el-menu>
        </div>
        <div class="user-actions">
          <el-dropdown @command="handleCommand">
            <span class="el-dropdown-link user-dropdown">
               <el-avatar size="small" :src="user?.avatar || (user ? 'https://api.dicebear.com/7.x/avataaars/png?seed=' + user.username : 'https://api.dicebear.com/7.x/avataaars/png?seed=default')"></el-avatar>
               <span class="username">{{ user?.username }}</span>
               <i class="el-icon-arrow-down el-icon--right"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="settings"><i class="el-icon-user"></i> 个人中心</el-dropdown-item>
              <el-dropdown-item divided command="logout"><i class="el-icon-switch-button"></i> 退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </div>
    </el-header>

    <div class="main-layout">
      <el-main class="glass-main fade-in">
          <!-- 专栏管理页 -->
          <div v-show="activeTab === 'columns'" class="view-container">
            <div class="view-header">
              <h2 class="view-title">我的知识专栏</h2>
              <el-button type="primary" icon="el-icon-plus" round @click="showCreateColumn = true" class="premium-btn">创建新专栏</el-button>
            </div>
            
            <div class="column-grid">
               <div v-for="col in myColumns" :key="col.id" class="column-card-premium fade-in">
                  <div class="card-cover-wrapper">
                    <img :src="col.cover || 'https://images.unsplash.com/photo-1499750310107-5fef28a66643?auto=format&fit=crop&w=500&q=80'" class="card-cover" />
                    <div class="card-status-tag" :class="{ 'is-published': col.status === 1 }">
                      {{ col.status === 1 ? '已上架' : '草稿' }}
                    </div>
                  </div>
                  <div class="card-body">
                    <h3 class="card-title">{{ col.title }}</h3>
                    <div class="card-meta">
                      <span class="price-tag">¥{{ col.price }}</span>
                      <span class="count-tag"><i class="el-icon-document"></i> {{ col.articleCount || 0 }} 篇内容</span>
                    </div>
                    <div class="card-actions">
                      <el-button type="primary" size="small" plain round @click="manageArticles(col)">管理</el-button>
                      <el-button type="danger" size="small" plain round v-if="col.status === 1" @click="offlineColumn(col.id)">下架</el-button>
                      <el-button type="success" size="small" plain round v-else @click="publishColumn(col.id)">上架</el-button>
                    </div>
                  </div>
               </div>
               
               <el-empty v-if="myColumns.length === 0" description="您还没有建立专栏，点击上方按钮开启第一步吧" style="grid-column: span 3"></el-empty>
            </div>
          </div>
          
          <!-- 内容管理页 -->
          <div v-show="activeTab === 'articles'" class="view-container">
             <div class="view-header">
                <div class="header-left">
                  <el-button icon="el-icon-arrow-left" circle @click="activeTab = 'columns'" style="margin-right: 15px"></el-button>
                  <h2 class="view-title">《{{ currentColumn?.title }}》管理</h2>
                </div>
                <div class="header-right">
                  <el-button type="info" size="medium" round plain icon="el-icon-folder-add" @click="showCreateChapter = true">规划章节</el-button>
                  <el-button type="primary" size="medium" round icon="el-icon-edit" @click="openPublishModal">开启创作</el-button>
                </div>
             </div>

             <div class="chapters-area">
                <!-- 章节循环 -->
                <div v-for="chapter in columnChapters" :key="chapter.id" class="chapter-block">
                   <div class="chapter-header">
                      <div class="chapter-name"><i class="el-icon-collection-tag"></i> {{ chapter.title }}</div>
                      <div class="chapter-ops">
                        <el-button type="text" icon="el-icon-delete" style="color: #f56c6c" @click="deleteChapter(chapter.id)">移除章节</el-button>
                      </div>
                   </div>
                   <div class="article-list">
                      <div v-for="article in getArticlesByChapter(chapter.id)" :key="article.id" class="article-row">
                        <div class="article-title">{{ article.title }}</div>
                        <div class="article-tags">
                           <el-tag size="mini" :type="article.isFree ? 'success' : 'warning'" effect="plain">{{ article.isFree ? '公开' : '锁定' }}</el-tag>
                           <span class="order-text">排序: {{ article.sortOrder }}</span>
                        </div>
                        <div class="article-btn-group">
                          <el-button type="text" size="small" icon="el-icon-edit" @click="editArticle(article)">修改</el-button>
                          <el-button type="text" size="small" icon="el-icon-delete" style="color: #f56c6c"></el-button>
                        </div>
                      </div>
                   </div>
                </div>

                <!-- 未分类 -->
                <div v-if="getArticlesByChapter(null).length > 0" class="chapter-block">
                   <div class="chapter-header">
                      <div class="chapter-name"><i class="el-icon-document-remove"></i> 散佚文章（未归类）</div>
                   </div>
                   <div class="article-list">
                      <div v-for="article in getArticlesByChapter(null)" :key="article.id" class="article-row">
                        <div class="article-title">{{ article.title }}</div>
                        <div class="article-btn-group">
                          <el-button type="text" size="small" icon="el-icon-edit" @click="editArticle(article)">修改</el-button>
                        </div>
                      </div>
                   </div>
                </div>
                
                <el-empty v-if="columnChapters.length === 0 && currentArticles.length === 0" description="章节荒芜，期待您的笔墨"></el-empty>
             </div>
          </div>
        </el-main>
    </div>

    <!-- 弹窗部分：统一美化 -->
    <el-dialog title="创建新专栏" :visible.sync="showCreateColumn" width="500px" custom-class="glass-dialog">
      <el-form :model="columnForm" label-position="top">
        <el-form-item label="专栏名称">
          <el-input v-model="columnForm.title" placeholder="请赋予它一个响亮的名字"></el-input>
        </el-form-item>
        <el-form-item label="内容精髓">
          <el-input type="textarea" :rows="3" v-model="columnForm.description" placeholder="它将如何吸引读者？"></el-input>
        </el-form-item>
        <el-form-item label="艺术封面 (URL)">
          <el-input v-model="columnForm.cover" placeholder="建议注入 16:9 的视觉力"></el-input>
        </el-form-item>
        <el-form-item label="知识定价">
          <el-input-number v-model="columnForm.price" :min="0" :precision="2" style="width: 100%"></el-input-number>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showCreateColumn = false" round>取消</el-button>
        <el-button type="primary" @click="createColumn" round class="premium-btn">创建</el-button>
      </div>
    </el-dialog>

    <el-dialog title="章节规划" :visible.sync="showCreateChapter" width="400px" custom-class="glass-dialog">
      <el-form :model="chapterForm" label-position="top">
        <el-form-item label="章节名录">
          <el-input v-model="chapterForm.title" placeholder="如：第一章·溯源"></el-input>
        </el-form-item>
        <el-form-item label="逻辑顺序">
          <el-input-number v-model="chapterForm.sortOrder" :min="0" style="width: 100%"></el-input-number>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showCreateChapter = false" round>取消</el-button>
        <el-button type="primary" @click="createChapter" round class="premium-btn">确定</el-button>
      </div>
    </el-dialog>
    
    <!-- 创作剧场：沉浸式全屏编辑 -->
    <el-dialog 
      :visible.sync="showPublishArticle" 
      fullscreen 
      :show-close="false"
      custom-class="immersive-editor-dialog"
    >
      <div class="editor-header">
        <div class="editor-left">
          <el-button icon="el-icon-close" circle @click="showPublishArticle = false"></el-button>
          <input class="title-input" v-model="articleForm.title" placeholder="在此注入您的标题..." />
        </div>
        <div class="editor-right">
          <div class="config-item">
            <span>归属章节:</span>
            <el-select v-model="articleForm.chapterId" size="small" placeholder="无分类" clearable>
              <el-option v-for="c in columnChapters" :key="c.id" :label="c.title" :value="c.id"></el-option>
            </el-select>
          </div>
          <div class="config-item">
            <span>试看:</span>
            <el-switch v-model="articleForm.isFree" :active-value="1" :inactive-value="0"></el-switch>
          </div>
          <el-button type="primary" round class="premium-btn publish-btn" @click="publishArticle">
            {{ articleForm.id ? '更新文章' : '发布岛屿' }}
          </el-button>
        </div>
      </div>
      
      <div class="editor-body">
        <mavon-editor 
          v-model="articleForm.content" 
          placeholder="开始流淌您的思想..."
          class="premium-md-editor"
          :boxShadow="false"
          :subfield="true"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/api/request'
import { mapState, mapMutations } from 'vuex'

export default {
  data() {
    return {
      activeTab: 'columns',
      myColumns: [],
      currentColumn: null,
      currentArticles: [],
      columnChapters: [],
      showCreateColumn: false,
      columnForm: { title: '', description: '', cover: '', price: 0 },
      showCreateChapter: false,
      chapterForm: { title: '', sortOrder: 0 },
      showPublishArticle: false,
      articleForm: { id: null, title: '', content: '', chapterId: null, isFree: 0, sortOrder: 0 }
    }
  },
  computed: {
    ...mapState(['user'])
  },
  created() {
    this.fetchColumns()
  },
  methods: {
    ...mapMutations(['LOGOUT']),
    handleCommand(cmd) {
      if (cmd === 'logout') {
        this.logout()
      } else if (cmd === 'settings') {
        this.$router.push('/settings')
      } else if (cmd === 'creator') {
        this.$router.push('/creator')
      }
    },
    logout() {
      this.LOGOUT()
      this.$router.push('/login')
    },
    fetchColumns() {
      request.get('/column/my').then(res => {
        this.myColumns = res
      })
    },
    createColumn() {
      request.post('/column/create', this.columnForm).then(() => {
        this.$message.success('创作空间已建立')
        this.showCreateColumn = false
        this.fetchColumns()
      })
    },
    downColumn(id) {
      this.$confirm('确认将其从大厅撤回吗？已订阅用户仍可查看。', '撤回确认', { type: 'warning' }).then(() => {
        request.post(`/column/${id}/status?status=0`).then(() => {
          this.$message.success('已撤回至草稿状态')
          this.fetchColumns()
        })
      }).catch(() => {})
    },
    offlineColumn(id) {
      this.$confirm('确认下架该专栏吗？下架后用户将无法访问。', '下架确认', { type: 'warning' }).then(() => {
        request.post(`/column/${id}/status?status=0`).then(() => {
          this.$message.success('专栏已下架')
          this.fetchColumns()
        })
      }).catch(() => {})
    },
    publishColumn(id) {
      request.post(`/column/${id}/status?status=1`).then(() => {
        this.$message.success('专栏已上架')
        this.fetchColumns()
      })
    },
    manageArticles(col) {
      this.currentColumn = col
      this.activeTab = 'articles'
      this.fetchChapters(col.id)
      this.fetchArticles(col.id)
    },
    fetchChapters(columnId) {
      request.get('/chapter/list/' + columnId).then(res => {
        this.columnChapters = res
      })
    },
    fetchArticles(columnId) {
      request.get('/article/list/' + columnId).then(res => {
        this.currentArticles = res
      })
    },
    createChapter() {
      this.chapterForm.columnId = this.currentColumn.id
      request.post('/chapter/create', this.chapterForm).then(() => {
        this.$message.success('章节脉络已理顺')
        this.showCreateChapter = false
        this.chapterForm = { title: '', sortOrder: 0 }
        this.fetchChapters(this.currentColumn.id)
      })
    },
    deleteChapter(id) {
       this.$confirm('确认移除该章节吗？', '提示').then(() => {
          request.delete('/chapter/' + id).then(() => {
             this.$message.success('章节已移除')
             this.fetchChapters(this.currentColumn.id)
             this.fetchArticles(this.currentColumn.id)
          })
       }).catch(() => {})
    },
    getArticlesByChapter(chapterId) {
       return this.currentArticles.filter(a => a.chapterId === chapterId)
    },
    openPublishModal() {
       this.articleForm = { id: null, title: '', content: '', chapterId: null, isFree: 0, sortOrder: 0 }
       this.showPublishArticle = true
    },
    editArticle(article) {
       request.get('/article/' + article.id).then(res => {
          this.articleForm = { ...res }
          this.showPublishArticle = true
       })
    },
    publishArticle() {
      if (!this.articleForm.title || !this.articleForm.content) {
        return this.$message.warning('标题与思想不可或缺')
      }
      this.articleForm.columnId = this.currentColumn.id
      request.post('/article/publish', this.articleForm).then(() => {
         this.$message.success(this.articleForm.id ? '修改已同步' : '已发布')
         this.showPublishArticle = false
         this.fetchArticles(this.currentColumn.id)
      })
    }
  }
}
</script>

<style scoped>
/* 全局样式需要在非 scoped 区域定义，这里用相同样式覆盖 */
.global-header {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  z-index: 1000;
  background-color: transparent;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
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
  text-shadow: 0 2px 8px rgba(0,0,0,0.2);
}
.logo:hover {
  text-shadow: 0 0 10px rgba(64, 158, 255, 0.8);
}
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
  color: rgba(255,255,255,0.9);
  cursor: pointer;
  font-weight: 500;
}
.global-header.is-scrolled .user-dropdown {
  color: #333;
}
::v-deep .el-avatar > img {
  object-fit: cover !important;
  width: 100%;
  height: 100%;
}

.creator-app {
  min-height: 100vh;
  position: relative;
  background-color: #f8f9fb;
  font-family: 'Outfit', 'Inter', -apple-system, sans-serif;
}
.glass-bg {
  position: absolute;
  top: -10%;
  right: -5%;
  width: 50%;
  height: 50%;
  background: radial-gradient(circle, rgba(64,158,255,0.08) 0%, rgba(255,255,255,0) 70%);
  z-index: 0;
  pointer-events: none;
}
.main-layout {
  padding-top: 60px;
  min-height: 100vh;
  z-index: 1;
  position: relative;
}

.glass-main {
  padding: 40px;
  margin-top: 60px;
}
.view-container {
  max-width: 1200px;
  margin: 0 auto;
}
.view-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
}
.view-title {
  font-size: 28px;
  font-weight: 800;
  color: #1a1a1a;
  margin: 0;
}
.premium-btn {
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  border: none;
  box-shadow: 0 4px 14px rgba(64,158,255,0.3);
  transition: all 0.3s;
}
.premium-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(64,158,255,0.4);
}

.column-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 30px;
}
.column-card-premium {
  background: #fff;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0,0,0,0.05);
  transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
  border: 1px solid rgba(0,0,0,0.02);
}
.column-card-premium:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0,0,0,0.08);
}
.card-cover-wrapper {
  height: 180px;
  position: relative;
}
.card-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.card-status-tag {
  position: absolute;
  top: 15px;
  right: 15px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 700;
  background: rgba(0,0,0,0.5);
  color: #fff;
  backdrop-filter: blur(4px);
}
.card-status-tag.is-published {
  background: rgba(103,194,58,0.9);
}
.card-body {
  padding: 20px;
}
.card-title {
  margin: 0 0 10px 0;
  font-size: 18px;
  font-weight: 700;
}
.card-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  font-size: 13px;
}
.price-tag {
  color: #f56c6c;
  font-weight: 800;
  font-size: 16px;
}
.count-tag {
  color: #909399;
}

.chapters-area {
  padding-bottom: 100px;
}
.chapter-block {
  margin-bottom: 35px;
}
.chapter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 10px 15px 10px;
  border-bottom: 2px solid #ebeef5;
  margin-bottom: 15px;
}
.chapter-name {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
}
.article-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.article-row {
  background: #fff;
  padding: 18px 25px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
  transition: all 0.2s;
}
.article-row:hover {
  background: #fcfcfc;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}
.article-title {
  font-weight: 600;
  color: #444;
  flex: 1;
}
.article-tags {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-right: 30px;
}
.order-text {
  font-size: 12px;
  color: #909399;
}

/* 沉浸式编辑器样式 */
.immersive-editor-dialog ::v-deep .el-dialog__header {
  display: none;
}
.immersive-editor-dialog ::v-deep .el-dialog__body {
  padding: 0;
  height: 100vh;
  display: flex;
  flex-direction: column;
}
.editor-header {
  height: 70px;
  padding: 0 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}
.editor-left {
  display: flex;
  align-items: center;
  gap: 20px;
  flex: 1;
}
.title-input {
  border: none;
  font-size: 22px;
  font-weight: 700;
  width: 100%;
  outline: none;
  color: #303133;
}
.editor-right {
  display: flex;
  align-items: center;
  gap: 25px;
}
.config-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #606266;
}
.editor-body {
  flex: 1;
  overflow: hidden;
}
.premium-md-editor {
  height: 100% !important;
  border: none !important;
}

/* 弹出框毛玻璃 */
.glass-dialog {
  border-radius: 20px !important;
  overflow: hidden;
}
.glass-dialog ::v-deep .el-dialog__header {
  padding: 25px 25px 10px;
  font-weight: 800;
}
</style>
