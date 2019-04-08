package com.envimate.httpmate.chains.builder;

import com.envimate.httpmate.chains.ChainName;
import com.envimate.httpmate.chains.rules.Processor;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

import static com.envimate.httpmate.util.Validators.validateNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public final class ChainBuilderEntry {
    private final ChainName chainName;
    private final List<? extends Processor> processors;

    public static ChainBuilderEntry chainBuilderEntry(final ChainName chainName,
                                                      final List<? extends Processor> processors) {
        validateNotNull(chainName, "chainName");
        validateNotNull(processors, "processors");
        return new ChainBuilderEntry(chainName, processors);
    }

    public ChainName chainName() {
        return chainName;
    }

    public List<? extends Processor> processors() {
        return processors;
    }
}
