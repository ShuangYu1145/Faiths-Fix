package dev.faiths;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import dev.faiths.utils.ReaderUtils;

import java.io.File;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.zip.ZipFile;

public class GitVersion {
    public static String VERSION = "";

    static {
        try {
            ZipFile zipFile = new ZipFile(new File(GitVersion.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
            Properties appProps = new Properties();
            appProps.load(ZipUtil.get(zipFile, "git.properties"));
            VERSION = appProps.getProperty("git.branch", "unknown") + "/" + appProps.getProperty("git.commit.id.abbrev", "unknown");
        } catch (Exception e2) {
            e2.printStackTrace();
            try {
                VERSION = FileUtil.readUtf8String(new File("../.git/refs/heads/main")).substring(0, 7);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
