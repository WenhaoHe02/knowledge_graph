<template>
  <div class="knowledge-search">
    <h1>知识点搜索页面</h1>
    
    <el-input
      v-model="searchQuery"
      placeholder="请输入知识点关键词"
      @input="onSearch"
      style="width: 300px; margin-right: 10px;"
    />
    
    <el-button type="primary" @click="onSearch">搜索</el-button>

    <!-- 搜索结果展示 -->
    <div v-if="results.length">
      <h4>搜索结果：</h4>
      <el-table :data="results" style="width: 100%">
        <el-table-column prop="name" label="知识点名称" width="180">
          <template #default="{ row }">
            <!-- 点击名称后跳转到详情页面 -->
            <el-button @click="goToDetailPage(row.id)" type="text">{{ row.name }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div v-else>
      <el-message type="info" show-icon>没有找到相关知识点。</el-message>
    </div>
  </div>
</template>

<script>
// import axios from 'axios';

export default {
  name: 'KnowledgeSearch',
  data() {
    return {
      searchQuery: '',
      results: [],
      allKnowledgePoints: [], // 存储所有的知识点
    };
  },
  created() {
    this.fetchAllKnowledgePoints();
  },
  methods: {
    // 获取所有的知识点
    async fetchAllKnowledgePoints() {
    try {
        const response = await this.$axios.get('http://localhost:8083/api/knowledge/search', {
            headers: {
                "x-api-token": this.$axios.defaults.headers["x-api-token"], // 添加 API token
            },
            params: {
                semester: this.name
            },
        });
        if (response.data.success) {
            this.allKnowledgePoints = response.data.result; // 根据响应数据结构可能需要调整
            this.$message({
                type: 'success',
                message: '知识点数据加载成功'
            });
        } else {
            this.$message({
                type: 'warning',
                message: '知识点数据加载失败'
            });
        }
    } catch (error) {
        console.error('Failed to fetch knowledge points:', error);
        this.$message({
            type: 'error',
            message: '加载知识点数据时出错'
        });
    }
}
,
        onSearch() {
      if (this.searchQuery) {
        this.results = this.allKnowledgePoints.filter(item =>
          item.name.toLowerCase().includes(this.searchQuery.toLowerCase())
        );
      } else {
        this.results = [];
      }
    },

    // 跳转到知识点详情页面
    goToDetailPage(id) {
      this.$router.push({ name: 'knowledge-detail', params: { id } });
    }
  }
};
</script>

<style scoped>
.knowledge-search {
  width: 80%;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
}
</style>
