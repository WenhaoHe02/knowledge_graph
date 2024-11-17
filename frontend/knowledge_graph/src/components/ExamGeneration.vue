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
      <el-button @click="openRecordDialog">查看生成记录</el-button>
    </div>

    <!-- Pagination -->
    <el-pagination background layout="total, prev, pager, next, jumper" :current-page.sync="currentPage"
      :page-size="pageSize" :total="totalRecords" @current-change="handlePageChange" class="page-container">
    </el-pagination>

    <!-- Preview Dialog -->
    <el-dialog :visible.sync="previewDialogVisible" title="试卷预览" width="50%">
      <div class="preview-content">
        <h4>试卷内容</h4>
        <el-table :data="paginatedTableData" border>
          <el-table-column prop="knowledgePoint" label="知识点列表"></el-table-column>
          <el-table-column prop="isIncluded" label="是否包含">
            <template slot-scope="scope">
              <el-checkbox v-model="scope.row.isIncluded.isSelected" label="包含" disabled></el-checkbox>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button @click="previewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="generateExam">确定生成</el-button>
      </div>
    </el-dialog>

    <!-- View Records Dialog -->
    <el-dialog :visible.sync="recordsDialogVisible" title="查看生成记录" width="50%">
      <div class="record-content">
        <h4>生成记录</h4>
        <el-table :data="generationRecords" border>
          <el-table-column prop="time" label="时间"></el-table-column>
          <el-table-column prop="action" label="操作"></el-table-column>
          <el-table-column label="操作" width="150">
            <template slot-scope="scope">
              <el-button @click="viewRecordDetails(scope.row)" size="mini">查看</el-button>
              <el-button @click="deleteRecord(scope.row)" type="danger" size="mini">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button @click="recordsDialogVisible = false">关闭</el-button>
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
      previewDialogVisible: false,  // 控制预览对话框的显示与隐藏
      recordsDialogVisible: false,  // 控制生成记录对话框的显示与隐藏
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
    // Open the preview dialog
    openPreviewDialog() {
      this.previewDialogVisible = true;
    },
    // Open the records dialog
    openRecordDialog() {
      this.recordsDialogVisible = true;
    },
    // View the details of a record
    viewRecordDetails(record) {
      alert(`查看记录: ${record.time} - ${record.action}`);
    },
    // Delete a record
    deleteRecord(record) {
      this.generationRecords = this.generationRecords.filter(item => item !== record);
      console.log(`删除记录: ${record.time} - ${record.action}`);
    },
    // Confirm selection (toggle)
    confirmSelection() {
      console.log("确认选择");
    },
    select() {
      this.updateSelectAllStatus();
    },
    toggleSelectAll() {
      this.tableData.forEach((row) => {
        row.isIncluded.isSelected = this.selectAll;
      });
    },
    // Update selectAll checkbox based on the current selection
    updateSelectAllStatus() {
      this.selectAll = this.tableData.every(row => row.isIncluded.isSelected);
    },
    // Handle the page change
    handlePageChange(page) {
      this.currentPage = page;
    },
    // View generated records (open dialog)
    viewRecords() {
      console.log("查看生成记录");
      this.recordsDialogVisible = true;  // Open the dialog to show records
    },
    // Handle the exam generation
    generateExam() {
      console.log("生成试卷");
      this.previewDialogVisible = false;  // Close the preview dialog
    },
  },
  async created() {
    await this.getKnowledgePoint();
    this.totalRecords = this.tableData.length;
  },
  computed: {
    // Paginated table data based on current page
    paginatedTableData() {
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return this.tableData.slice(start, end);
    },
  },
  watch: {
    // Watch for changes to selectAll to update table row selections
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
  justify-content: center;
  margin-bottom: 40px;
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
