package xyz.zcraft.ACGPicDownload.Util.FetchUtil.DownloadUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import xyz.zcraft.ACGPicDownload.Util.FetchUtil.Result;

public class DownloadUtil {
    public void download(Result r, File toDic, DownloadResult d) throws IOException {
        if (!toDic.exists() && !toDic.mkdirs()) {
            if(d!=null){
                d.setStatus(DownloadStatus.FAILED);
            }else{
                throw new IOException("Can't create directory");
            }  
        }

        URL url = new URL(r.getUrl());
        URLConnection c = url.openConnection();
        InputStream is = c.getInputStream();

        FileOutputStream fos = new FileOutputStream(new File(toDic, r.getFileName()));

        byte[] buffer = new byte[1];
        int byteRead;
        int total = 0;

        if(d != null){
            d.setTotalSize(c.getContentLengthLong());
            d.setStatus(DownloadStatus.STARTED);
        }

        while ((byteRead = is.read(buffer)) != -1) {
            total += byteRead;
            fos.write(buffer, 0, byteRead);
            if(d != null){
                d.setSizeDownloaded(total);
            }
        }

        is.close();
        fos.close();

        if(d != null){
            d.setStatus(DownloadStatus.COMPLETED);
        }
    }

    public void download(Result r, File toDic) throws IOException {
        download(r, toDic, null);
    }
}