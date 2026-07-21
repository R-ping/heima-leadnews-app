<template>
     <div class="upload_pic" >
              <el-form  status-icon label-width="100px">
                <img :src="upload_img_url" class="upload_pic_show" />
                <el-form-item label="用户图片" prop="logo">
                  <el-upload ref="myUpload" action="" :auto-upload="false" @change="handleChange">
                    <el-button size="small" type="primary">点击选择图片</el-button>
                  </el-upload>
                </el-form-item>
                <div class="upload-mask" v-if="uploading">
                  <div class="upload-progress">
                    <span class="progress-text">{{ uploadPercent }}%</span>
                  </div>
                </div>
        </el-form>
     </div>
</template>
<script>
import { uploadFile } from '@/common/oss_upload'
import { saveMaterial } from '@/apis/creator/publish'
import picBgUrl from '@/static/images/creator/pic_bg.png'
export default {
  name:"upload",
  props:['imgChange'],
  data () {
      return  {
         upload_img_url: picBgUrl,
         uploading: false,
         uploadPercent: 0
      }
  },
  methods:{
      handleChange(files) {
          const file = files.raw
          if (!file) return
          this.uploading = true
          this.uploadPercent = 0
          uploadFile(file, (percent) => {
              this.uploadPercent = percent
          }).then((url) => {
              return saveMaterial(url).then(() => url)
          }).then((url) => {
              this.$emit('input', url)
              this.uploading = false
              this.uploadPercent = 0
          }).catch((err) => {
              this.$message.error('上传失败: ' + (err.message || '网络错误'))
              this.uploading = false
              this.uploadPercent = 0
          })
      }
  }
}
</script>

<style lang="less">
 .upload_pic_show{
    display:block;
    width:240px;
    height:180px;
    margin:15px auto 10px;
  }
  .upload-mask{
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0,0,0,0.4);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 10;
    .upload-progress{
      text-align: center;
      .progress-text{
        color: #fff;
        font-size: 24px;
        font-weight: bold;
      }
    }
  }
</style>