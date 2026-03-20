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
  animation: fadeIn 0.6s ease both;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 路由切换过度效果 */
.page-fade-enter-active, .page-fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}
.page-fade-enter {
  opacity: 0;
  transform: translateY(5px);
}
.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-5px);
}
</style>
