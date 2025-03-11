package org.dataintegration.api;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dataintegration.model.DataIntegrationAPIModel;
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

    private final DataIntegrationHeaderAPI dataIntegrationHeaderAPI;
    private final DataIntegrationAPI dataIntegrationAPI;

    public abstract DataIntegrationHeaderAPIModel getHeadersRestCall(String language);

    protected DataIntegrationHeaderAPIModel defaultGetHeadersRestCall(String language) {
        return dataIntegrationHeaderAPI.getHeaders(language);
    }

    public abstract DataIntegrationAPIModel integrationRestCall(String database, String language,
                                                                DataIntegrationInputAPIModel dataIntegrationInput);

    protected DataIntegrationAPIModel defaultIntegrationRestCall(String database, String language,
                                                                 DataIntegrationInputAPIModel dataIntegrationInput) {
        return dataIntegrationAPI.doIntegration(database, language, dataIntegrationInput);
    }

}
