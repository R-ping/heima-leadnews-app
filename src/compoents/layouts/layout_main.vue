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
        width: 750px;
        height: 100%;
    }
    .router-body{
        flex: 1;
        display: flex;
        flex-direction : column;
    }
    .main-body{
        position: fixed;
        left: 0;
        bottom: 0;
    }
</style>
