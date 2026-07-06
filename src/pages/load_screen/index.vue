<template>
    <div class="wapper" ref="bg">
        <img class="img" src="/static/images/load_screen.png"/>
    </div>
</template>

<script>
    export default {
        name: "load_screen",
        created() {
            let _this = this
            if (this.isWeb()) {
                _this.$router.push("/home")
            } else {
                setTimeout(function(){
                   _this.goHome()
                },2000)
            }
        },
        methods : {
            isWeb: function() {
                let ua = navigator.userAgent.toLowerCase()
                return !/(iphone|ipad|ipod|android|mobile)/.test(ua)
            },
            goHome : function () {
                let _this = this
                this.$refs.bg.style.transition = 'opacity 800ms ease'
                this.$refs.bg.style.opacity = '0'
                setTimeout(function(){
                    _this.$router.push("/home")
                }, 800)
            }
        }
    }
</script>

<style scoped>
    .wapper{
        display: flex;
        flex: 1;
        width: 750px;
    }
    .img{
        flex: 1;
        width: 750px;
    }
</style>