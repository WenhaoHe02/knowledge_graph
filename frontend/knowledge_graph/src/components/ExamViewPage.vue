<template>
  <background title="试卷生成与批改" desc="在线试卷生成与批改系统">
    <el-tabs v-model="activeMainTab" @tab-click="handleMainTabClick">
      <!-- 试卷生成与批改 Tab -->
      <el-tab-pane label="试卷生成与批改" name="paperSystem">
        <el-tabs v-model="activeSubTab" @tab-click="handleSubTabClick">
          <el-tab-pane label="生成试卷" name="generatePaper" v-if="isTeacher">
            <keep-alive>
              <generate-paper v-if="activeSubTab === 'generatePaper'" :key="activeSubTab" />
            </keep-alive>
          </el-tab-pane>
          <el-tab-pane label="批改试卷" name="gradePaper" v-if="isTeacher">
            <keep-alive>
              <grade-paper v-if="activeSubTab === 'gradePaper'" :key="activeSubTab" />
            </keep-alive>
          </el-tab-pane>
          <el-tab-pane label="答题试卷" name="answerTest" v-if="isStudent || isTeacher">
            <keep-alive>
              <answer-test v-if="activeSubTab === 'answerTest'" :key="activeSubTab" />
            </keep-alive>
          </el-tab-pane>
        </el-tabs>
      </el-tab-pane>
    </el-tabs>
  </background>
</template>

<script>
import Background from "./BackGround.vue";
import { Tabs, TabPane } from 'element-ui';
import GeneratePaper from "../components/paperSystem/GeneratePaper.vue";
import GradePaper from "../components/paperSystem/GradePaper.vue";
import AnswerTest from "../components/paperSystem/AnswerTest.vue";

export default {
  components: {
    background: Background,
    'el-tabs': Tabs,
    'el-tab-pane': TabPane,
    'generate-paper': GeneratePaper,
    'grade-paper': GradePaper,
    'answer-test': AnswerTest,
  },
  data() {
    return {
      activeMainTab: 'paperSystem',
      activeSubTab: 'generatePaper',
      role: null,
    };
  },
  computed: {
    isStudent() {
      return this.role === '学生';
    },
    isTeacher() {
      return this.role === '教师';
    },
  },
  methods: {
    handleMainTabClick(tab) {
      this.activeMainTab = tab.name;
      this.activeSubTab = 'generatePaper';  // 默认选择第一个子tab
    },
    handleSubTabClick(tab) {
      this.activeSubTab = tab.name;
    },
  },
  created() {
    // 模拟角色设置（可以从接口获取）
    this.role = '学生';  // 可以修改为动态获取
  }
};
</script>

<style scoped>
.app-tabs {
  position: flex;
  display: flex;
  width: 100%;
  height: 100%;
  flex-direction: column;
  align-items: center;
}
</style>