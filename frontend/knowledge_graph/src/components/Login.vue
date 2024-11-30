<template>
  <div class="login-page">
    <div class="login-container">
      <h2>{{ isRegistering ? "注册" : "登录" }}</h2>
      <form @submit.prevent="handleSubmit">
        <el-form :model="form" ref="form" label-width="80px">
          <el-form-item label="角色" :rules="[{ required: true, message: '请输入角色', trigger: 'blur' }]">
            <el-input v-model="form.role" placeholder="请输入角色 (如 学生)"></el-input>
          </el-form-item>

          <el-form-item label="用户名" :rules="[{ required: true, message: '请输入用户名', trigger: 'blur' }]">
            <el-input v-model="form.userName" placeholder="请输入用户名"></el-input>
          </el-form-item>

          <el-form-item label="密码" :rules="[{ required: true, message: '请输入密码', trigger: 'blur' }]">
            <el-input type="password" v-model="form.password" placeholder="请输入密码"></el-input>
          </el-form-item>‘

          <el-form-item>
            <el-button type="primary" @click="handleSubmit" :loading="loading" :disabled="loading">
              {{ loading ? "Processing..." : isRegistering ? "注册" : "登录" }}
            </el-button>
          </el-form-item>
        </el-form>

        <p class="toggle-text" @click="toggleMode">
          {{ isRegistering ? "已有账号？点此登录" : "还没有账号？点此注册" }}
        </p>
      </form>

      <div v-if="error" class="error-message">{{ error }}</div>
    </div>
  </div>
</template>

<script>
import axios from "axios";
import { Button, Input, Form, FormItem } from "element-ui";

export default {
  name: "LogIn",
  components: {
    "el-button": Button,
    "el-input": Input,
    "el-form": Form,
    "el-form-item": FormItem,
  },
  data() {
    return {
      form: {
        userName: "",
        password: "",
        role: "",
      },
      isRegistering: false,
      loading: false,
      error: "",
    };
  },
  created() {
  },
  methods: {
    async handleSubmit() {
      this.loading = true;
      this.error = "";

      const endpoint = this.isRegistering
        ? "http://localhost:8083/api/role/register"
        : "http://localhost:8083/api/role/login";

      try {
        let response;
        if (this.isRegistering) {
          response = await axios.post(endpoint, JSON.stringify(this.form), {
            headers: { "Content-Type": "application/json" },
          });
        } else {
          response = await axios.get(endpoint, {
            params: {
              userName: this.form.userName,
              password: this.form.password,
              role: this.form.role,
            },
          });
        }

        const data = response.data;

        if (response.status === 200 && data.success) {
          localStorage.setItem('username', `${this.form.userName}`);  // 存储用户名
          console.log(this.$username);
          alert(this.isRegistering ? "注册成功!" : "登录成功!");
          if (!this.isRegistering) {
            this.$emit('login-success');
          }
        } else {
          throw new Error(data.message || "请求失败");
        }
      } catch (err) {
        if (err.response && err.response.status === 409) {
          this.error = "账号已存在";
        } else {
          this.error = err.message || "请求发生错误";
        }
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
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 50vh;
  background-color: rgba(0, 0, 0, 0);
}

.login-container {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0);
  width: 100%;
  max-width: 400px;
}

h2 {
  text-align: center;
  margin-bottom: 20px;
}

.el-form-item {
  margin-bottom: 15px;
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
