import Vue from 'vue';
import Router from 'vue-router';

import Home from '../components/Home.vue';
import KnowledgeSearch from '../components/KnowledgeSearch.vue';
import KnowledgeDetail from '../components/KnowledgeDetail.vue';
import ExamGeneration from '../components/ExamGeneration.vue';
import ExamCorrection from '../components/ExamCorrection.vue';
import QnA from '../components/QnA.vue';
import ExamSelect from "../components/ExamSelect.vue";
import ExamAnswer from "../components/ExamAnswer.vue";
import KnowledgeAdding from '../components/KnowledgeAdding.vue';
import KnowledgeChanging from '../components/KnowledgeChanging.vue';
import KnowledgeViewPage from '../components/KnowledgeViewPage.vue';
import ModifyViewPage from '@/components/ModifyViewPage.vue';
import Login from '../components/Login.vue';
Vue.use(Router);

export default new Router({
  routes: [
    { path: '/', name: 'Home', component: Home },
    { path: '/knowledge-search', component: KnowledgeSearch },
    { name: 'KnowledgeDetail', path: '/knowledge-detail', component: KnowledgeDetail },
    { path: '/exam-generation', component: ExamGeneration },
    { path: '/exam-correction', component: ExamCorrection },
    { path: '/qna', component: QnA },
    { path: '/exam-select', component: ExamSelect },
    { name: ' ExamAnswer', path: '/exam-answer', component: ExamAnswer },
    { path: '/knowledge-adding', component: KnowledgeAdding },
    { path: '/knowledge-changing', component: KnowledgeChanging },
    { path: '/knowledge-view-page', component: KnowledgeViewPage },
    { path: '/modify-view-page', component: ModifyViewPage },
    { path: '/log-in', component: Login }
  ]
});
