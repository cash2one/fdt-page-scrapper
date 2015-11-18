package com.ubs.opsit.interviews.support;

import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.io.LoadFromURL;
import org.jbehave.core.reporters.FilePrintStreamFactory;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.ParameterConverters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;
import static org.jbehave.core.reporters.Format.CONSOLE;
import static org.jbehave.core.reporters.Format.HTML;

/**
 * A class to fully encapsulates all of the JBehave plumbing behind a builder style API.  The expected use for this would be:
 * {code}aBehaviouralTestRunner().usingStepsFrom(this).withStory("your.story").run(){code}
 */
public final class BehaviouralTestEmbedder extends ConfigurableEmbedder {

    private static final Logger LOG = LoggerFactory.getLogger(BehaviouralTestEmbedder.class);
    public static final String BAD_USE_OF_API_MESSAGE = "You are trying to set the steps factory twice ... this is a paradox";

    private String wildcardStoryFilename;
    private InjectableStepsFactory stepsFactory;


    private BehaviouralTestEmbedder() {
    }

    public static BehaviouralTestEmbedder aBehaviouralTestRunner() {
        return new BehaviouralTestEmbedder();
    }

    public void run() throws Exception {
        List<String> paths = createStoryPaths();
        if (paths == null || paths.isEmpty()) {
            throw new IllegalStateException("No story paths found for state machine");
        }
        LOG.debug("Running {} with spring_stories {}", this.getClass().getSimpleName(), paths);
        configuredEmbedder().runStoriesAsPaths(paths);
    }

    @Override
    public InjectableStepsFactory stepsFactory() {
        assertThat(stepsFactory).isNotNull();
        return stepsFactory;
    }

    public Configuration configuration() {
        return new MostUsefulConfiguration()
                .useStoryLoader(new LoadFromURL())
                .useParameterConverters(new ParameterConverters().addConverters(new SandboxDateConverter()))
                .useStoryReporterBuilder(new SandboxStoryReporterBuilder());
    }

    private List<String> createStoryPaths() {
        return ClasspathStoryFinder.findFilenamesThatMatch(wildcardStoryFilename);
    }

    public BehaviouralTestEmbedder withStory(String aWildcardStoryFilename) {
        wildcardStoryFilename = aWildcardStoryFilename;
        return this;
    }

    public BehaviouralTestEmbedder usingStepsFrom(Object... stepsSource) {
        assertThat(stepsFactory).isNull();
        stepsFactory = new InstanceStepsFactory(configuration(), stepsSource);
        return this;
    }


    static class SandboxDateConverter extends ParameterConverters.DateConverter {

        public SandboxDateConverter() {
            super(new SimpleDateFormat("dd-MM-yyyy"));
        }
    }

    static class SandboxStoryReporterBuilder extends StoryReporterBuilder {

        public SandboxStoryReporterBuilder() {
            withCodeLocation(codeLocationFromClass(SandboxStoryReporterBuilder.class));
            withDefaultFormats();
            withFormats(HTML, CONSOLE);
            withFailureTrace(true);
            withPathResolver(new FilePrintStreamFactory.ResolveToSimpleName());
        }
    }
}
