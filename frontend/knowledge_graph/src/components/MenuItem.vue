<template>
    <div @click="routerPush" class="menu-item">
        <div class="icon-bg">
            <img :src="icon" class="icon" />
        </div>
        <div class="title">{{ title }}</div>
    </div>
</template>

<script>
export default {
    props: ["icon", "title", "route"], // 只保留需要的属性
    methods: {
        routerPush() {
            if (this.route) {
                this.$router.push(this.route); // 路由跳转
            }
        }
    }
};
</script>

<style lang="less" scoped>
.menu-item {
    margin: 8px;
    background-color: #f8f8f8;
    border-radius: 5px;
    flex-basis: 70px;
    flex-grow: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 15px 10px;
    position: relative;

    .icon-bg {
        width: 30px;
        height: 30px;
        border-radius: 100px;
        background-color: #f0f0f0;
        padding: 10px;
        margin-top: 10px;

        .icon {
            width: 100%;
            height: 100%;
        }
    }

    .title {
        margin-top: 15px;
        font-weight: bold;
        color: #005a3a;
        font-size: 14px;
        white-space: nowrap;
    }
}
</style>