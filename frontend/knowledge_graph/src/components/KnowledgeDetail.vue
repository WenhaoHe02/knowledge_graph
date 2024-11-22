<template>
  <div class="knowledge-detail">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>{{ knowledgeDetail.name }}</span>
      </div>
      <div v-if="knowledgeDetail">
        <el-descriptions column="1">
          <el-descriptions-item label="前置知识点">{{ knowledgeDetail.prePoint }}</el-descriptions-item>
          <el-descriptions-item label="后续知识点">{{ knowledgeDetail.postPoint }}</el-descriptions-item>
          <el-descriptions-item label="内容详情">{{ knowledgeDetail.content }}</el-descriptions-item>
          <el-descriptions-item label="认知点">{{ knowledgeDetail.cognition }}</el-descriptions-item>
          <el-descriptions-item label="标签">{{ knowledgeDetail.tag }}</el-descriptions-item>
          <el-descriptions-item label="相关知识点">{{ knowledgeDetail.relatedPoint }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ knowledgeDetail.note }}</el-descriptions-item>
          <el-descriptions-item label="知识点ID">{{ knowledgeDetail.id }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <div v-else>
        <el-alert
          title="无法加载知识点详情"
          type="error"
          show-icon>
        </el-alert>
      </div>
    </el-card>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'KnowledgeDetail',
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
      const knowledgeId = this.$route.params.id;
      try {
        const response = await axios.get(`http://localhost:8083/api/knowledge/detail`, {
          params: { id: knowledgeId }
        });
        if (response.data.success) {
          this.knowledgeDetail = response.data.result;
        } else {
          this.$message.error('获取知识点详情失败');
        }
      } catch (error) {
        console.error('获取知识点详情时错误:', error);
        this.$message.error('获取知识点详情错误');
      }
    }
  }
};
</script>

<style scoped>
.knowledge-detail {
  margin: 20px;
}
</style>
