package com.agentic.demo.service;

import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.grpc.GetCollectionStatisticsResponse;
import io.milvus.grpc.SearchResultData;
import io.milvus.grpc.SearchResults;
import io.milvus.param.ConnectParam;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.collection.*;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.R;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.logging.Logger;

@Service
public class MilvusService {

    private static final Logger logger = Logger.getLogger(MilvusService.class.getName());
    private final MilvusServiceClient client;

    @Value("${milvus.collection.dimension:768}")
    private int dimension;

    public MilvusService(@Value("${milvus.host}") String host, @Value("${milvus.port}") int port) {
        logger.info("Connecting to Milvus at " + host + ":" + port);
        ConnectParam connectParam = ConnectParam.newBuilder()
                .withHost(host)
                .withPort(port)
                .build();
        this.client = new MilvusServiceClient(connectParam);
        logger.info("Milvus client created successfully");
    }

    public void ensureCollection(String collectionName) {
        logger.info("Checking if collection exists: " + collectionName);
        try {
            R<Boolean> hasCollResp = client.hasCollection(HasCollectionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .build());

            logger.info("hasCollection response code: " + hasCollResp.getStatus());

            if (hasCollResp.getException() != null) {
                logger.severe("Exception checking collection: " + hasCollResp.getException().getMessage());
                hasCollResp.getException().printStackTrace();
                throw new RuntimeException("Failed to check collection: " + hasCollResp.getException().getMessage());
            }

            Boolean collectionExists = hasCollResp.getData();
            logger.info("Collection exists: " + collectionExists);

            if (collectionExists == null || !collectionExists) {
                logger.info("Creating collection: " + collectionName);
                createCollection(collectionName);
            }
        } catch (Exception e) {
            logger.severe("Error ensuring collection: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error ensuring collection: " + e.getMessage(), e);
        }
    }

    private void createCollection(String collectionName) {
        List<FieldType> fields = new ArrayList<>();
        fields.add(FieldType.newBuilder()
                .withName("id")
                .withDataType(DataType.Int64)
                .withPrimaryKey(true)
                .withAutoID(true)
                .build());
        fields.add(FieldType.newBuilder()
                .withName("embedding")
                .withDataType(DataType.FloatVector)
                .withDimension(dimension)
                .build());
        fields.add(FieldType.newBuilder()
                .withName("filename")
                .withDataType(DataType.VarChar)
                .withMaxLength(256)
                .build());
        fields.add(FieldType.newBuilder()
                .withName("path")
                .withDataType(DataType.VarChar)
                .withMaxLength(1024)
                .build());
        fields.add(FieldType.newBuilder()
                .withName("content")
                .withDataType(DataType.VarChar)
                .withMaxLength(1024)
                .build());
        CreateCollectionParam createCollectionReq = CreateCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .withFieldTypes(fields)
                .withShardsNum(1)
                .build();

        R<?> createResp = client.createCollection(createCollectionReq);

        if (createResp.getException() != null) {
            logger.severe("Failed to create collection: " + createResp.getException().getMessage());
            throw new RuntimeException("Failed to create collection: " + createResp.getException().getMessage());
        }

        logger.info("Collection created successfully: " + collectionName);
    }

    public String insertVector(String collectionName,  List<Float> embedding, Map<String,String> metadata) {
        logger.info("Inserting vector into collection: " + collectionName);
        ensureCollection(collectionName);

        List<InsertParam.Field> fields = new ArrayList<>();
        fields.add(new InsertParam.Field("embedding", Collections.singletonList(embedding)));
        fields.add(new InsertParam.Field("content",Collections.singletonList(metadata.getOrDefault("content", ""))));
        fields.add(new InsertParam.Field("filename", Collections.singletonList(metadata.getOrDefault("filename", ""))));
        fields.add(new InsertParam.Field("path", Collections.singletonList(metadata.getOrDefault("path", ""))));

        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName(collectionName)
                .withFields(fields)
                .build();

        R<?> res = client.insert(insertParam);

        if (res.getException() != null) {
            logger.severe("Insert failed: " + res.getException().getMessage());
            throw new RuntimeException("Insert failed: " + res.getException().getMessage());
        }
        client.flush(
                FlushParam.newBuilder()
                        .withCollectionNames(Collections.singletonList(collectionName))
                        .build()
        );
        CreateIndexParam indexParam = CreateIndexParam.newBuilder()
                .withCollectionName("documents")
                .withFieldName("embedding")   // must match your vector field name
                .withIndexType(IndexType.IVF_FLAT) // or IVF_SQ8, HNSW, etc.
                .withMetricType(MetricType.COSINE) // or L2, IP depending on your use case
                .withExtraParam("{\"nlist\":1024}") // index-specific params
                .build();

        client.createIndex(indexParam);

        client.loadCollection(
                LoadCollectionParam.newBuilder()
                        .withCollectionName("documents")
                        .build()
        );

        R<GetCollectionStatisticsResponse> count = client.getCollectionStatistics(
                GetCollectionStatisticsParam.newBuilder()
                        .withCollectionName(collectionName)
                        .build()
        );
        System.out.println("Row count: " + count.getData());

        logger.info("Vector inserted successfully into collection: " + collectionName);
        return "Inserted into collection: " + collectionName;
    }
    public  R<SearchResults> searchDocuments(String collectionName, List<Float> queryVector, int topK) {
        // Build search parameters

        SearchParam searchParam = SearchParam.newBuilder()
                .withCollectionName(collectionName)
                .withVectorFieldName("embedding")   // must match schema
                .withMetricType(MetricType.COSINE)
                .withTopK(topK)
                .withVectors(Collections.singletonList(queryVector))
                .withOutFields(Arrays.asList("id", "content")) // return text chunks
                .build();

        R<SearchResults> result = client.search(searchParam);


        return result;
    }
}