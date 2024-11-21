<template>
  <el-container>
    <!-- Sidebar Section -->
    <el-aside width="260px" class="sidebar">
      <div class="sidebar-header">
        <h2>问答</h2>
      </div>
      <el-menu :default-active="activeMenu" class="sidebar-menu" mode="vertical" background-color="#f4f4f4">
        <el-menu-item index="1" class="menu-item">
          <router-link to="/knowledge-search">知识点搜索</router-link>
        </el-menu-item>
        <el-menu-item index="2" class="menu-item">
          <router-link to="/knowledge-detail">知识点详情</router-link>
        </el-menu-item>
        <el-menu-item index="3" class="menu-item">
          <router-link to="/exam-generation">试卷生成</router-link>
        </el-menu-item>
        <el-menu-item index="4" class="menu-item">
          <router-link to="/exam-correction">试卷批改</router-link>
        </el-menu-item>
        <el-menu-item index="5" class="menu-item">
          <router-link to="/">返回主菜单</router-link>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- Main Content Section -->
    <el-main class="main-content">
      <!-- Chat Output Section -->
      <div id="chat-output" class="chat-output">
        <el-card v-for="message in messages" :key="message.id" class="message-card">
          <p><strong>{{ message.sender }}:</strong> {{ message.text }}</p>
        </el-card>
      </div>

      <!-- Input Section -->
      <el-row class="input-row" type="flex" justify="space-between">
        <el-col :span="22">
          <el-input v-model="userInput" placeholder="Type your message here..." />
        </el-col>
        <el-col :span="2">
          <el-button type="primary" @click="sendMessage" class="send-button">Send</el-button>
        </el-col>
      </el-row>
    </el-main>
  </el-container>
</template>

<script>
export default {
  name: 'QnA',
  data() {
    return {
      userInput: '',
      messages: [], // Store chat messages
      messageId: 0, // Unique ID for messages
      activeMenu: '1', // Active menu index
    };
  },
  methods: {
    async sendMessage() {
      if (this.userInput.trim()) {
        // Add the user message to the chat
        this.messages.push({ id: this.messageId++, sender: '你', text: this.userInput });
        const question = this.userInput;
        this.userInput = ''; // Clear the input field after sending

        // Make the POST request to the backend API
        try {
          const response = await fetch('http://localhost:8083/api/qa/ask', {
            method: 'POST',
            headers: {
              'Content-Type': 'text/plain',
            },
            body: question, // Send the question as plain text
          });

          if (response.ok) {
            const data = await response.json();
            const answer = data.answer || 'No answer available.';
            // Add AI's response
            this.messages.push({ id: this.messageId++, sender: 'AI', text: answer });
          } else {
            this.messages.push({
              id: this.messageId++,
              sender: 'AI',
              text: 'Error: Unable to get a response from the server.',
            });
          }
        } catch (error) {
          console.error('Error:', error);
          this.messages.push({
            id: this.messageId++,
            sender: 'AI',
            text: 'An error occurred while fetching the answer.',
          });
        }
      }
    },
  },
};
</script>

<style scoped>
/* Global reset */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

/* Sidebar styles */
.sidebar {
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100vh;
  background-color: #f4f4f4;
}

.sidebar-header {
  text-align: center;
  margin: 20px 0;
}

.sidebar-header img {
  width: 80px;
  height: 80px;
  margin-bottom: 10px;
}

.sidebar-header h2 {
  font-size: 1.5rem;
  color: #333;
}

.sidebar-menu {
  width: 100%;
  margin-top: 20px;
  text-align: center;
}

.menu-item {
  font-size: 1.1rem;
  font-weight: 600;
  text-align: center;
  padding: 10px 0;
  transition: background-color 0.3s ease, color 0.3s ease;
}

/* Remove underline from router-link inside el-menu-item */
::v-deep .el-menu-item a {
  text-decoration: none; /* Remove underline */
}

/* Main content styles */
.main-content {
  padding: 20px;
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

/* Chat output styles */
.chat-output {
  flex-grow: 1;
  margin-bottom: 20px;
  overflow-y: auto;
}

.message-card {
  margin-bottom: 10px;
}

/* Input area styles */
.input-row {
  padding: 10px 0;
}

.el-input {
  width: 100%;
}

.el-button {
  width: 100%;
  font-size: 1.1rem;
  padding: 12px;
  height: 50px;
}

.send-button {
  font-size: 1.3rem;
  padding: 15px 30px;
  height: 60px;
  border-radius: 8px;
}
</style>
