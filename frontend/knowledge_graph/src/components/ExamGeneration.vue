<template>
  <div class="exam-generation">
    <h3>试卷生成页面</h3>

    <!-- Input Field and Confirmation Button -->
    <div class="input-container">
      <p placeholder="请选择试卷中需包含的知识点"></p>
      <!-- 全选功能 -->
      <el-checkbox v-model="selectAll" @change="toggleSelectAll">全选</el-checkbox>
    </div>

    <!-- Knowledge List Table -->
    <el-row class="centered">
      <el-col>
        <el-table :data="paginatedTableData" border class="table-container">
          <el-table-column prop="name" label="知识点名称"></el-table-column>
          <el-table-column prop="isIncluded" label="是否包含">
            <template slot-scope="scope">
              <el-checkbox v-model="scope.row.isIncluded.isSelected" label="包含"
                @change="select(scope.row)"></el-checkbox>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>

    <!-- Action Buttons -->
    <div class="button-group">
      <el-button type="primary" @click="openPreviewDialog">预览试卷</el-button>
    </div>

    <!-- Pagination -->
    <el-pagination background layout="total, prev, pager, next, jumper" :current-page.sync="currentPage"
      :page-size="pageSize" :total="totalRecords" @current-change="handlePageChange" class="page-container">
    </el-pagination>

    <el-dialog :visible.sync="previewDialogVisible" title="试卷预览" width="50%">
      <div class="preview-content">
        <h4>{{ exam ? exam.examTitle : '试卷内容' }}</h4>
        <p v-if="exam">试卷 ID: {{ exam.examId }}</p>

        <el-table v-if="exam && exam.quesList && exam.quesList.length > 0" :data="exam.quesList" border>
          <el-table-column prop="titleContent" label="题目内容"></el-table-column>
        </el-table>
        <p v-else>暂无试卷内容可预览</p>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button @click="previewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="generateExam">确定生成</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { Button, Table, TableColumn, Row, Col, Checkbox, Pagination, Dialog } from "element-ui";
import axios from "axios";

export default {
  name: "ExamGeneration",
  components: {
    "el-table-column": TableColumn,
    "el-table": Table,
    "el-button": Button,
    "el-checkbox": Checkbox,
    "el-row": Row,
    "el-col": Col,
    "el-pagination": Pagination,
    "el-dialog": Dialog,
  },
  data() {
    return {
      selectAll: false,
      currentPage: 1,
      pageSize: 7,
      totalRecords: 0,
      tableData: [],
      exam: null,
      previewDialogVisible: false,
      generationRecords: [         // 记录生成的操作
        { time: '2024-11-15 12:30', action: '生成试卷' },
        { time: '2024-11-15 13:00', action: '生成试卷' },
      ],
    };
  },
  methods: {
    async getKnowledgePoint() {
      try {
        const baseUrl = "http://localhost:8083";
        const response = await axios.get(`${baseUrl}/api/knowledge/search`, {
          params: {
            keyword: '""',
          },
        });
        if (response.data.success) {
          this.tableData = response.data.result.map(item => ({ ...item, isIncluded: { isSelected: false } }));
          this.$message({
            type: 'success',
            message: '查询成功'
          });
          console.log(this.tableData);
        } else {
          this.$message({
            type: 'warning',
            message: '查询失败'
          });
        }
      } catch (error) {
        this.$message({
          type: 'error',
          message: '查询记录时出错'
        });
      }

    },

    async openPreviewDialog() {
      this.previewDialogVisible = true;
      const baseUrl = "http://localhost:8083";
      // const baseUrl = "http://127.0.0.1:4523/m1/5419934-5094163-default/api/exam/generate";
      try {
        const selectedIds = this.tableData
          .filter(item => item.isIncluded.isSelected)
          .map(item => item.id);
        const response = await axios.post(`${baseUrl}/api/exam/generate`, JSON.stringify(selectedIds),
          {
            headers: {
              'Content-Type': 'application/json; charset=UTF-8'
            }
          }
        );

        if (response.data.success) {
          this.exam = {
            examTitle: response.data.examTitle,
            examId: response.data.examId,
            quesList: response.data.quesList
          }
          this.$message({
            type: "success",
            message: "成功发送数据"
          });
          console.log(this.exam);
        } else {
          this.$message({
            type: "warning",
            message: "发送数据失败"
          });
        }
      } catch (error) {
        this.$message({
          type: "error",
          message: "请求数据出错"
        })
      }

    },

    openRecordDialog() {
      this.recordsDialogVisible = true;
    },

    viewRecordDetails(record) {
      alert(`查看记录: ${record.time} - ${record.action}`);
    },
    // Delete a record
    deleteRecord(record) {
      this.generationRecords = this.generationRecords.filter(item => item !== record);
      console.log(`删除记录: ${record.time} - ${record.action}`);
    },
    select() {
      this.updateSelectAllStatus();
    },
    toggleSelectAll() {
      this.tableData.forEach((row) => {
        row.isIncluded.isSelected = this.selectAll;
      });
    },

    updateSelectAllStatus() {
      this.selectAll = this.tableData.every(row => row.isIncluded.isSelected);
    },

    handlePageChange(page) {
      this.currentPage = page;
    },
    async generateExam() {
      try {
        const baseUrl = "http://localhost:8083";
        const response = await axios.get(`${baseUrl}/api/exam/save`, {
          params: {
            id: this.exam.examId,
          },
        });
        if (response.data.success) {
          this.$message({
            type: 'success',
            message: '生成试卷成功'
          });
          console.log(this.tableData);
        } else {
          this.$message({
            type: 'warning',
            message: '生成试卷失败'
          });
          return;
        }
      } catch (error) {
        this.$message({
          type: 'error',
          message: '生成试卷时出错'
        });
        return;
      }
      this.previewDialogVisible = false;
      this.$router.push({ name: 'ExamAnswer', params: { examId: this.exam.examId } });
    },
  },
  async created() {
    await this.getKnowledgePoint();
    this.totalRecords = this.tableData.length;
  },
  computed: {
    paginatedTableData() {
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return this.tableData.slice(start, end);
    },
  },
  watch: {
    selectAll() {
      this.toggleSelectAll();
    },
  },
};
</script>

<style lang="css">
.exam-generation {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

h3 {
  text-align: center;
  margin-bottom: 20px;
}

.page-container {
  margin-top: 40px;
}

.table-container {
  margin: 20px auto;
  width: 100%;
}

.button-group {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 20px;
}

.centered {
  text-align: center;
}

/* Styling for preview dialog */
.preview-content {
  margin-bottom: 20px;
}

/* Styling for records dialog */
.record-content {
  margin-bottom: 20px;
}
</style>
