<template>
  <background desc="欢迎来到「软工导论学习平台」">
    <el-container>
      <el-header>
        <h2>主页</h2>
      </el-header>
      <el-main>
        <el-menu :router="true" mode="horizontal">
          <menu-item :icon="classIcon" title="知识点查看与大模型答疑" route="/knowledge-view-page"></menu-item>
          <menu-item :icon="classIcon" title="知识点添加与修改" route="/modify-view-page"></menu-item>
        </el-menu>

        <!-- 登录弹窗 -->
        <el-dialog :visible.sync="notHaveUsername" @close="closeLoginModal" width="60%"
          :before-close="handleBeforeClose" :modal="true" :lock-scroll="true" :center="true" :append-to-body="true">
          <log-in @login-success="closeLoginModal" />
        </el-dialog>
      </el-main>
    </el-container>
  </background>
</template>

<script>
import BackGround from './BackGround.vue';
import MenuItem from './MenuItem.vue';
import Login from './Login.vue';
import classIcon from "../assets/menu-icon/classIcon.svg";
import { Container, Header, Main, Menu, Dialog } from 'element-ui';

export default {
  name: 'HomePage',
  components: {
    "background": BackGround,
    "menu-item": MenuItem,
    "log-in": Login,  // 注册登录组件
    "el-container": Container,
    "el-header": Header,
    "el-main": Main,
    "el-menu": Menu,
    "el-dialog": Dialog,
  },
  data() {
    return {
      classIcon,
      notHaveUsername: true,
    };
  },
  mounted() {
    if (localStorage.getItem('username') != null) {
      this.notHaveUsername = false;
    }
    // 页面加载时判断是否显示弹窗

    console.log('无用户名:', this.notHaveUsername);
  },
  methods: {
    // 关闭登录弹窗
    closeLoginModal() {
      this.haveUsername = false;  // 清空用户名，关闭弹窗
    },
    // 阻止关闭弹窗时点击背景
    handleBeforeClose(done) {
      done();  // 可以执行一些操作后再关闭弹窗
    }
  }
};
</script>

<style scoped>
/* 扩大登录弹窗的宽度 */
.el-dialog {
  width: 60%;
  max-width: 800px;
  min-width: 400px;
}

/* 如果你希望弹窗内容滚动时出现滚动条 */
.el-dialog__body {
  max-height: 70vh;
  /* 限制弹窗最大高度 */
  overflow-y: auto;
  /* 内容过多时出现滚动条 */
}
</style>
