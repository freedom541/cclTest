package com.ccl.SHA;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by ccl on 16/11/18.
 */
public class EncrypSHA {

    public byte[] eccrypt(String info) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("SHA");
        byte[] srcBytes = info.getBytes();
        // 使用srcBytes更新摘要
        md5.update(srcBytes);
        // 完成哈希计算，得到result
        byte[] resultBytes = md5.digest();
        return resultBytes;
    }
}

