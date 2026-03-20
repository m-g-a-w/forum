<template>
  <div class="creator-app">
    <!-- 背景层：增加一点氛围感 -->
    <div class="glass-bg"></div>
    
    <el-container class="main-layout">
      <!-- 侧边栏：毛玻璃风格 -->
      <el-aside width="260px" class="glass-aside">
        <div class="aside-header">
          <div class="brand-logo">
            <i class="el-icon-brush"></i>
            <span>CREATOR</span>
          </div>
        </div>
        
        <div class="aside-menu-wrapper">
          <div 
            :class="['menu-item', { active: activeTab === 'columns' }]" 
            @click="activeTab = 'columns'"
          >
            <i class="el-icon-collection"></i> 专栏智库
          </div>
          <div 
            class="menu-item" 
            @click="$router.push('/')"
          >
            <i class="el-icon-position"></i> 岛屿大厅
          </div>
          <div 
            class="menu-item logout-item" 
            @click="logout"
          >
            <i class="el-icon-turn-off"></i> 退出系统
          </div>
        </div>

        <div class="aside-footer">
          <el-avatar :size="40" :src="user?.avatar" class="user-avatar"></el-avatar>
          <div class="user-info-text">
            <div class="user-name">{{ user?.username }}</div>
            <div class="user-role">内容创作者</div>
          </div>
        </div>
      </el-aside>

      <el-container class="content-wrapper">
        <el-main class="glass-main fade-in">
          <!-- 专栏管理页 -->
          <div v-show="activeTab === 'columns'" class="view-container">
            <div class="view-header">
              <h2 class="view-title">我的知识专栏</h2>
              <el-button type="primary" icon="el-icon-plus" round @click="showCreateColumn = true" class="premium-btn">构造新专栏</el-button>
            </div>
            
            <div class="column-grid">
               <div v-for="col in myColumns" :key="col.id" class="column-card-premium fade-in">
                  <div class="card-cover-wrapper">
                    <img :src="col.cover || 'https://images.unsplash.com/photo-1499750310107-5fef28a66643?auto=format&fit=crop&w=500&q=80'" class="card-cover" />
                    <div class="card-status-tag" :class="{ 'is-published': col.status === 1 }">
                      {{ col.status === 1 ? '已步入大厅' : '草稿构思' }}
                    </div>
                  </div>
                  <div class="card-body">
                    <h3 class="card-title">{{ col.title }}</h3>
                    <div class="card-meta">
                      <span class="price-tag">¥{{ col.price }}</span>
                      <span class="count-tag"><i class="el-icon-document"></i> 0 篇内容</span>
                    </div>
                    <div class="card-actions">
                      <el-button type="primary" size="small" plain round @click="manageArticles(col)">深度管理</el-button>
                      <el-button type="danger" size="small" plain round icon="el-icon-bottom" v-if="col.status === 1" @click="downColumn(col.id)"></el-button>
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
                          <el-button type="text" size="small" icon="el-icon-edit" @click="editArticle(article)">修正</el-button>
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
                          <el-button type="text" size="small" icon="el-icon-edit" @click="editArticle(article)">修正</el-button>
                        </div>
                      </div>
                   </div>
                </div>
                
                <el-empty v-if="columnChapters.length === 0 && currentArticles.length === 0" description="章节荒芜，期待您的笔墨"></el-empty>
             </div>
          </div>
        </el-main>
      </el-container>
    </el-container>

    <!-- 弹窗部分：统一美化 -->
    <el-dialog title="塑造新专栏" :visible.sync="showCreateColumn" width="500px" custom-class="glass-dialog">
      <el-form :model="columnForm" label-position="top">
        <el-form-item label="专栏冠名">
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
        <el-button @click="showCreateColumn = false" round>暂缓</el-button>
        <el-button type="primary" @click="createColumn" round class="premium-btn">立即雕刻</el-button>
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
            {{ articleForm.id ? '更新智慧' : '发布岛屿' }}
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
        request.post(`/admin/column/status?id=${id}&status=0`).then(() => {
          this.$message.success('已撤回至草稿状态')
          this.fetchColumns()
        })
      }).catch(() => {})
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
         this.$message.success(this.articleForm.id ? '修正已同步' : '新思想已发布')
         this.showPublishArticle = false
         this.fetchArticles(this.currentColumn.id)
      })
    }
  }
}
</script>

<style scoped>
.creator-app {
  height: 100vh;
  position: relative;
  overflow: hidden;
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
  height: 100vh;
  z-index: 1;
  position: relative;
}
.glass-aside {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  border-right: 1px solid rgba(0,0,0,0.05);
  display: flex;
  flex-direction: column;
  padding: 30px 20px;
}
.aside-header {
  margin-bottom: 50px;
}
.brand-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 22px;
  font-weight: 900;
  color: #303133;
  letter-spacing: 2px;
}
.brand-logo i {
  color: #409EFF;
}
.aside-menu-wrapper {
  flex: 1;
}
.menu-item {
  padding: 14px 20px;
  margin-bottom: 10px;
  border-radius: 12px;
  color: #606266;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 12px;
}
.menu-item i {
  font-size: 18px;
}
.menu-item:hover {
  background: rgba(64,158,255,0.05);
  color: #409EFF;
}
.menu-item.active {
  background: #409EFF;
  color: #fff;
  box-shadow: 0 4px 15px rgba(64,158,255,0.3);
}
.logout-item {
  margin-top: 40px;
  color: #909399;
}
.logout-item:hover {
  color: #f56c6c;
  background: rgba(245,108,108,0.05);
}
.aside-footer {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px;
  background: rgba(0,0,0,0.03);
  border-radius: 16px;
}
.user-info-text .user-name {
  font-weight: 700;
  font-size: 14px;
}
.user-info-text .user-role {
  font-size: 11px;
  color: #909399;
}

.content-wrapper {
  background: transparent;
}
.glass-main {
  padding: 40px;
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
