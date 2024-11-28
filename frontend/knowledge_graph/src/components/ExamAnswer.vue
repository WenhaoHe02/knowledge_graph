<template>
  <div class="exam-correction">
    <h3>试卷批改页面</h3>

    <!-- 试卷选择 -->
    <el-select
      v-model="selectedExamId"
      placeholder="选择试卷"
      style="width: 300px;"
      @change="fetchUserAnswers"
    >
      <el-option
        v-for="exam in examList"
        :key="exam.examId"
        :label="exam.examTitle"
        :value="exam.examId"
      ></el-option>
    </el-select>

    <!-- 用户作答选择 -->
    <el-select
      v-model="selectedAnswerId"
      placeholder="选择用户作答"
      style="width: 300px; margin-top: 20px;"
      @change="fetchUserAnswer"
      v-if="userAnswers.length > 0"
    >
      <el-option
        v-for="answer in userAnswers"
        :key="answer.id"
        :label="`用户作答 - ${answer.id}`"
        :value="answer.id"
      ></el-option>
    </el-select>

    <!-- 用户作答内容展示 -->
    <el-table :data="exam?.quesList" border style="margin-top: 20px;" v-if="exam">
      <el-table-column prop="titleContent" label="题目"></el-table-column>
      <el-table-column label="标准答案">
        <template #default="scope">
          {{ scope.row.standardAnswer }}
        </template>
      </el-table-column>
      <el-table-column label="用户答案">
        <template #default="scope">
          {{ userAnswersMap[scope.$index] || "未作答" }}
        </template>
      </el-table-column>
      <el-table-column label="评分">
        <template #default="scope">
          <el-input-number
            v-model="scores[scope.$index]"
            :min="0"
            :max="10"
            placeholder="请输入得分"
          ></el-input-number>
        </template>
      </el-table-column>
      <el-table-column label="反馈">
        <template #default="scope">
          <el-input
            v-model="feedbacks[scope.$index]"
            type="textarea"
            placeholder="请输入反馈"
          ></el-input>
        </template>
      </el-table-column>
    </el-table>

    <!-- 提交批改 -->
    <div style="margin-top: 20px;" v-if="exam">
      <el-button type="primary" @click="submitCorrection">提交批改结果</el-button>
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
import {
  Select,
  Option,
  Table,
  TableColumn,
  Button,
  Input,
  InputNumber,
} from "element-ui";

export default {
  name: "ExamCorrection",
  components: {
    "el-select": Select,
    "el-option": Option,
    "el-table": Table,
    "el-table-column": TableColumn,
    "el-button": Button,
    "el-input": Input,
    "el-input-number": InputNumber,
  },
  data() {
    return {
      examList: [], // 试卷列表
      selectedExamId: "", // 当前选择的试卷 ID
      userAnswers: [], // 用户作答列表
      selectedAnswerId: "", // 当前选择的用户作答 ID
      userAnswersMap: {}, // 当前用户作答内容
      exam: null, // 当前试卷内容
      scores: [], // 批改分数
      feedbacks: [], // 批改反馈
      correctionResult: null, // 批改结果
    };
  },
  methods: {
    // 获取试卷列表
    async fetchExamList() {
      try {
        const response = await axios.get("http://localhost:8083/api/exam/getExam");
        this.examList = response.data.map((item) => ({
          examId: item.examId,
          examTitle: item.examTitle,
        }));
      } catch (error) {
        this.$message.error("加载试卷列表失败");
      }
    },
    // 获取用户作答列表
    async fetchUserAnswers() {
      try {
        const response = await axios.get(`http://localhost:8083/api/exam/${this.selectedExamId}/getAnswer`);
        this.userAnswers = response.data.userAnswers || [];
        this.exam = this.examList.find((exam) => exam.examId === this.selectedExamId); // 加载对应试卷
      } catch (error) {
        this.$message.error("加载用户作答失败");
      }
    },
    // 获取选定用户的作答内容
    async fetchUserAnswer() {
      const userAnswer = this.userAnswers.find((answer) => answer.id === this.selectedAnswerId);
      this.userAnswersMap = JSON.parse(userAnswer.answers || "[]");
      this.scores = Array(this.exam.quesList.length).fill(0); // 初始化分数
      this.feedbacks = Array(this.exam.quesList.length).fill(""); // 初始化反馈
    },
    // 提交批改结果
    async submitCorrection() {
      const correctionResults = this.exam.quesList.map((_, index) => ({
        feedback: this.feedbacks[index],
        score: this.scores[index],
      }));
      const totalScore = this.scores.reduce((a, b) => a + b, 0);

      try {
        const response = await axios.post(
          `http://localhost:8083/api/exam/${this.selectedExamId}/submit`,
          {
            correctionResults,
            totalScore,
            userAnswerId: this.selectedAnswerId,
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
  async created() {
    await this.fetchExamList();
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
