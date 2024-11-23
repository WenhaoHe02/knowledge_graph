<template>
  <div class="knowledge-search">
    <h1>知识点搜索页面</h1>

    <!-- 搜索输入框 -->
    <el-input v-model="searchQuery" placeholder="请输入知识点关键词" @input="onSearch"
      style="width: 300px; margin-right: 10px;" />

    <!-- 搜索按钮 -->
    <el-button type="primary" @click="onSearch">搜索</el-button>

    <!-- 搜索结果展示 -->
    <div v-if="paginatedResults.length">
      <h4>搜索结果：</h4>
      <el-table :data="paginatedResults" style="width: 100%">
        <!-- 知识点名称列 -->
        <el-table-column prop="name" label="知识点名称" width="200">
          <template #default="{ row }">
            <!-- 点击名称后跳转到详情页面 -->
            <el-button @click="goToDetailPage(row.id)" type="text">{{ row.name }}</el-button>
          </template>
        </el-table-column>

        <!-- 知识点ID列 -->
        <el-table-column prop="id" label="知识点ID" width="150">
          <template #default="{ row }">
            {{ row.id }}
          </template>
        </el-table-column>

        <!-- 知识点内容列 -->
        <el-table-column prop="content" label="内容" width="400">
          <template #default="{ row }">
            {{ row.content || '无内容' }}
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <el-pagination background layout="total, prev, pager, next, jumper" :current-page.sync="currentPage"
        :page-size="pageSize" :total="totalResults" @current-change="handlePageChange"
        class="page-container"></el-pagination>
    </div>
    <div v-else>
      <p>暂无搜索结果</p>
    </div>
  </div>
</template>

<script>
import { TableColumn, Button, Input, Table, Pagination } from "element-ui";

export default {
  name: "KnowledgeSearch",
  components: {
    "el-table-column": TableColumn,
    "el-button": Button,
    "el-input": Input,
    "el-table": Table,
    "el-pagination": Pagination,
  },
  data() {
    return {
      searchQuery: "",
      results: [],
      allKnowledgePoints: [], // 存储所有的知识点
      semester: "2024-1", // 默认学期或通过其他方式动态获取
      currentPage: 1, // 当前页
      pageSize: 7, // 每页显示的记录数
    };
  },
  created() {
    this.fetchAllKnowledgePoints();
  },
  computed: {
    // 计算分页后的数据
    paginatedResults() {
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return this.results.slice(start, end);
    },
    // 计算总结果数
    totalResults() {
      return this.results.length;
    },
  },
  methods: {
    // 获取所有的知识点
    async fetchAllKnowledgePoints() {
      try {
        const response = await this.$axios.get("http://localhost:8083/api/knowledge/search", {
          params: {
            keyword: "''",
          },
        });

        if (response.data.success) {
          this.allKnowledgePoints = response.data.result.map((item) => ({
            ...item,
            isIncluded: { isSelected: false },
          }));

          // 加载成功后，初始化搜索结果
          this.results = [...this.allKnowledgePoints];

          this.$message({
            type: "success",
            message: "知识点数据加载成功",
          });

          console.log(this.allKnowledgePoints);
        } else {
          this.$message({
            type: "warning",
            message: "知识点数据加载失败",
          });
        }
      } catch (error) {
        console.error("加载知识点数据时出错:", error);
        this.$message({
          type: "error",
          message: "加载知识点数据时出错",
        });
      }
    },

    // 搜索功能
    onSearch() {
      if (this.searchQuery) {
        // 执行搜索，过滤出符合条件的知识点
        this.results = this.allKnowledgePoints.filter((item) =>
          item.name.toLowerCase().includes(this.searchQuery.toLowerCase())
        );
      } else {
        // 如果没有搜索条件，显示所有数据
        this.results = [...this.allKnowledgePoints];
      }

      // 重置分页
      this.currentPage = 1;
    },

    // 跳转到知识点详情页面
    goToDetailPage(id) {
      this.$router.push({ name: "KnowledgeDetail", params: { id } });
    },

    // 处理分页页码变化
    handlePageChange(page) {
      this.currentPage = page;
    },
  },
};
</script>

<style scoped>
.knowledge-search {
  width: 80%;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
}

.page-container {
  margin-top: 20px;
}
</style>
