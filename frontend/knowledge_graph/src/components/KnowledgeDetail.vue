<template>
  <div class="knowledge-detail">
    <el-container>
      <!-- 标题部分 -->
      <el-header>
        <el-page-header content="知识点详情" :title="knowledgeDetail?.name || '未命名知识点'">
        </el-page-header>
      </el-header>

      <!-- 内容部分 -->
      <el-main>
        <el-card class="box-card">
          <div v-if="knowledgeDetail">
            <table class="detail-table">
              <tr>
                <th>前置知识点</th>
                <td>{{ knowledgeDetail.prePoint || '无' }}</td>
              </tr>
              <tr>
                <th>后续知识点</th>
                <td>{{ knowledgeDetail.postPoint || '无' }}</td>
              </tr>
              <tr>
                <th>内容详情</th>
                <td>{{ knowledgeDetail.content || '无' }}</td>
              </tr>
              <tr>
                <th>认知点</th>
                <td>{{ knowledgeDetail.cognition || '无' }}</td>
              </tr>
              <tr>
                <th>标签</th>
                <td>{{ knowledgeDetail.tag || '无' }}</td>
              </tr>
              <tr>
                <th>相关知识点</th>
                <td>{{ knowledgeDetail.relatedPoint || '无' }}</td>
              </tr>
              <tr>
                <th>备注</th>
                <td>{{ knowledgeDetail.note || '无' }}</td>
              </tr>
              <tr>
                <th>知识点ID</th>
                <td>{{ knowledgeDetail.id || '无' }}</td>
              </tr>
            </table>
          </div>
          <div v-else>
            <!-- 错误提示 -->
            <el-empty description="无法加载知识点详情">
              <el-button type="primary" @click="reloadData">重新加载</el-button>
            </el-empty>
          </div>
        </el-card>
      </el-main>
      <el-button type="primary" @click="goBack" class="btn">返回</el-button>
    </el-container>
  </div>
</template>

<script>
import axios from "axios";
import { Header, PageHeader, Card, Empty, Button, Main } from "element-ui";

export default {
  name: "KnowledgeDetail",
  components: {
    "el-header": Header,
    "el-page-header": PageHeader,
    "el-card": Card,
    "el-empty": Empty,
    "el-button": Button,
    "el-main": Main,
  },
  data() {
    return {
      knowledgeDetail: null,
    };
  },
  created() {
    this.fetchKnowledgeDetail();
  },
  methods: {
    // Fetch the detailed information for the knowledge point
    async fetchKnowledgeDetail() {
      try {
        const knowledgeId = `"${this.$route.params.id || ""}"`;
        const response = await axios.get(
          `http://localhost:8083/api/knowledge/detail`,
          {
            params: { id: knowledgeId },
          }
        );
        if (response.data.success) {
          this.knowledgeDetail = response.data.result;
        } else {
          this.$message.error("获取知识点详情失败");
        }
      } catch (error) {
        console.error("获取知识点详情时错误:", error);
        this.$message.error("获取知识点详情错误");
      }
    },
    // 返回上一页
    goBack() {
      this.$router.go(-1); // 返回到上一页
    },
    reloadData() {
      this.fetchKnowledgeDetail();
    },
  },
};
</script>

<style scoped>
.knowledge-detail {
  margin: 20px;
}

.detail-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 20px;
}

.detail-table th,
.detail-table td {
  border: 1px solid #FFFFFF;
  padding: 10px;
  text-align: left;
}

.detail-table th {
  background-color: #FFFFFF;
  font-weight: bold;
  width: 20%;
}

.detail-table td {
  width: 80%;
}

.btn {
  position: absolute;
  bottom: 30%;
  /* 调整按钮离底部的距离 */
  left: 50%;
  transform: translateX(-50%);
  /* 实现水平居中 */
}
</style>
