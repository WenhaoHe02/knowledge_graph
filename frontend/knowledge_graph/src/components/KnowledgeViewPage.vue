<template>
    <background title="知识搜索与问答" desc="知识点搜索与提问系统">
        <el-tabs v-model="activeMainTab" @tab-click="handleMainTabClick">
            <!-- 知识搜索与问答 Tab -->
            <el-tab-pane label="知识搜索与问答" name="knowledgeView">
                <el-tabs v-model="activeSubTab" @tab-click="handleSubTabClick">
                    <el-tab-pane label="搜索知识点" name="searchKnowledge">
                        <keep-alive>
                            <search-knowledge v-if="activeSubTab === 'searchKnowledge'" :key="activeSubTab" />
                        </keep-alive>
                    </el-tab-pane>
                    <el-tab-pane label="提问" name="askQuestion">
                        <keep-alive>
                            <ask-question v-if="activeSubTab === 'askQuestion'" :key="activeSubTab" />
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
import KnowledgeSearch from "./KnowledgeSearch.vue";
import AskQuestion from "./QnA.vue";

export default {
    components: {
        background: Background,
        'el-tabs': Tabs,
        'el-tab-pane': TabPane,
        'search-knowledge': KnowledgeSearch,
        'ask-question': AskQuestion,
    },
    data() {
        return {
            activeMainTab: 'knowledgeView',
            activeSubTab: 'searchKnowledge',
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
            this.activeSubTab = 'searchKnowledge';  // 默认选择第一个子tab
        },
        handleSubTabClick(tab) {
            this.activeSubTab = tab.name;
        },
    },
    created() {
        this.role = '学生';
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