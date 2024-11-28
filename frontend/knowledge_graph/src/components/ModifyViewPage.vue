<template>
    <background title="知识点添加与修改" desc="管理与修改知识点系统">
        <el-tabs v-model="activeMainTab" @tab-click="handleMainTabClick">
            <!-- 知识点管理 Tab -->
            <el-tab-pane label="知识点管理" name="knowledgeManageSystem">
                <el-tabs v-model="activeSubTab" @tab-click="handleSubTabClick">
                    <el-tab-pane label="添加知识点" name="addKnowledge" v-if="isTeacher">
                        <keep-alive>
                            <add-knowledge v-if="activeSubTab === 'addKnowledge'" :key="activeSubTab" />
                        </keep-alive>
                    </el-tab-pane>
                    <el-tab-pane label="修改知识点" name="editKnowledge" v-if="isTeacher">
                        <keep-alive>
                            <edit-knowledge v-if="activeSubTab === 'editKnowledge'" :key="activeSubTab" />
                        </keep-alive>
                    </el-tab-pane>
                </el-tabs>
            </el-tab-pane>
        </el-tabs>
    </background>
</template>

<script>
import Background from "./BackGround.vue";
import { Tabs, TabPane } from 'element-ui';
import AddKnowledge from "./KnowledgeAdding.vue";
import EditKnowledge from "./KnowledgeChanging.vue";

export default {
    components: {
        background: Background,
        'el-tabs': Tabs,
        'el-tab-pane': TabPane,
        'add-knowledge': AddKnowledge,
        'edit-knowledge': EditKnowledge,
    },
    data() {
        return {
            activeMainTab: 'knowledgeManageSystem',
            activeSubTab: 'addKnowledge',
            role: null,
        };
    },
    computed: {
        isStudent() {
            return this.role === '学生';
        },
        isTeacher() {
            return this.role === '教师';
        },
    },
    methods: {
        handleMainTabClick(tab) {
            this.activeMainTab = tab.name;
            this.activeSubTab = 'addKnowledge';  // 默认选择查看知识点
        },
        handleSubTabClick(tab) {
            this.activeSubTab = tab.name;
        },
    },
    created() {
        // 模拟角色设置（可以从接口获取）
        this.role = '教师';  // 可以修改为动态获取
    }
};
</script>

<style scoped>
.app-tabs {
    position: flex;
    display: flex;
    width: 100%;
    height: 100%;
    flex-direction: column;
    align-items: center;
}
</style>