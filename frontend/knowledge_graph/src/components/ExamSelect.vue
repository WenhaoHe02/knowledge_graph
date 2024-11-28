<template>
  <div class="exam-select">
    <!-- 返回按钮 -->
    <div class="navigation-bar">
      <el-button type="text" icon="el-icon-arrow-left" @click="goToHome">
        返回首页
      </el-button>
    </div>

    <h3>试卷选择页面</h3>

    <!-- Exam List Table -->
    <el-row class="centered">
      <el-col>
        <el-table :data="paginatedExamList" border class="table-container">
          <el-table-column prop="examTitle" label="试卷名称"></el-table-column>
          <el-table-column label="操作">
            <template #default="scope">
              <!-- 开始答题按钮 -->
              <el-button
                type="primary"
                @click="goToExam(scope.row.examId, scope.row.examTitle)"
              >
                开始答题
              </el-button>
              <!-- 删除按钮 -->
              <el-button
                type="danger"
                @click="deleteExam(scope.row.examId)"
                style="margin-left: 10px"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>

    <!-- Pagination -->
    <el-pagination
      background
      layout="total, prev, pager, next, jumper"
      :current-page.sync="currentPage"
      :page-size="pageSize"
      :total="totalRecords"
      @current-change="handlePageChange"
      class="page-container"
    >
    </el-pagination>
  </div>
</template>

<script>
import { Button, Table, TableColumn, Row, Col, Pagination } from "element-ui";
import axios from "axios";

export default {
  name: "ExamSelect",
  components: {
    "el-table": Table,
    "el-table-column": TableColumn,
    "el-button": Button,
    "el-row": Row,
    "el-col": Col,
    "el-pagination": Pagination,
  },
  data() {
    return {
      currentPage: 1,
      pageSize: 7,
      totalRecords: 0,
      examList: [],
    };
  },
  methods: {
    // 获取试卷列表
    async fetchExamList() {
      try {
        const baseUrl = "http://localhost:8083";
        const response = await axios.get(`${baseUrl}/api/exam/getExam`);
        this.examList = response.data.map(item => ({
          examId: item.examId,
          examTitle: item.examTitle,
        }));
        this.totalRecords = this.examList.length;
        this.$message({
          type: "success",
          message: "试卷列表加载成功",
        });
      } catch (error) {
        console.error("获取试卷列表失败:", error);
        this.$message({
          type: "error",
          message: "加载试卷列表失败",
        });
      }
    },
    // 跳转到答题页面
    goToExam(examId, examTitle) {
      this.$router.push({
        path: "/exam-answer",
        query: {
          examId,
          examTitle,
        },
      });
    },
    // 跳转到首页
    goToHome() {
      this.$router.push("/");
    },
    // 删除试卷
    async deleteExam(examId) {
      try {
        const baseUrl = "http://localhost:8083";
        const response = await axios.post(`${baseUrl}/api/exam/del`, { examId });
        if (response.data.code === 200) {
          this.$message({
            type: "success",
            message: "试卷删除成功",
          });
          // 刷新试卷列表
          this.fetchExamList();
        } else {
          this.$message({
            type: "warning",
            message: "试卷删除失败，试卷可能不存在",
          });
        }
      } catch (error) {
        console.error("删除试卷失败:", error);
        this.$message({
          type: "error",
          message: "删除试卷时出错",
        });
      }
    },
  },
  async created() {
    await this.fetchExamList();
  },
  computed: {
    paginatedExamList() {
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return this.examList.slice(start, end);
    },
  },
};
</script>

<style scoped>
.exam-select {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

h3 {
  text-align: center;
  margin-bottom: 20px;
}

.table-container {
  margin: 20px auto;
  width: 100%;
}

.page-container {
  margin-top: 40px;
}

.centered {
  text-align: center;
}

.navigation-bar {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.navigation-bar .el-button {
  margin-left: 0;
}
</style>
