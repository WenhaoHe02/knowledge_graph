<template>
  <el-container>
    <!-- Main Content Section -->
    <el-main class="main-content">
      <!-- Chat Section -->
      <div class="chat-section">
        <div id="chat-output" class="chat-output">
          <el-card v-for="message in messages" :key="message.id" class="message-card">
            <p><strong>{{ message.sender }}:</strong> {{ message.text }}</p>
          </el-card>
        </div>
      </div>

      <!-- History Section -->
      <div id="history-output" v-if="history.length > 0" class="history-section">
        <h3>历史问答</h3>
        <el-card v-for="(item, index) in history" :key="index" class="history-card">
          <p><strong>问题:</strong> {{ item.question }}</p>
          <p><strong>回答:</strong> {{ item.answer }}</p>
        </el-card>
      </div>

      <!-- View History Button -->
      <div class="view-history-btn-container">
        <el-button type="success" @click="fetchHistory" class="view-history-button">
          查看历史问答
        </el-button>
      </div>

      <!-- Input Section (Fixed at the bottom) -->
      <div class="input-section">
        <el-row class="input-row" type="flex" justify="space-between">
          <el-col :span="20">
            <el-input v-model="userInput" @keyup.enter="sendMessage" placeholder="请输入您的问题..." class="input-field"
              :autosize="{ minRows: 1, maxRows: 3 }" />
          </el-col>
          <el-col :span="4">
            <el-button type="primary" @click="sendMessage" class="send-button">
              发送
            </el-button>
          </el-col>
        </el-row>
      </div>
    </el-main>
  </el-container>
</template>

<script>
// 引入 axios 库
import axios from 'axios';

export default {
  name: 'QnA',
  data() {
    return {
      userInput: '',  // User input text
      messages: [],   // Store chat messages
      messageId: 0,   // Unique ID for messages
      history: [],    // Store historical Q&A data
      activeMenu: '1', // Active menu index
    };
  },
  methods: {
    async sendMessage() {
      if (this.userInput.trim()) {
        // Add user message to chat
        this.messages.push({ id: this.messageId++, sender: '你', text: this.userInput });
        const question = this.userInput;
        this.userInput = ''; // Clear the input field after sending

        try {
          // Send the question to the backend API using axios (GET request)
          const response = await axios.get('http://localhost:8083/api/qa/ask', {
            params: { question }, // Send the question as a query parameter
          });

          // Handle the response from the server
          if (response.status === 200) {
            const data = response.data;
            const answer = data.answer || 'No answer available.';
            // Add AI response to chat
            this.messages.push({ id: this.messageId++, sender: 'AI', text: answer });
          } else {
            this.messages.push({
              id: this.messageId++, sender: 'AI', text: 'Error: Unable to get a response from the server.',
            });
          }
        } catch (error) {
          console.error('Error:', error);
          this.messages.push({
            id: this.messageId++, sender: 'AI', text: 'An error occurred while fetching the answer.',
          });
        }
      }
    },

    async fetchHistory() {
      // Assuming we are using the same user for now
      const username = localStorage.getItem('username'); // Placeholder, replace with dynamic value if needed

      try {
        const response = await axios.get('http://localhost:8083/api/qa/history', {
          params: { username }, // Query parameter for username
        });

        if (response.status === 200 && response.data.success) {
          const historyData = response.data.result;
          this.history = historyData.map(item => ({
            question: item.qa[0].question,  // Assuming the question is in the first QA pair
            answer: item.qa[0].answer,      // Assuming the answer is in the first QA pair
          }));
        } else {
          this.history = [];
          this.$message.error('无法获取历史记录');
        }
      } catch (error) {
        console.error('Error fetching history:', error);
        this.$message.error('获取历史记录失败');
      }
    }
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

/* Main content styles */
.main-content {
  padding: 20px;
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

/* Chat section styles */
.chat-section {
  flex-grow: 1;
  margin-bottom: 20px;
  overflow-y: auto;
}

.chat-output {
  margin-bottom: 20px;
  max-height: 60vh;
  overflow-y: auto;
}

.message-card {
  margin-bottom: 10px;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.message-card p {
  margin: 0;
}

.message-card strong {
  color: #2c3e50;
}

/* History section styles */
.history-section {
  margin-top: 20px;
}

.history-card {
  margin-bottom: 10px;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.history-card p {
  margin: 0;
}

/* View history button styles */
.view-history-btn-container {
  margin-top: 20px;
  text-align: center;
}

.view-history-button {
  width: 100%;
  font-size: 1.2rem;
  padding: 12px 20px;
  height: 45px;
  border-radius: 8px;
  background-color: #87CEEB;
  color: #fff;
}

.view-history-button:hover {
  background-color: #1E90FF;
}

/* Input area styles */
.input-section {
  position: sticky;
  bottom: 0;
  background-color: #fff;
  padding: 20px 0;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
}

.input-row {
  padding: 10px 0;
}

.input-field {
  width: 100%;
  border-radius: 8px;
  background-color: #f4f4f4;
  border: 1px solid #dcdfe6;
  padding: 10px;
  font-size: 1rem;
}

.el-button {
  font-size: 1.1rem;
  height: 50px;
  border-radius: 8px;
}

.send-button {
  font-size: 1.3rem;
  padding: 10px 20px;
  height: 60px;
  border-radius: 8px;
  background-color: #87CEEB;
  color: #fff;
  width: 100%;
}

.send-button:hover {
  background-color: #1E90FF;
}
</style>
