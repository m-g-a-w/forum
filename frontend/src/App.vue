<template>
  <div id="app">
    <transition name="page-fade" mode="out-in">
      <router-view/>
    </transition>
  </div>
</template>

<style>
body {
  margin: 0;
  padding: 0;
  font-family: "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "Helvetica Neue", Helvetica, Arial, sans-serif;
  background-color: #f5f7fa;
  color: #303133;
}
#app {
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* 全局渐入动画类 */
.fade-in {
  animation: fadeIn 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94) both;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* 路由切换过度效果 */
.page-fade-enter-active, .page-fade-leave-active {
  transition: opacity 0.2s ease;
}
.page-fade-enter {
  opacity: 0;
}
.page-fade-leave-to {
  opacity: 0;
}
</style>
