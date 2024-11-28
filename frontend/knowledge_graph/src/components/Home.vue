<template>
  <background desc="欢迎来到「软工导论学习平台」">
    <el-container>
      <el-header>
        <h2>主页</h2>
      </el-header>
      <el-main>
        <el-menu :router="true" mode="horizontal">
          <!-- Menu items with icons -->
          <menu-item :icon="classIcon" v-if="functions['知识点搜索']" title="知识点搜索" route="/knowledge-search"></menu-item>
          <menu-item :icon="classIcon" v-if="functions['知识点添加']" title="知识点添加" route="/knowledge-adding"></menu-item>
          <menu-item :icon="classIcon" v-if="functions['知识点修改']" title="知识点修改" route="/knowledge-changing"></menu-item>
          <menu-item :icon="classIcon" v-if="functions['试卷生成']" title="试卷生成" route="/exam-generation"></menu-item>
          <menu-item :icon="classIcon" v-if="functions['试卷批改']" title="试卷批改" route="/exam-correction"></menu-item>
          <menu-item :icon="classIcon" v-if="functions['试卷答题']" title="试卷答题" route="/exam-select"></menu-item>
          <menu-item :icon="classIcon" v-if="functions['问答']" title="问答" route="/qna"></menu-item>
          <menu-item :icon="classIcon" v-if="functions['试卷内容']" title="试卷内容" route="/exam-answer"></menu-item>
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
      // Assuming these are defined based on user permissions or settings
      functions: {
        '知识点搜索': true,
        '知识点详情': true,
        '知识点添加': true,
        '知识点修改': true,
        '试卷生成': true,
        '试卷批改': true,
        '试卷答题': true,
        '问答': true,
        '试卷内容': true,
      },

    };
  }
};
</script>
