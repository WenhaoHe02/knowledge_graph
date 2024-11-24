<template>
    <div v-loading="isLoading">
        <el-row :gutter="24">
            <el-col :span="24">
                <el-form class="custom-popper" label-width="120px">
                    <el-row>
                        <!-- 知识点名称 -->
                        <el-col :span="16">
                            <el-form-item label="知识点名称" required>
                                <el-input v-model="knowledgePoint.name" placeholder="请输入名称"
                                    style="width: 100%"></el-input>
                            </el-form-item>
                        </el-col>

                        <!-- ID 编号 -->
                        <el-col :span="16">
                            <el-form-item label="ID 编号" required>
                                <el-input v-model="knowledgePoint.id" placeholder="请输入 ID 编号"
                                    style="width: 100%"></el-input>
                            </el-form-item>
                        </el-col>

                        <!-- 前置知识点 -->
                        <el-col :span="16">
                            <el-form-item label="前置知识点">
                                <el-input v-model="knowledgePoint.prePointString" placeholder="请输入前置知识点ID，用逗号分隔"
                                    style="width: 100%"></el-input>
                            </el-form-item>
                        </el-col>

                        <!-- 后置知识点 -->
                        <el-col :span="16">
                            <el-form-item label="后置知识点">
                                <el-input v-model="knowledgePoint.postPointString" placeholder="请输入后置知识点ID，用逗号分隔"
                                    style="width: 100%"></el-input>
                            </el-form-item>
                        </el-col>

                        <!-- 内容 -->
                        <el-col :span="16">
                            <el-form-item label="内容" required>
                                <el-input type="textarea" v-model="knowledgePoint.content" placeholder="请输入内容"
                                    style="width: 100%"></el-input>
                            </el-form-item>
                        </el-col>

                        <!-- 标签 -->
                        <el-col :span="16">
                            <el-form-item label="标签">
                                <el-input v-model="knowledgePoint.tag" placeholder="请输入标签(重点/难点)"
                                    style="width: 100%"></el-input>
                            </el-form-item>
                        </el-col>

                        <!-- 认知维度 -->
                        <el-col :span="16">
                            <el-form-item label="认知维度" required>
                                <el-input v-model="knowledgePoint.cognition" placeholder="请输入认知维度(了解/识记/应用)"
                                    style="width: 100%"></el-input>
                            </el-form-item>
                        </el-col>

                        <!-- 备注 -->
                        <el-col :span="16">
                            <el-form-item label="备注">
                                <el-input type="textarea" v-model="knowledgePoint.note" placeholder="请输入备注"
                                    style="width: 100%"></el-input>
                            </el-form-item>
                        </el-col>
                    </el-row>

                    <el-row>
                        <el-col :span="16">
                            <el-form-item>
                                <el-button type="primary" @click="handleSubmit">提交</el-button>
                                <el-button @click="handleReset">重置</el-button>
                            </el-form-item>
                        </el-col>
                    </el-row>
                </el-form>
            </el-col>
        </el-row>
    </div>
</template>

<script>
import { Input, Button, Row, Col, Form, FormItem } from 'element-ui';
import axios from 'axios';

export default {
    components: {
        "el-input": Input,
        "el-button": Button,
        "el-row": Row,
        "el-col": Col,
        "el-form": Form,
        "el-form-item": FormItem,
    },
    data() {
        return {
            isLoading: false,
            knowledgePoint: {
                name: null,
                id: null,
                prePointString: "", // 前置知识点输入框字符串
                postPointString: "", // 后置知识点输入框字符串
                content: null,
                cognition: null,
                tag: null,
                note: null,
            },
        };
    },
    methods: {
        async handleSubmit() {
            // 将输入的逗号分隔字符串转换为数组
            const dataToSubmit = {
                ...this.knowledgePoint,
                prePoint: this.knowledgePoint.prePointString
                    ? this.knowledgePoint.prePointString.split(",").map((item) => item.trim())
                    : [],
                postPoint: this.knowledgePoint.postPointString
                    ? this.knowledgePoint.postPointString.split(",").map((item) => item.trim())
                    : [],
            };

            delete dataToSubmit.prePointString;
            delete dataToSubmit.postPointString;

            try {
                this.isLoading = true;
                const baseUrl = "http://localhost:8083";
                const response = await axios.put(`${baseUrl}/api/knowledge/modify`, dataToSubmit, {
                    headers: {
                        'Content-Type': 'application/json',
                    },
                });
                console.log('提交成功:', response.data);
                this.$message.success('提交成功！');
            } catch (error) {
                console.error('提交失败:', error);
                this.$message.error('提交失败，请检查数据或稍后重试！');
            } finally {
                this.isLoading = false;
            }
        },
        handleReset() {
            this.knowledgePoint = {
                name: null,
                id: null,
                prePointString: "",
                postPointString: "",
                content: null,
                cognition: null,
                tag: null,
                note: null,
            };
        },
    },
};
</script>

<style scoped>
.custom-popper {
    margin-top: 20px;
}
</style>
