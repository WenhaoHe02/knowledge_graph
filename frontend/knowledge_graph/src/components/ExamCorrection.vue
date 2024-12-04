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
      <el-button type="primary" @click="submitCorrection" :disabled="!selectedExamId || !selectedAnswerId"
        :loading="isLoading">
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
      isLoading: false, // 加载状态
    };
  },
  methods: {
    // 获取试卷列表
    async fetchExamList() {
      try {
        console.log("加载试卷");
        const username = localStorage.getItem('username');
        if (!username) {
          this.$message.error("用户名未找到，请登录");
          return;
        }

        const response = await axios.get(`http://localhost:8083/api/exam/getExam`, {
          params: { username },
        });
        this.examList = response.data.map(exam => ({
          examId: exam.examId,
          examTitle: exam.examTitle,
        }));
      } catch (error) {
        this.$message.error("加载试卷列表失败");
        console.error(error);
      }
    },
    async fetchExamAndAnswers() {
      try {
        const username = localStorage.getItem('username');
        if (!username) {
          this.$message.error("用户名未找到，请登录");
          return;
        }

        // 获取试卷内容
        const examResponse = await axios.get(`http://localhost:8083/api/exam/getExam`, {
          params: { username, examId: this.selectedExamId },
        });
        if (examResponse.data.length === 0) {
          this.$message.error("未找到指定的试卷");
          return;
        }
        this.exam = examResponse.data[0];

        // 获取用户作答数据
        const answersResponse = await axios.get(`http://localhost:8083/api/exam/getAnswer`, {
          params: { username, examId: this.selectedExamId },
        });
        this.userAnswers = answersResponse.data.userAnswers;

        // 初始化用户答案数组，确保长度与题目数量一致
        this.answers = new Array(this.exam.quesList.length).fill(""); // 初始化为指定长度的空数组
        console.log("试卷数据:", this.exam);
        console.log("用户答案数据:", this.userAnswers);
      } catch (error) {
        this.$message.error("加载试卷或用户作答失败");
        console.error(error);
      }
    },

    // 提交批改并展示结果
    async submitCorrection() {
      // 设置加载状态
      this.isLoading = true;

      try {
        // 确保试卷和答案数据已经加载
        if (!this.exam || !Array.isArray(this.exam.quesList)) {
          this.$message.error("试卷数据加载失败，请稍后再试");
          this.isLoading = false;
          return;
        }

        // 确保用户答案数据已加载
        if (!this.userAnswers || !Array.isArray(this.userAnswers)) {
          this.$message.error("用户作答数据加载失败");
          this.isLoading = false;
          return;
        }

        // 根据 selectedAnswerId 找到对应的用户作答
        const selectedAnswer = this.userAnswers.find(
          (answer) => answer.id === this.selectedAnswerId
        );
        if (!selectedAnswer || !Array.isArray(selectedAnswer.answers)) {
          this.$message.error("未找到对应的用户作答");
          this.isLoading = false;
          return;
        }

        // 确保用户答案数量与题目数量一致
        if (selectedAnswer.answers.length !== this.exam.quesList.length) {
          this.$message.error("用户答案数量与题目数量不匹配");
          this.isLoading = false;
          return;
        }

        // 构建请求体
        const answers = this.exam.quesList.map((question, index) => ({
          id: String(index + 1), // 题目序号从 1 开始
          userAnswer: selectedAnswer.answers[index] || "", // 用户作答
        }));

        const username = localStorage.getItem("username");
        if (!username) {
          this.$message.error("用户名未找到，请重新登录");
          this.isLoading = false;
          return;
        }

        // 打印请求体以供调试
        console.log("提交批改请求体:", JSON.stringify(answers));

        // 请求批改接口
        const config = {
          method: 'post',
          url: `http://localhost:8083/api/exam/${this.selectedExamId}/grade`,
          params: {
            username: username,
            userAnsId: this.selectedAnswerId
          },
          headers: {
            'Content-Type': 'application/json'
          },
          data: answers
        };

        const response = await axios(config);
        console.log("批改响应:", response.data);

        // 处理返回结果
        if (response.data.code === 200) {
          this.correctionResult = response.data;
          this.$message.success("批改完成");

          // 在响应中处理批改结果
          const results = response.data.correctionResults;
          this.correctionResult.correctionResults = results.map((result) => ({
            score: result.score, // 每道题的得分
            feedback: result.feedback || "无反馈", // 提供反馈信息
            // 可以选择是否显示题目内容和用户答案
            // title: this.exam.quesList[index].titleContent, // 题目内容
            // userAnswer: selectedAnswer.answers[index], // 用户的答案
          }));
        } else {
          this.$message.error(`批改失败: ${response.data.message || '未知错误'}`);
        }
      } catch (error) {
        this.$message.error("提交批改结果失败");
        console.error(error);
      } finally {
        this.isLoading = false;
      }
    }
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
