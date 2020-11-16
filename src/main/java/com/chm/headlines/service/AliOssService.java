package com.chm.headlines.service;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectRequest;
import com.chm.headlines.util.ToutiaoUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.UUID;

@Service
public class AliOssService {


    private static String endpoint = "oss-cn-beijing.aliyuncs.com";

    //为了账号安全，已隐去具体的的密钥对信息，之前错误提交的代码中包含的密钥对已失效
    private static String accessKeyId = "***Your Key Id***";
    private static String accessKeySecret = "***Your Key Secret***";

    private static String bucketName = "headlines10";
//    private static String key = "*** Provide key ***";

    public String savaImage(MultipartFile file) throws IOException {
        /*
         * Constructs a client instance with your account for accessing OSS
         */
        OSS client = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {

            int dotPos = file.getOriginalFilename().lastIndexOf(".");
            if (dotPos < 0) {
                return null;
            }
            String fileExt = file.getOriginalFilename().substring(dotPos + 1).toLowerCase(); //记得toLowerCase()
            if (!ToutiaoUtil.isFileAllowed(fileExt)) {
                return null;
            }
            //这里的key即待上传文件的文件名
            String key = UUID.randomUUID().toString().replaceAll("-", "")+"."+fileExt;

            /**
             * Note that there are two ways of uploading an object to your bucket, the one
             * by specifying an input stream as content source, the other by specifying a file.
             */

            /*
             * Upload an object to your bucket from an input stream
             */
            client.putObject(bucketName, key, new ByteArrayInputStream(file.getBytes()));

            return ToutiaoUtil.AliOss_DOMAIN_PREFIX + key;

        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message: " + oe.getErrorMessage());
            System.out.println("Error Code:       " + oe.getErrorCode());
            System.out.println("Request ID:      " + oe.getRequestId());
            System.out.println("Host ID:           " + oe.getHostId());

            return  null;
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ce.getMessage());

            return  null;
        } finally {
            /*
             * Do not forget to shut down the client finally to release all allocated resources.
             */
            client.shutdown();
        }
    }
}
