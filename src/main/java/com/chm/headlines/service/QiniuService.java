package com.chm.headlines.service;

import com.chm.headlines.util.ToutiaoUtil;
import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.UploadManager;
import com.qiniu.common.Zone;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class QiniuService {
    public static final Logger logger = LoggerFactory.getLogger(QiniuService.class);

    //构造一个带指定Zone对象的配置类
    Configuration cfg = new Configuration(Zone.zone0());
//...其他参数参考类注释

    UploadManager uploadManager = new UploadManager(cfg);
    //...生成上传凭证，然后准备上传
    String accessKey = "Emb_NCCNq71Rt9FkegXerJCt1B4mG84GXXzYYNvi";
    String secretKey = "dFi76xNy4FhcagQdozF1qdSBh0DXlTS_2ELMpSNs";
    String bucket = "headlines40";
    //如果是Windows情况下，格式是 D:\\qiniu\\test.png
//    String localFilePath = "/home/qiniu/test.png";
    //默认不指定key的情况下，以文件内容的hash值作为文件名
//    String key = null;

    Auth auth = Auth.create(accessKey, secretKey);
    String upToken = auth.uploadToken(bucket);

    public String savaImage(MultipartFile file) throws IOException {
        try {
            int dotPos = file.getOriginalFilename().lastIndexOf(".");
            if (dotPos < 0) {
                return null;
            }
            String fileExt = file.getOriginalFilename().substring(dotPos + 1).toLowerCase(); //记得toLowerCase()
            if (!ToutiaoUtil.isFileAllowed(fileExt)) {
                return null;
            }
            String fileName = UUID.randomUUID().toString().replaceAll("-", "")+"."+fileExt;

            Response response = uploadManager.put(file.getBytes(), fileName, upToken);

            //Files.copy(file.getInputStream());

            //解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            System.out.println(putRet.key);     //返回的key即云端该图片的文件名（是上面的UUID随机生成的文件名）
            System.out.println(putRet.hash);
            if (response.isOK() && response.isJson()) {
                return ToutiaoUtil.QINIU_DOMAIN_PREFIX + fileName;
            } else {
                logger.error("七牛异常:" + response.bodyString());
                return null;
            }
        } catch (QiniuException e) {
            logger.error("七牛异常:" + e.getMessage());
            return null;
        }
    }
}
