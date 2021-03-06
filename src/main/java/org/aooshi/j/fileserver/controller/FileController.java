package org.aooshi.j.fileserver.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aooshi.j.fileserver.util.FileUtils;
import org.aooshi.j.util.PathHelper;
import org.aooshi.j.util.StringHelper;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@EnableAutoConfiguration
public class FileController {

    @ResponseBody
    @CrossOrigin
    @PostMapping("/file/upload")
    public String Upload(MultipartFile file, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String bucket = request.getParameter("bucket");
        String path = request.getParameter("path");
        //String json = request.getParameter("json");

        if (file == null) {
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return "No file";
        }

        //原始文件扩展名
        String extension = PathHelper.getExtension(file.getOriginalFilename());
        //路径+随机名.扩展名
        String filePath = "";
        //
        if (StringHelper.isEmpty(path) == false) {
            String regEx = "^\\/(\\w+\\/?)+\\/$";
            Pattern pattern = Pattern.compile(regEx);
            Matcher matcher = pattern.matcher(path);
            boolean rs = matcher.matches();
            if (rs == false) {
                response.setCharacterEncoding("UTF-8");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return "Path invalid";
            }
            filePath = path + extension;
        } else {
            filePath = PathHelper.getSecondPath(extension);
        }

        //随机名.扩展名
        String fileName = PathHelper.getFileNameByPath(filePath);
        //路径
        //String fileDir=filePath.replace(fileName, "");
        String fileDir = PathHelper.getDirectoryByPath(filePath);
        //
        String result = "/" + bucket + "/" + filePath;

        FileUtils fu = new FileUtils();
        fu.Upload(file.getInputStream(), bucket + "/" + fileDir, fileName);

        String accept = request.getHeader("Accept").toLowerCase();
        if (accept.indexOf("json") > -1 || accept.indexOf("javascript") > -1) {
            return "\"" + result + "\"";
        }
        return result;
    }


    @ResponseBody
    @GetMapping("/file/get")
    public void Get(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String path = request.getParameter("path");
        String downloadStr = request.getParameter("download");//0 1
        Integer download = 0;
        if ("1".equals(downloadStr)) {
            //1:下载，其他为显示
            download = 1;
        } else if ("2".equals(downloadStr)) {
            //2:显示
            download = 2;
        }
        String downname = request.getParameter("downname");
        if (null == downname || downname.equals("")) {
            downname = PathHelper.getFileNameByPath(path);
        }

        response.setHeader("Cache-Control", "private");
        response.setHeader("Pragma", "no-cache");

        //fileName = new String(fileName.getBytes("iso8859-1"), "UTF-8");
        FileUtils fu = new FileUtils();
        fu.DownLoad(request, response, path, downname, download);
    }

    @ResponseBody
    @GetMapping("/fileaccess")
    public void Access(HttpServletRequest request, HttpServletResponse response) throws Exception {

        // /a/bucket/path1/path2/filename
        String requestURI = (String) request.getAttribute("RequestURI");
        if (StringHelper.isEmpty(requestURI)) {
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("NOT_FOUND");
            return;
        }

        //String path = request.getParameter("path");
        String path = requestURI.substring(3);
        String downloadStr = request.getParameter("download");//0 1
        Integer download = 0;
        if ("1".equals(downloadStr)) {
            //1:下载，其他为显示
            download = 1;
        } else if ("2".equals(downloadStr)) {
            //2:显示
            download = 2;
        }
        String downname = request.getParameter("downname");
        if (null == downname || downname.equals("")) {
            downname = PathHelper.getFileNameByPath(path);
        }

        //fileName = new String(fileName.getBytes("iso8859-1"), "UTF-8");
        FileUtils fu = new FileUtils();
        fu.DownLoad(request, response, path, downname, download);
    }
}
