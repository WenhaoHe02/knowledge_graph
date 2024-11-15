package com.knowledgegraph.application.model;

import java.util.Map;
import java.util.HashMap;

public class Exam {
    String examId;
    String examName;
    public Map<String, String> answers;

    public void setExamId(String examId) {
        this.examId = examId;
    }
    public void setExamName(String examName) {
        this.examName = examName;
    }
    public void setAnswers(Map<String, String> answers) {
        this.answers = answers;
    }

    public void getExample()
    {
        this.examId="1";
        this.examName="example";
        Map<String,String> ans=new HashMap<>();
        ans.put("度量（software measurement）的目的是什么？","度量（software measurement）是对软件开发项目、过程及其产品进行数据定义、收集以及分析的持续性定量化过程，目的在于对此加以理解、预测、评估、控制和改善。");
        ans.put("软件项目管理的目的是什么？它涉及哪些主要元素的分析和管理？","软件项目管理的目的是为了使软件项目能够按照预定的成本、进度、质量顺利完成，它涉及对人员（People）、产品（Product）、过程（Process）和项目(Project)的分析和管理。软件项目管理先于任何技术活动之前开始，并且贯穿于软件的整个生命周期。");
        ans.put("软件生命周期内的软件评审是什么活动，涉及哪些内容，由哪些部门组织进行，结果如何呈现？","软件生命周期内的软件评审是一个关键的质量保证活动，涉及对软件各个阶段的成果进行系统性的检查和分析，包括需求评审、功能评审、质量评审、成本评审和维护评审等。评审通常由需求部门、技术部门、质量控制部门和产品部门等跨部门团队组织进行。评审结果会形成具体的评审报告，为项目的持续改进和决策提供依据。");
        ans.put("软件配置管理（SCM）是什么，并简述其主要功能？","软件配置管理（Software Configuration Management，SCM）是指在开发过程中各阶段，管理计算机程序演变的学科。它作为软件工程的关键元素，涵盖了软件生命周期所有领域并影响所有数据和过程。SCM提供了结构化的、有序化的、产品化的管理软件开发的方法，主要功能是对产品进行标识、存储和控制，以维护其完整性、可追溯性以及正确性。");
        ans.put("软件质量控制过程包括哪些主要活动？","软件质量控制过程是一系列旨在确保软件产品满足既定质量标准和用户需求的活动，包括规划、实施质量保证措施、进行质量检测和评估、以及采取必要的纠正措施，以持续改进软件质量。");
        this.answers=ans;
    }
    public Map<String,String> getExamAns()
    {
        Map<String,String> ans=new HashMap<>();
        ans.put("1","度量（software measurement）是对软件开发项目、过程及其产品进行数据定义");
        ans.put("2","软件项目管理的目的是为了使软件项目能够按照预定的成本、进度、质量顺利完成，");
        ans.put("3","软件生命周期内的软件评审是一个关键的质量保证活动，涉及对软件各个阶段的成果进行系统性的检查和分析，");
        ans.put("4","件配置管理（Software Configuration Management，SCM）是指在开发过程中各阶段，管理计算机程序演变的学科。");
        ans.put("5","我不知道");
        return ans;
    }

}