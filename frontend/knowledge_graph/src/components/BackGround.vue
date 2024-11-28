<template>
    <div>
        <div class="bg">
            <img class="blue-block block-mobile" src="../assets/background/small-blue-block.svg" />
            <img class="blue-block block-pc" src="../assets/background/small-blue-block-pc.svg" />
            <img class="orange-block block-mobile" src="../assets/background/small-orange-block.svg" />
            <img class="orange-block block-pc" src="../assets/background/small-orange-block-pc.svg" />
        </div>
        <div class="app-content-wrapper">
            <div class="header">
                <div style="margin-left: 10px"></div>
                <img class="seu-logo" src="../assets/seu-logo.svg" />
                <div class="title" :style="'font-size:18px'">
                    软工导论学习平台
                </div>
            </div>
            <div class="app-content">
                <div class="panel">
                    <slot></slot>
                </div>
                <!-- 回到首页按钮 -->
                <el-button type="primary" @click="goHome"
                    style="position: fixed; bottom: 20px; right: 20px; z-index: 10;">
                    回到首页
                </el-button>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    data() {
        return {};
    },
    created() {
        // Do nothing here
    },
    methods: {
        goHome() {
            // 检查当前路径是否已经是主页
            if (this.$router.currentRoute.path !== '/') {
                this.$router.push('/');
            }
        }
    }
};
</script>

<style lang="less" scoped>
.bg {
    z-index: -100;
    width: 100%;
    top: 0;
    position: fixed;

    @media screen and (min-width: 600px) {
        .block-mobile {
            display: none;
        }
    }

    @media screen and (max-width: 600px) {
        .block-pc {
            display: none;
        }
    }

    .blue-block {
        width: 100%;
        position: fixed;
        top: 0;
        z-index: -98;
    }

    .orange-block {
        width: 60%;
        position: fixed;
        right: 0;
        top: 160px;
        z-index: -99;
    }
}

.header {
    position: absolute;
    top: 5px;
    display: flex;
    flex-direction: row;
    margin-top: 5px;
    align-items: center;
    width: 100%;
    max-width: 1200px;

    .seu-logo {
        width: 50px;
        margin-left: 7px;
    }

    .title {
        color: #fff;
        font-size: 20px;
        font-weight: bold;
        margin-left: 7px;
    }

    .space {
        flex-grow: 1;
    }
}

.app-content-wrapper {
    display: flex;
    flex-direction: column;
    align-items: center;
}

.app-content {
    position: absolute;
    width: 100%;
    max-width: 1200px;
    display: flex;
    flex-direction: column;
    align-items: stretch;
    top: 50px;
}

.panel {
    background: #fff;
    margin: 0 15px;
    box-shadow: 0px 2px 4px rgba(0, 0, 0, 0.1);
    border-radius: 5px;
    margin-bottom: 30px;
    padding: 20px 15px 17px 15px;
}

@media screen and (min-width: 600px) {
    .panel {
        margin: 0 40px;
        margin-bottom: 30px;
        margin-top: 30px;
    }

    .app-title {
        margin-left: 40px;
        margin-right: 40px;
        margin-top: 30px;
    }

    .app-desc {
        margin-left: 40px;
    }
}
</style>
