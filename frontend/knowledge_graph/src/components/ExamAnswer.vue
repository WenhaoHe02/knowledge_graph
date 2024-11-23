<template>
  <div class="exam-answer">
    <h3>试卷答题页面</h3>

    <!-- Exam Title -->
    <div v-if="exam">
      <h4>{{ exam.examTitle }}</h4>
      <p>试卷 ID: {{ exam.examId }}</p>
    </div>

    <!-- Question List -->
    <el-table :data="exam.quesList" border v-if="exam">
      <el-table-column prop="titleContent" label="题目内容"></el-table-column>
      <el-table-column label="回答">
        <template #default="scope">
          <el-input type="textarea" v-model="answers[scope.$index]" placeholder="请输入答案"></el-input>
        </template>
      </el-table-column>
    </el-table>

    <p v-else>正在加载试卷内容...</p>

    <!-- Submit Button -->
    <div class="button-group">
      <el-button type="primary" @click="submitAnswers">提交试卷</el-button>
    </div>
  </div>
</template>

<script>
import axios from "axios";
import { Button, Table, TableColumn, Input } from "element-ui";

export default {
  name: "ExamAnswer",
  components: {
    "el-table": Table,
    "el-table-column": TableColumn,
    "el-input": Input,
    "el-button": Button,
  },
  data() {
    return {
      examId: "", // 试卷 ID
      exam: null, // 当前试卷数据
      answers: [], // 用户填写的答案
    };
  },
  methods: {
    // 获取试卷内容
    async fetchExam() {
      try {
        const baseUrl = "http://localhost:8083";
        const response = await axios.get(`${baseUrl}/api/exam/getExam`, {
          params: { examId: this.examId },
        });

        if (response.data.length > 0) {
          this.exam = response.data[0]; // 假设返回的是数组
          this.answers = Array(this.exam.quesList.length).fill(""); // 初始化答案数组
          this.$message({
            type: "success",
            message: "试卷加载成功",
          });
        } else {
          this.$message({
            type: "warning",
            message: "未找到试卷内容",
          });
        }
      } catch (error) {
        this.$message({
          type: "error",
          message: "加载试卷内容失败",
        });
      }
    },
    // 提交答案
    async submitAnswers() {
      try {
        const baseUrl = "http://localhost:8083";
        const body = {
          examId: this.examId,
          answers: this.answers,
        };
        const response = await axios.post(`${baseUrl}/api/exam/submit`, body);

        if (response.data.code === 200) {
          this.$message({
            type: "success",
            message: "试卷提交成功",
          });
          this.$router.push("/exam-select"); // 返回试卷选择页面
        } else {
          this.$message({
            type: "warning",
            message: "试卷提交失败",
          });
        }
      } catch (error) {
        this.$message({
          type: "error",
          message: "提交试卷失败",
        });
      }
    },
  },
  async created() {
    // 获取路由参数中的试卷 ID
    this.examId = this.$route.params.examId;

    // 加载试卷数据
    await this.fetchExam();
  },
};
</script>

<style scoped>
.exam-answer {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

h3 {
  text-align: center;
  margin-bottom: 20px;
}

.button-group {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>