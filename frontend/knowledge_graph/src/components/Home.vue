<template>
  <el-container>
    <el-header>
      <h2>Home Page</h2>
    </el-header>
    <el-main>
      <el-menu :router="true" mode="horizontal">
        <el-menu-item index="/knowledge-search">知识点搜索</el-menu-item>
        <el-menu-item index="/knowledge-detail">知识点详情</el-menu-item>
        <el-menu-item index="/exam-generation">试卷生成</el-menu-item>
        <el-menu-item index="/exam-correction">试卷批改</el-menu-item>
        <el-menu-item index="/exam-select">试卷答题</el-menu-item>
        <el-menu-item index="/qna">问答</el-menu-item>
      </el-menu>
    </el-main>
  </el-container>
</template>

<script>
export default {
  name: 'HomePage',
};
</script>
