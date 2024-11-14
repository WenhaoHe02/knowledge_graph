<template>
  <div>
    <el-card class="exam-card">
      <h2>{{ examName }}</h2> <!-- 显示试卷名称 -->

      <!-- 选择题展示 -->
      <div v-for="(question, index) in choiceQuestions" :key="index">
        <p>{{ index + 1 }}. {{ question.text }}</p>
        <el-radio-group v-model="answers.choice[question.id]">
          <el-radio v-for="option in question.options" :label="option" :key="option">
            {{ option }}
          </el-radio>
        </el-radio-group>
      </div>

      <!-- 填空题展示 -->
      <div v-for="(question, index) in fillInTheBlankQuestions" :key="index">
        <p>{{ choiceQuestions.length + index + 1 }}. {{ question.text }}</p>
        <el-input v-model="answers.fill[question.id]" placeholder="请填写答案"></el-input>
      </div>

      <!-- 提交按钮 -->
      <div class="submit-button-container">
        <el-button type="primary" @click="submitAnswers">提交试卷</el-button>
      </div>
    </el-card>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  props: {
    examId: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      examName: "",               // 用于存储试卷名称
      choiceQuestions: [],         // 存储选择题数据
      fillInTheBlankQuestions: [], // 存储填空题数据
      answers: {                   // 用户的答案
        choice: {},
        fill: {}
      }
    };
  },
  created() {
    this.fetchExamData();
  },
  methods: {
    // 获取试卷数据
    fetchExamData() {
      axios.get(`/api/exam/${this.examId}`)
          .then(response => {
            const data = response.data;
            this.examName = data.examName; // 设置试卷名称
            this.choiceQuestions = data.choiceQuestions;
            this.fillInTheBlankQuestions = data.fillInTheBlankQuestions;
          })
          .catch(error => {
            console.error("Failed to fetch exam data:", error);
          });
    },

    // 提交试卷答案
    submitAnswers() {
      const payload = {
        examId: this.examId,
        answers: this.answers
      };

      axios.post(`/api/exam/submit`, payload)
          .then(() => {
            this.$message.success("提交成功！等待批改结果。");
          })
          .catch(error => {
            this.$message.error("提交失败，请重试！");
            console.error("Failed to submit answers:", error);
          });
    }
  }
};
</script>

<style scoped>
.exam-card {
  max-width: 600px;
  margin: 20px auto;
  padding: 20px;
}

.submit-button-container {
  display: flex;
  justify-content: center;
  margin-top: 30px; /* 将按钮下移 */
}
</style>
