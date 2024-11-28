<template>
  <div class="login-page">
    <div class="login-container">
      <h2>{{ isRegistering ? "注册" : "登录" }}</h2>
      <form @submit.prevent="handleSubmit">
        <div class="form-group">
          <label for="username">Username:</label>
          <input type="text" id="username" v-model="form.username" required placeholder="请输入用户名" />
        </div>
        <div class="form-group">
          <label for="password">Password:</label>
          <input type="password" id="password" v-model="form.password" required placeholder="请输入密码" />
        </div>
        <button type="submit" :disabled="loading">
          {{ loading ? "Processing..." : isRegistering ? "注册" : "登录" }}
        </button>
        <p class="toggle-text" @click="toggleMode">
          {{ isRegistering
            ? "已有账号？点此登录"
            : "还没有账号？点此注册" }}
        </p>
      </form>
      <div v-if="error" class="error-message">{{ error }}</div>
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  name: "LogIn",
  data() {
    return {
      form: {
        username: "",
        password: "",
      },
      isRegistering: false,
      loading: false,
      error: "",
    };
  },
  methods: {
    async handleSubmit() {
      this.loading = true;
      this.error = "";

      const endpoint = this.isRegistering
        ? "http://localhost:8083/api/role/register"
        : "http://localhost:8083/api/role/login";

      try {
        const response = await axios.post(endpoint, this.form, {
          headers: { "Content-Type": "application/json" },
        });

        const data = response.data;

        if (response.status === 200) {
          this.$store.commit("setUsername", data.username);
          alert(this.isRegistering ? "注册成功!" : "登录成功!");
          this.$router.push({ name: "Home" }); // 使用 Vue Router 跳转
        } else {
          throw new Error(data.message || "请求失败");
        }
      } catch (err) {
        this.error = err.message || "请求发生错误";
      } finally {
        this.loading = false;
      }
    },
    toggleMode() {
      this.isRegistering = !this.isRegistering;
      this.error = "";
    },
  },
};
</script>

<style scoped>
/* 样式保持不变 */
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f0f0;
}

.login-container {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

h2 {
  text-align: center;
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 15px;
}

label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

button {
  width: 100%;
  padding: 10px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.error-message {
  color: red;
  margin-top: 10px;
  text-align: center;
}

.toggle-text {
  text-align: center;
  margin-top: 15px;
  color: #007bff;
  cursor: pointer;
}
</style>