package soham.sksamples.util;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.NonAzureOpenAIKeyCredential;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.ai.embeddings.EmbeddingGeneration;
import com.microsoft.semantickernel.SKBuilders;
import com.microsoft.semantickernel.connectors.ai.openai.util.OpenAISettings;
import com.microsoft.semantickernel.connectors.ai.openai.util.SettingsMap;
import com.microsoft.semantickernel.exceptions.ConfigurationException;
import com.microsoft.semantickernel.memory.MemoryQueryResult;
import com.microsoft.semantickernel.memory.VolatileMemoryStore;
import com.microsoft.semantickernel.textcompletion.TextCompletion;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class KernelUtils {

    public static Kernel openAIKernel() throws ConfigurationException {
        TextCompletion textCompletion = SKBuilders.textCompletion()
                .withModelId("text-davinci-003")
                .withOpenAIClient(openAIAsyncClient())
                .build();
        return SKBuilders.kernel()
                .withDefaultAIService(textCompletion)
                .build();
    }

    public static Kernel openAIKernelWithEmbedding() throws ConfigurationException, IOException {
        EmbeddingGeneration<String> textEmbeddingGenerationService =
                SKBuilders.textEmbeddingGeneration()
                        .withOpenAIClient(openAIAsyncClient())
                        .withModelId("text-embedding-ada-002")
                        .build();
        return SKBuilders.kernel()
                .withDefaultAIService(textEmbeddingGenerationService)
                .withMemoryStorage(new VolatileMemoryStore.Builder().build())
                .build();
    }

    private static OpenAIAsyncClient openAIAsyncClient() throws ConfigurationException {
        return new OpenAIClientBuilder().credential(new NonAzureOpenAIKeyCredential(openAISettings().getKey())).buildAsyncClient();
    }

    private static OpenAISettings openAISettings() throws ConfigurationException {
        return new OpenAISettings(SettingsMap.
                getWithAdditional(List.of(
                        new File("src/main/resources/conf.properties"))));
    }

    public static Mono<Void> storeInKernelMemory(Kernel kernel, Map<String, String> data, String collectionName) {
        return Flux.fromIterable(data.entrySet())
                .map(entry -> kernel.getMemory().saveInformationAsync(
                        collectionName,
                        entry.getValue(),
                        entry.getKey(),
                        "skdocs",
                        null)
                )
                .mapNotNull(Mono::block)
                .then();
    }

    public static Mono<List<MemoryQueryResult>> searchMemory(Kernel kernel, String query, String collectionName) {
        return kernel.getMemory()
                .searchAsync(collectionName,
                        query, 2, 0.7f, false);
    }
}

