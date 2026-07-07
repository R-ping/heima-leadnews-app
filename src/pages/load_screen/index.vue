<template>
    <div class="wapper" ref="bg" v-if="!isWeb">
        <img class="img" src="/static/images/load_screen.png"/>
    </div>
</template>

<script>
    export default {
        name: "load_screen",
        data() {
            return {
                isWeb: false
            }
        },
        created() {
            this.isWeb = this.checkWeb()
            if (this.isWeb) {
                this.$router.push("/home")
            } else {
                let _this = this
                setTimeout(function(){
                   _this.goHome()
                },2000)
            }
        },
        methods : {
            checkWeb: function() {
                let ua = navigator.userAgent.toLowerCase()
                return !/(iphone|ipad|ipod|android|mobile)/.test(ua)
            },
            goHome : function () {
                let _this = this
                if (this.$refs.bg) {
                    this.$refs.bg.style.transition = 'opacity 800ms ease'
                    this.$refs.bg.style.opacity = '0'
                }
                setTimeout(function(){
                    _this.$router.push("/home")
                }, 800)
            }
        }
    }
</script>

<style scoped>
    .wapper{
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background-color: #3194ff;
        display: flex;
        align-items: center;
        justify-content: center;
    }
    .img{
        width: 100%;
        height: 100%;
        object-fit: cover;
    }
</style>