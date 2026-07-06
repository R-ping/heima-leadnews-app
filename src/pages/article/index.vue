<template>
    <div class="art-page">
        <div class="art-top"><TopBar :text="title"/></div>
        <div class="scroller" ref="scroller" @scroll="scroller" show-scrollbar="true">
            <span class="title">{{title}}</span>
            <div class="info">
                <img src="https://p3.pstatp.com/thumb/1480/7186611868" class="head"></img>
                <div class="more">
                    <span class="author">{{source}}</span>
                    <span class="time">{{formatDate(date)}}</span>
                </div>
                <div class="empty"></div>
                <div class="follow-btn" v-if="relation.isfollow" @click="follow">取消关注</div>
                <div class="follow-btn" v-if="!relation.isfollow" @click="follow">+ 关注</div>
            </div>
            <div class="content">
                <template v-for="item in content">
                    <span class="text" :style="getStyle(item.style)" v-if="item.type=='text'">{{item.value}}</span>
                    <img @load="imageLoad(item,$event)" class="image" :style="{width:'710px',height:imageHeight[item.value]}" v-if="item.type=='image'" :src="item.value"></img>
                </template>
            </div>
            <div class="tools">
                <Button text="点赞" @onClick="like" :icon='icon.like' :active="relation.islike" active-text="取消赞"/>
                <Button text="不喜欢" @onClick="unlike" :icon='icon.unlike' :active="relation.isunlike" />
<!--                <Button text="微信" :icon='icon.wechat' @onClick="share(0)"/>-->
<!--                <Button text="朋友圈" :icon='icon.friend' @onClick="share(1)"/>-->
            </div>
        </div>
        <div class="art-bottom"><BottomBar :forward="test.isforward" @clickForward="forward"
                                           :collection="relation.iscollection" @clickCollection="collection" /></div>
    </div>
</template>

<script>
    import TopBar from '@/compoents/bars/article_top_bar'
    import BottomBar from '@/compoents/bars/article_bottom_bar'
    import Button from '@/compoents/buttons/button'
    
    import Api from '@/apis/article/api'

    import { toast, confirmDialog } from "@/utils/toast"
    import Utils from '@/utils/env'

    export default {
        name: "index",
        components:{TopBar,BottomBar,Button},
        props:['id','title','date','comment','type','source','authorId'],
        data(){
            return {
                scrollerHeight:'500px',
                icon : {
                    like : '\uf164',
                    unlike : '\uf1f6',
                    wechat : '\uf086',
                    friend : '\uf268'
                },
                imageHeight : {},
                config:{},//文章配置
                content:{},//文章内容
                relation:{
                    islike: false,
                    isunlike: false,
                    iscollection: false,
                    isfollow: false,
                    isforward:false
                },//关系
                time : {
                    timer:null,//定时器
                    timerStep:100,//定时器步长
                    readDuration:0,//阅读时长
                    percentage:0,//阅读比例
                    loadDuration:0,//加载时长
                    loadOff:true//加载完成控制
                },//时间相关属性
                test : {
                    isforward : false
                }
            }
        },
        created(){
            this.loadInfo();
            this.loadBehavior();
            let _this = this;
            this.time.timer = setInterval(function(){
                _this.time.readDuration+=_this.time.timerStep
                if(_this.time.loadOff){
                    _this.time.loadDuration+=_this.time.timerStep
                }
            },this.time.timerStep)
        },
        destroyed(){
            this.read();
        },
        mounted(){
            this.scrollerHeight=(Utils.getPageHeight()-180)+'px';
        },
        methods : {
            imageLoad : function(item,e){
                // Standard img onload event
                var img = e.target;
                if(img.naturalWidth > 150){
                    var height = parseInt(img.naturalHeight * (750 / img.naturalWidth)) + 'px';
                    this.$set(this.imageHeight, item.value, height);
                } else {
                    img.style.objectFit = 'contain';
                }
            },
            loadInfo : function(){
                Api.loadinfo(this.id).then((d)=>{
                    if(d.code==0){
                        this.config = d.data['config']
                        let temp = d.data['content']
                        if(temp){
                            temp = temp.content
                            try {
                                this.content = JSON.parse(temp)
                            } catch(e) {
                                this.content = new Function('return (' + temp + ')')()
                            }
                            this.time.loadOff=false;//关闭加载时间的记录
                        }else{
                            toast('文章已被删除', 3)
                        }
                    }else{
                        toast( d.error_message, 3)
                    }
                }).catch((e)=>{
                    console.log(e)
                })
            },
            loadBehavior : function(){
                Api.loadbehavior(this.id,this.authorId).then((d)=>{
                    if(d.code==0){
                        this.relation = d.data
                    }else{
                        toast( d.error_message, 3)
                    }
                }).catch((e)=>{
                    console.log(e)
                })
            },
            // 点赞
            like : function(){
                if(!this.$store.getters.isLoggedIn){
                    this.$store.dispatch('showLogin')
                    return
                }
                Api.like({articleId:this.id,operation:this.relation.islike?1:0}).then(d=>{
                    if(d.code==0){
                        this.relation.islike = !this.relation.islike
                    }else{
                        toast( d.error_message, 3)
                    }
                }).catch((e)=>{
                    console.log(e)
                })
            },
            // 不喜欢
            unlike : function(){
                if(!this.$store.getters.isLoggedIn){
                    this.$store.dispatch('showLogin')
                    return
                }
                Api.unlike({articleId:this.id,type:this.relation.isunlike?1:0}).then(d=>{
                    if(d.code==0){
                        this.relation.isunlike = !this.relation.isunlike
                    }else{
                        toast( d.error_message, 3)
                    }
                }).catch((e)=>{
                    console.log(e)
                })
            },
            // 分享
            share : function(type){
                Api.share({articleId:this.id,type:type}).then(d=>{
                    if(d.code==0){
                        toast('分享成功', 3)
                    }else{
                        toast( d.error_message, 3)
                    }
                }).catch((e)=>{
                    console.log(e)
                })
            },
            // 收藏
            collection : function(){
                if(!this.$store.getters.isLoggedIn){
                    this.$store.dispatch('showLogin')
                    return
                }
                Api.collection({articleId:this.id,publishedTime:this.date,operation:this.relation.iscollection?1:0}).then(d=>{
                    if(d.code==0){
                        this.relation.iscollection = !this.relation.iscollection
                    }else{
                        toast( d.error_message, 3)
                    }
                }).catch((e)=>{
                    console.log(e)
                })
            },
            // 转发
            forward : function(){
                if(!this.$store.getters.isLoggedIn){
                    this.$store.dispatch('showLogin')
                    return
                }
                Api.forward({articleId:this.id}).then(d=>{
                    this.test.isforward = !this.test.isforward
                }).catch((e)=>{
                    console.log(e)
                })
            },
            // 关注
            follow : function(){
                if(!this.$store.getters.isLoggedIn){
                    this.$store.dispatch('showLogin')
                    return
                }
                Api.follow({articleId:this.id,authorId:this.authorId,operation:this.relation.isfollow?1:0}).then(d=>{
                    if(d.code==0){
                        this.relation.isfollow = !this.relation.isfollow
                        toast(this.relation.isfollow?'成功关注':'成功取消关注', 3)
                    }else{
                        toast( d.error_message, 3)
                    }
                }).catch((e)=>{
                    console.log(e)
                })
            },
            // 阅读行为
            read : function(){
                clearInterval( this.time.timer)
                Api.read({articleId:this.id,readDuration:this.time.readDuration,percentage:this.time.percentage,loadDuration:this.time.loadDuration});
            },
            formatDate:function(time){
                return this.$date.format13(time);
            },
            getStyle:function(item){
                if(item){
                    if( typeof(item) == 'string'){
                        try{
                            item = item.replace(/(-.)/g,function($1){return $1.replace('-','').toLocaleUpperCase()})
                            item=eval('({'+item.replace(/-/ig,'')+'})');
                        }catch (e) {
                            console.log(e)
                        }
                    }
                    return item;
                }else{
                    return {}
                }
            },
            getImgStyle:function(item){
                item = this.getStyle();
                item['width']='750px'
                item['height']='1px'
                // 处理动态图片高度
                let temp = this.imageHeight[item.value]
                if(temp){
                    item['height']=temp
                }
                return item;
            },
            scroller : function(e){
                // Standard scroll event — use scrollTop/scrollHeight instead of Weex contentOffset/contentSize
                var y = Math.abs(e.target.scrollTop) + (Utils.getPageHeight() - 180);
                var height = e.target.scrollHeight;
                this.time.percentage = Math.max(parseInt((y * 100) / height), this.time.percentage);
            }
        }
    }
</script>

<style scoped>
    .art-page{
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        width: 750px;
        display: flex;
        flex-direction: column;
    }
    .art-top{
        top: 0;
        height: 90px;
        position: fixed;
        z-index: 999;
    }
    .art-bottom{
        bottom: 0;
        position: fixed;
        width: 750px;
    }
    .scroller{
        flex: 1;
        display: flex;
        flex-direction: column;
        width: 750px;
        padding: 0px 20px;
        margin: 90px 0px;
    }
    .title{
        font-size: 48px;
        font-weight: bold;
        margin: 10px 0px;
    }
    .info{
        margin-top: 20px;
        line-height: 48px;
        align-items: center;
        display: flex;
        flex-direction: row;
    }
    .head{
        width: 48px;
        height: 48px;
        border-radius: 48px;
    }
    .more{
        display: flex;
        flex-direction: column;
    }
    .author{
        font-size: 25px;
        color: #383839;
        margin-left: 15px;
    }
    .time{
        font-size: 21px;
        color: #b5b5b5;
        margin-left: 15px;
    }
    .empty{
        flex: 1;
    }
    .content{
        display: flex;
        flex-direction: column;
        font-size: 30px;
        display: flex;
        justify-content:flex-start;
        margin-top: 20px;
        color: #222;
        word-wrap: break-word;
        text-align: justify;
    }
    .text {
        margin: 15px 0px;
    }
    .image{
        display:inline-block;
        margin: 15px 0px;
        border-radius: 5px;
        height: 300px;
    }
    .tools{
        margin: 20px 0px 30px;
        display: flex;
        flex-direction: row;
        height: 60px;
        display: flex;
        justify-content: center;
    }
</style>
