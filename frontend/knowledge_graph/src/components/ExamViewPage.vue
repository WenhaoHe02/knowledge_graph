<template>
  <background title="试卷生成与批改" desc="在线试卷生成与批改系统">
    <el-tabs v-model="activeMainTab" @tab-click="handleMainTabClick">
      <!-- 试卷生成与批改 Tab -->
      <el-tab-pane label="试卷生成与批改" name="paperSystem">
        <el-tabs v-model="activeSubTab" @tab-click="handleSubTabClick">
          <el-tab-pane label="生成试卷" name="examGeneration" v-if="isTeacher || isStudent">
            <keep-alive>
              <exam-generation v-if="activeSubTab === 'examGeneration'" :key="activeSubTab" />
            </keep-alive>
          </el-tab-pane>
          <el-tab-pane label="批改试卷" name="examCorrection" v-if="isTeacher || isStudent">
            <keep-alive>
              <exam-correction v-if="activeSubTab === 'examCorrection'" :key="activeSubTab" />
            </keep-alive>
          </el-tab-pane>
          <el-tab-pane label="答题试卷" name="examSelect" v-if="isStudent || isTeacher">
            <keep-alive>
              <exam-select v-if="activeSubTab === 'examSelect'" :key="activeSubTab" />
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
import ExamGeneration from "./ExamGeneration.vue";
import ExamSelect from "./ExamSelect.vue";
import ExamCorrection from "./ExamCorrection.vue";

export default {
  components: {
    background: Background,
    'el-tabs': Tabs,
    'el-tab-pane': TabPane,
    'exam-generation': ExamGeneration,
    'exam-select': ExamSelect,
    'exam-correction': ExamCorrection,
  },
  data() {
    return {
      activeMainTab: 'paperSystem',
      activeSubTab: 'examSelect',
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
      this.activeSubTab = 'examSelect';  // 默认选择第一个子tab
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