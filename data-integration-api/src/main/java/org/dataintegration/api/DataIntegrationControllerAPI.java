package org.dataintegration.api;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dataintegration.model.DataIntegrationAPIModel;
import org.dataintegration.model.DataIntegrationEndpointsAPIModel;
import org.dataintegration.model.DataIntegrationHeaderAPIModel;
import org.dataintegration.model.DataIntegrationInputAPIModel;

/**
 * API for data integration REST controller.
 * Provides which endpoint implementations need to be considered. Default implementations are included.
 * <p>
 * <b>Example implementation for spring:</b>
 * <pre>
 * {@code
 * @RestController
 * @RequestMapping("/example/spring")
 * public class ExampleSpringRestController extends DataIntegrationControllerAPI<String> {
 *
 *     public ExampleSpringRestController(DataIntegrationEndpointsAPI dataIntegrationEndpointsAPI, DataIntegrationAPI<String> dataIntegrationAPI) {
 *         super(dataIntegrationEndpointsAPI, dataIntegrationAPI);
 *     }
 *
 *     @GetMapping
 *     @Override
 *     public DataIntegrationEndpointsAPIModel endpointsRestCall() {
 *         return super.defaultEndpointsRestCall();
 *     }
 *
 *     @PostMapping
 *     @Override
 *     public DataIntegrationAPIModel integrationRestCall(@RequestBody List<String> context) {
 *         return super.defaultIntegrationRestCall(context);
 *     }
 *
 * }
 * }
 * </pre>
 */
@Getter(AccessLevel.PROTECTED)
@RequiredArgsConstructor
public abstract class DataIntegrationControllerAPI {

    private final DataIntegrationEndpointsAPI dataIntegrationEndpointsAPI;
    private final DataIntegrationHeaderAPI dataIntegrationHeaderAPI;
    private final DataIntegrationAPI dataIntegrationAPI;

    public abstract DataIntegrationEndpointsAPIModel endpointsRestCall();
    protected DataIntegrationEndpointsAPIModel defaultEndpointsRestCall(String getHeadersPath, String integrationPath) {
        return dataIntegrationEndpointsAPI.listEndpoints(getHeadersPath, integrationPath);
    }

    public abstract DataIntegrationHeaderAPIModel getHeadersRestCall();
    protected DataIntegrationHeaderAPIModel defaultGetHeadersRestCall() {
        return dataIntegrationHeaderAPI.getHeaders();
    }

    public abstract DataIntegrationAPIModel integrationRestCall(String database, DataIntegrationInputAPIModel dataIntegrationInput);
    protected DataIntegrationAPIModel defaultIntegrationRestCall(String database, DataIntegrationInputAPIModel dataIntegrationInput) {
        return dataIntegrationAPI.doIntegration(database, dataIntegrationInput);
    }

}
