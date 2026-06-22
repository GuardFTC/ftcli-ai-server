package com.ftc.ftcli.infra.chroma;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 冯铁城 [17615007230@163.com]
 * @date 2026-06-22 17:21:50
 * @describe 向量记录操作
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class RecordRepository {

    private final ChromaUrlBuilder urlBuilder;

    /**
     * 获取文档片段列表
     *
     * @param collectionId 集合ID
     * @param fileNameMd5  文件名MD5
     * @param offset       偏移量
     * @param size         每页条数
     * @return 文档片段列表
     */
    public List<Map<String, Object>> getChunks(String collectionId, String fileNameMd5, int offset, int size) {

        //1.获取查询集合向量记录URL
        String getUrl = urlBuilder.getUrl(collectionId);
        try {

            //2.构建Chroma查询请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("include", List.of("documents", "metadatas"));
            requestBody.put("limit", size);
            requestBody.put("offset", offset);
            JSONObject where = new JSONObject();
            where.put("file_name_md5", fileNameMd5);
            requestBody.put("where", where);

            //3.发起请求
            String resp = HttpUtil.post(getUrl, requestBody.toJSONString());
            JSONObject result = JSON.parseObject(resp);

            //4.解析片段列表
            JSONArray ids = result.getJSONArray("ids");
            JSONArray documents = result.getJSONArray("documents");
            JSONArray metadatas = result.getJSONArray("metadatas");

            //5.组装片段数据
            List<Map<String, Object>> chunks = new ArrayList<>();
            for (int i = 0; i < ids.size(); i++) {
                Map<String, Object> chunk = new LinkedHashMap<>();
                chunk.put("id", ids.getString(i));
                chunk.put("document", documents.getString(i));
                chunk.put("metadata", metadatas.getJSONObject(i));
                chunks.add(chunk);
            }

            //6.返回片段列表
            return chunks;
        } catch (Exception e) {
            log.error("[Chroma] 获取文档片段 失败 文档名MD5:[{}]", fileNameMd5, e);
            return List.of();
        }
    }

    /**
     * 获取指定文档的片段总数
     *
     * @param collectionId 集合ID
     * @param fileNameMd5  文件名MD5
     * @return 片段总数
     */
    public int getChunkCount(String collectionId, String fileNameMd5) {

        //1.获取查询集合向量记录URL
        String getUrl = urlBuilder.getUrl(collectionId);

        //2.构建计数请求体（不限制数量，只取ID用于计数）
        JSONObject requestBody = new JSONObject();
        requestBody.put("include", List.of());
        JSONObject where = new JSONObject();
        where.put("file_name_md5", fileNameMd5);
        requestBody.put("where", where);

        //3.发起请求
        String resp = HttpUtil.post(getUrl, requestBody.toJSONString());
        JSONObject result = JSON.parseObject(resp);

        //4.解析ID数组长度作为总数
        JSONArray ids = result.getJSONArray("ids");
        return ids != null ? ids.size() : 0;
    }
}
