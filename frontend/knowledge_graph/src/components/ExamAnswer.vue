<template>
  <div class="exam-answer">
    <h3>{{ examTitle }}</h3>

    <!-- 试卷内容展示 -->
    <el-table :data="quesList" border style="margin-top: 20px;">
      <!-- 题目内容列 -->
      <el-table-column prop="titleContent" label="题目内容"></el-table-column>
      <!-- 用户作答列 -->
      <el-table-column label="作答">
        <template #default="scope">
          <!-- 简答题 -->
          <el-input v-if="scope.row.type === 1" v-model="answers[scope.$index]" placeholder="请输入答案"></el-input>

          <!-- 选择题 -->
          <el-select v-else-if="scope.row.type === 2" v-model="answers[scope.$index]" placeholder="请选择答案">
            <el-option v-for="option in scope.row.options" :key="option" :label="option" :value="option"></el-option>
          </el-select>

          <!-- 判断题 -->
          <el-radio-group v-else-if="scope.row.type === 3" v-model="answers[scope.$index]">
            <el-radio :label="'正确'">正确</el-radio>
            <el-radio :label="'错误'">错误</el-radio>
          </el-radio-group>
        </template>
      </el-table-column>
    </el-table>

    <!-- 提交按钮 -->
    <div style="margin-top: 20px;">
      <el-button type="primary" @click="submitAnswers">提交答案</el-button>
    </div>
  </div>
</template>

<script>
import { Table, TableColumn, Button, Input, Select, Option, RadioGroup, Radio } from "element-ui";
import axios from "axios";

export default {
  name: "ExamAnswer",
  components: {
    "el-table": Table,
    "el-table-column": TableColumn,
    "el-button": Button,
    "el-input": Input,
    "el-select": Select,
    "el-option": Option,
    "el-radio-group": RadioGroup,
    "el-radio": Radio,
  },
  data() {
    return {
      examId: this.$route.query.examId, // 从路由获取试卷ID
      examTitle: this.$route.query.examTitle || "试卷标题", // 从路由获取试卷标题
      quesList: [], // 试题列表
      answers: [], // 用户作答列表
    };
  },
  methods: {
    // 获取试卷内容
    async fetchExam() {
      try {
        const username = localStorage.getItem("username"); // 从 localStorage 获取用户名
        if (!username) {
          this.$message.error("用户名未找到，请登录");
          return;
        }

        if (!this.examId) {
          this.$message.error("试卷 ID 未找到，请选择试卷");
          return;
        }

        const response = await axios.get("http://localhost:8083/api/exam/getExam", {
          params: { username, examId: this.examId }, // 添加 examId 参数
        });

        console.log("API 返回数据：", response.data);

        // 转化试卷列表为单个试卷
        if (response.data.length > 0) {
          const examData = response.data[0]; // 获取列表中第一个试卷
          console.log(examData);
          this.exam = {
            examTitle: examData.examTitle,
            examId: examData.examId,
            quesList: examData.quesList,
          };

          if (this.exam.quesList && this.exam.quesList.length > 0) {
            this.answers = Array(this.exam.quesList.length).fill(""); // 初始化用户答案
            this.$message.success("试卷加载成功");
          } else {
            this.$message.warning("试卷题目为空");
          }
        } else {
          this.$message.error("试卷加载失败：未找到试卷数据");
        }
      } catch (error) {
        this.$message.error("加载试卷时出错");
        console.error("加载试卷失败：", error);
      }
      console.log("quesList 数据：", this.exam.quesList);

      // 处理选择题选项
      this.exam.quesList.forEach((ques) => {
        if (ques.type === 2) {
          // 提取选项
          const options = this.extractOptions(ques.titleContent);
          ques.options = options; // 添加选项
          console.log(options);
          // 去掉题目中的选项部分
          ques.titleContent = ques.titleContent.split("[")[0].trim();
        }
      });

      this.quesList = this.exam.quesList;
    },

    // 提交用户作答
    async submitAnswers() {
      try {
        const username = localStorage.getItem("username"); // 获取用户名
        if (!username) {
          this.$message.error("用户名未找到，请登录");
          return;
        }

        // 对答案进行处理，选择题只发送 A,B,C,D 等字母，不包含选项内容
        const processedAnswers = this.answers.map(answer => {
          // 如果是选择题答案 (A. 选项内容)
          if (answer && answer.match(/^[A-D]\./)) {
            // 只提取 A、B、C、D 字母部分，去掉句点
            return answer.split(' ')[0].replace('.', ''); // 获取字母部分并去掉句点
          }
          // 对于其他类型的答案 (简答题、判断题)，保持原样
          return answer;
        });

        const response = await axios.post("http://localhost:8083/api/exam/submit", {
          examId: this.examId,
          username,
          answers: processedAnswers, // 提交处理过的答案
        });

        if (response.data.code === 200) {
          this.$message.success("答案提交成功");
          this.$router.push("/exam-view-page"); // 提交成功后跳转到试卷选择页面
        } else {
          this.$message.error("答案提交失败");
        }
      } catch (error) {
        console.error("提交答案失败:", error);
        this.$message.error("提交答案时出错");
      }
    },



    // 提取选项
    // 提取选项方法
    extractOptions(titleContent) {
      // 先匹配中括号内的内容
      const optionsMatch = titleContent.match(/\[(.*?)\]/);
      if (!optionsMatch || !optionsMatch[1]) return [];

      // 将中括号内的内容按选项分割
      const optionsStr = optionsMatch[1].trim();
      return optionsStr.split(/(?=[A-Z]\.)/).map(option => option.trim()).filter(Boolean);
    }

  },
  async created() {
    await this.fetchExam(); // 初始化加载试卷内容
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
</style>
