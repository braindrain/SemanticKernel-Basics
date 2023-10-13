package soham.sksamples;

import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.SKBuilders;
import com.microsoft.semantickernel.exceptions.ConfigurationException;
import com.microsoft.semantickernel.orchestration.SKContext;
import com.microsoft.semantickernel.semanticfunctions.PromptTemplateConfig;
import com.microsoft.semantickernel.textcompletion.CompletionSKFunction;
import org.slf4j.Logger;
import reactor.core.publisher.Mono;

import static soham.sksamples.util.Constants.TextToSummarize;
import static soham.sksamples.util.KernelUtils.*;

public class Example01_InlineFunction {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(Example01_InlineFunction.class);

    public static void main(String[] args) {
        try {
            log.debug("== Instantiates the Kernel ==");
            Kernel kernel = openAIKernel();
            log.debug("== Define inline function ==");
            String semanticFunctionInline = """
                {{$input}}
                
                Summarize the content above in less than 140 characters.
                """;
            CompletionSKFunction summarizeFunction = SKBuilders
                    .completionFunctions()
                    .withKernel(kernel)
                    .withPromptTemplate(semanticFunctionInline)
                    .withCompletionConfig(
                            new PromptTemplateConfig.CompletionConfigBuilder()
                                    .maxTokens(100)
                                    .temperature(0.4)
                                    .topP(1)
                                    .build()).build();

            log.debug("== Run the Kernel ==");
            Mono<SKContext> result = summarizeFunction.invokeAsync(TextToSummarize);

            log.debug("== Result ==");
            log.debug(result.block().getResult());

        } catch (ConfigurationException | NullPointerException e) {
            log.error("Problem in paradise", e);
        }
    }
}
