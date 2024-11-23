<template>
    <div v-loading="isLoading">
        <el-row :gutter="24">
            <el-col :span="24">
                <el-form class="custom-popper" label-width="120px">
                    <el-row>
                        <!-- 搜索知识点 -->
                        <el-col :span="16">
                            <el-form-item label="搜索知识点 ID" required>
                                <el-input v-model="searchId" placeholder="请输入知识点 ID" style="width: 100%"></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col :span="16">
                            <el-form-item>
                                <el-button type="primary" @click="loadKnowledgePoint">搜索知识点</el-button>
                            </el-form-item>
                        </el-col>
                    </el-row>
                    <el-divider></el-divider>
                    <!-- 修改知识点部分 -->
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
                                <el-input v-model="knowledgePoint.id" placeholder="请输入 ID 编号" readonly
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
                                <el-button type="primary" @click="handleSubmit">保存修改</el-button>
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
import { Input, Button, Row, Col, Form, FormItem, Divider } from 'element-ui';
import axios from 'axios';

export default {
    components: {
        "el-input": Input,
        "el-button": Button,
        "el-row": Row,
        "el-col": Col,
        "el-form": Form,
        "el-form-item": FormItem,
        "el-divider": Divider,
    },
    data() {
        return {
            isLoading: false,
            searchId: null, // 搜索时输入的知识点 ID
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
        // 加载知识点数据
        async loadKnowledgePoint() {
            if (!this.searchId) {
                this.$message.error('请输入知识点 ID 以进行搜索！');
                return;
            }

            try {
                this.isLoading = true;
                const baseUrl = "http://localhost:8083";
                const id = `"${this.searchId || ""}"`;
                const response = await axios.get(`${baseUrl}/api/knowledge/detail`, {
                    params: { id: id },
                });

                const data = response.data;
                if (data.success && data.result) {
                    const kp = data.result;
                    this.knowledgePoint = {
                        name: kp.name,
                        id: kp.id,
                        prePointString: kp.prePoint.join(","),
                        postPointString: kp.postPoint.join(","),
                        content: kp.content,
                        cognition: kp.cognition,
                        tag: kp.tag,
                        note: kp.note,
                    };
                    this.$message.success('知识点加载成功！');
                } else {
                    this.$message.error('未找到对应的知识点！');
                }
            } catch (error) {
                console.error('加载失败:', error);
                this.$message.error('加载失败，请检查网络或稍后重试！');
            } finally {
                this.isLoading = false;
            }
        },

        // 提交修改
        async handleSubmit() {
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
                this.$message.success('修改保存成功！');
            } catch (error) {
                console.error('提交失败:', error);
                this.$message.error('保存失败，请检查数据或稍后重试！');
            } finally {
                this.isLoading = false;
            }
        },

        // 重置表单
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
