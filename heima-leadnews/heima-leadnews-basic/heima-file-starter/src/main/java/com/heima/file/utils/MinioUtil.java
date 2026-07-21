package com.heima.file.utils;

import com.heima.file.config.MinIOConfig;
import com.heima.model.search.vos.SearchArticleVo;
import com.rabbitmq.client.Channel;
import freemarker.template.Configuration;
import freemarker.template.Template;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Slf4j
public class MinioUtil {

    @Autowired
    private MinIOConfig prop;
    @Autowired
    private MinioClient minioClient;

    private final static String separator = "/";

    /**
     * @param filename yyyy/mm/dd/file.jpg
     */
    public String builderFilePath(String dirPath, String filename) {
        StringBuilder stringBuilder = new StringBuilder(50);
        if (!StringUtils.isEmpty(dirPath)) {
            stringBuilder.append(dirPath).append(separator);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String todayStr = sdf.format(new Date());
        stringBuilder.append(todayStr).append(separator);
        stringBuilder.append(filename);
        return stringBuilder.toString();
    }

    /**
     * 上传图片文件
     *
     * @param prefix 文件前缀
     * @param filename 文件名
     * @param inputStream 文件流
     * @return 文件全路径
     */
    public String uploadImgFile(String prefix, String filename, InputStream inputStream) {
        String filePath = builderFilePath(prefix, filename);
        try {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                .object(filePath)
                .contentType("image/jpg")
                .bucket(prop.getBucket()).stream(inputStream, inputStream.available(), -1)
                .build();
            minioClient.putObject(putObjectArgs);
            StringBuilder urlPath = new StringBuilder(prop.getReadPath());
            urlPath.append(separator + prop.getBucket());
            urlPath.append(separator);
            urlPath.append(filePath);
            return urlPath.toString();
        } catch (Exception ex) {
            log.error("minio put file error.", ex);
            throw new RuntimeException("上传文件失败");
        }
    }

    /**
     * 上传html文件
     *
     * @param prefix 文件前缀
     * @param filename 文件名
     * @param inputStream 文件流
     * @return 文件全路径
     */
    public String uploadHtmlFile(String prefix, String filename, InputStream inputStream) {
        String filePath = builderFilePath(prefix, filename);
        try {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                .object(filePath)
                .contentType("text/html")
                .bucket(prop.getBucket()).stream(inputStream, inputStream.available(), -1)
                .build();
            minioClient.putObject(putObjectArgs);
            StringBuilder urlPath = new StringBuilder(prop.getReadPath());
            urlPath.append(separator + prop.getBucket());
            urlPath.append(separator);
            urlPath.append(filePath);
            return urlPath.toString();
        } catch (Exception ex) {
            log.error("minio put file error.", ex);
            ex.printStackTrace();
            throw new RuntimeException("上传文件失败");
        }
    }

    @Autowired
    private Configuration configuration;

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = "minio.queue", durable = "true"),
        exchange = @Exchange(value = "article.exchange"),
        key = "article.minio.*"
    ))
    public void uploadHtmlFile(Message message, SearchArticleVo vo, Channel channel) {
        HashMap<String,String> resultMap=new HashMap<>();
        resultMap.put("articleId",vo.getId().toString());
        resultMap.put("type","minio");
        try {
            String fileName = vo.getFileName();// yyyy/mm/dd/articleId
            StringWriter out = new StringWriter();
            Template template = configuration.getTemplate("article.ftl");
            //数据模型
            Map<String, Object> contentDataModel = buildDataModel(vo);
            //合成
            template.process(contentDataModel, out);
            // 将HTML内容转换为输入流
            InputStream inputStream = new ByteArrayInputStream(out.toString().getBytes());
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                .object(fileName)
                .contentType("text/html")
                .bucket(prop.getBucket()).stream(inputStream, inputStream.available(), -1)
                .build();
            minioClient.putObject(putObjectArgs);
            resultMap = new HashMap<>();
            resultMap.put("status", "success");
            rabbitTemplate.convertAndSend("process.exchange","process.result",resultMap);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception ex) {
            log.error("minio put file error.", ex);
            resultMap.put("status", "fail");
            rabbitTemplate.convertAndSend("process.exchange","process.result",resultMap);
        }
    }

    /**
     * 构建 Freemarker 数据模型
     */
    private Map<String, Object> buildDataModel(SearchArticleVo vo) {
        Map<String, Object> contentDataModel = new HashMap<>();
        contentDataModel.put("title", vo.getTitle());
        contentDataModel.put("authorName", vo.getAuthorName());
        contentDataModel.put("publishTime", vo.getPublishTime());
        contentDataModel.put("articleId", vo.getId());
        contentDataModel.put("htmlContent", vo.getHtmlContent());
        List<?> tocList = vo.getTocList();
        contentDataModel.put("tocList", tocList != null ? tocList : new ArrayList<>());
        contentDataModel.put("relation", defaultRelation());
        // 作者作品：从 SearchArticleVo 中获取，如果为空则传空列表
        List<?> authorWorks = vo.getAuthorWorks();
        contentDataModel.put("authorWorks", authorWorks != null ? authorWorks : new ArrayList<>());
        return contentDataModel;
    }

    /**
     * 默认的点赞/收藏/关注状态，页面加载后可通过 JS 从接口刷新真实状态
     */
    private Map<String, Object> defaultRelation() {
        Map<String, Object> relation = new HashMap<>();
        relation.put("islike", false);
        relation.put("iscollection", false);
        relation.put("isfollow", false);
        relation.put("isunlike", false);
        return relation;
    }

    /**
     * 删除文件
     *
     * @param pathUrl 文件全路径
     */
    public void delete(String pathUrl) {
        String key = pathUrl.replace(prop.getEndPoint() + "/", "");
        int index = key.indexOf(separator);
        String bucket = key.substring(0, index);
        String filePath = key.substring(index + 1);
        // 删除Objects
        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder().bucket(bucket).object(filePath).build();
        try {
            minioClient.removeObject(removeObjectArgs);
        } catch (Exception e) {
            log.error("minio remove file error.  pathUrl:{}", pathUrl);
            e.printStackTrace();
        }
    }


    /**
     * 下载文件
     *
     * @param pathUrl 文件全路径
     * @return 文件流
     */
    public byte[] downLoadFile(String pathUrl) {
        String key = pathUrl.replace(prop.getEndPoint() + "/", "");
        int index = key.indexOf(separator);
        String bucket = key.substring(0, index);
        String filePath = key.substring(index + 1);
        InputStream inputStream = null;
        try {
            inputStream = minioClient.getObject(
                GetObjectArgs.builder().bucket(prop.getBucket()).object(filePath).build());
        } catch (Exception e) {
            log.error("minio down file error.  pathUrl:{}", pathUrl);
            e.printStackTrace();
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        while (true) {
            try {
                if (!((rc = inputStream.read(buff, 0, 100)) > 0)) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            byteArrayOutputStream.write(buff, 0, rc);
        }
        return byteArrayOutputStream.toByteArray();
    }

//    @Autowired
//    private CodeService codeService;

//    /**
//     * 查看存储bucket是否存在
//     * @return boolean
//     */
//    public Boolean bucketExists(String bucketName) {
//        Boolean found;
//        try {
//            found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//        return found;
//    }
//
//    /**
//     * 创建存储bucket
//     * @return Boolean
//     */
//    public Boolean makeBucket(String bucketName) {
//        try {
//            minioClient.makeBucket(MakeBucketArgs.builder()
//                .bucket(bucketName)
//                .build());
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//        return true;
//    }
//    /**
//     * 删除存储bucket
//     * @return Boolean
//     */
//    public Boolean removeBucket(String bucketName) {
//        try {
//            minioClient.removeBucket(RemoveBucketArgs.builder()
//                .bucket(bucketName)
//                .build());
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//        return true;
//    }
//    /**
//     * 获取全部bucket
//     */
//    public List<Bucket> getAllBuckets() {
//        try {
//            List<Bucket> buckets = minioClient.listBuckets();
//            return buckets;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//
//
//    /**
//     * 文件上传
//     *
//     * @param file 文件
//     * @return Boolean
//     */
//    public String upload(MultipartFile file) {
//        String originalFilename = file.getOriginalFilename();
//        if (StringUtils.isBlank(originalFilename)){
//            throw new RuntimeException();
//        }
//        String fileName = UuidUtils.generateUuid() + originalFilename.substring(originalFilename.lastIndexOf("."));
//        String objectName = CommUtils.getNowDateLongStr("yyyy-MM/dd") + "/" + fileName;
//        try {
//            PutObjectArgs objectArgs = PutObjectArgs.builder().bucket(prop.getBucketName()).object(objectName)
//                .stream(file.getInputStream(), file.getSize(), -1).contentType(file.getContentType()).build();
//            //文件名称相同会覆盖
//            minioClient.putObject(objectArgs);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//        return objectName;
//    }
//
//    /**
//     * 预览图片
//     * @param fileName
//     * @return
//     */
//    public String preview(String fileName){
//        // 查看文件地址
//        GetPresignedObjectUrlArgs build = new GetPresignedObjectUrlArgs().builder().bucket(prop.getBucketName()).object(fileName).method(Method.GET).build();
//        try {
//            String url = minioClient.getPresignedObjectUrl(build);
//            return url;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    /**
//     * 文件下载
//     * @param fileName 文件名称
//     * @param res response
//     * @return Boolean
//     */
//    public void download(String fileName, HttpServletResponse res) {
//        GetObjectArgs objectArgs = GetObjectArgs.builder().bucket(prop.getBucketName())
//            .object(fileName).build();
//        try (GetObjectResponse response = minioClient.getObject(objectArgs)){
//            byte[] buf = new byte[1024];
//            int len;
//            try (FastByteArrayOutputStream os = new FastByteArrayOutputStream()){
//                while ((len=response.read(buf))!=-1){
//                    os.write(buf,0,len);
//                }
//                os.flush();
//                byte[] bytes = os.toByteArray();
//                res.setCharacterEncoding("utf-8");
//                // 设置强制下载不打开
//                // res.setContentType("application/force-download");
//                res.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
//                try (ServletOutputStream stream = res.getOutputStream()){
//                    stream.write(bytes);
//                    stream.flush();
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 查看文件对象
//     * @return 存储bucket内文件对象信息
//     */
//    public List<Item> listObjects() {
//        Iterable<Result<Item>> results = minioClient.listObjects(
//            ListObjectsArgs.builder().bucket(prop.getBucketName()).build());
//        List<Item> items = new ArrayList<>();
//        try {
//            for (Result<Item> result : results) {
//                items.add(result.get());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//        return items;
//    }
//
//    /**
//     * 删除
//     * @param fileName
//     * @return
//     * @throws Exception
//     */
//    public boolean remove(String fileName){
//        try {
//            minioClient.removeObject( RemoveObjectArgs.builder().bucket(prop.getBucketName()).object(fileName).build());
//        }catch (Exception e){
//            return false;
//        }
//        return true;
//    }

}
