<template>
    <div class="wrapper">
        <!-- 二级路由视图 -->
        <div class="router-body"><router-view/></div>
        <!-- 底部菜单 -->
        <div class="main-body"><Menu/></div>
    </div>
</template>

<script>
    // 引入底部菜单
    import Menu from "@/compoents/menus/menu";

    export default {
        name: 'HeiMa-Layout-Mian',
        components: {Menu}
    }
</script>

<style scoped>
    .wrapper{
        display: flex;
        flex-direction: column;
        width: 100%;
        height: 100%;
        max-width: 1200px;
        margin: 0 auto;
    }
    .router-body{
        flex: 1;
        display: flex;
        flex-direction: column;
        overflow-y: auto;
    }
    .main-body{
        position: fixed;
        left: 0;
        right: 0;
        bottom: 0;
    }
</style>