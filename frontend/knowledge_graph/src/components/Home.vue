<template>
  <background desc="欢迎来到「软工导论学习平台」">
    <el-container>
      <el-header>
        <h2>主页</h2>
      </el-header>
      <el-main>
        <el-menu :router="true" mode="horizontal">
          <!-- Menu items with icons -->
          <menu-item :icon="classIcon" title="知识点查看与大模型答疑" route="/knowledge-view-page"></menu-item>
          <menu-item :icon="classIcon" title="知识点添加与修改" route="/modify-view-page"></menu-item>
        </el-menu>
      </el-main>
    </el-container>
  </background>
</template>

<script>
import BackGround from './BackGround.vue';
import MenuItem from './MenuItem.vue';

// Importing SVG files as Vue components (you can use Vue's require or import statements for this)
import classIcon from "../assets/menu-icon/classIcon.svg"

export default {
  name: 'HomePage',
  components: {
    "background": BackGround,
    "menu-item": MenuItem,
    // Make sure you register the menu-item component if needed
  },
  data() {
    return {
      classIcon,
      // Assuming these are defined based on user permissions or setting
    };
  }
};
</script>