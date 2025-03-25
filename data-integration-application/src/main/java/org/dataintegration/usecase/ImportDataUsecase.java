package org.dataintegration.usecase;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.dataintegration.model.DelimiterModel;
import org.dataintegration.service.ImportDataService;
import org.dataintegration.service.ProjectsService;
import org.dataintegration.service.S3Service;
import org.dataintegration.utils.DataIntegrationUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;
import java.util.concurrent.Callable;

@Component
@RequiredArgsConstructor
public class ImportDataUsecase {

    private final ProjectsService projectsService;
    private final S3Service s3Service;
    private final ImportDataService importDataService;

    @Async
    public void importFromFile(byte[] bytes, UUID projectId, UUID scopeId, char delimiter, String createdBy) {
        projectsService.isPermitted(projectId, createdBy);
        final Callable<CSVReader> csvReaderCallable = () -> {
            final ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            final BOMInputStream bomInputStream = BOMInputStream.builder()
                    .setInputStream(inputStream)
                    .setInclude(false).setByteOrderMarks(ByteOrderMark.UTF_8)
                    .get();
            final InputStreamReader inputStreamReader = new InputStreamReader(bomInputStream);
            return new CSVReaderBuilder(inputStreamReader)
                    .withCSVParser(new CSVParserBuilder()
                            .withSeparator(delimiter)
                            .build()
                    ).build();
        };
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)))) {
            final long lineCount = reader.lines().count() - 1;
            importDataService.importData(csvReaderCallable, projectId, scopeId, lineCount);
        } catch (IOException ex) {
            throw new IllegalStateException();
        }
    }

    @Async
    public void importFromS3(UUID scopeId, String bucket, String key, String createdBy) {
        final UUID projectId = DataIntegrationUtils.getProjectIdFromS3Key(key);
        projectsService.isPermitted(projectId, createdBy);
        final long lineCount = Long.parseLong(s3Service.getS3ObjectTag(bucket, key, "lineCount"));
        final char delimiter =
                DelimiterModel.toCharacter(DelimiterModel.valueOf(s3Service.getS3ObjectTag(bucket, key, "delimiter")));
        final Callable<CSVReader> csvReaderCallable = () -> {
            final ResponseInputStream<GetObjectResponse> inputStream = s3Service.getS3Object(bucket, key);
            final BOMInputStream bomInputStream = BOMInputStream.builder()
                    .setInputStream(inputStream)
                    .setInclude(false).setByteOrderMarks(ByteOrderMark.UTF_8)
                    .get();
            final InputStreamReader inputStreamReader = new InputStreamReader(bomInputStream);
            return new CSVReaderBuilder(inputStreamReader)
                    .withCSVParser(new CSVParserBuilder()
                            .withSeparator(delimiter)
                            .build()
                    ).build();
        };
        final boolean success =
                importDataService.importData(csvReaderCallable, projectId, scopeId, lineCount);
        if (success) {
            s3Service.deleteObject(bucket, key);
        }
    }

}
