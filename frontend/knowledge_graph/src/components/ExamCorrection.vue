<template>
  <div class="exam-correction">
    <h3>试卷批改页面</h3>

    <!-- 选择试卷 -->
    <el-select v-model="selectedExamId" placeholder="选择试卷" style="width: 300px;" @change="fetchExamAndAnswers">
      <el-option v-for="exam in examList" :key="exam.examId" :label="exam.examTitle" :value="exam.examId"></el-option>
    </el-select>

    <!-- 用户作答选择 -->
    <el-select v-model="selectedAnswerId" placeholder="选择用户作答" style="width: 300px; margin-top: 20px;">
      <el-option v-for="answer in userAnswers" :key="answer.id" :label="`用户作答 - ${answer.id}`"
        :value="answer.id"></el-option>
    </el-select>

    <!-- 批改按钮 -->
    <div style="margin-top: 20px;">
      <el-button type="primary" @click="submitCorrection" :disabled="!selectedExamId || !selectedAnswerId">
        批改
      </el-button>
    </div>

    <!-- 批改结果展示 -->
    <div v-if="correctionResult" style="margin-top: 20px;">
      <h4>批改结果</h4>
      <p>总分: {{ correctionResult.totalScore }}</p>
      <el-table :data="correctionResult.correctionResults" border>
        <el-table-column prop="feedback" label="反馈"></el-table-column>
        <el-table-column prop="score" label="得分"></el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
import axios from "axios";
import { Select, Option, Table, TableColumn, Button } from "element-ui";

export default {
  name: "ExamCorrection",
  components: {
    "el-select": Select,
    "el-option": Option,
    "el-table": Table,
    "el-table-column": TableColumn,
    "el-button": Button,
  },
  data() {
    return {
      examList: [], // 所有试卷列表
      selectedExamId: "", // 当前选择的试卷 ID
      userAnswers: [], // 用户作答列表
      selectedAnswerId: "", // 当前选择的用户作答 ID
      correctionResult: null, // 批改结果
      exam: null, // 当前试卷内容
    };
  },
  methods: {
    // 获取试卷列表
    async fetchExamList() {
      try {
        console.log("加载试卷");
        const username = localStorage.getItem('username');
        const response = await axios.get(`http://localhost:8083/api/exam/getExam`, {
          params: { username },
        });
        console.log("加载试卷");
        this.examList = response.data.map(exam => ({
          examId: exam.examId,
          examTitle: exam.examTitle,
        }));
      } catch (error) {
        this.$message.error("加载试卷列表失败");
      }
    },
    // 获取试卷内容和用户作答
    async fetchExamAndAnswers() {
      try {
        const username = localStorage.getItem('username');
        const examResponse = await axios.get(`http://localhost:8083/api/exam/getExam`, {
          params: { username, examId: this.selectedExamId },
        });
        this.exam = examResponse.data[0];

        const answersResponse = await axios.get(`http://localhost:8083/api/exam/getAnswer`, {
          params: { username, examId: this.selectedExamId },
        });
        this.userAnswers = answersResponse.data.userAnswers;
      } catch (error) {
        this.$message.error("加载试卷或用户作答失败");
      }
    },
    // 提交批改并展示结果
    async submitCorrection() {
      const correctionResults = this.exam.quesList.map((_, index) => ({
        feedback: this.feedbacks[index],
        score: this.scores[index],
      }));
      const totalScore = this.scores.reduce((a, b) => a + b, 0);

      try {
        // 从 localStorage 获取 username
        const username = localStorage.getItem("username");
        if (!username) {
          this.$message.error("用户名未找到，请重新登录");
          return;
        }

        const response = await axios.post(
          `http://localhost:8083/api/exam/${this.selectedExamId}/submit`,
          {
            correctionResults,
            totalScore,
            userAnswerId: this.selectedAnswerId,
            username, // 添加 username 参数
          }
        );

        if (response.data.code === 200) {
          this.correctionResult = response.data;
          this.$message.success("批改完成");
        } else {
          this.$message.error("批改失败");
        }
      } catch (error) {
        this.$message.error("提交批改结果失败");
      }
    },
  },
  created() {
    this.fetchExamList(); // 初始化加载试卷列表
  },
};
</script>

<style scoped>
.exam-correction {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}
</style>
