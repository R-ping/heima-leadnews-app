<template>
  <div class="publish-editor-page">
    <div class="editor-topbar">
      <div class="topbar-left">
        <input
          v-model="FormData.title"
          type="text"
          class="title-input"
          placeholder="输入文章标题..."
        />
      </div>
      <div class="topbar-right">
        <span class="draft-save-status" :class="saveStatus">
          <template v-if="saveStatus === 'saving'">保存中...</template>
          <template v-else-if="saveStatus === 'saved'">保存成功</template>
          <template v-else-if="saveStatus === 'error'">出现异常</template>
        </span>
        <el-button size="medium" @click="goDraftBox">草稿箱</el-button>
        <el-button size="medium" type="primary" :disabled="saveStatus !== 'saved'" @click="openPublishDrawer">发布</el-button>
        <div class="user-avatar">
          <i class="fa fa-user-circle-o"></i>
        </div>
      </div>
    </div>

    <div class="editor-content-area">
      <ByteMdEditor
        ref="byteMdEditor"
        v-model="FormData.content"
        class="main-editor"
        :sync-scroll="syncScroll"
        @change="handleContentChange"
        @import-doc="openImportDialog"
      />
    </div>

    <div class="editor-statusbar">
      <div class="status-left">
        <span class="status-item">字符数 {{ charCount }}</span>
        <span class="status-divider">|</span>
        <span class="status-item">行数 {{ lineCount }}</span>
        <span class="status-divider">|</span>
        <span class="status-item">正文字数 {{ wordCount }}</span>
      </div>
      <div class="status-right">
        <label class="sync-scroll-label">
          <input type="checkbox" v-model="syncScroll" />
          <span>同步滚动</span>
        </label>
        <a class="back-to-top" @click="backToTop">回到顶部</a>
      </div>
    </div>

    <el-drawer
      :visible.sync="publishDrawerVisible"
      direction="rtl"
      size="420px"
      :with-header="false"
      :show-close="false"
      class="publish-drawer publish-drawer-custom"
      :wrapper-closable="true"
      :modal="true"
      :modal-append-to-body="true"
      :append-to-body="true"
      :close-on-press-escape="true"
    >
      <div class="drawer-inner">
        <div class="drawer-header">
          <h3>发布文章</h3>
          <i class="fa fa-times close-drawer" @click="publishDrawerVisible = false"></i>
        </div>
        <div class="drawer-body">
          <div class="drawer-section">
            <label class="section-label">
              分类 <span class="required">*</span>
            </label>
            <el-radio-group v-model="FormData.channel_id" class="channel-chips">
              <el-radio-button
                v-for="item in channel_list"
                :key="item.id"
                :label="item.id"
                border
              >{{ item.name }}</el-radio-button>
            </el-radio-group>
          </div>

          <div class="drawer-section">
            <label class="section-label">添加标签 <span class="tag-limit">最多{{ maxTags }}个</span></label>
            <el-select
              v-model="selectedTags"
              multiple
              filterable
              remote
              reserve-keyword
              placeholder="搜索并选择标签"
              :remote-method="searchTags"
              :loading="tagLoading"
              size="small"
              class="tag-select"
              :disabled="selectedTags.length >= maxTags"
              @visible-change="onTagDropdownVisible"
            >
              <el-option
                v-for="item in tagOptions"
                :key="item.id"
                :label="item.name"
                :value="item.name"
              >
                <span style="float: left">{{ item.name }}</span>
                <span style="float: right; color: #8492a6; font-size: 12px">{{ item.category }}</span>
              </el-option>
            </el-select>
          </div>

          <div class="drawer-section" v-if="canSchedulePublish">
            <label class="section-label">定时发布</label>
            <el-date-picker
              v-model="FormData.publish_time"
              type="datetime"
              placeholder="选择发布时间"
              size="small"
              class="datetime-picker"
            />
          </div>

          <div class="drawer-section">
            <label class="section-label">话题</label>
            <el-select
              v-model="FormData.topic"
              filterable
              remote
              reserve-keyword
              clearable
              placeholder="搜索并选择话题"
              :remote-method="searchTopics"
              :loading="topicLoading"
              size="small"
              class="topic-select"
              @visible-change="onTopicDropdownVisible"
            >
              <el-option
                v-for="item in topicOptions"
                :key="item.id"
                :label="item.name"
                :value="item.name"
              >
                <span style="float: left">{{ item.name }}</span>
                <span style="float: right; color: #8492a6; font-size: 12px">{{ item.description }}</span>
              </el-option>
            </el-select>
          </div>

          <div class="drawer-section">
            <label class="section-label">文章封面</label>
            <div class="cover-upload-area" @click="selectSinglePic">
              <div v-if="!singlePic" class="upload-placeholder">
                <i class="fa fa-plus upload-icon"></i>
                <span class="upload-text">上传封面（不上传则为无图文章）</span>
              </div>
              <img v-else :src="parseImage(singlePic)" class="cover-preview-img" />
            </div>
            <p v-if="singlePic" class="cover-tip">建议尺寸: 192*128px</p>
          </div>

          <div class="drawer-section">
            <label class="section-label">编辑摘要</label>
            <div class="summary-wrapper">
              <el-input
                v-model="FormData.summary"
                type="textarea"
                :rows="4"
                placeholder="自动从文章中提取摘要，也可以手动编辑"
                maxlength="100"
                class="summary-textarea"
              />
              <span class="summary-count">{{ (FormData.summary || '').length }}/100</span>
            </div>
          </div>
        </div>
        <div class="drawer-footer">
          <el-button @click="publishDrawerVisible = false">取消</el-button>
          <el-button type="primary" :disabled="saveStatus !== 'saved'" @click="confirmPublish">确定并发布</el-button>
        </div>
      </div>
    </el-drawer>

    <el-dialog
      :visible.sync="showPicDialog"
      width="50%"
      :close-on-click-modal="false"
      :show-close="false"
      :center="true"
      :modal-append-to-body="true"
      :append-to-body="true"
    >
      <el-tabs type="card" v-model="activeName">
        <el-tab-pane label="素材库" name="first">
          <el-radio-group @change="getImgData" v-model="activeName2" style="margin-bottom: 30px;">
            <el-radio-button label="all">全部</el-radio-button>
            <el-radio-button label="collect">收藏</el-radio-button>
          </el-radio-group>
          <div class="img_list_con">
            <div
              class="img_list"
              v-for="item in imgData"
              :key="item.id"
              @click="selectPic(item.id,item.url)"
            >
              <img :src="item.url">
              <img v-if="item.id == selectedImg.id" :src="selected_img_url" class="selected">
            </div>
          </div>
          <div class="pagination">
            <el-pagination
              background
              layout="total, prev, pager, next, jumper"
              :page-size="imgPage.pageSize"
              :total="imgPage.total"
              :page-count="imgPage.pageCount"
              :current-page.sync="imgPage.currentPage"
              @current-change="getImgData"
            ></el-pagination>
          </div>
        </el-tab-pane>
        <el-tab-pane label="上传图片" name="second">
          <upload :imgChange="uploadSuccess"/>
        </el-tab-pane>
      </el-tabs>
      <span slot="footer" class="dialog-footer">
        <el-button @click="cancleImg">取 消</el-button>
        <el-button type="primary" @click="btnOKImg">确 定</el-button>
      </span>
    </el-dialog>

    <el-dialog
      title="文章导入"
      :visible.sync="importDialogVisible"
      width="480px"
      :close-on-click-modal="false"
      :modal-append-to-body="true"
      :append-to-body="true"
      class="import-dialog"
    >
      <el-upload
        ref="importUpload"
        drag
        action=""
        accept=".md"
        :limit="1"
        :file-list="importFileList"
        :auto-upload="false"
        :on-change="handleImportFileChange"
        :on-remove="handleImportFileRemove"
        :before-upload="beforeImportUpload"
        class="import-uploader"
      >
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">拖拽 MD 文件到这里，或点击进行上传</div>
        <div class="el-upload__tip" slot="tip">
          仅支持导入 MD 格式的文档，最大 10 MB，每次仅可上传 1 篇<br/>
          请在上传前检查文档图片路径，本地路径的图片会上传失败
        </div>
      </el-upload>
      <span slot="footer" class="dialog-footer">
        <el-button @click="importDialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="importLoading" @click="confirmImport">导入文档</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
  import ByteMdEditor from "@/pages/creator/components/editor/ByteMdEditor.vue";
  import Upload from "@/pages/creator/components/Upload/upload.vue";
  import { getArticleById } from "@/apis/creator/content";
  import {
    getAllImgData,
    getChannels,
    publishArticles,
    modifyArticles,
    getTagList,
    getTopicList,
    importMarkdown
  } from "@/apis/creator/publish";
  import selectedImgUrl from "@/static/images/creator/selected.png";
  import uploadImgUrl from "@/static/images/creator/pic_bg.png";
  import { permission } from "@/utils/permission";
  import { API_DRAFT_CREATE, API_DRAFT_UPDATE, API_DRAFT_PUBLISH } from "@/pages/creator/constants/api";
  import wemediaRequest from '@/common/article_request';

  export default {
    name: "PublishEditor",
    components: { Upload, ByteMdEditor },
    data() {
      return {
        FormData: {
          id: "",
          title: "",
          type: "0",
          labels: "",
          topic: "",
          publish_time: "",
          channel_id: null,
          content: "",
          summary: ""
        },
        maxTags: 1,
        canAddVideo: false,
        canSchedulePublish: false,
        host: '',
        singlePic: null,
        threePicList: [null, null, null],
        channel_list: [],
        showPicDialog: false,
        activeName: "first",
        activeName2: "all",
        selected_img_url: selectedImgUrl,
        upload_img_url: uploadImgUrl,
        imgPage: {
          total: 0,
          currentPage: 1,
          pageSize: 5,
          pageCount: 1
        },
        imgData: [],
        selectedImg: {},
        currentType: {
          type: "",
          index: null
        },
        publishDrawerVisible: false,
        syncScroll: true,
        charCount: 0,
        lineCount: 1,
        wordCount: 0,
        // 标签相关
        selectedTags: [],
        tagOptions: [],
        tagLoading: false,
        // 话题相关
        topicOptions: [],
        topicLoading: false,
        // 文档导入
        importDialogVisible: false,
        importFileList: [],
        importLoading: false,
        // 草稿自动保存
        draftId: null,
        draftTimer: null,
        draftSaving: false,
        draftContent: '',
        saveStatus: '' // idle | saving | saved | error
      };
    },
    watch: {
      selectedTags: {
        handler(newVal) {
          this.FormData.labels = newVal.join(',')
        },
        deep: true
      },
      'FormData.content': {
        handler() { this.scheduleDraftSave() }
      },
      'FormData.title': {
        handler() { this.scheduleDraftSave() }
      }
    },
    beforeMount() {
      const token = localStorage.getItem('ACCESS_TOKEN')
      if (!token) {
        this.$router.replace('/home')
        return
      }
      let { articleId } = this.$route.query;
      if (articleId) {
        this.getArticle(articleId);
      }
      this.getChannels();
      this.initPermissions();
    },
    beforeDestroy() {
      if (this.draftTimer) {
        clearTimeout(this.draftTimer)
        this.draftTimer = null
      }
      // 最后保存一次草稿
      if (this.FormData.content || this.FormData.title) {
        this.autoSaveDraft()
      }
    },
    methods: {
      initPermissions() {
        this.maxTags = permission.getMaxTags();
        this.canAddVideo = permission.canAddVideo();
        this.canSchedulePublish = permission.canSchedulePublish();
      },
      // 打开文档导入弹窗
      openImportDialog() {
        this.importDialogVisible = true
        this.importFileList = []
        this.importLoading = false
      },
      // 导入文件选择变化
      handleImportFileChange(file, fileList) {
        this.importFileList = fileList.slice(-1)
      },
      // 移除导入文件
      handleImportFileRemove() {
        this.importFileList = []
      },
      // 导入前校验
      beforeImportUpload(file) {
        const isMd = file.name.toLowerCase().endsWith('.md')
        const isLt10M = file.size / 1024 / 1024 < 10
        if (!isMd) {
          this.$message.error('仅支持导入 MD 格式的文档')
        }
        if (!isLt10M) {
          this.$message.error('文档大小不能超过 10 MB')
        }
        return isMd && isLt10M
      },
      // 确认导入
      confirmImport() {
        if (this.importFileList.length === 0) {
          this.$message.warning('请先选择要导入的 MD 文档')
          return
        }
        const file = this.importFileList[0].raw
        if (!this.beforeImportUpload(file)) return

        this.importLoading = true
        importMarkdown(file).then(res => {
          this.importLoading = false
          if (res && res.code === 200) {
            const { title, content } = res.data || {}
            if (title) {
              this.FormData.title = title
            }
            if (content) {
              this.FormData.content = content
              this.handleContentChange(content)
            }
            this.importDialogVisible = false
            this.$message.success('文档导入成功')
          } else {
            this.$message.error(res?.message || '文档导入失败')
          }
        }).catch(err => {
          this.importLoading = false
          this.$message.error(err?.message || '文档导入失败')
        })
      },
      // 标签搜索
      searchTags(query) {
        if (query) {
          this.tagLoading = true
          getTagList(query).then(res => {
            this.tagLoading = false
            if (res && res.code === 200) {
              this.tagOptions = res.data || []
            }
          }).catch(() => {
            this.tagLoading = false
          })
        } else {
          this.tagOptions = []
        }
      },
      // 标签下拉展开时加载全部
      onTagDropdownVisible(visible) {
        if (visible) {
          this.tagLoading = true
          getTagList('').then(res => {
            this.tagLoading = false
            if (res && res.code === 200) {
              this.tagOptions = res.data || []
            }
          }).catch(() => {
            this.tagLoading = false
          })
        }
      },
      // 话题搜索
      searchTopics(query) {
        if (query) {
          this.topicLoading = true
          getTopicList(query).then(res => {
            this.topicLoading = false
            if (res && res.code === 200) {
              this.topicOptions = res.data || []
            }
          }).catch(() => {
            this.topicLoading = false
          })
        } else {
          this.topicOptions = []
        }
      },
      // 话题下拉展开时加载全部
      onTopicDropdownVisible(visible) {
        if (visible) {
          this.topicLoading = true
          getTopicList('').then(res => {
            this.topicLoading = false
            if (res && res.code === 200) {
              this.topicOptions = res.data || []
            }
          }).catch(() => {
            this.topicLoading = false
          })
        }
      },
      parseImage: function (item) {
        if (item) {
          return item;
        } else {
          return this.upload_img_url
        }
      },
      async getChannels() {
        let result = await getChannels();
        this.channel_list = result.data;
      },
      // 调度草稿保存（防抖2秒）
      scheduleDraftSave() {
        if (this.draftTimer) clearTimeout(this.draftTimer)
        this.draftTimer = setTimeout(() => {
          this.autoSaveDraft()
        }, 2000)
      },
      async autoSaveDraft() {
        if (!this.FormData.content && !this.FormData.title) return
        const currentContent = this.FormData.content + this.FormData.title
        if (currentContent === this.draftContent) return
        if (this.draftSaving) return

        this.draftSaving = true
        this.saveStatus = 'saving'
        this.draftContent = currentContent
        try {
          let images = this.getImages()
          let data = {
            id: this.draftId,
            title: this.FormData.title,
            content: this.FormData.content,
            channelId: this.FormData.channel_id,
            images: images.join(','),
            labels: this.FormData.labels,
            topic: this.FormData.topic,
            summary: this.FormData.summary,
            publishTime: this.FormData.publish_time,
            layout: images.length > 0 ? 1 : 0
          }

          let result
          if (!this.draftId) {
            // 首次创建草稿
            result = await wemediaRequest.post(API_DRAFT_CREATE, data)
          } else {
            // 更新已有草稿
            result = await wemediaRequest.put(API_DRAFT_UPDATE, data)
          }
          if (result && result.code === 200) {
            if (result.data && result.data.id) {
              this.draftId = result.data.id
            }
            this.saveStatus = 'saved'
          } else {
            this.saveStatus = 'error'
          }
        } catch (e) {
          this.saveStatus = 'error'
        }
        this.draftSaving = false
      },
      async getArticle(id) {
        let result = await getArticleById(id);
        this.FormData = {
          id: result.data.id,
          title: result.data.title,
          channel_id: result.data.channel_id,
          labels: result.data.labels,
          topic: result.data.topic || "",
          type: "" + result.data.type,
          publish_time: result.data.publish_time,
          content: result.data.content || "",
          summary: result.data.summary || this.generateSummary(result.data.content || "")
        }
        this.selectedTags = (result.data.labels || "").split(",").map(item => item.trim()).filter(item => item.length > 0);
        this.host = result.host
        this.transImages(this.FormData.type, result.data.images);
        this.updateCounts(result.data.content || "");
      },
      generateSummary(content) {
        if (!content) return "";
        let text = content.replace(/[#*`>\-\[\]()!_~\n\r]/g, '').trim();
        return text.substring(0, 100);
      },
      handleContentChange(val) {
        this.updateCounts(val);
        if (!this.FormData.summary || this.FormData.summary === this.generateSummary(this._lastContent || "")) {
          this.FormData.summary = this.generateSummary(val);
        }
        this._lastContent = val;
      },
      updateCounts(content) {
        this.charCount = content.length;
        this.lineCount = content.split('\n').length;
        const plainText = content.replace(/[#*`>\-\[\]()!_~\n\r\s]/g, '');
        this.wordCount = plainText.length;
      },
      selectPic(id, url) {
        this.selectedImg = { id, url };
      },
      uploadSuccess(url) {
        this.selectedImg = { url };
      },
      uploadPic() {
        this.imgPage.currentPage = 1
        this.showPicDialog = true;
        this.getImgData();
      },
      btnOKImg() {
        if (this.selectedImg.url) {
          if (this.currentType.type == "single") {
            this.singlePic = this.selectedImg.url;
          } else if (this.currentType.type == "three") {
            this.threePicList[this.currentType.index] = this.selectedImg.url;
            this.$forceUpdate();
          }
        }
        this.currentType = {};
        this.selectedImg = {};
        this.showPicDialog = false;
      },
      cancleImg() {
        this.showPicDialog = false;
      },
      selectThreePic(index) {
        this.currentType.type = "three";
        this.currentType.index = index;
        this.uploadPic();
      },
      selectSinglePic() {
        this.currentType.type = "single";
        this.uploadPic();
      },
      async getImgData(page) {
        let temp = page == undefined ? this.imgPage.currentPage : page
        try {
          temp = parseInt(temp)
        } catch (e) {
          temp = 1
        }
        let isCollect = this.activeName2 == "collect";
        let result = await getAllImgData({
          size: this.imgPage.pageSize,
          page: temp,
          is_collected: isCollect ? 1 : 0
        });
        this.imgData = result.data.list;
        this.imgPage.total = result.data.total;
        this.imgPage.pageCount = Math.ceil(
          this.imgPage.total / this.imgPage.pageSize
        );
      },
      transImages(type, images) {
        if (!images) return;
        images = images.split(",")
        if (images.length > 0 && images[0]) {
          this.singlePic = images[0];
        }
      },
      getImages() {
        return this.singlePic ? [this.singlePic] : [];
      },
      saveDraft() {
        this.autoSaveDraft()
      },
      goDraftBox() {
        this.$router.push({ path: '/creator/article/list' })
      },
      openPublishDrawer() {
        if (!this.FormData.summary) {
          this.FormData.summary = this.generateSummary(this.FormData.content);
        }
        this.publishDrawerVisible = true;
      },
      backToTop() {
        const editorEl = this.$el.querySelector('.main-editor');
        if (editorEl) {
          const bytemdEl = editorEl.querySelector('.bytemd');
          if (bytemdEl) {
            const editorPanels = bytemdEl.querySelectorAll('.bytemd-editor, .bytemd-preview');
            editorPanels.forEach(panel => {
              panel.scrollTop = 0;
            });
          }
        }
      },
      roundUpToFiveMinutes(date) {
        const d = new Date(date);
        const minutes = d.getMinutes();
        const remainder = minutes % 5;
        if (remainder !== 0) {
          d.setMinutes(minutes + (5 - remainder));
          d.setSeconds(0);
        } else {
          d.setSeconds(0);
        }
        return d.toISOString();
      },
      confirmPublish() {
        this.publish();
      },
      async publish() {
        this.FormData.labels = this.selectedTags.join(',');

        // 校验
        if (!this.FormData.title || this.FormData.title.length < 5 || this.FormData.title.length > 32) {
          this.$message({ type: "warning", message: "文章标题不能小于5个字符或大于32个字符" });
          return;
        }
        if (!this.FormData.labels || this.FormData.labels.length > 20) {
          this.$message({ type: "warning", message: "内容标签不能为空或超过20字符" });
          return;
        }
        if (!this.FormData.content) {
          this.$message({ type: "warning", message: "文章内容不能为空" });
          return;
        }
        if (!this.FormData.channel_id) {
          this.$message({ type: "warning", message: "文章频道不能为空" });
          return;
        }
        if (!this.draftId) {
          this.$message({ type: "warning", message: "草稿尚未保存成功，请稍后再试" });
          return;
        }

        // 先确保草稿已保存最新内容
        await this.autoSaveDraft();
        if (this.saveStatus !== 'saved') {
          this.$message({ type: "warning", message: "草稿保存失败，请重试" });
          return;
        }

        try {
          // 发布只发送 draft_id
          let result = await wemediaRequest.post(API_DRAFT_PUBLISH, { draftId: this.draftId });
          if (result && result.code === 200) {
            this.$message({ type: "success", message: "文章发布成功，已进入审核" });
            this.publishDrawerVisible = false;
            this.$router.replace({ path: "/creator/article/list" });
          } else {
            this.$message({ type: "error", message: result?.errorMessage || "发布失败" });
          }
        } catch (e) {
          this.$message({ type: "error", message: "发布失败，请重试" });
        }
      }
    }
  };
</script>

<style rel="stylesheet/less" lang="less" scoped>
  @import "../layout/styles/variables.less";

  .publish-editor-page {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: #ffffff;
    z-index: 2000;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  .editor-topbar {
    height: 60px;
    background: #ffffff;
    border-bottom: 1px solid #e4e6eb;
    display: flex;
    align-items: center;
    padding: 0 24px;
    flex-shrink: 0;
  }

  .topbar-left {
    flex: 1;
    .title-input {
      width: 100%;
      height: 40px;
      border: none;
      outline: none;
      font-size: 24px;
      font-weight: 600;
      color: @textPrimary;
      background: transparent;
      &::placeholder {
        color: #c0c4cc;
        font-weight: 400;
      }
    }
  }

  .topbar-right {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-shrink: 0;
    .draft-save-status {
      font-size: 13px;
      margin-right: 4px;
      &.saving {
        color: #e6a23c;
      }
      &.saved {
        color: #67c23a;
      }
      &.error {
        color: #f56c6c;
      }
    }
    .user-avatar {
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 28px;
      color: @textMuted;
      cursor: pointer;
      margin-left: 8px;
    }
  }

  .editor-content-area {
    flex: 1;
    overflow: auto;
    display: flex;
    .main-editor {
      flex: 1;
      width: 100%;
      min-height: 100%;
    }
  }

  .editor-statusbar {
    height: 30px;
    background: #f7f8fa;
    border-top: 1px solid #e4e6eb;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 24px;
    font-size: 12px;
    color: @textSecondary;
    flex-shrink: 0;
  }

  .status-left {
    display: flex;
    align-items: center;
    .status-item {
      padding: 0 8px;
    }
    .status-divider {
      color: #dcdfe6;
    }
  }

  .status-right {
    display: flex;
    align-items: center;
    gap: 16px;
    .sync-scroll-label {
      display: flex;
      align-items: center;
      gap: 4px;
      cursor: pointer;
      user-select: none;
      input {
        cursor: pointer;
      }
    }
    .back-to-top {
      color: @brandBlue;
      cursor: pointer;
      &:hover {
        text-decoration: underline;
      }
    }
  }

  .publish-drawer {
    :deep(.el-drawer) {
      box-shadow: -4px 0 20px rgba(0,0,0,0.08);
    }
    :deep(.el-drawer__body) {
      padding: 0;
      display: flex;
      flex-direction: column;
      overflow: hidden;
    }
  }

  .publish-drawer-custom {
    :deep(.el-drawer__wrapper) {
      background-color: rgba(0, 0, 0, 0.2);
    }
    :deep(.el-drawer__container) {
      background-color: transparent;
    }
    :deep(.v-modal) {
      background-color: rgba(0, 0, 0, 0.2);
    }
  }

  .drawer-inner {
    display: flex;
    flex-direction: column;
    height: 100%;
  }

  .drawer-header {
    height: 56px;
    padding: 0 24px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    border-bottom: 1px solid #e4e6eb;
    flex-shrink: 0;
    h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      color: @textPrimary;
    }
    .close-drawer {
      font-size: 18px;
      color: @textMuted;
      cursor: pointer;
      padding: 8px;
      &:hover {
        color: @textPrimary;
      }
    }
  }

  .drawer-body {
    flex: 1;
    overflow-y: auto;
    padding: 24px;
  }

  .drawer-section {
    margin-bottom: 28px;
    .section-label {
      display: block;
      font-size: 14px;
      font-weight: 600;
      color: @textPrimary;
      margin-bottom: 12px;
      .required {
        color: @red;
        margin-left: 2px;
      }
      .tag-limit {
        font-weight: 400;
        color: @textMuted;
        font-size: 12px;
        margin-left: 8px;
      }
    }
  }

  .datetime-picker {
    width: 100%;
  }

  .channel-chips {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    :deep(.el-radio-button__inner) {
      border-radius: 4px;
      border: 1px solid #e4e6eb;
      background: #fff;
      color: @textSecondary;
      padding: 8px 16px;
      font-size: 13px;
      box-shadow: none;
      &:hover {
        color: @brandBlue;
        border-color: @brandBlue;
      }
    }
    :deep(.el-radio-button__orig-radio:checked + .el-radio-button__inner) {
      background: @brandBlue;
      border-color: @brandBlue;
      color: #fff;
      box-shadow: none;
    }
    :deep(.el-radio-button:first-child .el-radio-button__inner),
    :deep(.el-radio-button:last-child .el-radio-button__inner) {
      border-radius: 4px;
    }
    :deep(.el-radio-button__inner) {
      border-left: 1px solid #e4e6eb;
    }
  }

  .tag-select, .topic-select {
    width: 100%;
  }

  .cover-type-group {
    margin-bottom: 16px;
    :deep(.el-radio) {
      margin-right: 20px;
    }
  }

  .cover-upload-area {
    width: 200px;
    height: 140px;
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    overflow: hidden;
    transition: all 0.2s;
    background-color: #fafafa;
    &:hover {
      border-color: @brandBlue;
      background-color: #f5f7ff;
    }
    .upload-placeholder {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      .upload-icon {
        font-size: 32px;
        color: @textMuted;
        margin-bottom: 10px;
      }
      .upload-text {
        font-size: 14px;
        color: @textMuted;
        font-weight: 500;
      }
    }
    .cover-preview-img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .cover-upload-three {
    display: flex;
    gap: 12px;
    .three-cover {
      width: 120px;
      height: 85px;
      .upload-placeholder {
        .upload-icon {
          font-size: 22px;
          margin-bottom: 6px;
        }
        .upload-text {
          font-size: 12px;
        }
      }
    }
  }

  .cover-tip {
    font-size: 13px;
    color: @textSecondary;
    margin-top: 10px;
    margin-bottom: 0;
    padding: 6px 10px;
    background: #f7f8fa;
    border-radius: 4px;
    display: inline-block;
  }

  .summary-wrapper {
    position: relative;
    .summary-textarea {
      :deep(.el-textarea__inner) {
        border-radius: 4px;
        padding-bottom: 28px;
        font-size: 13px;
        line-height: 1.6;
      }
    }
    .summary-count {
      position: absolute;
      right: 12px;
      bottom: 8px;
      font-size: 12px;
      color: @textMuted;
    }
  }

  .drawer-footer {
    padding: 16px 24px;
    border-top: 1px solid #e4e6eb;
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    flex-shrink: 0;
  }

  .img_list_con {
    overflow: hidden;
    margin-left: 20px;
    height: 250px;
  }
  .img_list {
    width: 128px;
    height: 100px;
    float: left;
    margin: 0px 20px 20px 0;
    border: 1px solid #eee;
    overflow: hidden;
    border-radius: 4px;
    position: relative;
    img {
      width: 128px;
      height: 100px;
      display: block;
      cursor: pointer;
    }
  }
  .selected {
    width: 60px !important;
    height: 60px !important;
    position: absolute;
    bottom: 0;
    left: 0;
    margin-left: 50%;
    margin-bottom: 50%;
    transform: translate(-30px, 50px);
  }
  .pagination {
    text-align: center;
  }

  .import-dialog {
    :deep(.el-dialog__body) {
      padding: 20px 24px 10px;
    }
    .import-uploader {
      width: 100%;
      :deep(.el-upload) {
        width: 100%;
      }
      :deep(.el-upload-dragger) {
        width: 100%;
        height: 240px;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        background-color: #f7f8fa;
        border: 1px dashed #d9d9d9;
        border-radius: 8px;
        .el-icon-upload {
          font-size: 48px;
          color: #1e80ff;
          margin: 0 0 16px;
          line-height: 1;
        }
        .el-upload__text {
          font-size: 15px;
          color: #515767;
          margin-bottom: 12px;
        }
      }
      :deep(.el-upload__tip) {
        text-align: center;
        font-size: 13px;
        color: #8a93a6;
        line-height: 1.8;
        margin-top: 16px;
      }
    }
  }
</style>
