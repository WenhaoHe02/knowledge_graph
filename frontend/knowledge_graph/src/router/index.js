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

Vue.use(Router);

export default new Router({
  routes: [
    { path: '/', component: Home },
    { path: '/knowledge-search', component: KnowledgeSearch },
    { path: '/knowledge-detail', component: KnowledgeDetail },
    { path: '/exam-generation', component: ExamGeneration },
    { path: '/exam-correction', component: ExamCorrection },
    { path: '/qna', component: QnA },
    { path: '/exam-select', component: ExamSelect },
    { path: '/exam-answer', component: ExamAnswer },
  ],
});